/*
 * Project Name: MR^3 (Meta-Model Management based on RDFs Revision Reflection)
 * Project Website: https://mr-3.github.io/
 *
 * Copyright (C) 2003-2024 Takeshi Morita. All rights reserved.
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

package jp.ac.aoyama.it.ke.mrcube.views.instance_editor;

import org.apache.jena.rdf.model.ResourceFactory;
import jp.ac.aoyama.it.ke.mrcube.jgraph.GraphManager;
import jp.ac.aoyama.it.ke.mrcube.models.MR3Constants;
import jp.ac.aoyama.it.ke.mrcube.utils.GraphUtilities;
import jp.ac.aoyama.it.ke.mrcube.utils.PrefixNSUtil;
import jp.ac.aoyama.it.ke.mrcube.utils.Translator;
import jp.ac.aoyama.it.ke.mrcube.utils.Utilities;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

/**
 * @author Takeshi Morita
 */
public class InsertInstanceDialog extends JDialog implements ItemListener {

    private boolean isConfirm;
    private final JComboBox resTypeBox;
    private final JTextField uriField;
    private final JComboBox uriPrefixBox;
    private JButton confirmButton;
    private JButton cancelButton;
    private Object resourceType;
    private final JCheckBox isAnonBox;

    private final ConfirmAction confirmAction;
    private final CancelAction cancelAction;
    private final GraphManager gmanager;

    private static final int FIELD_WIDTH = 300;
    private static final int FIELD_HEIGHT = 30;

    public InsertInstanceDialog(GraphManager gm) {
        super(gm.getRootFrame(), Translator.getString("InsertInstanceDialog.Title"), true);
        gmanager = gm;
        confirmAction = new ConfirmAction();
        cancelAction = new CancelAction();
        Container contentPane = getContentPane();

        resTypeBox = new JComboBox();
        resTypeBox.addItemListener(this);
        JPanel resTypeBoxP = new JPanel();
        resTypeBoxP.setLayout(new BorderLayout());
        resTypeBoxP.add(resTypeBox, BorderLayout.CENTER);
        resTypeBoxP.setBorder(BorderFactory.createTitledBorder(Translator.getString("InstanceType")));

        uriField = new JTextField();
        JComponent uriFieldP = Utilities.createTitledPanel(uriField, Translator.getString("Instance"), FIELD_WIDTH, FIELD_HEIGHT);

        isAnonBox = new JCheckBox(Translator.getString("BlankNode"));
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

        Component[] order = new Component[]{resTypeBox, uriPrefixBox, isAnonBox, uriField, confirmButton,
                cancelButton};
        setFocusTraversalPolicy(Utilities.getMyFocusTraversalPolicy(order, 3));

        contentPane.add(panel);
        int DIALOG_WIDTH = 400;
        int DIALOG_HEIGHT = 200;
        setSize(new Dimension(DIALOG_WIDTH, DIALOG_HEIGHT));
        setMinimumSize(new Dimension(DIALOG_WIDTH, DIALOG_HEIGHT));
        setResizable(true);
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
        PrefixNSUtil.setNamespaceModelSet(GraphUtilities.getNamespaceModelSet());
        uriPrefixBox.setModel(new DefaultComboBoxModel(PrefixNSUtil.getPrefixes().toArray()));
        resTypeBox.setModel(new DefaultComboBoxModel(cells));
        Object[] typeCells = gmanager.getClassGraph().getSelectionCells();
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
            uriField.requestFocus(); // ダイアログが表示されている時に，requestFocusしないといけない
            setVisible(false);
        }
    }

    class CancelAction extends AbstractAction {
        CancelAction() {
            super(MR3Constants.CANCEL);
        }

        public void actionPerformed(ActionEvent e) {
            isConfirm = false;
            uriField.requestFocus(); // ダイアログが表示されている時に，requestFocusしないといけない
            setVisible(false);
        }
    }
}
