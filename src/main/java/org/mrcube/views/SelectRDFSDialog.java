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

package org.mrcube.views;

import org.mrcube.jgraph.GraphManager;
import org.mrcube.jgraph.RDFGraph;
import org.mrcube.models.MR3Constants;
import org.mrcube.utils.Utilities;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Set;

/**
 * @author Takeshi Morita
 */
public class SelectRDFSDialog extends JDialog implements ActionListener {

    private boolean isConfirm;
    private JButton confirmButton;
    private final SelectRDFSPanel panel;

    public SelectRDFSDialog(String title, GraphManager gm) {
        super(gm.getRootFrame(), title, true);
        panel = new SelectRDFSPanel(gm);
        getContentPane().add(panel, BorderLayout.CENTER);
        getContentPane().add(getButtonPanel(), BorderLayout.SOUTH);

        setLocationRelativeTo(gm.getRootFrame());
        setSize(new Dimension(500, 500));
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

    public void replaceGraph(RDFGraph graph) {
        panel.replaceGraph(graph);
    }

    public void setRegionSet(Set regionSet) {
        panel.setRegionSet(regionSet);
    }

    public Object getValue() {
        if (isConfirm) {
            isConfirm = false;
            return panel.getRegionSet();
        }
        return null;
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
