/*
 * Created on 2003/11/25
 *  
 */
package org.semanticweb.mmm.mr3.layout;

import java.awt.*;
import java.util.*;

import org.jgraph.*;
import org.jgraph.graph.*;
import org.semanticweb.mmm.mr3.data.*;
import org.semanticweb.mmm.mr3.jgraph.*;
import org.semanticweb.mmm.mr3.util.*;

import com.hp.hpl.jena.rdf.model.*;

/**
 * @author takeshi morita
 */
public class MR3TreeLayout {

	private GraphManager gmanager;
	private RDFCellMaker cellMaker;
	private RDFGraph rdfGraph;
	private RDFGraph classGraph;
	private RDFGraph propGraph;
	private RDFResourceInfoMap resInfoMap = RDFResourceInfoMap.getInstance();

	public MR3TreeLayout(GraphManager gm) {
		gmanager = gm;
		cellMaker = new RDFCellMaker(gmanager);
		rdfGraph = gmanager.getRDFGraph();
		classGraph = gmanager.getClassGraph();
		propGraph = gmanager.getPropertyGraph();
	}

	private void addChild(JGraph graph, DefaultGraphCell cell, GraphLayoutData data, Map map) {
		Port port = (Port) cell.getChildAt(0);

		for (Iterator i = port.edges(); i.hasNext();) {
			Edge edge = (Edge) i.next();
			GraphCell sourceCell = (GraphCell) rdfGraph.getSourceVertex(edge);
			GraphCell targetCell = (GraphCell) rdfGraph.getTargetVertex(edge);

			if (targetCell != cell) { // é©êgÇ™TargetÇÃèÍçá
				data.addChild(map.get(targetCell));
			}
			if (targetCell == cell && targetCell != sourceCell) {
				data.setHasParent(true);
			}
		}
	}

	public void performVGJTreeLayout() {
		gmanager.removeTypeCells();
		performVGJTreeLayout(rdfGraph, 'r');
		gmanager.addTypeCells();
		performVGJRDFSTreeLayout();
	}

	public void performJGraphTreeLayout() {
		gmanager.removeTypeCells();
		performJGraphTreeLayout(rdfGraph, TreeLayoutAlgorithm.LEFT_TO_RIGHT, 200, 20);
		gmanager.addTypeCells();
		performJGraphRDFSTreeLayout();
	}

	public void performVGJRDFSTreeLayout() {
		performVGJTreeLayout(classGraph, 'u');
		performVGJTreeLayout(propGraph, 'u');
		gmanager.changeCellView();
		gmanager.clearSelection();
	}

	public void performJGraphRDFSTreeLayout() {
		performJGraphTreeLayout(classGraph, TreeLayoutAlgorithm.UP_TO_DOWN, 30, 50);
		performJGraphTreeLayout(propGraph, TreeLayoutAlgorithm.UP_TO_DOWN, 30, 50);
		gmanager.changeCellView();
		gmanager.clearSelection();
	}

	public void performJGraphTreeLayout(RDFGraph graph, int orientation, int distance, int border) {
		Map cellLayoutMap = new HashMap();
		Set dataSet = initGraphLayoutData(graph, cellLayoutMap);
		Set rootCells = new HashSet();
		for (Iterator i = dataSet.iterator(); i.hasNext();) {
			GraphLayoutData data = (GraphLayoutData) i.next();
			DefaultGraphCell cell = (DefaultGraphCell) data.getCell();
			addChild(graph, cell, data, cellLayoutMap);
			if (!data.hasParent()) {
				rootCells.add(cell);
			}
		}

		Object tmpRoot = null;
		if (rootCells.size() != 1) {
			tmpRoot = collectRoot(rootCells, dataSet, cellLayoutMap);
		}
		TreeLayoutAlgorithm treeLayout = new TreeLayoutAlgorithm(orientation, distance, border);
		if (tmpRoot != null) {
			treeLayout.perform(graph, new Object[] { tmpRoot });
		} else {
			treeLayout.perform(graph, rootCells.toArray());
		}
		dataSet.remove(cellLayoutMap.get(tmpRoot));
		removeTemporaryRoot((DefaultGraphCell) tmpRoot);

		centerCellsInGraph(graph);
	}

	private Set initGraphLayoutData(RDFGraph graph, Map cellLayoutMap) {
		Object[] cells = graph.getAllCells();
		Set dataSet = new HashSet();
		for (int i = 0; i < cells.length; i++) {
			GraphCell cell = (GraphCell) cells[i];
			if (!graph.isTypeCell(cell) && (graph.isRDFSCell(cell) || graph.isRDFResourceCell(cell) || graph.isRDFLiteralCell(cell))) {
				GraphLayoutData data = new GraphLayoutData(cell, graph);
				cellLayoutMap.put(cell, data);
				dataSet.add(data);
			}
		}
		return dataSet;
	}

