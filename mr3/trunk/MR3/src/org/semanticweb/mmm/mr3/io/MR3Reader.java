/*
 * @(#) MR3Reader.java
 * 
 * 
 * Copyright (C) 2003-2005 The MMM Project
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

package org.semanticweb.mmm.mr3.io;

import java.io.*;
import java.lang.ref.*;

import org.jgraph.graph.*;
import org.semanticweb.mmm.mr3.*;
import org.semanticweb.mmm.mr3.data.*;
import org.semanticweb.mmm.mr3.data.MR3Constants.*;
import org.semanticweb.mmm.mr3.jgraph.*;
import org.semanticweb.mmm.mr3.layout.*;
import org.semanticweb.mmm.mr3.ui.*;
import org.semanticweb.mmm.mr3.util.*;

import com.hp.hpl.jena.ontology.*;
import com.hp.hpl.jena.rdf.model.*;

/**
 * @author takeshi morita
 */
public class MR3Reader {

    private MR3Parser mr3Parser;
    private GraphManager gmanager;
    private MR3Generator mr3Generator;
    private RDFSModelExtraction extractRDFS;
    private NameSpaceTableDialog nsTableDialog;
    private JGraphTreeLayout jgraphTreeLayout;

    private WeakReference<ReplaceRDFSDialog> replaceRDFSDialogRef;

    private RDFSInfoMap rdfsInfoMap = RDFSInfoMap.getInstance();

    public MR3Reader(GraphManager gm) {
        gmanager = gm;
        nsTableDialog = gmanager.getNSTableDialog();
        mr3Parser = new MR3Parser(gmanager);
        mr3Generator = new MR3Generator(gmanager);
        jgraphTreeLayout = new JGraphTreeLayout(gmanager);
        extractRDFS = new RDFSModelExtraction(gmanager);
        replaceRDFSDialogRef = new WeakReference<ReplaceRDFSDialog>(null);
    }

    public void replaceRDFModel(Model newModel) {
        model = newModel;
        if (model == null) { return; }
        new Thread() {
            public void run() {
                gmanager.importing(true);
                replaceRDF(model);
                performTreeLayout();
                // gmanager.addTypeCells();
                gmanager.importing(false);
            }
        }.start();
    }

    public void mergeRDFModelThread(Model newModel) {
        model = newModel;
        new Thread() {
            public void run() {
                gmanager.importing(true);
                mergeRDFModel(model);
                gmanager.importing(false);
            }
        }.start();
    }

    public void mergeRDFModel(Model newModel) {
        try {
            Model rdfModel = mr3Generator.getRDFModel(false);
            rdfModel.add(newModel);
            nsTableDialog.setCurrentNSPrefix(rdfModel);
            gmanager.getRDFGraph().removeAllCells();
            if (ProjectManager.getLayoutMap() != null) {
                mr3Parser.replaceProjectRDFGraph(rdfModel);
            } else {
                mr3Parser.replaceDefaultRDFGraph(rdfModel);
            }
        } catch (RDFException e) {
            e.printStackTrace();
        }
        mergeRDFSModel(ModelFactory.createDefaultModel()); // RDFからRDFSへ反映されたクラス，プロパティの処理
        resetPropertyInfo();
    }

    private void resetPropertyInfo() {
        RDFGraph rdfGraph = gmanager.getRDFGraph();
        Object[] cells = rdfGraph.getAllCells();
        for (int i = 0; i < cells.length; i++) {
            GraphCell cell = (GraphCell) cells[i];
            if (RDFGraph.isRDFPropertyCell(cell)) {
                RDFSInfo info = (RDFSInfo) GraphConstants.getValue(cell.getAttributes());
                if (!info.getURI().equals(MR3Resource.Nil)) {
                    GraphCell propCell = gmanager.getPropertyCell(info.getURI(), false);
                    Object newInfo = GraphConstants.getValue(propCell.getAttributes());
                    GraphConstants.setValue(cell.getAttributes(), newInfo);
                }
            }
        }
    }

    public void mergeRDFSModel(Model model) {
        MR3.STATUS_BAR.initIndeterminate();
        // addInnerModel(model);
        // model.add(mr3Generator.getPropertyModel(false));
        // model.add(mr3Generator.getClassModel(false));
        // Model classModel = extractRDFS.extractClassModel(model);
        // Model propertyModel = extractRDFS.extractPropertyModel(model);
        mergePropertyModel(model);
        mergeClassModel(model);
        // nsTableDialog.setCurrentNSPrefix(model);
        setPropertyLabels();
        MR3.STATUS_BAR.hideProgressBar();
    }

    private OntModel ontModel;

