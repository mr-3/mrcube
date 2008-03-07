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
import java.awt.Container;
import java.awt.event.*;

import javax.swing.*;

import org.semanticweb.mmm.mr3.data.*;
import org.semanticweb.mmm.mr3.jgraph.*;
import org.semanticweb.mmm.mr3.util.*;

import com.hp.hpl.jena.rdf.model.*;

/**
 * @author takeshi morita
 */
public class InsertRDFResDialog extends JDialog implements ItemListener {

    private boolean isConfirm;
    private JComboBox resTypeBox;
    private JTextField uriField;
    private JComboBox uriPrefixBox;
    private JButton confirmButton;
    private JButton cancelButton;
    private Object resourceType;
    private JCheckBox isAnonBox;

    private ConfirmAction confirmAction;
    private CancelAction cancelAction;
    private GraphManager gmanager;

    private static final int FIELD_WIDTH = 300;
    private static final int FIELD_HEIGHT = 20;

    public InsertRDFResDialog(GraphManager gm) {
        super(gm.getRootFrame(), Translator.getString("InsertResourceDialog.Title"), true);
        gmanager = gm;
        confirmAction = new ConfirmAction();
        cancelAction = new CancelAction();
        Container contentPane = getContentPane();

        resTypeBox = new JComboBox();
        resTypeBox.addItemListener(this);
        JPanel resTypeBoxP = new JPanel();
        resTypeBoxP.setLayout(new BorderLayout());
        resTypeBoxP.add(resTypeBox, BorderLayout.CENTER);
        resTypeBoxP.setBorder(BorderFactory.createTitledBorder(Translator.getString("ResourceType")));

        uriField = new JTextField();
        JComponent uriFieldP = Utilities.createTitledPanel(uriField, Translator.getString("RDFResource"), FIELD_WIDTH,
                FIELD_HEIGHT);

        isAnonBox = new JCheckBox(Translator.getString("IsBlank"));
        isAnonBox.addActionListener(new IsAnonAction());

        uriPrefixBox = new JComboBox();
        uriPrefixBox.addActionListener(new ChangePrefixAction());
        JComponent uriPrefixBoxP = Utilities.createTitledPanel(uriPrefixBox, MR3Constants.PREFIX);
        JPanel uriPanel = new JPanel();
        uriPanel.setLayout(new GridLayout(1, 2));
        uriPanel.add(uriPrefixBoxP);
        uriPanel.add(isAnonBox);

        JPanel panel = new JPanel();
        setAction(panel);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.add(resTypeBoxP);
        panel.add(uriPanel);
        panel.add(uriFieldP);
        panel.add(getButtonPanel());

        Component order[] = new Component[] { resTypeBox, uriPrefixBox, isAnonBox, uriField, confirmButton,
                cancelButton};
        setFocusTraversalPolicy(Utilities.getMyFocusTraversalPolicy(order, 3));

        contentPane.add(panel);
        pack();
        setResizable(false);
        setVisible(false);
    }

    private void setAction(JComponent panel) {
        ActionMap actionMap = panel.getActionMap();
        actionMap.put(confirmAction.getValue(Action.NAME), confirmAction);
        actionMap.put(cancelAction.getValue(Action.NAME), cancelAction);
        InputMap inputMap = panel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        inputMap.put(KeyStroke.getKeyStroke("ENTER"), confirmAction.getValue(Action.NAME));
        inputMap.put(KeyStroke.getKeyStroke("ESCAPE"), cancelAction.getValue(Action.NAME));
    }

    private JComponent getButtonPanel() {
        confirmButton = new JButton(confirmAction);
        confirmButton.setMnemonic('o');
        cancelButton = new JButton(cancelAction);
        cancelButton.setMnemonic('c');
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(1, 2, 5, 5));
        buttonPanel.add(confirmButton);
        buttonPanel.add(cancelButton);
        return Utilities.createEastPanel(buttonPanel);
    }

    public void initData(Object[] cells) {
        resourceType = null;
        uriField.setText("");
        PrefixNSUtil.setPrefixNSInfoSet(GraphUtilities.getPrefixNSInfoSet());
        uriPrefixBox.setModel(new DefaultComboBoxModel(PrefixNSUtil.getPrefixes().toArray()));
        resTypeBox.setModel(new DefaultComboBoxModel(cells));
        Object[] typeCells = gmanager.getCurrentClassGraph().getSelectionCells();
        if (typeCells.length == 1) {
            resTypeBox.setSelectedItem(typeCells[0]);
        }
        uriPrefixBox.setSelectedItem(PrefixNSUtil.getBaseURIPrefix(gmanager.getBaseURI()));
        setLocationRelativeTo(gmanager.getRootFrame());
        setVisible(true);
    }

    class ChangePrefixAction extends AbstractAction {
        public void actionPerformed(ActionEvent e) {
            String ns = PrefixNSUtil.getNameSpace((String) uriPrefixBox.getSelectedItem());
            String id = ResourceFactory.createResource(uriField.getText()).getLocalName();
            uriField.setText(ns + id);
        }
    }

    class IsAnonAction extends AbstractAction {
        public void actionPerformed(ActionEvent e) {
            setIDField("", !isAnonBox.isSelected());
            uriField.setEnabled(!isAnonBox.isSelected());
            uriPrefixBox.setEnabled(!isAnonBox.isSelected());
        }
    }

    public boolean isConfirm() {
        return isConfirm;
    }

    public boolean isAnonymous() {
        return isAnonBox.isSelected();
    }

    public Object getResourceType() {
        return resourceType;
    }

    public String getURI() {
        return uriField.getText();
    }

    public void itemStateChanged(ItemEvent e) {
        resourceType = resTypeBox.getSelectedItem();
    }

    private void setIDField(String str, boolean t) {
        uriField.setText(str);
        uriField.setToolTipText(str);
        uriField.setEditable(t);
    }

    class ConfirmAction extends AbstractAction {
        ConfirmAction() {
            super(MR3Constants.OK);
        }

        public void actionPerformed(ActionEvent e) {
            isConfirm = true;
            uriField.requestFocus(); // �_�C�A���O���\������Ă��鎞�ɁCrequestFocus���Ȃ��Ƃ����Ȃ�
            setVisible(false);
        }
    }

    class CancelAction extends AbstractAction {
        CancelAction() {
            super(MR3Constants.CANCEL);
        }

        public void actionPerformed(ActionEvent e) {
            isConfirm = false;
            uriField.requestFocus(); // �_�C�A���O���\������Ă��鎞�ɁCrequestFocus���Ȃ��Ƃ����Ȃ�
            setVisible(false);
        }
    }
}