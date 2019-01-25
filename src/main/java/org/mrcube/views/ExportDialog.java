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

import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.rdf.model.*;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.shared.InvalidPropertyURIException;
import org.apache.jena.util.URIref;
import org.apache.jena.vocabulary.RDFS;
import org.jgraph.graph.GraphCell;
import org.jgraph.graph.GraphConstants;
import org.mrcube.MR3;
import org.mrcube.io.MR3Writer;
import org.mrcube.jgraph.GraphManager;
import org.mrcube.jgraph.RDFGraph;
import org.mrcube.models.MR3Constants;
import org.mrcube.models.NamespaceModel;
import org.mrcube.models.PrefConstants;
import org.mrcube.models.RDFResourceModel;
import org.mrcube.utils.*;
import org.mrcube.utils.file_filter.*;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author Takeshi Morita
 */
public class ExportDialog extends JDialog implements ActionListener {

    private final MR3Writer mr3Writer;
    private final GraphManager gmanager;
    private MR3TreePanel treePanel;

    private final JRadioButton turtleRadioButton;
    private final JRadioButton jsonldRadioButton;
    private final JRadioButton xmlRadioButton;
    private final JRadioButton nTripleRadioButton;

    private final JCheckBox rdfConvertBox;
    private final JCheckBox classConvertBox;
    private final JCheckBox propertyConvertBox;

    private final JCheckBox encodeCheckBox;
    private final JCheckBox selectedCheckBox;
    private final JCheckBox abbrevCheckBox;
    private final JCheckBox xmlbaseCheckBox;

    private static JTextArea exportTextArea;
    private static final int FRAME_HEIGHT = 500;
    private static final int FRAME_WIDTH = 600;
    private static final ImageIcon EXPORT_ICON = Utilities.getImageIcon(Translator.getString("Component.File.Export.Icon"));
    private static final ImageIcon FILE_ICON = Utilities.getImageIcon("page_white_text.png");
    private static final ImageIcon IMAGE_ICON = Utilities.getImageIcon("image.png");

    public ExportDialog(GraphManager gm) {
        super(gm.getRootFrame(), Translator.getString("ExportDialog.Title"), true);
        setIconImage(EXPORT_ICON.getImage());

        gmanager = gm;
        mr3Writer = new MR3Writer(gmanager);

        turtleRadioButton = new JRadioButton("Turtle");
        turtleRadioButton.addActionListener(this);
        turtleRadioButton.setSelected(true);
        jsonldRadioButton = new JRadioButton("JSONLD");
        jsonldRadioButton.addActionListener(this);
        xmlRadioButton = new JRadioButton("XML");
        xmlRadioButton.addActionListener(this);
        nTripleRadioButton = new JRadioButton("N-Triples");
        nTripleRadioButton.addActionListener(this);
        ButtonGroup group = new ButtonGroup();
        group.add(turtleRadioButton);
        group.add(jsonldRadioButton);
        group.add(xmlRadioButton);
        group.add(nTripleRadioButton);
        JPanel outputCheckPanel = new JPanel();
        outputCheckPanel.setLayout(new GridLayout(1, 4));
        outputCheckPanel.setBorder(BorderFactory.createTitledBorder(Translator.getString("ImportDialog.Syntax")));
        outputCheckPanel.add(turtleRadioButton);
        outputCheckPanel.add(jsonldRadioButton);
        outputCheckPanel.add(xmlRadioButton);
        outputCheckPanel.add(nTripleRadioButton);

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
        dataTypePanel.setBorder(BorderFactory.createTitledBorder(Translator.getString("ImportDialog.DataType")));
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
        optionCheckPanel.setBorder(BorderFactory.createTitledBorder(Translator.getString("ExportDialog.Option")));
        optionCheckPanel.setLayout(new GridLayout(2, 2));
        optionCheckPanel.add(encodeCheckBox);
        optionCheckPanel.add(selectedCheckBox);
        optionCheckPanel.add(abbrevCheckBox);
        optionCheckPanel.add(xmlbaseCheckBox);

        JButton exportFileButton = new JButton(Translator.getString("ExportDialog.File") + "(F)", FILE_ICON);
        exportFileButton.setHorizontalAlignment(JButton.LEFT);
        exportFileButton.setMnemonic('f');
        exportFileButton.addActionListener(new ExportFileEvent());
        JButton exportImgButton = new JButton(Translator.getString("ExportDialog.Image") + "(I)", IMAGE_ICON);
        exportImgButton.setHorizontalAlignment(JButton.LEFT);
        exportImgButton.setMnemonic('i');
        exportImgButton.addActionListener(new ExportImgEvent());

        JButton reloadButton = new JButton(MR3Constants.RELOAD + "(L)");
        reloadButton.setMnemonic('l');
        reloadButton.addActionListener(this);
        JButton cancelButton = new JButton(MR3Constants.CANCEL);
        cancelButton.setMnemonic('c');
        cancelButton.addActionListener(e -> setVisible(false));
        JPanel otherButtonPanel = new JPanel();
        otherButtonPanel.setLayout(new GridLayout(2, 1, 5, 5));
        otherButtonPanel.add(reloadButton);
        otherButtonPanel.add(cancelButton);

        JPanel exportButtonPanel = new JPanel();
        exportButtonPanel.setLayout(new GridLayout(2, 1, 5, 5));
        exportButtonPanel.setBorder(BorderFactory.createTitledBorder(Translator.getString("Component.File.Export.Text")));
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
        JSplitPane sourcePane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, Utilities.createWestPanel(settingPanel),
                new JScrollPane(exportTextArea));
        sourcePane.setOneTouchExpandable(true);