    public void mergeOntologyModel(OntModel model) {
        ontModel = model;
        if (ontModel == null) { return; }
        new Thread() {
            public void run() {
                gmanager.importing(true);
                // OntModelからDefaultModelへの変換処理
                Model rdfsModel = OntModelToRDFSModel.convertOntModelToRDFSModel(ontModel);
                mergeRDFs(rdfsModel);
                performTreeLayout();
                gmanager.importing(false);
            }
        }.start();
    }

    // private void addRDFTypeModel(Model rdfModel) {
    // Model innerModel = ModelFactory.createDefaultModel();
    // for (NodeIterator i = rdfModel.listObjectsOfProperty(RDF.type);
    // i.hasNext();) {
    // innerModel.add((Resource) i.next(), RDF.type, RDFS.Class);
    // }
    // rdfModel.add(innerModel);
    // }

    // private void addRDFPropertyModel(Model rdfModel) {
    // Model innerModel = ModelFactory.createDefaultModel();
    // for (StmtIterator i = rdfModel.listStatements(); i.hasNext();) {
    // Statement stmt = i.nextStatement();
    // innerModel.add(stmt.getPredicate(), RDF.type, RDF.Property);
    // }
    // rdfModel.add(innerModel);
    // }

    private void setPropertyLabel(RDFGraph graph, Object edge) {
        GraphCell sourceCell = (GraphCell) graph.getSourceVertex(edge);
        RDFSInfo sourceInfo = (RDFSInfo) GraphConstants.getValue(sourceCell.getAttributes());
        Resource sourceRes = sourceInfo.getURI();
        GraphCell targetCell = (GraphCell) graph.getTargetVertex(edge);
        RDFSInfo targetInfo = (RDFSInfo) GraphConstants.getValue(targetCell.getAttributes());
        Resource targetRes = targetInfo.getURI();
        Model model = rdfsInfoMap.getPropertyLabelModel();
        for (StmtIterator i = model.listStatements(); i.hasNext();) {
            Statement stmt = i.nextStatement();
            Resource subject = stmt.getSubject();
            Property predicate = stmt.getPredicate();
            RDFNode object = stmt.getObject();
            if (sourceRes.equals(subject) && targetRes.equals(object)) {
                String uri = predicate.toString();
                gmanager.setCellValue((GraphCell) edge, uri.substring(uri.indexOf('_') + 1));
            }
        }
    }

    private void setPropertyLabels() {
        RDFGraph classGraph = gmanager.getClassGraph();
        Object[] cells = classGraph.getAllCells();
        for (int i = 0; i < cells.length; i++) {
            if (RDFGraph.isEdge(cells[i])) {
                setPropertyLabel(classGraph, cells[i]);
            }
        }
    }

    private void mergeClassModel(Model model) {
        try {
            model.add(mr3Generator.getClassModel(false));
            nsTableDialog.setCurrentNSPrefix(model);
            // gmanager.getRDFSTreeEditor().setClassTreeRoot(model);
            extractRDFS.extractClassModel(model);
            if (ProjectManager.getLayoutMap() != null) {
                mr3Parser.createClassGraph(ProjectManager.getLayoutMap());
            } else {
                mr3Parser.createClassGraph(VGJTreeLayout.getVGJClassCellLayoutMap());
            }
            rdfsInfoMap.clearTemporaryObject();
            gmanager.getClassGraph().clearSelection();
        } catch (RDFException e) {
            e.printStackTrace();
        }
    }

    private void mergePropertyModel(Model model) {
        try {
            model.add(mr3Generator.getPropertyModel(false));
            nsTableDialog.setCurrentNSPrefix(model);
            // gmanager.getRDFSTreeEditor().setPropertyTreeRoot(model);
            extractRDFS.extractPropertyModel(model);
            if (ProjectManager.getLayoutMap() != null) {
                mr3Parser.createPropertyGraph(ProjectManager.getLayoutMap());
            } else {
                mr3Parser.createPropertyGraph(VGJTreeLayout.getVGJPropertyCellLayoutMap());
            }
            rdfsInfoMap.clearTemporaryObject();
            gmanager.getPropertyGraph().clearSelection();
        } catch (RDFException e) {
            e.printStackTrace();
        }
    }

    public void replaceRDF(Model model) {
        if (model != null) {
            nsTableDialog.setCurrentNSPrefix(model);
            gmanager.getRDFGraph().removeAllCells();
            // replaceGraph(mr3Parser.createRDFGraph(model,
            // VGJTreeLayout.getVGJRDFCellLayoutMap(model)));
            mr3Parser.replaceDefaultRDFGraph(model);
            mergeRDFSModel(ModelFactory.createDefaultModel()); // RDFからRDFSへ反映されたクラス，プロパティの処理
        }
    }

