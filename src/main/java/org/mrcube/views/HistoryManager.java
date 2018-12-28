/*
 * Project Name: MR^3 (Meta-Model Management based on RDFs Revision Reflection)
 * Project Website: http://mrcube.org/
 * 
 * Copyright (C) 2003-2018 Yamaguchi Laboratory, Keio University. All rights reserved.
 * 
 * This file is part of MR^3.
 * 
 * MR^3 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * MR^3 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with MR^3.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */

package org.mrcube.views;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.jgraph.graph.GraphCell;
import org.jgraph.graph.GraphConstants;
import org.mrcube.MR3;
import org.mrcube.io.MR3Reader;
import org.mrcube.io.MR3Writer;
import org.mrcube.jgraph.GraphManager;
import org.mrcube.jgraph.RDFGraph;
import org.mrcube.models.*;
import org.mrcube.models.MR3Constants.HistoryType;
import org.mrcube.utils.Utilities;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.*;
import java.util.logging.*;

/**
 * @author Takeshi Morita
 */
public class HistoryManager extends JDialog implements ActionListener {

	private static MR3Reader mr3Reader;
	private static Map<Date, HistoryModel> dateHistoryDataMap;
	private static DefaultTableModel historyTableModel;
	private static JTable historyTable;

	private static GraphManager gmanager;

	private JButton applyButton;
	private JButton cancelButton;

	private static final int WINDOW_WIDTH = 400;
	private static final int WINDOW_HEIGHT = 400;

	public static final String DEFAULT_LOG_FILE_NAME = "mr3_log.txt";

	private static Logger logger;

	public static class CustomLogFormatter extends SimpleFormatter {
		private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

		public String format(LogRecord logRecord) {
			final StringBuilder stringBuffer = new StringBuilder();

			stringBuffer.append(this.dateFormat.format(new Date(logRecord.getMillis())));
			stringBuffer.append(" ");

			Level level = logRecord.getLevel();
			if (level == Level.FINEST) {
				stringBuffer.append("FINEST");
			} else if (level == Level.FINER) {
				stringBuffer.append("FINER ");
			} else if (level == Level.FINE) {
				stringBuffer.append("FINE ");
			} else if (level == Level.CONFIG) {
				stringBuffer.append("CONFIG");
			} else if (level == Level.INFO) {
				stringBuffer.append("INFO ");
			} else if (level == Level.WARNING) {
				stringBuffer.append("WARN ");
			} else if (level == Level.SEVERE) {
				stringBuffer.append("SEVERE");
			} else {
				stringBuffer.append(logRecord.getLevel().intValue());
				stringBuffer.append(" ");
			}
			stringBuffer.append(" ");
			stringBuffer.append(logRecord.getLoggerName());
			stringBuffer.append(" - ");
			stringBuffer.append(logRecord.getMessage());
			stringBuffer.append(System.lineSeparator());

			return stringBuffer.toString();
		}
	}

	public static void initLogger(String logFilePath) {
		logger = Logger.getGlobal();
		logger.setLevel(Level.INFO);
		if (GraphManager.isLogAvailable()) {
			try {
				FileHandler fileHandler = new FileHandler(logFilePath, true);
				fileHandler.setFormatter(new CustomLogFormatter());
				logger.addHandler(fileHandler);
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}
		}
	}

	public static void resetFileAppender(String logFilePath) {
		if (!logFilePath.equals("")) {
			if (GraphManager.isLogAvailable()) {
				initLogger(logFilePath);
			}
		}
	}

	private static final Object[] columnNames = new Object[] { "Date", "History Type" };

