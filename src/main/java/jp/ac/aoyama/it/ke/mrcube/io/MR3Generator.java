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

package jp.ac.aoyama.it.ke.mrcube.io;

import jp.ac.aoyama.it.ke.mrcube.jgraph.GraphManager;
import jp.ac.aoyama.it.ke.mrcube.jgraph.RDFGraph;
import jp.ac.aoyama.it.ke.mrcube.models.*;
import org.apache.jena.rdf.model.*;
import org.apache.jena.vocabulary.RDF;
import org.jgraph.graph.DefaultGraphCell;
import org.jgraph.graph.Edge;
import org.jgraph.graph.GraphCell;
import org.jgraph.graph.GraphConstants;

import java.util.*;

/*
 *
 * @author Takeshi Morita
 *
 */
public class MR3Generator {

    private final GraphManager gmanager;

    public MR3Generator(GraphManager gm) {
        gmanager = gm;
    }

    public Model getPropertyModel(boolean isSelected) {
        RDFGraph graph = gmanager.getPropertyGraph();
        Object[] cells = null;
        if (isSelected) {
            cells = graph.getAllSelectedCells();
        } else {
            cells = graph.getAllCells();
        }
        Model propertyModel = ModelFactory.createDefaultModel();
        createPropertyModel(graph, cells, propertyModel);
        return propertyModel;
    }

    private void createPropertyModel(RDFGraph graph, Object[] cells, Model propertyModel) {
        for (Object cell : cells) {
            if (RDFGraph.isRDFSPropertyCell(cell)) {
                PropertyModel info = (PropertyModel) GraphConstants.getValue(((GraphCell) cell)
                        .getAttributes());
                Set<GraphCell> supProperties = graph.getTargetCells((DefaultGraphCell) cell);
                info.setSuperRDFS(supProperties);
                if (!info.getURI().equals(MR3Resource.Property)) {
                    propertyModel.add(info.getModel());
                }
            }
        }
    }

    public Model getClassModel(boolean isSelected) {
        RDFGraph graph = gmanager.getClassGraph();
        Object[] cells = null;
        if (isSelected) {
            cells = graph.getAllSelectedCells();
        } else {
            cells = graph.getAllCells();
        }
        Model classModel = ModelFactory.createDefaultModel();
        createClassModel(graph, cells, classModel);
        return classModel;
    }

    private void createClassModel(RDFGraph graph, Object[] cells, Model classModel) {
        for (Object cell : cells) {
            if (RDFGraph.isRDFSClassCell(cell)) {
                DefaultGraphCell classCell = (DefaultGraphCell) cell;
                ClassModel info = (ClassModel) GraphConstants.getValue(classCell.getAttributes());
                Set<GraphCell> supClasses = graph.getTargetCells(classCell);
                info.setSuperRDFS(supClasses);
                classModel.add(info.getModel());
            }
        }
    }

    private void setResourceType(Model rdfModel, GraphCell cell) {
        InstanceModel resInfo = (InstanceModel) GraphConstants.getValue(cell.getAttributes());
        if (resInfo.hasType()) {
            rdfModel.add(rdfModel.createStatement(resInfo.getURI(), RDF.type, resInfo.getType()));
        }
    }

    /**
     * Edgeのリストを得て，rdf:typeのStatementsを作成．
     */
    private Object[] getEdges(Model rdfModel, Object[] cells) {
        if (cells != null) {
            List<GraphCell> result = new ArrayList<>();
            for (Object cell1 : cells) {
                GraphCell cell = (GraphCell) cell1;
                if (RDFGraph.isEdge(cell)) {
                    result.add(cell);
                } else if (!RDFGraph.isTypeCell(cell) && RDFGraph.isRDFResourceCell(cell)) {
                    setResourceType(rdfModel, cell);
                }
            }
            return result.toArray();
        }
        return null;
    }

    public Model getRDFModel(boolean isSelected) {
        Object[] edges = null;
        Object[] cells = null;
        RDFGraph graph = gmanager.getRDFGraph();
        Model rdfModel = ModelFactory.createDefaultModel();
        if (isSelected) {
            edges = getEdges(rdfModel, graph.getAllSelectedCells());
            cells = graph.getAllSelectedCells();
        } else {
            edges = getEdges(rdfModel, graph.getAllCells());
            cells = graph.getAllCells();
        }
        createRDFModel(graph, rdfModel, edges);
        addRDFModel(cells, rdfModel);

        return rdfModel;
    }

    private void addRDFModel(Object[] cells, Model rdfModel) {
        for (Object cell : cells) {
            if (RDFGraph.isRDFResourceCell(cell)) {
                InstanceModel info = (InstanceModel) GraphConstants.getValue(((GraphCell) cell)
                        .getAttributes());
                rdfModel.add(info.getModel(info.getURI()));
            }
        }
    }

    private Property getRDFProperty(Edge edge) {
        if (edge.getAttributes() == null
                || !(GraphConstants.getValue(edge.getAttributes()) instanceof RDFSModel propInfo)) {
            return MR3Resource.Nil;
        }

        Property property = null;
        if (propInfo == null) {
            property = MR3Resource.Nil;
        } else {
            property = ResourceFactory.createProperty(propInfo.getURI().getURI());
        }
        return property;
    }

    /*
     * JenaのRDFModelにrdf:liを含むStatementを与えるとエラーが表示されるため，
     * subjectとなるリソースごとにrdf:liの番号を割り振る
     */
    private Property getRDFLIProperty(Resource subject, Property property,
                                      Map<Resource, Integer> containerNumMap) {
        if (property.getURI().equals(RDF.getURI() + "li")) {
            if (containerNumMap.get(subject) != null) {
                Integer num = containerNumMap.get(subject);
                num = num + 1;
                // System.out.println(num);
                property = RDF.li(num);
                containerNumMap.put(subject, num);
            } else {
                containerNumMap.put(subject, 1);
                property = RDF.li(1);
            }
        }
        return property;
    }

    private void createRDFModel(RDFGraph graph, Model rdfModel, Object[] edges) {
        // rdf:liを利用していた場合に，subjectとなるリソースごとに番号を割り振るために利用
        Map<Resource, Integer> containerNumMap = new HashMap<>();
        for (Object edge1 : edges) {
            Edge edge = (Edge) edge1;
            GraphCell sourceCell = (GraphCell) graph.getSourceVertex(edge);
            InstanceModel info = (InstanceModel) GraphConstants.getValue(sourceCell.getAttributes());
            Resource subject = info.getURI();
            Property property = getRDFProperty(edge);
            property = getRDFLIProperty(subject, property, containerNumMap);
            GraphCell targetCell = (GraphCell) graph.getTargetVertex(edge);

            if (RDFGraph.isRDFResourceCell(targetCell)) {
                info = (InstanceModel) GraphConstants.getValue(targetCell.getAttributes());
                rdfModel.add(rdfModel.createStatement(subject, property, info.getURI()));
            } else if (RDFGraph.isRDFLiteralCell(targetCell)) {
                // MR3Literal to Literal
                MR3Literal literal = (MR3Literal) GraphConstants.getValue(targetCell.getAttributes());
                rdfModel.add(rdfModel.createStatement(subject, property, literal.getLiteral()));
            }
        }
    }

}
