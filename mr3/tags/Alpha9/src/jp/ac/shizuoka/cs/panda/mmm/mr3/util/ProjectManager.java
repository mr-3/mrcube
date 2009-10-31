/*
 * Created on 2003/07/12
 *
 */
package jp.ac.shizuoka.cs.panda.mmm.mr3.util;

import java.awt.*;
import java.util.*;

import jp.ac.shizuoka.cs.panda.mmm.mr3.data.*;
import jp.ac.shizuoka.cs.panda.mmm.mr3.jgraph.*;
import jp.ac.shizuoka.cs.panda.mmm.mr3.ui.*;
import jp.ac.shizuoka.cs.panda.mmm.mr3.ui.NameSpaceTableDialog.*;

import org.jgraph.graph.*;

import com.hp.hpl.jena.mem.*;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.rdf.model.impl.*;
import com.hp.hpl.jena.vocabulary.*;

/**
 * @author takeshi morita
 */
public class ProjectManager {

	private GraphManager gmanager;
	private NSTableModel nsTableModel;
	private NameSpaceTableDialog nsTableDialog;
	private RDFResourceInfoMap resInfoMap = RDFResourceInfoMap.getInstance();
	private RDFSInfoMap rdfsInfoMap = RDFSInfoMap.getInstance();
	private RDFLiteralInfoMap litInfoMap = RDFLiteralInfoMap.getInstance();

	public ProjectManager(GraphManager gm, NameSpaceTableDialog nsTableD) {
		gmanager = gm;
		nsTableDialog = nsTableD;
		nsTableModel = nsTableD.getNSTableModel();
	}

	/*
	 * RDFモデルのプロジェクトの保存に関する値を保存
	 * 
	 */
	private void addRDFProjectModel(Model projectModel) throws RDFException {
		int literal_cnt = 0;
		RDFGraph graph = gmanager.getRDFGraph();
		Object[] cells = graph.getAllCells();
		for (int i = 0; i < cells.length; i++) {
			GraphCell cell = (GraphCell) cells[i];
			if (graph.isRDFResourceCell(cell)) {
				addRDFResourceProjectModel(projectModel, cell);
			} else if (graph.isRDFPropertyCell(cell)) {
				literal_cnt = addRDFLiteralProjectModel(projectModel, literal_cnt, cell);
			}
		}
	}

	/*
	 * x,y,width,heightを保存．
	 * typeがない場合には，Emptyとする．
	 */
	private void addRDFResourceProjectModel(Model projectModel, GraphCell cell) throws RDFException {
		Rectangle rec = GraphConstants.getBounds(cell.getAttributes());
		RDFResourceInfo info = resInfoMap.getCellInfo(cell);
		projectModel.add(info.getURI(), MR3Resource.PointX, rec.getX());
		projectModel.add(info.getURI(), MR3Resource.PointY, rec.getY());
		projectModel.add(info.getURI(), MR3Resource.NodeWidth, rec.getWidth());
		projectModel.add(info.getURI(), MR3Resource.NodeHeight, rec.getHeight());
		if (info.getTypeCell() == null) {
			projectModel.add(info.getURI(), RDF.type, MR3Resource.Empty);
		}
	}

