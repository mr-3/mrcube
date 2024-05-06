/*
 * Project Name: MR^3 (Meta-Model Management based on RDFs Revision Reflection)
 * Project Website: https://mr-3.github.io/
 *
 * Copyright (C) 2003-2024 Takeshi Morita. All rights reserved.
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

package jp.ac.aoyama.it.ke.mrcube.layout;

import jp.ac.aoyama.it.ke.mrcube.MR3;
import jp.ac.aoyama.it.ke.mrcube.models.*;
import org.apache.jena.rdf.model.*;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.jgraph.graph.*;
import jp.ac.aoyama.it.ke.mrcube.jgraph.GraphManager;
import jp.ac.aoyama.it.ke.mrcube.jgraph.RDFGraph;
import jp.ac.aoyama.it.ke.mrcube.jgraph.InstancePropertyCell;
import jp.ac.aoyama.it.ke.mrcube.jgraph.InstanceCell;
import jp.ac.aoyama.it.ke.mrcube.models.MR3Resource;
import jp.ac.aoyama.it.ke.mrcube.models.InstanceModel;
import jp.ac.aoyama.it.ke.mrcube.models.RDFSModel;
import jp.ac.aoyama.it.ke.mrcube.models.RDFSModelMap;
import jp.ac.aoyama.it.ke.mrcube.utils.GraphUtilities;
import jp.ac.aoyama.it.ke.mrcube.utils.MR3CellMaker;
import jp.ac.aoyama.it.ke.mrcube.utils.Translator;

import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.*;

/**
 * @author Takeshi Morita
 */
public class GraphLayoutUtilities {

    private static GraphManager gmanager;

    public static final String UP_TO_DOWN = Translator.getString("OptionDialog.Layout.UpToDown");
    public static final String LEFT_TO_RIGHT = Translator.getString("OptionDialog.Layout.LeftToRight");

    public static final String VGJ_TREE_LAYOUT = "VGJ Tree Layout";
    public static final String JGRAPH_TREE_LAYOUT = "JGraph Tree Layout";

    public static final int VERTICAL_SPACE = 50;
    public static final int HORIZONTAL_SPACE = 80;
    public static int RDF_VERTICAL_SPACE = VERTICAL_SPACE;
    public static int RDF_HORIZONTAL_SPACE = HORIZONTAL_SPACE;
    public static int CLASS_VERTICAL_SPACE = VERTICAL_SPACE;
    public static int CLASS_HORIZONTAL_SPACE = HORIZONTAL_SPACE;
    public static int PROPERTY_VERTICAL_SPACE = VERTICAL_SPACE;
    public static int PROPERTY_HORIZONTAL_SPACE = HORIZONTAL_SPACE;

    public static String LAYOUT_TYPE = VGJ_TREE_LAYOUT;

    public static String RDF_LAYOUT_DIRECTION = LEFT_TO_RIGHT;
    public static String CLASS_LAYOUT_DIRECTION = LEFT_TO_RIGHT;
    public static String PROPERTY_LAYOUT_DIRECTION = LEFT_TO_RIGHT;

    public static void setGraphManager(GraphManager gm) {
        gmanager = gm;
    }

    public static char getVGJRDFLayoutDirection() {
        if (RDF_LAYOUT_DIRECTION.equals(UP_TO_DOWN)) {
            return 'u';
        }
        return 'r';
    }

    public static int getJGraphRDFLayoutDirection() {
        if (RDF_LAYOUT_DIRECTION.equals(UP_TO_DOWN)) {
            return 1;
        }
        return 0;
    }

    public static char getVGJClassLayoutDirection() {
        if (CLASS_LAYOUT_DIRECTION.equals(UP_TO_DOWN)) {
            return 'u';
        }
        return 'r';
    }

    public static int getJGraphClassLayoutDirection() {
        if (CLASS_LAYOUT_DIRECTION.equals(UP_TO_DOWN)) {
            return 1;
        }
        return 0;
    }

