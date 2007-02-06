/*
 * @(#) FindResourceDialog.java
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

import org.semanticweb.mmm.mr3.data.*;
import org.semanticweb.mmm.mr3.data.MR3Constants.*;
import org.semanticweb.mmm.mr3.jgraph.*;
import org.semanticweb.mmm.mr3.util.*;

import com.hp.hpl.jena.rdf.model.*;

/**
 * @author takeshi morita
 * 
 */
public class FindResourceDialog extends JDialog {

    private JTextField findField;
    private Set<PrefixNSInfo> prefixNSInfoSet;
    private JComboBox uriPrefixBox;
    private JList resourceList;

    private JCheckBox rdfCheckBox;
    private JCheckBox classCheckBox;
    private JCheckBox propertyCheckBox;

    private JButton cancelButton;

    private GraphManager gmanager;
    private static Object[] NULL = new Object[0];

    private static final int BOX_WIDTH = 100;
    private static final int BOX_HEIGHT = 20;
    private static final int LIST_WIDTH = 300;
    private static final int LIST_HEIGHT = 120;
    private static final int FIELD_HEIGHT = 20;

    public FindResourceDialog(GraphManager gm) {
        super(gm.getRootFrame(), Translator.getString("FindResourceDialog.Title"), false);
        // setFrameIcon(Utilities.getImageIcon(Translator.getString("FindResourceDialog.Icon")));
        Container contentPane = getContentPane();

        gmanager = gm;
        JComponent buttonGroupPanel = getButtonGroupPanel();
        JComponent findAreaPanel = getFindAreaPanel();
        JPanel northPanel = new JPanel();
        northPanel.setLayout(new BoxLayout(northPanel, BoxLayout.Y_AXIS));
        northPanel.add(buttonGroupPanel);
        northPanel.add(findAreaPanel);

        resourceList = new JList();
        resourceList.addListSelectionListener(new JumpAction());
        JComponent resourceListP = Utilities.createTitledPanel(new JScrollPane(resourceList), Translator
                .getString("FindResult"), LIST_WIDTH, LIST_HEIGHT);

        cancelButton = new JButton(MR3Constants.CANCEL);
        cancelButton.setMnemonic('c');
        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
            }
        });
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BorderLayout());
        buttonPanel.add(cancelButton, BorderLayout.EAST);

        contentPane.setLayout(new BorderLayout());
        contentPane.add(northPanel, BorderLayout.NORTH);
        contentPane.add(resourceListP, BorderLayout.CENTER);
        contentPane.add(buttonPanel, BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(gmanager.getRootFrame());
        setVisible(false);
    }

    private JComponent getButtonGroupPanel() {
        FindAreaCheck findAreaCheck = new FindAreaCheck();
        rdfCheckBox = new JCheckBox("RDF");
        rdfCheckBox.setSelected(true);
        rdfCheckBox.addItemListener(findAreaCheck);
        classCheckBox = new JCheckBox(Translator.getString("Class"));
        classCheckBox.addItemListener(findAreaCheck);
        propertyCheckBox = new JCheckBox(Translator.getString("Property"));
        propertyCheckBox.addItemListener(findAreaCheck);

        JPanel buttonGroupPanel = new JPanel();
        buttonGroupPanel.setLayout(new GridLayout(1, 3));
        buttonGroupPanel.setBorder(BorderFactory.createTitledBorder(Translator.getString("GraphType")));
        buttonGroupPanel.add(rdfCheckBox);
        buttonGroupPanel.add(classCheckBox);
        buttonGroupPanel.add(propertyCheckBox);

        return Utilities.createWestPanel(buttonGroupPanel);
    }

    private JComponent getFindAreaPanel() {
        uriPrefixBox = new JComboBox();
        uriPrefixBox.addActionListener(new ChangePrefixAction());
        JComponent uriPrefixBoxP = Utilities
                .createTitledPanel(uriPrefixBox, MR3Constants.PREFIX, BOX_WIDTH, BOX_HEIGHT);

        findField = new JTextField();
        findField.getDocument().addDocumentListener(new FindAction());
        JComponent findFieldP = Utilities.createTitledPanel(findField, "URI", LIST_WIDTH, FIELD_HEIGHT);

        JPanel inlinePanel = new JPanel();
        inlinePanel.setLayout(new BoxLayout(inlinePanel, BoxLayout.X_AXIS));
        inlinePanel.add(uriPrefixBoxP);
        inlinePanel.add(findFieldP);
        return inlinePanel;
    }

    public void setAllCheckBoxSelected(boolean t) {
        rdfCheckBox.setSelected(t);
        classCheckBox.setSelected(t);
        propertyCheckBox.setSelected(t);
    }

    public void setFindArea(GraphType type) {
        findField.setText("");
        setAllCheckBoxSelected(false);

        if (type == null) {
            setAllCheckBoxSelected(true);
        } else if (type == GraphType.RDF) {
            rdfCheckBox.setSelected(true);
        } else if (type == GraphType.CLASS) {
            classCheckBox.setSelected(true);
        } else if (type == GraphType.PROPERTY) {
            propertyCheckBox.setSelected(true);
        }
    }

    public void setURIPrefixBox() {
        prefixNSInfoSet = GraphUtilities.getPrefixNSInfoSet();
        PrefixNSUtil.setPrefixNSInfoSet(prefixNSInfoSet);
        uriPrefixBox.setModel(new DefaultComboBoxModel(PrefixNSUtil.getPrefixes().toArray()));
        // findField.setText(PrefixNSUtil.getNameSpace((String)
        // uriPrefixBox.getSelectedItem()));
    }

    public void setVisible(boolean aFlag) {
        if (aFlag) {
            setURIPrefixBox();
        }
        super.setVisible(aFlag);
    }

    class ChangePrefixAction extends AbstractAction {
        public void actionPerformed(ActionEvent e) {
            if (e.getSource() == uriPrefixBox) {
                String ns = PrefixNSUtil.getNameSpace((String) uriPrefixBox.getSelectedItem());
                String id = ResourceFactory.createResource(findField.getText()).getLocalName();
                findField.setText(ns + id);
                setFindList();
            }
        }
    }

    class FindAreaCheck implements ItemListener {
        public void itemStateChanged(ItemEvent e) {
            setFindList();
        }
    }

    public Object[] getFindResources(String key) {
        Map<String, Object> resourceMap = new TreeMap<String, Object>();
        key = resolvePrefix(key);
        if (rdfCheckBox.isSelected()) {
            Set set = gmanager.getFindRDFResult(key);
            for (Iterator i = set.iterator(); i.hasNext();) {
                Object cell = i.next();
                resourceMap.put(cell.toString(), cell);
            }
        }
        if (classCheckBox.isSelected()) {
            Set set = gmanager.getFindClassResult(key);
            for (Iterator i = set.iterator(); i.hasNext();) {
                Object cell = i.next();
                resourceMap.put(cell.toString(), cell);
            }
        }
        if (propertyCheckBox.isSelected()) {
            Set set = gmanager.getFindPropertyResult(key);
            for (Iterator i = set.iterator(); i.hasNext();) {
                Object cell = i.next();
                resourceMap.put(cell.toString(), cell);
            }
        }

        return resourceMap.values().toArray();
    }

    private String resolvePrefix(String key) {
        String[] tokens = key.split(":");
        if (tokens.length != 2) { return key; }
        String prefix = tokens[0];
        String id = tokens[1];
        if (prefix.equals("http")) { return key; }
        // for (Iterator i = prefixNSInfoSet.iterator(); i.hasNext();) {
        // PrefixNSInfo info = (PrefixNSInfo) i.next();
        for (PrefixNSInfo info : prefixNSInfoSet) {
            if (info.getPrefix().equals(prefix)) { return info.getNameSpace() + id; }
        }
        return key;
    }

    private void setFindList() {
        resourceList.removeAll();
        if (findField.getText().length() == 0) {
            resourceList.setListData(NULL);
            return;
        }
        String key = findField.getText() + ".*";
        Object[] findList = getFindResources(key);
        resourceList.setListData(findList);
        if (0 < resourceList.getModel().getSize()) {
            resourceList.setSelectedIndex(0);
        }
    }

    class FindAction implements DocumentListener {

        public void changedUpdate(DocumentEvent e) {
            setFindList();
        }

        public void insertUpdate(DocumentEvent e) {
            setFindList();
        }

        public void removeUpdate(DocumentEvent e) {
            setFindList();
        }
    }

    class JumpAction implements ListSelectionListener {
        public void valueChanged(ListSelectionEvent e) {
            Object cell = resourceList.getSelectedValue();
            // Object[] cells = resourceList.getSelectedValues();
            if (rdfCheckBox.isSelected()) {
                gmanager.selectRDFCell(cell);
            }
            if (classCheckBox.isSelected()) {
                gmanager.selectClassCell(cell);
            }
            if (propertyCheckBox.isSelected()) {
                gmanager.selectPropertyCell(cell);
            }
        }
    }
}