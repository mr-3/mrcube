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

import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.vocabulary.RDF;
import org.jgraph.graph.GraphCell;
import org.jgraph.graph.GraphConstants;
import org.mrcube.actions.EditRDFPropertyAction;
import org.mrcube.jgraph.GraphManager;
import org.mrcube.models.*;
import org.mrcube.models.MR3Constants.HistoryType;
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
import java.awt.event.ActionListener;
import java.util.List;
import java.util.*;

/**
 * @author Takeshi Morita
 */
public class RDFPropertyPanel extends JPanel implements ActionListener, ListSelectionListener {

    private final TypePropertyIDAction typePropertyIDAction;
    private final JCheckBox isContainerBox;
    private final JSpinner numSpinner;

    private final JCheckBox propOnlyCheck;
    private final JComboBox uriPrefixBox;
    private final JTextField idField;
    private final JTextField findIDField;
    private final JLabel nsLabel;
    private final JButton applyButton;
    private final JButton cancelButton;
    private final JButton jumpPropertyButton;
    private GraphCell edge;

    private JList localNameList;
    private DefaultListModel<String> localNameListModel;
    private Map<String, Set<String>> propMap;

    private List<GraphCell> propList;
    private final GraphManager gmanager;

    private final EditRDFPropertyAction editRDFPropertyAction;

    private static final int FIELD_WIDTH = 80;
    private static final int FIELD_HEIGHT = 30;

