/*
 * Project Name: MR^3 (Meta-Model Management based on RDFs Revision Reflection)
 * Project Website: http://mr3.sourceforge.net/
 * 
 * Copyright (C) 2003-2015 Yamaguchi Laboratory, Keio University. All rights reserved. 
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

package net.sourceforge.mr3.ui;

import java.awt.*;
import java.awt.event.*;
import java.lang.ref.*;
import java.util.*;

import javax.swing.*;
import javax.swing.event.*;

import net.sourceforge.mr3.data.*;
import net.sourceforge.mr3.data.MR3Constants.*;
import net.sourceforge.mr3.jgraph.*;
import net.sourceforge.mr3.util.*;

import org.jgraph.graph.*;

/**
 * @author Takeshi Morita
 */
public class PropertyPanel extends OntologyPanel {

    private RegionPanel regionPanel;
    private JList supProperties;
    private WeakReference selectRDFSDialogRef;

    public PropertyPanel(GraphManager manager) {
        super(manager.getCurrentPropertyGraph(), manager);
        labelPanel.setGraphType(GraphType.PROPERTY);
        commentPanel.setGraphType(GraphType.PROPERTY);
        // setBorder(BorderFactory.createTitledBorder(Translator.getString(
        // "AttributeDialog.OntPropertyAttribute.Text")));
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
                    for (Object rd : domainList.getSelectedValues()) {
                        info.removeDomain(rd);
                    }
                    RDFSInfoMap rdfsInfoMap = gmanager.getCurrentRDFSInfoMap();
                    rdfsInfoMap.putURICellMap(info, cell);
                    domainList.setListData(info.getDomain().toArray());
                    HistoryManager.saveHistory(HistoryType.DELETE_ONT_PROPERTY_DOMAIN, beforeDomainSet, info
                            .getDomain(), info.getURIStr());
                }
                if (!rangeList.isSelectionEmpty()) {
                    Set beforeRangeSet = new HashSet(info.getRange());
                    for (Object rr : rangeList.getSelectedValues()) {
                        info.removeRange(rr);
                    }
                    RDFSInfoMap rdfsInfoMap = gmanager.getCurrentRDFSInfoMap();
                    rdfsInfoMap.putURICellMap(info, cell);
                    rangeList.setListData(info.getRange().toArray());
                    HistoryManager.saveHistory(HistoryType.DELETE_ONT_PROPERTY_RANGE, beforeRangeSet, info.getRange(),
                            info.getURIStr());
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

            private void setDomainList(Set<GraphCell> set) {
                if (set != null) {
                    PropertyInfo info = (PropertyInfo) GraphConstants.getValue(cell.getAttributes());
                    Set<GraphCell> beforeDomainSet = new HashSet<GraphCell>(info.getDomain());
                    info.addAllDomain(set);
                    domainList.setListData(info.getDomain().toArray());
                    HistoryManager.saveHistory(HistoryType.ADD_ONT_PROPERTY_DOMAIN, beforeDomainSet, info.getDomain(),
                            info.getURIStr());
                }
            }

            private void setRangeList(Set<GraphCell> set) {
                if (set != null) {
                    PropertyInfo info = (PropertyInfo) GraphConstants.getValue(cell.getAttributes());
                    Set<GraphCell> beforeRangeSet = new HashSet<GraphCell>(info.getRange());
                    info.addAllRange(set);
                    rangeList.setListData(info.getRange().toArray());
                    HistoryManager.saveHistory(HistoryType.ADD_ONT_PROPERTY_RANGE, beforeRangeSet, info.getRange(),
                            info.getURIStr());
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
        result.replaceGraph(gmanager.getCurrentClassGraph());
        result.setRegionSet(regionSet);
        result.setVisible(true);
        return result;
    }

    public void setValue(Set<GraphCell> supCellSet) {
        PropertyInfo propInfo = (PropertyInfo) rdfsInfo;
        super.setValue();
        basePanel.setMetaClassList(gmanager.getPropertyClassList());
        RDFSInfoMap rdfsInfoMap = gmanager.getCurrentRDFSInfoMap();
        if (rdfsInfoMap.isPropertyCell(MR3Resource.Property)) {
            supCellSet.remove(gmanager.getPropertyCell(MR3Resource.Property, false));
        }
        supProperties.setListData(getTargetInfo(supCellSet));
        regionPanel.getDomainList().setListData(propInfo.getDomain().toArray());
        regionPanel.getRangeList().setListData(propInfo.getRange().toArray());
    }
}
