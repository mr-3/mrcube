/*
 * @(#) GroupAction.java
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

package org.semanticweb.mmm.mr3.actions;

import java.awt.event.*;

import javax.swing.*;

import org.jgraph.graph.*;
import org.semanticweb.mmm.mr3.jgraph.*;
import org.semanticweb.mmm.mr3.util.*;

/**
 * @author takeshi morita
 *
 */
public class GroupAction extends AbstractAction {

	private RDFGraph graph;
	private static final String TITLE = Translator.getString("Action.Group.Text");
	private static final ImageIcon ICON = Utilities.getImageIcon(Translator.getString("Action.Group.Icon"));

	public GroupAction(RDFGraph g) {
		super(TITLE, ICON);
		graph = g;
		putValue(SHORT_DESCRIPTION, TITLE);
		putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_G, KeyEvent.CTRL_MASK));
	}

	// Returns the total number of cells in a graph
	private int getCellCount(RDFGraph graph) {
		Object[] cells = graph.getAllCells();
		return cells.length;
	}

	// Create a Group that Contains the Cells
	public void actionPerformed(ActionEvent e) {
		Object[] cells = graph.getSelectionCells();
		cells = graph.getGraphLayoutCache().order(cells);

		if (cells != null && cells.length > 0) {
			int count = getCellCount(graph);
			DefaultGraphCell group = new DefaultGraphCell(new Integer(count - 1));
			ParentMap map = new ParentMap();
			for (int i = 0; i < cells.length; i++) {
				map.addEntry(cells[i], group);
			}
			graph.getModel().insert(new Object[] { group }, null, null, map, null);
		}
	}
}
