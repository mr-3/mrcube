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
