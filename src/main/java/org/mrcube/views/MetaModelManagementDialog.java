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
import org.mrcube.models.MR3Constants;
import org.mrcube.models.MR3Constants.CreateRDFSType;
import org.mrcube.utils.Translator;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * @author Takeshi Morita
 */
public class MetaModelManagementDialog extends JDialog implements ActionListener {
	private CreateRDFSType type;
	private JButton confirmButton;
	private JButton cancelButton;
	private JRadioButton renameButton;
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
        JRadioButton createButton = new JRadioButton(Translator.getString("Create"));
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

	public CreateRDFSType getCreateRDFSType() {
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
