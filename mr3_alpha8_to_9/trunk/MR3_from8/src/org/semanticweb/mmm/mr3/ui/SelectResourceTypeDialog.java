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

	private boolean isOk;
	private JButton confirmButton;
	private JButton cancelButton;
	private SelectResourceTypePanel panel;

	SelectResourceTypeDialog(GraphManager gm) {
		super(gm.getRoot(), Translator.getString("SelectResourceTypeDialog.Title"), true);
		panel = new SelectResourceTypePanel(gm);
		getContentPane().add(panel, BorderLayout.CENTER);
		JPanel inlinePanel = new JPanel();
		initButton();
		inlinePanel.add(confirmButton);
		inlinePanel.add(cancelButton);
		getContentPane().add(inlinePanel, BorderLayout.SOUTH);
		setLocation(100, 100);
		setSize(new Dimension(500, 450));
		setVisible(false);
	}

	private void initButton() {
		confirmButton = new JButton(MR3Constants.OK);
		confirmButton.addActionListener(this);
		cancelButton = new JButton(MR3Constants.CANCEL);
		cancelButton.addActionListener(this);
	}

	public Object getValue() {
		if (panel.getPrevCell() != null) {
			if (isOk) {
				isOk = false;
				ChangeCellAttrUtil.changeDefaultCellStye(panel.getGraph(), panel.getPrevCell(), ChangeCellAttrUtil.classColor);
				return panel.getURI();
			} else {
				ChangeCellAttrUtil.changeDefaultCellStye(panel.getGraph(), panel.getPrevCell(), ChangeCellAttrUtil.classColor);
				return null;
			}
		} else {
			return null;
		}
	}

	public void replaceGraph(RDFGraph graph) {
		panel.replaceGraph(graph);
	}
	
	public void setInitCell(Object typeCell) {
		panel.setInitCell(typeCell);
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == confirmButton) {
			isOk = true;
		} else {
			isOk = false;
		}
		setVisible(false);
	}
}
