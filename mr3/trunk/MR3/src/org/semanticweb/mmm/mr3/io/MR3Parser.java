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

package org.semanticweb.mmm.mr3.io;

import java.awt.*;
import java.io.*;
import java.util.*;

import org.jgraph.graph.*;
import org.semanticweb.mmm.mr3.*;
import org.semanticweb.mmm.mr3.data.*;
import org.semanticweb.mmm.mr3.data.MR3Constants.*;
import org.semanticweb.mmm.mr3.jgraph.*;
import org.semanticweb.mmm.mr3.layout.*;
import org.semanticweb.mmm.mr3.util.*;

import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.vocabulary.*;

/*
 * 
 * @author takeshi morita
 * 
 */
public class MR3Parser {

    private MR3CellMaker cellMaker;
    private GraphManager gmanager;

    public MR3Parser(GraphManager manager) {
        gmanager = manager;
        cellMaker = new MR3CellMaker(gmanager);
    }

    private Set<RDFSInfo> duplicateSubInfo;

    public void createClassGraph(Map cellLayoutMap) throws RDFException {
        duplicateSubInfo = new HashSet<RDFSInfo>();
        RDFGraph graph = gmanager.getCurrentClassGraph();
        graph.removeEdges();
        DefaultGraphCell rootCell = (DefaultGraphCell) gmanager.getClassCell(RDFS.Resource, cellLayoutMap);
        Port rootPort = (Port) rootCell.getChildAt(0);
        
        RDFSInfoMap rdfsInfoMap = gmanager.getCurrentRDFSInfoMap();
        ClassInfo rootInfo = (ClassInfo) rdfsInfoMap.getResourceInfo(RDFS.Resource);
        if (rootInfo != null) {
            GraphConstants.setValue(rootCell.getAttributes(), rootInfo);
            rdfsInfoMap.putURICellMap(rootInfo, rootCell);
            rootInfo.setURI(RDFS.Resource.getURI());
            if (rootInfo.getRDFSSubList().size() > 0) {
                createRDFSGraph(graph.getGraphLayoutCache(), rootInfo, rootCell, rootPort, cellLayoutMap);
            }
        }
    }

    public void createPropertyGraph(Map cellLayoutMap) throws RDFException {
        RDFSInfoMap rdfsInfoMap = gmanager.getCurrentRDFSInfoMap();
        duplicateSubInfo = new HashSet<RDFSInfo>();
        RDFGraph graph = gmanager.getCurrentPropertyGraph();
        graph.removeEdges();

        DefaultGraphCell rootCell = (DefaultGraphCell) gmanager.getPropertyCell(MR3Resource.Property, cellLayoutMap);
        Port rootPort = (Port) rootCell.getChildAt(0);

        Map<Object, Map> attributes = new HashMap<Object, Map>();
        for (Resource property : rdfsInfoMap.getRootProperties()) {

            // _1.._numは ， グラフに描画しない
            if (property.getURI().matches(RDF.getURI() + "_\\d*")) {
                continue;
            }

            PropertyInfo info = (PropertyInfo) rdfsInfoMap.getResourceInfo(property);
            DefaultGraphCell pCell = (DefaultGraphCell) gmanager.getPropertyCell(property, cellLayoutMap);
            Port pPort = (Port) pCell.getChildAt(0);
            GraphConstants.setValue(pCell.getAttributes(), info);
            rdfsInfoMap.putURICellMap(info, pCell);

            Edge edge = getEdge(attributes, null);
            ConnectionSet cs = new ConnectionSet(edge, pPort, rootPort);
            graph.getGraphLayoutCache().insert(new Object[] { edge}, attributes, cs, null);

            if (info.getRDFSSubList().size() > 0) {
                createRDFSGraph(graph.getGraphLayoutCache(), info, pCell, pPort, cellLayoutMap);
            }
        }
    }

