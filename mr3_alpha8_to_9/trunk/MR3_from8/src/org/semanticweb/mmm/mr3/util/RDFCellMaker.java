/*
 * @(#) RDFCellMaker.java
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

import org.semanticweb.mmm.mr3.data.*;
import org.semanticweb.mmm.mr3.jgraph.*;

import org.jgraph.*;
import org.jgraph.graph.*;

import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.vocabulary.*;

/*
 * 
 * @author takeshi morita
 *
 */
public class RDFCellMaker {

	private GraphManager gmanager;
	private RDFResourceInfoMap resInfoMap = RDFResourceInfoMap.getInstance();
	private RDFLiteralInfoMap litInfoMap = RDFLiteralInfoMap.getInstance();
	private RDFSInfoMap rdfsInfoMap = RDFSInfoMap.getInstance();

	private static final int CELL_WIDTH = 100;
	private static final int CELL_HEIGHT = 25;

	public RDFCellMaker(GraphManager manager) {
		gmanager = manager;
	}

	public Map getEdgeMap(String value) {
		Map map = GraphConstants.createMap();
		GraphConstants.setLineEnd(map, GraphConstants.ARROW_TECHNICAL);
		GraphConstants.setValue(map, value);

		return map;
	}

	public Map getResourceMap(Point point, Color cellColor) {
		Map map = GraphConstants.createMap();

		if (ChangeCellAttrUtil.isColor) {
			GraphConstants.setBackground(map, cellColor);
			GraphConstants.setOpaque(map, true);
		} else {
			GraphConstants.setOpaque(map, false);
		}
		GraphConstants.setBorderColor(map, Color.black);
		GraphConstants.setBounds(map, new Rectangle(point, new Dimension(CELL_WIDTH, CELL_HEIGHT)));

		return map;
	}

	public GraphCell insertRDFLiteral(Point point) {
		JGraph graph = gmanager.getRDFGraph();
		point = graph.snap(new Point(point)); // Snap the Point to the Grid
		Map map = getResourceMap(point, ChangeCellAttrUtil.literalColor);

		DefaultGraphCell vertex = new RDFLiteralCell("");
		setCell(graph, vertex, map);
		litInfoMap.putCellInfo(vertex, RDFLiteralUtil.createLiteral("", "", null));
		gmanager.jumpRDFArea(vertex);
		return vertex;
	}

	public DefaultGraphCell addTypeCell(Object resourceCell, Map attributes, Rectangle rec) {
		RDFGraph graph = gmanager.getRDFGraph();
		DefaultGraphCell typeCell = null;

		if (gmanager.isShowTypeCell()) {
			typeCell = new TypeCell("");
			Point typePoint = new Point(rec.x, rec.y + rec.height);
			Map typeMap = getResourceMap(typePoint, ChangeCellAttrUtil.classColor);
			attributes.put(typeCell, typeMap);

			ParentMap parentMap = new ParentMap();
			DefaultGraphCell group = new DefaultGraphCell();
			parentMap.addEntry(resourceCell, group);
			parentMap.addEntry(typeCell, group);

			graph.getModel().insert(new Object[] { resourceCell, typeCell, group }, attributes, null, parentMap, null);
		} else {
			graph.getModel().insert(new Object[] { resourceCell }, attributes, null, null, null);
		}
		return typeCell;
	}

	public GraphCell insertRDFResource(Point point, String uri, Object resTypeCell, URIType type) {
		JGraph graph = gmanager.getRDFGraph();
		HashMap attributes = new HashMap();
		point = graph.snap(new Point(point));

		RDFResourceCell resourceCell = new RDFResourceCell(uri);
		resourceCell.add(new DefaultPort());
		Map resMap = getResourceMap(point, ChangeCellAttrUtil.rdfResourceColor);
		attributes.put(resourceCell, resMap);

		DefaultGraphCell typeCell = addTypeCell(resourceCell, attributes, new Rectangle(point, new Dimension(CELL_WIDTH, CELL_HEIGHT)));

		RDFResourceInfo info = null;
		if (type == URIType.ANONYMOUS) {
//			info = new RDFResourceInfo(type, new AnonId().toString(), typeCell);
			info = new RDFResourceInfo(type, ResourceFactory.createResource().toString(), typeCell);
		} else {
			info = new RDFResourceInfo(type, uri, typeCell);
		}
		info.setTypeCell(resTypeCell);
		resInfoMap.putCellInfo(resourceCell, info);
		gmanager.changeCellView();
		gmanager.jumpRDFArea(resourceCell);

		return resourceCell;
	}

