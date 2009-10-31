package mr3.util;
import java.awt.*;
import java.util.*;

import javax.swing.*;

import mr3.data.*;
import mr3.jgraph.*;

import com.hp.hpl.mesa.rdf.jena.common.*;
import com.jgraph.*;
import com.jgraph.graph.*;

public class RDFCellMaker {

	private GraphManager gmanager;
	private RDFResourceInfoMap resInfoMap = RDFResourceInfoMap.getInstance();
	private RDFLiteralInfoMap litInfoMap = RDFLiteralInfoMap.getInstance();
	private RDFSInfoMap rdfsInfoMap = RDFSInfoMap.getInstance();

	private static final int cellWidth = 100;
	private static final int cellHeight = 30;

	public RDFCellMaker(GraphManager manager) {
		gmanager = manager;
	}

	public Map getEdgeMap(String value) {
		Map map = GraphConstants.createMap();
		GraphConstants.setLineEnd(map, GraphConstants.ARROW_TECHNICAL); // Add a Line End Attribute
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

	public Map getTypeMap(Point point) {
		Dimension size = new Dimension(cellWidth, 25);
		Map map = GraphConstants.createMap();
		GraphConstants.setBounds(map, new Rectangle(point, size));
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

	public GraphCell insertRDFResource(Point point, String uri, Object resTypeCell, URIType type) {
		JGraph graph = gmanager.getRDFGraph();
		HashMap attributes = new HashMap();
		point = graph.snap(new Point(point));

		RDFResourceCell resourceCell = new RDFResourceCell(uri);
		resourceCell.add(new DefaultPort());
		Map resMap = getResourceMap(point);
		attributes.put(resourceCell, resMap);

		DefaultGraphCell typeCell = new TypeCell("");
		Point typePoint = new Point(point.x, point.y + cellHeight);
		Map typeMap = getTypeMap(typePoint);
		attributes.put(typeCell, typeMap);

		//		ParentMap parentMap = new ParentMap(); // 2.0系はこうだった
		ParentMap parentMap = new ParentMap(graph.getModel());
		DefaultGraphCell group = new DefaultGraphCell();
		parentMap.addEntry(resourceCell, group);
		parentMap.addEntry(typeCell, group);
		Object[] cells = new Object[] { resourceCell, typeCell, group };
		graph.getModel().insert(cells, attributes, null, parentMap, null);

		RDFResourceInfo info = new RDFResourceInfo(type, uri, typeCell);
		info.setTypeCell(resTypeCell);
		resInfoMap.putCellInfo(resourceCell, info);
		gmanager.changeCellView();
		gmanager.jumpRDFArea(resourceCell);
		
		return resourceCell;
	}

	public GraphCell insertClass(Point point, String uri, URIType uriType) {
		JGraph graph = gmanager.getClassGraph();
		point = graph.snap(new Point(point)); // Snap the Point to the Grid
		Map map = getClassMap(point);
		RDFSClassCell vertex = new RDFSClassCell(uri);

		setCell(graph, vertex, map);

		RDFSInfo info = new ClassInfo(uri, uriType);
		rdfsInfoMap.putCellInfo(vertex, info);
		gmanager.changeCellView();
		gmanager.jumpClassArea(vertex);
		return vertex;
	}

	public GraphCell insertProperty(Point point, String uri, URIType uriType) {
		JGraph graph = gmanager.getPropertyGraph();
		point = graph.snap(new Point(point)); // Snap the Point to the Grid
		Map map = getPropertyMap(point);
		RDFSPropertyCell vertex = new RDFSPropertyCell(uri);
		setCell(graph, vertex, map);

		RDFSInfo info = new PropertyInfo(uri, uriType);
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
