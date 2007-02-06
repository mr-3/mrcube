/*
 * @(#) ReplaceRDFSDialog.java
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
import java.awt.Container;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;
import javax.swing.event.*;

import org.jgraph.graph.*;
import org.semanticweb.mmm.mr3.data.*;
import org.semanticweb.mmm.mr3.io.*;
import org.semanticweb.mmm.mr3.jgraph.*;
import org.semanticweb.mmm.mr3.util.*;

import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.vocabulary.*;

/**
 * @author takeshi morita
 */
public class ReplaceRDFSDialog extends JDialog implements ListSelectionListener, ActionListener {

    private GraphManager gmanager;
    private RDFSModelExtraction rdfsModelExtraction;

    private JList currentClassList;
    private DefaultListModel currentClassListModel;
    private JList replaceClassList;
    private DefaultListModel replaceClassListModel;
    private JList currentPropertyList;
    private DefaultListModel currentPropertyListModel;
    private JList replacePropertyList;
    private DefaultListModel replacePropertyListModel;

    private JButton replaceClassUpButton;
    private JButton replaceClassDownButton;
    private JButton replacePropertyUpButton;
    private JButton replacePropertyDownButton;

    private JButton applyButton;
    private JButton cancelButton;

    private static final int DIALOG_WIDTH = 650;
    private static final int DIALOG_HEIGHT = 450;

    private boolean isApply;

    private static final String NULL = "NULL";

    private RDFSInfoMap rdfsInfoMap = RDFSInfoMap.getInstance();

    private static final ImageIcon UP_ICON = Utilities.getImageIcon(Translator.getString("ReplaceRDFSDialog.Icon.up"));
    private static final ImageIcon DOWN_ICON = Utilities.getImageIcon(Translator
            .getString("ReplaceRDFSDialog.Icon.down"));

    public ReplaceRDFSDialog(GraphManager gm) {
        super(gm.getRootFrame(), Translator.getString("ReplaceRDFSDialog.Title"), true);
        gmanager = gm;

        rdfsModelExtraction = new RDFSModelExtraction(gm);

        currentClassListModel = new DefaultListModel();
        currentClassList = new JList(currentClassListModel);
        currentClassList.addListSelectionListener(this);
        JScrollPane currentClassListScroll = new JScrollPane(currentClassList);
        currentClassListScroll.setBorder(BorderFactory.createTitledBorder(Translator
                .getString("ReplaceRDFSDialog.CurrentClassList")));

        replaceClassListModel = new DefaultListModel();
        replaceClassList = new JList(replaceClassListModel);
        replaceClassList.addListSelectionListener(this);
        JScrollPane replaceClassListScroll = new JScrollPane(replaceClassList);
        replaceClassListScroll.setBorder(BorderFactory.createTitledBorder(Translator
                .getString("ReplaceRDFSDialog.ReplaceClassList")));

        currentPropertyListModel = new DefaultListModel();
        currentPropertyList = new JList(currentPropertyListModel);
        currentPropertyList.addListSelectionListener(this);
        JScrollPane currentPropertyListScroll = new JScrollPane(currentPropertyList);
        currentPropertyListScroll.setBorder(BorderFactory.createTitledBorder(Translator
                .getString("ReplaceRDFSDialog.CurrentPropertyList")));

        replacePropertyListModel = new DefaultListModel();
        replacePropertyList = new JList(replacePropertyListModel);
        replacePropertyList.addListSelectionListener(this);
        JScrollPane replacePropertyListScroll = new JScrollPane(replacePropertyList);
        replacePropertyListScroll.setBorder(BorderFactory.createTitledBorder(Translator
                .getString("ReplaceRDFSDialog.ReplacePropertyList")));

        replaceClassUpButton = new JButton(UP_ICON);
        replaceClassUpButton.addActionListener(this);
        replaceClassDownButton = new JButton(DOWN_ICON);
        replaceClassDownButton.addActionListener(this);
        JPanel replaceClassButtonPanel = new JPanel();
        replaceClassButtonPanel.setLayout(new BoxLayout(replaceClassButtonPanel, BoxLayout.Y_AXIS));
        replaceClassButtonPanel.add(replaceClassUpButton);
        replaceClassButtonPanel.add(replaceClassDownButton);

        replacePropertyUpButton = new JButton(UP_ICON);
        replacePropertyUpButton.addActionListener(this);
        replacePropertyDownButton = new JButton(DOWN_ICON);
        replacePropertyDownButton.addActionListener(this);
        JPanel replacePropertyButtonPanel = new JPanel();
        replacePropertyButtonPanel.setLayout(new BoxLayout(replacePropertyButtonPanel, BoxLayout.Y_AXIS));
        replacePropertyButtonPanel.add(replacePropertyUpButton);
        replacePropertyButtonPanel.add(replacePropertyDownButton);

        applyButton = new JButton(MR3Constants.APPLY);
        applyButton.setMnemonic('a');
        applyButton.addActionListener(this);
        cancelButton = new JButton(MR3Constants.CANCEL);
        cancelButton.setMnemonic('c');
        cancelButton.addActionListener(this);

        JPanel classPanel = new JPanel();
        classPanel.setLayout(new BoxLayout(classPanel, BoxLayout.X_AXIS));
        classPanel.add(currentClassListScroll);
        classPanel.add(replaceClassListScroll);
        classPanel.add(replaceClassButtonPanel);
        classPanel.setBorder(BorderFactory.createTitledBorder(Translator.getString("Class")));

        JPanel propertyPanel = new JPanel();
        propertyPanel.setLayout(new BoxLayout(propertyPanel, BoxLayout.X_AXIS));
        propertyPanel.add(currentPropertyListScroll);
        propertyPanel.add(replacePropertyListScroll);
        propertyPanel.add(replacePropertyButtonPanel);
        propertyPanel.setBorder(BorderFactory.createTitledBorder(Translator.getString("Property")));

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(applyButton);
        buttonPanel.add(cancelButton);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.add(classPanel);
        mainPanel.add(propertyPanel);

        Container contentPane = getContentPane();
        contentPane.add(mainPanel, BorderLayout.CENTER);
        contentPane.add(Utilities.createEastPanel(buttonPanel), BorderLayout.SOUTH);

        setSize(new Dimension(DIALOG_WIDTH, DIALOG_HEIGHT));
        setLocationRelativeTo(gm.getRootFrame());
    }

