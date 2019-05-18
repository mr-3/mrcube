/*
 * Project Name: MR^3 (Meta-Model Management based on RDFs Revision Reflection)
 * Project Website: http://mrcube.org/
 *
 * Copyright (C) 2003-2019 Yamaguchi Laboratory, Keio University. All rights reserved.
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

package org.mrcube.io;

import org.apache.jena.rdf.model.*;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.jgraph.graph.*;
import org.mrcube.MR3;
import org.mrcube.jgraph.*;
import org.mrcube.layout.GraphLayoutData;
import org.mrcube.layout.VGJTreeLayout;
import org.mrcube.models.*;
import org.mrcube.models.MR3Constants.URIType;
import org.mrcube.utils.MR3CellMaker;
import org.mrcube.utils.ProjectManager;

import java.awt.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/*
 *
 * @author Takeshi Morita
 *
 */
public class MR3Parser {

    private final MR3CellMaker cellMaker;
    private final GraphManager gmanager;

    public MR3Parser(GraphManager manager) {
        gmanager = manager;
        cellMaker = new MR3CellMaker(gmanager);
    }

    private Set<RDFSModel> duplicateSubInfo;

    public void createClassGraph(Map<RDFNode, GraphLayoutData> cellLayoutMap) {
        duplicateSubInfo = new HashSet<>();
        RDFGraph graph = gmanager.getClassGraph();
        graph.removeEdges();
        DefaultGraphCell rootCell = (DefaultGraphCell) gmanager.getClassCell(RDFS.Resource, cellLayoutMap);
        Port rootPort = (Port) rootCell.getChildAt(0);

        RDFSModelMap rdfsModelMap = gmanager.getRDFSInfoMap();
        ClassModel rootInfo = (ClassModel) rdfsModelMap.getResourceInfo(RDFS.Resource);
        if (rootInfo != null) {
            GraphConstants.setValue(rootCell.getAttributes(), rootInfo);
            rdfsModelMap.putURICellMap(rootInfo, rootCell);
            rootInfo.setURI(RDFS.Resource.getURI());
            if (rootInfo.getRDFSSubList().size() > 0) {
                createRDFSGraph(graph.getGraphLayoutCache(), rootInfo, rootCell, rootPort, cellLayoutMap);
            }
        }
    }

    public void createPropertyGraph(Map<RDFNode, GraphLayoutData> cellLayoutMap) {
        RDFSModelMap rdfsModelMap = gmanager.getRDFSInfoMap();
        duplicateSubInfo = new HashSet<>();
        RDFGraph graph = gmanager.getPropertyGraph();
        graph.removeEdges();

        DefaultGraphCell rootCell = (DefaultGraphCell) gmanager.getPropertyCell(
                MR3Resource.Property, cellLayoutMap);
        Port rootPort = (Port) rootCell.getChildAt(0);

        Map<Object, AttributeMap> attributes = new HashMap<>();
        for (Resource property : rdfsModelMap.getRootProperties()) {

            // _1.._numは ， グラフに描画しない
            if (property.getURI().matches(RDF.getURI() + "_\\d*")) {
                continue;
            }

            PropertyModel info = (PropertyModel) rdfsModelMap.getResourceInfo(property);
            DefaultGraphCell pCell = (DefaultGraphCell) gmanager.getPropertyCell(property,
                    cellLayoutMap);
            Port pPort = (Port) pCell.getChildAt(0);
            GraphConstants.setValue(pCell.getAttributes(), info);
            rdfsModelMap.putURICellMap(info, pCell);

            Edge edge = getEdge(attributes, null);
            ConnectionSet cs = new ConnectionSet(edge, pPort, rootPort);
            graph.getGraphLayoutCache().insert(new Object[]{edge}, attributes, cs, null);

            if (info.getRDFSSubList().size() > 0) {
                createRDFSGraph(graph.getGraphLayoutCache(), info, pCell, pPort, cellLayoutMap);
            }
        }
    }

