/*
 * Project Name: MR^3 (Meta-Model Management based on RDFs Revision Reflection)
 * Project Website: http://mrcube.org/
 *
 * Copyright (C) 2003-2022 Takeshi Morita. All rights reserved.
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

package org.mrcube.views.rdf_editor;

import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
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
import org.mrcube.views.HistoryManager;

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

    private final JComboBox uriPrefixBox;
    private final JTextField idField;
    private final JTextField findIDField;
    private final JLabel nsLabel;
    private final JButton applyButton;
    private final JButton cancelButton;
    private final JButton editRDFSPropertyButton;
    private GraphCell edge;

    private JList<String> localNameList;
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

        uriPrefixBox = new JComboBox();
        uriPrefixBox.addActionListener(new ChangePrefixAction());
        var uriPrefixBoxP = Utilities.createTitledPanel(uriPrefixBox, MR3Constants.PREFIX);
        idField = new JTextField();
        idField.addActionListener(this);
        var idFieldP = Utilities.createTitledPanel(idField, "ID");
        editRDFSPropertyButton = new JButton("RDFS" + Translator.getString("Property") + " " + Translator.getString("Edit"));
        editRDFSPropertyButton.addActionListener(this);
        var editRDFSPropertyButtonP = Utilities.createTitledPanel(editRDFSPropertyButton, " ");

        JPanel uriPanel = new JPanel();
        uriPanel.setLayout(new GridLayout(1, 3, 5, 5));
        uriPanel.add(uriPrefixBoxP);
        uriPanel.add(idFieldP);
        uriPanel.add(editRDFSPropertyButtonP);

        nsLabel = new JLabel();
        JComponent nsLabelP = Utilities.createTitledPanel(nsLabel, MR3Constants.NAME_SPACE);
        typePropertyIDAction = new TypePropertyIDAction();
        findIDField = new JTextField();
        findIDField.getDocument().addDocumentListener(typePropertyIDAction);
        JComponent findIDFieldP = Utilities.createTitledPanel(findIDField, "ID " + Translator.getString("Filter"), FIELD_WIDTH, FIELD_HEIGHT);
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.add(nsLabelP, BorderLayout.CENTER);
        panel.add(findIDFieldP, BorderLayout.EAST);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
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
        titlePanel.setBackground(MR3Constants.TITLE_BACKGROUND_COLOR);
        ImageIcon icon = Utilities.getImageIcon(Translator.getString("RDFEditor.Icon"));
        JLabel titleLabel = new JLabel(
                Translator.getString("AttributeDialog.RDFPropertyAttribute.Text"), icon,
                SwingConstants.LEFT);
        titleLabel.setForeground(Color.white);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, MR3Constants.TITLE_FONT_SIZE));
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
            localNameList.setListData(list.stream().toArray(String[]::new));
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
        uriPrefixBox.setEnabled(!t);
        idField.setEnabled(!t);
        nsLabel.setEnabled(!t);
        findIDField.setEditable(!t);
        editRDFSPropertyButton.setEnabled(!t);
        localNameList.setEnabled(!t);
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
        uriPrefixBox.setModel(new DefaultComboBoxModel(PrefixNSUtil.getPrefixes().toArray()));
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
            String ln = localNameList.getSelectedValue();
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
        setPrefix();
        if (info == null) {
            setNSLabel(MR3Resource.Nil.getNameSpace());
            idField.setText(MR3Resource.Nil.getLocalName());
            editRDFPropertyAction.setURIString(getURI());
            editRDFPropertyAction.setEdge(edge);
            editRDFPropertyAction.editRDFProperty();
        } else {
            setContainer(info.isContainer());
            // mr3:nilの場合には，名前空間はBaseURIとする
            if (info.getURIStr().equals(MR3Resource.Nil.getURI())) {
                if (propList != null && !propList.isEmpty()) {
                    GraphCell cell = propList.get(0);
                    RDFSModel propInfo = (RDFSModel) GraphConstants.getValue(cell.getAttributes());
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

    private void editRDFSProperty() {
        Resource uri = ResourceFactory.createResource(nsLabel.getText() + idField.getText());
        if (!PrefixNSUtil.isValidURI(uri.getURI())) {
            return;
        }
        RDFSModelMap rdfsModelMap = gmanager.getRDFSInfoMap();
        if (rdfsModelMap.isPropertyCell(uri)) {
            Object propertyCell = rdfsModelMap.getPropertyCell(uri);
            gmanager.selectPropertyCell(propertyCell);
        } else {
            Utilities.showErrorMessageDialog(Translator.getString("Warning.Message12"));
        }
    }

    public void actionPerformed(ActionEvent e) {
        if (edge == null) {
            return;
        }
        if (e.getSource() == applyButton || e.getSource() == idField) {
            RDFSModel beforeRDFSModel = (RDFSModel) GraphConstants.getValue(edge.getAttributes());
            String beforeProperty = beforeRDFSModel.getURIStr();
            editRDFPropertyAction.setURIString(getURI());
            editRDFPropertyAction.setEdge(edge);
            editRDFPropertyAction.editRDFProperty();
            findIDField.setText("");
            gmanager.selectRDFCell(edge);
            RDFSModel afterRDFSModel = (RDFSModel) GraphConstants.getValue(edge.getAttributes());
            String afterProperty = afterRDFSModel.getURIStr();
            HistoryManager.saveHistory(HistoryType.EDIT_PROPERTY_WITH_DIAGLOG, beforeProperty, afterProperty);
        } else if (e.getSource() == editRDFSPropertyButton) {
            editRDFSProperty();
        } else if (e.getSource() == cancelButton) {
            gmanager.setVisibleAttrDialog(false);
        }
    }
}
