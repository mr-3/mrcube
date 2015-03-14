/*
 * Project Name: MR^3 (Meta-Model Management based on RDFs Revision Reflection)
 * Project Website: http://mr3.sourceforge.net/
 * 
 * Copyright (C) 2003-2008 Yamaguchi Laboratory, Keio University. All rights reserved. 
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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.tree.DefaultMutableTreeNode;

import net.sourceforge.mr3.data.MR3Constants;
import net.sourceforge.mr3.data.PrefConstants;
import net.sourceforge.mr3.data.PrefixNSInfo;
import net.sourceforge.mr3.data.RDFResourceInfo;
import net.sourceforge.mr3.io.MR3Writer;
import net.sourceforge.mr3.jgraph.GraphManager;
import net.sourceforge.mr3.jgraph.RDFGraph;
import net.sourceforge.mr3.util.GPConverter;
import net.sourceforge.mr3.util.GraphUtilities;
import net.sourceforge.mr3.util.MR3FileFilter;
import net.sourceforge.mr3.util.NTripleFileFilter;
import net.sourceforge.mr3.util.OWLFileFilter;
import net.sourceforge.mr3.util.PNGFileFilter;
import net.sourceforge.mr3.util.RDFsFileFilter;
import net.sourceforge.mr3.util.Translator;
import net.sourceforge.mr3.util.TurtleFileFilter;
import net.sourceforge.mr3.util.Utilities;

import org.jgraph.graph.GraphCell;
import org.jgraph.graph.GraphConstants;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.RDFWriter;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.util.URIref;
import com.hp.hpl.jena.vocabulary.RDFS;

/**
 * @author takeshi morita
 * 
 */
public class ExportDialog extends JDialog implements ActionListener {

	private MR3Writer mr3Writer;
	private GraphManager gmanager;
	private MR3TreePanel treePanel;

	private JRadioButton xmlRadioButton;
	private JRadioButton nTripleRadioButton;
	private JRadioButton turtleRadioButton;
	private JRadioButton n3RadioButton;
	private JRadioButton n3PPRadioButton;
	private JRadioButton n3PLAINRadioButton;
	private JRadioButton n3TRIPLERadioButton;

	private JCheckBox rdfConvertBox;
	private JCheckBox classConvertBox;
	private JCheckBox propertyConvertBox;

	private JCheckBox encodeCheckBox;
	private JCheckBox selectedCheckBox;
	private JCheckBox abbrevCheckBox;
	private JCheckBox xmlbaseCheckBox;

	private JButton reloadButton;
	private JButton cancelButton;
	private JButton exportFileButton;
	private JButton exportImgButton;

	private static JTextArea exportTextArea;
	private static final int FRAME_HEIGHT = 500;
	private static final int FRAME_WIDTH = 600;
	private static ImageIcon EXPORT_ICON = Utilities.getImageIcon(Translator
			.getString("Component.File.Export.Icon"));
	private static ImageIcon FILE_ICON = Utilities.getImageIcon("page_white_text.png");
	private static ImageIcon IMAGE_ICON = Utilities.getImageIcon("image.png");