    public static char getVGJPropertyLayoutDirection() {
        if (PROPERTY_LAYOUT_DIRECTION.equals(UP_TO_DOWN)) {
            return 'u';
        }
        return 'r';
    }

    public static int getJGraphPropertyLayoutDirection() {
        if (PROPERTY_LAYOUT_DIRECTION.equals(UP_TO_DOWN)) {
            return 1;
        }
        return 0;
    }

    public static void reverseArc(MR3CellMaker cellMaker, RDFGraph graph) {
        Set<Edge> removeEdges = new HashSet<>();
        for (Object cell : graph.getAllCells()) {
            if (RDFGraph.isEdge(cell)) {
                Edge edge = (Edge) cell;
                removeEdges.add(edge);
                Object info = GraphConstants.getValue(edge.getAttributes());
                cellMaker.connect((Port) graph.getModel().getTarget(edge), (Port) graph.getModel().getSource(edge), info, graph);
            }
        }
        graph.getModel().remove(removeEdges.toArray());
    }

    public static void addChild(Model model, GraphLayoutData data, Map<RDFNode, GraphLayoutData> cellLayoutMap) {
        for (Statement stmt : model.listStatements().toList()) {
            RDFNode source = stmt.getSubject();
            RDFNode target = stmt.getObject();

            // 同一文字列のリテラルが同一オブジェクトになるため
            if (target instanceof Literal) {
                target = ResourceFactory.createResource(source.toString() + target.hashCode());
                // System.out.println(target);
            }

            if (stmt.getPredicate().equals(RDF.type)) {
                continue;
            }

            if (source.equals(data.getRDFNode()) && target != data.getRDFNode()) {
                data.addChild(cellLayoutMap.get(target));
            }
            if (target.equals(data.getRDFNode()) && !target.equals(source)) {
                data.setHasParent(true);
            }
        }
    }

    public static void addChild(RDFGraph graph, DefaultGraphCell cell, GraphLayoutData data,
                                Map<Object, GraphLayoutData> cellLayoutMap) {
        Port port = (Port) cell.getChildAt(0);

        for (Iterator i = port.edges(); i.hasNext(); ) {
            Edge edge = (Edge) i.next();
            GraphCell sourceCell = (GraphCell) graph.getSourceVertex(edge);
            GraphCell targetCell = (GraphCell) graph.getTargetVertex(edge);

            if (targetCell != cell) { // 自身がTargetの場合
                data.addChild(cellLayoutMap.get(targetCell));
            }
            if (targetCell == cell && targetCell != sourceCell) {
                data.setHasParent(true);
            }
        }
    }

    // _1.._numは ， グラフに描画しない
    private static boolean isNumProperty(Resource resource) {
        return resource.toString().matches(RDF.getURI() + "_\\d*");
    }

    public static void initPropertyGraphLayoutData(Map<RDFNode, GraphLayoutData> cellLayoutMap) {
        duplicateResourceSet = new HashSet<>();
        Dimension dim = GraphUtilities.getAutoNodeDimension(gmanager, gmanager.getRDFSNodeValue(MR3Resource.Property,
                null));
        GraphLayoutData rootData = new GraphLayoutData(MR3Resource.Property, dim);
        rootData.setHasParent(false);
        cellLayoutMap.put(MR3Resource.Property, rootData);
        RDFSModelMap rdfsModelMap = gmanager.getRDFSInfoMap();
        for (Resource property : rdfsModelMap.getRootProperties()) {
            if (!isNumProperty(property)) {
                GraphLayoutData childData = cellLayoutMap.get(property);
                if (childData == null) {
                    dim = GraphUtilities.getAutoNodeDimension(gmanager, gmanager.getRDFSNodeValue(property, null));
                    childData = new GraphLayoutData(property, dim);
                    childData.setHasParent(true);
                    cellLayoutMap.put(property, childData);
                }
                rootData.addChild(childData);
                RDFSModel info = rdfsModelMap.getResourceInfo(property);
                if (info.getRDFSSubList().size() > 0) {
                    initRDFSGraphLayoutData(cellLayoutMap, info, childData);
                }
            }
        }
    }

