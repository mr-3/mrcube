/*
 * Created on 2003/11/25
 *  
 */
package org.semanticweb.mmm.mr3.layout;

import java.util.*;

import org.jgraph.graph.*;
import org.semanticweb.mmm.mr3.jgraph.*;
import org.semanticweb.mmm.mr3.util.*;

/**
 * @author takeshi morita
 */
public class JGraphTreeLayout {

	private GraphManager gmanager;
	private RDFCellMaker cellMaker;
	private RDFGraph rdfGraph;
	private RDFGraph classGraph;
	private RDFGraph propGraph;

	public JGraphTreeLayout(GraphManager gm) {
		gmanager = gm;
		cellMaker = new RDFCellMaker(gmanager);
		rdfGraph = gmanager.getRDFGraph();
		classGraph = gmanager.getClassGraph();
		propGraph = gmanager.getPropertyGraph();
	}

	public void performJGraphTreeLayout() {
		gmanager.removeTypeCells();
		performJGraphTreeLayout(rdfGraph, TreeLayoutAlgorithm.LEFT_TO_RIGHT, 200, 20);
		gmanager.addTypeCells();
		performJGraphRDFSTreeLayout();
	}

	public void performJGraphRDFSTreeLayout() {
		performJGraphTreeLayout(classGraph, TreeLayoutAlgorithm.UP_TO_DOWN, 30, 50);
		performJGraphTreeLayout(propGraph, TreeLayoutAlgorithm.UP_TO_DOWN, 30, 50);
		gmanager.changeCellView();
		gmanager.clearSelection();
	}

	public void performJGraphTreeLayout(RDFGraph graph, int orientation, int distance, int border) {
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
		TreeLayoutAlgorithm treeLayout = new TreeLayoutAlgorithm(orientation, distance, border);
		if (tmpRoot != null) {
			treeLayout.perform(graph, new Object[] { tmpRoot });
		} else {
			treeLayout.perform(graph, rootCells.toArray());
		}
		dataSet.remove(cellLayoutMap.get(tmpRoot));
		GraphLayoutUtilities.removeTemporaryRoot(rdfGraph, (DefaultGraphCell) tmpRoot);

		GraphLayoutUtilities.centerCellsInGraph(graph);
	}
}
