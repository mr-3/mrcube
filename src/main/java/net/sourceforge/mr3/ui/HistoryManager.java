/*
 * Project Name: MR^3 (Meta-Model Management based on RDFs Revision Reflection)
 * Project Website: http://mr3.sourceforge.net/
 * 
 * Copyright (C) 2003-2015 Yamaguchi Laboratory, Keio University. All rights reserved. 
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

package net.sourceforge.mr3.ui;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.util.List;

import javax.swing.*;
import javax.swing.table.*;

import net.sourceforge.mr3.*;
import net.sourceforge.mr3.data.*;
import net.sourceforge.mr3.data.MR3Constants.*;
import net.sourceforge.mr3.io.*;
import net.sourceforge.mr3.jgraph.*;
import net.sourceforge.mr3.util.*;

import org.apache.log4j.*;
import org.jgraph.graph.*;

import com.hp.hpl.jena.rdf.model.*;

/**
 * @author Takeshi Morita
 */
public class HistoryManager extends JDialog implements ActionListener {

    private static MR3 mr3;
    private static MR3Reader mr3Reader;
    private static MR3Writer mr3Writer;
    private static Map<Date, HistoryData> dateHistoryDataMap;
    private static DefaultTableModel historyTableModel;
    private static JTable historyTable;

    private static GraphManager gmanager;

    private JButton applyButton;
    private JButton cancelButton;

    private static final int WINDOW_WIDTH = 400;
    private static final int WINDOW_HEIGHT = 400;

    public static final String DEFAULT_LOG_FILE_NAME = "mr3_log.txt";

    private static Logger logger;

    static {
        if (GraphManager.isLogAvailable()) {
            initLogger();
        }
    }