    private static Set<Resource> duplicateResourceSet;

    public static void initClassGraphLayoutData(Map<RDFNode, GraphLayoutData> cellLayoutMap) {
        duplicateResourceSet = new HashSet<>();
        RDFSModelMap rdfsModelMap = gmanager.getRDFSInfoMap();
        RDFSModel rootInfo = rdfsModelMap.getResourceInfo(RDFS.Resource);
        if (rootInfo != null && rootInfo.getRDFSSubList().size() > 0) {
            Dimension dim = GraphUtilities.getAutoNodeDimension(gmanager, gmanager
                    .getRDFSNodeValue(RDFS.Resource, null));
            GraphLayoutData rootData = new GraphLayoutData(RDFS.Resource, dim);
            rootData.setHasParent(false);
            cellLayoutMap.put(RDFS.Resource, rootData);
            initRDFSGraphLayoutData(cellLayoutMap, rootInfo, rootData);
        }
    }

    private static void initRDFSGraphLayoutData(Map<RDFNode, GraphLayoutData> cellLayoutMap, RDFSModel supInfo,
                                                GraphLayoutData parentData) {
        for (Resource resource : supInfo.getRDFSSubList()) {
            RDFSModelMap rdfsModelMap = gmanager.getRDFSInfoMap();
            if (!isNumProperty(resource)) {
                GraphLayoutData childData = cellLayoutMap.get(resource);
                if (childData == null) {
                    Dimension dim = GraphUtilities.getAutoNodeDimension(gmanager, gmanager.getRDFSNodeValue(resource,
                            null));
                    childData = new GraphLayoutData(resource, dim);
                    childData.setHasParent(true);
                    cellLayoutMap.put(resource, childData);
                }
                parentData.addChild(childData);
                RDFSModel subInfo = rdfsModelMap.getResourceInfo(resource);
                if (!duplicateResourceSet.contains(resource)) {
                    duplicateResourceSet.add(resource);
                } else {
                    continue;
                }
                if (subInfo.getRDFSSubList().size() > 0) {
                    initRDFSGraphLayoutData(cellLayoutMap, subInfo, childData);
                }
            }
        }
    }

    public static Set<GraphLayoutData> initGraphLayoutData(Model model, Map<RDFNode, GraphLayoutData> cellLayoutMap) {
        GraphLayoutData data = null;
        Set<RDFNode> nodeSet = new HashSet<>();
        Set<GraphLayoutData> dataSet = new HashSet<>();

        for (Statement stmt : model.listStatements().toList()) {
            RDFNode rdfNode = stmt.getSubject();
            if (!nodeSet.contains(rdfNode)) {
                Dimension dim = GraphUtilities.getAutoNodeDimension(gmanager, gmanager.getRDFNodeValue(
                        (Resource) rdfNode, null));
                data = new GraphLayoutData(rdfNode, dim);
                cellLayoutMap.put(rdfNode, data);
                dataSet.add(data);
                nodeSet.add(rdfNode);
            }

            rdfNode = stmt.getObject();
            if (!nodeSet.contains(rdfNode) && !stmt.getPredicate().equals(RDF.type)) {
                if (rdfNode instanceof Literal) {
                    Dimension dim = GraphUtilities.getAutoLiteralNodeDimension(gmanager, rdfNode.toString());
                    // 同じ文字列のリテラルをレイアウトするための処理.
                    // getSubject().getURIでは無名ノードの処理ができない
                    rdfNode = ResourceFactory.createResource(stmt.getSubject().toString() + rdfNode.hashCode());
                    // System.out.println(rdfNode);
                    data = new GraphLayoutData(rdfNode, dim);
                } else {
                    Dimension dim = GraphUtilities.getAutoNodeDimension(gmanager, gmanager.getRDFNodeValue(
                            (Resource) rdfNode, null));
                    data = new GraphLayoutData(rdfNode, dim);
                }
                cellLayoutMap.put(rdfNode, data);
                dataSet.add(data);
                nodeSet.add(rdfNode);
            }
        }

        return dataSet;
    }

