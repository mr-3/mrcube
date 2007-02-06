/*
 * @(#) SelectResourceTypeDialog.java
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
import java.awt.event.*;

import javax.swing.*;

import org.semanticweb.mmm.mr3.data.*;
import org.semanticweb.mmm.mr3.jgraph.*;
import org.semanticweb.mmm.mr3.util.*;

/**
 * @author takeshi morita
 */
public class SelectResourceTypeDialog extends JDialog implements ActionListener {

    private boolean isConfirm;
    private JButton confirmButton;
    private JButton cancelButton;
    private SelectResourceTypePanel panel;

    SelectResourceTypeDialog(GraphManager gm) {
        super(gm.getRootFrame(), Translator.getString("SelectResourceTypeDialog.Title"), true);
        panel = new SelectResourceTypePanel(gm);
        getContentPane().add(panel, BorderLayout.CENTER);
        getContentPane().add(getButtonPanel(), BorderLayout.SOUTH);
        setLocation(100, 100);
        setSize(new Dimension(500, 450));
        setVisible(false);
    }

    private JComponent getButtonPanel() {
        confirmButton = new JButton(MR3Constants.OK);
        confirmButton.setMnemonic('o');
        confirmButton.addActionListener(this);
        cancelButton = new JButton(MR3Constants.CANCEL);
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
