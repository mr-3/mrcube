/*
 * @(#) PropertyPanel.java
 * 
 * Copyright (C) 2003 The MMM Project
 * 
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation; either version 2.1 of the License, or (at your
 * option) any later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *  
 */

package org.semanticweb.mmm.mr3.ui;

import java.awt.*;
import java.awt.event.*;
import java.lang.ref.*;
import java.util.*;

import javax.swing.*;
import javax.swing.event.*;

import org.jgraph.graph.*;
import org.semanticweb.mmm.mr3.data.*;
import org.semanticweb.mmm.mr3.data.MR3Constants.*;
import org.semanticweb.mmm.mr3.jgraph.*;
import org.semanticweb.mmm.mr3.util.*;

/**
 * @author takeshi morita
 */
public class PropertyPanel extends OntologyPanel {

    private RegionPanel regionPanel;
    private JList supProperties;
    private WeakReference selectRDFSDialogRef;

    public PropertyPanel(GraphManager manager) {
        super(manager.getPropertyGraph(), manager);
        labelPanel.setGraphType(GraphType.PROPERTY);
        commentPanel.setGraphType(GraphType.PROPERTY);
        // setBorder(BorderFactory.createTitledBorder(Translator.getString("AttributeDialog.OntPropertyAttribute.Text")));
        selectRDFSDialogRef = new WeakReference<SelectRDFSDialog>(null);

        regionPanel = new RegionPanel();
        supProperties = new JList();

        menuList = new JList(new Object[] { basePanel.toString(), labelPanel.toString(), commentPanel.toString(),
                regionPanel.toString(), Translator.getString("Instances"), Translator.getString("SuperProperties")});
        menuList.addListSelectionListener(this);
        cardLayout = new CardLayout();
        menuPanel = new JPanel();
        menuPanel.setLayout(cardLayout);
        menuPanel.add(basePanel.toString(), basePanel);
        menuPanel.add(labelPanel.toString(), labelPanel);
        menuPanel.add(commentPanel.toString(), commentPanel);
        menuPanel.add(regionPanel.toString(), regionPanel);
        menuPanel.add(Translator.getString("Instances"), instanceListScroll);
        menuPanel.add(Translator.getString("SuperProperties"), new JScrollPane(supProperties));

        menuList.setSelectedIndex(0);

        setLayout(new BorderLayout());
        JPanel titlePanel = new JPanel();
        titlePanel.setBackground(new Color(49, 105, 198));
        ImageIcon icon = Utilities.getImageIcon(Translator.getString("PropertyEditor.Icon"));
        JLabel titleLabel = new JLabel(Translator.getString("AttributeDialog.OntPropertyAttribute.Text"), icon,
                SwingConstants.LEFT);
        titleLabel.setForeground(Color.white);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 13));
        titlePanel.setLayout(new BorderLayout());
        titlePanel.add(titleLabel, BorderLayout.WEST);

        add(titlePanel, BorderLayout.NORTH);

        add(menuPanel, BorderLayout.CENTER);
        add(Utilities.createTitledPanel(menuList, "", MENU_WIDTH, MENU_WIDTH), BorderLayout.WEST);
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(1, 3, 5, 5));
        buttonPanel.add(applyButton);
        buttonPanel.add(resetButton);
        buttonPanel.add(cancelButton);
        add(Utilities.createEastPanel(buttonPanel), BorderLayout.SOUTH);
    }

    public void setInstanceList() {
        instanceList.setListData(gmanager.getPropertyInstanceSet(cell).toArray());
    }

    class InstanceAction implements ListSelectionListener {
        public void valueChanged(ListSelectionEvent e) {

        }
    }

    class RegionPanel extends JPanel implements ListSelectionListener {

        private JList domainList;
        private JList rangeList;

        private JButton addDomainButton;
        private JButton addRangeButton;
        private JButton removeRegionListButton;

        private JScrollPane domainListScroll;
        private JScrollPane rangeListScroll;

        RegionPanel() {
            domainList = new JList();
            domainList.addListSelectionListener(this);
            domainListScroll = new JScrollPane(domainList);
            domainListScroll.setBorder(BorderFactory.createTitledBorder(Translator.getString("Domain")));

            rangeList = new JList();
            rangeList.addListSelectionListener(this);
            rangeListScroll = new JScrollPane(rangeList);
            rangeListScroll.setBorder(BorderFactory.createTitledBorder(Translator.getString("Range")));

            JPanel regionListPanel = new JPanel();
            regionListPanel.setLayout(new GridLayout(1, 2));
            regionListPanel.add(domainListScroll);
            regionListPanel.add(rangeListScroll);

            setLayout(new BorderLayout());
            add(regionListPanel, BorderLayout.CENTER);
            add(getRegionButtonPanel(), BorderLayout.SOUTH);
        }

        public void valueChanged(ListSelectionEvent e) {
            if (e.getSource() == domainList) {
                if (!rangeList.isSelectionEmpty()) {
                    rangeList.clearSelection();
                }
            } else if (e.getSource() == rangeList) {
                if (!domainList.isSelectionEmpty()) {
                    domainList.clearSelection();
                }
            }
        }

        public String toString() {
            return Translator.getString("Region");
        }

        public JList getDomainList() {
            return domainList;
        }

        public JList getRangeList() {
            return rangeList;
        }

        private JComponent getRegionButtonPanel() {
            AddRegionAction addRegionAction = new AddRegionAction();
            addDomainButton = new JButton(Translator.getString("AddDomain"));
            addDomainButton.addActionListener(addRegionAction);
            addRangeButton = new JButton(Translator.getString("AddRange"));
            addRangeButton.addActionListener(addRegionAction);
            removeRegionListButton = new JButton(Translator.getString("Remove"));
            removeRegionListButton.addActionListener(new RemoveListAction());

            JPanel buttonPanel = new JPanel();
            buttonPanel.setLayout(new GridLayout(1, 3, 5, 5));
            buttonPanel.add(addDomainButton);
            buttonPanel.add(addRangeButton);
            buttonPanel.add(removeRegionListButton);
            return Utilities.createEastPanel(buttonPanel);
        }

        class RemoveListAction implements ActionListener {
            public void actionPerformed(ActionEvent e) {
                PropertyInfo info = (PropertyInfo) GraphConstants.getValue(cell.getAttributes());
                if (!domainList.isSelectionEmpty()) {
                    Set beforeDomainSet = new HashSet(info.getDomain());
                    Object[] rlist = domainList.getSelectedValues();
                    for (int i = 0; i < rlist.length; i++) {
                        info.removeDomain(rlist[i]);
                    }
                    rdfsInfoMap.putURICellMap(info, cell);
                    domainList.setListData(info.getDomain().toArray());
                    HistoryManager.saveHistory(HistoryType.DELETE_ONT_PROPERTY_DOMAIN, beforeDomainSet, info.getDomain(),
                            info.getURIStr());
                }
                if (!rangeList.isSelectionEmpty()) {
                    Set beforeRangeSet = new HashSet(info.getRange());
                    Object[] rlist = rangeList.getSelectedValues();
                    for (int i = 0; i < rlist.length; i++) {
                        info.removeRange(rlist[i]);
                    }
                    rdfsInfoMap.putURICellMap(info, cell);
                    rangeList.setListData(info.getRange().toArray());
                    HistoryManager.saveHistory(HistoryType.DELETE_ONT_PROPERTY_RANGE, beforeRangeSet, info.getRange(), info
                            .getURIStr());
                }
            }
        }

        class AddRegionAction implements ActionListener {
            public void actionPerformed(ActionEvent e) {
                PropertyInfo info = (PropertyInfo) GraphConstants.getValue(cell.getAttributes());
                if (info == null) { return; }
                if (e.getSource() == addDomainButton) {
                    SelectRDFSDialog regionDialog = getSelectRDFSDialog(info.getDomain());
                    setDomainList((Set) regionDialog.getValue());
                } else if (e.getSource() == addRangeButton) {
                    SelectRDFSDialog regionDialog = getSelectRDFSDialog(info.getRange());
                    setRangeList((Set) regionDialog.getValue());
                }
            }

            private void setDomainList(Set<Object> set) {
                if (set != null) {
                    PropertyInfo info = (PropertyInfo) GraphConstants.getValue(cell.getAttributes());
                    Set beforeDomainSet = new HashSet(info.getDomain());
                    info.addAllDomain(set);
                    domainList.setListData(info.getDomain().toArray());
                    HistoryManager.saveHistory(HistoryType.ADD_ONT_PROPERTY_DOMAIN, beforeDomainSet, info.getDomain(), info
                            .getURIStr());
                }
            }

            private void setRangeList(Set<Object> set) {
                if (set != null) {
                    PropertyInfo info = (PropertyInfo) GraphConstants.getValue(cell.getAttributes());
                    Set beforeRangeSet = new HashSet(info.getRange());
                    info.addAllRange(set);
                    rangeList.setListData(info.getRange().toArray());
                    HistoryManager.saveHistory(HistoryType.ADD_ONT_PROPERTY_RANGE, beforeRangeSet, info.getRange(), info
                            .getURIStr());
                }
            }
        }
    }

    public SelectRDFSDialog getSelectRDFSDialog(Set regionSet) {
        SelectRDFSDialog result = (SelectRDFSDialog) selectRDFSDialogRef.get();
        if (result == null) {
            result = new SelectRDFSDialog(Translator.getString("SelectRegionDialog.Title"), gmanager);
            selectRDFSDialogRef = new WeakReference<SelectRDFSDialog>(result);
        }
        result.replaceGraph(gmanager.getClassGraph());
        result.setRegionSet(regionSet);
        result.setVisible(true);
        return result;
    }

    public void setValue(Set<GraphCell> supCellSet) {
        PropertyInfo propInfo = (PropertyInfo) rdfsInfo;
        super.setValue();
        basePanel.setMetaClassList(gmanager.getPropertyClassList());
        if (rdfsInfoMap.isPropertyCell(MR3Resource.Property)) {
            supCellSet.remove(gmanager.getPropertyCell(MR3Resource.Property, false));
        }
        supProperties.setListData(getTargetInfo(supCellSet));
        regionPanel.getDomainList().setListData(propInfo.getDomain().toArray());
        regionPanel.getRangeList().setListData(propInfo.getRange().toArray());
    }
}
