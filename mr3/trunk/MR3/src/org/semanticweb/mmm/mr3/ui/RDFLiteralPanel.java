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
import java.awt.event.*;
import java.util.*;

import javax.swing.*;

import org.jgraph.graph.*;
import org.semanticweb.mmm.mr3.data.*;
import org.semanticweb.mmm.mr3.data.MR3Constants.*;
import org.semanticweb.mmm.mr3.jgraph.*;
import org.semanticweb.mmm.mr3.util.*;

import com.hp.hpl.jena.datatypes.*;
import com.hp.hpl.jena.rdf.model.*;

/**
 * 
 * @author takeshi morita
 * 
 */
public class RDFLiteralPanel extends JPanel implements ActionListener {

    private JTextField langField;
    private JCheckBox isTypedLiteralBox;
    private JComboBox typeBox;
    private JTextArea literalValueArea;
    private JButton applyButton;
    private JButton cancelButton;
    private GraphCell cell;
    private GraphManager gmanager;
    private TypeMapper typeMapper;

    private static final int LABEL_WIDTH = 350;
    private static final int LABEL_HEIGHT = 100;

    public RDFLiteralPanel(GraphManager gm) {
        gmanager = gm;
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
        JComponent typeBoxP = Utilities.createTitledPanel(typeBox, Translator.getString("Type"), 300, 30);

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
        // mainPanel.setBorder(BorderFactory.createTitledBorder(Translator
        // .getString("AttributeDialog.RDFLiteralAttribute.Text")));
        mainPanel.add(langPanel);
        mainPanel.add(selectLitTypePanel);
        mainPanel.add(valueScroll);

        applyButton = new JButton(MR3Constants.APPLY);
        applyButton.setMnemonic('a');
        applyButton.addActionListener(this);
        cancelButton = new JButton(MR3Constants.CANCEL);
        cancelButton.setMnemonic('c');
        cancelButton.addActionListener(this);
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(1, 2, 5, 5));
        buttonPanel.add(applyButton);
        buttonPanel.add(cancelButton);

        setLayout(new BorderLayout());
        JPanel titlePanel = new JPanel();
        titlePanel.setBackground(new Color(49, 105, 198));
        ImageIcon icon = Utilities.getImageIcon(Translator.getString("RDFEditor.Icon"));
        JLabel titleLabel = new JLabel(Translator.getString("AttributeDialog.RDFLiteralAttribute.Text"), icon,
                SwingConstants.LEFT);
        titleLabel.setForeground(Color.white);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 13));
        titlePanel.setLayout(new BorderLayout());
        titlePanel.add(titleLabel, BorderLayout.WEST);

        add(titlePanel, BorderLayout.NORTH);
        add(mainPanel, BorderLayout.CENTER);
        add(Utilities.createEastPanel(buttonPanel), BorderLayout.SOUTH);
    }

    private void clearTextField() {
        langField.setText("");
        literalValueArea.setText("");
    }

    public void setValue(GraphCell c) {
        cell = c;
        clearTextField();
        try {
            MR3Literal literal = (MR3Literal) GraphConstants.getValue(cell.getAttributes());
            if (literal != null) {
                DefaultComboBoxModel model = new DefaultComboBoxModel();
                Set<String> sortedSet = new TreeSet<String>();
                for (Iterator i = typeMapper.listTypes(); i.hasNext();) {
                    sortedSet.add(((RDFDatatype) i.next()).getURI());
                }
                for (Iterator i = sortedSet.iterator(); i.hasNext();) {
                    model.addElement(i.next());
                }
                typeBox.setModel(model);
                if (literal.getDatatype() != null) {
                    langField.setEnabled(false);
                    setTypeLiteralEnable(true);
                    typeBox.setSelectedItem(literal.getDatatype().getURI());
                } else {
                    setTypeLiteralEnable(false);
                    langField.setEnabled(true);
                    langField.setText(literal.getLanguage());
                }
                literalValueArea.setText(literal.getString());
            }
        } catch (RDFException e) {
            e.printStackTrace();
        }
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                literalValueArea.requestFocus();
            }
        });
    }

    public void apply() {
        if (cell != null) {
            String dataType = null;
            if (isTypedLiteralBox.isSelected()) {
                dataType = (String) typeBox.getSelectedItem();
            }
            MR3Literal beforeLiteral = (MR3Literal) GraphConstants.getValue(cell.getAttributes());
            String str = literalValueArea.getText();
            MR3Literal literal = new MR3Literal(str, langField.getText(), typeMapper.getTypeByName(dataType));            
            GraphConstants.setValue(cell.getAttributes(), literal);            
            Dimension size = GraphUtilities.getAutoLiteralNodeDimention(gmanager, str);
            RDFGraph graph = gmanager.getCurrentRDFGraph();
            GraphUtilities.resizeCell(size, graph, cell);
            HistoryManager.saveHistory(HistoryType.EDIT_LITERAL_WITH_DIAGLOG, beforeLiteral, literal);
        }
    }

    private void setTypeLiteralEnable(boolean t) {
        isTypedLiteralBox.setSelected(t);
        typeBox.setEnabled(t);
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == isTypedLiteralBox) {
            langField.setEnabled(!isTypedLiteralBox.isSelected());
            typeBox.setEnabled(isTypedLiteralBox.isSelected());
        } else if (e.getSource() == applyButton) {
            apply();
        } else if (e.getSource() == cancelButton) {
            gmanager.setVisibleAttrDialog(false);
        }
    }
}
