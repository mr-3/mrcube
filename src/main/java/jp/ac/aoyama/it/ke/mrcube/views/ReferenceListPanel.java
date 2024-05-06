/*
 * Project Name: MR^3 (Meta-Model Management based on RDFs Revision Reflection)
 * Project Website: https://mr-3.github.io/
 *
 * Copyright (C) 2003-2024 Takeshi Morita. All rights reserved.
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

package jp.ac.aoyama.it.ke.mrcube.views;

import org.jgraph.graph.GraphCell;
import org.jgraph.graph.GraphConstants;
import jp.ac.aoyama.it.ke.mrcube.jgraph.GraphManager;
import jp.ac.aoyama.it.ke.mrcube.jgraph.RDFGraph;
import jp.ac.aoyama.it.ke.mrcube.models.MR3Resource;
import jp.ac.aoyama.it.ke.mrcube.models.PropertyModel;
import jp.ac.aoyama.it.ke.mrcube.models.InstanceModel;
import jp.ac.aoyama.it.ke.mrcube.utils.Translator;
import jp.ac.aoyama.it.ke.mrcube.utils.Utilities;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

/**
 * @author Takeshi Morita
 */
class ReferenceListPanel extends JPanel {

    private JTable rdfRefTable;
    private JTable propRefTable;
    private final JTabbedPane tab;
    private final GraphManager gmanager;
    private static TableModel nullTableModel;

    private JButton selectAllButton;
    private JButton clearAllButton;
    private JButton reverseButton;
    private JButton editButton;

    private final Map<Object, TableModel> rdfTableModelMap;
    private final Map<Object, TableModel> propTableModelMap;

    private static final int LIST_WIDTH = 380;
    private static final int LIST_HEIGHT = 100;

    ReferenceListPanel(GraphManager manager) {
        gmanager = manager;
        rdfTableModelMap = new HashMap<>();
        propTableModelMap = new HashMap<>();

        initTable();
        setLayout(new BorderLayout());

        tab = new JTabbedPane();
        setTableLayout(rdfRefTable, Translator.getString("InstanceEditor"));
        setTableLayout(propRefTable, Translator.getString("PropertyEditor.Title"));
        add(tab, BorderLayout.CENTER);
        add(getButtonPanel(), BorderLayout.SOUTH);
        setBorder(BorderFactory.createTitledBorder(Translator.getString("RemoveDialog.ReferenceResourceList.Title")));
    }

    private void initTable() {
        nullTableModel = getTableModel();
        rdfRefTable = new JTable(getTableModel());
        rdfRefTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        propRefTable = new JTable(getTableModel());
        propRefTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        setTableColumnModel(rdfRefTable);
        setTableColumnModel(propRefTable);
    }

    private void setTableColumnModel(JTable table) {
        TableColumnModel tcModel = table.getColumnModel();
        tcModel.getColumn(0).setPreferredWidth(50);
        tcModel.getColumn(1).setPreferredWidth(300);
    }