	public ExportDialog(GraphManager gm) {
		super(gm.getRootFrame(), Translator.getString("ExportDialog.Title"), true);
		setIconImage(EXPORT_ICON.getImage());

		gmanager = gm;
		mr3Writer = new MR3Writer(gmanager);

		xmlRadioButton = new JRadioButton("RDF/XML");
		xmlRadioButton.addActionListener(this);
		nTripleRadioButton = new JRadioButton("N-Triple");
		nTripleRadioButton.addActionListener(this);
		turtleRadioButton = new JRadioButton("Turtle");
		turtleRadioButton.addActionListener(this);
		n3RadioButton = new JRadioButton("N3");
		n3RadioButton.addActionListener(this);
		n3PPRadioButton = new JRadioButton("N3-PP");
		n3PPRadioButton.addActionListener(this);
		n3PLAINRadioButton = new JRadioButton("N3-PLAIN");
		n3PLAINRadioButton.addActionListener(this);
		n3TRIPLERadioButton = new JRadioButton("N3-TRIPLE");
		n3TRIPLERadioButton.addActionListener(this);
		ButtonGroup group = new ButtonGroup();
		group.add(xmlRadioButton);
		group.add(nTripleRadioButton);
		group.add(turtleRadioButton);
		group.add(n3RadioButton);
		group.add(n3PPRadioButton);
		group.add(n3PLAINRadioButton);
		group.add(n3TRIPLERadioButton);
		JPanel outputCheckPanel = new JPanel();
		outputCheckPanel.setLayout(new GridLayout(2, 4));
		outputCheckPanel.setBorder(BorderFactory.createTitledBorder(Translator
				.getString("ImportDialog.Syntax")));
		outputCheckPanel.add(xmlRadioButton);
		xmlRadioButton.setSelected(true);
		outputCheckPanel.add(nTripleRadioButton);
		outputCheckPanel.add(turtleRadioButton);
		outputCheckPanel.add(n3RadioButton);
		outputCheckPanel.add(n3PPRadioButton);
		outputCheckPanel.add(n3PLAINRadioButton);
		outputCheckPanel.add(n3TRIPLERadioButton);

		rdfConvertBox = new JCheckBox("RDF");
		rdfConvertBox.setSelected(true);
		rdfConvertBox.addActionListener(this);
		classConvertBox = new JCheckBox(Translator.getString("Class"));
		classConvertBox.setSelected(true);
		classConvertBox.addActionListener(this);
		propertyConvertBox = new JCheckBox(Translator.getString("Property"));
		propertyConvertBox.setSelected(true);
		propertyConvertBox.addActionListener(this);
		JPanel dataTypePanel = new JPanel();
		dataTypePanel.setLayout(new GridLayout(1, 3));
		dataTypePanel.setBorder(BorderFactory.createTitledBorder(Translator
				.getString("ImportDialog.DataType")));
		dataTypePanel.add(rdfConvertBox);
		dataTypePanel.add(classConvertBox);
		dataTypePanel.add(propertyConvertBox);

		encodeCheckBox = new JCheckBox("Encode(UTF-8)");
		encodeCheckBox.addActionListener(this);
		selectedCheckBox = new JCheckBox("Selected");
		selectedCheckBox.addActionListener(this);
		abbrevCheckBox = new JCheckBox("Abbrev");
		abbrevCheckBox.setSelected(true);
		abbrevCheckBox.addActionListener(this);
		xmlbaseCheckBox = new JCheckBox("XMLBase");
		xmlbaseCheckBox.setSelected(true);
		xmlbaseCheckBox.addActionListener(this);
		JPanel optionCheckPanel = new JPanel();
		optionCheckPanel.setBorder(BorderFactory.createTitledBorder(Translator
				.getString("ExportDialog.Option")));
		optionCheckPanel.setLayout(new GridLayout(2, 2));
		optionCheckPanel.add(encodeCheckBox);
		optionCheckPanel.add(selectedCheckBox);
		optionCheckPanel.add(abbrevCheckBox);
		optionCheckPanel.add(xmlbaseCheckBox);

		exportFileButton = new JButton(Translator.getString("ExportDialog.File") + "(F)", FILE_ICON);
		exportFileButton.setHorizontalAlignment(JButton.LEFT);
		exportFileButton.setMnemonic('f');
		exportFileButton.addActionListener(new ExportFileEvent());
		exportImgButton = new JButton(Translator.getString("ExportDialog.Image") + "(I)",
				IMAGE_ICON);
		exportImgButton.setHorizontalAlignment(JButton.LEFT);
		exportImgButton.setMnemonic('i');
		exportImgButton.addActionListener(new ExportImgEvent());

		reloadButton = new JButton(MR3Constants.RELOAD + "(L)");
		reloadButton.setMnemonic('l');
		reloadButton.addActionListener(this);
		cancelButton = new JButton(MR3Constants.CANCEL);
		cancelButton.setMnemonic('c');
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setVisible(false);
			}
		});
		JPanel otherButtonPanel = new JPanel();
		otherButtonPanel.setLayout(new GridLayout(2, 1, 5, 5));
		otherButtonPanel.add(reloadButton);
		otherButtonPanel.add(cancelButton);

		JPanel exportButtonPanel = new JPanel();
		exportButtonPanel.setLayout(new GridLayout(2, 1, 5, 5));
		exportButtonPanel.setBorder(BorderFactory.createTitledBorder(Translator
				.getString("Component.File.Export.Text")));
		exportButtonPanel.add(exportFileButton);
		exportButtonPanel.add(exportImgButton);

		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new BorderLayout());
		buttonPanel.add(exportButtonPanel, BorderLayout.NORTH);
		buttonPanel.add(otherButtonPanel, BorderLayout.SOUTH);

		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new GridLayout(3, 1));
		mainPanel.add(outputCheckPanel);
		mainPanel.add(dataTypePanel);
		mainPanel.add(optionCheckPanel);
		JPanel settingPanel = new JPanel();
		settingPanel.setLayout(new BorderLayout());
		settingPanel.add(mainPanel, BorderLayout.CENTER);
		settingPanel.add(buttonPanel, BorderLayout.EAST);

		exportTextArea = new JTextArea();
		JSplitPane sourcePane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
				Utilities.createWestPanel(settingPanel), new JScrollPane(exportTextArea));
		sourcePane.setOneTouchExpandable(true);

		setContentPane(sourcePane);
		Dimension size = new Dimension(FRAME_WIDTH, FRAME_HEIGHT);
		setSize(size);
		setLocationRelativeTo(gmanager.getRootFrame());
		setVisible(false);
	}

	private static RDFsFileFilter rdfsFileFilter = new RDFsFileFilter();
	private static NTripleFileFilter n3FileFilter = new NTripleFileFilter();
	private static TurtleFileFilter turtleFileFilter = new TurtleFileFilter();
	private static OWLFileFilter owlFileFilter = new OWLFileFilter();
	private static PNGFileFilter pngFileFilter = new PNGFileFilter();

	private File getFile() {
		return getFile(getExtension());
	}

	private File getFile(String extension) {
		JFileChooser jfc = new JFileChooser(gmanager.getUserPrefs().get(
				PrefConstants.WorkDirectory, ""));
		if (extension.equals("rdf")) {
			jfc.addChoosableFileFilter(rdfsFileFilter);
			jfc.addChoosableFileFilter(owlFileFilter);
		} else if (extension.equals("n3")) {
			jfc.setFileFilter(n3FileFilter);
		} else if (extension.equals("ttl")) {
			jfc.setFileFilter(turtleFileFilter);
		} else if (extension.equals("png")) {
			jfc.setFileFilter(pngFileFilter);
		}

		if (jfc.showSaveDialog(gmanager.getDesktopTabbedPane()) == JFileChooser.APPROVE_OPTION) {
			String defaultPath = jfc.getSelectedFile().getAbsolutePath();
			if (jfc.getFileFilter() instanceof MR3FileFilter) {
				MR3FileFilter filter = (MR3FileFilter) jfc.getFileFilter();
				extension = filter.getExtension();
			}
			return new File(complementRDFsExtension(defaultPath, extension));
		}
		return null;
	}

	private String complementRDFsExtension(String tmp, String extension) {
		String ext = (extension != null) ? "." + extension.toLowerCase() : "";
		if (extension != null && !tmp.toLowerCase().endsWith(".rdf")
				&& !tmp.toLowerCase().endsWith(".rdfs") && !tmp.toLowerCase().endsWith(".n3")
				&& !tmp.toLowerCase().endsWith(".ttl") && !tmp.toLowerCase().endsWith(".owl")) {
			tmp += ext;
		}
		return tmp;
	}

	class ExportFileEvent implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			File file = getFile();
			if (file == null) {
				return;
			}
			try {
				String encoding = gmanager.getUserPrefs().get(PrefConstants.OutputEncoding, "UTF8");
				Writer writer = new OutputStreamWriter(new BufferedOutputStream(
						new FileOutputStream(file)), encoding);
				writeModel(getModel(), writer);
				writer.close();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}

	private int getSelectedCount() {
		int cnt = 0;
		if (rdfConvertBox.isSelected()) {
			cnt++;
		}
		if (classConvertBox.isSelected()) {
			cnt++;
		}
		if (propertyConvertBox.isSelected()) {
			cnt++;
		}
		return cnt;
	}

	class ExportImgEvent implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			if (getSelectedCount() != 1) {
				JOptionPane.showMessageDialog(gmanager.getDesktopTabbedPane(),
						"Check (RDF or Class or Property)", "", JOptionPane.ERROR_MESSAGE);
				return;
			}
			String fileType = "png";
			File file = getFile(fileType);
			if (file == null) {
				return;
			}
			try {
				BufferedImage img = null;
				if (rdfConvertBox.isSelected()
						&& gmanager.getCurrentRDFGraph().getModel().getRootCount() > 0) {
					img = GPConverter.toImage(gmanager.getCurrentRDFGraph());
				} else if (classConvertBox.isSelected()
						&& gmanager.getCurrentClassGraph().getModel().getRootCount() > 0) {
					img = GPConverter.toImage(gmanager.getCurrentClassGraph());
				} else if (propertyConvertBox.isSelected()
						&& gmanager.getCurrentPropertyGraph().getModel().getRootCount() > 0) {
					img = GPConverter.toImage(gmanager.getCurrentPropertyGraph());
				}
				if (img != null) {
					ImageIO.write(img, fileType, file);
				}
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}
		}
	}

	private void setRDFTreeRoot() {
		DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode();
		Map<Resource, Set<GraphCell>> map = new HashMap<Resource, Set<GraphCell>>();
		RDFGraph graph = gmanager.getCurrentRDFGraph();
		Object[] cells = graph.getAllCells();
		for (int i = 0; i < cells.length; i++) {
			GraphCell cell = (GraphCell) cells[i];
			if (RDFGraph.isRDFResourceCell(cell)) {
				RDFResourceInfo info = (RDFResourceInfo) GraphConstants.getValue(cell
						.getAttributes());
				Resource resType = info.getType();
				if (resType.getURI().length() == 0) {
					resType = RDFS.Resource;
				}
				Set<GraphCell> instanceSet = map.get(resType);
				if (instanceSet == null) {
					instanceSet = new HashSet<GraphCell>();
				}
				instanceSet.add(cell);
				map.put(resType, instanceSet);
			}
		}
		for (Iterator i = map.keySet().iterator(); i.hasNext();) {
			Object typeRes = i.next();
			Set instanceSet = map.get(typeRes);
			DefaultMutableTreeNode typeNode = new DefaultMutableTreeNode(typeRes);
			for (Iterator j = instanceSet.iterator(); j.hasNext();) {
				Object instance = j.next();
				DefaultMutableTreeNode instanceNode = new DefaultMutableTreeNode(instance);
				typeNode.add(instanceNode);
			}
			rootNode.add(typeNode);
		}
		treePanel.setNonRoot(rootNode);
	}

	private String getExtension() {
		if (xmlRadioButton.isSelected()) {
			return "rdf";
		} else if (n3RadioButton.isSelected() || n3PPRadioButton.isSelected()
				|| n3PLAINRadioButton.isSelected() || n3TRIPLERadioButton.isSelected()) {
			return "n3";
		} else if (nTripleRadioButton.isSelected()) {
			return "n3";
		} else if (turtleRadioButton.isSelected()) {
			return "ttl";
		}
		return "rdf";
	}

	private void writeModel(Model model, Writer writer) {
		String convertType = getConvertType();
		RDFWriter rdfWriter = model.getWriter(convertType);
		setNsPrefix(model);
		if (convertType.equals("RDF/XML") || convertType.equals("RDF/XML-ABBREV")) {
			if (xmlbaseCheckBox.isSelected()) {
				rdfWriter.setProperty("xmlbase", gmanager.getBaseURI());
			}
			rdfWriter.setProperty("showXmlDeclaration", Boolean.TRUE);
		}
		try {
			rdfWriter.write(model, writer, gmanager.getBaseURI());
		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(gmanager.getDesktopTabbedPane(), "Export Error", "",
					JOptionPane.ERROR_MESSAGE);
		}
	}

	private void writeModelToPager(Model model) {
		Writer writer = new StringWriter();
		writeModel(model, writer);
		exportTextArea.setText(writer.toString());
	}

	private String getConvertType() {
		if (xmlRadioButton.isSelected()) {
			if (abbrevCheckBox.isSelected()) {
				return "RDF/XML-ABBREV";
			}
			return "RDF/XML";
		} else if (n3RadioButton.isSelected()) {
			return "N3";
		} else if (n3PPRadioButton.isSelected()) {
			return "N3-PP";
		} else if (n3PLAINRadioButton.isSelected()) {
			return "N3-PLAIN";
		} else if (n3TRIPLERadioButton.isSelected()) {
			return "N3-TRIPLE";
		} else if (nTripleRadioButton.isSelected()) {
			return "N-TRIPLE";
		} else if (turtleRadioButton.isSelected()) {
			return "TURTLE";
		}
		return "RDF/XML";
	}

	private Model getModel() {
		Model model = ModelFactory.createDefaultModel();
		// treePanel.setNonRoot(null);

		if (rdfConvertBox.isSelected()) {
			if (selectedCheckBox.isSelected()) {
				model.add(mr3Writer.getSelectedRDFModel());
			} else {
				model.add(mr3Writer.getRDFModel());
			}
			if (getSelectedCount() == 1) {
				// treePanel.setRDFTreeCellRenderer();
				// setRDFTreeRoot();
			}
		}
		if (classConvertBox.isSelected()) {
			if (selectedCheckBox.isSelected()) {
				model.add(mr3Writer.getSelectedClassModel());
			} else {
				model.add(mr3Writer.getClassModel());
			}
			if (getSelectedCount() == 1) {
				// treePanel.setClassTreeCellRenderer();
				// treePanel.setRDFSTreeRoot(model, RDFS.Resource,
				// RDFS.subClassOf);
			}
		}
		if (propertyConvertBox.isSelected()) {
			if (selectedCheckBox.isSelected()) {
				model.add(mr3Writer.getSelectedPropertyModel());
			} else {
				model.add(mr3Writer.getPropertyModel());
			}
			if (getSelectedCount() == 1) {
				// treePanel.setPropertyTreeCellRenderer();
				// treePanel.setRDFSTreeRoot(model, MR3Resource.Property,
				// RDFS.subPropertyOf);
			}
		}

		if (getSelectedCount() == 1) {
			// treePanel.replaceNameSpace(treePanel.getRoot(),
			// gmanager.getPrefixNSInfoSet());
		}
		if (encodeCheckBox.isSelected()) {
			model = getEncodedModel(model);
		}

		return model;
	}

	private Model getEncodedModel(Model model) {
		Model encodedModel = ModelFactory.createDefaultModel();
		// String encoding =
		// gmanager.getUserPrefs().get(PrefConstants.OutputEncoding, "UTF8");
		for (StmtIterator i = model.listStatements(); i.hasNext();) {
			Statement stmt = (Statement) i.next();
			Resource subject = stmt.getSubject();
			if (!subject.isAnon()) {
				subject = ResourceFactory.createResource(URIref.encode(stmt.getSubject().getURI()));
			}
			Property predicate = ResourceFactory.createProperty(URIref.encode(stmt.getPredicate()
					.getURI()));
			RDFNode object = stmt.getObject();
			if (object.isResource() && !object.isAnon()) {
				object = ResourceFactory
						.createResource(URIref.encode(((Resource) object).getURI()));
			}
			encodedModel.add(subject, predicate, object);
		}
		return encodedModel;
	}

	private void setNsPrefix(Model model) {
		Set<PrefixNSInfo> prefixNsInfoSet = GraphUtilities.getPrefixNSInfoSet();
		for (PrefixNSInfo info : prefixNsInfoSet) {
			if (info.isAvailable()) {
				model.setNsPrefix(info.getPrefix(), info.getNameSpace());
			}
		}
	}

	public void actionPerformed(ActionEvent e) {
		writeModelToPager(getModel());
	}

	public void setFont(Font font) {
		super.setFont(font);
		exportTextArea.setFont(font);
	}

	public static void setText(String text) {
		exportTextArea.setText(text);
	}

	public void setVisible(boolean t) {
		if (t) {
			writeModelToPager(getModel());
			if (GraphUtilities.defaultFont != null) {
				setFont(GraphUtilities.defaultFont);
			}
		}
		super.setVisible(t);
	}
}
