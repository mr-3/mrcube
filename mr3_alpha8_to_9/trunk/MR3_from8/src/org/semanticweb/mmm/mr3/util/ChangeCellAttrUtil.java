/*
 * @(#) ChangeCellAttributes.java
 * 
 * Copyright (C) 2003 The MMM Project
 * 
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation; either version 2.1 of the License, or (at your
 * option) any later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *  
 */

package org.semanticweb.mmm.mr3.util;

import java.awt.*;
import java.util.*;

import org.jgraph.graph.*;
import org.semanticweb.mmm.mr3.jgraph.*;

/**
 * @author takeshi morita
 */
public class ChangeCellAttrUtil {

	public static boolean isColor = true;

	public static Color rdfResourceColor = Color.pink;
	public static Color rdfPropertyColor = Color.black;
	public static Color literalColor = Color.orange;
	public static Color classColor = Color.green;
	public static Color propertyColor = new Color(255, 158, 62);
	public static Color selectedColor = new Color(153, 255, 255);
	public static Color selectedBorderColor = Color.red;

	public static void changeAllCellColor(GraphManager gmanager) {
		RDFGraph rdfGraph = gmanager.getRDFGraph();
		RDFGraph classGraph = gmanager.getClassGraph();
		RDFGraph propertyGraph = gmanager.getPropertyGraph();

		Object[] cells = rdfGraph.getAllCells();
		for (int i = 0; i < cells.length; i++) {
			GraphCell cell = (GraphCell) cells[i];
			if (rdfGraph.isRDFResourceCell(cell)) {
				changeDefaultCellStye(rdfGraph, cell, rdfResourceColor);
			} else if (rdfGraph.isRDFLiteralCell(cell)) {
				changeDefaultCellStye(rdfGraph, cell, literalColor);
			} else if (rdfGraph.isTypeCell(cell)) {
				changeDefaultCellStye(rdfGraph, cell, classColor);
			}
		}

		cells = classGraph.getAllCells();
		for (int i = 0; i < cells.length; i++) {
			GraphCell cell = (GraphCell) cells[i];
			if (classGraph.isRDFSClassCell(cell)) {
				changeDefaultCellStye(rdfGraph, cell, classColor);
			}
		}

		cells = propertyGraph.getAllCells();
		for (int i = 0; i < cells.length; i++) {
			GraphCell cell = (GraphCell) cells[i];
			if (propertyGraph.isRDFSPropertyCell(cell)) {
				changeDefaultCellStye(rdfGraph, cell, propertyColor);
			}
		}
	}

	/**
	 * 
	 * �Z���̐F��ύX���邽�߂̃��\�b�h
	 * 
	 * @param graph
	 * @param cell
	 * @param backGroundColor
	 * @param borderColor
	 * @param lineWidth
	 *  
	 */
	public static void changeCellStyle(RDFGraph graph, GraphCell cell, Color backGroundColor, Color borderColor, float lineWidth) {
		if (cell != null) {
			Map map = GraphConstants.createMap();

			if (isColor) {
				GraphConstants.setLineWidth(map, lineWidth);
				if (graph.isRDFPropertyCell(cell)) {
					GraphConstants.setLineColor(map, borderColor);
				} else {
					GraphConstants.setBorderColor(map, borderColor);
					GraphConstants.setBackground(map, backGroundColor);
					GraphConstants.setOpaque(map, true);
				}
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

	public static void changeDefaultCellStye(RDFGraph graph, GraphCell cell, Color backGroundColor) {
		changeCellStyle(graph, cell, backGroundColor, Color.black, 1);
	}

	public static boolean isChangedSelectedColor = true;

	public static Object[] changeSelectionCellStyle(RDFGraph graph, Object[] lastSelectionCells) {
		if (!isChangedSelectedColor) {
			return lastSelectionCells;
		}
		for (int i = 0; i < lastSelectionCells.length; i++) {
			GraphCell cell = (GraphCell) lastSelectionCells[i];
			if (graph.isRDFResourceCell(cell)) {
				changeDefaultCellStye(graph, cell, rdfResourceColor);
			} else if (graph.isTypeCell(cell)) {
				changeDefaultCellStye(graph, cell, classColor);
			} else if (graph.isRDFPropertyCell(cell)) {
				changeDefaultCellStye(graph, cell, rdfPropertyColor);
			} else if (graph.isRDFLiteralCell(cell)) {
				changeDefaultCellStye(graph, cell, literalColor);
			} else if (graph.isRDFSClassCell(cell)) {
				changeDefaultCellStye(graph, cell, classColor);
			} else if (graph.isRDFSPropertyCell(cell)) {
				changeDefaultCellStye(graph, cell, propertyColor);
			}
		}
		Object[] cells = graph.getSelectionCells();
		cells = graph.getDescendants(cells);
		for (int i = 0; i < cells.length; i++) {
			GraphCell cell = (GraphCell) cells[i];
			if (graph.isRDFsCell(cell) || graph.isTypeCell(cell)) {
				changeCellStyle(graph, cell, selectedColor, selectedBorderColor, 2);
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