	/*
	 * リテラルの情報の保存．
	 * 
	 */
	private int addRDFLiteralProjectModel(Model projectModel, int literal_cnt, GraphCell cell) throws RDFException {
		Edge edge = (Edge) cell;
		RDFGraph graph = gmanager.getRDFGraph();
		GraphCell sourceCell = (GraphCell) graph.getSourceVertex(edge);
		GraphCell targetCell = (GraphCell) graph.getTargetVertex(edge);
		if (graph.isRDFLiteralCell(targetCell)) {
			RDFResourceInfo info = resInfoMap.getCellInfo(sourceCell);

			Object propCell = rdfsInfoMap.getEdgeInfo(edge);
			RDFSInfo propInfo = rdfsInfoMap.getCellInfo(propCell);

			Resource litRes = new ResourceImpl(MR3Resource.Literal + Integer.toString(literal_cnt++));
			projectModel.add(litRes, MR3Resource.HasLiteralResource, info.getURI());
			if (propInfo == null) {
				projectModel.add(litRes, MR3Resource.LiteralProperty, MR3Resource.Nil);
			} else {
				projectModel.add(litRes, MR3Resource.LiteralProperty, propInfo.getURI());
			}

			Literal litInfo = litInfoMap.getCellInfo(targetCell);
			projectModel.add(litRes, MR3Resource.LiteralLang, litInfo.getLanguage());
			projectModel.add(litRes, MR3Resource.LiteralDatatype, litInfo.getDatatypeURI());
			projectModel.add(litRes, MR3Resource.LiteralString, litInfo.getString());

			Rectangle rec = GraphConstants.getBounds(targetCell.getAttributes());
			projectModel.add(litRes, MR3Resource.PointX, rec.getX());
			projectModel.add(litRes, MR3Resource.PointY, rec.getY());
			projectModel.add(litRes, MR3Resource.NodeWidth, rec.getWidth());
			projectModel.add(litRes, MR3Resource.NodeHeight, rec.getHeight());
		}
		return literal_cnt;
	}

	private void addRDFSProjectModel(Model projectModel, RDFGraph graph) throws RDFException {
		Object[] cells = graph.getAllCells();
		for (int i = 0; i < cells.length; i++) {
			if (graph.isRDFSCell(cells[i])) {
				GraphCell cell = (GraphCell) cells[i];
				Rectangle rec = GraphConstants.getBounds(cell.getAttributes());
				RDFSInfo info = rdfsInfoMap.getCellInfo(cell);
				projectModel.add(info.getURI(), MR3Resource.PointX, rec.getX());
				projectModel.add(info.getURI(), MR3Resource.PointY, rec.getY());
				projectModel.add(info.getURI(), MR3Resource.NodeWidth, rec.getWidth());
				projectModel.add(info.getURI(), MR3Resource.NodeHeight, rec.getHeight());
			}
		}
	}

	private static final int IS_AVAILABLE_COLUMN = 0;
	private static final int PREFIX_COLUMN = 1;
	private static final int NS_COLUMN = 2;

	private void addPrefixNSProjectModel(Model projectModel) throws RDFException {
		for (int i = 0; i < nsTableModel.getRowCount(); i++) {
			Boolean isAvailable = (Boolean) nsTableModel.getValueAt(i, IS_AVAILABLE_COLUMN);
			String prefix = (String) nsTableModel.getValueAt(i, PREFIX_COLUMN);
			String nameSpace = (String) nsTableModel.getValueAt(i, NS_COLUMN);
			projectModel.add(new ResourceImpl(nameSpace), MR3Resource.IsPrefixAvailable, isAvailable.toString());
			projectModel.add(new ResourceImpl(nameSpace), MR3Resource.Prefix, prefix);
		}
	}

	private void addDefaultLangModel(Model projectModel) throws RDFException {
		projectModel.add(MR3Resource.DefaultURI, MR3Resource.DefaultLang, gmanager.getDefaultLang());
	}

	/*
	 * RDF，クラス，プロパティのそれぞれのプロジェクト保存に必要な値をＲＤＦモデル
	 * として保存する．（Ｘ，Ｙ座標など）
	 */
	public Model getProjectModel() {
		Model projectModel = new ModelMem();
		try {
			addDefaultLangModel(projectModel);
			addRDFProjectModel(projectModel);
			addRDFSProjectModel(projectModel, gmanager.getClassGraph());
			addRDFSProjectModel(projectModel, gmanager.getPropertyGraph());
			addPrefixNSProjectModel(projectModel);
		} catch (RDFException e) {
			e.printStackTrace();
		}
		return projectModel;
	}

	/*
	 * RDFのモデルから，リテラルをもつステートメント集合のモデルを得る
	 */
	public Model getLiteralModel(Model model) {
		Model literalModel = new ModelMem();
		try {
			for (StmtIterator i = model.listStatements(); i.hasNext();) {
				Statement stmt = i.nextStatement();
				if (stmt.getObject() instanceof Literal) {
					literalModel.add(stmt);
				}
			}
		} catch (RDFException e) {
			e.printStackTrace();
		}
		return literalModel;
	}

