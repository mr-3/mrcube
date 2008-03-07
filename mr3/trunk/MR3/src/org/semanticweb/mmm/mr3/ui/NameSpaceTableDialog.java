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

package org.semanticweb.mmm.mr3.ui;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.util.List;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;

import org.semanticweb.mmm.mr3.data.*;
import org.semanticweb.mmm.mr3.jgraph.*;
import org.semanticweb.mmm.mr3.util.*;

import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.vocabulary.*;

/**
 * 
 * 名前空間と接頭辞の対応付けをテーブルで行う チェックにより，Class, Property, Resourceの名前空間を接頭辞で置き換える
 * 接頭辞の名前変更はテーブルから行うことができる
 * 
 * @author takeshi morita
 */
public class NameSpaceTableDialog extends JDialog implements ActionListener, TableModelListener, Serializable {

    private Map<String, String> prefixNSMap;
    private JTable nsTable;
    private NSTableModel nsTableModel;

    private static final long serialVersionUID = 5974381131839067739L;

    transient private JButton addNSButton;
    transient private JButton removeNSButton;
    transient private JButton cancelButton;
    transient private JTextField prefixField;
    transient private JTextField nsField;
    transient private JPanel nsPanel;

    transient private GraphManager gmanager;
    transient private Map<String, String> knownNSPrefixMap;

    private static final String WARNING = Translator.getString("Warning");

    // private static final ImageIcon ICON =
    // Utilities.getImageIcon(Translator.getString("NameSpaceTable.Icon"));

    public NameSpaceTableDialog(GraphManager gm) {
        super(gm.getRootFrame(), Translator.getString("NameSpaceTable.Title"), false);
        gmanager = gm;
        prefixNSMap = new HashMap<String, String>();
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
        knownNSPrefixMap = new HashMap<String, String>();
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
        if (isValidNS(addNS)) {
            addNameSpaceTable(new Boolean(true), prefix, addNS);
        }
    }

    public void setDefaultNSPrefix() {
        addDefaultNS("mr3", MR3Resource.getURI());
        addDefaultNS("base", gmanager.getBaseURI());
        addDefaultNS("rdf", RDF.getURI());
        addDefaultNS("rdfs", RDFS.getURI());
        addDefaultNS("owl", OWL.NS);
        setPrefixNSInfoSet();
    }

    private String getKnownPrefix(Model model, String ns) {
        String prefix = model.getNsURIPrefix(ns);
        // System.out.println(ns);
        // System.out.println(prefix);
        if (prefix != null && (!prefix.equals(""))) { return prefix; }
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
        Set<String> nsSet = new HashSet<String>();
        for (StmtIterator i = model.listStatements(); i.hasNext();) {
            Statement stmt = i.nextStatement();
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
            if (isValidNS(ns)) {
                String knownPrefix = getKnownPrefix(model, ns);
                if (isValidPrefix(knownPrefix) && (!knownPrefix.equals(PREFIX))) {
                    addNameSpaceTable(new Boolean(true), knownPrefix, ns);
                } else {
                    addNameSpaceTable(new Boolean(true), getMR3Prefix(getKnownPrefix(ns)), ns);
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
            if (isValidPrefix(prefix) && isValidNS(ns)) {
                addNameSpaceTable(isAvailable, prefix, ns);
            }
        }
        // ここでprefixNSMapを設定しないと，上の内容を元に戻すことができない．(non validとなる）
        prefixNSMap.putAll(map);
        setPrefixNSInfoSet();
    }

    public void resetNSTable() {
        prefixNSMap = new HashMap<String, String>();
        // 一気にすべて削除する方法がわからない．
        while (nsTableModel.getRowCount() != 0) {
            nsTableModel.removeRow(nsTableModel.getRowCount() - 1);
        }
        GraphUtilities.setPrefixNSInfoSet(new HashSet<PrefixNSInfo>());
    }

    private void initTable() {
        Object[] columnNames = new Object[] { Translator.getString("Available"), Translator.getString("Prefix"), "URI"};
        nsTableModel = new NSTableModel(columnNames, 0);
        nsTableModel.addTableModelListener(this);
        nsTable = new JTable(nsTableModel);
        nsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
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
        removeNSButton = new JButton(MR3Constants.REMOVE + "(R)");
        removeNSButton.setMnemonic('r');
        removeNSButton.addActionListener(this);
        cancelButton = new JButton(MR3Constants.CANCEL);
        cancelButton.setMnemonic('c');
        cancelButton.addActionListener(this);
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(1, 3, 5, 5));
        buttonPanel.add(addNSButton);
        buttonPanel.add(removeNSButton);
        buttonPanel.add(cancelButton);
        return Utilities.createSouthPanel(buttonPanel);
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == addNSButton) {
            addNameSpaceTable(new Boolean(true), prefixField.getText(), nsField.getText());
            setPrefixNSInfoSet();
        } else if (e.getSource() == removeNSButton) {
            removeNameSpaceTable();
        } else if (e.getSource() == cancelButton) {
            setVisible(false);
        }
    }

