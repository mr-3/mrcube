/*
 * Project Name: MR^3 (Meta-Model Management based on RDFs Revision Reflection)
 * Project Website: http://mr3.sourceforge.net/
 * 
 * Copyright (C) 2003-2015 Yamaguchi Laboratory, Keio University. All rights reserved. 
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

package net.sourceforge.mr3.ui;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;

import net.sourceforge.mr3.data.*;
import net.sourceforge.mr3.data.MR3Constants.*;
import net.sourceforge.mr3.jgraph.*;
import net.sourceforge.mr3.util.*;

/**
 * @author Takeshi Morita
 */
public class OntManagementDialog extends JDialog implements ActionListener {

	private CreateRDFSType type;
	private JButton confirmButton;
	private JButton cancelButton;
	private JRadioButton renameButton;
	private JRadioButton createButton;
	private SelectRDFSPanel panel;
	private static final String TITLE = Translator.getString("RDFSManagementDialog.Title");

	public OntManagementDialog(GraphManager gm) {
		super(gm.getRootFrame(), TITLE, true);
		getContentPane().add(getChooseOnePanel(), BorderLayout.NORTH);
		panel = new SelectRDFSPanel(gm);
		getContentPane().add(panel, BorderLayout.CENTER);
		JPanel inlinePanel = new JPanel();
		initButton();
		inlinePanel.add(confirmButton);
		inlinePanel.add(cancelButton);
		getContentPane().add(inlinePanel, BorderLayout.SOUTH);

		setLocationRelativeTo(gm.getRootFrame());
		setSize(new Dimension(500, 550));
		setVisible(false);
	}

	private JComponent getChooseOnePanel() {
		JPanel chooseOnePanel = new JPanel();
		ButtonGroup group = new ButtonGroup();
		renameButton = new JRadioButton(Translator.getString("Rename"));
		renameButton.addActionListener(this);
		createButton = new JRadioButton(Translator.getString("Create"));
		createButton.addActionListener(this);
		createButton.setSelected(true);
		group.add(renameButton);
		group.add(createButton);
		chooseOnePanel.setLayout(new BoxLayout(chooseOnePanel, BoxLayout.X_AXIS));
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

	public void replaceGraph(RDFGraph graph) {
		panel.replaceGraph(graph);
	}

	public void setRegionSet(Set regionSet) {
		panel.setRegionSet(regionSet);
	}

	public Set getSupRDFSSet() {
		return panel.getRegionSet();
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
			setVisible(false);
		} else if (e.getSource() == cancelButton) {
			type = null;
			setVisible(false);
		} else {
			if (renameButton.isSelected()) {
				panel.setEnabled(false);
			} else if (createButton.isSelected()) {
				panel.setEnabled(true);
			}
		}
	}

}