	private boolean hasProjectPredicate(Statement stmt) {
		return (
			stmt.getPredicate().equals(MR3Resource.DefaultLang)
				|| stmt.getPredicate().equals(MR3Resource.PointX)
				|| stmt.getPredicate().equals(MR3Resource.PointY)
				|| stmt.getPredicate().equals(MR3Resource.NodeWidth)
				|| stmt.getPredicate().equals(MR3Resource.NodeHeight)
				|| stmt.getPredicate().equals(MR3Resource.LiteralProperty)
				|| stmt.getPredicate().equals(MR3Resource.HasLiteralResource)
				|| stmt.getPredicate().equals(MR3Resource.LiteralLang)
				|| stmt.getPredicate().equals(MR3Resource.LiteralDatatype)
				|| stmt.getPredicate().equals(MR3Resource.LiteralString)
				|| stmt.getPredicate().equals(MR3Resource.Prefix)
				|| stmt.getPredicate().equals(MR3Resource.IsPrefixAvailable));
	}

	public Model extractProjectModel(Model model) {
		Model extractModel = ModelFactory.createDefaultModel();
		try {
			for (StmtIterator i = model.listStatements(); i.hasNext();) {
				Statement stmt = i.nextStatement();
				if (hasProjectPredicate(stmt)) {
					extractModel.add(stmt);
				}
			}
			model.remove(extractModel);
		} catch (RDFException e) {
			e.printStackTrace();
		}		
		return extractModel;
	}

	private void changeNSModel(Map uriPrefixMap, Map uriIsAvailableMap) {
		Set existNSSet = new HashSet();
		for (int i = 0; i < nsTableModel.getRowCount(); i++) {
			String nameSpace = (String) nsTableModel.getValueAt(i, NS_COLUMN);
			String prefix = (String) uriPrefixMap.get(nameSpace);
			Boolean isAvailable = (Boolean) uriIsAvailableMap.get(nameSpace);

			if (!nsTableModel.getValueAt(i, PREFIX_COLUMN).equals(prefix)) {
				nsTableModel.setValueAt(prefix, i, PREFIX_COLUMN);
			}
			nsTableModel.setValueAt(isAvailable, i, IS_AVAILABLE_COLUMN);
			existNSSet.add(nameSpace);
		}

		Collection notExistNSSet = uriPrefixMap.keySet();
		notExistNSSet.removeAll(existNSSet);
		for (Iterator i = notExistNSSet.iterator(); i.hasNext();) {
			String nameSpace = (String) i.next();
			String prefix = (String) uriPrefixMap.get(nameSpace);
			Boolean isAvailable = (Boolean) uriIsAvailableMap.get(nameSpace);
			nsTableDialog.addNameSpaceTable(isAvailable, prefix, nameSpace);
		}
	}

	public void removeEmptyClass() {
		RDFGraph graph = gmanager.getRDFGraph();
		Object[] cells = graph.getAllCells();
		for (int i = 0; i < cells.length; i++) {
			GraphCell cell = (GraphCell) cells[i];
			if (graph.isRDFResourceCell(cell)) {
				RDFResourceInfo info = resInfoMap.getCellInfo(cell);
				if (info.getType().equals(MR3Resource.Empty)) {
					info.setTypeCell(null);
					resInfoMap.putCellInfo(cell, info);
				}
			}
		}
		graph = gmanager.getClassGraph();
		Object cell = rdfsInfoMap.getClassCell(MR3Resource.Empty);
		graph.clearSelection();
		graph.setSelectionCell(cell);
		gmanager.initRemoveAction(graph);
		gmanager.removeAction();
	}