    /**
     * graphModel -> class or property
     */
    private void createRDFSGraph(GraphLayoutCache graphLayoutCache, RDFSInfo supInfo, GraphCell supCell, Port supPort,
            Map cellLayoutMap) throws RDFException {
        Map<Object, Map> attributes = new HashMap<Object, Map>();
        RDFSInfoMap rdfsInfoMap = gmanager.getCurrentRDFSInfoMap();
        for (Resource subRes : supInfo.getRDFSSubList()) {
            RDFSInfo subInfo = rdfsInfoMap.getResourceInfo(subRes);

            DefaultGraphCell subCell = null;
            if (supInfo instanceof ClassInfo) {
                subCell = (DefaultGraphCell) gmanager.getClassCell(subRes, cellLayoutMap);
            } else if (subInfo instanceof PropertyInfo) {
                subCell = (DefaultGraphCell) gmanager.getPropertyCell(subRes, cellLayoutMap);
            }

            Port subPort = (Port) subCell.getChildAt(0);
            GraphConstants.setValue(subCell.getAttributes(), subInfo);
            rdfsInfoMap.putURICellMap(subInfo, subCell);
            subInfo.addSupRDFS(supCell);

            Edge edge = getEdge(attributes, null);
            ConnectionSet cs = new ConnectionSet(edge, subPort, supPort);
            graphLayoutCache.insert(new Object[] { edge}, attributes, cs, null);

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

    private Edge getEdge(Map<Object, Map> attributes, Resource predicate) {
        if (predicate == null) {
            DefaultEdge edge = new RDFPropertyCell("");
            Map edgeMap = cellMaker.getEdgeMap(null, edge);
            attributes.put(edge, edgeMap);
            return edge;
        }
        RDFSInfo rdfsInfo = null;
        if (predicate.equals(MR3Resource.Nil)) {
            rdfsInfo = new PropertyInfo(MR3Resource.Nil.getURI());
        } else {
            GraphCell propertyCell = gmanager.getPropertyCell(predicate, false);
            if (propertyCell != null) {
                rdfsInfo = (RDFSInfo) GraphConstants.getValue(propertyCell.getAttributes());
            }
        }
        DefaultEdge edge = new RDFPropertyCell(rdfsInfo);
        Map edgeMap = cellMaker.getEdgeMap(rdfsInfo, edge);
        attributes.put(edge, edgeMap);

        return edge;
    }
    private boolean isExtractProperty(GraphCell subjectCell, Property predicate, RDFNode object) {
        RDFResourceInfo info = (RDFResourceInfo) GraphConstants.getValue(subjectCell.getAttributes());
        if (predicate.equals(RDF.type)) {
            GraphCell cell = (GraphCell) gmanager.getClassCell((Resource) object, false);
            info.setTypeCell(cell);
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
            Map<Object, Map> attr, GraphLayoutData data) {
        RDFResourceInfo resInfo = null;
        if (uri.isAnon()) {
            resInfo = new RDFResourceInfo(URIType.ANONYMOUS, uri.getId().toString());
        } else {
            resInfo = new RDFResourceInfo(URIType.URI, uri.toString());
        }

        DefaultGraphCell resCell = new RDFResourceCell(resInfo);
        DefaultPort port = new DefaultPort();
        resCell.add(port);
        resMap.put(uri, resCell);
        Rectangle rectangle = null;
        if (data != null) {
            rectangle = data.getRectangle();
        }
        attr.put(resCell, cellMaker.getResourceMap(rectangle, RDFResourceCell.rdfResourceColor));
        GraphConstants.setValue(resCell.getAttributes(), resInfo);

        return resCell;
    }

    private DefaultGraphCell createLiteralCell(MR3Literal literal, Map<Object, Map> attr, GraphLayoutData data)
            throws RDFException {
        DefaultGraphCell litCell = new RDFLiteralCell(literal);
        DefaultPort tp = new DefaultPort();
        litCell.add(tp);
        Rectangle rectangle = null;
        if (data != null) {
            rectangle = data.getRectangle();
        }
        attr.put(litCell, cellMaker.getResourceMap(rectangle, RDFLiteralCell.literalColor));
        GraphConstants.setValue(litCell.getAttributes(), literal);

        return litCell;
    }

    private void replaceGraph(RDFGraph newGraph) {
        gmanager.getCurrentRDFGraph().setModel(newGraph.getModel());
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
        for (StmtIterator i = model.listStatements(); i.hasNext();) {
            Statement stmt = i.nextStatement();
            if (stmt.getPredicate().equals(RDFS.label) || stmt.getPredicate().equals(RDFS.comment)) {
                extractModel.add(stmt);
            }
        }
        model.remove(extractModel);
        return extractModel;
    }

    private void addForHumanInfo(Model extractModel) {
        for (StmtIterator i = extractModel.listStatements(); i.hasNext();) {
            Statement stmt = i.nextStatement();
            if (stmt.getPredicate().equals(RDFS.label)) {
                GraphCell cell = (GraphCell) gmanager.getRDFResourceCell(stmt.getSubject());
                ResourceInfo info = (ResourceInfo) GraphConstants.getValue(cell.getAttributes());
                if (info == null) {
                    // ラベルまたは，コメント以外の関係をもたないリソースがくる．
                    // 無視している
                    continue;
                }
                info.addLabel(new MR3Literal((Literal) stmt.getObject()));
            } else if (stmt.getPredicate().equals(RDFS.comment)) {
                GraphCell cell = (GraphCell) gmanager.getRDFResourceCell(stmt.getSubject());
                ResourceInfo info = (ResourceInfo) GraphConstants.getValue(cell.getAttributes());
                if (info == null) {
                    // ラベルまたは，コメント以外の関係をもたないリソースがくる．
                    // 無視している
                    continue;
                }
                info.addComment(new MR3Literal((Literal) stmt.getObject()));
            }
        }
    }

    public RDFGraph createRDFGraph(Model model, Map cellLayoutMap) throws RDFException {
        Map<Object, DefaultGraphCell> resourceMap = new HashMap<Object, DefaultGraphCell>();
        Map<Object, Map> attributes = new HashMap<Object, Map>();
        RDFGraph graph = new RDFGraph(gmanager, new RDFGraphModel(), null);
        GraphLayoutCache graphLayoutCache = graph.getGraphLayoutCache();

        MR3.STATUS_BAR.initNormal((int) model.size());

        for (StmtIterator iter = model.listStatements(); iter.hasNext();) {
            Statement stmt = iter.nextStatement();
            Resource subject = stmt.getSubject();
            Property predicate = stmt.getPredicate();
            RDFNode object = stmt.getObject();

            GraphLayoutData data = null;
            if (cellLayoutMap != null) {
                data = (GraphLayoutData) cellLayoutMap.get(subject);
            }

            MR3.STATUS_BAR.addValue();

            // Resource
            DefaultGraphCell sourceCell = resourceMap.get(subject);
            if (sourceCell == null) {
                sourceCell = createResourceCell(subject, resourceMap, attributes, data);
                graphLayoutCache.insert(new Object[] { sourceCell}, attributes, null, null);
            }

            // プロパティがrdfs:type, rdfs:label, rdfs:commentならば，グラフに描画しない．
            if (isExtractProperty(sourceCell, predicate, object)) {
                continue;
            }

            if (cellLayoutMap != null) {
                if (object instanceof Literal) {
                    Resource tmp = ResourceFactory.createResource(subject.toString() + object.hashCode());
                    data = (GraphLayoutData) cellLayoutMap.get(tmp);
                } else {
                    data = (GraphLayoutData) cellLayoutMap.get(object);
                }
            }

            // Object
            DefaultGraphCell targetCell = null;
            if (object instanceof Resource) {
                targetCell = resourceMap.get(object);
                if (targetCell == null) {
                    targetCell = createResourceCell((Resource) object, resourceMap, attributes, data);
                    graphLayoutCache.insert(new Object[] { targetCell}, attributes, null, null);
                }
            } else if (object instanceof Literal) {
                Literal literal = (Literal) object;
                targetCell = createLiteralCell(new MR3Literal(literal), attributes, data);
                graphLayoutCache.insert(new Object[] { targetCell}, attributes, null, null);
            }

            Edge edge = getEdge(attributes, predicate);

            Port sp = (Port) sourceCell.getChildAt(0);
            Port tp = (Port) targetCell.getChildAt(0);
            if (sp == tp) { // self connect
                Map map = edge.getAttributes();
                GraphConstants.setRouting(map, GraphConstants.ROUTING_SIMPLE);
                attributes.put(edge, map);
            }
            ConnectionSet cs = new ConnectionSet(edge, sp, tp);
            graphLayoutCache.insert(new Object[] { edge}, attributes, cs, null);
        }
        MR3.STATUS_BAR.hideProgressBar();

        return graph;
    }

    public void mergeRDFToJGraph(Reader reader, Model currentModel) {
        Model model = ModelFactory.createDefaultModel();
        try {
            model.read(reader, gmanager.getBaseURI());
            model.add(currentModel);
            gmanager.getCurrentRDFGraph().removeAllCells();
            replaceDefaultRDFGraph(model);
        } catch (RDFException e) {
            e.printStackTrace();
        }
    }
}
