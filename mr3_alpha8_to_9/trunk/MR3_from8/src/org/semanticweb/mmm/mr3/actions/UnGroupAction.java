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
import java.util.*;

import javax.swing.*;

import org.jgraph.graph.*;
import org.semanticweb.mmm.mr3.jgraph.*;
import org.semanticweb.mmm.mr3.util.*;

/**
 * @author takeshi morita
 *
 */
public class UnGroupAction extends AbstractAction {

	private RDFGraph graph;
	private static final String TITLE = Translator.getString("Action.UnGroup.Text");
	private static final ImageIcon ICON = Utilities.getImageIcon(Translator.getString("Action.UnGroup.Icon"));

	public UnGroupAction(RDFGraph g) {
		super(TITLE, ICON);
		graph = g;
		putValue(SHORT_DESCRIPTION, TITLE);
		putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_U, KeyEvent.CTRL_MASK));
	}

	// Determines if a Cell is a Group
	private boolean isGroup(Object cell) {
		// Map the Cell to its View
		CellView view = graph.getGraphLayoutCache().getMapping(cell, false);
		if (view != null)
			return !view.isLeaf();
		return false;
	}

	//	Ungroup the Groups in Cells and Select the Children
	public void actionPerformed(ActionEvent e) {
		Object[] cells = graph.getSelectionCells();

		if (cells != null && cells.length > 0) {
			List groups = new ArrayList();
			List children = new ArrayList();
			for (int i = 0; i < cells.length; i++) {
				if (isGroup(cells[i])) {
					groups.add(cells[i]);
					for (int j = 0; j < graph.getModel().getChildCount(cells[i]); j++) {
						Object child = graph.getModel().getChild(cells[i], j);
						if (!graph.isPort(child)) {
							children.add(child);
						}
					}
				}
			}
			graph.getModel().remove(groups.toArray());
			graph.setSelectionCells(children.toArray());
		}
	}

}
