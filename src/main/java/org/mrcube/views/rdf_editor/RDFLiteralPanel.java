/*
 * Project Name: MR^3 (Meta-Model Management based on RDFs Revision Reflection)
 * Project Website: http://mrcube.org/
 *
 * Copyright (C) 2003-2020 Takeshi Morita. All rights reserved.
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

package org.mrcube.views.rdf_editor;

import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.datatypes.TypeMapper;
import org.jgraph.graph.GraphCell;
import org.jgraph.graph.GraphConstants;
import org.mrcube.jgraph.GraphManager;
import org.mrcube.jgraph.RDFGraph;
import org.mrcube.models.MR3Constants;
import org.mrcube.models.MR3Constants.HistoryType;
import org.mrcube.models.MR3Literal;
import org.mrcube.utils.GraphUtilities;
import org.mrcube.utils.Translator;
import org.mrcube.utils.Utilities;
import org.mrcube.views.HistoryManager;

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
public class RDFLiteralPanel extends JPanel implements ActionListener {

    private final JTextField langField;
    private final JCheckBox typedLiteralCheckBox;
    private final JComboBox dataTypeComboBox;
    private final JTextArea literalValueArea;
    private final JButton applyButton;
    private final JButton cancelButton;
    private GraphCell cell;
    private final GraphManager gmanager;
    private final TypeMapper typeMapper;

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

        typedLiteralCheckBox = new JCheckBox(Translator.getString("DataType"));
        typedLiteralCheckBox.addActionListener(this);
        typedLiteralCheckBox.setSelected(false);
        dataTypeComboBox = new JComboBox();
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
        mainPanel.setLayout(new BorderLayout());
        mainPanel.add(langTypePanel, BorderLayout.NORTH);
        mainPanel.add(valueScroll, BorderLayout.CENTER);

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
        titlePanel.setBackground(MR3Constants.TITLE_BACKGROUND_COLOR);
        ImageIcon icon = Utilities.getImageIcon(Translator.getString("RDFEditor.Icon"));
        JLabel titleLabel = new JLabel(Translator.getString("AttributeDialog.RDFLiteralAttribute.Text"), icon, SwingConstants.LEFT);
        titleLabel.setForeground(Color.white);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, MR3Constants.TITLE_FONT_SIZE));
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

        MR3Literal literal = (MR3Literal) GraphConstants.getValue(cell.getAttributes());
        if (literal != null) {
            DefaultComboBoxModel model = new DefaultComboBoxModel();
            Set<String> sortedSet = new TreeSet<>();
            for (Iterator i = typeMapper.listTypes(); i.hasNext(); ) {
                sortedSet.add(((RDFDatatype) i.next()).getURI());
            }
            for (String s : sortedSet) {
                model.addElement(s);
            }
            dataTypeComboBox.setModel(model);
            if (literal.getDatatype() != null) {
                langField.setEnabled(false);
                setTypeLiteralEnable(true);
                dataTypeComboBox.setSelectedItem(literal.getDatatype().getURI());
            } else {
                setTypeLiteralEnable(false);
                langField.setEnabled(true);
                langField.setText(literal.getLanguage());
            }
            literalValueArea.setText(literal.getString());
        }

        SwingUtilities.invokeLater(() -> literalValueArea.requestFocus());
    }

    private void apply() {
        if (cell != null) {
            String dataType = null;
            if (typedLiteralCheckBox.isSelected()) {
                dataType = (String) dataTypeComboBox.getSelectedItem();
            }
            MR3Literal beforeLiteral = (MR3Literal) GraphConstants.getValue(cell.getAttributes());
            String str = literalValueArea.getText();
            MR3Literal literal = new MR3Literal(str, langField.getText(),
                    typeMapper.getTypeByName(dataType));
            GraphConstants.setValue(cell.getAttributes(), literal);
            Dimension size = GraphUtilities.getAutoLiteralNodeDimention(gmanager, str);
            RDFGraph graph = gmanager.getRDFGraph();
            GraphUtilities.resizeCell(size, graph, cell);
            HistoryManager.saveHistory(HistoryType.EDIT_LITERAL_WITH_DIAGLOG, beforeLiteral,
                    literal);
        }
    }

    private void setTypeLiteralEnable(boolean t) {
        typedLiteralCheckBox.setSelected(t);
        dataTypeComboBox.setEnabled(t);
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == typedLiteralCheckBox) {
            langField.setEnabled(!typedLiteralCheckBox.isSelected());
            dataTypeComboBox.setEnabled(typedLiteralCheckBox.isSelected());
        } else if (e.getSource() == applyButton) {
            apply();
        } else if (e.getSource() == cancelButton) {
            gmanager.setVisibleAttrDialog(false);
        }
    }
}