	private Object collectRoot(Set rootCells, Set dataSet, Map cellLayoutMap) {
		DefaultGraphCell rootCell = new RDFResourceCell("");
		DefaultPort rootPort = new DefaultPort();
		rootCell.add(rootPort);

		Map attributes = new HashMap();
		attributes.put(rootCell, cellMaker.getResourceMap(new Point(50, 50), ChangeCellAttributes.rdfResourceColor));
		resInfoMap.putCellInfo(rootCell, new RDFResourceInfo(URIType.ANONYMOUS, new AnonId().toString(), null));
		rdfGraph.getModel().insert(new Object[] { rootCell }, attributes, null, null, null);
		GraphLayoutData rootData = new GraphLayoutData(rootCell, rdfGraph);
		rootData.setHasParent(false);

		for (Iterator i = rootCells.iterator(); i.hasNext();) {
			DefaultGraphCell cell = (DefaultGraphCell) i.next();
			Port port = (Port) cell.getChildAt(0);
			DefaultEdge edge = new DefaultEdge("");
			ConnectionSet cs = new ConnectionSet(edge, rootPort, port);
			rdfGraph.getModel().insert(new Object[] { edge }, null, cs, null, null);
			GraphLayoutData data = (GraphLayoutData) cellLayoutMap.get(cell);
			data.setHasParent(true);
			rootData.addChild(data);
		}
		dataSet.add(rootData);

		return rootCell;
	}

	private void removeTemporaryRoot(DefaultGraphCell tmpRoot) {
		if (tmpRoot == null) {
			return;
		}
		Set removeCellsSet = new HashSet();
		Port port = (Port) tmpRoot.getChildAt(0);
		removeCellsSet.add(tmpRoot);
		removeCellsSet.add(port);
		for (Iterator edges = rdfGraph.getModel().edges(port); edges.hasNext();) {
			removeCellsSet.add(edges.next());
		}
		rdfGraph.getModel().remove(removeCellsSet.toArray());
	}

	private void performVGJTreeLayout(RDFGraph graph, char arc) {
		Map cellLayoutMap = new HashMap();
		Set dataSet = initGraphLayoutData(graph, cellLayoutMap);
		Set rootCells = new HashSet();
		for (Iterator i = dataSet.iterator(); i.hasNext();) {
			GraphLayoutData data = (GraphLayoutData) i.next();
			DefaultGraphCell cell = (DefaultGraphCell) data.getCell();
			addChild(graph, cell, data, cellLayoutMap);
			if (!data.hasParent()) {
				rootCells.add(cell);
			}
		}

		Object tmpRoot = null;
		if (rootCells.size() != 1) {
			tmpRoot = collectRoot(rootCells, dataSet, cellLayoutMap);
		}

		TreeAlgorithm treeAlgorithm = new TreeAlgorithm(arc);
		treeAlgorithm.applyTreeAlgorithm(dataSet, null);
		dataSet.remove(cellLayoutMap.get(tmpRoot));
		removeTemporaryRoot((DefaultGraphCell) tmpRoot);

		for (Iterator i = dataSet.iterator(); i.hasNext();) {
			GraphLayoutData data = (GraphLayoutData) i.next();
			data.setRealResourcePosition();
		}
		centerCellsInGraph(graph);
	}

	private void centerCellsInGraph(RDFGraph graph) {
		int MARGIN = 50;
		Object[] cells = graph.getAllCells();
		if (cells.length == 0) {
			return;
		}
		Rectangle rec = graph.getCellBounds(cells);

		int reviseX = 0;
		int reviseY = 0;
		if (rec.x <= 0) {
			reviseX = (-rec.x) + MARGIN;
		} else if (MARGIN < rec.x) {
			reviseX = MARGIN - rec.x;
		}
		if (rec.y <= 0) {
			reviseY = (-rec.y) + MARGIN;
		} else if (MARGIN < rec.y) {
			reviseY = MARGIN - rec.y;
		}

		for (int i = 0; i < cells.length; i++) {
			if (graph.isRDFsCell(cells[i]) || graph.isTypeCell(cells[i])) {
				GraphCell cell = (GraphCell) cells[i];
				Map map = cell.getAttributes();
				Rectangle cellRec = GraphConstants.getBounds(map);
				cellRec.x += reviseX;
				cellRec.y += reviseY;
				GraphConstants.setBounds(map, cellRec);
				ChangeCellAttributes.editCell(cell, map, graph);
			}
		}
	}
}
