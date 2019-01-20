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

import org.apache.jena.ontology.OntModel;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.mrcube.MR3;
import org.mrcube.io.MR3Reader;
import org.mrcube.jgraph.GraphManager;
import org.mrcube.models.MR3Constants;
import org.mrcube.models.PrefConstants;
import org.mrcube.utils.*;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.*;
import java.util.prefs.Preferences;
import java.util.regex.PatternSyntaxException;

/**
 * @author Takeshi Morita
 */
public class ImportDialog extends JDialog implements ActionListener {

    private final MR3Reader mr3Reader;
    private final GraphManager gmanager;

    private final JRadioButton syntaxXMLButton;
    private final JRadioButton syntaxNTripleButton;
    private final JRadioButton syntaxTurtleButton;

    private final JRadioButton importReplaceButton;
    private final JRadioButton importMergeButton;

    private final JRadioButton dataTypeRDFButton;
    private final JRadioButton dataTypeRDFSButton;
    private final JRadioButton dataTypeOWLButton;

    private final JList containerListUI;
    private final JButton addDirButton;
    private final JButton addURIButton;
    private final DefaultListModel containerListModel;

    private final JTextField findField;
    private Set<File> fileSet;
    private Set<String> uriSet;
    private final JList fileListUI;

    private final JComboBox filterBox;

    private final ChangeContainerAction changeContainerAction;

    private static final FileFilter owlFileFilter = new OWLFileFilter(false);
    private static final FileFilter rdfsFileFilter = new RDFsFileFilter(false);
    private static final FileFilter n3FileFilter = new NTripleFileFilter(false);
    private static final FileFilter turtleFileFilter = new TurtleFileFilter(false);

    private static final int FRAME_HEIGHT = 400;
    private static final int FRAME_WIDTH = 600;
    private static final ImageIcon IMPORT_ICON = Utilities.getImageIcon(Translator.getString("ImportDialog.Icon"));

    public ImportDialog(GraphManager gm) {
        super(gm.getRootFrame(), Translator.getString("ImportDialog.Title"), true);
        setIconImage(IMPORT_ICON.getImage());
        gmanager = gm;
        mr3Reader = new MR3Reader(gmanager);

        ActionListener changeFileFilterAction = new ChangeFileFilterAction();
        syntaxTurtleButton = new JRadioButton("Turtle");
        syntaxTurtleButton.addActionListener(changeFileFilterAction);
        syntaxXMLButton = new JRadioButton("XML");
        syntaxTurtleButton.setSelected(true);
        syntaxXMLButton.addActionListener(changeFileFilterAction);
        syntaxNTripleButton = new JRadioButton("N-Triple");
        syntaxNTripleButton.addActionListener(changeFileFilterAction);
        ButtonGroup group = new ButtonGroup();
        group.add(syntaxTurtleButton);
        group.add(syntaxXMLButton);
        group.add(syntaxNTripleButton);
        JPanel syntaxPanel = new JPanel();
        syntaxPanel.setLayout(new GridLayout(3, 1));
        syntaxPanel.setBorder(BorderFactory.createTitledBorder(Translator.getString("ImportDialog.Syntax")));
        syntaxPanel.add(syntaxTurtleButton);
        syntaxPanel.add(syntaxXMLButton);
        syntaxPanel.add(syntaxNTripleButton);
        importReplaceButton = new JRadioButton(Translator.getString("ImportDialog.ImportMethod.Replace"));
        importMergeButton = new JRadioButton(Translator.getString("ImportDialog.ImportMethod.Merge"));
        importMergeButton.setSelected(true);
        group = new ButtonGroup();
        group.add(importReplaceButton);
        group.add(importMergeButton);
        JPanel importMethodPanel = new JPanel();
        importMethodPanel.setLayout(new GridLayout(2, 1));
        importMethodPanel.setBorder(BorderFactory.createTitledBorder(Translator.getString("ImportDialog.ImportMethod")));
        importMethodPanel.add(importMergeButton);
        importMethodPanel.add(importReplaceButton);
        ActionListener dataTypeManagementAction = new DataTypeManagementAction();
        dataTypeRDFButton = new JRadioButton("RDF");
        dataTypeRDFButton.addActionListener(dataTypeManagementAction);
        // dataTypeRDFButton.setSelected(true);
        dataTypeRDFSButton = new JRadioButton("RDFS");
        dataTypeRDFSButton.addActionListener(dataTypeManagementAction);
        dataTypeRDFSButton.setSelected(true);
        dataTypeOWLButton = new JRadioButton("OWL");
        dataTypeOWLButton.addActionListener(dataTypeManagementAction);
        group = new ButtonGroup();
        group.add(dataTypeRDFButton);
        group.add(dataTypeRDFSButton);
        group.add(dataTypeOWLButton);
        JPanel dataTypePanel = new JPanel();
        dataTypePanel.setLayout(new GridLayout(3, 1));
        dataTypePanel.setBorder(BorderFactory.createTitledBorder(Translator.getString("ImportDialog.DataType")));
        dataTypePanel.add(dataTypeRDFButton);
        dataTypePanel.add(dataTypeRDFSButton);
        dataTypePanel.add(dataTypeOWLButton);
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new GridLayout(3, 1));
        mainPanel.add(syntaxPanel);
        mainPanel.add(dataTypePanel);
        mainPanel.add(importMethodPanel);
        mainPanel.setPreferredSize(new Dimension(150, 400));
        changeContainerAction = new ChangeContainerAction();
        containerListModel = new DefaultListModel();
        Set<String> containerSet = new TreeSet(Arrays.asList(gmanager.getResourceContainer()));
        if (!gmanager.getWorkDirectory().equals("")) {
            containerSet.add(gmanager.getWorkDirectory());
        }
        for (String s : containerSet) {
            containerListModel.addElement(s);
        }

