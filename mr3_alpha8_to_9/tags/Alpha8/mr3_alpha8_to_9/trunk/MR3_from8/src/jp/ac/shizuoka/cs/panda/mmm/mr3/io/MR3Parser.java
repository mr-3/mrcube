package jp.ac.shizuoka.cs.panda.mmm.mr3.io;
import java.awt.*;
import java.io.*;
import java.util.*;

import jp.ac.shizuoka.cs.panda.mmm.mr3.data.*;
import jp.ac.shizuoka.cs.panda.mmm.mr3.jgraph.*;
import jp.ac.shizuoka.cs.panda.mmm.mr3.util.*;

import org.jgraph.graph.*;

import com.hp.hpl.jena.mem.*;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.vocabulary.*;

public class MR3Parser {

	private RDFCellMaker cellMaker;
	private GraphManager gmanager;
	private RDFSModelExtraction extractRDFS;
	private RDFResourceInfoMap resInfoMap = RDFResourceInfoMap.getInstance();
	private RDFLiteralInfoMap litInfoMap = RDFLiteralInfoMap.getInstance();
	private RDFSInfoMap rdfsInfoMap = RDFSInfoMap.getInstance();

	public MR3Parser(GraphManager manager) {
		gmanager = manager;
		cellMaker = new RDFCellMaker(gmanager);
		extractRDFS = new RDFSModelExtraction(manager);
	}

	public void createClassGraph(Model model) throws RDFException {
		RDFGraph graph = gmanager.getClassGraph();
		extractRDFS.extractClassModel(model);
		graph.removeEdges();
		DefaultGraphCell rootCell = (DefaultGraphCell) gmanager.getClassCell(RDFS.Resource, false);
		Port rootPort = (Port) rootCell.getChildAt(0);

		ClassInfo rootInfo = (ClassInfo) rdfsInfoMap.getResourceInfo(RDFS.Resource);
		if (rootInfo != null) { // RDFSが未定義で，すべてのRDFリソースにタイプがない場合．
			rdfsInfoMap.putCellInfo(rootCell, rootInfo);
			rootInfo.setURI(RDFS.Resource.getURI());

			if (rootInfo.getRDFSSubList().size() > 0) {
				createRDFSGraph(graph.getModel(), rootInfo, rootCell, rootPort);
			}
		}
	}

	public void createPropertyGraph(Model model) throws RDFException {
		extractRDFS.extractPropertyModel(model);
		RDFGraph graph = gmanager.getPropertyGraph();
		graph.removeEdges();

		DefaultGraphCell rootCell = (DefaultGraphCell) gmanager.getPropertyCell(MR3Resource.Property, false);
		Port rootPort = (Port) rootCell.getChildAt(0);

		Map attributes = new HashMap();
		Set propertySet = rdfsInfoMap.getRootProperties();
		for (Iterator i = propertySet.iterator(); i.hasNext();) {
			Resource property = (Resource) i.next();

			PropertyInfo info = (PropertyInfo) rdfsInfoMap.getResourceInfo(property);
			DefaultGraphCell pCell = (DefaultGraphCell) gmanager.getPropertyCell(property, false);
			Port pPort = (Port) pCell.getChildAt(0);

			rdfsInfoMap.putCellInfo(pCell, info);

			// _1.._numは，グラフに描画しない
//			if (property.getURI().matches(RDF.getURI() + "_\\d*")) {
//				continue;
//			}

			Edge edge = getEdge(attributes, "");
			ConnectionSet cs = new ConnectionSet(edge, rootPort, pPort);
			graph.getModel().insert(new Object[] { edge }, attributes, cs, null, null);

			if (info.getRDFSSubList().size() > 0) {
				createRDFSGraph(graph.getModel(), info, pCell, pPort);
			}
		}
	}

