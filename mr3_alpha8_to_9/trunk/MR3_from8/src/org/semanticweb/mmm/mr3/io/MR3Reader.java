/*
 * @(#) MR3Reader.java
 *
 *
 * Copyright (C) 2003 The MMM Project
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 */

package org.semanticweb.mmm.mr3.io;

import org.jgraph.graph.*;
import org.semanticweb.mmm.mr3.*;
import org.semanticweb.mmm.mr3.data.*;
import org.semanticweb.mmm.mr3.jgraph.*;
import org.semanticweb.mmm.mr3.ui.*;
import org.semanticweb.mmm.mr3.util.*;

import com.hp.hpl.jena.rdf.model.*;

/**
 * @author takeshi morita
 */
public class MR3Reader {

	private GraphManager gmanager;
	private MR3Parser mr3Parser;
	private MR3Generator mr3Generator;
	private NameSpaceTableDialog nsTableDialog;
	private RDFSInfoMap rdfsInfoMap = RDFSInfoMap.getInstance();

	/*
	 * RDFReader -> JenaReader or N3JenaReader
	 */
	public MR3Reader(GraphManager gm, NameSpaceTableDialog nsTableD) {
		gmanager = gm;
		nsTableDialog = nsTableD;
		mr3Parser = new MR3Parser(gmanager);
		mr3Generator = new MR3Generator(gmanager);
	}

	private void replaceGraph(RDFGraph newGraph) {
		gmanager.getRDFGraph().setRDFState(newGraph.getRDFState());
	}

	public void replaceRDFModel(Model model) {
		gmanager.getRDFGraph().removeAllCells();
		RDFGraph newGraph = mr3Parser.convertRDFToJGraph(model);
		replaceGraph(newGraph);
		nsTableDialog.setCurrentNSPrefix();
	}

	public void mergeRDFModel(Model newModel) {
		try {
			Model model = mr3Generator.getRDFModel();
			model.add(newModel);
			gmanager.getRDFGraph().removeAllCells();
			RDFGraph newGraph = mr3Parser.convertRDFToJGraph(model);
			replaceGraph(newGraph);
			nsTableDialog.setCurrentNSPrefix();
		} catch (RDFException e) {
			e.printStackTrace();
		}
	}

	public void mergeRDFSModel(Model model) {
		mergePropertyModel(model);
		mergeClassModel(model);
		nsTableDialog.setCurrentNSPrefix();
		setPropertyLabels();
	}

	private void setPropertyLabel(RDFGraph graph, Object edge) {
		RDFSInfo sourceInfo = rdfsInfoMap.getCellInfo(graph.getSourceVertex(edge));
		Resource sourceRes = sourceInfo.getURI();
		RDFSInfo targetInfo = rdfsInfoMap.getCellInfo(graph.getTargetVertex(edge));
		Resource targetRes = targetInfo.getURI();
		Model model = rdfsInfoMap.getPropertyLabelModel();
		for (StmtIterator i = model.listStatements(); i.hasNext();) {
			Statement stmt = i.nextStatement();
			Resource subject = stmt.getSubject();
			Property predicate = stmt.getPredicate();
			RDFNode object = stmt.getObject();
			if (sourceRes.equals(subject) && targetRes.equals(object)) {
				String uri = predicate.toString();
				gmanager.setCellValue((GraphCell) edge, uri.substring(uri.indexOf('_')+1));
			}
		}
	}

	private void setPropertyLabels() {
		RDFGraph classGraph = gmanager.getClassGraph();
		Object[] cells = classGraph.getAllCells();
		for (int i = 0; i < cells.length; i++) {
			if (classGraph.isEdge(cells[i])) {
				setPropertyLabel(classGraph, cells[i]);
			}
		}
		gmanager.changeCellView();
	}

	private void mergeClassModel(Model model) {
		try {
			model.add(mr3Generator.getClassModel());
			mr3Parser.createClassGraph(model);
			rdfsInfoMap.setClassTreeModel();			
			rdfsInfoMap.clearTemporaryObject();
		} catch (RDFException e) {
			e.printStackTrace();
		}
	}

	private void mergePropertyModel(Model model) {
		try {
			model.add(mr3Generator.getPropertyModel());
			mr3Parser.createPropertyGraph(model);
			rdfsInfoMap.setPropTreeModel();
			rdfsInfoMap.clearTemporaryObject();
		} catch (RDFException e) {
			e.printStackTrace();
		}
	}

	public void replaceRDF(Model model) {
		if (model != null) {
			replaceRDFModel(model);
			mergeRDFSModel(ModelFactory.createDefaultModel()); // RDFからRDFSへ反映されたクラス，プロパティの処理
		}
	}

	public void replaceRDFS(Model model) {
		if (model != null) {
			mergeRDFSModel(model);
		}
	}

	public void mergeRDF(Model model) {
		if (model != null) {
			mergeRDFModel(model);
			mergeRDFSModel(ModelFactory.createDefaultModel()); // RDFからRDFSへ反映されたクラス，プロパティの処理
		}
	}

	public void mergeRDFS(Model model) {
		if (model != null) {
			mergeRDFSModel(model);
			mergeRDF(model); // RDFSにRDFが含まれていた場合の処理(mergeRDFModel()ではない)
		}
	}

	public void replaceProjectModel(Model model, MR3 mr3) {
		if (model != null) {
			if (model != null) {
				mr3.newProject();
				// 順番が重要
				ProjectManager projectManager = new ProjectManager(mr3.getGraphManager(), mr3.getNSTableDialog());
				Model projectModel = projectManager.extractProjectModel(model);
				mergeRDFS(model); // mergeRDFModelではない．まぎらわしい．
				projectManager.loadProject(projectModel);
				projectManager.removeEmptyClass();
				gmanager.removeTypeCells();
				gmanager.addTypeCells();
				nsTableDialog.changeCellView();
				gmanager.clearSelection();
			}
		}
	}
}
