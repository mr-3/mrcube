/*
 * Project Name: MR^3 (Meta-Model Management based on RDFs Revision Reflection)
 * Project Website: http://mrcube.org/
 * 
 * Copyright (C) 2003-2018 Yamaguchi Laboratory, Keio University. All rights reserved.
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

package org.mrcube.utils;

import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.datatypes.TypeMapper;
import org.apache.jena.rdf.model.*;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.jgraph.graph.Edge;
import org.jgraph.graph.GraphCell;
import org.jgraph.graph.GraphConstants;
import org.mrcube.MR3;
import org.mrcube.jgraph.GraphManager;
import org.mrcube.jgraph.RDFGraph;
import org.mrcube.layout.GraphLayoutData;
import org.mrcube.models.*;
import org.mrcube.views.NameSpaceTableDialog;
import org.mrcube.views.NameSpaceTableDialog.NSTableModel;

import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.*;

/**
 * @author Takeshi Morita
 */
public class ProjectManager {

	private GraphManager gmanager;
	private NSTableModel nsTableModel;
	private NameSpaceTableDialog nsTableDialog;

	public ProjectManager(GraphManager gm) {
		gmanager = gm;
		nsTableDialog = gmanager.getNSTableDialog();
		nsTableModel = nsTableDialog.getNSTableModel();
	}

	/*
	 * RDFモデルのプロジェクトの保存に関する値を保存
	 */
	private void addRDFProjectModel(Model projectModel) {
		int literal_cnt = 0;
		RDFGraph graph = gmanager.getCurrentRDFGraph();
		Object[] cells = graph.getAllCells();
		for (Object cell1 : cells) {
			GraphCell cell = (GraphCell) cell1;
			if (RDFGraph.isRDFResourceCell(cell)) {
				addRDFResourceProjectModel(projectModel, cell);
			} else if (RDFGraph.isRDFPropertyCell(cell)) {
				literal_cnt = addRDFLiteralProjectModel(projectModel, literal_cnt, cell);
			}
		}
	}

	/*
	 * x,y,width,heightを保存． typeがない場合には，Emptyとする．
	 */
	private void addRDFResourceProjectModel(Model projectModel, GraphCell cell) {
		Rectangle2D rec = GraphConstants.getBounds(cell.getAttributes());
		RDFResourceModel info = (RDFResourceModel) GraphConstants.getValue(cell.getAttributes());
		Literal x = ResourceFactory.createPlainLiteral(String.valueOf(rec.getX()));
		projectModel.add(info.getURI(), MR3Resource.PointX, x);
		Literal y = ResourceFactory.createPlainLiteral(String.valueOf(rec.getY()));
		projectModel.add(info.getURI(), MR3Resource.PointY, y);
		Literal width = ResourceFactory.createPlainLiteral(String.valueOf(rec.getWidth()));
		projectModel.add(info.getURI(), MR3Resource.NodeWidth, width);
		Literal height = ResourceFactory.createPlainLiteral(String.valueOf(rec.getHeight()));
		projectModel.add(info.getURI(), MR3Resource.NodeHeight, height);
		if (info.getTypeCell() == null) {
			projectModel.add(info.getURI(), RDF.type, MR3Resource.Empty);
		}
	}

