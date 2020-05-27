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

package org.mrcube.actions;

import org.jgraph.graph.CellView;
import org.mrcube.jgraph.GraphManager;
import org.mrcube.jgraph.RDFGraph;
import org.mrcube.utils.Translator;
import org.mrcube.utils.Utilities;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Takeshi Morita
 * 
 */
public class UnGroupAction extends AbstractAction {

	private final RDFGraph graph;
	private static GraphManager gmanager;
	private static final String TITLE = Translator.getString("Action.UnGroup.Text");
	private static final ImageIcon ICON = Utilities.getImageIcon(Translator
			.getString("Action.UnGroup.Icon"));

	public UnGroupAction(RDFGraph g, GraphManager gm) {
		super(TITLE, ICON);
		graph = g;
		gmanager = gm;
		putValue(SHORT_DESCRIPTION, TITLE);
		putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_U,
				Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
	}

	// Determines if a Cell is a Group
	private static boolean isGroup(RDFGraph graph, Object cell) {
		// Map the Cell to its View
		CellView view = graph.getGraphLayoutCache().getMapping(cell, false);
		if (view != null)
			return !view.isLeaf();
		return false;
	}

	public static void ungroup(RDFGraph graph) {
		Object[] cells = graph.getSelectionCells();
		if (cells != null && cells.length > 0) {
			List<Object> groups = new ArrayList<>();
			List<Object> children = new ArrayList<>();
			for (Object cell : cells) {
				if (isGroup(graph, cell)) {
					groups.add(cell);
					for (int j = 0; j < graph.getModel().getChildCount(cell); j++) {
						Object child = graph.getModel().getChild(cell, j);
						if (!RDFGraph.isPort(child)) {
							children.add(child);
						}
					}
				}
			}
			graph.getModel().remove(groups.toArray());
			graph.setSelectionCells(children.toArray());
			gmanager.resetTypeCells();
		}
	}

	// Ungroup the Groups in Cells and Select the Children
	public void actionPerformed(ActionEvent e) {
		ungroup(graph);
	}

}
