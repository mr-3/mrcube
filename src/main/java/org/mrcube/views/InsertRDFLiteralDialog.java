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

package org.mrcube.views;

import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.datatypes.TypeMapper;
import org.mrcube.models.MR3Constants;
import org.mrcube.models.MR3Literal;
import org.mrcube.utils.Translator;
import org.mrcube.utils.Utilities;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

/**
 * @author Takeshi Morita
 */
public class InsertRDFLiteralDialog extends JDialog implements ActionListener {

    private boolean isConfirm;
    private Action confirmAction;
    private Action cancelAction;

    private JTextField langField;
    private JCheckBox isTypedLiteralBox;
    private JComboBox typeBox;
    private JTextArea literalValueArea;
    private TypeMapper typeMapper;

    private static final int LABEL_WIDTH = 350;
    private static final int LABEL_HEIGHT = 90;

    public InsertRDFLiteralDialog(Frame rootFrame) {
        super(rootFrame, Translator.getString("InsertLiteralDialog.Title"), true);
        confirmAction = new ConfirmAction();
        cancelAction = new CancelAction();
        typeMapper = TypeMapper.getInstance();

        langField = new JTextField(5);
        JComponent langFieldP = Utilities.createTitledPanel(langField, Translator.getString("Lang"));
        JPanel langPanel = new JPanel();
        langPanel.setLayout(new BorderLayout());
        langPanel.add(langFieldP, BorderLayout.WEST);

        isTypedLiteralBox = new JCheckBox(Translator.getString("IsType"));
        isTypedLiteralBox.addActionListener(this);
        isTypedLiteralBox.setSelected(false);
        typeBox = new JComboBox();
        typeBox.setEnabled(false);
        JComponent typeBoxP = Utilities.createTitledPanel(typeBox, Translator.getString("Type"), 300, 20);

        JPanel selectLitTypePanel = new JPanel();
        selectLitTypePanel.setLayout(new BoxLayout(selectLitTypePanel, BoxLayout.X_AXIS));
        selectLitTypePanel.add(isTypedLiteralBox);
        selectLitTypePanel.add(typeBoxP);

        literalValueArea = new JTextArea();
        literalValueArea.setLineWrap(true);
        JScrollPane valueScroll = new JScrollPane(literalValueArea);
        Utilities.initComponent(valueScroll, Translator.getString("Literal"), LABEL_WIDTH, LABEL_HEIGHT);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createTitledBorder(Translator
                .getString("AttributeDialog.RDFLiteralAttribute.Text")));
        mainPanel.add(langPanel);
        mainPanel.add(selectLitTypePanel);
        mainPanel.add(valueScroll);

        JButton confirmButton = new JButton(confirmAction);
        confirmButton.setMnemonic('o');
        JButton cancelButton = new JButton(cancelAction);
        cancelButton.setMnemonic('c');
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(1, 2, 5, 5));
        buttonPanel.add(confirmButton);
        buttonPanel.add(cancelButton);

        setAction(buttonPanel);

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(mainPanel, BorderLayout.CENTER);
        getContentPane().add(Utilities.createEastPanel(buttonPanel), BorderLayout.SOUTH);

        Component[] order = new Component[]{langField, typeBox, isTypedLiteralBox, literalValueArea, confirmButton,
                cancelButton};
        setFocusTraversalPolicy(Utilities.getMyFocusTraversalPolicy(order, 3));

        pack();
        setLocationRelativeTo(rootFrame);
    }

    private void setAction(JComponent panel) {
        ActionMap actionMap = panel.getActionMap();
        actionMap.put(confirmAction.getValue(Action.NAME), confirmAction);
        actionMap.put(cancelAction.getValue(Action.NAME), cancelAction);
        InputMap inputMap = panel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        inputMap.put(KeyStroke.getKeyStroke("ENTER"), confirmAction.getValue(Action.NAME));
        inputMap.put(KeyStroke.getKeyStroke("ESCAPE"), cancelAction.getValue(Action.NAME));
    }

    public void initData() {
        langField.setEnabled(true);
        langField.setText("");
        literalValueArea.setText("");
        typeBox.setEnabled(false);
        isTypedLiteralBox.setSelected(false);
        DefaultComboBoxModel model = new DefaultComboBoxModel();
        Set<String> sortedSet = new TreeSet<>();
        for (Iterator i = typeMapper.listTypes(); i.hasNext();) {
            sortedSet.add(((RDFDatatype) i.next()).getURI());
        }

        for (String dataType : sortedSet) {
            model.addElement(dataType);
        }
        typeBox.setModel(model);
        setVisible(true);
    }

    public MR3Literal getLiteral() {
        if (isTypedLiteralBox.isSelected()) {
            String dataType = (String) typeBox.getSelectedItem();
            return new MR3Literal(literalValueArea.getText(), langField.getText(), typeMapper.getTypeByName(dataType));
        }
        return new MR3Literal(literalValueArea.getText(), langField.getText(), null);
    }

    class ConfirmAction extends AbstractAction {
        ConfirmAction() {
            super(MR3Constants.OK);
        }

        public void actionPerformed(ActionEvent e) {
            isConfirm = true;
            literalValueArea.requestFocus(); // ダイアログが表示されている時に，requestFocusしないといけない
            setVisible(false);
        }
    }

    class CancelAction extends AbstractAction {
        CancelAction() {
            super(MR3Constants.CANCEL);
        }

        public void actionPerformed(ActionEvent e) {
            isConfirm = false;
            literalValueArea.requestFocus(); // ダイアログが表示されている時に，requestFocusしないといけない
            setVisible(false);
        }
    }

    public boolean isConfirm() {
        return isConfirm;
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == isTypedLiteralBox) {
            langField.setEnabled(!isTypedLiteralBox.isSelected());
            typeBox.setEnabled(isTypedLiteralBox.isSelected());
        }
    }
}