	/*
	 * リテラルの情報の保存．
	 */
	private int addRDFLiteralProjectModel(Model projectModel, int literal_cnt, GraphCell cell) {
		Edge edge = (Edge) cell;
		RDFGraph graph = gmanager.getCurrentRDFGraph();
		GraphCell sourceCell = (GraphCell) graph.getSourceVertex(edge);
		GraphCell targetCell = (GraphCell) graph.getTargetVertex(edge);
		if (RDFGraph.isRDFLiteralCell(targetCell)) {
			RDFResourceModel info = (RDFResourceModel) GraphConstants.getValue(sourceCell.getAttributes());

			RDFSModel propInfo = (RDFSModel) GraphConstants.getValue(edge.getAttributes());
			Resource litRes = ResourceFactory.createResource(MR3Resource.Literal + Integer.toString(literal_cnt++));
			projectModel.add(litRes, MR3Resource.HasLiteralResource, info.getURI());
			if (propInfo == null) {
				projectModel.add(litRes, MR3Resource.LiteralProperty, MR3Resource.Nil);
			} else {
				projectModel.add(litRes, MR3Resource.LiteralProperty, propInfo.getURI());
			}

			MR3Literal litInfo = (MR3Literal) GraphConstants.getValue(targetCell.getAttributes());
			projectModel.add(litRes, MR3Resource.LiteralLang, litInfo.getLanguage());
			if (litInfo.getDatatype() != null) {
				projectModel.add(litRes, MR3Resource.LiteralDatatype, litInfo.getDatatype().getURI());
			}
			projectModel.add(litRes, MR3Resource.LiteralString, litInfo.getString());

			Rectangle2D rec = GraphConstants.getBounds(targetCell.getAttributes());
			Literal x = ResourceFactory.createPlainLiteral(String.valueOf(rec.getX()));
			projectModel.add(litRes, MR3Resource.PointX, x);
			Literal y = ResourceFactory.createPlainLiteral(String.valueOf(rec.getY()));
			projectModel.add(litRes, MR3Resource.PointY, y);
			Literal width = ResourceFactory.createPlainLiteral(String.valueOf(rec.getWidth()));
			projectModel.add(litRes, MR3Resource.NodeWidth, width);
			Literal height = ResourceFactory.createPlainLiteral(String.valueOf(rec.getHeight()));
			projectModel.add(litRes, MR3Resource.NodeHeight, height);
		}
		return literal_cnt;
	}

	private void addRDFSProjectModel(Model projectModel, RDFGraph graph) {
		Object[] cells = graph.getAllCells();
		for (Object cell1 : cells) {
			if (RDFGraph.isRDFSCell(cell1)) {
				GraphCell cell = (GraphCell) cell1;
				Rectangle2D rec = GraphConstants.getBounds(cell.getAttributes());
				RDFSModel info = (RDFSModel) GraphConstants.getValue(cell.getAttributes());
				Literal x = ResourceFactory.createPlainLiteral(String.valueOf(rec.getX()));
				projectModel.add(info.getURI(), MR3Resource.PointX, x);
				Literal y = ResourceFactory.createPlainLiteral(String.valueOf(rec.getY()));
				projectModel.add(info.getURI(), MR3Resource.PointY, y);
				Literal width = ResourceFactory.createPlainLiteral(String.valueOf(rec.getWidth()));
				projectModel.add(info.getURI(), MR3Resource.NodeWidth, width);
				Literal height = ResourceFactory.createPlainLiteral(String.valueOf(rec.getHeight()));
				projectModel.add(info.getURI(), MR3Resource.NodeHeight, height);
			}
		}
	}

	private static final int IS_AVAILABLE_COLUMN = 0;
	private static final int PREFIX_COLUMN = 1;
	private static final int NS_COLUMN = 2;

	private void addPrefixNSProjectModel(Model projectModel) {
		for (int i = 0; i < nsTableModel.getRowCount(); i++) {
			Boolean isAvailable = (Boolean) nsTableModel.getValueAt(i, IS_AVAILABLE_COLUMN);
			String prefix = (String) nsTableModel.getValueAt(i, PREFIX_COLUMN);
			String nameSpace = (String) nsTableModel.getValueAt(i, NS_COLUMN);
			projectModel.add(ResourceFactory.createResource(nameSpace), MR3Resource.IsPrefixAvailable, isAvailable.toString());
			projectModel.add(ResourceFactory.createResource(nameSpace), MR3Resource.Prefix, prefix);
		}
	}

	private void addDefaultLangModel(Model projectModel) {
		projectModel.add(MR3Resource.DefaultURI, MR3Resource.DefaultLang, GraphManager.getDefaultLang());
	}

	/*
	 * RDF，クラス，プロパティのそれぞれのプロジェクト保存に必要な値をＲＤＦモデル として保存する．（Ｘ，Ｙ座標など）
	 */
	public Model getProjectModel() {
		Model projectModel = ModelFactory.createDefaultModel();

		addDefaultLangModel(projectModel);
		addRDFProjectModel(projectModel);
		addRDFSProjectModel(projectModel, gmanager.getCurrentClassGraph());
		addRDFSProjectModel(projectModel, gmanager.getCurrentPropertyGraph());
		addPrefixNSProjectModel(projectModel);

		return projectModel;
	}