	/** graphModel -> class or property */
	private void createRDFSGraph(GraphModel graphModel, RDFSInfo supInfo, GraphCell supCell, Port supPort) throws RDFException {
		Map attributes = new HashMap();
		for (Iterator rdfsSubList = supInfo.getRDFSSubList().iterator(); rdfsSubList.hasNext();) {
			Resource subRes = (Resource) rdfsSubList.next();
			RDFSInfo subInfo = rdfsInfoMap.getResourceInfo(subRes);

			DefaultGraphCell subCell = null;
			if (supInfo instanceof ClassInfo) {
				subCell = (DefaultGraphCell) gmanager.getClassCell(subRes, false);
			} else if (subInfo instanceof PropertyInfo) {
				subCell = (DefaultGraphCell) gmanager.getPropertyCell(subRes, false);
			} else {
				System.err.println("cannot reach");
			}

			Port subPort = (Port) subCell.getChildAt(0);
			rdfsInfoMap.putCellInfo(subCell, subInfo);
			subInfo.addSupRDFS(supCell);

			Edge edge = getEdge(attributes, "");
			ConnectionSet cs = new ConnectionSet(edge, supPort, subPort);
			graphModel.insert(new Object[] { edge }, attributes, cs, null, null);

			if (subInfo.getRDFSSubList().size() > 0) {
				createRDFSGraph(graphModel, subInfo, subCell, subPort);
			}
		}
	}

	private Edge getEdge(Map attributes, String str) {
		DefaultEdge edge = new DefaultEdge(str);
		Map edgeMap = cellMaker.getEdgeMap(str);
		attributes.put(edge, edgeMap);
		return edge;
	}

	private void setResourceInfo(Object cell, Resource uri, GraphCell typeCell) throws RDFException {
		RDFResourceInfo resInfo = null;
		if (uri.isAnon()) {
			resInfo = new RDFResourceInfo(URIType.ANONYMOUS, uri.getId().toString(), typeCell);
		} else {
			resInfo = new RDFResourceInfo(URIType.URI, uri.toString(), typeCell);
		}
		resInfoMap.putCellInfo(cell, resInfo);
	}

	// TypeCellが作られた後に呼び出す
	private boolean isTypeProperty(Statement stmt, Object subjectCell) {
		Property predicate = stmt.getPredicate(); // get the predicate
		RDFNode object = stmt.getObject(); // get the object

		if (predicate.equals(RDF.type)) {
			Object cell = gmanager.getClassCell((Resource) object, false);
			RDFResourceInfo info = resInfoMap.getCellInfo(subjectCell);
			info.setTypeCell(cell);
			return true;
		} else {
			return false;
		}
	}

	private DefaultGraphCell createResourceCell(Object res, Map resMap, Map portMap, Map attr, Point point) {
		DefaultGraphCell resCell = new RDFResourceCell(res.toString());
		DefaultPort port = new DefaultPort();
		resCell.add(port);
		portMap.put(res, port);
		resMap.put(res, resCell);
		attr.put(resCell, cellMaker.getResourceMap(point, ChangeCellAttributes.rdfResourceColor));

		return resCell;
	}

	private DefaultGraphCell createTypeCell(Map attributes, Point point) {
		DefaultGraphCell typeCell = new TypeCell("");
		attributes.put(typeCell, cellMaker.getResourceMap(point, ChangeCellAttributes.classColor));
		return typeCell;
	}

	private DefaultGraphCell createLiteralCell(Literal literal, Map portMap, Map attr, Point point) throws RDFException {
		//literal = "<html>" + literal + "</html>";
		//literal = literal.replaceAll("(\n|\r)+", "<br>");
		String str = literal.getString();
		DefaultGraphCell litCell = new RDFLiteralCell(str);
		DefaultPort tp = new DefaultPort();
		litCell.add(tp);
		portMap.put(literal, tp);
		attr.put(litCell, cellMaker.getResourceMap(point, ChangeCellAttributes.literalColor));

		return litCell;
	}

	private void insertGroup(GraphModel model, Object cell, Object typeCell, Map attr) {
		ParentMap parentMap = new ParentMap();
		DefaultGraphCell group = new DefaultGraphCell();

		parentMap.addEntry(cell, group);
		parentMap.addEntry(typeCell, group);

		model.insert(new Object[] { cell, typeCell, group }, attr, null, parentMap, null);
	}

