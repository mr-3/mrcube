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

import org.mrcube.jgraph.GraphManager;
import org.mrcube.models.MR3Constants;
import org.mrcube.utils.GraphUtilities;
import org.mrcube.utils.PrefixNSUtil;
import org.mrcube.utils.Utilities;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

/**
 * @author Takeshi Morita
 */
public class InsertRDFSResourceDialog extends JDialog {

    private final JComboBox uriPrefixBox;
    private final JTextField idField;
    private final JLabel nsLabel;
    private JButton confirmButton;
    private JButton cancelButton;

    private final ConfirmAction confirmAction;
    private final CancelAction cancelAction;

    private boolean isConfirm;
    private final GraphManager gmanager;

    private static final int FIELD_WIDTH = 300;
    private static final int FIELD_HEIGHT = 30;

    public InsertRDFSResourceDialog(GraphManager gm) {
        super(gm.getRootFrame(), true);
        gmanager = gm;
        confirmAction = new ConfirmAction();
        cancelAction = new CancelAction();

        idField = new JTextField();
        JComponent idFieldP = Utilities.createTitledPanel(idField, "ID");
        uriPrefixBox = new JComboBox();
        uriPrefixBox.addActionListener(new ChangePrefixAction());
        JComponent uriPrefixBoxP = Utilities.createTitledPanel(uriPrefixBox, MR3Constants.PREFIX);
        JPanel uriPanel = new JPanel();
        uriPanel.setLayout(new GridLayout(1, 2, 5, 5));
        uriPanel.add(uriPrefixBoxP);
        uriPanel.add(idFieldP);

        nsLabel = new JLabel("");
        JComponent nsLabelP = Utilities.createTitledPanel(nsLabel, MR3Constants.NAME_SPACE, FIELD_WIDTH, FIELD_HEIGHT);

        Component[] order = new Component[]{uriPrefixBox, idField, confirmButton, cancelButton};
        setFocusTraversalPolicy(Utilities.getMyFocusTraversalPolicy(order, 1));

        JPanel panel = new JPanel();
        setAction(panel);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.add(uriPanel);
        panel.add(nsLabelP);
        panel.add(getButtonPanel());
        getContentPane().add(panel);
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

    public void initData(String title) {
        setTitle(title);
        idField.setText("");
        PrefixNSUtil.setNamespaceModelSet(GraphUtilities.getNamespaceModelSet());
        uriPrefixBox.setModel(new DefaultComboBoxModel(PrefixNSUtil.getPrefixes().toArray()));
        uriPrefixBox.setSelectedItem(PrefixNSUtil.getBaseURIPrefix(gmanager.getBaseURI()));
        PrefixNSUtil.replacePrefix((String) uriPrefixBox.getSelectedItem(), nsLabel);
        pack();
        setLocationRelativeTo(gmanager.getRootFrame());
        setVisible(true);
    }

    class ChangePrefixAction extends AbstractAction {
        public void actionPerformed(ActionEvent e) {
            PrefixNSUtil.replacePrefix((String) uriPrefixBox.getSelectedItem(), nsLabel);
        }
    }

    public boolean isConfirm() {
        return isConfirm;
    }

    public String getURI() {
        return nsLabel.getText() + idField.getText();
    }

    class ConfirmAction extends AbstractAction {
        ConfirmAction() {
            super(MR3Constants.OK);
        }

        public void actionPerformed(ActionEvent e) {
            isConfirm = true;
            idField.requestFocus(); // ダイアログが表示されている時に，requestFocusしないといけない
            setVisible(false);
        }
    }

    class CancelAction extends AbstractAction {
        CancelAction() {
            super(MR3Constants.CANCEL);
        }

        public void actionPerformed(ActionEvent e) {
            isConfirm = false;
            idField.requestFocus(); // ダイアログが表示されている時に，requestFocusしないといけない
            setVisible(false);
        }
    }
}
