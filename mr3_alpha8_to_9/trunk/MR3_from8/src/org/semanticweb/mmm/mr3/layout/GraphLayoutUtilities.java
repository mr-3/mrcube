/*
 * Created on 2003/11/25
 *  
 */
package org.semanticweb.mmm.mr3.layout;

import java.awt.*;
import java.util.*;

import org.jgraph.graph.*;
import org.semanticweb.mmm.mr3.data.*;
import org.semanticweb.mmm.mr3.jgraph.*;
import org.semanticweb.mmm.mr3.util.*;

import com.hp.hpl.jena.rdf.model.*;

/**
 * @author takeshi morita
 */
public class GraphLayoutUtilities {

	private static RDFResourceInfoMap resInfoMap = RDFResourceInfoMap.getInstance();

	public static void reverseArc(RDFCellMaker cellMaker, RDFGraph graph) {
		Set removeEdges = new HashSet();
		Object[] cells = graph.getAllCells();
		for (int i = 0; i < cells.length; i++) {
			if (graph.isEdge(cells[i])) {
				Edge edge = (Edge) cells[i];
				removeEdges.add(edge);
				cellMaker.connect((Port)graph.getModel().getTarget(edge), (Port) graph.getModel().getSource(edge), edge.toString(), graph);
			}
		}
		graph.getModel().remove(removeEdges.toArray());
	}

	public static void addChild(RDFGraph graph, DefaultGraphCell cell, GraphLayoutData data, Map map) {
		Port port = (Port) cell.getChildAt(0);

		for (Iterator i = port.edges(); i.hasNext();) {
			Edge edge = (Edge) i.next();
			GraphCell sourceCell = (GraphCell) graph.getSourceVertex(edge);
			GraphCell targetCell = (GraphCell) graph.getTargetVertex(edge);

			if (targetCell != cell) { // Ž©g‚ªTarget‚Ìê‡
				data.addChild(map.get(targetCell));
			}
			if (targetCell == cell && targetCell != sourceCell) {
				data.setHasParent(true);
			}
		}
	}

	public static Set initGraphLayoutData(RDFGraph graph, Map cellLayoutMap) {
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

	public static Object collectRoot(RDFGraph graph, RDFCellMaker cellMaker, Set rootCells, Set dataSet, Map cellLayoutMap) {
		DefaultGraphCell rootCell = new RDFResourceCell("");
		DefaultPort rootPort = new DefaultPort();
		rootCell.add(rootPort);

		Map attributes = new HashMap();
		attributes.put(rootCell, cellMaker.getResourceMap(new Point(50, 50), ChangeCellAttributes.rdfResourceColor));
		resInfoMap.putCellInfo(rootCell, new RDFResourceInfo(URIType.ANONYMOUS, new AnonId().toString(), null));
		graph.getModel().insert(new Object[] { rootCell }, attributes, null, null, null);
		GraphLayoutData rootData = new GraphLayoutData(rootCell, graph);
		rootData.setHasParent(false);

		for (Iterator i = rootCells.iterator(); i.hasNext();) {
			DefaultGraphCell cell = (DefaultGraphCell) i.next();
			Port port = (Port) cell.getChildAt(0);
			DefaultEdge edge = new DefaultEdge("");
			ConnectionSet cs = new ConnectionSet(edge, rootPort, port);
			graph.getModel().insert(new Object[] { edge }, null, cs, null, null);
			GraphLayoutData data = (GraphLayoutData) cellLayoutMap.get(cell);
			data.setHasParent(true);
			rootData.addChild(data);
		}
		dataSet.add(rootData);

		return rootCell;
	}

	public static void removeTemporaryRoot(RDFGraph graph, DefaultGraphCell tmpRoot) {
		if (tmpRoot == null) {
			return;
		}
		Set removeCellsSet = new HashSet();
		Port port = (Port) tmpRoot.getChildAt(0);
		removeCellsSet.add(tmpRoot);
		removeCellsSet.add(port);
		for (Iterator edges = graph.getModel().edges(port); edges.hasNext();) {
			removeCellsSet.add(edges.next());
		}
		graph.getModel().remove(removeCellsSet.toArray());
	}

	public static void centerCellsInGraph(RDFGraph graph) {
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
