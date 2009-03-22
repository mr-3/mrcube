/*
 * Project Name: MR^3 (Meta-Model Management based on RDFs Revision Reflection)
 * Project Website: http://mr3.sourceforge.net/
 * 
 * Copyright (C) 2003-2008 Yamaguchi Laboratory, Keio University. All rights reserved. 
 * 
 * This file is part of MR^3.
 * 
 * MR^3 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * MR^3 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with MR^3.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */

package org.semanticweb.mmm.mr3.util;

import java.awt.*;
import java.awt.geom.*;
import java.util.*;

import org.jgraph.*;
import org.jgraph.graph.*;
import org.semanticweb.mmm.mr3.*;
import org.semanticweb.mmm.mr3.data.*;
import org.semanticweb.mmm.mr3.data.MR3Constants.*;
import org.semanticweb.mmm.mr3.jgraph.*;
import org.semanticweb.mmm.mr3.ui.*;

import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.vocabulary.*;

/**
 * 
 * @author takeshi morita
 * 
 */
public class MR3CellMaker {

    private GraphManager gmanager;

    public static final int CELL_MARGIN = 10;
    public static int CELL_WIDTH = 100;
    public static int DEFAULT_CELL_WIDTH = 100;
    public static int CELL_HEIGHT = 25;
    public static int DEFAULT_CELL_HEIGHT = 25;
    private static final Dimension initDimension = new Dimension(CELL_WIDTH, CELL_HEIGHT);
    private static final Rectangle initRectangle = new Rectangle(new Point(0, 0), initDimension);

    public MR3CellMaker(GraphManager manager) {
        gmanager = manager;
    }

    public AttributeMap getEdgeMap(Object info, Edge edge) {
        AttributeMap map = new AttributeMap();
        // GraphConstants.setRouting(map,
        // JGraphParallelEdgeRouter.getSharedInstance());
        GraphConstants.setRouting(map, JGParallelEdgeRouter.getSharedInstance());
        GraphConstants.setLineStyle(map, GraphConstants.STYLE_BEZIER);
        GraphConstants.setLineEnd(map, GraphConstants.ARROW_TECHNICAL);
        GraphConstants.setLineColor(map, RDFPropertyCell.rdfPropertyColor);
        if (GraphUtilities.defaultFont != null) {
            GraphConstants.setFont(map, GraphUtilities.defaultFont);
        }
        if (info != null) {
            GraphConstants.setValue(edge.getAttributes(), info);
        } else {
            GraphConstants.setValue(edge.getAttributes(), "");
        }
        return map;
    }

    public Rectangle2D getRDFNodeRectangle(Point2D point, String uri) {
        String value = gmanager.getRDFNodeValue(ResourceFactory.createResource(uri), null);
        Dimension dim = GraphUtilities.getAutoNodeDimension(gmanager, value);
        return new Rectangle2D.Double(point.getX(), point.getY(), dim.getWidth(), dim.getHeight());
    }

    public Rectangle2D getRDFSNodeRectangle(Point2D point, String uri) {
        String value = gmanager.getRDFSNodeValue(ResourceFactory.createResource(uri), null);
        Dimension dim = GraphUtilities.getAutoNodeDimension(gmanager, value);
        return new Rectangle2D.Double(point.getX(), point.getY(), dim.getWidth(), dim.getHeight());
    }

    public Rectangle2D getRectangle(Point2D point, String value) {
        Dimension size = GraphUtilities.getAutoLiteralNodeDimention(gmanager, value);
        return new Rectangle2D.Double(point.getX(), point.getY(), size.getWidth(), size.getHeight());
    }

    public Rectangle2D getRectangle(Point2D point) {
        return new Rectangle2D.Double(point.getX(), point.getY(), initDimension.getWidth(), initDimension.getHeight());
    }

    public AttributeMap getTypeMap(Rectangle2D rectangle) {
        AttributeMap map = new AttributeMap();

        if (GraphUtilities.defaultFont != null) {
            GraphConstants.setFont(map, GraphUtilities.defaultFont);
        }

        GraphConstants.setOpaque(map, false);
        GraphConstants.setForeground(map, Color.blue);

        if (gmanager.isAutoNodeSize()) {
            GraphConstants.setAutoSize(map, true);
        }
        if (rectangle != null) {
            GraphConstants.setBounds(map, rectangle);
        } else {
            GraphConstants.setBounds(map, new Rectangle(initRectangle));
        }

        return map;
    }

    public AttributeMap getResourceMap(Rectangle2D rectangle, Color cellColor) {
        AttributeMap map = new AttributeMap();

        if (GraphUtilities.defaultFont != null) {
            GraphConstants.setFont(map, GraphUtilities.defaultFont);
        }
        if (GraphUtilities.isColor) {
            GraphConstants.setBackground(map, cellColor);
            GraphConstants.setOpaque(map, true);
        } else {
            GraphConstants.setOpaque(map, false);
        }

        GraphConstants.setBorderColor(map, Color.black);
        GraphConstants.setLineWidth(map, 1);

        if (gmanager.isAutoNodeSize()) {
            GraphConstants.setAutoSize(map, true);
        }
        if (rectangle != null) {
            GraphConstants.setBounds(map, rectangle);
        } else {
            GraphConstants.setBounds(map, new Rectangle(initRectangle));
        }
        return map;
    }