    public void initListData(Model replaceModel) {
        isApply = false;
        currentClassListModel.clear();
        replaceClassListModel.clear();
        currentPropertyListModel.clear();
        replacePropertyListModel.clear();

        rdfsModelExtraction.extractClassModel(replaceModel);
        rdfsModelExtraction.extractPropertyModel(replaceModel);

        setListData(currentClassListModel, gmanager.getClassSet());
        setListData(replaceClassListModel, rdfsInfoMap.getClassSet(new HashSet<String>(), RDFS.Resource));
        setListData(currentPropertyListModel, gmanager.getPropertySet());
        Set replacePropertySet = new HashSet();

        for (Resource res : rdfsInfoMap.getRootProperties()) {
            replacePropertySet.add(res.getURI());
            rdfsInfoMap.getPropertySet(replacePropertySet, res);
        }
        setListData(replacePropertyListModel, replacePropertySet);
        fixListData();
    }

    private void setListData(DefaultListModel listModel, Set<String> dataSet) {
        for (Iterator i = dataSet.iterator(); i.hasNext();) {
            listModel.addElement(i.next());
        }
    }

    private void fixListData() {
        fixListData(currentClassListModel, replaceClassListModel);
        fixListData(currentPropertyListModel, replacePropertyListModel);
    }

    /**
     * 1. 同一クラスは，置換前リストと置換後リストを一致させる． 2. 同一ID (LocalName)は，置換前リストと置換後リストを一致させる．
     * 3. １，２に一致しない場合には，NULLを対応させる．
     * NULLは，RDFSクラスの場合は，空クラス，RDFSプロパティの場合には，MR3#Nilに対応する
     * 
     * @param currentListModel
     * @param replaceListModel
     */
    private void fixListData(DefaultListModel currentListModel, DefaultListModel replaceListModel) {
        for (int i = 0; i < currentListModel.getSize(); i++) {
            Resource prevURI = ResourceFactory.createResource((String) currentListModel.getElementAt(i));
            if (i > replaceListModel.getSize()) {
                break;
            }
            boolean isHit = false;
            for (int j = i; j < replaceListModel.getSize(); j++) {
                Resource replaceURI = ResourceFactory.createResource((String) replaceListModel.getElementAt(j));
                if (prevURI.equals(replaceURI)) {
                    replaceListModel.removeElementAt(j);
                    replaceListModel.insertElementAt(replaceURI.getURI(), i);
                    isHit = true;
                    break;
                } else if (prevURI.getLocalName().equals(replaceURI.getLocalName())) {
                    replaceListModel.removeElementAt(j);
                    replaceListModel.insertElementAt(replaceURI.getURI(), i);
                    isHit = true;
                    // 完全に一致する場合があるかもしれないので，breakしない
                }
            }
            if (!isHit) {
                replaceListModel.insertElementAt(NULL, i);
            }
        }
    }

