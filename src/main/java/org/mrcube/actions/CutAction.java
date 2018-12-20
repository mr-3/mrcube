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

package org.mrcube.actions;

import org.jgraph.graph.GraphCell;
import org.jgraph.graph.GraphConstants;
import org.mrcube.jgraph.GraphManager;
import org.mrcube.jgraph.RDFGraph;
import org.mrcube.models.ClassInfo;
import org.mrcube.models.MR3Constants.GraphType;
import org.mrcube.models.MR3Constants.HistoryType;
import org.mrcube.models.PropertyInfo;
import org.mrcube.models.RDFSInfoMap;
import org.mrcube.utils.Translator;
import org.mrcube.utils.Utilities;
import org.mrcube.views.HistoryManager;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

/**
 * @author Takeshi Morita
 */
public class CutAction extends AbstractAction {
	private RDFGraph graph;
	private GraphManager gmanager;
	private static final String TITLE = Translator.getString("Action.Cut.Text");
	private static final ImageIcon ICON = Utilities.getImageIcon(Translator
			.getString("Action.Cut.Icon"));

	public CutAction(RDFGraph g, GraphManager gm) {
		super(TITLE, ICON);
		graph = g;
		gmanager = gm;
		putValue(SHORT_DESCRIPTION, TITLE);
		putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_X, KeyEvent.CTRL_MASK));
	}

	public void actionPerformed(ActionEvent e) {
		if (graph.getType() == GraphType.RDF) {
			HistoryManager.saveHistory(HistoryType.CUT_RDF_GRAPH);
		} else if (graph.getType() == GraphType.CLASS) {
			RDFSInfoMap rdfsInfoMap = gmanager.getCurrentRDFSInfoMap();
			for (Object cell : graph.getSelectionCells()) {
				GraphCell gcell = (GraphCell) cell;
				if (RDFGraph.isRDFSClassCell(gcell)) {
					ClassInfo orgInfo = (ClassInfo) GraphConstants.getValue(gcell.getAttributes());
					rdfsInfoMap.removeURICellMap(orgInfo);
					rdfsInfoMap.removeCellInfo(gcell);
				}
			}
			HistoryManager.saveHistory(HistoryType.CUT_CLASS_GRAPH);
		} else if (graph.getType() == GraphType.PROPERTY) {
			RDFSInfoMap rdfsInfoMap = gmanager.getCurrentRDFSInfoMap();
			for (Object cell : graph.getSelectionCells()) {
				GraphCell gcell = (GraphCell) cell;
				if (RDFGraph.isRDFSPropertyCell(gcell)) {
					PropertyInfo orgInfo = (PropertyInfo) GraphConstants.getValue(gcell
							.getAttributes());
					rdfsInfoMap.removeURICellMap(orgInfo);
					rdfsInfoMap.removeCellInfo(gcell);
				}
			}
			HistoryManager.saveHistory(HistoryType.CUT_PROPERTY_GRAPH);
		}
		TransferHandler.getCutAction().actionPerformed(
				new ActionEvent(graph, e.getID(), e.getActionCommand()));
	}
}