    public RDFPropertyPanel(GraphManager manager) {
        gmanager = manager;
        editRDFPropertyAction = new EditRDFPropertyAction(gmanager);

        isContainerBox = new JCheckBox(Translator.getString("IsContainer"));
        isContainerBox.addActionListener(new ContainerBoxAction());
        isContainerBox.setSelected(false);
        numSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 99, 1));
        numSpinner.setEnabled(false);

        propOnlyCheck = new JCheckBox(Translator.getString("ShowPropertyPrefixOnly"));
        propOnlyCheck.addActionListener(this);
        propOnlyCheck.setSelected(true);

        JPanel containerPanel = new JPanel();
        containerPanel.setLayout(new BoxLayout(containerPanel, BoxLayout.X_AXIS));
        containerPanel.add(propOnlyCheck);
        containerPanel.add(isContainerBox);
        containerPanel.add(numSpinner);

        uriPrefixBox = new JComboBox();
        uriPrefixBox.addActionListener(new ChangePrefixAction());
        JComponent uriPrefixBoxP = Utilities.createTitledPanel(uriPrefixBox, MR3Constants.PREFIX);
        idField = new JTextField();
        idField.addActionListener(this);
        JComponent idFieldP = Utilities.createTitledPanel(idField, "ID");
        jumpPropertyButton = new JButton(Translator.getString("Property"));
        jumpPropertyButton.addActionListener(this);
        JComponent jumpPropertyButtonP = Utilities.createTitledPanel(jumpPropertyButton, Translator.getString("Jump"));

        JPanel uriPanel = new JPanel();
        uriPanel.setLayout(new GridLayout(1, 3, 5, 5));
        uriPanel.add(uriPrefixBoxP);
        uriPanel.add(idFieldP);
        uriPanel.add(jumpPropertyButtonP);

        nsLabel = new JLabel();
        JComponent nsLabelP = Utilities.createTitledPanel(nsLabel, MR3Constants.NAME_SPACE);
        typePropertyIDAction = new TypePropertyIDAction();
        findIDField = new JTextField();
        findIDField.getDocument().addDocumentListener(typePropertyIDAction);
        JComponent findIDFieldP = Utilities.createTitledPanel(findIDField, "Find ID", FIELD_WIDTH, FIELD_HEIGHT);
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.add(nsLabelP, BorderLayout.CENTER);
        panel.add(findIDFieldP, BorderLayout.EAST);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.add(containerPanel);
        mainPanel.add(uriPanel);
        mainPanel.add(panel);
        mainPanel.add(getSelectPropertyPanel());

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

        setLayout(new BorderLayout());
        JPanel titlePanel = new JPanel();
        titlePanel.setBackground(new Color(49, 105, 198));
        ImageIcon icon = Utilities.getImageIcon(Translator.getString("RDFEditor.Icon"));
        JLabel titleLabel = new JLabel(
                Translator.getString("AttributeDialog.RDFPropertyAttribute.Text"), icon,
                SwingConstants.LEFT);
        titleLabel.setForeground(Color.white);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 13));
        titlePanel.setLayout(new BorderLayout());
        titlePanel.add(titleLabel, BorderLayout.WEST);

        add(titlePanel, BorderLayout.NORTH);
        add(mainPanel, BorderLayout.CENTER);
        add(Utilities.createEastPanel(buttonPanel), BorderLayout.SOUTH);
    }

    class TypePropertyIDAction implements DocumentListener {
        public void selectLocalName() {
            selectNameSpaceList();
            List<String> list = new ArrayList<>();
            String idStr = findIDField.getText();
            for (int i = 0; i < localNameListModel.getSize(); i++) {
                String elementStr = localNameListModel.getElementAt(i);
                if (elementStr.length() < idStr.length()) {
                    continue;
                }
                if (elementStr.substring(0, idStr.length()).matches(idStr)) {
                    list.add(localNameListModel.getElementAt(i));
                }
            }
            localNameList.setListData(list.toArray());
            if (0 < list.size()) {
                localNameList.setSelectedIndex(0);
            }
        }

        public void changedUpdate(DocumentEvent e) {
            selectLocalName();
        }

        public void insertUpdate(DocumentEvent e) {
            selectLocalName();
        }

        public void removeUpdate(DocumentEvent e) {
            selectLocalName();
        }
    }

    private void setContainer(boolean t) {
        numSpinner.setEnabled(t);
        propOnlyCheck.setEnabled(!t);
        uriPrefixBox.setEnabled(!t);
        idField.setEnabled(!t);
        nsLabel.setEnabled(!t);
        findIDField.setEditable(!t);
        jumpPropertyButton.setEnabled(!t);
        localNameList.setEnabled(!t);
    }

    private boolean isContainer() {
        return isContainerBox.isSelected();
    }

    class ContainerBoxAction extends AbstractAction {
        public void actionPerformed(ActionEvent e) {
            setContainer(isContainer());
        }
    }

    class ChangePrefixAction extends AbstractAction {
        public void actionPerformed(ActionEvent e) {
            findIDField.setText("");
            typePropertyIDAction.selectLocalName();
        }
    }

    private JComponent getSelectPropertyPanel() {
        localNameListModel = new DefaultListModel<>();
        localNameList = new JList(localNameListModel);
        localNameList.addListSelectionListener(this);
        JScrollPane localNameListScroll = new JScrollPane(localNameList);
        localNameListScroll.setBorder(BorderFactory.createTitledBorder(Translator.getString("Property") + " ID"));
        return localNameListScroll;
    }

    private static final String NULL_LOCAL_NAME = "(Null)";

    private void setURIPrefixBoxModel() {
        if (propList != null && propOnlyCheck.isSelected()) {
            uriPrefixBox.setModel(new DefaultComboBoxModel(PrefixNSUtil.getPropPrefixes(propList).toArray()));
        } else {
            uriPrefixBox.setModel(new DefaultComboBoxModel(PrefixNSUtil.getPrefixes().toArray()));
        }
    }

    private void setPrefix() {
        setURIPrefixBoxModel();
        for (NamespaceModel prefNSInfo : GraphUtilities.getNamespaceModelSet()) {
            if (prefNSInfo.getNameSpace().equals(nsLabel.getText())) {
                uriPrefixBox.setSelectedItem(prefNSInfo.getPrefix());
                break;
            }
        }
    }

    private void selectNameSpaceList() {
        PrefixNSUtil.replacePrefix((String) uriPrefixBox.getSelectedItem(), nsLabel);
        if (propMap == null) {
            return;
        }

        if (!localNameList.isSelectionEmpty()) {
            localNameList.clearSelection();
        }
        String nameSpace = nsLabel.getText();
        Set<String> localNames = propMap.get(nameSpace);
        if (localNames == null) {
            localNameListModel.clear();
            return;
        }
        localNameListModel.clear();
        Set<String> modifyLocalNames = new TreeSet<>();
        for (String localName : localNames) {
            if (localName.length() == 0) { // localNameがない場合，Nullを表示
                modifyLocalNames.add(NULL_LOCAL_NAME);
            } else {
                modifyLocalNames.add(localName);
            }
        }
        localNameListModel.addAll(modifyLocalNames);
        localNameList.setSelectedValue(idField.getText(), true);
    }

    private void setNSLabel(String str) {
        nsLabel.setText(str);
        nsLabel.setToolTipText(str);
    }

    private void selectLocalNameList() {
        if (localNameList.getSelectedValue() != null) {
            String ln = localNameList.getSelectedValue().toString();
            if (ln.equals(NULL_LOCAL_NAME)) {
                ln = "";
            }
            idField.setText(ln);
        }
    }

    public void valueChanged(ListSelectionEvent e) {
        try {
            if (e.getSource() == localNameList) {
                selectLocalNameList();
            }
        } catch (NullPointerException np) {
            np.printStackTrace();
        }
    }

    public void setValue(GraphCell c, PropertyModel info) {
        edge = c;
        if (info == null) {
            setNSLabel(MR3Resource.Nil.getNameSpace());
            idField.setText(MR3Resource.Nil.getLocalName());
            editRDFPropertyAction.setURIString(getURI());
            editRDFPropertyAction.setEdge(edge);
            editRDFPropertyAction.editRDFProperty();
        } else {
            isContainerBox.setSelected(info.isContainer());
            setContainer(info.isContainer());
            if (info.isContainer()) {
                numSpinner.setValue(info.getNum());
            } else {
                // mr3:nilの場合には，名前空間はBaseURIとする
                if (info.getURIStr().equals(MR3Resource.Nil.getURI())) {
                    if (0 < propList.size()) {
                        GraphCell cell = propList.get(0);
                        RDFSModel propInfo = (RDFSModel) GraphConstants
                                .getValue(cell.getAttributes());
                        setNSLabel(propInfo.getURI().getNameSpace());
                    } else {
                        setNSLabel(gmanager.getBaseURI());
                    }
                    idField.setText("");
                } else {
                    setNSLabel(info.getNameSpace());
                    idField.setText(info.getLocalName());
                }
            }
        }
        setPrefix();
        SwingUtilities.invokeLater(() -> idField.requestFocus());
    }

    public void setPropertyList(List<GraphCell> plist) {
        propList = plist;
        propMap = new HashMap<>();
        Set<String> propNameSpaceSet = new HashSet<>();

        for (GraphCell cell : propList) {
            RDFSModel info = (RDFSModel) GraphConstants.getValue(cell.getAttributes());
            Resource uri = info.getURI();
            propNameSpaceSet.add(uri.getNameSpace());
            Set<String> localNames = propMap.computeIfAbsent(uri.getNameSpace(), k -> new HashSet<>());
            localNames.add(uri.getLocalName());
        }
        selectNameSpaceList();
    }

    private String getURI() {
        return nsLabel.getText() + idField.getText();
    }

    private void jumpRDFSProperty() {
        Resource uri = ResourceFactory.createResource(nsLabel.getText() + idField.getText());
        if (gmanager.isEmptyURI(uri.getURI())) {
            return;
        }
        RDFSModelMap rdfsModelMap = gmanager.getCurrentRDFSInfoMap();
        if (rdfsModelMap.isPropertyCell(uri)) {
            Object propertyCell = rdfsModelMap.getPropertyCell(uri);
            gmanager.selectPropertyCell(propertyCell);
        } else {
            Utilities.showErrorMessageDialog(Translator.getString("Warning.Message3"));
        }
    }

    private void setContainerMemberProperty() {
        Integer num = (Integer) numSpinner.getValue();
        Resource resource = ResourceFactory.createResource(RDF.getURI() + "_" + num);
        GraphCell propertyCell = gmanager.getPropertyCell(resource, false);
        RDFSModel rdfsModel = (RDFSModel) GraphConstants.getValue(propertyCell.getAttributes());
        GraphConstants.setValue(edge.getAttributes(), rdfsModel);
        gmanager.getCurrentRDFGraph().getGraphLayoutCache().editCell(edge, edge.getAttributes());
    }

    public void actionPerformed(ActionEvent e) {
        if (edge == null) {
            return;
        }
        if (e.getSource() == applyButton || e.getSource() == idField) {
            RDFSModel beforeRDFSModel = (RDFSModel) GraphConstants.getValue(edge.getAttributes());
            String beforeProperty = beforeRDFSModel.getURIStr();
            if (isContainer()) {
                setContainerMemberProperty();
            } else {
                editRDFPropertyAction.setURIString(getURI());
                editRDFPropertyAction.setEdge(edge);
                editRDFPropertyAction.editRDFProperty();
                findIDField.setText("");
                gmanager.selectRDFCell(edge);
            }
            RDFSModel afterRDFSModel = (RDFSModel) GraphConstants.getValue(edge.getAttributes());
            String afterProperty = afterRDFSModel.getURIStr();
            HistoryManager.saveHistory(HistoryType.EDIT_PROPERTY_WITH_DIAGLOG, beforeProperty, afterProperty);
        } else if (e.getSource() == propOnlyCheck) {
            setURIPrefixBoxModel();
            selectNameSpaceList();
        } else if (e.getSource() == jumpPropertyButton) {
            jumpRDFSProperty();
        } else if (e.getSource() == cancelButton) {
            gmanager.setVisibleAttrDialog(false);
        }
    }
}
