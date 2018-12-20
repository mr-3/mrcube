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

import org.jgraph.graph.GraphCell;
import org.jgraph.graph.GraphConstants;
import org.mrcube.jgraph.GraphManager;
import org.mrcube.jgraph.RDFGraph;
import org.mrcube.models.MR3Resource;
import org.mrcube.models.PropertyInfo;
import org.mrcube.models.RDFResourceInfo;
import org.mrcube.utils.Translator;
import org.mrcube.utils.Utilities;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.*;

/**
 * @author Takeshi Morita
 * 
 */
public class ReferenceListPanel extends JPanel {

    private JTable rdfRefTable;
    private JTable propRefTable;
    private JTabbedPane tab;
    private GraphManager gmanager;
    private static TableModel nullTableModel;

    private JButton selectAllButton;
    private JButton clearAllButton;
    private JButton reverseButton;
    private JButton jumpButton;

    private Map rdfTableModelMap;
    private Map propTableModelMap;

    private static final int LIST_WIDTH = 380;
    private static final int LIST_HEIGHT = 100;

    ReferenceListPanel(GraphManager manager) {
        gmanager = manager;
        rdfTableModelMap = new HashMap();
        propTableModelMap = new HashMap();

        initTable();
        setLayout(new BorderLayout());

        tab = new JTabbedPane();
        setTableLayout(rdfRefTable, "RDF");
        setTableLayout(propRefTable, Translator.getString("Property"));
        add(tab, BorderLayout.CENTER);
        add(getButtonPanel(), BorderLayout.SOUTH);
        setBorder(BorderFactory.createTitledBorder(Translator.getString("ReferenceList.Title")));
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
        selectAllButton = new JButton(Translator.getString("ReferenceList.SelectAll"));
        selectAllButton.addActionListener(buttonAction);
        clearAllButton = new JButton(Translator.getString("ReferenceList.ClearAll"));
        clearAllButton.addActionListener(buttonAction);
        reverseButton = new JButton(Translator.getString("ReferenceList.Reverse"));
        reverseButton.addActionListener(buttonAction);
        jumpButton = new JButton(Translator.getString("ReferenceList.Jump"));
        jumpButton.addActionListener(buttonAction);
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(1, 4, 5, 5));
        buttonPanel.add(selectAllButton);
        buttonPanel.add(clearAllButton);
        buttonPanel.add(reverseButton);
        buttonPanel.add(jumpButton);
        return Utilities.createEastPanel(buttonPanel);
    }

    private void setCheck(TableModel tableModel, boolean t) {
        if (tableModel.getRowCount() <= 0) { return; }
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            tableModel.setValueAt(new Boolean(t), i, 0);
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
        if (tableModel.getRowCount() <= 0) { return; }
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            Boolean t = (Boolean) tableModel.getValueAt(i, 0);
            tableModel.setValueAt(new Boolean(!t.booleanValue()), i, 0);
        }
    }

    private void rdfJump() {
        int row = rdfRefTable.getSelectedRow();
        if (row == -1) { // 選択されていない場合
            return;
        }
        Object cell = rdfRefTable.getValueAt(row, 1);
        gmanager.selectRDFCell(cell);
        gmanager.setVisibleAttrDialog(true);
    }

    private void propJump() {
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
            } else if (e.getSource() == jumpButton) {
                // System.out.println(tab.getSelectedIndex());
                if (tab.getSelectedIndex() == 0) {
                    rdfJump();
                } else if (tab.getSelectedIndex() == 1) {
                    propJump();
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
        Object[] columnNames = new Object[] { Translator.getString("ReferenceList.DeleteCheck"),
                Translator.getString("ReferenceList.List")};
        ReferenceTableModel tModel = new ReferenceTableModel(columnNames, 0);
        return tModel;
    }

    private void replaceRDFRefTableModel(Object cell) {
        TableModel tableModel = (TableModel) rdfTableModelMap.get(cell);
        if (tableModel != null) {
            rdfRefTable.setModel(tableModel);
            setTableColumnModel(rdfRefTable);
        }
    }

    private void replacePropRefTableModel(Object cell) {
        TableModel tableModel = (TableModel) propTableModelMap.get(cell);
        if (tableModel != null) {
            propRefTable.setModel(tableModel);
            setTableColumnModel(propRefTable);
        }
    }

    public void replaceTableModel(Object cell) {
        replaceRDFRefTableModel(cell);
        replacePropRefTableModel(cell);
    }

    public void removeRefRDFAction(Object cell) {
        TableModel tableModel = (TableModel) rdfTableModelMap.get(cell);
        if (tableModel == null) { return; }
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            if (isAvailable(tableModel, i, 0)) {
                GraphCell rdfCell = (GraphCell) tableModel.getValueAt(i, 1);
                if (RDFGraph.isRDFResourceCell(rdfCell)) {
                    RDFResourceInfo info = (RDFResourceInfo) GraphConstants.getValue(rdfCell.getAttributes());
                    info.setTypeCell(null, gmanager.getCurrentRDFGraph()); // Typeをnullに変更
                } else if (RDFGraph.isRDFPropertyCell(rdfCell)) {
                    GraphConstants.setValue(rdfCell.getAttributes(), new PropertyInfo(MR3Resource.Nil.getURI()));
                    gmanager.getCurrentRDFGraph().getGraphLayoutCache().editCell(rdfCell, rdfCell.getAttributes());
                }
            }
        }
    }
    public void removeRefPropAction(Object cell) {
        TableModel tableModel = (TableModel) propTableModelMap.get(cell);
        if (tableModel == null) { return; }

        for (int i = 0; i < tableModel.getRowCount(); i++) {
            if (isAvailable(tableModel, i, 0)) {
                GraphCell propertyCell = (GraphCell) tableModel.getValueAt(i, 1);
                PropertyInfo info = (PropertyInfo) GraphConstants.getValue(propertyCell.getAttributes());
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

    private Object getTableModel(Object cell, Map map) {
        DefaultTableModel tableModel = getTableModel();
        Set set = (Set) map.get(cell);
        if (set == null) { return null; }
        for (Iterator i = set.iterator(); i.hasNext();) {
            Object[] list = new Object[] { new Boolean(true), i.next()};
            tableModel.addRow(list);
        }
        return tableModel;
    }

    public void setTableModelMap(Set cells, Map classRDFMap, Map classPropMap) {
        rdfRefTable.setModel(nullTableModel);
        propRefTable.setModel(nullTableModel);
        for (Iterator i = cells.iterator(); i.hasNext();) {
            Object cell = i.next();

            Object tModel = getTableModel(cell, classRDFMap);
            rdfTableModelMap.put(cell, tModel);

            tModel = getTableModel(cell, classPropMap);
            propTableModelMap.put(cell, tModel);
        }
    }

    private boolean isAvailable(TableModel tableModel, int row, int column) {
        Boolean isAvailable = (Boolean) tableModel.getValueAt(row, column);
        return isAvailable.booleanValue();
    }

    class ReferenceTableModel extends DefaultTableModel {

        public ReferenceTableModel(Object[] columnNames, int rowCount) {
            super(columnNames, rowCount);
        }

        public boolean isCellEditable(int row, int col) {
            return col == 0 ? true : false;
        }

        public Class getColumnClass(int column) {
            Vector v = (Vector) dataVector.elementAt(0);
            return v.elementAt(column).getClass();
        }
    }
}
