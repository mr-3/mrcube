/*
 * Project Name: MR^3 (Meta-Model Management based on RDFs Revision Reflection)
 * Project Website: http://mrcube.org/
 *
 * Copyright (C) 2003-2022 Takeshi Morita. All rights reserved.
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

package org.mrcube.utils;

import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.vocabulary.RDF;
import org.jgraph.JGraph;
import org.jgraph.graph.*;
import org.mrcube.MR3;
import org.mrcube.jgraph.*;
import org.mrcube.models.*;
import org.mrcube.models.MR3Constants.HistoryType;
import org.mrcube.models.MR3Constants.URIType;
import org.mrcube.views.HistoryManager;

import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.HashMap;
import java.util.Objects;
import java.util.Set;

/**
 * @author Takeshi Morita
 */
public class MR3CellMaker {

    private final GraphManager gmanager;

    public static int CELL_WIDTH = 100;
    public static int CELL_HEIGHT = 25;
    public static final int DEFAULT_CELL_WIDTH = 100;
    public static final int DEFAULT_CELL_HEIGHT = 25;
    public static final int CELL_MARGIN = 30;
    private static final Dimension initDimension = new Dimension(CELL_WIDTH, CELL_HEIGHT);
    private static final Rectangle initRectangle = new Rectangle(new Point(0, 0), initDimension);

    public MR3CellMaker(GraphManager manager) {
        gmanager = manager;
    }

    public AttributeMap getEdgeMap(Object info, Edge edge) {
        AttributeMap map = new AttributeMap();
        GraphConstants.setLineStyle(map, GraphConstants.STYLE_BEZIER);
        GraphConstants.setLineEnd(map, GraphConstants.ARROW_TECHNICAL);
        GraphConstants.setRouting(map, JGParallelEdgeRouter.getSharedInstance());
        if (GraphUtilities.defaultFont != null) {
            GraphConstants.setFont(map, GraphUtilities.defaultFont);
        }
        GraphConstants.setValue(edge.getAttributes(), Objects.requireNonNullElse(info, ""));
        if (GraphUtilities.isBlackAndWhite) {
            GraphConstants.setLineColor(map, GraphUtilities.graphForegroundColor);
            GraphConstants.setForeground(map, GraphUtilities.graphForegroundColor);
        } else {
            if (gmanager.getRDFGraph().isContains(edge)) {
                GraphConstants.setLineColor(map, InstancePropertyCell.borderColor);
                GraphConstants.setForeground(map, InstancePropertyCell.foregroundColor);
            } else {
                GraphConstants.setLineColor(map, Color.BLACK);
                GraphConstants.setForeground(map, Color.BLACK);
            }
        }
        return map;
    }

    private Rectangle2D getRDFNodeRectangle(Point2D point, String uri) {
        String value = gmanager.getRDFNodeValue(ResourceFactory.createResource(uri), null);
        Dimension dim = GraphUtilities.getAutoNodeDimension(gmanager, value);
        return new Rectangle2D.Double(point.getX(), point.getY(), dim.getWidth(), dim.getHeight());
    }

    private Rectangle2D getRDFSNodeRectangle(Point2D point, String uri) {
        String value = gmanager.getRDFSNodeValue(ResourceFactory.createResource(uri), null);
        Dimension dim = GraphUtilities.getAutoNodeDimension(gmanager, value);
        return new Rectangle2D.Double(point.getX(), point.getY(), dim.getWidth(), dim.getHeight());
    }

    private Rectangle2D getRectangle(Point2D point, String value) {
        Dimension size = GraphUtilities.getAutoLiteralNodeDimension(gmanager, value);
        return new Rectangle2D.Double(point.getX(), point.getY(), size.getWidth(), size.getHeight());
    }

    public Rectangle2D getRectangle(Point2D point) {
        return new Rectangle2D.Double(point.getX(), point.getY(), initDimension.getWidth(), initDimension.getHeight());
    }

    private AttributeMap getTypeMap(Rectangle2D rectangle) {
        AttributeMap map = new AttributeMap();

        if (GraphUtilities.defaultFont != null) {
            GraphConstants.setFont(map, GraphUtilities.defaultFont);
        }

        GraphConstants.setOpaque(map, false);
        if (GraphUtilities.isBlackAndWhite) {
            GraphConstants.setForeground(map, GraphUtilities.graphForegroundColor);
        } else {
            GraphConstants.setForeground(map, TypeViewCell.fontColor);
        }

        GraphConstants.setBounds(map, Objects.requireNonNullElseGet(rectangle, () -> new Rectangle(initRectangle)));

        return map;
    }

