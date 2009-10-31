package mr3.util;
import java.util.*;

import mr3.data.*;
import mr3.jgraph.*;

import com.hp.hpl.mesa.rdf.jena.common.*;
import com.hp.hpl.mesa.rdf.jena.mem.*;
import com.hp.hpl.mesa.rdf.jena.model.*;
import com.hp.hpl.mesa.rdf.jena.vocabulary.*;
import com.jgraph.graph.*;

public class JGraphToRDF {

	private GraphManager gmanager;
	private RDFResourceInfoMap resInfoMap = RDFResourceInfoMap.getInstance();
	private RDFLiteralInfoMap litInfoMap = RDFLiteralInfoMap.getInstance();
	private RDFSInfoMap rdfsInfoMap = RDFSInfoMap.getInstance();

	public JGraphToRDF(GraphManager manager) {
		gmanager = manager;
	}

	public Model getPropertyModel() throws RDFException {
		RDFGraph graph = gmanager.getPropertyGraph();
		Object[] cells = graph.getAllCells();
		Model propertyModel = new ModelMem();

		for (int i = 0; i < cells.length; i++) {
			Object cell = cells[i];
			//			if (graph.isRDFResourceCell(cell)) {
			if (graph.isRDFSPropertyCell(cell)) {
				PropertyInfo info = (PropertyInfo) rdfsInfoMap.getCellInfo(cell);
				Set supProperties = graph.getTargetCells((DefaultGraphCell) cell);
				info.setSupRDFS(supProperties);
				if (!info.getURI().equals(MR3Resource.Property)) {
					propertyModel.add(info.getModel());
				}
			}
		}
		return propertyModel;
	}

	public Model getClassModel() throws RDFException {
		RDFGraph graph = gmanager.getClassGraph();
		Object[] cells = graph.getAllCells();
		Model classModel = new ModelMem();

		for (int i = 0; i < cells.length; i++) {
			Object cell = cells[i];
			//			if (graph.isRDFResourceCell(cell)) {
			if (graph.isRDFSClassCell(cell)) {
				ClassInfo info = (ClassInfo) rdfsInfoMap.getCellInfo(cell);
				Set supClasses = graph.getTargetCells((DefaultGraphCell) cell);
				info.setSupRDFS(supClasses);
				classModel.add(info.getModel());
			}
		}
		return classModel;
	}

	private void setResourceType(Model rdfModel, Object cell) {
		try {
			RDFResourceInfo info = resInfoMap.getCellInfo(cell);
			if (info.getType().getURI().length() != 0) {
				rdfModel.add(rdfModel.createStatement(info.getURI(), RDF.type, info.getType()));
			}
		} catch (RDFException rex) {
			rex.printStackTrace();
		}
	}

	/** Edgeのリストを得るついでに，TypeのStatementsも作っている．分けた方がわかりやすいが．*/
	private Object[] getEdges(Model rdfModel, RDFGraph graph) {
		Object[] cells = graph.getAllCells();
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

	private Resource getResource(RDFResourceInfo info) {
		if (info.getURIType() == URIType.ANONYMOUS) {
			//			System.out.println(info.getURI().isAnon());
			return info.getURI();
		} else if (info.getURIType() == URIType.ID) {
			return new ResourceImpl(gmanager.getBaseURI() + info.getURI());
		} else {
			return info.getURI();
		}
	}

	public Model getRDFModel() {
		RDFGraph graph = gmanager.getRDFGraph();
		Model rdfModel = new ModelMem();
		Object[] edges = getEdges(rdfModel, graph);

		for (int i = 0; i < edges.length; i++) {
			Edge edge = (Edge) edges[i];
			RDFResourceInfo info = resInfoMap.getCellInfo(graph.getSourceVertex(edge));
			Resource subject = getResource(info);
			try {
				Object propCell = rdfsInfoMap.getEdgeInfo(edge);
				RDFSInfo propInfo = rdfsInfoMap.getCellInfo(propCell);
				Property property = new PropertyImpl(propInfo.getURI().getURI());
				GraphCell targetCell = (GraphCell) graph.getTargetVertex(edge);

				if (graph.isRDFResourceCell(targetCell)) {
					info = resInfoMap.getCellInfo(targetCell);
					Resource object = getResource(info);
					rdfModel.add(rdfModel.createStatement(subject, property, object));
				} else if (graph.isRDFLiteralCell(targetCell)) {
					Literal object = litInfoMap.getCellInfo(targetCell);
					rdfModel.add(rdfModel.createStatement(subject, property, object));
				}
			} catch (RDFException e) {
				e.printStackTrace();
			}
		}
		return rdfModel;
	}
}