    public void valueChanged(ListSelectionEvent e) {
        if (e.getSource() == currentClassList) {
            replaceClassList.setSelectedIndex(currentClassList.getSelectedIndex());
        } else if (e.getSource() == replaceClassList) {
            currentClassList.setSelectedIndex(replaceClassList.getSelectedIndex());
        } else if (e.getSource() == currentPropertyList) {
            replacePropertyList.setSelectedIndex(currentPropertyList.getSelectedIndex());
        } else if (e.getSource() == replacePropertyList) {
            currentPropertyList.setSelectedIndex(replacePropertyList.getSelectedIndex());
        }
    }

    private void listUp(JList jList) {
        int selectedIndex = jList.getSelectedIndex();
        DefaultListModel listModel = (DefaultListModel) jList.getModel();
        if (0 < selectedIndex && selectedIndex < listModel.getSize()) {
            listUpDown(-1, selectedIndex, jList, listModel);
        }
    }

    private void listDown(JList jList) {
        int selectedIndex = jList.getSelectedIndex();
        DefaultListModel listModel = (DefaultListModel) jList.getModel();
        if (0 <= selectedIndex && selectedIndex < listModel.getSize() - 1) {
            listUpDown(+1, selectedIndex, jList, listModel);
        }
    }

    private void listUpDown(int num, int selectedIndex, JList jList, DefaultListModel listModel) {
        Object current = listModel.getElementAt(selectedIndex);
        Object prev = listModel.getElementAt(selectedIndex + num);
        listModel.setElementAt(current, selectedIndex + num);
        listModel.setElementAt(prev, selectedIndex);
        jList.setSelectedIndex(selectedIndex + num);
    }

    private Map getCurrentReplaceMap(ListModel currentListModel, ListModel replaceListModel) {
        Map currentReplaceMap = new HashMap();
        for (int i = 0; i < currentListModel.getSize(); i++) {
            Object prevObject = currentListModel.getElementAt(i);
            if (i < replaceListModel.getSize()) {
                currentReplaceMap.put(prevObject, replaceListModel.getElementAt(i));
            }
        }
        return currentReplaceMap;
    }

    private void replaceRDFResourceType(GraphCell cell, Map currentReplaceMap) {
        RDFResourceInfo resInfo = (RDFResourceInfo) GraphConstants.getValue(cell.getAttributes());
        // RDFSInfo rdfsInfo = (RDFSInfo)
        // GraphConstants.getValue(resInfo.getTypeCell().getAttributes());
        RDFSInfo rdfsInfo = resInfo.getTypeInfo();
        String res = (String) currentReplaceMap.get(rdfsInfo.getURIStr());
        if (res.equals(NULL)) {
            resInfo.setTypeCell(null);
        } else {
            Resource resource = ResourceFactory.createResource(res);
            resInfo.setTypeCell((GraphCell) gmanager.getClassCell(resource, false));
        }
    }