    public void replaceRDFS(Model model) {
        if (model != null) {
            mergeRDFSModel(model);
        }
    }

    public void replaceRDFSModel(Model mdl) {
        model = mdl;
        if (model == null) { return; }
        new Thread() {
            public void run() {
                if (gmanager.getRDFGraph().getAllCells().length == 0) {
                    gmanager.getClassGraph().removeAllCells();
                    gmanager.getPropertyGraph().removeAllCells();
                    gmanager.importing(true);
                    replaceRDFS(model);
                    performRDFSTreeLayout();
                } else {
                    ReplaceRDFSDialog replaceRDFSDialog = getReplaceRDFSDialog(model);
                    if (replaceRDFSDialog.isApply()) {
                        gmanager.importing(true);
                        replaceRDFS(model);
                        performRDFSTreeLayout();
                    }
                }
                gmanager.importing(false);
            }
        }.start();
    }

    public ReplaceRDFSDialog getReplaceRDFSDialog(Model model) {
        ReplaceRDFSDialog result = replaceRDFSDialogRef.get();
        if (result == null) {
            result = new ReplaceRDFSDialog(gmanager);
            replaceRDFSDialogRef = new WeakReference<ReplaceRDFSDialog>(result);
        }
        result.initListData(model.union(ModelFactory.createDefaultModel()));
        result.setVisible(true);
        return result;
    }

    public void mergeRDFs(Model model) {
        if (model != null) {
            mergeRDFSModel(model);
            mergeRDFModel(model);
        }
    }

    public void mergeRDFPlusRDFSModel(Model mdl) {
        model = mdl;
        if (model == null) { return; }
        new Thread() {
            public void run() {
                gmanager.importing(true);
                mergeRDFs(model);
                performTreeLayout();
                gmanager.importing(false);
            }
        }.start();
    }

    /**
     * 
     * グラフのレイアウトを行う vgjTreeLayout.performVGJTreeLayoutを使用する場合はGPL
     * jgraphTreeLayout.performJGraphTreeLayoutの場合はLGPL
     */
    public void performTreeLayout() {
        if (GraphLayoutUtilities.LAYOUT_TYPE.equals(GraphLayoutUtilities.JGRAPH_TREE_LAYOUT)) {
            jgraphTreeLayout.performJGraphTreeLayout();
        }
    }

    /**
     * 
     * グラフのレイアウトを行う vgjTreeLayout.performVGJRDFSTreeLayoutを使用する場合はGPL
     * jgraphTreeLayout.performJGraphRDFSTreeLayoutの場合はLGPL
     */
    public void performRDFSTreeLayout() {
        if (GraphLayoutUtilities.LAYOUT_TYPE.equals(GraphLayoutUtilities.JGRAPH_TREE_LAYOUT)) {
            jgraphTreeLayout.performJGraphRDFSTreeLayout();
        }
    }

    // private void addInnerModel(Model model) {
    // Model innerModel = ModelFactory.createDefaultModel();
    // Property[] classProperties = { RDFS.subClassOf, RDFS.domain, RDFS.range
    // };
    //
    // for (int i = 0; i < classProperties.length; i++) {
    // for (NodeIterator iter =
    // model.listObjectsOfProperty(classProperties[i]); iter.hasNext();) {
    // innerModel.add((Resource) iter.next(), RDF.type, RDFS.Class);
    // }
    // }
    // for (NodeIterator iter =
    // model.listObjectsOfProperty(RDFS.subPropertyOf); iter.hasNext();) {
    // innerModel.add((Resource) iter.next(), RDF.type, RDF.Property);
    // }
    // model.add(innerModel);
    // }

    private MR3 mr3;
    private Model model;

    public void replaceProjectModel(Model projectModel, MR3 project) {
        model = projectModel;
        mr3 = project;
        if (model == null) { return; }
        new Thread() {
            public void run() {
                File currentProject = MR3.getCurrentProject(); // NewProjectよりも前のを保存
                mr3.newProject(null);
                gmanager.importing(true);
                ProjectManager projectManager = new ProjectManager(gmanager);
                Model projectModel = projectManager.extractProjectModel(model);
                mergeRDFs(model);
                projectManager.loadProject(projectModel);
                projectManager.removeEmptyClass();
                nsTableDialog.setPrefixNSInfoSet();
                gmanager.clearSelection();
                gmanager.importing(false);
                if (currentProject != null) {
                    MR3.setCurrentProject(currentProject);
                    mr3.setTitle("MR3 - " + MR3.getCurrentProject().getAbsolutePath());
                    HistoryManager.saveHistory(HistoryType.OPEN_PROJECT, MR3.getCurrentProject().getAbsolutePath());
                }
            }
        }.start();
    }
}