/*
 * Created on 2003/05/25
 *
 */
package org.semanticweb.mmm.mr3.util;

import java.awt.*;
import java.util.*;

import org.semanticweb.mmm.mr3.jgraph.*;

import org.jgraph.*;
import org.jgraph.graph.*;

/**
 * @author takeshi morita
 */
public class ChangeCellAttributes {

	public static boolean isColor = true;
	
	public static Color rdfResourceColor = Color.pink;
	public static Color literalColor = Color.orange;
	public static Color classColor = Color.green;
	public static Color propertyColor = new Color(255, 158, 62);
	public static Color selectedColor = new Color(255, 255, 50);

	public static void changeAllCellColor(GraphManager gmanager) {
		RDFGraph rdfGraph = gmanager.getRDFGraph();
		RDFGraph classGraph = gmanager.getClassGraph();
		RDFGraph propertyGraph = gmanager.getPropertyGraph();

		Object[] cells = rdfGraph.getAllCells();
		for (int i = 0; i < cells.length; i++) {
			GraphCell cell = (GraphCell) cells[i];
			if (rdfGraph.isRDFResourceCell(cell)) {
				changeCellColor(rdfGraph, cell, rdfResourceColor);
			} else if (rdfGraph.isRDFLiteralCell(cell)) {
				changeCellColor(rdfGraph, cell, literalColor);
			} else if (rdfGraph.isTypeCell(cell)) {
				changeCellColor(rdfGraph, cell, classColor);
			}
		}

		cells = classGraph.getAllCells();
		for (int i = 0; i < cells.length; i++) {
			GraphCell cell = (GraphCell) cells[i];
			if (classGraph.isRDFSClassCell(cell)) {
				changeCellColor(classGraph, cell, classColor);
			}
		}

		cells = propertyGraph.getAllCells();
		for (int i = 0; i < cells.length; i++) {
			GraphCell cell = (GraphCell) cells[i];
			if (propertyGraph.isRDFSPropertyCell(cell)) {
				changeCellColor(propertyGraph, cell, propertyColor);
			}
		}
	}

	/**
	 *  
	 * セルの色を変更するためのメソッド
	 * @param graph
	 * @param cell
	 * @param color
	 */
	public static void changeCellColor(JGraph graph, GraphCell cell, Color color) {
		if (cell != null) {
			Map map = GraphConstants.createMap();
			
			if (isColor) {
				GraphConstants.setBackground(map, color);
				GraphConstants.setOpaque(map, true);
			} else {
				GraphConstants.setOpaque(map, false);
			}
			map = GraphConstants.cloneMap(map);
			map.remove(GraphConstants.BOUNDS);
			map.remove(GraphConstants.POINTS);

			Map nested = new HashMap();
			nested.put(cell, GraphConstants.cloneMap(map));
			graph.getGraphLayoutCache().edit(nested, null, null, null);
		}
	}

	public static boolean isChangedSelectedColor = true;

	public static Object[] changeSelectionCellColor(RDFGraph graph, Object[] lastSelectionCells) {
		if (!isChangedSelectedColor) {
			return lastSelectionCells;
		}
		for (int i = 0; i < lastSelectionCells.length; i++) {
			GraphCell cell = (GraphCell) lastSelectionCells[i];
			if (graph.isRDFResourceCell(cell)) {
				changeCellColor(graph, cell, rdfResourceColor);
			} else if (graph.isRDFLiteralCell(cell)) {
				changeCellColor(graph, cell, literalColor);
			} else if (graph.isRDFSClassCell(cell)) {
				changeCellColor(graph, cell, classColor);
			} else if (graph.isRDFSPropertyCell(cell)) {
				changeCellColor(graph, cell, propertyColor);
			}
		}
		Object[] cells = graph.getSelectionCells();
		cells = graph.getDescendants(cells);
		for (int i = 0; i < cells.length; i++) {
			GraphCell cell = (GraphCell) cells[i];
			if (graph.isRDFsCell(cell) && (!graph.isRDFPropertyCell(cell))) {
				changeCellColor(graph, cell, selectedColor);
			}
		}
		return cells;
	}
	
	public static void editCell(GraphCell cell, Map map, RDFGraph graph) {
		Map nested = new HashMap();
		nested.put(cell, GraphConstants.cloneMap(map));
		graph.getModel().edit(nested, null, null, null);
	}
}
