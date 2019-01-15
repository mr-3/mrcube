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

import org.mrcube.jgraph.GraphManager;
import org.mrcube.jgraph.OntClassCell;
import org.mrcube.jgraph.RDFGraph;
import org.mrcube.models.MR3Constants;
import org.mrcube.utils.GraphUtilities;
import org.mrcube.utils.Translator;
import org.mrcube.utils.Utilities;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * @author Takeshi Morita
 */
public class SelectResourceTypeDialog extends JDialog implements ActionListener {

    private boolean isConfirm;
    private JButton confirmButton;
    private final SelectResourceTypePanel panel;

    SelectResourceTypeDialog(GraphManager gm) {
        super(gm.getRootFrame(), Translator.getString("SelectResourceTypeDialog.Title"), true);
        panel = new SelectResourceTypePanel(gm);
        getContentPane().add(panel, BorderLayout.CENTER);
        getContentPane().add(getButtonPanel(), BorderLayout.SOUTH);
        setLocationRelativeTo(gm.getRootFrame());
        setSize(new Dimension(500, 450));
        setVisible(false);
    }

    private JComponent getButtonPanel() {
        confirmButton = new JButton(MR3Constants.OK);
        confirmButton.setMnemonic('o');
        confirmButton.addActionListener(this);
        JButton cancelButton = new JButton(MR3Constants.CANCEL);
        cancelButton.setMnemonic('c');
        cancelButton.addActionListener(this);
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(1, 2, 5, 5));
        buttonPanel.add(confirmButton);
        buttonPanel.add(cancelButton);
        return Utilities.createEastPanel(buttonPanel);
    }

    public Object getValue() {
        if (panel.getPrevCell() != null) {
            if (isConfirm) {
                isConfirm = false;
                GraphUtilities.changeDefaultCellStyle(panel.getGraph(), panel.getPrevCell(), OntClassCell.classColor);
                return panel.getURI();
            }
            GraphUtilities.changeDefaultCellStyle(panel.getGraph(), panel.getPrevCell(), OntClassCell.classColor);
            return null;
        }
        return null;
    }

    public void replaceGraph(RDFGraph graph) {
        panel.replaceGraph(graph);
    }

    public void setInitCell(Object typeCell) {
        panel.setInitCell(typeCell);
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == confirmButton) {
            isConfirm = true;
        } else {
            isConfirm = false;
        }
        setVisible(false);
    }
}