    private void replaceRDFProperty(GraphCell cell, Map currentReplaceMap) {
        RDFSInfo info = (RDFSInfo) GraphConstants.getValue(cell.getAttributes());
        String prop = (String) currentReplaceMap.get(info.getURIStr());
        if (prop.equals(NULL)) {
            GraphCell rdfsPropCell = gmanager.getPropertyCell(MR3Resource.Nil, false);
            RDFSInfo rdfsInfo = (RDFSInfo) GraphConstants.getValue(rdfsPropCell.getAttributes());
            GraphConstants.setValue(cell.getAttributes(), rdfsInfo);
        } else {
            GraphCell rdfsPropCell = gmanager.getPropertyCell(ResourceFactory.createResource(prop), false);
            RDFSInfo rdfsInfo = (RDFSInfo) GraphConstants.getValue(rdfsPropCell.getAttributes());
            GraphConstants.setValue(cell.getAttributes(), rdfsInfo);
        }
    }
    private void replaceClassList() {
        Map currentReplaceMap = getCurrentReplaceMap(currentClassListModel, replaceClassListModel);
        RDFGraph graph = gmanager.getRDFGraph();
        Object[] cells = graph.getAllCells();
        for (int i = 0; i < cells.length; i++) {
            Object cell = cells[i];
            if (RDFGraph.isRDFResourceCell(cell)) {
                replaceRDFResourceType((GraphCell) cell, currentReplaceMap);
            }
        }
    }

    private void replacePropertyList() {
        Map currentReplaceMap = getCurrentReplaceMap(currentPropertyListModel, replacePropertyListModel);
        RDFGraph graph = gmanager.getRDFGraph();
        Object[] cells = graph.getAllCells();
        for (int i = 0; i < cells.length; i++) {
            GraphCell cell = (GraphCell) cells[i];
            if (RDFGraph.isRDFPropertyCell(cell)) {
                replaceRDFProperty(cell, currentReplaceMap);
            }
        }
    }

    private boolean isContainsListModel(String uri, DefaultListModel currentListModel, DefaultListModel replaceListModel) {
        return currentListModel.contains(uri) && !replaceListModel.contains(uri);
    }

    private void removeCurrentClass() {
        RDFGraph graph = gmanager.getClassGraph();
        Object[] cells = graph.getAllCells();
        graph.clearSelection();
        for (int i = 0; i < cells.length; i++) {
            GraphCell cell = (GraphCell) cells[i];
            if (RDFGraph.isRDFSClassCell(cell)) {
                ClassInfo info = (ClassInfo) GraphConstants.getValue(cell.getAttributes());
                // ラベルとコメントを消すべきか？
                info.clearSubClass();
                info.clearSupClass();
                if (isContainsListModel(info.getURIStr(), currentClassListModel, replaceClassListModel)) {
                    graph.addSelectionCell(cell);
                }
            } else if (RDFGraph.isEdge(cell)) {
                graph.addSelectionCell(cell);
            }
        }
        graph.removeCellsWithEdges(graph.getAllSelectedCells());
    }

    private void removeCurrentProperty() {
        RDFGraph graph = gmanager.getPropertyGraph();
        Object[] cells = graph.getAllCells();
        graph.clearSelection();
        for (int i = 0; i < cells.length; i++) {
            GraphCell cell = (GraphCell) cells[i];
            if (RDFGraph.isRDFSPropertyCell(cell)) {
                PropertyInfo info = (PropertyInfo) GraphConstants.getValue(cell.getAttributes());
                info.clearSubProperty();
                info.clearSupProperty();
                info.clearDomain();
                info.clearRange();
                if (isContainsListModel(info.getURIStr(), currentPropertyListModel, replacePropertyListModel)) {
                    graph.addSelectionCell(cell);
                }
            } else if (RDFGraph.isEdge(cell)) {
                graph.addSelectionCell(cell);
            }
        }
        graph.removeCellsWithEdges(graph.getAllSelectedCells());
    }

    private void apply() {
        replaceClassList();
        replacePropertyList();
        removeCurrentClass();
        removeCurrentProperty();
        isApply = true;
        setVisible(false);
    }

    private void cancel() {
        isApply = false;
        setVisible(false);
    }

    public boolean isApply() {
        return isApply;
    }

    public void setVisible(boolean flag) {
        super.setVisible(flag);
        if (!flag) {
            rdfsInfoMap.clearTemporaryObject();
        }
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == replaceClassUpButton) {
            listUp(replaceClassList);
        } else if (e.getSource() == replaceClassDownButton) {
            listDown(replaceClassList);
        } else if (e.getSource() == replacePropertyUpButton) {
            listUp(replacePropertyList);
        } else if (e.getSource() == replacePropertyDownButton) {
            listDown(replacePropertyList);
        } else if (e.getSource() == applyButton) {
            apply();
        } else if (e.getSource() == cancelButton) {
            cancel();
        }
    }

}