	public void loadProject(Model model) {
		Map uriNodeInfoMap = new HashMap(); // リソースのＵＲＩとMR3Literalのマップ
		Map uriPrefixMap = new HashMap(); // URIとプレフィックスのマップ
		Map uriIsAvailableMap = new HashMap(); // URIとisAvailable(boolean)のマップ 

		try {
			for (StmtIterator i = model.listStatements(); i.hasNext();) {
				Statement stmt = i.nextStatement();
				MR3Literal rec = (MR3Literal) uriNodeInfoMap.get(stmt.getSubject());
				if (rec == null) {
					rec = new MR3Literal();
					uriNodeInfoMap.put(stmt.getSubject(), rec);
				}
				if (stmt.getPredicate().equals(MR3Resource.DefaultLang)) {
					gmanager.setDefaultLang(stmt.getObject().toString());
				} else if (stmt.getPredicate().equals(MR3Resource.PointX)) {
					setPositionX(uriNodeInfoMap, stmt, rec);
				} else if (stmt.getPredicate().equals(MR3Resource.PointY)) {
					setPositionY(uriNodeInfoMap, stmt, rec);
				} else if (stmt.getPredicate().equals(MR3Resource.NodeWidth)) {
					setNodeWidth(uriNodeInfoMap, stmt, rec);
				} else if (stmt.getPredicate().equals(MR3Resource.NodeHeight)) {
					setNodeHeight(uriNodeInfoMap, stmt, rec);
				} else if (stmt.getPredicate().equals(MR3Resource.LiteralLang)) {
					rec.setLanguage(stmt.getObject().toString());
					uriNodeInfoMap.put(stmt.getSubject(), rec);
				} else if (stmt.getPredicate().equals(MR3Resource.LiteralDatatype)) {
					rec.setDatatype(stmt.getObject().toString());
					uriNodeInfoMap.put(stmt.getSubject(), rec);
				} else if (stmt.getPredicate().equals(MR3Resource.LiteralString)) {
					rec.setString(stmt.getObject().toString());
					uriNodeInfoMap.put(stmt.getSubject(), rec);
				} else if (stmt.getPredicate().equals(MR3Resource.Prefix)) {
					uriPrefixMap.put(stmt.getSubject().getURI(), stmt.getObject().toString());
				} else if (stmt.getPredicate().equals(MR3Resource.HasLiteralResource)) {
					rec.setResource(stmt.getObject().toString());
					uriNodeInfoMap.put(stmt.getSubject(), rec);
				} else if (stmt.getPredicate().equals(MR3Resource.LiteralProperty)) {
					rec.setProperty(stmt.getObject().toString());
					uriNodeInfoMap.put(stmt.getSubject(), rec);
				} else if (stmt.getPredicate().equals(MR3Resource.IsPrefixAvailable)) {
					if (stmt.getObject().toString().equals("true")) {
						uriIsAvailableMap.put(stmt.getSubject().getURI(), new Boolean(true));
					} else {
						uriIsAvailableMap.put(stmt.getSubject().getURI(), new Boolean(false));
					}
				}
			}
		} catch (RDFException e) {
			e.printStackTrace();
		}
		gmanager.setNodeBounds(uriNodeInfoMap);
		changeNSModel(uriPrefixMap, uriIsAvailableMap);
	}

	private void setNodeWidth(Map uriNodeInfoMap, Statement stmt, MR3Literal rec) {
		int width = (int) Float.parseFloat(stmt.getObject().toString());
		rec.setWidth(width);
		uriNodeInfoMap.put(stmt.getSubject(), rec);
	}

	private void setNodeHeight(Map uriNodeInfoMap, Statement stmt, MR3Literal rec) {
		int height = (int) Float.parseFloat(stmt.getObject().toString());
		rec.setHeight(height);
		uriNodeInfoMap.put(stmt.getSubject(), rec);
	}

	private void setPositionY(Map uriNodeInfoMap, Statement stmt, MR3Literal rec) {
		int y = (int) Float.parseFloat(stmt.getObject().toString());
		rec.setY(y);
		uriNodeInfoMap.put(stmt.getSubject(), rec);
	}

	private void setPositionX(Map uriNodeInfoMap, Statement stmt, MR3Literal rec) {
		int x = (int) Float.parseFloat(stmt.getObject().toString());
		rec.setX(x);
		uriNodeInfoMap.put(stmt.getSubject(), rec);
	}

}