    /**
     * graphModel -> class or property
     */
    private void createRDFSGraph(GraphLayoutCache graphLayoutCache, RDFSModel supInfo,
                                 GraphCell supCell, Port supPort, Map<RDFNode, GraphLayoutData> cellLayoutMap) {
        Map<Object, AttributeMap> attributes = new HashMap<>();
        RDFSModelMap rdfsModelMap = gmanager.getRDFSInfoMap();
        for (Resource subRes : supInfo.getRDFSSubList()) {
            RDFSModel subInfo = rdfsModelMap.getResourceInfo(subRes);

            DefaultGraphCell subCell = null;
            if (supInfo instanceof ClassModel) {
                subCell = (DefaultGraphCell) gmanager.getClassCell(subRes, cellLayoutMap);
            } else if (subInfo instanceof PropertyModel) {
                subCell = (DefaultGraphCell) gmanager.getPropertyCell(subRes, cellLayoutMap);
            }

            Port subPort = (Port) subCell.getChildAt(0);
            GraphConstants.setValue(subCell.getAttributes(), subInfo);
            rdfsModelMap.putURICellMap(subInfo, subCell);
            subInfo.addSupRDFS(supCell);

            Edge edge = getEdge(attributes, null);
            ConnectionSet cs = new ConnectionSet(edge, subPort, supPort);
            graphLayoutCache.insert(new Object[]{edge}, attributes, cs, null);

            if (!duplicateSubInfo.contains(subInfo)) {
                duplicateSubInfo.add(subInfo);
            } else {
                continue;
            }

            if (subInfo.getRDFSSubList().size() > 0) {
                createRDFSGraph(graphLayoutCache, subInfo, subCell, subPort, cellLayoutMap);
            }
        }
    }

    private Edge getEdge(Map<Object, AttributeMap> attributes, Resource predicate) {
        if (predicate == null) {
            DefaultEdge edge = new RDFPropertyCell("");
            AttributeMap edgeMap = cellMaker.getEdgeMap(null, edge);
            attributes.put(edge, edgeMap);
            return edge;
        }
        RDFSModel rdfsModel = null;
        if (predicate.equals(MR3Resource.Nil)) {
            rdfsModel = new PropertyModel(MR3Resource.Nil.getURI());
        } else {
            GraphCell propertyCell = gmanager.getPropertyCell(predicate, false);
            if (propertyCell != null) {
                rdfsModel = (RDFSModel) GraphConstants.getValue(propertyCell.getAttributes());
            }
        }
        DefaultEdge edge = new RDFPropertyCell(rdfsModel);
        AttributeMap edgeMap = cellMaker.getEdgeMap(rdfsModel, edge);
        attributes.put(edge, edgeMap);

        return edge;
    }

    private boolean isExtractProperty(GraphCell subjectCell, Property predicate, RDFNode object) {
        RDFResourceModel info = (RDFResourceModel) GraphConstants.getValue(subjectCell.getAttributes());
        if (predicate.equals(RDF.type)) {
            GraphCell cell = gmanager.getClassCell((Resource) object, false);
            info.setTypeCell(cell, gmanager.getRDFGraph());
        } else if (predicate.equals(RDFS.label)) {
            MR3Literal literal = new MR3Literal((Literal) object);
            info.addLabel(literal);
        } else if (predicate.equals(RDFS.comment)) {
            MR3Literal literal = new MR3Literal((Literal) object);
            info.addComment(literal);
        } else {
            return false;
        }
        return true;
    }

    private DefaultGraphCell createResourceCell(Resource uri, Map<Object, DefaultGraphCell> resMap,
                                                Map<Object, AttributeMap> attr, GraphLayoutData data) {
        RDFResourceModel resInfo = null;
        if (uri.isAnon()) {
            resInfo = new RDFResourceModel(URIType.ANONYMOUS, uri.getId().toString());
        } else {
            resInfo = new RDFResourceModel(URIType.URI, uri.toString());
        }

        DefaultGraphCell resCell = new RDFResourceCell(resInfo);
        DefaultPort port = new DefaultPort();
        resCell.add(port);
        resMap.put(uri, resCell);
        Rectangle rectangle = null;
        if (data != null) {
            rectangle = data.getRectangle();
        }
        attr.put(resCell, cellMaker.getResourceMap(rectangle, RDFResourceCell.backgroundColor));
        GraphConstants.setValue(resCell.getAttributes(), resInfo);

        return resCell;
    }

    private DefaultGraphCell createLiteralCell(MR3Literal literal, Map<Object, AttributeMap> attr,
                                               GraphLayoutData data) {
        DefaultGraphCell litCell = new RDFLiteralCell(literal);
        DefaultPort tp = new DefaultPort();
        litCell.add(tp);
        Rectangle rectangle = null;
        if (data != null) {
            rectangle = data.getRectangle();
        }
        attr.put(litCell, cellMaker.getLiteralMap(rectangle, RDFLiteralCell.backgroundColor));
        GraphConstants.setValue(litCell.getAttributes(), literal);

        return litCell;
    }

    private void replaceGraph(RDFGraph newGraph) {
        gmanager.getRDFGraph().setModel(newGraph.getModel());
    }

    public void replaceDefaultRDFGraph(Model model) {
        Model extractModel = extractForHumanModel(model);
        replaceGraph(createRDFGraph(model, VGJTreeLayout.getVGJRDFCellLayoutMap(model)));
        addForHumanInfo(extractModel);
    }

