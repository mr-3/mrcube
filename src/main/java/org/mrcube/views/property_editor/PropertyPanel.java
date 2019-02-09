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

package org.mrcube.views.property_editor;

import org.jgraph.graph.GraphCell;
import org.jgraph.graph.GraphConstants;
import org.mrcube.jgraph.GraphManager;
import org.mrcube.models.MR3Constants;
import org.mrcube.models.MR3Constants.GraphType;
import org.mrcube.models.MR3Constants.HistoryType;
import org.mrcube.models.MR3Resource;
import org.mrcube.models.PropertyModel;
import org.mrcube.models.RDFSModelMap;
import org.mrcube.utils.Translator;
import org.mrcube.utils.Utilities;
import org.mrcube.views.HistoryManager;
import org.mrcube.views.OntologyPanel;
import org.mrcube.views.SelectRDFSDialog;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.ref.WeakReference;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Takeshi Morita
 */
public class PropertyPanel extends OntologyPanel {

    private final RegionPanel regionPanel;
    private final JList supProperties;
    private WeakReference selectRDFSDialogRef;

    public PropertyPanel(GraphManager manager) {
        super(manager.getCurrentPropertyGraph(), manager);
        labelPanel.setGraphType(GraphType.PROPERTY);
        commentPanel.setGraphType(GraphType.PROPERTY);
        selectRDFSDialogRef = new WeakReference<SelectRDFSDialog>(null);

        regionPanel = new RegionPanel();
        supProperties = new JList();

        menuList = new JList(new Object[]{basePanel.toString(), labelPanel.toString(), commentPanel.toString(),
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
        titlePanel.setBackground(Color.black);
        ImageIcon icon = Utilities.getImageIcon(Translator.getString("PropertyEditor.Icon"));
        JLabel titleLabel = new JLabel(Translator.getString("AttributeDialog.OntPropertyAttribute.Text"), icon,
                SwingConstants.LEFT);
        titleLabel.setForeground(Color.white);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, MR3Constants.TITLE_FONT_SIZE));
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

    class RegionPanel extends JPanel implements ListSelectionListener {

        private final JList domainList;
        private final JList rangeList;

        private JButton addDomainButton;
        private JButton addRangeButton;
        private JButton removeRegionListButton;

        private final JScrollPane domainListScroll;
        private final JScrollPane rangeListScroll;

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
            return Translator.getString("Domain") + "/" +
                    Translator.getString("Range");
        }

        JList getDomainList() {
            return domainList;
        }

        JList getRangeList() {
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
                PropertyModel info = (PropertyModel) GraphConstants.getValue(cell.getAttributes());
                if (!domainList.isSelectionEmpty()) {
                    Set beforeDomainSet = new HashSet(info.getDomain());
                    for (Object rd : domainList.getSelectedValuesList()) {
                        info.removeDomain(rd);
                    }
                    RDFSModelMap rdfsModelMap = gmanager.getCurrentRDFSInfoMap();
                    rdfsModelMap.putURICellMap(info, cell);
                    domainList.setListData(info.getDomain().toArray());
                    HistoryManager.saveHistory(HistoryType.DELETE_ONT_PROPERTY_DOMAIN, beforeDomainSet, info
                            .getDomain(), info.getURIStr());
                }
                if (!rangeList.isSelectionEmpty()) {
                    Set beforeRangeSet = new HashSet(info.getRange());
                    for (Object rr : rangeList.getSelectedValuesList()) {
                        info.removeRange(rr);
                    }
                    RDFSModelMap rdfsModelMap = gmanager.getCurrentRDFSInfoMap();
                    rdfsModelMap.putURICellMap(info, cell);
                    rangeList.setListData(info.getRange().toArray());
                    HistoryManager.saveHistory(HistoryType.DELETE_ONT_PROPERTY_RANGE, beforeRangeSet, info.getRange(),
                            info.getURIStr());
                }
            }
        }

        class AddRegionAction implements ActionListener {
            public void actionPerformed(ActionEvent e) {
                PropertyModel info = (PropertyModel) GraphConstants.getValue(cell.getAttributes());
                if (info == null) {
                    return;
                }
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
                    PropertyModel info = (PropertyModel) GraphConstants.getValue(cell.getAttributes());
                    Set<GraphCell> beforeDomainSet = new HashSet<>(info.getDomain());
                    info.addAllDomain(set);
                    domainList.setListData(info.getDomain().toArray());
                    HistoryManager.saveHistory(HistoryType.ADD_ONT_PROPERTY_DOMAIN, beforeDomainSet, info.getDomain(),
                            info.getURIStr());
                }
            }

            private void setRangeList(Set<GraphCell> set) {
                if (set != null) {
                    PropertyModel info = (PropertyModel) GraphConstants.getValue(cell.getAttributes());
                    Set<GraphCell> beforeRangeSet = new HashSet<>(info.getRange());
                    info.addAllRange(set);
                    rangeList.setListData(info.getRange().toArray());
                    HistoryManager.saveHistory(HistoryType.ADD_ONT_PROPERTY_RANGE, beforeRangeSet, info.getRange(),
                            info.getURIStr());
                }
            }
        }
    }

    private SelectRDFSDialog getSelectRDFSDialog(Set regionSet) {
        SelectRDFSDialog result = (SelectRDFSDialog) selectRDFSDialogRef.get();
        if (result == null) {
            result = new SelectRDFSDialog(Translator.getString("SelectRegionDialog.Title"), gmanager);
            selectRDFSDialogRef = new WeakReference<>(result);
        }
        result.replaceGraph(gmanager.getCurrentClassGraph());
        result.setRegionSet(regionSet);
        result.setVisible(true);
        return result;
    }

    public void setValue(Set<GraphCell> supCellSet) {
        PropertyModel propInfo = (PropertyModel) rdfsModel;
        super.setValue();
        basePanel.setMetaClassList(gmanager.getPropertyClassList());
        RDFSModelMap rdfsModelMap = gmanager.getCurrentRDFSInfoMap();
        if (rdfsModelMap.isPropertyCell(MR3Resource.Property)) {
            supCellSet.remove(gmanager.getPropertyCell(MR3Resource.Property, false));
        }
        supProperties.setListData(getTargetInfo(supCellSet));
        regionPanel.getDomainList().setListData(propInfo.getDomain().toArray());
        regionPanel.getRangeList().setListData(propInfo.getRange().toArray());
    }
}
