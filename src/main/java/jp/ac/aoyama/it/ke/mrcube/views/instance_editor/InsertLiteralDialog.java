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

import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.datatypes.TypeMapper;
import jp.ac.aoyama.it.ke.mrcube.models.MR3Constants;
import jp.ac.aoyama.it.ke.mrcube.models.MR3Literal;
import jp.ac.aoyama.it.ke.mrcube.utils.Translator;
import jp.ac.aoyama.it.ke.mrcube.utils.Utilities;

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
public class InsertLiteralDialog extends JDialog implements ActionListener {

    private boolean isConfirm;
    private final Action confirmAction;
    private final Action cancelAction;

    private final JTextField langField;
    private final JCheckBox typedLiteralCheckBox;
    private final JComboBox<String> dataTypeComboBox;
    private final JTextArea literalValueArea;
    private final TypeMapper typeMapper;

    private static final int LABEL_WIDTH = 350;
    private static final int LABEL_HEIGHT = 90;

    public InsertLiteralDialog(Frame rootFrame) {
        super(rootFrame, Translator.getString("InsertLiteralDialog.Title"), true);
        confirmAction = new ConfirmAction();
        cancelAction = new CancelAction();
        typeMapper = TypeMapper.getInstance();

        langField = new JTextField(5);
        JComponent langFieldP = Utilities.createTitledPanel(langField, Translator.getString("LanguageTag"));
        JPanel langPanel = new JPanel();
        langPanel.setLayout(new BorderLayout());
        langPanel.add(langFieldP, BorderLayout.WEST);

        typedLiteralCheckBox = new JCheckBox(Translator.getString("DataType"));
        typedLiteralCheckBox.addActionListener(this);
        typedLiteralCheckBox.setSelected(false);
        dataTypeComboBox = new JComboBox<>();
        dataTypeComboBox.setEnabled(false);

        var literalDataTypePanel = new JPanel();
        literalDataTypePanel.add(typedLiteralCheckBox);
        literalDataTypePanel.add(dataTypeComboBox);

        literalValueArea = new JTextArea();
        literalValueArea.setLineWrap(true);
        JScrollPane valueScroll = new JScrollPane(literalValueArea);
        Utilities.initComponent(valueScroll, Translator.getString("Literal"), LABEL_WIDTH, LABEL_HEIGHT);

        var langTypePanel = new JPanel();
        langTypePanel.setLayout(new BoxLayout(langTypePanel, BoxLayout.Y_AXIS));
        langTypePanel.add(langPanel);
        langTypePanel.add(literalDataTypePanel);

        var mainPanel = new JPanel();
        mainPanel.setBorder(BorderFactory.createTitledBorder(Translator.getString("AttributeDialog.LiteralAttribute.Text")));
        mainPanel.setLayout(new BorderLayout());
        mainPanel.add(langTypePanel, BorderLayout.NORTH);
        mainPanel.add(valueScroll, BorderLayout.CENTER);

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

        Component[] order = new Component[]{langField, dataTypeComboBox, typedLiteralCheckBox, literalValueArea, confirmButton,
                cancelButton};
        setFocusTraversalPolicy(Utilities.getMyFocusTraversalPolicy(order, 3));

        int DIALOG_WIDTH = 550;
        int DIALOG_HEIGHT = 300;
        setSize(new Dimension(DIALOG_WIDTH, DIALOG_HEIGHT));
        setMinimumSize(new Dimension(DIALOG_WIDTH, DIALOG_HEIGHT));
        setResizable(true);
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
        dataTypeComboBox.setEnabled(false);
        typedLiteralCheckBox.setSelected(false);
        DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>();
        Set<String> sortedSet = new TreeSet<>();
        for (Iterator i = typeMapper.listTypes(); i.hasNext(); ) {
            sortedSet.add(((RDFDatatype) i.next()).getURI());
        }

        for (String dataType : sortedSet) {
            model.addElement(dataType);
        }
        dataTypeComboBox.setModel(model);
        setVisible(true);
    }

    public MR3Literal getLiteral() {
        if (typedLiteralCheckBox.isSelected()) {
            String dataType = dataTypeComboBox.getItemAt(dataTypeComboBox.getSelectedIndex());
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
        if (e.getSource() == typedLiteralCheckBox) {
            langField.setEnabled(!typedLiteralCheckBox.isSelected());
            dataTypeComboBox.setEnabled(typedLiteralCheckBox.isSelected());
        }
    }
}