    private JComponent getButtonPanel() {
        ButtonAction buttonAction = new ButtonAction();
        selectAllButton = new JButton(Translator.getString("RemoveDialog.ReferenceResourceList.SelectAll"));
        selectAllButton.addActionListener(buttonAction);
        clearAllButton = new JButton(Translator.getString("RemoveDialog.ReferenceResourceList.ClearAll"));
        clearAllButton.addActionListener(buttonAction);
        reverseButton = new JButton(Translator.getString("RemoveDialog.ReferenceResourceList.InverseSelection"));
        reverseButton.addActionListener(buttonAction);
        editButton = new JButton(Translator.getString("RemoveDialog.ReferenceResourceList.Edit"));
        editButton.addActionListener(buttonAction);
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(1, 4, 5, 5));
        buttonPanel.add(selectAllButton);
        buttonPanel.add(clearAllButton);
        buttonPanel.add(reverseButton);
        buttonPanel.add(editButton);
        return Utilities.createEastPanel(buttonPanel);
    }

    private void setCheck(TableModel tableModel, boolean t) {
        if (tableModel.getRowCount() <= 0) {
            return;
        }
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            tableModel.setValueAt(t, i, 0);
        }
    }

    private void checkAll() {
        setCheck(rdfRefTable.getModel(), true);
        setCheck(propRefTable.getModel(), true);
    }

    private void clearCheckAll() {
        setCheck(rdfRefTable.getModel(), false);
        setCheck(propRefTable.getModel(), false);
    }

    private void checkReverse(TableModel tableModel) {
        if (tableModel.getRowCount() <= 0) {
            return;
        }
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            Boolean t = (Boolean) tableModel.getValueAt(i, 0);
            tableModel.setValueAt(!t, i, 0);
        }
    }

    private void editSelectedRDFResource() {
        int row = rdfRefTable.getSelectedRow();
        if (row == -1) { // 選択されていない場合
            return;
        }
        Object cell = rdfRefTable.getValueAt(row, 1);
        gmanager.selectRDFCell(cell);
        gmanager.setVisibleAttrDialog(true);
    }

    private void editSelectedRDFSProperty() {
        int row = propRefTable.getSelectedRow();
        if (row == -1) { // 選択されていない場合
            return;
        }
        Object cell = propRefTable.getValueAt(row, 1);
        gmanager.selectPropertyCell(cell);
        gmanager.setVisibleAttrDialog(true);
    }

    class ButtonAction extends AbstractAction {
        public void actionPerformed(ActionEvent e) {

            if (e.getSource() == selectAllButton) {
                checkAll();
            } else if (e.getSource() == clearAllButton) {
                clearCheckAll();
            } else if (e.getSource() == reverseButton) {
                checkReverse(rdfRefTable.getModel());
                checkReverse(propRefTable.getModel());
            } else if (e.getSource() == editButton) {
                if (tab.getSelectedIndex() == 0) {
                    editSelectedRDFResource();
                } else if (tab.getSelectedIndex() == 1) {
                    editSelectedRDFSProperty();
                }
            }
        }
    }

    private void setTableLayout(JTable table, String title) {
        JScrollPane tableScroll = new JScrollPane(table);
        tableScroll.setPreferredSize(new Dimension(LIST_WIDTH, LIST_HEIGHT));
        tableScroll.setMinimumSize(new Dimension(LIST_WIDTH, LIST_HEIGHT));
        tab.addTab(title, tableScroll);
    }

    private ReferenceTableModel getTableModel() {
        Object[] columnNames = new Object[]{Translator.getString("RemoveDialog.ReferenceResourceList.DeleteCheck"),
                Translator.getString("RemoveDialog.ReferenceResourceList.ResourceList")};
        return new ReferenceTableModel(columnNames, 0);
    }

    private void replaceRDFRefTableModel(Object cell) {
        TableModel tableModel = rdfTableModelMap.get(cell);
        if (tableModel != null) {
            rdfRefTable.setModel(tableModel);
            setTableColumnModel(rdfRefTable);
        }
    }

    private void replacePropRefTableModel(Object cell) {
        TableModel tableModel = propTableModelMap.get(cell);
        if (tableModel != null) {
            propRefTable.setModel(tableModel);
            setTableColumnModel(propRefTable);
        }
    }

    public void replaceTableModel(Object cell) {
        replaceRDFRefTableModel(cell);
        replacePropRefTableModel(cell);
    }

    private void removeRefRDFAction(Object cell) {
        TableModel tableModel = rdfTableModelMap.get(cell);
        if (tableModel == null) {
            return;
        }
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            if (isAvailable(tableModel, i, 0)) {
                GraphCell rdfCell = (GraphCell) tableModel.getValueAt(i, 1);
                if (RDFGraph.isRDFResourceCell(rdfCell)) {
                    InstanceModel info = (InstanceModel) GraphConstants.getValue(rdfCell.getAttributes());
                    info.setTypeCell(null, gmanager.getInstanceGraph()); // Typeをnullに変更
                } else if (RDFGraph.isRDFPropertyCell(rdfCell)) {
                    GraphConstants.setValue(rdfCell.getAttributes(), new PropertyModel(MR3Resource.Nil.getURI()));
                    gmanager.getInstanceGraph().getGraphLayoutCache().editCell(rdfCell, rdfCell.getAttributes());
                }
            }
        }
    }

    private void removeRefPropAction(Object cell) {
        TableModel tableModel = propTableModelMap.get(cell);
        if (tableModel == null) {
            return;
        }

        for (int i = 0; i < tableModel.getRowCount(); i++) {
            if (isAvailable(tableModel, i, 0)) {
                GraphCell propertyCell = (GraphCell) tableModel.getValueAt(i, 1);
                PropertyModel info = (PropertyModel) GraphConstants.getValue(propertyCell.getAttributes());
                info.removeDomain(cell);
                info.removeRange(cell);
            }
        }
    }

    // チェックがついているものにデフォルトの処理
    public void removeAction(Object cell) {
        removeRefRDFAction(cell);
        removeRefPropAction(cell);
    }

    private TableModel getTableModel(Object cell, Map map) {
        DefaultTableModel tableModel = getTableModel();
        Set set = (Set) map.get(cell);
        if (set == null) {
            return null;
        }
        for (Object o : set) {
            Object[] list = new Object[]{Boolean.TRUE, o};
            tableModel.addRow(list);
        }
        return tableModel;
    }

    public void setTableModelMap(Set cells, Map classRDFMap, Map classPropMap) {
        rdfRefTable.setModel(nullTableModel);
        propRefTable.setModel(nullTableModel);
        for (Object cell : cells) {
            TableModel tModel = getTableModel(cell, classRDFMap);
            rdfTableModelMap.put(cell, tModel);

            tModel = getTableModel(cell, classPropMap);
            propTableModelMap.put(cell, tModel);
        }
    }

    private boolean isAvailable(TableModel tableModel, int row, int column) {
        return (Boolean) tableModel.getValueAt(row, column);
    }

    class ReferenceTableModel extends DefaultTableModel {

        ReferenceTableModel(Object[] columnNames, int rowCount) {
            super(columnNames, rowCount);
        }

        public boolean isCellEditable(int row, int col) {
            return col == 0 ? true : false;
        }

        public Class getColumnClass(int column) {
            Vector v = dataVector.elementAt(0);
            return v.elementAt(column).getClass();
        }
    }
}