    public static Set<GraphLayoutData> initGraphLayoutData(RDFGraph graph, Map<Object, GraphLayoutData> cellLayoutMap) {
        Object[] cells = graph.getAllCells();
        Set<GraphLayoutData> dataSet = new HashSet<>();
        for (Object cell1 : cells) {
            GraphCell cell = (GraphCell) cell1;
            if (!RDFGraph.isTypeCell(cell)
                    && (RDFGraph.isRDFSCell(cell) || RDFGraph.isRDFResourceCell(cell) || RDFGraph
                    .isRDFLiteralCell(cell))) {
                GraphLayoutData data = new GraphLayoutData(cell, graph);
                cellLayoutMap.put(cell, data);
                dataSet.add(data);
            }
        }
        return dataSet;
    }

    public static RDFNode collectRoot(Model model, Set<RDFNode> rootNodes, Set<GraphLayoutData> dataSet,
                                      Map<RDFNode, GraphLayoutData> cellLayoutMap) {
        Resource rootNode = ResourceFactory.createResource();
        GraphLayoutData rootData = new GraphLayoutData(rootNode);
        rootData.setHasParent(false);

        for (RDFNode node : rootNodes) {
            model.add(rootNode, MR3Resource.Nil, node);
            GraphLayoutData data = cellLayoutMap.get(node);
            data.setHasParent(true);
            rootData.addChild(data);
        }
        dataSet.add(rootData);
        cellLayoutMap.put(rootNode, rootData);

        return rootNode;
    }

    private static final Point initPoint = new Point(-50, -50);

    public static Object collectRoot(RDFGraph graph, MR3CellMaker cellMaker, Set<DefaultGraphCell> rootCells,
                                     Set<GraphLayoutData> dataSet, Map<Object, GraphLayoutData> cellLayoutMap) {
        InstanceModel rootInfo = new InstanceModel(MR3Constants.URIType.ANONYMOUS, new AnonId().toString());
        DefaultGraphCell rootCell = new InstanceCell(rootInfo);
        DefaultPort rootPort = new DefaultPort();
        rootCell.add(rootPort);

        Map attributes = new HashMap();
        attributes.put(rootCell, cellMaker.getResourceMap(cellMaker.getRectangle(initPoint), InstanceCell.backgroundColor));
        graph.getGraphLayoutCache().insert(new Object[]{rootCell}, attributes, null, null);
        GraphLayoutData rootData = new GraphLayoutData(rootCell, graph);
        rootData.setHasParent(false);

        for (DefaultGraphCell cell : rootCells) {
            Port port = (Port) cell.getChildAt(0);
            DefaultEdge edge = new InstancePropertyCell("");
            ConnectionSet cs = new ConnectionSet(edge, rootPort, port);
            graph.getGraphLayoutCache().insert(new Object[]{edge}, null, cs, null);
            GraphLayoutData data = cellLayoutMap.get(cell);
            data.setHasParent(true);
            rootData.addChild(data);
        }

        dataSet.add(rootData);
        cellLayoutMap.put(rootCell, rootData);

        return rootCell;
    }

    public static void removeTemporaryRoot(Model model, RDFNode tmpRoot) {
        if (tmpRoot == null) {
            return;
        }
        Model removeModel = ModelFactory.createDefaultModel();
        for (Statement stmt : model.listStatements().toList()) {
            RDFNode subject = stmt.getSubject();
            if (subject.equals(tmpRoot)) {
                removeModel.add(stmt);
            }
        }
        model.remove(removeModel);
    }