	private RDFGraph createRDFGraph(Model model, GraphType gType) throws RDFException {
		Map portMap = new HashMap();
		Map resourceMap = new HashMap();
		Map attributes = new HashMap();
		RDFGraph graph = new RDFGraph();
		GraphModel graphModel = graph.getModel();

		int x = 0;
		int y = 0;

		for (StmtIterator iter = model.listStatements(); iter.hasNext();) {
			Statement stmt = iter.nextStatement(); // get next statement
			Resource subject = stmt.getSubject(); // get the subject
			Property predicate = stmt.getPredicate(); // get the predicate
			RDFNode object = stmt.getObject(); // get the object

			// Resource
			DefaultGraphCell source = (DefaultGraphCell) resourceMap.get(subject);
			if (source == null) {
				source = createResourceCell(subject, resourceMap, portMap, attributes, new Point(x, y));
				if (gType == GraphType.RDF) {
					DefaultGraphCell typeCell = createTypeCell(attributes, new Point(x, y - 25));
					setResourceInfo(source, subject, typeCell);
					insertGroup(graphModel, source, typeCell, attributes);
				}
			}

			y += 150;
			if (y > 500) {
				y = 150;
				x += 150;
			}

			// プロパティがrdfs:typeならば，グラフに描画しない．
			if (gType == GraphType.RDF && isTypeProperty(stmt, source)) {
				continue;
			}

			// Object
			DefaultGraphCell target = null;
			if (object instanceof Resource) {
				target = (DefaultGraphCell) resourceMap.get(object);
				if (target == null) {
					target = createResourceCell(object, resourceMap, portMap, attributes, new Point(x, y));
					if (gType == GraphType.RDF) {
						DefaultGraphCell typeCell = createTypeCell(attributes, new Point(x, y - 25));
						setResourceInfo(target, (Resource) object, typeCell);
						insertGroup(graphModel, target, typeCell, attributes);
					}
				}
			} else if (object instanceof Literal) {
				Literal literal = (Literal) object;
				target = createLiteralCell(literal, portMap, attributes, new Point(x, y));
				litInfoMap.putCellInfo(target, literal);
				graphModel.insert(new Object[] { target }, attributes, null, null, null);
			} else {
				System.err.println("cannot reach");
			}

			y += 150;
			if (y > 500) {
				y = 150;
				x += 150;
			}

			Edge edge = getEdge(attributes, predicate.toString());
			//			putPropertyInfo(stmt, edge);
			/*********************************************************************/
			/* 後で直す*/
			rdfsInfoMap.putEdgeInfo(edge, gmanager.getPropertyCell(predicate, false));

			Port sp = (Port) portMap.get(subject);
			Port tp = (Port) portMap.get(object);
			if (sp == tp) { // self connect
				Map map = edge.getAttributes();
				GraphConstants.setRouting(map, GraphConstants.ROUTING_SIMPLE);
				attributes.put(edge, map);
			}
			ConnectionSet cs = new ConnectionSet(edge, sp, tp);
			if (gType == GraphType.RDF) {
				graphModel.insert(new Object[] { edge }, attributes, cs, null, null);
			} else if (gType == GraphType.REAL_RDF) {
				graphModel.insert(new Object[] { source, target, edge }, attributes, cs, null, null);
			}
		}

		return graph;
	}

	public RDFGraph convertRDFToJGraph(Model model) {
		RDFGraph graph = null;
		try {
			graph = createRDFGraph(model, GraphType.RDF);
		} catch (RDFException e) {
			e.printStackTrace();
		}
		return graph;
	}

	public RDFGraph mergeRDFToJGraph(Reader reader, Model currentModel) {
		RDFGraph graph = null;
		Model model = new ModelMem(); //create RDFModel

		try {
			model.read(reader, gmanager.getBaseURI());
			model.add(currentModel);
			gmanager.getRDFGraph().removeAllCells();
			graph = createRDFGraph(model, GraphType.RDF);
		} catch (RDFException e) {
			e.printStackTrace();
		}
		return graph;
	}

	public RDFGraph convertRealRDFToJGraph(Reader reader) {
		RDFGraph graph = null;
		Model model = new ModelMem(); //create RDFModel

		try {
			model.read(reader, "");
			graph = createRDFGraph(model, GraphType.REAL_RDF);
		} catch (RDFException e) {
			e.printStackTrace();
		}
		return graph;
	}
}