    public GraphCell insertRDFLiteral(Point pt, MR3Literal literal) {
        return insertRDFLiteral(getRectangle(pt, literal.getString()), literal);
    }

    public GraphCell insertRDFLiteral(Rectangle2D rect, MR3Literal literal) {
        JGraph graph = gmanager.getCurrentRDFGraph();
        AttributeMap map = getResourceMap(rect, RDFLiteralCell.literalColor);

        DefaultGraphCell vertex = new RDFLiteralCell(literal);
        setCell(graph, vertex, map);
        if (literal == null) {
            GraphConstants.setValue(vertex.getAttributes(), Utilities.createLiteral("", "", null));
        } else {
            GraphConstants.setValue(vertex.getAttributes(), literal);
        }
        gmanager.selectRDFCell(vertex);
        return vertex;
    }

    public void addTypeCell(GraphCell rdfCell, AttributeMap attributes) {
        RDFGraph graph = gmanager.getCurrentRDFGraph();
        GraphCell typeViewCell = null;
        RDFResourceInfo resInfo = (RDFResourceInfo) GraphConstants.getValue(rdfCell.getAttributes());
        if (gmanager.isShowTypeCell()) {
            typeViewCell = new TypeViewCell(resInfo.getTypeInfo());
            AttributeMap typeViewMap = getTypeMap(GraphUtilities.getTypeCellRectangle(rdfCell, resInfo.getTypeInfo(),
                    gmanager));
            attributes.put(typeViewCell, typeViewMap);

            ParentMap parentMap = new ParentMap();
            DefaultGraphCell group = new DefaultGraphCell();            
            parentMap.addEntry(rdfCell, group);
            parentMap.addEntry(typeViewCell, group);
            graph.getGraphLayoutCache().insert(new Object[] { group}, attributes, null, parentMap);
            resInfo.setTypeViewCell(typeViewCell);
        }
    }

    public GraphCell insertRDFResource(Point2D point, String uri, Object resTypeCell, URIType type) {
        JGraph graph = gmanager.getCurrentRDFGraph();
        AttributeMap attributes = new AttributeMap();
        point = graph.snap(new Point2D.Double(point.getX(), point.getY()));

        RDFResourceInfo info = null;
        if (type == URIType.ANONYMOUS) {
            info = new RDFResourceInfo(type, ResourceFactory.createResource().toString());
        } else {
            info = new RDFResourceInfo(type, uri);
        }
        RDFResourceCell rdfCell = new RDFResourceCell(info);
        rdfCell.add(new DefaultPort());
        AttributeMap resMap = getResourceMap(getRDFNodeRectangle(point, uri), RDFResourceCell.rdfResourceColor);
        attributes.put(rdfCell, resMap);
        info.setTypeCell((GraphCell) resTypeCell, gmanager.getCurrentRDFGraph());
        GraphConstants.setValue(rdfCell.getAttributes(), info);
        graph.getGraphLayoutCache().insert(new Object[] { rdfCell}, attributes, null, null);
        GraphUtilities.resizeRDFResourceCell(gmanager, info, rdfCell);
        addTypeCell(rdfCell, attributes);

        gmanager.selectRDFCell(rdfCell);

        return rdfCell;
    }

    public Point2D calcInsertPoint(Set supCells) {
        boolean isFirst = true;
        double left = 50;
        double right = 50;
        double bottom = 0;
        if (supCells == null) { return new Point2D.Double((left + right) / 2, bottom + 100); }
        for (Iterator i = supCells.iterator(); i.hasNext();) {
            GraphCell cell = (GraphCell) i.next();
            AttributeMap map = cell.getAttributes();
            Rectangle2D rec = GraphConstants.getBounds(map);
            if (isFirst) {
                right = rec.getX();
                left = rec.getX();
                bottom = rec.getY();
                isFirst = false;
            } else {
                if (right < rec.getX()) {
                    right = rec.getX();
                }
                if (left > rec.getX()) {
                    left = rec.getX();
                }
                if (bottom < rec.getY()) {
                    bottom = rec.getY();
                }
            }
        }
        return new Point2D.Double((left + right) / 2, bottom + 100);
    }

    public void connectSubToSups(Port subPort, Object[] supCells, RDFGraph graph) {
        for (int i = 0; i < supCells.length; i++) {
            if (RDFGraph.isPort(supCells[i])) {
                Port supPort = (Port) supCells[i];
                connect(subPort, supPort, null, graph);
            }
        }
    }