        setContentPane(sourcePane);
        Dimension size = new Dimension(FRAME_WIDTH, FRAME_HEIGHT);
        setSize(size);
        setLocationRelativeTo(gmanager.getRootFrame());
        setVisible(false);
    }

    private static final RDFsFileFilter rdfsFileFilter = new RDFsFileFilter(true);
    private static final NTripleFileFilter n3FileFilter = new NTripleFileFilter(true);
    private static final TurtleFileFilter turtleFileFilter = new TurtleFileFilter(true);
    private static final JSONLDFileFilter jsonldFileFilter = new JSONLDFileFilter(true);
    private static final OWLFileFilter owlFileFilter = new OWLFileFilter(true);
    private static final PNGFileFilter pngFileFilter = new PNGFileFilter();

    private File getFile() {
        return getFile(getExtension());
    }

    private File getFile(String extension) {
        JFileChooser jfc = new JFileChooser(gmanager.getUserPrefs().get(PrefConstants.WorkDirectory, ""));
        switch (extension) {
            case "rdf":
                jfc.addChoosableFileFilter(rdfsFileFilter);
                jfc.addChoosableFileFilter(owlFileFilter);
                break;
            case "n3":
                jfc.setFileFilter(n3FileFilter);
                break;
            case "ttl":
                jfc.setFileFilter(turtleFileFilter);
                break;
            case "jsonld":
                jfc.setFileFilter(jsonldFileFilter);
            case "png":
                jfc.setFileFilter(pngFileFilter);
                break;
        }

        if (jfc.showSaveDialog(MR3.getCurrentProject()) == JFileChooser.APPROVE_OPTION) {
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
        if (extension != null && !tmp.toLowerCase().endsWith(".rdf") && !tmp.toLowerCase().endsWith(".rdfs")
                && !tmp.toLowerCase().endsWith(".n3") && !tmp.toLowerCase().endsWith(".ttl")
                && !tmp.toLowerCase().endsWith(".jsonld")
                && !tmp.toLowerCase().endsWith(".owl")) {
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
            String encoding = gmanager.getUserPrefs().get(PrefConstants.OutputEncoding, "UTF8");
            try (Writer writer = new OutputStreamWriter(new BufferedOutputStream(new FileOutputStream(file)), encoding)) {
                writer.write(getModelString(getModel()));
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
                Utilities.showErrorMessageDialog("Check (RDF or Class or Property)");
                return;
            }
            String fileType = "png";
            File file = getFile(fileType);
            if (file == null) {
                return;
            }
            try {
                BufferedImage img = null;
                if (rdfConvertBox.isSelected() && gmanager.getCurrentRDFGraph().getModel().getRootCount() > 0) {
                    img = GPConverter.toImage(gmanager.getCurrentRDFGraph());
                } else if (classConvertBox.isSelected() && gmanager.getCurrentClassGraph().getModel().getRootCount() > 0) {
                    img = GPConverter.toImage(gmanager.getCurrentClassGraph());
                } else if (propertyConvertBox.isSelected() && gmanager.getCurrentPropertyGraph().getModel().getRootCount() > 0) {
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
        Map<Resource, Set<GraphCell>> map = new HashMap<>();
        RDFGraph graph = gmanager.getCurrentRDFGraph();
        Object[] cells = graph.getAllCells();
        for (Object cell1 : cells) {
            GraphCell cell = (GraphCell) cell1;
            if (RDFGraph.isRDFResourceCell(cell)) {
                RDFResourceModel info = (RDFResourceModel) GraphConstants.getValue(cell.getAttributes());
                Resource resType = info.getType();
                if (resType.getURI().length() == 0) {
                    resType = RDFS.Resource;
                }
                Set<GraphCell> instanceSet = map.get(resType);
                if (instanceSet == null) {
                    instanceSet = new HashSet<>();
                }
                instanceSet.add(cell);
                map.put(resType, instanceSet);
            }
        }
        for (Object typeRes : map.keySet()) {
            Set instanceSet = map.get(typeRes);
            DefaultMutableTreeNode typeNode = new DefaultMutableTreeNode(typeRes);
            for (Object instance : instanceSet) {
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
        } else if (nTripleRadioButton.isSelected()) {
            return "n3";
        } else if (turtleRadioButton.isSelected()) {
            return "ttl";
        } else if (jsonldRadioButton.isSelected()) {
            return "jsonld";
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
            Utilities.showErrorMessageDialog("Export Error");
        }
    }

    private String getModelString(Model model) {
        Writer writer = new StringWriter();
        writeModel(model, writer);
        if (!encodeCheckBox.isSelected()) {
            return URLDecoder.decode(writer.toString(), StandardCharsets.UTF_8);
        }
        return writer.toString();
    }

    private String getConvertType() {
        if (xmlRadioButton.isSelected()) {
            if (abbrevCheckBox.isSelected()) {
                return "RDF/XML-ABBREV";
            }
            return "RDF/XML";
        } else if (nTripleRadioButton.isSelected()) {
            return "N-TRIPLE";
        } else if (turtleRadioButton.isSelected()) {
            return "TURTLE";
        } else if (jsonldRadioButton.isSelected()) {
            return "JSONLD";
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
            // gmanager.getNamespaceModelSet());
        }
        if (encodeCheckBox.isSelected()) {
            model = getEncodedModel(model);
        }

        return model;
    }

    private Model getEncodedModel(Model model) {
        Model encodedModel = ModelFactory.createDefaultModel();
        for (Statement stmt : model.listStatements().toList()) {
            Resource subject = stmt.getSubject();
            if (!subject.isAnon()) {
                subject = ResourceFactory.createResource(URIref.encode(stmt.getSubject().getURI()));
            }
            Property predicate = stmt.getPredicate();
            try {
                predicate = ResourceFactory.createProperty(URIref.encode(stmt.getPredicate().getURI()));
            } catch (InvalidPropertyURIException ipue) {
                ipue.printStackTrace();
            }
            RDFNode object = stmt.getObject();
            if (object.isResource() && !object.isAnon()) {
                object = ResourceFactory.createResource(URIref.encode(((Resource) object).getURI()));
            } else if (object.isLiteral()) {
                Literal literal = object.asLiteral();
                RDFDatatype dType = literal.getDatatype();
                String lang = literal.getLanguage();
                String text = URIref.encode(literal.getString());
                if (dType != null) {
                    object = ResourceFactory.createTypedLiteral(text, dType);
                } else if (0 < lang.length()) {
                    object = ResourceFactory.createLangLiteral(text, lang);
                } else {
                    object = ResourceFactory.createPlainLiteral(text);
                }
            }
            encodedModel.add(subject, predicate, object);
        }
        return encodedModel;
    }

    private void setNsPrefix(Model model) {
        Set<NamespaceModel> namespaceModelSet = GraphUtilities.getNamespaceModelSet();
        for (NamespaceModel info : namespaceModelSet) {
            if (info.isAvailable()) {
                model.setNsPrefix(info.getPrefix(), info.getNameSpace());
            }
        }
    }

    public void actionPerformed(ActionEvent e) {
        exportTextArea.setText(getModelString(getModel()));
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
            exportTextArea.setText(getModelString(getModel()));
            if (GraphUtilities.defaultFont != null) {
                setFont(GraphUtilities.defaultFont);
            }
        }
        super.setVisible(t);
    }
}
