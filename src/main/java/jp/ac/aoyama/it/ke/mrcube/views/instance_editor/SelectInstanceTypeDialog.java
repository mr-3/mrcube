/*
 * Project Name: MR^3 (Meta-Model Management based on RDFs Revision Reflection)
 * Project Website: https://mr-3.github.io/
 *
 * Copyright (C) 2003-2025 Takeshi Morita. All rights reserved.
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

import jp.ac.aoyama.it.ke.mrcube.jgraph.GraphManager;
import jp.ac.aoyama.it.ke.mrcube.jgraph.OntClassCell;
import jp.ac.aoyama.it.ke.mrcube.jgraph.OntPropertyCell;
import jp.ac.aoyama.it.ke.mrcube.jgraph.RDFGraph;
import jp.ac.aoyama.it.ke.mrcube.models.MR3Constants;
import jp.ac.aoyama.it.ke.mrcube.utils.GraphUtilities;
import jp.ac.aoyama.it.ke.mrcube.utils.Translator;
import jp.ac.aoyama.it.ke.mrcube.utils.Utilities;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * @author Takeshi Morita
 */
public class SelectInstanceTypeDialog extends JDialog implements ActionListener {

    private boolean isConfirmed;
    private JButton confirmButton;
    private final SelectInstanceTypePanel panel;

    SelectInstanceTypeDialog(GraphManager gm) {
        super(gm.getRootFrame(), Translator.getString("SelectInstanceTypeDialog.Title"), true);
        setIconImage(Utilities.getImageIcon("class_node.png").getImage());
        panel = new SelectInstanceTypePanel(gm);
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
            if (isConfirmed) {
                isConfirmed = false;
                GraphUtilities.changeCellStyle(panel.getGraph(), panel.getPrevCell(), OntPropertyCell.foregroundColor, OntClassCell.backgroundColor, OntClassCell.borderColor);
                return panel.getURI();
            }
            GraphUtilities.changeCellStyle(panel.getGraph(), panel.getPrevCell(), OntClassCell.foregroundColor, OntClassCell.backgroundColor, OntClassCell.borderColor);
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
        isConfirmed = e.getSource() == confirmButton;
        setVisible(false);
    }
}
