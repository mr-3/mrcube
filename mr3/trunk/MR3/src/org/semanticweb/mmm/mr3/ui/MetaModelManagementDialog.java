/*
 * @(#)  2005/06/23
 *
 *
 * Copyright (C) 2003 The MMM Project
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 */

package org.semanticweb.mmm.mr3.ui;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;

import org.semanticweb.mmm.mr3.data.*;
import org.semanticweb.mmm.mr3.data.MR3Constants.*;
import org.semanticweb.mmm.mr3.jgraph.*;
import org.semanticweb.mmm.mr3.util.*;

/**
 * @author takeshi morita
 */
public class MetaModelManagementDialog extends JDialog implements ActionListener {
    private CreateRDFSType type;
    private JButton confirmButton;
    private JButton cancelButton;
    private JRadioButton renameButton;
    private JRadioButton createButton;
    private static final String TITLE = Translator.getString("RDFSManagementDialog.Title");

    public MetaModelManagementDialog(GraphManager gm) {
        super(gm.getRootFrame(), TITLE, true);
        getContentPane().add(getChooseOnePanel(), BorderLayout.CENTER);
        JPanel inlinePanel = new JPanel();
        initButton();
        inlinePanel.add(confirmButton);
        inlinePanel.add(cancelButton);
        getContentPane().add(inlinePanel, BorderLayout.SOUTH);

        setLocationRelativeTo(gm.getRootFrame());
        setSize(new Dimension(200, 150));
        setVisible(false);
    }

    private JComponent getChooseOnePanel() {
        JPanel chooseOnePanel = new JPanel();
        ButtonGroup group = new ButtonGroup();
        renameButton = new JRadioButton(Translator.getString("Rename"));
        renameButton.setSelected(true);
        createButton = new JRadioButton(Translator.getString("Create"));
        group.add(renameButton);
        group.add(createButton);
        chooseOnePanel.setLayout(new GridLayout(1, 2));
        chooseOnePanel.add(renameButton);
        chooseOnePanel.add(createButton);
        chooseOnePanel.setBorder(BorderFactory.createTitledBorder(Translator.getString("ChooseOneSelect")));
        return chooseOnePanel;
    }

    private void initButton() {
        confirmButton = new JButton(MR3Constants.OK);
        confirmButton.setMnemonic('o');
        confirmButton.addActionListener(this);
        cancelButton = new JButton(MR3Constants.CANCEL);
        cancelButton.setMnemonic('c');
        cancelButton.addActionListener(this);
    }

    public CreateRDFSType getType() {
        return type;
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == confirmButton) {
            if (renameButton.isSelected()) {
                type = CreateRDFSType.RENAME;
            } else {
                type = CreateRDFSType.CREATE;
            }
        } else if (e.getSource() == cancelButton) {
            type = null;
        }
        setVisible(false);
    }
}
