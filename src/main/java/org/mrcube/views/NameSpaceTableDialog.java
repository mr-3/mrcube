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

package org.mrcube.views;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.mrcube.jgraph.GraphManager;
import org.mrcube.models.MR3Constants;
import org.mrcube.models.MR3Resource;
import org.mrcube.models.NamespaceModel;
import org.mrcube.utils.GraphUtilities;
import org.mrcube.utils.PrefixNSUtil;
import org.mrcube.utils.Translator;
import org.mrcube.utils.Utilities;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serializable;
import java.util.List;
import java.util.*;

/**
 * 名前空間と接頭辞の対応付けをテーブルで行う チェックにより，Class, Property, Resourceの名前空間を接頭辞で置き換える
 * 接頭辞の名前変更はテーブルから行うことができる
 *
 * @author Takeshi Morita
 */
public class NameSpaceTableDialog extends JDialog implements ActionListener, TableModelListener, Serializable {

    private Map<String, String> prefixNSMap;
    private JTable nsTable;
    private NSTableModel nsTableModel;

    private static final long serialVersionUID = 5974381131839067739L;

    transient private JButton addNSButton;
    transient private JButton editNSButton;
    transient private JButton removeNSButton;
    transient private JButton cancelButton;
    transient private JTextField prefixField;
    transient private JTextField nsField;
    final transient private JPanel nsPanel;

    final transient private GraphManager gmanager;
    transient private Map<String, String> knownNSPrefixMap;

    private static final String WARNING = Translator.getString("Warning");

    public NameSpaceTableDialog(GraphManager gm) {
        super(gm.getRootFrame(), Translator.getString("NameSpaceTable.Title"), false);
        setIconImage(Utilities.getImageIcon(Translator.getString("NameSpaceTable.Icon")).getImage());
        gmanager = gm;
        prefixNSMap = new HashMap<>();
        initTable();
        nsPanel = new JPanel();
        nsPanel.setLayout(new BorderLayout());
        setTableLayout();
        setInputLayout();
        getContentPane().add(nsPanel);

        initKnownPrefixNSMap();
        pack();
        setLocationRelativeTo(gmanager.getRootFrame());
        setVisible(false);
    }

    private void initKnownPrefixNSMap() {
        knownNSPrefixMap = new HashMap<>();
        knownNSPrefixMap.put("http://purl.org/dc/elements/1.1/", "dc");
        knownNSPrefixMap.put("http://purl.org/rss/1.0/", "rss");
        knownNSPrefixMap.put("http://xmlns.com/foaf/0.1/", "foaf");
        knownNSPrefixMap.put("http://www.w3.org/2002/07/owl#", "owl");
        knownNSPrefixMap.put("http://web.resource.org/cc/", "cc");
    }

    // baseURIがrdf, rdfs, mr3の場合があるため
    private void addDefaultNS(String prefix, String addNS) {
        if (!isValidPrefix(prefix)) {
            prefix = getMR3Prefix(addNS);
        }
        if (isValidNamespace(addNS)) {
            addNameSpaceTable(Boolean.TRUE, prefix, addNS);
        }
    }

    public void setDefaultNSPrefix() {
        addDefaultNS("mr3", MR3Resource.getURI());
        if (!MR3Resource.getURI().equals(gmanager.getBaseURI())) {
            addDefaultNS("base", gmanager.getBaseURI());
        }
        addDefaultNS("rdf", RDF.getURI());
        addDefaultNS("rdfs", RDFS.getURI());
        addDefaultNS("owl", OWL.NS);
        setPrefixNSInfoSet();
    }

    private String getKnownPrefix(Model model, String ns) {
        String prefix = model.getNsURIPrefix(ns);
        if (prefix != null && (!prefix.equals(""))) {
            return prefix;
        }
        return getKnownPrefix(ns);
    }

    private static final String PREFIX = "p";

    private String getKnownPrefix(String ns) {
        String knownPrefix = knownNSPrefixMap.get(ns);
        if (knownPrefix == null) {
            knownPrefix = PREFIX;
        }
        return knownPrefix;
    }