    public AttributeMap getResourceMap(Rectangle2D rectangle, Color cellColor) {
        AttributeMap map = new AttributeMap();
        if (GraphUtilities.isBlackAndWhite) {
            GraphConstants.setLineWidth(map, RDFCellStyleChanger.LINE_WIDTH);
            GraphConstants.setForeground(map, GraphUtilities.graphForegroundColor);
            GraphConstants.setBorderColor(map, GraphUtilities.graphForegroundColor);
            GraphConstants.setBackground(map, GraphUtilities.graphBackgroundColor);
            GraphConstants.setOpaque(map, false);
        } else {
            GraphConstants.setLineWidth(map, RDFCellStyleChanger.LINE_WIDTH);
            GraphConstants.setForeground(map, Color.white);
            GraphConstants.setBorderColor(map, cellColor);
            GraphConstants.setBackground(map, cellColor);
            GraphConstants.setFont(map, new Font("SansSerif", Font.PLAIN, RDFCellStyleChanger.FONT_SIZE));
            GraphConstants.setOpaque(map, true);
        }

        GraphConstants.setBounds(map, Objects.requireNonNullElseGet(rectangle, () -> new Rectangle(initRectangle)));
        return map;
    }

    public AttributeMap getLiteralMap(Rectangle2D rectangle, Color cellColor) {
        return getResourceMap(rectangle, cellColor);
    }

    public GraphCell insertRDFLiteral(Point pt, MR3Literal literal) {
        return insertRDFLiteral(getRectangle(pt, literal.getString()), literal);
    }

    public GraphCell insertRDFLiteral(Rectangle2D rect, MR3Literal literal) {
        JGraph graph = gmanager.getRDFGraph();
        AttributeMap map = getLiteralMap(rect, LiteralCell.backgroundColor);

        DefaultGraphCell vertex = new LiteralCell(literal);
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
        RDFGraph graph = gmanager.getRDFGraph();
        InstanceModel resInfo = (InstanceModel) GraphConstants.getValue(rdfCell.getAttributes());
        if (gmanager.isShowTypeCell()) {
            GraphCell typeViewCell = new TypeViewCell(resInfo.getTypeInfo());
            AttributeMap typeViewMap = getTypeMap(GraphUtilities.getTypeCellRectangle(rdfCell, resInfo.getTypeInfo(), gmanager));
            attributes.put(typeViewCell, typeViewMap);
            ParentMap parentMap = new ParentMap();
            DefaultGraphCell group = new DefaultGraphCell();
            parentMap.addEntry(rdfCell, group);
            parentMap.addEntry(typeViewCell, group);
            resInfo.setTypeViewCell(typeViewCell);
            graph.getGraphLayoutCache().insert(new Object[]{group}, attributes, null, parentMap);
        }
    }

    public GraphCell insertRDFResource(Point2D point, String uri, Object resTypeCell, URIType type) {
        JGraph graph = gmanager.getRDFGraph();
        AttributeMap attributes = new AttributeMap();
        point = graph.snap(new Point2D.Double(point.getX(), point.getY()));

        InstanceModel model = null;
        if (type == URIType.ANONYMOUS) {
            model = new InstanceModel(type, ResourceFactory.createResource().toString());
        } else {
            model = new InstanceModel(type, uri);
        }
        InstanceCell rdfCell = new InstanceCell(model);
        rdfCell.add(new DefaultPort());
        AttributeMap resMap = getResourceMap(getRDFNodeRectangle(point, uri), InstanceCell.backgroundColor);
        attributes.put(rdfCell, resMap);
        model.setTypeCell((GraphCell) resTypeCell, gmanager.getRDFGraph());
        GraphConstants.setValue(rdfCell.getAttributes(), model);
        graph.getGraphLayoutCache().insert(new Object[]{rdfCell}, attributes, null, null);
        GraphUtilities.resizeRDFResourceCell(gmanager, model, rdfCell);
        addTypeCell(rdfCell, attributes);

        gmanager.selectRDFCell(rdfCell);

        return rdfCell;
    }