    private static void initLogger() {
        try {
            logger = Logger.getLogger(HistoryManager.class);
            logger.setLevel(Level.INFO);
            FileAppender appender = new FileAppender(new PatternLayout("%d{yyyy-MMM-dd HH:mm:ss} %m"), "./"
                    + DEFAULT_LOG_FILE_NAME);
            appender.setName("LOG FILE");
            appender.setAppend(true);
            logger.addAppender(appender);
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    public static void resetFileAppender(String path) {
        if (!path.equals("")) {
            if (GraphManager.isLogAvailable()) {
                if (logger == null) {
                    initLogger();
                }
                FileAppender appender = (FileAppender) logger.getAppender("LOG FILE");
                appender.setFile(path);
                appender.activateOptions();
            }
        }
    }

    private static final Object[] columnNames = new Object[] { "Date", "History Type"};

    public HistoryManager(Frame root, MR3 m) {
        super(root, "HistoryManager");
        mr3 = m;
        mr3Reader = mr3.getMR3Reader();
        mr3Writer = mr3.getMR3Writer();
        dateHistoryDataMap = new HashMap<Date, HistoryData>();
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

    public synchronized static void saveHistory(HistoryType historyType, RDFSInfo info, GraphCell sourceCell,
            GraphCell targetCell) {
        if (!GraphManager.isLogAvailable()) { return; }
        switch (historyType) {
        case INSERT_PROPERTY:
            RDFResourceInfo resInfo = (RDFResourceInfo) GraphConstants.getValue(sourceCell.getAttributes());
            String sourceStr = "Source: RDF Resource: " + resInfo.getURIStr() + "\tRDF Resource Type: "
                    + resInfo.getType() + "\n";
            String targetStr = "";
            if (RDFGraph.isRDFLiteralCell(targetCell)) {
                MR3Literal literal = (MR3Literal) GraphConstants.getValue(targetCell.getAttributes());
                targetStr = "Target: Language: " + literal.getLanguage() + "\tDatatype: " + literal.getDatatype()
                        + "\tString: " + literal.getString() + "\n";
            } else {
                resInfo = (RDFResourceInfo) GraphConstants.getValue(targetCell.getAttributes());
                targetStr = "Target: RDF Resource: " + resInfo.getURIStr() + "\tRDF Resource Type: "
                        + resInfo.getType() + "\n";
            }
            logger.info(MODEL + "[" + historyType + "]\n" + "Insert Property: " + info.getURIStr() + "\n" + sourceStr
                    + targetStr);
            break;
        case CONNECT_SUP_SUB_CLASS:
            RDFSInfo subInfo = (RDFSInfo) GraphConstants.getValue(sourceCell.getAttributes());
            String subStr = "Sub Class: " + subInfo.getURIStr() + "\tMeta Class: " + subInfo.getMetaClass() + "\n";
            RDFSInfo supInfo = (RDFSInfo) GraphConstants.getValue(targetCell.getAttributes());
            String supStr = "Super Class: " + supInfo.getURIStr() + "\tMeta Class: " + supInfo.getMetaClass() + "\n";
            logger.info(META_MODEL + "[" + historyType + "]\n" + subStr + supStr);
            break;
        case CONNECT_SUP_SUB_PROPERTY:
            subInfo = (RDFSInfo) GraphConstants.getValue(sourceCell.getAttributes());
            subStr = "Sub Property: " + subInfo.getURIStr() + "\tMeta Class: " + subInfo.getMetaClass() + "\n";
            supInfo = (RDFSInfo) GraphConstants.getValue(targetCell.getAttributes());
            supStr = "Super Property: " + supInfo.getURIStr() + "\tMeta Class: " + supInfo.getMetaClass() + "\n";
            logger.info(META_MODEL + "[" + historyType + "]\n" + subStr + supStr);
            break;
        }
    }

    public synchronized static void saveHistory(HistoryType historyType, Object[] removeCells) {
        if (!GraphManager.isLogAvailable()) { return; }
        StringBuffer buf = new StringBuffer("DELETE: \n");
        for (int i = 0; i < removeCells.length; i++) {
            GraphCell cell = (GraphCell) removeCells[i];
            if (RDFGraph.isRDFResourceCell(cell)) {
                RDFResourceInfo resInfo = (RDFResourceInfo) GraphConstants.getValue(cell.getAttributes());
                buf.append("RDF Resource: " + resInfo.getURIStr() + "\t");
                buf.append("RDF Resource Type: " + resInfo.getType() + "\n");
            } else if (historyType == HistoryType.DELETE_RDF && RDFGraph.isRDFPropertyCell(cell)) {
                RDFSInfo rdfsInfo = (RDFSInfo) GraphConstants.getValue(cell.getAttributes());
                buf.append("RDF Property: " + rdfsInfo.getURIStr() + "\n");
            } else if (RDFGraph.isRDFLiteralCell(cell)) {
                MR3Literal literal = (MR3Literal) GraphConstants.getValue(cell.getAttributes());
                buf.append("RDF Literal: \n");
                buf.append("Language: " + literal.getLanguage() + "\t");
                buf.append("Datatype: " + literal.getDatatype() + "\t");
                buf.append("String: " + literal.getString() + "\n");
            } else if (RDFGraph.isRDFSClassCell(cell)) {
                RDFSInfo rdfsInfo = (RDFSInfo) GraphConstants.getValue(cell.getAttributes());
                buf.append("Ont Class: " + rdfsInfo.getURIStr() + "\t");
                buf.append("Meta Class: " + rdfsInfo.getMetaClass() + "\n");
            } else if (RDFGraph.isRDFSPropertyCell(cell)) {
                RDFSInfo rdfsInfo = (RDFSInfo) GraphConstants.getValue(cell.getAttributes());
                buf.append("Ont Property: " + rdfsInfo.getURIStr() + "\t");
                buf.append("Meta Property: " + rdfsInfo.getMetaClass() + "\n");
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
        if (!GraphManager.isLogAvailable()) { return; }
        StringBuffer beforeBuf = new StringBuffer("");
        for (MR3Literal lit : beforeMR3LiteralList) {
            beforeBuf.append("Language: " + lit.getLanguage() + "\tString: " + lit.getString() + "\n");
        }
        StringBuffer afterBuf = new StringBuffer("");
        for (MR3Literal lit : afterMR3LiteralList) {
            afterBuf.append("Language: " + lit.getLanguage() + "\tString: " + lit.getString() + "\n");
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

    public synchronized static void saveHistory(HistoryType historyType, RDFResourceInfo beforeInfo,
            RDFResourceInfo afterInfo) {
        if (!GraphManager.isLogAvailable()) { return; }
        if (beforeInfo.isSameInfo(afterInfo)) { return; }
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
        if (!GraphManager.isLogAvailable()) { return; }
        if (beforeProperty.equals(afterProperty)) { return; }
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
        if (!GraphManager.isLogAvailable()) { return; }
        if (beforeLiteral.equals(afterLiteral)) { return; }
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
        if (!GraphManager.isLogAvailable()) { return; }
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

    public synchronized static void saveHistory(HistoryType historyType, RDFSInfo beforeInfo, RDFSInfo afterInfo) {
        if (!GraphManager.isLogAvailable()) { return; }
        if (beforeInfo.isSameInfo(afterInfo)) { return; }
        switch (historyType) {
        case EDIT_CLASS_WITH_DIAGLOG:
        case EDIT_CLASS_WITH_GRAPH:
            logger.info(META_MODEL + "[" + historyType + "]\n" + "Before ONT Class: " + beforeInfo.getURIStr()
                    + "\nBefore ONT Class Type: " + beforeInfo.getMetaClass() + "\nAfter ONT Class: "
                    + afterInfo.getURIStr() + "\nAfter ONT Class Type: " + afterInfo.getMetaClass() + "\n");
            if (!MR3.OFF_META_MODEL_MANAGEMENT) {
                ClassInfo clsInfo = (ClassInfo) afterInfo;
                Set<RDFResourceInfo> instanceInfoSet = gmanager.getClassInstanceInfoSet(clsInfo);
                if (0 < instanceInfoSet.size()) {
                    StringBuffer instanceInfoStr = new StringBuffer("");
                    for (RDFResourceInfo resInfo : instanceInfoSet) {
                        instanceInfoStr.append("RDF Resource: " + resInfo.getURIStr() + "\n");
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
                PropertyInfo propInfo = (PropertyInfo) afterInfo;
                Set instanceSet = gmanager.getPropertyInstanceInfoSet(propInfo);
                if (0 < instanceSet.size()) {
                    StringBuffer instanceInfoStr = new StringBuffer("");
                    for (Object cell : instanceSet) {
                        RDFResourceInfo resInfo = (RDFResourceInfo) GraphConstants.getValue(((GraphCell) cell)
                                .getAttributes());
                        instanceInfoStr.append("Source RDF Resource: " + resInfo.getURIStr() + "\n");
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
        if (!GraphManager.isLogAvailable()) { return; }
        switch (historyType) {
        case INSERT_RESOURCE:
        case INSERT_CONNECTED_RESOURCE:
            RDFResourceInfo info = (RDFResourceInfo) GraphConstants.getValue(insertCell.getAttributes());
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
            RDFSInfo rdfsInfo = (RDFSInfo) GraphConstants.getValue(insertCell.getAttributes());
            logger.info(META_MODEL + "[" + historyType + "]\n" + "ONT Property: " + rdfsInfo.getURIStr()
                    + "\nONT Property Type: " + rdfsInfo.getMetaClass() + "\n");
            break;
        case INSERT_CLASS:
            rdfsInfo = (RDFSInfo) GraphConstants.getValue(insertCell.getAttributes());
            logger.info(META_MODEL + "[" + historyType + "]\n" + "ONT Class: " + rdfsInfo.getURIStr()
                    + "\nONT Class Type: " + rdfsInfo.getMetaClass() + "\n");
            break;
        }
    }

    public synchronized static void saveHistory(HistoryType historyType, String path) {
        if (!GraphManager.isLogAvailable()) { return; }
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
        if (!GraphManager.isLogAvailable()) { return; }
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
        // HistoryData data = new HistoryData(historyType,
        // mr3Writer.getProjectModel());
        // dateHistoryDataMap.put(data.getDate(), data);
        // historyTableModel.insertRow(0, new Object[] { data.getDate(),
        // data.getHistoryType()});
    }

    public void loadHistory() {
        if (historyTable.getSelectedRowCount() == 1) {
            Date date = (Date) historyTableModel.getValueAt(historyTable.getSelectedRow(), 0);
            HistoryData data = dateHistoryDataMap.get(date);
            Model projectModel = ModelFactory.createDefaultModel();
            projectModel = projectModel.union(data.getProjectModel());
            mr3Reader.replaceProjectModel(projectModel);
            saveHistory(HistoryType.LOAD_HISTORY);
        }
    }

    public static void main(String[] arg) {
        System.out.println(Calendar.getInstance().getTime());
    }
}
