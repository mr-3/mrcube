package jp.ac.shizuoka.cs.panda.mmm.mr3.io;
import java.util.*;

import jp.ac.shizuoka.cs.panda.mmm.mr3.data.*;
import jp.ac.shizuoka.cs.panda.mmm.mr3.jgraph.*;

import com.hp.hpl.mesa.rdf.jena.common.*;
import com.hp.hpl.mesa.rdf.jena.mem.*;
import com.hp.hpl.mesa.rdf.jena.model.*;
import com.hp.hpl.mesa.rdf.jena.vocabulary.*;
import com.jgraph.graph.*;

public class MR3Generator {

	private GraphManager gmanager;
	private RDFResourceInfoMap resInfoMap = RDFResourceInfoMap.getInstance();
	private RDFLiteralInfoMap litInfoMap = RDFLiteralInfoMap.getInstance();
	private RDFSInfoMap rdfsInfoMap = RDFSInfoMap.getInstance();

	public MR3Generator(GraphManager manager) {
		gmanager = manager;
	}

	public Model getSelectedPropertyModel() {
		RDFGraph graph = gmanager.getPropertyGraph();
		Object[] cells = graph.getAllSelectedCells();
		Model propertyModel = new ModelMem();
		createPropertyModel(graph, cells, propertyModel);

		return propertyModel;
	}

	public Model getPropertyModel() {
		RDFGraph graph = gmanager.getPropertyGraph();
		Object[] cells = graph.getAllCells();
		Model propertyModel = new ModelMem();
		createPropertyModel(graph, cells, propertyModel);

		return propertyModel;
	}

	private void createPropertyModel(RDFGraph graph, Object[] cells, Model propertyModel) {
		try {
			for (int i = 0; i < cells.length; i++) {
				Object cell = cells[i];
				if (graph.isRDFSPropertyCell(cell)) {
					PropertyInfo info = (PropertyInfo) rdfsInfoMap.getCellInfo(cell);
					Set supProperties = graph.getSourceCells((DefaultGraphCell) cell);
					info.setSupRDFS(supProperties);
					if (!info.getURI().equals(MR3Resource.Property)) {
						propertyModel.add(info.getModel());
					}
				}
			}
		} catch (RDFException e) {
			e.printStackTrace();
		}
	}

	public Model getSelectedClassModel() {
		RDFGraph graph = gmanager.getClassGraph();
		Object[] cells = graph.getAllSelectedCells();
		Model classModel = new ModelMem();
		createClassModel(graph, cells, classModel);

		return classModel;
	}

	public Model getClassModel() {
		RDFGraph graph = gmanager.getClassGraph();
		Object[] cells = graph.getAllCells();
		Model classModel = new ModelMem();
		createClassModel(graph, cells, classModel);

		return classModel;
	}

	private void createClassModel(RDFGraph graph, Object[] cells, Model classModel) {
		try {
			for (int i = 0; i < cells.length; i++) {
				Object cell = cells[i];
				if (graph.isRDFSClassCell(cell)) {
					ClassInfo info = (ClassInfo) rdfsInfoMap.getCellInfo(cell);
					Set supClasses = graph.getSourceCells((DefaultGraphCell) cell);
					info.setSupRDFS(supClasses);
					classModel.add(info.getModel());
				}
			}
		} catch (RDFException e) {
			e.printStackTrace();
		}
	}

	private void setResourceType(Model rdfModel, Object cell) {
		try {
			RDFResourceInfo resInfo = resInfoMap.getCellInfo(cell);
			if (resInfo.getType().getURI().length() != 0) {
				rdfModel.add(rdfModel.createStatement(resInfo.getURI(), RDF.type, resInfo.getType()));
			}
		} catch (RDFException rex) {
			rex.printStackTrace();
		}
	}

	/** Edgeのリストを得るついでに，TypeのStatementsも作っている．分けた方がわかりやすいが．*/
	private Object[] getEdges(Model rdfModel, RDFGraph graph, Object[] cells) {
		if (cells != null) {
			ArrayList result = new ArrayList();
			for (int i = 0; i < cells.length; i++) {
				Object cell = cells[i];
				if (graph.isEdge(cell)) {
					result.add(cell);
				} else if (!graph.isTypeCell(cell) && graph.isRDFResourceCell(cell)) {
					setResourceType(rdfModel, cell);
				}
			}
			return result.toArray();
		}
		return null;
	}

	public Model getSelectedRDFModel() {
		RDFGraph graph = gmanager.getRDFGraph();
		Model rdfModel = new ModelMem();
		Object[] edges = getEdges(rdfModel, graph, graph.getAllSelectedCells());
		createRDFModel(graph, rdfModel, edges);

		return rdfModel;
	}

	public Model getRDFModel() {
		RDFGraph graph = gmanager.getRDFGraph();
		Model rdfModel = new ModelMem();
		Object[] edges = getEdges(rdfModel, graph, graph.getAllCells());
		createRDFModel(graph, rdfModel, edges);

		return rdfModel;
	}

	private Property getRDFProperty(Edge edge) throws RDFException {
		Object propCell = rdfsInfoMap.getEdgeInfo(edge);
		RDFSInfo propInfo = rdfsInfoMap.getCellInfo(propCell);
		Property property = null;
		if (propInfo == null) {
			property = MR3Resource.Nil;
		} else {
			property = new PropertyImpl(propInfo.getURI().getURI());
		}
		return property;
	}

	private void createRDFModel(RDFGraph graph, Model rdfModel, Object[] edges) {
		for (int i = 0; i < edges.length; i++) {
			Edge edge = (Edge) edges[i];
			RDFResourceInfo info = resInfoMap.getCellInfo(graph.getSourceVertex(edge));
			Resource subject = info.getURI();
			try {
				Property property = getRDFProperty(edge);
				GraphCell targetCell = (GraphCell) graph.getTargetVertex(edge);

				if (graph.isRDFResourceCell(targetCell)) {
					info = resInfoMap.getCellInfo(targetCell);
					Resource object = info.getURI();
					rdfModel.add(rdfModel.createStatement(subject, property, object));
				} else if (graph.isRDFLiteralCell(targetCell)) {
					Literal object = litInfoMap.getCellInfo(targetCell);
					rdfModel.add(rdfModel.createStatement(subject, property, object));
				}
			} catch (RDFException e) {
				e.printStackTrace();
			}
		}
	}

}
