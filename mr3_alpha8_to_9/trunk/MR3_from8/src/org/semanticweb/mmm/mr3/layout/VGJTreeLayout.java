/*
 * VGJTreeLayout.java
 *
 * Copyright (C) 2003 The MMM Project
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package org.semanticweb.mmm.mr3.layout;

import java.util.*;

import org.jgraph.graph.*;
import org.semanticweb.mmm.mr3.jgraph.*;
import org.semanticweb.mmm.mr3.util.*;

/**
 * @author takeshi morita
 */
public class VGJTreeLayout {

	private GraphManager gmanager;
	private RDFCellMaker cellMaker;
	private RDFGraph rdfGraph;
	private RDFGraph classGraph;
	private RDFGraph propGraph;

	public VGJTreeLayout(GraphManager gm) {
		gmanager = gm;
		cellMaker = new RDFCellMaker(gmanager);
		rdfGraph = gmanager.getRDFGraph();
		classGraph = gmanager.getClassGraph();
		propGraph = gmanager.getPropertyGraph();
	}

	public void performVGJTreeLayout() {
		gmanager.removeTypeCells();
		performVGJTreeLayout(rdfGraph, 'r');
		gmanager.addTypeCells();
		performVGJRDFSTreeLayout();
	}

	public void performVGJRDFSTreeLayout() {
		GraphLayoutUtilities.reverseArc(cellMaker, classGraph);
		GraphLayoutUtilities.reverseArc(cellMaker, propGraph);
		performVGJTreeLayout(classGraph, 'u');
		performVGJTreeLayout(propGraph, 'u');
		GraphLayoutUtilities.reverseArc(cellMaker, classGraph);
		GraphLayoutUtilities.reverseArc(cellMaker, propGraph);
		gmanager.changeCellView();
		gmanager.clearSelection();
	}

	private void performVGJTreeLayout(RDFGraph graph, char arc) {
		Map cellLayoutMap = new HashMap();
		Set dataSet = GraphLayoutUtilities.initGraphLayoutData(graph, cellLayoutMap);
		Set rootCells = new HashSet();
		for (Iterator i = dataSet.iterator(); i.hasNext();) {
			GraphLayoutData data = (GraphLayoutData) i.next();
			DefaultGraphCell cell = (DefaultGraphCell) data.getCell();
			GraphLayoutUtilities.addChild(graph, cell, data, cellLayoutMap);
			if (!data.hasParent()) {
				rootCells.add(cell);
			}
		}

		Object tmpRoot = null;
		if (rootCells.size() != 1) {
			tmpRoot = GraphLayoutUtilities.collectRoot(rdfGraph, cellMaker, rootCells, dataSet, cellLayoutMap);
		}

		TreeAlgorithm treeAlgorithm = new TreeAlgorithm(arc);
		treeAlgorithm.applyTreeAlgorithm(dataSet, null);
		dataSet.remove(cellLayoutMap.get(tmpRoot));
		GraphLayoutUtilities.removeTemporaryRoot(rdfGraph, (DefaultGraphCell) tmpRoot);

		for (Iterator i = dataSet.iterator(); i.hasNext();) {
			GraphLayoutData data = (GraphLayoutData) i.next();
			data.setRealResourcePosition();
		}
		GraphLayoutUtilities.centerCellsInGraph(graph);
	}

}