	public Point calcInsertPoint(Set supCells) {
		boolean isFirst = true;
		int left = 50;
		int right = 50;
		int bottom = 0;
		for (Iterator i = supCells.iterator(); i.hasNext();) {
			GraphCell cell = (GraphCell) i.next();
			Map map = cell.getAttributes();
			Rectangle rec = GraphConstants.getBounds(map);
			if (isFirst) {
				right = rec.x;
				left = rec.x;
				bottom = rec.y;
				isFirst = false;
			} else {
				if (right < rec.x) {
					right = rec.x;
				}
				if (left > rec.x) {
					left = rec.x;
				}
				if (bottom < rec.y) {
					bottom = rec.y;
				}
			}
		}
		return new Point((left + right) / 2, bottom + 100);
	}

	public void connectSubToSups(Port subPort, Object[] supCells, RDFGraph graph) {
		for (int i = 0; i < supCells.length; i++) {
			if (graph.isPort(supCells[i])) {
				Port supPort = (Port) supCells[i];
				connect(subPort, supPort, "", graph);
			}
		}
	}

	public Edge connect(Port source, Port target, String edgeName, RDFGraph graph) {
		DefaultEdge edge = new DefaultEdge(edgeName);
		ConnectionSet cs = new ConnectionSet(edge, source, target);
		HashMap attributes = new HashMap();
		Map map = getEdgeMap(edgeName);
		attributes.put(edge, map);
		graph.getGraphLayoutCache().insert(new Object[] { edge }, attributes, cs, null, null);

		return edge;
	}

	public void selfConnect(Port port, String edgeName, RDFGraph graph) {
		DefaultEdge edge = new DefaultEdge(edgeName);
		ConnectionSet cs = new ConnectionSet(edge, port, port);
		HashMap attributes = new HashMap();
		Map map = getEdgeMap(edgeName);
		GraphConstants.setRouting(map, GraphConstants.ROUTING_SIMPLE);
		attributes.put(edge, map);
		graph.getGraphLayoutCache().insert(new Object[] { edge }, attributes, cs, null, null);
	}

	public GraphCell insertClass(Point point, String uri) {
		JGraph graph = gmanager.getClassGraph();
		point = graph.snap(new Point(point)); // Snap the Point to the Grid
		Map map = getResourceMap(point, ChangeCellAttrUtil.classColor);
		RDFSClassCell vertex = new RDFSClassCell(uri);

		setCell(graph, vertex, map);

		RDFSInfo info = new ClassInfo(uri);
		rdfsInfoMap.putCellInfo(vertex, info);
		gmanager.changeCellView();
		gmanager.jumpClassArea(vertex);
		return vertex;
	}

	public GraphCell insertProperty(Point point, String uri) {
		JGraph graph = gmanager.getPropertyGraph();
		point = graph.snap(new Point(point)); // Snap the Point to the Grid
		Map map = getResourceMap(point, ChangeCellAttrUtil.propertyColor);

		RDFSPropertyCell vertex = new RDFSPropertyCell(uri);
		PropertyInfo info = new PropertyInfo(uri);
		if (uri.matches(RDF.getURI() + "_\\d*")) {
			info.setContainer(true);
			info.setNum(Integer.parseInt(uri.split("_")[1]));
		} else {
			setCell(graph, vertex, map);
		}

		rdfsInfoMap.putCellInfo(vertex, info);
		gmanager.changeCellView();
		gmanager.jumpPropertyArea(vertex);

		return vertex;
	}

	private void setCell(JGraph graph, DefaultGraphCell cell, Map map) {
		cell.add(new DefaultPort());
		HashMap attributes = new HashMap();
		attributes.put(cell, map);
		graph.getModel().insert(new Object[] { cell }, attributes, null, null, null);
	}
}