    public static void removeTemporaryRoot(RDFGraph graph, DefaultGraphCell tmpRoot) {
        if (tmpRoot == null) {
            return;
        }
        Set<Object> removeCellsSet = new HashSet<>();
        Port port = (Port) tmpRoot.getChildAt(0);
        removeCellsSet.add(tmpRoot);
        removeCellsSet.add(port);
        for (Iterator edges = graph.getModel().edges(port); edges.hasNext(); ) {
            removeCellsSet.add(edges.next());
        }
        graph.getModel().remove(removeCellsSet.toArray());
    }

    private static Point2D.Double getStartPoint(Collection<GraphLayoutData> dataSet) {
        Point2D.Double startPoint = null;
        for (GraphLayoutData data : dataSet) {
            Point2D.Double dataPoint = data.getPosition();
            if (startPoint == null) {
                startPoint = new Point2D.Double(dataPoint.x, dataPoint.y);
            }
            if (dataPoint.x < startPoint.x) {
                startPoint.x = dataPoint.x;
            }
            if (dataPoint.y < startPoint.y) {
                startPoint.y = dataPoint.y;
            }
        }
        return startPoint;
    }

    private static void setRevisePoint(Point2D.Double revisePoint, Point2D.Double startPoint) {
        int MARGIN = 50;
        if (startPoint.x <= 0) {
            revisePoint.x = (-startPoint.x) + MARGIN;
        } else if (MARGIN < startPoint.x) {
            revisePoint.x = MARGIN - startPoint.x;
        }
        if (startPoint.y <= 0) {
            revisePoint.y = (-startPoint.y) + MARGIN;
        } else if (MARGIN < startPoint.y) {
            revisePoint.y = MARGIN - startPoint.y;
        }
    }

    public static void centralizeGraph(Collection<GraphLayoutData> dataSet) {
        if (dataSet.size() == 0) {
            return;
        }
        Point2D.Double startPoint = getStartPoint(dataSet);
        Point2D.Double revisePoint = new Point2D.Double(0, 0);
        setRevisePoint(revisePoint, startPoint);
        for (GraphLayoutData data : dataSet) {
            Point2D.Double point = data.getPosition();
            data.setPosition(point.x + revisePoint.x, point.y + revisePoint.y);
        }
    }

    public static void centralizeGraph(RDFGraph graph) {
        int MARGIN = 50;
        Object[] cells = graph.getAllCells();
        if (cells.length == 0) {
            return;
        }
        Rectangle2D rec = graph.getCellBounds(cells);

        double reviseX = 0;
        double reviseY = 0;
        if (rec.getX() <= 0) {
            reviseX = (-rec.getX()) + MARGIN;
        } else if (MARGIN < rec.getX()) {
            reviseX = MARGIN - rec.getX();
        }
        if (rec.getY() <= 0) {
            reviseY = (-rec.getY()) + MARGIN;
        } else if (MARGIN < rec.getY()) {
            reviseY = MARGIN - rec.getY();
        }
        MR3.STATUS_BAR.initNormal(cells.length);
        for (Object cell1 : cells) {
            if (RDFGraph.isRDFsCell(cell1) || RDFGraph.isTypeCell(cell1)) {
                GraphCell cell = (GraphCell) cell1;
                AttributeMap map = cell.getAttributes();
                Rectangle2D cellRec = GraphConstants.getBounds(map);
                if (cellRec != null) {
                    cellRec.setRect(cellRec.getX() + reviseX, cellRec.getY() + reviseY, cellRec.getWidth(), cellRec
                            .getHeight());
                    GraphConstants.setBounds(map, cellRec);
                    GraphUtilities.editCell(cell, map, graph);
                }
            }
            MR3.STATUS_BAR.addValue();
        }
        MR3.STATUS_BAR.hideProgressBar();
    }
}