    public Point2D calcInsertPoint(Set supCells) {
        boolean isFirst = true;
        double left = 50;
        double right = 50;
        double bottom = 0;
        if (supCells == null) {
            return new Point2D.Double((left + right) / 2, bottom + 100);
        }
        for (Object supCell : supCells) {
            GraphCell cell = (GraphCell) supCell;
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
        for (Object supCell : supCells) {
            if (RDFGraph.isPort(supCell)) {
                Port supPort = (Port) supCell;
                connect(subPort, supPort, null, graph);
            }
        }
    }

    public Edge connect(Port source, Port target, Object info, RDFGraph graph) {
        DefaultEdge edge = new InstancePropertyCell();
        ConnectionSet cs = new ConnectionSet(edge, source, target);
        AttributeMap attributes = new AttributeMap();
        AttributeMap map = getEdgeMap(info, edge);
        attributes.put(edge, map);
        graph.getGraphLayoutCache().insert(new Object[]{edge}, attributes, cs, null);
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
        DefaultEdge edge = new InstancePropertyCell(edgeName);
        ConnectionSet cs = new ConnectionSet(edge, port, port);
        AttributeMap attributes = new AttributeMap();

        GraphCell rdfsPropCell = null;
        Object[] rdfsPropertyCells = gmanager.getPropertyGraph().getSelectionCells();
        RDFSModel info = null;
        if (rdfsPropertyCells.length == 1 && RDFGraph.isRDFSPropertyCell(rdfsPropertyCells[0])) {
            rdfsPropCell = (GraphCell) rdfsPropertyCells[0];
            info = (RDFSModel) GraphConstants.getValue(rdfsPropCell.getAttributes());
            if (MR3.OFF_META_MODEL_MANAGEMENT) {
                PropertyModel pInfo = (PropertyModel) info;
                info = new PropertyModel(pInfo.getURIStr());
            }
        } else {
            info = new PropertyModel(MR3Resource.Nil.getURI());
        }
        AttributeMap map = getEdgeMap(info, edge);
        GraphConstants.setRouting(map, GraphConstants.ROUTING_SIMPLE);
        attributes.put(edge, map);
        graph.getGraphLayoutCache().insert(new Object[]{edge}, attributes, cs, null);
    }

    public DefaultGraphCell insertClass(Point2D point, String uri) {
        return insertClass(getRDFSNodeRectangle(point, uri), uri);
    }

    public DefaultGraphCell insertClass(Rectangle2D rectangle, String uri) {
        JGraph graph = gmanager.getClassGraph();
        rectangle.getBounds().setLocation((Point) graph.snap(rectangle.getBounds().getLocation()));
        AttributeMap map = getResourceMap(rectangle, OntClassCell.backgroundColor);
        RDFSModel info = new ClassModel(uri);
        info.setMetaClass(gmanager.getDefaultClassClass());
        OntClassCell vertex = new OntClassCell(info);
        setCell(graph, vertex, map);
        GraphConstants.setValue(vertex.getAttributes(), info);
        GraphUtilities.resizeRDFSResourceCell(gmanager, info, vertex);
        RDFSModelMap rdfsModelMap = gmanager.getRDFSInfoMap();
        rdfsModelMap.putURICellMap(info, vertex);

        return vertex;
    }

    public DefaultGraphCell insertProperty(Point2D point, String uri) {
        return insertProperty(getRDFSNodeRectangle(point, uri), uri);
    }

    public DefaultGraphCell insertProperty(Rectangle2D rectangle, String uri) {
        JGraph graph = gmanager.getPropertyGraph();
        rectangle.getBounds().setLocation((Point) graph.snap(rectangle.getBounds().getLocation()));
        AttributeMap map = getResourceMap(rectangle, OntPropertyCell.backgroundColor);

        PropertyModel info = new PropertyModel(uri);
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
        RDFSModelMap rdfsModelMap = gmanager.getRDFSInfoMap();
        rdfsModelMap.putURICellMap(info, vertex);

        return vertex;
    }

    private void setCell(JGraph graph, DefaultGraphCell cell, AttributeMap map) {
        cell.add(new DefaultPort());
        HashMap<Object, AttributeMap> attributes = new HashMap<>();
        attributes.put(cell, map);
        graph.getGraphLayoutCache().insert(new Object[]{cell}, attributes, null, null);
    }
}