	/*
	 * RDFのモデルから，リテラルをもつステートメント集合のモデルを得る
	 */
	public Model getLiteralModel(Model model) {
		Model literalModel = ModelFactory.createDefaultModel();
		for (StmtIterator i = model.listStatements(); i.hasNext();) {
			Statement stmt = i.nextStatement();
			Property predicate = stmt.getPredicate();
			RDFNode object = stmt.getObject();
			if (object instanceof Literal && !(predicate.equals(RDFS.label) || predicate.equals(RDFS.comment))) {
				literalModel.add(stmt);
			}
		}

		return literalModel;
	}

	private boolean hasProjectPredicate(Statement stmt) {
		return (stmt.getPredicate().equals(MR3Resource.DefaultLang) || stmt.getPredicate().equals(MR3Resource.PointX)
				|| stmt.getPredicate().equals(MR3Resource.PointY) || stmt.getPredicate().equals(MR3Resource.NodeWidth)
				|| stmt.getPredicate().equals(MR3Resource.NodeHeight) || stmt.getPredicate().equals(MR3Resource.LiteralProperty)
				|| stmt.getPredicate().equals(MR3Resource.HasLiteralResource)
				|| stmt.getPredicate().equals(MR3Resource.LiteralLang) || stmt.getPredicate().equals(MR3Resource.LiteralDatatype)
				|| stmt.getPredicate().equals(MR3Resource.LiteralString) || stmt.getPredicate().equals(MR3Resource.Prefix) || stmt
				.getPredicate().equals(MR3Resource.IsPrefixAvailable));
	}

	public Model extractProjectModel(Model model) {
		Model extractModel = ModelFactory.createDefaultModel();

		for (StmtIterator i = model.listStatements(); i.hasNext();) {
			Statement stmt = i.nextStatement();
			if (hasProjectPredicate(stmt)) {
				extractModel.add(stmt);
			}
		}
		model.remove(extractModel);

		setCellLayoutMap(extractModel);
		return extractModel;
	}

	private void changeNSModel(Map<String, String> uriPrefixMap, Map<String, Boolean> uriIsAvailableMap) {
		Set<String> existNSSet = new HashSet<>();
		for (int i = 0; i < nsTableModel.getRowCount(); i++) {
			String nameSpace = (String) nsTableModel.getValueAt(i, NS_COLUMN);
			String prefix = uriPrefixMap.get(nameSpace);
			Boolean isAvailable = uriIsAvailableMap.get(nameSpace);

			if (prefix == null || isAvailable == null) {
				continue;
			}

			if (!nsTableModel.getValueAt(i, PREFIX_COLUMN).equals(prefix)) {
				nsTableModel.setValueAt(prefix, i, PREFIX_COLUMN);
			}
			nsTableModel.setValueAt(isAvailable, i, IS_AVAILABLE_COLUMN);
			existNSSet.add(nameSpace);
		}

		Collection<String> notExistNSSet = uriPrefixMap.keySet();
		notExistNSSet.removeAll(existNSSet);
		for (String nameSpace : notExistNSSet) {
			String prefix = uriPrefixMap.get(nameSpace);
			Boolean isAvailable = uriIsAvailableMap.get(nameSpace);
			nsTableDialog.addNameSpaceTable(isAvailable, prefix, nameSpace);
		}
	}

	public void removeEmptyClass() {
		RDFGraph graph = gmanager.getCurrentRDFGraph();
		Object[] cells = graph.getAllCells();
		for (Object cell1 : cells) {
			GraphCell cell = (GraphCell) cell1;
			if (RDFGraph.isRDFResourceCell(cell)) {
				RDFResourceModel info = (RDFResourceModel) GraphConstants.getValue(cell.getAttributes());
				if (info.getType().equals(MR3Resource.Empty)) {
					info.setTypeCell(null, gmanager.getCurrentRDFGraph());
					GraphConstants.setValue(cell.getAttributes(), info);
				}
			}
		}
		graph = gmanager.getCurrentClassGraph();
		RDFSModelMap rdfsModelMap = gmanager.getCurrentRDFSInfoMap();
		Object cell = rdfsModelMap.getClassCell(MR3Resource.Empty);
		graph.clearSelection();
		graph.setSelectionCell(cell);
		gmanager.removeAction(graph);
	}

	private int getStmtCount(Model model) {
		int total = 0;
		for (Iterator i = model.listStatements(); i.hasNext();) {
			i.next();
			total++;
		}
		return total;
	}