    private String getMR3Prefix(String knownPrefix) {
        for (int i = 0; true; i++) {
            String cnt = Integer.toString(i);
            if (isValidPrefix(knownPrefix + cnt)) {
                knownPrefix = knownPrefix + cnt;
                break;
            }
        }
        return knownPrefix;
    }

    public void setCurrentNSPrefix(Model model) {
        Set<String> nsSet = new HashSet<>();
        for (Statement stmt : model.listStatements().toList()) {
            String ns = Utilities.getNameSpace(stmt.getSubject());
            if (ns != null) {
                nsSet.add(ns);
            }
            RDFNode object = stmt.getObject();
            if (object instanceof Resource) {
                Resource res = (Resource) object;
                ns = Utilities.getNameSpace(res);
                if (ns != null) {
                    nsSet.add(ns);
                }
            }
        }
        for (String ns : nsSet) {
            if (isValidNamespace(ns)) {
                String knownPrefix = getKnownPrefix(model, ns);
                if (isValidPrefix(knownPrefix) && (!knownPrefix.equals(PREFIX))) {
                    addNameSpaceTable(Boolean.TRUE, knownPrefix, ns);
                } else {
                    addNameSpaceTable(Boolean.TRUE, getMR3Prefix(getKnownPrefix(ns)), ns);
                }
            }
        }
        setPrefixNSInfoSet();
    }

    public NSTableModel getNSTableModel() {
        return nsTableModel;
    }

    public void loadState(List list) {
        Map map = (Map) list.get(0);
        NSTableModel model = (NSTableModel) list.get(1);
        for (int i = 0; i < model.getRowCount(); i++) {
            Boolean isAvailable = (Boolean) model.getValueAt(i, 0);
            String prefix = (String) model.getValueAt(i, 1);
            String ns = (String) model.getValueAt(i, 2);
            if (isValidPrefix(prefix) && isValidNamespace(ns)) {
                addNameSpaceTable(isAvailable, prefix, ns);
            }
        }
        // ここでprefixNSMapを設定しないと，上の内容を元に戻すことができない．(non validとなる）
        prefixNSMap.putAll(map);
        setPrefixNSInfoSet();
    }

    public void resetNSTable() {
        prefixNSMap = new HashMap<>();
        // 一気にすべて削除する方法がわからない．
        while (nsTableModel.getRowCount() != 0) {
            nsTableModel.removeRow(nsTableModel.getRowCount() - 1);
        }
        GraphUtilities.setNamespaceModelSet(new HashSet<>());
    }

    private void initTable() {
        Object[] columnNames = new Object[]{Translator.getString("Available"), Translator.getString("Prefix"), "URI"};
        nsTableModel = new NSTableModel(columnNames, 0);
        nsTableModel.addTableModelListener(this);
        nsTable = new JTable(nsTableModel);
        nsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        nsTable.getSelectionModel().addListSelectionListener(e -> {
            int selectedRow = nsTable.getSelectedRow();
            if (0 <= selectedRow) {
                String selectedPrefix = (String) nsTable.getValueAt(selectedRow, 1);
                String selectedNamespace = (String) nsTable.getValueAt(selectedRow, 2);
                prefixField.setText(selectedPrefix);
                nsField.setText(selectedNamespace);
            }
        });
        TableColumnModel tcModel = nsTable.getColumnModel();
        tcModel.getColumn(0).setPreferredWidth(50);
        tcModel.getColumn(1).setPreferredWidth(100);
        tcModel.getColumn(2).setPreferredWidth(450);
    }

    private void setTableLayout() {
        JScrollPane nsTableScroll = new JScrollPane(nsTable);
        nsTableScroll.setPreferredSize(new Dimension(700, 115));
        nsTableScroll.setMinimumSize(new Dimension(700, 115));
        nsPanel.add(nsTableScroll, BorderLayout.CENTER);
    }

    private void setInputLayout() {
        prefixField = new JTextField(10);
        JComponent prefixFieldP = Utilities.createTitledPanel(prefixField, MR3Constants.PREFIX);

        nsField = new JTextField(30);
        JComponent nsFieldP = Utilities.createTitledPanel(nsField, MR3Constants.NAME_SPACE);

        JPanel southPanel = new JPanel();
        southPanel.setLayout(new BorderLayout());
        southPanel.add(prefixFieldP, BorderLayout.WEST);
        southPanel.add(nsFieldP, BorderLayout.CENTER);
        southPanel.add(getButtonPanel(), BorderLayout.EAST);
        nsPanel.add(southPanel, BorderLayout.SOUTH);
    }