    public Edge connect(Port source, Port target, Object info, RDFGraph graph) {
        DefaultEdge edge = new RDFPropertyCell();
        ConnectionSet cs = new ConnectionSet(edge, source, target);
        AttributeMap attributes = new AttributeMap();
        AttributeMap map = getEdgeMap(info, edge);
        attributes.put(edge, map);
        graph.getGraphLayoutCache().insert(new Object[] { edge}, attributes, cs, null);
        graph.getGraphLayoutCache().reload();

        if (gmanager.isClassGraph(graph)) {
            HistoryManager.saveHistory(HistoryType.CONNECT_SUP_SUB_CLASS, null, (GraphCell) graph.getModel().getParent(
                    source), (GraphCell) graph.getModel().getParent(target));
        } else if (gmanager.isPropertyGraph(graph)) {
            HistoryManager.saveHistory(HistoryType.CONNECT_SUP_SUB_PROPERTY, null, (GraphCell) graph.getModel()
                    .getParent(source), (GraphCell) graph.getModel().getParent(target));
        }

        return edge;
    }

    public void selfConnect(Port port, String edgeName, RDFGraph graph) {
        DefaultEdge edge = new RDFPropertyCell(edgeName);
        ConnectionSet cs = new ConnectionSet(edge, port, port);
        AttributeMap attributes = new AttributeMap();

        GraphCell rdfsPropCell = null;
        Object[] rdfsPropertyCells = gmanager.getCurrentPropertyGraph().getSelectionCells();
        RDFSInfo info = null;
        if (rdfsPropertyCells.length == 1 && RDFGraph.isRDFSPropertyCell(rdfsPropertyCells[0])) {
            rdfsPropCell = (GraphCell) rdfsPropertyCells[0];
            info = (RDFSInfo) GraphConstants.getValue(rdfsPropCell.getAttributes());
            if (MR3.OFF_META_MODEL_MANAGEMENT) {
                PropertyInfo pInfo = (PropertyInfo) info;
                info = new PropertyInfo(pInfo.getURIStr());
            }
        } else {
            info = new PropertyInfo(MR3Resource.Nil.getURI());
        }
        AttributeMap map = getEdgeMap(info, edge);
        GraphConstants.setRouting(map, GraphConstants.ROUTING_SIMPLE);
        attributes.put(edge, map);
        // graph.getGraphLayoutCache().insert(new Object[] { edge}, attributes,
        // cs, null, null);
        graph.getGraphLayoutCache().insert(new Object[] { edge}, attributes, cs, null);
    }

    public DefaultGraphCell insertClass(Point2D point, String uri) {
        return insertClass(getRDFSNodeRectangle(point, uri), uri);
    }

    public DefaultGraphCell insertClass(Rectangle2D rectangle, String uri) {
        JGraph graph = gmanager.getCurrentClassGraph();
        rectangle.getBounds().setLocation((Point) graph.snap(rectangle.getBounds().getLocation()));
        AttributeMap map = getResourceMap(rectangle, OntClassCell.classColor);
        RDFSInfo info = new ClassInfo(uri);
        info.setMetaClass(gmanager.getDefaultClassClass());
        OntClassCell vertex = new OntClassCell(info);
        setCell(graph, vertex, map);
        GraphConstants.setValue(vertex.getAttributes(), info);
        GraphUtilities.resizeRDFSResourceCell(gmanager, info, vertex);
        RDFSInfoMap rdfsInfoMap = gmanager.getCurrentRDFSInfoMap();
        rdfsInfoMap.putURICellMap(info, vertex);

        return vertex;
    }

    public DefaultGraphCell insertProperty(Point2D point, String uri) {
        return insertProperty(getRDFSNodeRectangle(point, uri), uri);
    }

    public DefaultGraphCell insertProperty(Rectangle2D rectangle, String uri) {
        JGraph graph = gmanager.getCurrentPropertyGraph();
        rectangle.getBounds().setLocation((Point) graph.snap(rectangle.getBounds().getLocation()));
        AttributeMap map = getResourceMap(rectangle, OntPropertyCell.propertyColor);

        PropertyInfo info = new PropertyInfo(uri);
        info.setMetaClass(gmanager.getDefaultPropertyClass());
        OntPropertyCell vertex = new OntPropertyCell(info);
        if (uri.matches(RDF.getURI() + "_\\d*")) {
            info.setContainer(true);
            info.setNum(Integer.parseInt(uri.split("_")[1]));
        } else {
            setCell(graph, vertex, map);
        }
        GraphConstants.setValue(vertex.getAttributes(), info);
        GraphUtilities.resizeRDFSResourceCell(gmanager, info, vertex);
        RDFSInfoMap rdfsInfoMap = gmanager.getCurrentRDFSInfoMap();
        rdfsInfoMap.putURICellMap(info, vertex);

        return vertex;
    }

    private void setCell(JGraph graph, DefaultGraphCell cell, AttributeMap map) {
        cell.add(new DefaultPort());
        HashMap<Object, AttributeMap> attributes = new HashMap<Object, AttributeMap>();
        attributes.put(cell, map);
        // graph.getGraphLayoutCache().insert(new Object[] { cell}, attributes,
        // null, null, null);
        graph.getGraphLayoutCache().insert(new Object[] { cell}, attributes, null, null);
    }
}