	private static Map<RDFNode, GraphLayoutData> layoutMap;

	public static Map<RDFNode, GraphLayoutData> getLayoutMap() {
		return layoutMap;
	}

	public void initLayoutMap() {
		layoutMap = null;
	}

	public void setCellLayoutMap(Model model) {
		layoutMap = new HashMap<>();

		for (StmtIterator i = model.listStatements(); i.hasNext();) {
			Statement stmt = i.nextStatement();
			GraphLayoutData data = layoutMap.get(stmt.getSubject());
			if (data == null) {
				data = new GraphLayoutData(stmt.getSubject());
			}
			if (stmt.getPredicate().equals(MR3Resource.PointX)) {
				int x = (int) Float.parseFloat(stmt.getObject().toString());
				Point2D.Double point = data.getPosition();
				data.setPosition(x, point.y);
			} else if (stmt.getPredicate().equals(MR3Resource.PointY)) {
				int y = (int) Float.parseFloat(stmt.getObject().toString());
				Point2D.Double point = data.getPosition();
				data.setPosition(point.x, y);
			} else if (stmt.getPredicate().equals(MR3Resource.NodeWidth)) {
				int width = (int) Float.parseFloat(stmt.getObject().toString());
				Dimension dimension = data.getBoundingBox();
				data.setBoundingBox(width, dimension.height);
			} else if (stmt.getPredicate().equals(MR3Resource.NodeHeight)) {
				int height = (int) Float.parseFloat(stmt.getObject().toString());
				Dimension dimension = data.getBoundingBox();
				data.setBoundingBox(dimension.width, height);
			}
			layoutMap.put(stmt.getSubject(), data);
		}
	}

	public void loadProject(Model model) {
		Map<Resource, MR3Literal> uriNodeInfoMap = new HashMap<>(); // リソースのＵＲＩとMR3Literalのマップ
		Map<String, String> uriPrefixMap = new HashMap<>(); // URIとプレフィックスのマップ
		Map<String, Boolean> uriIsAvailableMap = new HashMap<>(); // URIとisAvailable(boolean)のマップ

		MR3.STATUS_BAR.initNormal(getStmtCount(model));
		for (StmtIterator i = model.listStatements(); i.hasNext();) {
			Statement stmt = i.nextStatement();
			MR3Literal rec = uriNodeInfoMap.get(stmt.getSubject());
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
				RDFDatatype literalType = TypeMapper.getInstance().getSafeTypeByName(stmt.getObject().asLiteral().toString());
				rec.setDatatype(literalType);
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
					uriIsAvailableMap.put(stmt.getSubject().getURI(), Boolean.TRUE);
				} else {
					uriIsAvailableMap.put(stmt.getSubject().getURI(), Boolean.FALSE);
				}
			}
			MR3.STATUS_BAR.addValue();
		}

		gmanager.setNodeBounds(uriNodeInfoMap);
		changeNSModel(uriPrefixMap, uriIsAvailableMap);
		MR3.STATUS_BAR.hideProgressBar();
		initLayoutMap();
	}

	private void setNodeWidth(Map<Resource, MR3Literal> uriNodeInfoMap, Statement stmt, MR3Literal rec) {
		int width = (int) Float.parseFloat(stmt.getObject().toString());
		rec.setWidth(width);
		uriNodeInfoMap.put(stmt.getSubject(), rec);
	}

	private void setNodeHeight(Map<Resource, MR3Literal> uriNodeInfoMap, Statement stmt, MR3Literal rec) {
		int height = (int) Float.parseFloat(stmt.getObject().toString());
		rec.setHeight(height);
		uriNodeInfoMap.put(stmt.getSubject(), rec);
	}

	private void setPositionY(Map<Resource, MR3Literal> uriNodeInfoMap, Statement stmt, MR3Literal rec) {
		int y = (int) Float.parseFloat(stmt.getObject().toString());
		rec.setY(y);
		uriNodeInfoMap.put(stmt.getSubject(), rec);
	}

	private void setPositionX(Map<Resource, MR3Literal> uriNodeInfoMap, Statement stmt, MR3Literal rec) {
		int x = (int) Float.parseFloat(stmt.getObject().toString());
		rec.setX(x);
		uriNodeInfoMap.put(stmt.getSubject(), rec);
	}

}