    public void replaceProjectRDFGraph(Model model) {
        Model extractModel = extractForHumanModel(model);
        replaceGraph(createRDFGraph(model, ProjectManager.getLayoutMap()));
        addForHumanInfo(extractModel);
    }

    private Model extractForHumanModel(Model model) {
        Model extractModel = ModelFactory.createDefaultModel();
        for (Statement stmt : model.listStatements().toList()) {
            if (stmt.getPredicate().equals(RDFS.label) || stmt.getPredicate().equals(RDFS.comment)) {
                extractModel.add(stmt);
            }
        }
        model.remove(extractModel);
        return extractModel;
    }

    private void addForHumanInfo(Model extractModel) {
        for (Statement stmt : extractModel.listStatements().toList()) {
            if (stmt.getPredicate().equals(RDFS.label)) {
                GraphCell cell = (GraphCell) gmanager.getRDFResourceCell(stmt.getSubject());
                ResourceModel info = (ResourceModel) GraphConstants.getValue(cell.getAttributes());
                if (info == null) {
                    // ラベルまたは，コメント以外の関係をもたないリソースがくる．
                    // 無視している
                    continue;
                }
                info.addLabel(new MR3Literal((Literal) stmt.getObject()));
            } else if (stmt.getPredicate().equals(RDFS.comment)) {
                GraphCell cell = (GraphCell) gmanager.getRDFResourceCell(stmt.getSubject());
                ResourceModel info = (ResourceModel) GraphConstants.getValue(cell.getAttributes());
                if (info == null) {
                    // ラベルまたは，コメント以外の関係をもたないリソースがくる．
                    // 無視している
                    continue;
                }
                info.addComment(new MR3Literal((Literal) stmt.getObject()));
            }
        }
    }

    private RDFGraph createRDFGraph(Model model, Map<RDFNode, GraphLayoutData> cellLayoutMap) {
        Map<Object, DefaultGraphCell> resourceMap = new HashMap<>();
        Map<Object, AttributeMap> attributes = new HashMap<>();
        RDFGraph graph = new RDFGraph(gmanager, new RDFGraphModel(), null);
        GraphLayoutCache graphLayoutCache = graph.getGraphLayoutCache();

        MR3.STATUS_BAR.initNormal((int) model.size());

        for (Statement stmt : model.listStatements().toList()) {
            Resource subject = stmt.getSubject();
            Property predicate = stmt.getPredicate();
            RDFNode object = stmt.getObject();

            GraphLayoutData data = null;
            if (cellLayoutMap != null) {
                data = cellLayoutMap.get(subject);
            }

            MR3.STATUS_BAR.addValue();

            // Resource
            DefaultGraphCell sourceCell = resourceMap.get(subject);
            if (sourceCell == null) {
                sourceCell = createResourceCell(subject, resourceMap, attributes, data);
                graphLayoutCache.insert(new Object[]{sourceCell}, attributes, null, null);
            }

            // プロパティがrdfs:type, rdfs:label, rdfs:commentならば，グラフに描画しない．
            if (isExtractProperty(sourceCell, predicate, object)) {
                continue;
            }

            if (cellLayoutMap != null) {
                if (object instanceof Literal) {
                    Resource tmp = ResourceFactory.createResource(subject.toString() + object.hashCode());
                    data = cellLayoutMap.get(tmp);
                } else {
                    data = cellLayoutMap.get(object);
                }
            }

            // Object
            DefaultGraphCell targetCell = null;
            if (object instanceof Resource) {
                targetCell = resourceMap.get(object);
                if (targetCell == null) {
                    targetCell = createResourceCell((Resource) object, resourceMap, attributes, data);
                    graphLayoutCache.insert(new Object[]{targetCell}, attributes, null, null);
                }
            } else if (object instanceof Literal) {
                Literal literal = (Literal) object;
                targetCell = createLiteralCell(new MR3Literal(literal), attributes, data);
                graphLayoutCache.insert(new Object[]{targetCell}, attributes, null, null);
            }

            Edge edge = getEdge(attributes, predicate);

            Port sp = (Port) sourceCell.getChildAt(0);
            Port tp = (Port) targetCell.getChildAt(0);

            if (sp == tp) { // self connect
                AttributeMap map = edge.getAttributes();
                GraphConstants.setRouting(map, GraphConstants.ROUTING_SIMPLE);
                attributes.put(edge, map);
            }
            ConnectionSet cs = new ConnectionSet(edge, sp, tp);
            graphLayoutCache.insert(new Edge[]{edge}, attributes, cs, null);
        }
        MR3.STATUS_BAR.hideProgressBar();

        return graph;
    }
}
