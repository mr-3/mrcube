package mr3.util;
import java.awt.*;
import java.util.*;

import javax.swing.*;

import mr3.data.*;
import mr3.jgraph.*;

import com.hp.hpl.mesa.rdf.jena.common.*;
import com.hp.hpl.mesa.rdf.jena.model.*;
import com.jgraph.*;
import com.jgraph.graph.*;

public class RDFCellMaker {

	private GraphManager gmanager;
	private RDFResourceInfoMap resInfoMap = RDFResourceInfoMap.getInstance();
	private RDFLiteralInfoMap litInfoMap = RDFLiteralInfoMap.getInstance();
	private RDFSInfoMap rdfsInfoMap = RDFSInfoMap.getInstance();

	private static final int cellWidth = 100;
	private static final int cellHeight = 25;

	public RDFCellMaker(GraphManager manager) {
		gmanager = manager;
	}

	public Map getEdgeMap(String value) {
		Map map = GraphConstants.createMap();
		GraphConstants.setLineEnd(map, GraphConstants.ARROW_CLASSIC); 
		GraphConstants.setValue(map, value);
		return map;
	}

	private static final Color RESOURCE_COLOR = new Color(255, 128, 192);

	public Map getResourceMap(Point point) {
		Dimension size = new Dimension(cellWidth, cellHeight);
		Map map = GraphConstants.createMap();
		GraphConstants.setBounds(map, new Rectangle(point, size));
		GraphConstants.setBorderColor(map, Color.black);
		GraphConstants.setBackground(map, Color.pink);
		GraphConstants.setForeground(map, Color.black);
		// Make Vertex Opaque 透明にしない(true),透明にする(false)??
		GraphConstants.setOpaque(map, true);
		return map;
	}

	public Map getTypeMap(Point p) {
		return getTypeMap(new Rectangle(p, new Dimension(cellWidth, cellHeight)));
	}

	public Map getTypeMap(Rectangle rec) {
		Map map = GraphConstants.createMap();
		GraphConstants.setBounds(map, rec);
		GraphConstants.setBorderColor(map, Color.black);
		GraphConstants.setBackground(map, Color.green);
		GraphConstants.setForeground(map, Color.black);
		GraphConstants.setBorder(map, BorderFactory.createRaisedBevelBorder());
		GraphConstants.setOpaque(map, true);
		GraphConstants.setConnectable(map, false);

		return map;
	}

	public Map getClassMap(Point point) {
		Map map = getResourceMap(point);
		GraphConstants.setBackground(map, Color.green);
		// 矩形でクラスを表現する場合以下のオプションを使う．ちょっと，立体的に見える．
		GraphConstants.setBorder(map, BorderFactory.createRaisedBevelBorder());
		GraphConstants.setOpaque(map, true);
		return map;
	}

	private static final Color PROPERTY_COLOR = new Color(255, 158, 62);

	public Map getPropertyMap(Point point) {
		Map map = getResourceMap(point);
		GraphConstants.setBackground(map, PROPERTY_COLOR);
		GraphConstants.setOpaque(map, true);

		return map;
	}

	public Map getLiteralMap(Point point) {
		Map map = GraphConstants.createMap();
		Dimension size = new Dimension(cellWidth, cellHeight);
		GraphConstants.setBounds(map, new Rectangle(point, size));
		GraphConstants.setBorderColor(map, Color.black);
		GraphConstants.setBackground(map, Color.orange);
		GraphConstants.setForeground(map, Color.black);
		GraphConstants.setBorder(map, BorderFactory.createRaisedBevelBorder());
		GraphConstants.setOpaque(map, true);
		return map;
	}

	public GraphCell insertRDFLiteral(Point point) {
		JGraph graph = gmanager.getRDFGraph();
		point = graph.snap(new Point(point)); // Snap the Point to the Grid
		Map map = getLiteralMap(point);

		DefaultGraphCell vertex = new RDFLiteralCell("");
		setCell(graph, vertex, map);
		litInfoMap.putCellInfo(vertex, new LiteralImpl("", ""));
		gmanager.jumpRDFArea(vertex);
		return vertex;
	}

	public DefaultGraphCell addTypeCell(Object resourceCell, Map attributes, Point p) {
		return addTypeCell(resourceCell, attributes, new Rectangle(p, new Dimension(cellWidth, cellHeight)));
	}

	public DefaultGraphCell addTypeCell(Object resourceCell, Map attributes, Rectangle rec) {
		RDFGraph graph = gmanager.getRDFGraph();

		DefaultGraphCell typeCell = new TypeCell("");
		Point typePoint = new Point(rec.x, rec.y + rec.height);
		Map typeMap = getTypeMap(new Rectangle(typePoint, new Dimension(rec.width, cellHeight)));
		attributes.put(typeCell, typeMap);

		ParentMap parentMap = new ParentMap();
		DefaultGraphCell group = new DefaultGraphCell();
		parentMap.addEntry(resourceCell, group);
		parentMap.addEntry(typeCell, group);
		if (gmanager.isShowTypeCell()) {
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
		Map resMap = getResourceMap(point);
		attributes.put(resourceCell, resMap);

		DefaultGraphCell typeCell = addTypeCell(resourceCell, attributes, new Rectangle(point, new Dimension(cellWidth, cellHeight)));

		RDFResourceInfo info = null;
		if (type == URIType.ANONYMOUS) {
			info = new RDFResourceInfo(type, new AnonId().toString(), typeCell);
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

	public void connectSubToSups(Port sourcePort, Object[] supCells, RDFGraph graph) {
		for (int i = 0; i < supCells.length; i++) {
			if (graph.isPort(supCells[i])) {
				Port targetPort = (Port) supCells[i];
				connect(sourcePort, targetPort, "", graph);
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
		Map map = getClassMap(point);
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
		Map map = getPropertyMap(point);
		RDFSPropertyCell vertex = new RDFSPropertyCell(uri);
		setCell(graph, vertex, map);

		RDFSInfo info = new PropertyInfo(uri);
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
