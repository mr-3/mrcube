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

import org.apache.jena.rdf.model.ResourceFactory;
import org.jgraph.graph.GraphCell;
import org.mrcube.jgraph.*;
import org.mrcube.models.MR3Constants;
import org.mrcube.models.MR3Constants.GraphType;
import org.mrcube.models.NamespaceModel;
import org.mrcube.utils.GraphUtilities;
import org.mrcube.utils.PrefixNSUtil;
import org.mrcube.utils.Translator;
import org.mrcube.utils.Utilities;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.*;

/**
 * @author Takeshi Morita
 */
public class FindResourceDialog extends JDialog {

    private JTextField findField;
    private final JTextField findLabelField;
    private final JTextField findCommentField;
    private Set<NamespaceModel> namespaceModelSet;
    private JComboBox uriPrefixBox;
    private final JList<GraphCell> resourceList;
    private DefaultListModel<GraphCell> resourceListModel;

    private JCheckBox rdfCheckBox;
    private JCheckBox classCheckBox;
    private JCheckBox propertyCheckBox;

    private final GraphManager gmanager;

    private static final int BOX_WIDTH = 100;
    private static final int BOX_HEIGHT = 30;
    private static final int LIST_WIDTH = 300;
    private static final int LIST_HEIGHT = 120;
    private static final int FIELD_HEIGHT = 30;

    public FindResourceDialog(GraphManager gm) {
        super(gm.getRootFrame(), Translator.getString("FindResourceDialog.Title"), false);
        setIconImage(Utilities.getImageIcon(Translator.getString("FindResourceDialog.Icon")).getImage());
        Container contentPane = getContentPane();

        gmanager = gm;
        JComponent buttonGroupPanel = getButtonGroupPanel();
        JComponent findAreaPanel = getFindAreaPanel();

        findLabelField = new JTextField();
        findLabelField.getDocument().addDocumentListener(new FindAction(FindActionType.LABEL));
        JComponent findLabelFieldP = Utilities.createTitledPanel(findLabelField, MR3Constants.LABEL, LIST_WIDTH, FIELD_HEIGHT);
        findCommentField = new JTextField();
        findCommentField.getDocument().addDocumentListener(new FindAction(FindActionType.COMMENT));
        JComponent findCommentFieldP = Utilities.createTitledPanel(findCommentField, MR3Constants.COMMENT, LIST_WIDTH, FIELD_HEIGHT);

        JPanel northPanel = new JPanel();
        northPanel.setLayout(new BoxLayout(northPanel, BoxLayout.Y_AXIS));
        northPanel.add(buttonGroupPanel);
        northPanel.add(findAreaPanel);
        northPanel.add(findLabelFieldP);
        northPanel.add(findCommentFieldP);

        resourceListModel = new DefaultListModel<>();
        resourceList = new JList(resourceListModel);
        resourceList.setCellRenderer(new ResourceListCellRenderer());
        resourceList.addListSelectionListener(new JumpAction());
        JComponent resourceListP = Utilities.createTitledPanel(new JScrollPane(resourceList),
                Translator.getString("FindResult"), LIST_WIDTH, LIST_HEIGHT);

        JButton cancelButton = new JButton(MR3Constants.CANCEL);
        cancelButton.setMnemonic('c');
        cancelButton.addActionListener(e -> setVisible(false));
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BorderLayout());
        buttonPanel.add(cancelButton, BorderLayout.EAST);

        contentPane.setLayout(new BorderLayout());
        contentPane.add(northPanel, BorderLayout.NORTH);
        contentPane.add(resourceListP, BorderLayout.CENTER);
        contentPane.add(buttonPanel, BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(gmanager.getRootFrame());
        setVisible(false);
    }

    private JComponent getButtonGroupPanel() {
        FindAreaCheck findAreaCheck = new FindAreaCheck();
        rdfCheckBox = new JCheckBox("RDF");
        rdfCheckBox.setSelected(true);
        rdfCheckBox.addItemListener(findAreaCheck);
        classCheckBox = new JCheckBox(Translator.getString("Class"));
        classCheckBox.addItemListener(findAreaCheck);
        propertyCheckBox = new JCheckBox(Translator.getString("Property"));
        propertyCheckBox.addItemListener(findAreaCheck);

        JPanel buttonGroupPanel = new JPanel();
        buttonGroupPanel.setLayout(new GridLayout(1, 3));
        buttonGroupPanel.setBorder(BorderFactory.createTitledBorder(Translator.getString("GraphType")));
        buttonGroupPanel.add(rdfCheckBox);
        buttonGroupPanel.add(classCheckBox);
        buttonGroupPanel.add(propertyCheckBox);

        return Utilities.createWestPanel(buttonGroupPanel);
    }

    private JComponent getFindAreaPanel() {
        uriPrefixBox = new JComboBox();
        uriPrefixBox.addActionListener(new ChangePrefixAction());
        JComponent uriPrefixBoxP = Utilities.createTitledPanel(uriPrefixBox, MR3Constants.PREFIX, BOX_WIDTH, BOX_HEIGHT);

        findField = new JTextField();
        findField.getDocument().addDocumentListener(new FindAction(FindActionType.URI));
        JComponent findFieldP = Utilities.createTitledPanel(findField, "URI", LIST_WIDTH, FIELD_HEIGHT);

        JPanel inlinePanel = new JPanel();
        inlinePanel.setLayout(new BoxLayout(inlinePanel, BoxLayout.X_AXIS));
        inlinePanel.add(uriPrefixBoxP);
        inlinePanel.add(findFieldP);
        return inlinePanel;
    }

