/*
 * @(#) RemoveDialog.java
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
import java.util.*;

import javax.swing.*;
import javax.swing.event.*;

import org.semanticweb.mmm.mr3.data.*;
import org.semanticweb.mmm.mr3.jgraph.*;
import org.semanticweb.mmm.mr3.util.*;

import org.jgraph.graph.*;

/**
 * @author takeshi morita
 *
 */
public class RemoveDialog extends JInternalFrame implements ListSelectionListener, ActionListener {

	private JButton apply;
	private JButton cancel;

	private RDFGraph graph;
	private JList removeRDFSList;
	private ReferenceListPanel refListPanel;
	private GraphManager gmanager;

	private static final int LIST_WIDTH = 400;
	private static final int LIST_HEIGHT = 100;

	public RemoveDialog(GraphManager manager) {
		super(Translator.getString("RemoveDialog.Title"), true, false);
		gmanager = manager;

		removeRDFSList = new JList();
		removeRDFSList.addListSelectionListener(this);
		JScrollPane removeRDFSListScroll = new JScrollPane(removeRDFSList);
		Utilities.initComponent(removeRDFSListScroll, Translator.getString("RemoveDialog.Label.RemoveList"), LIST_WIDTH, LIST_HEIGHT);
		refListPanel = new ReferenceListPanel(gmanager);

		JPanel buttonPanel = new JPanel();
		apply = new JButton(MR3Constants.APPLY);
		apply.addActionListener(this);
		cancel = new JButton(MR3Constants.CANCEL);
		cancel.addActionListener(this);
		buttonPanel.add(apply);
		buttonPanel.add(cancel);

		Container contentPane = getContentPane();
		contentPane.add(removeRDFSListScroll, BorderLayout.NORTH);
		contentPane.add(refListPanel, BorderLayout.CENTER);
		contentPane.add(buttonPanel, BorderLayout.SOUTH);

		setLocation(100, 100);
		setSize(new Dimension(500, 350));
		setVisible(false);
	}

	public void setRefListInfo(RDFGraph graph, Set cells, Map classRDFMap, Map classPropMap) {
		this.graph = graph;
		removeRDFSList.setListData(cells.toArray());
		refListPanel.setTableModelMap(cells, classRDFMap, classPropMap);
		if (removeRDFSList.getModel().getSize() != 0) {
			removeRDFSList.setSelectedIndex(0);
		}
	}

	public void valueChanged(ListSelectionEvent e) {
		Object cell = removeRDFSList.getSelectedValue();
		refListPanel.replaceTableModel(cell);
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == apply) {
			ListModel listModel = removeRDFSList.getModel();
			for (int i = 0; i < listModel.getSize(); i++) {
				DefaultGraphCell removeRDFSCell = (DefaultGraphCell) listModel.getElementAt(i);
				refListPanel.removeAction(removeRDFSCell);
			}
			setVisible(false);
			gmanager.retryRemoveCells();
		} else if (e.getSource() == cancel) {
			setVisible(false);
		}
	}

	public void setVisible(boolean t) {
		if (gmanager != null) {
			gmanager.setEnabled(!t);
		}
		super.setVisible(t);
	}
}