	public HistoryManager(Frame root, MR3 mr3) {
		super(root, "HistoryManager");
		mr3Reader = mr3.getMR3Reader();
		MR3Writer mr3Writer = mr3.getMR3Writer();
		dateHistoryDataMap = new HashMap<>();
		historyTableModel = new DefaultTableModel(columnNames, 0);
		historyTable = new JTable(historyTableModel);
		TableColumnModel tcModel = historyTable.getColumnModel();
		tcModel.getColumn(0).setPreferredWidth(50);
		tcModel.getColumn(1).setPreferredWidth(100);
		historyTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		JScrollPane historyTableScroll = new JScrollPane(historyTable);

		applyButton = new JButton(MR3Constants.APPLY);
		applyButton.setMnemonic('a');
		applyButton.addActionListener(this);
		cancelButton = new JButton(MR3Constants.CANCEL);
		cancelButton.setMnemonic('c');
		cancelButton.addActionListener(this);
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new GridLayout(1, 2, 5, 5));
		buttonPanel.add(applyButton);
		buttonPanel.add(cancelButton);

		add(historyTableScroll, BorderLayout.CENTER);
		add(Utilities.createEastPanel(buttonPanel), BorderLayout.SOUTH);

		setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
		setLocationRelativeTo(root);
		setVisible(false);
	}

	public static void setGraphManager(GraphManager gm) {
		gmanager = gm;
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == applyButton) {
			loadHistory();
		} else if (e.getSource() == cancelButton) {
			setVisible(false);
		}
	}

	private static final String MODEL = "[Model]";
	private static final String META_MODEL = "[Meta Model]";

	public synchronized static void saveHistory(HistoryType historyType, RDFSModel info, GraphCell sourceCell,
												GraphCell targetCell) {
		if (!GraphManager.isLogAvailable()) {
			return;
		}
		switch (historyType) {
		case INSERT_PROPERTY:
			RDFResourceModel resInfo = (RDFResourceModel) GraphConstants.getValue(sourceCell.getAttributes());
			String sourceStr = "Source: RDF Resource: " + resInfo.getURIStr() + "\tRDF Resource Type: "
					+ resInfo.getType() + "\n";
			String targetStr = "";
			if (RDFGraph.isRDFLiteralCell(targetCell)) {
				MR3Literal literal = (MR3Literal) GraphConstants.getValue(targetCell.getAttributes());
				targetStr = "Target: Language: " + literal.getLanguage() + "\tDatatype: " + literal.getDatatype()
						+ "\tString: " + literal.getString() + "\n";
			} else {
				resInfo = (RDFResourceModel) GraphConstants.getValue(targetCell.getAttributes());
				targetStr = "Target: RDF Resource: " + resInfo.getURIStr() + "\tRDF Resource Type: "
						+ resInfo.getType() + "\n";
			}
			logger.info(MODEL + "[" + historyType + "]\n" + "Insert Property: " + info.getURIStr() + "\n" + sourceStr
					+ targetStr);
			break;
		case CONNECT_SUP_SUB_CLASS:
			RDFSModel subInfo = (RDFSModel) GraphConstants.getValue(sourceCell.getAttributes());
			String subStr = "Sub Class: " + subInfo.getURIStr() + "\tMeta Class: " + subInfo.getMetaClass() + "\n";
			RDFSModel supInfo = (RDFSModel) GraphConstants.getValue(targetCell.getAttributes());
			String supStr = "Super Class: " + supInfo.getURIStr() + "\tMeta Class: " + supInfo.getMetaClass() + "\n";
			logger.info(META_MODEL + "[" + historyType + "]\n" + subStr + supStr);
			break;
		case CONNECT_SUP_SUB_PROPERTY:
			subInfo = (RDFSModel) GraphConstants.getValue(sourceCell.getAttributes());
			subStr = "Sub Property: " + subInfo.getURIStr() + "\tMeta Class: " + subInfo.getMetaClass() + "\n";
			supInfo = (RDFSModel) GraphConstants.getValue(targetCell.getAttributes());
			supStr = "Super Property: " + supInfo.getURIStr() + "\tMeta Class: " + supInfo.getMetaClass() + "\n";
			logger.info(META_MODEL + "[" + historyType + "]\n" + subStr + supStr);
			break;
		}
	}

	public synchronized static void saveHistory(HistoryType historyType, Object[] removeCells) {
		if (!GraphManager.isLogAvailable()) {
			return;
		}
		StringBuilder buf = new StringBuilder("DELETE: \n");
		for (Object removeCell : removeCells) {
			GraphCell cell = (GraphCell) removeCell;
			if (RDFGraph.isRDFResourceCell(cell)) {
				RDFResourceModel resInfo = (RDFResourceModel) GraphConstants.getValue(cell.getAttributes());
				buf.append("RDF Resource: ").append(resInfo.getURIStr()).append("\t");
				buf.append("RDF Resource Type: ").append(resInfo.getType()).append("\n");
			} else if (historyType == HistoryType.DELETE_RDF && RDFGraph.isRDFPropertyCell(cell)) {
				RDFSModel rdfsModel = (RDFSModel) GraphConstants.getValue(cell.getAttributes());
				buf.append("RDF Property: ").append(rdfsModel.getURIStr()).append("\n");
			} else if (RDFGraph.isRDFLiteralCell(cell)) {
				MR3Literal literal = (MR3Literal) GraphConstants.getValue(cell.getAttributes());
				buf.append("RDF Literal: \n");
				buf.append("Language: ").append(literal.getLanguage()).append("\t");
				buf.append("Datatype: ").append(literal.getDatatype()).append("\t");
				buf.append("String: ").append(literal.getString()).append("\n");
			} else if (RDFGraph.isRDFSClassCell(cell)) {
				RDFSModel rdfsModel = (RDFSModel) GraphConstants.getValue(cell.getAttributes());
				buf.append("Ont Class: ").append(rdfsModel.getURIStr()).append("\t");
				buf.append("Meta Class: ").append(rdfsModel.getMetaClass()).append("\n");
			} else if (RDFGraph.isRDFSPropertyCell(cell)) {
				RDFSModel rdfsModel = (RDFSModel) GraphConstants.getValue(cell.getAttributes());
				buf.append("Ont Property: ").append(rdfsModel.getURIStr()).append("\t");
				buf.append("Meta Property: ").append(rdfsModel.getMetaClass()).append("\n");
			}
		}

		switch (historyType) {
		case DELETE_RDF:
			logger.info(MODEL + "[" + historyType + "]\n" + buf.toString());
			break;
		case DELETE_CLASS:
		case DELETE_ONT_PROPERTY:
			logger.info(META_MODEL + "[" + historyType + "]\n" + buf.toString());
			break;
		}
	}

	public synchronized static void saveHistory(HistoryType historyType, List<MR3Literal> beforeMR3LiteralList,
			List<MR3Literal> afterMR3LiteralList) {
		if (!GraphManager.isLogAvailable()) {
			return;
		}
		StringBuilder beforeBuf = new StringBuilder();
		for (MR3Literal lit : beforeMR3LiteralList) {
			beforeBuf.append("Language: ").append(lit.getLanguage()).append("\tString: ").append(lit.getString()).append("\n");
		}
		StringBuilder afterBuf = new StringBuilder();
		for (MR3Literal lit : afterMR3LiteralList) {
			afterBuf.append("Language: ").append(lit.getLanguage()).append("\tString: ").append(lit.getString()).append("\n");
		}

		switch (historyType) {
		case EDIT_RESOURCE_LABEL:
		case EDIT_RESOURCE_LABEL_WITH_GRAPH:
		case ADD_RESOURCE_LABEL:
		case DELETE_RESOURCE_LABEL:
			logger.info(MODEL + "[" + historyType + "]\n" + "Before Label: \n" + beforeBuf.toString()
					+ "After Label: \n" + afterBuf.toString());
			break;
		case EDIT_RESOURCE_COMMENT:
		case ADD_RESOURCE_COMMENT:
		case DELETE_RESOURCE_COMMENT:
			logger.info(MODEL + "[" + historyType + "]\n" + "Before Comment: \n" + beforeBuf.toString()
					+ "After Comment: \n" + afterBuf.toString());
			break;
		case EDIT_CLASS_LABEL:
		case EDIT_CLASS_LABEL_WITH_GRAPH:
		case ADD_CLASS_LABEL:
		case DELETE_CLASS_LABEL:
		case EDIT_ONT_PROPERTY_LABEL:
		case EDIT_ONT_PROPERTY_LABEL_WITH_GRAPH:
		case ADD_ONT_PROPERTY_LABEL:
		case DELETE_ONT_PROPERTY_LABEL:
			logger.info(META_MODEL + "[" + historyType + "]\n" + "Before Label: \n" + beforeBuf.toString()
					+ "After Label: " + afterBuf.toString());
			break;
		case EDIT_CLASS_COMMENT:
		case ADD_CLASS_COMMENT:
		case DELETE_CLASS_COMMENT:
		case EDIT_ONT_PROPERTY_COMMENT:
		case ADD_ONT_PROPERTY_COMMENT:
		case DELETE_ONT_PROPERTY_COMMENT:
			logger.info(META_MODEL + "[" + historyType + "]\n" + "Before Comment: \n" + beforeBuf.toString()
					+ "After Comment: " + afterBuf.toString());
			break;

		}
	}

	public synchronized static void saveHistory(HistoryType historyType, RDFResourceModel beforeInfo,
			RDFResourceModel afterInfo) {
		if (!GraphManager.isLogAvailable()) {
			return;
		}
		if (beforeInfo.isSameInfo(afterInfo)) {
			return;
		}
		switch (historyType) {
		case EDIT_RESOURCE_WITH_DIALOG:
		case EDIT_RESOURCE_WITH_GRAPH:
			logger.info(MODEL + "[" + historyType + "]\n" + "Before RDF Resource URI Type: " + beforeInfo.getURIType()
					+ "\nBefore RDF Resource: " + beforeInfo.getURIStr() + "\nBefore RDF Resource Type: "
					+ beforeInfo.getType() + "\nAfter RDF Resource URI Type: " + afterInfo.getURIType()
					+ "\nAfter RDF Resource: " + afterInfo.getURIStr() + "\nAfter RDF Resource Type: "
					+ afterInfo.getType() + "\n");
			break;
		}
	}

	public synchronized static void saveHistory(HistoryType historyType, String beforeProperty, String afterProperty) {
		if (!GraphManager.isLogAvailable()) {
			return;
		}
		if (beforeProperty.equals(afterProperty)) {
			return;
		}
		switch (historyType) {
		case EDIT_PROPERTY_WITH_DIAGLOG:
		case EDIT_PROPERTY_WITH_GRAPH:
			logger.info(MODEL + "[" + historyType + "]\n" + "Before Property: " + beforeProperty + "\nAfter Property: "
					+ afterProperty + "\n");
			break;
		}
	}

	public synchronized static void saveHistory(HistoryType historyType, MR3Literal beforeLiteral,
			MR3Literal afterLiteral) {
		if (!GraphManager.isLogAvailable()) {
			return;
		}
		if (beforeLiteral.equals(afterLiteral)) {
			return;
		}
		switch (historyType) {
		case EDIT_LITERAL_WITH_DIAGLOG:
		case EDIT_LITERAL_WITH_GRAPH:
			logger.info(MODEL + "[" + historyType + "]\n" + "Before Language: " + beforeLiteral.getLanguage()
					+ "\nBefore String: " + beforeLiteral.getString() + "\nBefore Data type: "
					+ beforeLiteral.getDatatype() + "\nAfter Language: " + afterLiteral.getLanguage()
					+ "\nAfter String: " + afterLiteral.getString() + "\nAfter Data type: "
					+ afterLiteral.getDatatype() + "\n");
			break;
		}
	}

	public synchronized static void saveHistory(HistoryType historyType, Set beforeRegion, Set afterRegion, String uri) {
		if (!GraphManager.isLogAvailable()) {
			return;
		}
		switch (historyType) {
		case ADD_ONT_PROPERTY_DOMAIN:
		case DELETE_ONT_PROPERTY_DOMAIN:
			logger.info(META_MODEL + "[" + historyType + "]\n" + "ONT Property: " + uri
					+ "\nBefore ONT Property Domain: " + beforeRegion + "\nAfter ONT Property Domain: " + afterRegion
					+ "\n");
			break;
		case ADD_ONT_PROPERTY_RANGE:
		case DELETE_ONT_PROPERTY_RANGE:
			logger.info(META_MODEL + "[" + historyType + "]\n" + "ONT Property: " + uri
					+ "\nBefore ONT Property Range: " + beforeRegion + "\nAfter ONT Property Range: " + afterRegion
					+ "\n");
			break;
		}
	}

	public synchronized static void saveHistory(HistoryType historyType, RDFSModel beforeInfo, RDFSModel afterInfo) {
		if (!GraphManager.isLogAvailable()) {
			return;
		}
		if (beforeInfo.isSameInfo(afterInfo)) {
			return;
		}
		switch (historyType) {
		case EDIT_CLASS_WITH_DIAGLOG:
		case EDIT_CLASS_WITH_GRAPH:
			logger.info(META_MODEL + "[" + historyType + "]\n" + "Before ONT Class: " + beforeInfo.getURIStr()
					+ "\nBefore ONT Class Type: " + beforeInfo.getMetaClass() + "\nAfter ONT Class: "
					+ afterInfo.getURIStr() + "\nAfter ONT Class Type: " + afterInfo.getMetaClass() + "\n");
			if (!MR3.OFF_META_MODEL_MANAGEMENT) {
				ClassModel clsInfo = (ClassModel) afterInfo;
				Set<RDFResourceModel> instanceInfoSet = gmanager.getClassInstanceInfoSet(clsInfo);
				if (0 < instanceInfoSet.size()) {
					StringBuilder instanceInfoStr = new StringBuilder();
					for (RDFResourceModel resInfo : instanceInfoSet) {
						instanceInfoStr.append("RDF Resource: ").append(resInfo.getURIStr()).append("\n");
					}
					logger.info(META_MODEL + "[" + HistoryType.META_MODEL_MANAGEMNET_REPLACE_CLASS + "]\n"
							+ instanceInfoStr);
				}
			}
			break;
		case EDIT_ONT_PROPERTY_WITH_DIAGLOG:
		case EDIT_ONT_PROPERTY_WITH_GRAPH:
			logger.info(META_MODEL + "[" + historyType + "]\n" + "Before ONT Property: " + beforeInfo.getURIStr()
					+ "\nBefore ONT Property Type: " + beforeInfo.getMetaClass() + "\nAfter ONT Property: "
					+ afterInfo.getURIStr() + "\nAfter ONT Property Type: " + afterInfo.getMetaClass() + "\n");
			if (!MR3.OFF_META_MODEL_MANAGEMENT) {
				PropertyModel propInfo = (PropertyModel) afterInfo;
				Set instanceSet = gmanager.getPropertyInstanceInfoSet(propInfo);
				if (0 < instanceSet.size()) {
					StringBuilder instanceInfoStr = new StringBuilder();
					for (Object cell : instanceSet) {
						RDFResourceModel resInfo = (RDFResourceModel) GraphConstants.getValue(((GraphCell) cell)
								.getAttributes());
						instanceInfoStr.append("Source RDF Resource: ").append(resInfo.getURIStr()).append("\n");
					}
					// 正確にやるなら，RDFプロパティのグラフセルのセットから，sourcevertex,
					// targetvertexを得て，
					// Source Resource, Target Resourceを表示するようにすべき
					logger.info(META_MODEL + "[" + HistoryType.META_MODEL_MANAGEMNET_REPLACE_ONT_PROPERTY + "]\n"
							+ instanceInfoStr);
				}
			}
			break;
		}
	}

	public synchronized static void saveHistory(HistoryType historyType, GraphCell insertCell) {
		if (!GraphManager.isLogAvailable()) {
			return;
		}
		switch (historyType) {
		case INSERT_RESOURCE:
		case INSERT_CONNECTED_RESOURCE:
			RDFResourceModel info = (RDFResourceModel) GraphConstants.getValue(insertCell.getAttributes());
			logger.info(MODEL + "[" + historyType + "]\n" + "RDF Resource URI Type: " + info.getURIType()
					+ "\nRDF Resource: " + info.getURIStr() + "\nRDF Resource Type: " + info.getType() + "\n");
			break;
		case INSERT_LITERAL:
		case INSERT_CONNECTED_LITERAL:
			MR3Literal literal = (MR3Literal) GraphConstants.getValue(insertCell.getAttributes());
			logger.info(MODEL + "[" + historyType + "]\n" + "Language: " + literal.getLanguage() + "\nString: "
					+ literal.getString() + "\nData type: " + literal.getDatatype() + "\n");
			break;
		case INSERT_ONT_PROPERTY:
		case INSERT_CONNECTED_ONT_PROPERTY:
			RDFSModel rdfsModel = (RDFSModel) GraphConstants.getValue(insertCell.getAttributes());
			logger.info(META_MODEL + "[" + historyType + "]\n" + "ONT Property: " + rdfsModel.getURIStr()
					+ "\nONT Property Type: " + rdfsModel.getMetaClass() + "\n");
			break;
		case INSERT_CLASS:
			rdfsModel = (RDFSModel) GraphConstants.getValue(insertCell.getAttributes());
			logger.info(META_MODEL + "[" + historyType + "]\n" + "ONT Class: " + rdfsModel.getURIStr()
					+ "\nONT Class Type: " + rdfsModel.getMetaClass() + "\n");
			break;
		}
	}

	public synchronized static void saveHistory(HistoryType historyType, String path) {
		if (!GraphManager.isLogAvailable()) {
			return;
		}
		switch (historyType) {
		case OPEN_PROJECT:
			logger.info("[---][" + historyType + "]\nPath: " + path + "\n");
			break;
		case SAVE_PROJECT:
		case SAVE_PROJECT_AS:
			logger.info("[---][" + historyType + "]\nPath: " + path + "\n");
			break;
		}
	}

	public synchronized static void saveHistory(HistoryType historyType) {
		if (!GraphManager.isLogAvailable()) {
			return;
		}
		switch (historyType) {
		case NEW_PROJECT:
			logger.info("[---][" + historyType + "]\n");
			break;
		case COPY_CLASS_GRAPH:
		case CUT_CLASS_GRAPH:
		case PASTE_CLASS_GRAPH:
		case COPY_PROPERTY_GRAPH:
		case CUT_PROPERTY_GRAPH:
		case PASTE_PROPERTY_GRAPH:
			logger.info(META_MODEL + "[" + historyType + "]\n");
			break;
		case META_MODEL_MANAGEMNET_REPLACE_RESOURCE_TYPE_WITH_CREATE_CLASS:
		case META_MODEL_MANAGEMNET_REPLACE_RESOURCE_TYPE_WITH_REPLACE_CLASS:
		case META_MODEL_MANAGEMNET_REPLACE_PROPERTY_WITH_CREATE_ONT_PROPERTY:
		case META_MODEL_MANAGEMNET_REPLACE_PROPERTY_WITH_REPLACE_ONT_PROPERTY:
		case COPY_RDF_GRAPH:
		case CUT_RDF_GRAPH:
		case PASTE_RDF_GRAPH:
			logger.info(MODEL + "[" + historyType + "]\n");
			break;
		}
		// HistoryModel data = new HistoryModel(historyType,
		// mr3Writer.getProjectModel());
		// dateHistoryDataMap.put(data.getDate(), data);
		// historyTableModel.insertRow(0, new Object[] { data.getDate(),
		// data.getHistoryType()});
	}

	public void loadHistory() {
		if (historyTable.getSelectedRowCount() == 1) {
			Date date = (Date) historyTableModel.getValueAt(historyTable.getSelectedRow(), 0);
			HistoryModel data = dateHistoryDataMap.get(date);
			Model projectModel = ModelFactory.createDefaultModel();
			projectModel = projectModel.union(data.getProjectModel());
			mr3Reader.replaceProjectModel(projectModel);
			saveHistory(HistoryType.LOAD_HISTORY);
		}
	}
}