        ActionListener addContainerAction = new AddContainerAction();
        addDirButton = new JButton(Translator.getString("ImportDialog.AddDir") + "(D)");
        addDirButton.setHorizontalAlignment(JButton.LEFT);
        addDirButton.setMnemonic('d');
        addDirButton.addActionListener(addContainerAction);
        addURIButton = new JButton(Translator.getString("ImportDialog.AddURI") + "(U)");
        addURIButton.setHorizontalAlignment(JButton.LEFT);
        addURIButton.setMnemonic('u');
        addURIButton.addActionListener(addContainerAction);
        JButton removeContainerButton = new JButton(MR3Constants.REMOVE + "(R)");
        removeContainerButton.setHorizontalAlignment(JButton.LEFT);
        removeContainerButton.setMnemonic('r');
        removeContainerButton.addActionListener(new RemoveContainerListAction());
        JPanel containerButtonPanel = new JPanel();
        containerButtonPanel.setLayout(new GridLayout(3, 1, 5, 5));
        containerButtonPanel.add(addDirButton);
        containerButtonPanel.add(addURIButton);
        containerButtonPanel.add(removeContainerButton);
        JComponent containerButtonPanelNorth = Utilities.createNorthPanel(containerButtonPanel);

        containerListUI = new JList(containerListModel);
        containerListUI.setSelectedIndex(0);
        containerListUI.addListSelectionListener(changeContainerAction);
        JScrollPane containerListUIScroll = new JScrollPane(containerListUI);
        containerListUIScroll.setBorder(BorderFactory.createTitledBorder(Translator.getString("ImportDialog.containerList")));
        JPanel containerListPanel = new JPanel();
        containerListPanel.setMinimumSize(new Dimension(FRAME_WIDTH, 120));
        containerListPanel.setLayout(new BorderLayout());
        containerListPanel.add(containerListUIScroll, BorderLayout.CENTER);
        containerListPanel.add(containerButtonPanelNorth, BorderLayout.EAST);
        JLabel findLabel = new JLabel(Translator.getString("Component.Edit.FindResource.Text") + ": ");
        findField = new JTextField();
        findField.getDocument().addDocumentListener(new FindAction());
        JPanel findPanel = new JPanel();
        findPanel.setLayout(new BorderLayout());
        findPanel.add(findLabel, BorderLayout.WEST);
        findPanel.add(findField, BorderLayout.CENTER);
        fileListUI = new JList();
        fileListUI.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        JScrollPane fileListScroll = new JScrollPane(fileListUI);
        fileListScroll.setBorder(BorderFactory.createTitledBorder(Translator.getString("ImportDialog.ImportFileList")));
        filterBox = new JComboBox(new Object[]{rdfsFileFilter, n3FileFilter, turtleFileFilter, owlFileFilter, "All Files"});
        filterBox.addActionListener(changeContainerAction);
        filterBox.setSelectedItem(turtleFileFilter);
        JPanel fileListPanel = new JPanel();
        fileListPanel.setLayout(new BorderLayout());
        fileListPanel.add(fileListScroll, BorderLayout.CENTER);
        fileListPanel.add(filterBox, BorderLayout.SOUTH);
        JButton reloadButton = new JButton(MR3Constants.RELOAD + "(L)");
        reloadButton.setMnemonic('l');
        reloadButton.addActionListener(changeContainerAction);
        JComponent reloadButtonPanel = Utilities.createEastPanel(reloadButton);
        JPanel selectFilePanel = new JPanel();
        selectFilePanel.setLayout(new BorderLayout());
        selectFilePanel.add(findPanel, BorderLayout.NORTH);
        selectFilePanel.add(fileListPanel, BorderLayout.CENTER);
        selectFilePanel.add(reloadButtonPanel, BorderLayout.SOUTH);
        JButton importButton = new JButton(Translator.getString("Component.File.Import.Text") + "(I)", IMPORT_ICON);
        importButton.setMnemonic('i');
        importButton.addActionListener(this);
        JButton cancelButton = new JButton(MR3Constants.CANCEL);
        cancelButton.setMnemonic('c');
        cancelButton.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
            }
        });
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(1, 2, 5, 5));
        buttonPanel.add(importButton);
        buttonPanel.add(cancelButton);

        Container contentPane = getContentPane();
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, mainPanel, selectFilePanel);
        JSplitPane centerSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, containerListPanel, splitPane);
        centerSplitPane.setDividerLocation(0.5);
        contentPane.add(centerSplitPane, BorderLayout.CENTER);
        contentPane.add(Utilities.createEastPanel(buttonPanel), BorderLayout.SOUTH);
        Dimension size = new Dimension(FRAME_WIDTH, FRAME_HEIGHT);
        setSize(size);
        setLocationRelativeTo(gmanager.getRootFrame());
        setVisible(false);
    }

    class RemoveContainerListAction implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            for (Object selectedValue : containerListUI.getSelectedValuesList()) {
                if (selectedValue.equals(gmanager.getWorkDirectory())) {
                    continue;
                }
                containerListModel.removeElement(selectedValue);
            }
            gmanager.setResourceContainer(containerListModel);
        }
    }

    public void setVisible(boolean t) {
        if (t) {
            changeContainerAction.changeContainerList();
        }
        super.setVisible(t);
    }

    class AddContainerAction implements ActionListener {

        private File getDirectory() {
            Preferences userPrefs = gmanager.getUserPrefs();
            JFileChooser jfc = new JFileChooser(userPrefs.get(PrefConstants.WorkDirectory, ""));
            jfc.setFileHidingEnabled(true);
            jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            if (jfc.showOpenDialog(MR3.getCurrentProject()) == JFileChooser.APPROVE_OPTION) {
                return jfc.getSelectedFile();
            }
            return null;
        }

        private boolean isExistDir(String path) {
            for (int i = 0; i < containerListModel.getSize(); i++) {
                if (containerListModel.getElementAt(i).equals(path)) {
                    return true;
                }
            }
            return false;
        }

        private boolean isExistURI(URL url) {
            for (int i = 0; i < containerListModel.getSize(); i++) {
                if (containerListModel.getElementAt(i).equals(url.toString())) {
                    return true;
                }
            }
            return false;
        }

        private void addContainer(Object container) {
            containerListModel.addElement(container);
            containerListUI.setSelectedIndex(containerListModel.getSize() - 1);
            gmanager.setResourceContainer(containerListModel);
        }

        public void actionPerformed(ActionEvent e) {
            if (e.getSource() == addDirButton) {
                File dir = getDirectory();
                if (dir != null && !isExistDir(dir.getAbsolutePath())) {
                    addContainer(dir.getAbsolutePath());
                }
            } else if (e.getSource() == addURIButton) {
                try {
                    String uri = JOptionPane.showInputDialog("Input URI ( exp. https://creativecommons.org/schema.rdf )");
                    if (uri == null) {
                        return;
                    }
                    URL url = getURI(uri);
                    if (url != null && !isExistURI(url)) {
                        addContainer(url.toString());
                    }
                } catch (MalformedURLException mre) {
                    mre.printStackTrace();
                }
            }
        }
    }

    class ChangeFileFilterAction implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            if (syntaxXMLButton.isSelected()) {
                filterBox.setSelectedItem(rdfsFileFilter);
            } else if (syntaxNTripleButton.isSelected()) {
                filterBox.setSelectedItem(n3FileFilter);
            } else if (syntaxTurtleButton.isSelected()) {
                filterBox.setSelectedItem(turtleFileFilter);
            }
        }
    }

    class ChangeContainerAction implements ActionListener, ListSelectionListener {

        private void changeContainerList() {
            List selectedContainers = containerListUI.getSelectedValuesList();
            if (selectedContainers == null) {
                return;
            }
            uriSet = new TreeSet<>();
            fileSet = new TreeSet<>();
            for (Object selectedContainer : selectedContainers) {
                File selectedDirectory = new File(selectedContainer.toString());
                if (!selectedDirectory.isDirectory()) {
                    uriSet.add(selectedContainer.toString());
                } else {
                    if (filterBox.getSelectedItem() instanceof java.io.FileFilter) {
                        fileSet.addAll(Arrays.asList(selectedDirectory.listFiles((java.io.FileFilter) filterBox.getSelectedItem())));
                    } else {
                        fileSet.addAll(Arrays.asList(selectedDirectory.listFiles()));
                    }
                }
            }
            setFindList();
        }

        public void valueChanged(ListSelectionEvent e) {
            changeContainerList();
        }

        public void actionPerformed(ActionEvent e) {
            changeContainerList();
        }
    }

    private int getDirNum(String dir) {
        for (int i = 0; i < containerListModel.getSize(); i++) {
            if (containerListModel.get(i).equals(dir)) {
                return i;
            }
        }
        return -1;
    }

    private void setFindList() {
        int[] lastSelectedIndices = fileListUI.getSelectedIndices();
        Set<String> fileNameSet = new TreeSet<>();
        for (File file : fileSet) {
            String regex = ".*";
            if (findField.getText().length() != 0) {
                regex = findField.getText();
            }
            if (!findField.getText().matches("\\|")) {
                regex += ".*";
            }
            try {
                if (file.getName().matches(regex)) {
                    // System.out.println(file.getParent());
                    // System.out.println(getDirNum(file.getParent()));
                    fileNameSet.add("[Dir" + getDirNum(file.getParent()) + "] " + file.getName());
                }
            } catch (PatternSyntaxException e) {
                // ignore
            }
        }
        fileListUI.setListData(fileNameSet.toArray());
        if (0 < fileNameSet.size()) {
            fileListUI.setSelectedIndices(lastSelectedIndices);
        }
    }

    class FindAction implements DocumentListener {

        public void changedUpdate(DocumentEvent e) {
            setFindList();
        }

        public void insertUpdate(DocumentEvent e) {
            setFindList();
        }

        public void removeUpdate(DocumentEvent e) {
            setFindList();
        }
    }

    private URL getURI(String uri) throws MalformedURLException {
        Preferences userPrefs = gmanager.getUserPrefs();
        URL rdfURI = null;
        boolean isProxy = userPrefs.getBoolean("Proxy", false);
        if (isProxy) {
            String proxyURL = userPrefs.get(PrefConstants.ProxyHost, "http://localhost");
            int proxyPort = userPrefs.getInt(PrefConstants.ProxyPort, 8080);
            rdfURI = new URL("http", proxyURL, proxyPort, uri);
        } else {
            rdfURI = new URL(uri);
        }
        return rdfURI;
    }

    private File getFile(String file) {
        String[] tokens = file.split("] ");
        // System.out.println(tokens[0].substring(4));
        // System.out.println(tokens[1]);
        String fileName = tokens[1];
        String dirPath = containerListModel.get(Integer.parseInt(tokens[0].substring(4))).toString();
        return new File(dirPath, fileName);
    }

    private Set<InputStream> getFileInputStreamSet() {
        Set<InputStream> inputStreamSet = new HashSet<>();
        if (fileListUI.isSelectionEmpty()) {
            return inputStreamSet;
        }
        for (Object fileObj : fileListUI.getSelectedValuesList()) {
            File file = getFile(fileObj.toString());
            if (file == null || file.isDirectory()) {
                continue;
            }
            try {
                inputStreamSet.add(new BufferedInputStream(new FileInputStream(file)));
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        return inputStreamSet;
    }

    private Set<InputStream> getURIInputStreamSet() {
        Set<InputStream> inputStreamSet = new HashSet<>();
        for (String uri : uriSet) {
            if (uri == null) {
                return null;
            }
            try {
                inputStreamSet.add(new BufferedInputStream(getURI(uri).openStream()));
            } catch (UnknownHostException uhe) {
                Utilities.showErrorMessageDialog("Unknown Host (Proxy)");
            } catch (MalformedURLException uriex) {
                uriex.printStackTrace();
            } catch (IOException ioe) {
                Utilities.showErrorMessageDialog("File Not Found");
            }
        }
        return inputStreamSet;
    }

    private Model readModel(Model model) {
        Set<InputStream> inputStreamSet = getURIInputStreamSet();
        inputStreamSet.addAll(getFileInputStreamSet());
        if (inputStreamSet.size() == 0) {
            return null;
        }
        for (InputStream is : inputStreamSet) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder();
            String line = "";
            try {
                while ((line = reader.readLine()) != null) {
                    builder.append(line);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            String decodedText = null;
            decodedText = URLDecoder.decode(builder.toString(), StandardCharsets.UTF_8);
            StringReader r = new StringReader(decodedText);
            model.read(r, gmanager.getBaseURI(), getSyntax());
        }
        return model;
    }

    private String getSyntax() {
        if (syntaxXMLButton.isSelected()) {
            return "RDF/XML";
        } else if (syntaxNTripleButton.isSelected()) {
            return "N-TRIPLE";
        } else if (syntaxTurtleButton.isSelected()) {
            return "TURTLE";
        } else {
            return "RDF/XML";
        }
    }

    class DataTypeManagementAction implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            boolean isDataTypeOWL = dataTypeOWLButton.isSelected();
            importMergeButton.setSelected(isDataTypeOWL);
            importMergeButton.setEnabled(!isDataTypeOWL);
            importReplaceButton.setEnabled(!isDataTypeOWL);
            if (dataTypeRDFButton.isSelected() || dataTypeRDFSButton.isSelected()) {
                if (syntaxNTripleButton.isSelected()) {
                    filterBox.setSelectedItem(n3FileFilter);
                } else {
                    filterBox.setSelectedItem(rdfsFileFilter);
                }
            } else if (dataTypeOWLButton.isSelected()) {
                filterBox.setSelectedItem(owlFileFilter);
            }
            setFindList();
        }
    }

    public void actionPerformed(ActionEvent e) {
        Model model = null;
        try {
            if (dataTypeOWLButton.isSelected()) {
                model = readModel(ModelFactory.createOntologyModel());
            } else {
                model = readModel(ModelFactory.createDefaultModel());
            }
            if (model == null) {
                return;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            setVisible(false);
        }

        if (importReplaceButton.isSelected()) {
            if (dataTypeRDFButton.isSelected()) {
                mr3Reader.replaceRDFModel(model);
            } else if (dataTypeRDFSButton.isSelected()) {
                mr3Reader.replaceRDFSModel(model);
            }
        } else if (importMergeButton.isSelected()) {
            if (dataTypeRDFButton.isSelected()) {
                mr3Reader.mergeRDFModelThread(model);
            } else if (dataTypeRDFSButton.isSelected()) {
                mr3Reader.mergeRDFPlusRDFSModel(model);
            } else if (dataTypeOWLButton.isSelected()) {
                mr3Reader.mergeOntologyModel((OntModel) model);
            }
        }
    }
}