    private boolean isValidPrefix(String prefix) {
        Set keySet = prefixNSMap.keySet();
        return (!keySet.contains(prefix) && !prefix.equals(""));
    }

    private boolean isValidNS(String ns) {
        Collection values = prefixNSMap.values();
        return (ns != null && !ns.equals("") && !ns.equals("http://") && !values.contains(ns));
    }

    /** prefix が空でなくかつ，すでに登録されていない場合true */
    private boolean isValidPrefixWithWarning(String prefix) {
        if (isValidPrefix(prefix)) { return true; }
        JOptionPane.showMessageDialog(gmanager.getDesktopTabbedPane(), Translator.getString("Warning.Message5"), WARNING,
                JOptionPane.ERROR_MESSAGE);
        return false;
    }

    /** nsが空でもnullでもなく，すでに登録されてない場合 true */
    private boolean isValidNSWithWarning(String ns) {
        if (isValidNS(ns)) { return true; }
        JOptionPane.showMessageDialog(gmanager.getDesktopTabbedPane(), Translator.getString("Warning.Message6"), WARNING,
                JOptionPane.ERROR_MESSAGE);
        return false;
    }

    public void addNameSpaceTable(Boolean isAvailable, String prefix, String ns) {
        if (isValidPrefixWithWarning(prefix) && isValidNSWithWarning(ns)) {
            prefixNSMap.put(prefix, ns);
            Object[] list = new Object[] { isAvailable, prefix, ns};
            nsTableModel.insertRow(nsTableModel.getRowCount(), list);
            prefixField.setText("");
            nsField.setText("");
        }
    }

    private void removeNameSpaceTable() {
        int[] removeList = nsTable.getSelectedRows();
        int length = removeList.length;
        // どうやったら，複数のrowを消すせるのかがよくわからない．
        // modelから消した時点でrow番号が変わってしまうのが原因
        if (length == 0) { return; }
        int row = removeList[0];
        String rmPrefix = (String) nsTableModel.getValueAt(row, 1);
        String rmNS = (String) nsTableModel.getValueAt(row, 2);
        if (rmNS.equals(gmanager.getBaseURI())) {
            JOptionPane.showMessageDialog(gmanager.getDesktopTabbedPane(), Translator.getString("Warning.Message7"), WARNING,
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (rmNS.equals(MR3Resource.DefaultURI.getNameSpace())) {
            JOptionPane.showMessageDialog(gmanager.getDesktopTabbedPane(), Translator.getString("Warning.Message8"), WARNING,
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (!gmanager.getAllNameSpaceSet().contains(rmNS)) {
            prefixNSMap.remove(rmPrefix);
            nsTableModel.removeRow(row);
            setPrefixNSInfoSet();
        } else {
            JOptionPane.showMessageDialog(gmanager.getDesktopTabbedPane(), Translator.getString("Warning.Message9"), WARNING,
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    public void setPrefixNSInfoSet() {
        GraphUtilities.setPrefixNSInfoSet(getPrefixNSInfoSet());
    }

    private boolean isCheckBoxChanged(int type, int column) {
        return (type == TableModelEvent.UPDATE && column == 0);
    }

    /** テーブルのチェックボックスがチェックされたかどうか */
    private boolean isPrefixAvailable(int row, int column) {
        Boolean isPrefixAvailable = (Boolean) nsTableModel.getValueAt(row, column);
        return isPrefixAvailable.booleanValue();
    }

    public void tableChanged(TableModelEvent e) {
        // int row = e.getFirstRow();
        int column = e.getColumn();
        int type = e.getType();

        if (isCheckBoxChanged(type, column)) {
            setPrefixNSInfoSet();
        }
    }

    private Set<PrefixNSInfo> getPrefixNSInfoSet() {
        Set<PrefixNSInfo> infoSet = new HashSet<PrefixNSInfo>();
        for (int i = 0; i < nsTableModel.getRowCount(); i++) {
            String prefix = (String) nsTableModel.getValueAt(i, 1);
            String ns = (String) nsTableModel.getValueAt(i, 2);
            infoSet.add(new PrefixNSInfo(prefix, ns, isPrefixAvailable(i, 0)));
        }
        return infoSet;
    }

    public class NSTableModel extends DefaultTableModel implements Serializable {

        private static final long serialVersionUID = -5977304717491874293L;

        public NSTableModel(Object[] columnNames, int rowCount) {
            super(columnNames, rowCount);
        }

        public boolean isCellEditable(int row, int col) {
            if (col == 2) return false;
            return true;
        }

        public Class getColumnClass(int column) {
            Vector v = (Vector) dataVector.elementAt(0);
            return v.elementAt(column).getClass();
        }

        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            if (aValue instanceof String) {
                String prefix = (String) aValue;
                // 多分prefixのチェックはいらない．
                String oldPrefix = (String) nsTableModel.getValueAt(rowIndex, columnIndex);
                prefixNSMap.remove(oldPrefix);
                String ns = (String) nsTableModel.getValueAt(rowIndex, 2);
                prefixNSMap.put(prefix, ns);
            }
            super.setValueAt(aValue, rowIndex, columnIndex);
            setPrefixNSInfoSet();
        }
    }
}