    private JComponent getButtonPanel() {
        addNSButton = new JButton(MR3Constants.ADD + "(A)");
        addNSButton.setMnemonic('a');
        addNSButton.addActionListener(this);
        editNSButton = new JButton(MR3Constants.EDIT + "(E)");
        editNSButton.setMnemonic('e');
        editNSButton.addActionListener(this);
        removeNSButton = new JButton(MR3Constants.REMOVE + "(R)");
        removeNSButton.setMnemonic('r');
        removeNSButton.addActionListener(this);
        cancelButton = new JButton(MR3Constants.CANCEL);
        cancelButton.setMnemonic('c');
        cancelButton.addActionListener(this);
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(1, 4, 5, 5));
        buttonPanel.add(addNSButton);
        buttonPanel.add(editNSButton);
        buttonPanel.add(removeNSButton);
        buttonPanel.add(cancelButton);
        return Utilities.createSouthPanel(buttonPanel);
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == addNSButton) {
            addNameSpaceTable(Boolean.TRUE, prefixField.getText(), nsField.getText());
            setPrefixNSInfoSet();
        } else if (e.getSource() == editNSButton) {
            editNameSpaceTable(prefixField.getText(), nsField.getText());
        } else if (e.getSource() == removeNSButton) {
            removeNameSpaceTable();
        } else if (e.getSource() == cancelButton) {
            setVisible(false);
        }
    }

    /**
     * prefix が空でなくかつ，すでに登録されていない場合true
     */
    private boolean isValidPrefix(String prefix) {
        return prefix != null && !prefix.equals("") && !prefixNSMap.keySet().contains(prefix);
    }

    /**
     * prefix が空でなくかつ，すでに登録されていない場合true
     */
    private boolean isValidPrefixWithDialog(String prefix) {
        if (prefix == null || prefix.equals("")) {
            Utilities.showErrorMessageDialog(Translator.getString("Warning.Message13"));
            return false;
        }
        if (prefixNSMap.keySet().contains(prefix)) {
            Utilities.showErrorMessageDialog(Translator.getString("Warning.Message5"));
            return false;
        }
        return true;
    }

    /**
     * 名前空間が以下の条件を満たしているか確認する
     * - 空でもnullでもない
     * - URI構文に準拠している
     * - 名前空間テーブルに登録されてない
     */
    private boolean isValidNamespace(String ns) {
        return PrefixNSUtil.isValidURI(ns) && !prefixNSMap.values().contains(ns);
    }

    /**
     * 名前空間が以下の条件を満たしているか確認する
     * - 空でもnullでもない
     * - URI構文に準拠している
     * - 名前空間テーブルに登録されてない
     */
    private boolean isValidNamespaceWithDialog(String ns) {
        if (!PrefixNSUtil.isValidURI(ns)) {
            return false;
        }
        if (prefixNSMap.values().contains(ns)) {
            Utilities.showErrorMessageDialog(Translator.getString("Warning.Message6"));
            return false;
        }
        return true;
    }

    public void addNameSpaceTable(Boolean isAvailable, String prefix, String ns) {
        if (isValidPrefixWithDialog(prefix) && isValidNamespaceWithDialog(ns)) {
            prefixNSMap.put(prefix, ns);
            Object[] list = new Object[]{isAvailable, prefix, ns};
            nsTableModel.insertRow(nsTableModel.getRowCount(), list);
            prefixField.setText("");
            nsField.setText("");
        }
    }

    public void editNameSpaceTable(String prefix, String ns) {
        int selectedRow = nsTable.getSelectedRow();
        String orgPrefix = (String) nsTable.getValueAt(selectedRow, 1);
        String orgNs = (String) nsTable.getValueAt(selectedRow, 2);

        if (orgPrefix.equals(prefix)) {
            if (!isValidNamespaceWithDialog(ns)) {
                return;
            }
        } else {
            if (orgNs.equals(ns)) {
                if (!isValidPrefixWithDialog(prefix)) {
                    return;
                }
            } else {
                if (!isValidPrefixWithDialog(prefix) || !isValidNamespaceWithDialog(ns)) {
                    return;
                }
            }
        }
        nsTableModel.editNamespace(selectedRow, orgPrefix, prefix, orgNs, ns);
    }

    private void removeNameSpaceTable() {
        int[] removeList = nsTable.getSelectedRows();
        int length = removeList.length;
        if (length == 0) {
            return;
        }
        int row = removeList[0];
        String rmPrefix = (String) nsTableModel.getValueAt(row, 1);
        String rmNS = (String) nsTableModel.getValueAt(row, 2);
        if (rmNS.equals(gmanager.getBaseURI())) {
            Utilities.showErrorMessageDialog(Translator.getString("Warning.Message7"));
            return;
        }
        if (rmNS.equals(MR3Resource.DefaultURI.getNameSpace())) {
            Utilities.showErrorMessageDialog(Translator.getString("Warning.Message8"));
            return;
        }
        if (!gmanager.getAllNameSpaceSet().contains(rmNS)) {
            prefixNSMap.remove(rmPrefix);
            nsTableModel.removeRow(row);
            setPrefixNSInfoSet();
        } else {
            Utilities.showErrorMessageDialog(Translator.getString("Warning.Message9"));
        }
    }

    public void setPrefixNSInfoSet() {
        GraphUtilities.setNamespaceModelSet(getPrefixNSInfoSet());
    }

    private boolean isCheckBoxChanged(int type, int column) {
        return (type == TableModelEvent.UPDATE && column == 0);
    }

    /**
     * テーブルのチェックボックスがチェックされたかどうか
     */
    private boolean isPrefixAvailable(int row, int column) {
        return (Boolean) nsTableModel.getValueAt(row, column);
    }

    public void tableChanged(TableModelEvent e) {
        // int row = e.getFirstRow();
        int column = e.getColumn();
        int type = e.getType();

        if (isCheckBoxChanged(type, column)) {
            setPrefixNSInfoSet();
        }
    }

    private Set<NamespaceModel> getPrefixNSInfoSet() {
        Set<NamespaceModel> infoSet = new HashSet<>();
        for (int i = 0; i < nsTableModel.getRowCount(); i++) {
            String prefix = (String) nsTableModel.getValueAt(i, 1);
            String ns = (String) nsTableModel.getValueAt(i, 2);
            infoSet.add(new NamespaceModel(prefix, ns, isPrefixAvailable(i, 0)));
        }
        return infoSet;
    }

    public class NSTableModel extends DefaultTableModel implements Serializable {

        private static final long serialVersionUID = -5977304717491874293L;

        NSTableModel(Object[] columnNames, int rowCount) {
            super(columnNames, rowCount);
        }

        public boolean isCellEditable(int row, int col) {
            if (col == 2) return false;
            return true;
        }

        public Class getColumnClass(int column) {
            Vector v = dataVector.elementAt(0);
            return v.elementAt(column).getClass();
        }

        public void editNamespace(int selectedRow, String orgPrefix, String prefix, String orgNs, String ns) {
            prefixNSMap.remove(orgPrefix);
            prefixNSMap.put(prefix, ns);
            // TODO:  replace org namespaces with new namespaces in all editors.
            super.setValueAt(prefix, selectedRow, 1);
            super.setValueAt(ns, selectedRow, 2);
            setPrefixNSInfoSet();
            gmanager.refreshGraphs();
            prefixField.setText("");
            nsField.setText("");
        }

        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            if (aValue instanceof String && columnIndex == 1) {
                String prefix = (String) aValue;
                String oldPrefix = (String) nsTableModel.getValueAt(rowIndex, 1);
                prefixNSMap.remove(oldPrefix);
                String ns = (String) nsTableModel.getValueAt(rowIndex, 2);
                prefixNSMap.put(prefix, ns);
            }
            super.setValueAt(aValue, rowIndex, columnIndex);
            setPrefixNSInfoSet();
            gmanager.refreshGraphs();
        }
    }
}