    public void setAllCheckBoxSelected(boolean t) {
        rdfCheckBox.setSelected(t);
        classCheckBox.setSelected(t);
        propertyCheckBox.setSelected(t);
    }

    public void setFindArea(GraphType type) {
        findField.setText("");
        setAllCheckBoxSelected(false);

        if (type == null) {
            setAllCheckBoxSelected(true);
        } else if (type == GraphType.RDF) {
            rdfCheckBox.setSelected(true);
        } else if (type == GraphType.CLASS) {
            classCheckBox.setSelected(true);
        } else if (type == GraphType.PROPERTY) {
            propertyCheckBox.setSelected(true);
        }
    }

    public void setURIPrefixBox() {
        namespaceModelSet = GraphUtilities.getNamespaceModelSet();
        PrefixNSUtil.setNamespaceModelSet(namespaceModelSet);
        uriPrefixBox.setModel(new DefaultComboBoxModel(PrefixNSUtil.getPrefixes().toArray()));
    }

    public void setVisible(boolean aFlag) {
        if (aFlag) {
            setURIPrefixBox();
        }
        super.setVisible(aFlag);
    }

    class ChangePrefixAction extends AbstractAction {
        public void actionPerformed(ActionEvent e) {
            if (e.getSource() == uriPrefixBox) {
                String ns = PrefixNSUtil.getNameSpace((String) uriPrefixBox.getSelectedItem());
                String id = ResourceFactory.createResource(findField.getText()).getLocalName();
                findField.setText(ns + id);
                setFindList(FindActionType.URI);
            }
        }
    }

    class FindAreaCheck implements ItemListener {
        public void itemStateChanged(ItemEvent e) {
            setFindList(FindActionType.URI);
        }
    }

    public Vector<GraphCell> getFindResources(String key, FindActionType type) {
        Map<String, GraphCell> resourceMap = new TreeMap<>();
        if (key.isEmpty()) {
            return new Vector<>(resourceMap.values());
        }
        key = resolvePrefix(key);
        if (rdfCheckBox.isSelected()) {
            Set<GraphCell> rdfCellSet = null;
            if (type == FindActionType.URI) {
                rdfCellSet = gmanager.getFindRDFResult(key);
            } else if (type == FindActionType.LABEL || type == FindActionType.COMMENT) {
                rdfCellSet = gmanager.getFindRDFResult(key, type);
            }
            for (GraphCell rdfCell : rdfCellSet) {
                resourceMap.put(rdfCell.toString(), rdfCell);
            }
        }
        if (classCheckBox.isSelected()) {
            Set<GraphCell> classCellSet = gmanager.getFindClassResult(key, type);
            for (GraphCell classCell : classCellSet) {
                resourceMap.put(classCell.toString(), classCell);
            }
        }
        if (propertyCheckBox.isSelected()) {
            Set<GraphCell> propertyCellSet = gmanager.getFindPropertyResult(key, type);
            for (GraphCell propertyCell : propertyCellSet) {
                resourceMap.put(propertyCell.toString(), propertyCell);
            }
        }
        return new Vector<>(resourceMap.values());
    }

    private String resolvePrefix(String key) {
        String[] tokens = key.split(":");
        if (tokens.length != 2) {
            return key;
        }
        String prefix = tokens[0];
        String id = tokens[1];
        if (prefix.equals("http")) {
            return key;
        }
        for (NamespaceModel info : namespaceModelSet) {
            if (info.getPrefix().equals(prefix)) {
                return info.getNameSpace() + id;
            }
        }
        return key;
    }

    private void setFindList(FindActionType type) {
        resourceListModel.clear();
        Vector<GraphCell> findList = new Vector<>();
        if (type == FindActionType.URI) {
            findList = getFindResources(findField.getText(), type);
        } else if (type == FindActionType.LABEL) {
            findList = getFindResources(findLabelField.getText(), type);
        } else if (type == FindActionType.COMMENT) {
            findList = getFindResources(findCommentField.getText(), type);
        }
        resourceListModel.addAll(findList);
        if (0 < resourceList.getModel().getSize()) {
            resourceList.setSelectedIndex(0);
        }
    }

    public enum FindActionType {
        URI, LABEL, COMMENT
    }

    class FindAction implements DocumentListener {
        private final FindActionType type;

        FindAction(FindActionType type) {
            this.type = type;
        }

        public void changedUpdate(DocumentEvent e) {
            setFindList(type);
        }

        public void insertUpdate(DocumentEvent e) {
            setFindList(type);
        }

        public void removeUpdate(DocumentEvent e) {
            setFindList(type);
        }
    }

    class JumpAction implements ListSelectionListener {
        public void valueChanged(ListSelectionEvent e) {
            Object cell = resourceList.getSelectedValue();
            if (rdfCheckBox.isSelected()) {
                gmanager.selectRDFCell(cell);
            }
            if (classCheckBox.isSelected()) {
                gmanager.selectClassCell(cell);
            }
            if (propertyCheckBox.isSelected()) {
                gmanager.selectPropertyCell(cell);
            }
        }
    }

    class ResourceListCellRenderer extends DefaultListCellRenderer {
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value instanceof RDFResourceCell) {
                setIcon(RDFGraphMarqueeHandler.RDF_RESOURCE_ELLIPSE_ICON);
            } else if (value instanceof OntClassCell) {
                setIcon(ClassGraphMarqueeHandler.CLASS_RECTANGLE_ICON);
            } else if (value instanceof OntPropertyCell) {
                setIcon(PropertyGraphMarqueeHandler.PROPERTY_ELLIPSE_ICON);
            }
            return this;
        }
    }

}