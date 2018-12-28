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

package org.mrcube.jgraph;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.jgraph.JGraph;
import org.jgraph.graph.*;
import org.jgraph.plaf.basic.BasicGraphUI;
import org.mrcube.actions.EditConceptAction;
import org.mrcube.actions.EditRDFPropertyAction;
import org.mrcube.io.MR3Reader;
import org.mrcube.models.*;
import org.mrcube.models.MR3Constants.HistoryType;
import org.mrcube.utils.GraphUtilities;
import org.mrcube.utils.Translator;
import org.mrcube.utils.Utilities;
import org.mrcube.views.HistoryManager;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Takeshi Morita
 */
public class RDFGraphUI extends BasicGraphUI {

	private RDFGraph graph;
	private GraphManager gmanager;
	private MR3Reader mr3Reader;
	private EditConceptAction editConceptAction;
	private EditRDFPropertyAction editRDFPropertyAction;
	private static final String WARNING = Translator.getString("Warning");

	RDFGraphUI(RDFGraph g, GraphManager gm) {
		graph = g;
		gmanager = gm;
		mr3Reader = new MR3Reader(gmanager);
		editConceptAction = new EditConceptAction(graph, gmanager);
		editRDFPropertyAction = new EditRDFPropertyAction(gmanager);
	}

	public RDFGraph getRDFGraph() {
		return graph;
	}

	protected void completeEditing(boolean messageStop, boolean messageCancel, boolean messageGraph) {
		if (editingCell == null) {
			return;
		}
		GraphCell cell = (GraphCell) editingCell;
		Object info = GraphConstants.getValue(cell.getAttributes());
		super.completeEditing(messageStop, messageCancel, messageGraph);
		if (RDFGraph.isRDFLiteralCell(cell)) {
			if (info instanceof MR3Literal) {
				changeLiteralCell(cell, (MR3Literal) info);
			}
		} else if (stopEditingInCompleteEditing) {
			switch (GraphManager.cellViewType) {
			case LABEL:
				editLabel(cell, info);
				break;
			case URI:
				editURI(cell, info);
				break;
			case ID:
				editID(cell, info);
				break;
			}
		}
	}

	private void changeLiteralCell(GraphCell cell, MR3Literal literal) {
		MR3Literal beforeLiteral = (MR3Literal) GraphConstants.getValue(cell.getAttributes());
		MR3Literal newLiteral = new MR3Literal(cell.toString(), literal.getLanguage(),
				literal.getDatatype());
		GraphConstants.setValue(cell.getAttributes(), newLiteral);
		Dimension size = GraphUtilities.getAutoLiteralNodeDimention(gmanager, cell.toString());
		GraphUtilities.resizeCell(size, graph, cell);
		HistoryManager.saveHistory(HistoryType.EDIT_LITERAL_WITH_GRAPH, beforeLiteral, newLiteral);
	}

	private void changeResourceLabel(ResourceModel info, GraphCell cell) {
		String label = cell.toString().replaceAll("　", "");
		MR3Literal literal = info.getDefaultLabel(GraphManager.getDefaultLang());
		if (literal == null) {
			literal = new MR3Literal(label, GraphManager.getDefaultLang(), null);
			info.addLabel(literal);
		} else {
			literal.setString(label);
		}
		GraphConstants.setValue(cell.getAttributes(), info);
	}

	private void editLabel(GraphCell cell, Object info) {
		if (RDFGraph.isRDFResourceCell(cell)) {
			RDFResourceModel beforeInfo = new RDFResourceModel((RDFResourceModel) info);
			changeResourceLabel((ResourceModel) info, cell);
			GraphUtilities.resizeRDFResourceCell(gmanager, (RDFResourceModel) info, cell);
			HistoryManager.saveHistory(HistoryType.EDIT_RESOURCE_LABEL_WITH_GRAPH,
					beforeInfo.getLabelList(), ((RDFResourceModel) info).getLabelList());
		} else if (RDFGraph.isRDFPropertyCell(cell)) {
			// ラベルだけでは判別は困難なので何もしない
		} else if (RDFGraph.isRDFSClassCell(cell)) {
			RDFSModel beforeInfo = new ClassModel((ClassModel) info);
			changeResourceLabel((ResourceModel) info, cell);
			GraphUtilities.resizeRDFSResourceCell(gmanager, (RDFSModel) info, cell);
			gmanager.selectChangedRDFCells((RDFSModel) info);
			HistoryManager.saveHistory(HistoryType.EDIT_CLASS_LABEL_WITH_GRAPH,
					beforeInfo.getLabelList(), ((RDFSModel) info).getLabelList());
		} else if (RDFGraph.isRDFSPropertyCell(cell)) {
			RDFSModel beforeInfo = new PropertyModel((PropertyModel) info);
			changeResourceLabel((ResourceModel) info, cell);
			GraphUtilities.resizeRDFSResourceCell(gmanager, (RDFSModel) info, cell);
			gmanager.selectChangedRDFCells((RDFSModel) info);
			HistoryManager.saveHistory(HistoryType.EDIT_ONT_PROPERTY_LABEL_WITH_GRAPH,
					beforeInfo.getLabelList(), ((RDFSModel) info).getLabelList());
		}
	}

	private Resource getResource(String str) {
		String[] tokens = str.split(":");
		if (tokens.length != 2) {
			return null;
		}
		String prefix = tokens[0];
		String id = tokens[1];
		Set<NamespaceModel> namespaceModelSet = GraphUtilities.getNamespaceModelSet();
		for (NamespaceModel info : namespaceModelSet) {
			if (prefix.equals(info.getPrefix())) {
				return ResourceFactory.createResource(info.getNameSpace() + id);
			}
		}
		return null;
	}

	private boolean isValidResource(String newRes, String oldRes) {
		return !newRes.equals(oldRes)
				&& !gmanager.isDuplicatedWithDialog(newRes, null, graph.getType());
	}

	private void cancelAction(GraphCell cell) {
		graph.getGraphLayoutCache().editCell(cell, cell.getAttributes());
	}

	private void editURI(GraphCell cell, Object info) {
		if (cell.toString() == null) {
			cancelAction(cell);
			return;
		}
		Resource resource = getResource(cell.toString().replaceAll("　", ""));
		if (resource == null) {
			cancelAction(cell);
			return;
		}
		if (RDFGraph.isRDFResourceCell(cell)) {
			RDFResourceModel resInfo = (RDFResourceModel) info;
			if (isValidResource(resource.getURI(), resInfo.getURIStr())) {
				RDFResourceModel beforeInfo = new RDFResourceModel(resInfo);
				resInfo.setURI(resource.getURI());
				GraphConstants.setValue(cell.getAttributes(), resInfo);
				GraphUtilities.resizeRDFResourceCell(gmanager, resInfo, cell);
				HistoryManager.saveHistory(HistoryType.EDIT_RESOURCE_WITH_GRAPH, beforeInfo,
						resInfo);
			} else {
				cancelAction(cell);
			}
		} else if (RDFGraph.isRDFPropertyCell(cell)) {
			RDFSModel beforePropInfo = (RDFSModel) GraphConstants.getValue(cell.getAttributes());
			String beforePropURI = beforePropInfo.getURIStr();
			editRDFPropertyAction.setURIString(resource.getURI());
			editRDFPropertyAction.setEdge(cell);
			if (!editRDFPropertyAction.editRDFProperty()) {
				cancelAction(cell);
			} else {
				RDFSModel afterPropInfo = (RDFSModel) GraphConstants.getValue(cell.getAttributes());
				HistoryManager.saveHistory(HistoryType.EDIT_PROPERTY_WITH_GRAPH, beforePropURI,
						afterPropInfo.getURIStr());
			}
		} else if (RDFGraph.isRDFSCell(cell)) {
			editConceptAction.editWithGraph(resource.getURI(), (RDFSModel) info, cell);
		}
	}

	private void editID(GraphCell cell, Object info) {
		if (RDFGraph.isRDFResourceCell(cell)) {
			RDFResourceModel resInfo = (RDFResourceModel) info;
			String uri = resInfo.getURI().getNameSpace() + cell.toString().replaceAll("　", "");
			if (isValidResource(uri, resInfo.getURIStr())) {
				RDFResourceModel beforeInfo = new RDFResourceModel(resInfo);
				resInfo.setURI(uri);
				HistoryManager.saveHistory(HistoryType.EDIT_RESOURCE_WITH_GRAPH, beforeInfo,
						resInfo);
			}
			GraphConstants.setValue(cell.getAttributes(), info);
			GraphUtilities.resizeRDFResourceCell(gmanager, resInfo, cell);
		} else if (RDFGraph.isRDFPropertyCell(cell)) {
			// IDだけでは判別は困難なので何もしない
		} else if (RDFGraph.isRDFSCell(cell)) {
			String uri = gmanager.getBaseURI() + cell.toString().replaceAll("　", "");
			editConceptAction.editWithGraph(uri, (RDFSModel) info, cell);
		}
	}

	/**
	 * Paint the background of this graph. Calls paintGrid.
	 */
	protected void paintBackground(Graphics g) {
		Rectangle pageBounds = graph.getBounds();
		if (getRDFGraph().getBackgroundImage() != null) {
			// Use clip and pageBounds
			double s = graph.getScale();
			Graphics2D g2 = (Graphics2D) g;
			AffineTransform tmp = g2.getTransform();
			g2.scale(s, s);
			g.drawImage(getRDFGraph().getBackgroundImage().getImage(), 0, 0, graph);
			g2.setTransform(tmp);
		} else if (getRDFGraph().isPageVisible()) { // FIX: Use clip
			double w = getRDFGraph().getPageFormat().getWidth();
			double h = getRDFGraph().getPageFormat().getHeight();
			Point2D p = graph.toScreen(new Point2D.Double(w, h));
			w = p.getX();
			h = p.getY();
			g.setColor(graph.getHandleColor());
			g.fillRect(0, 0, graph.getWidth(), graph.getHeight());
			g.setColor(Color.darkGray);
			g.fillRect(3, 3, (int) w, (int) h);
			g.setColor(graph.getBackground());
			g.fillRect(1, 1, (int) w - 1, (int) h - 1);
			pageBounds = new Rectangle(0, 0, (int) w, (int) h);
		}
		if (graph.isGridVisible())
			paintGrid(graph.getGridSize(), g, pageBounds);
	}

	protected TransferHandler createTransferHandler() {
		return new RDFTransferHandler();
	}

	public class RDFTransferHandler extends GraphTransferHandler {

		protected void handleExternalDrop(JGraph graph, Object[] cells, Map nested,
				ConnectionSet cs, ParentMap pm, double dx, double dy) {
			Iterator it = cs.connections();
			while (it.hasNext()) {
				ConnectionSet.Connection conn = (ConnectionSet.Connection) it.next();
				if (!pm.getChangedNodes().contains(conn.getPort())
						&& !graph.getModel().contains(conn.getPort())) {
					it.remove();
				}
			}
			Map clones = graph.cloneCells(cells);
			graph.getGraphLayoutCache().insertClones(cells, clones, nested, cs, pm, dx, dy);
			((RDFGraph) graph).setCopyCells(clones.values().toArray());
		}

		public boolean importDataImpl(JComponent comp, Transferable t) {
			if (super.importDataImpl(comp, t)) {
				return true;
			}
			try {
				if (t.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
					List<File> importFileList = (List<File>) t
							.getTransferData(DataFlavor.javaFileListFlavor);
					if (importFileList.size() == 1) {
						File importFile = importFileList.get(0);
						// System.out.println(importFile.getAbsolutePath());
						BufferedInputStream bis = new BufferedInputStream(new FileInputStream(
								importFile));
						if (bis == null) {
							return false;
						}
						Model model = ModelFactory.createDefaultModel();
						model.read(bis, MR3Resource.getURI(), "RDF/XML");
						mr3Reader.mergeRDFPlusRDFSModel(model);
						return true;
					}
					Utilities.showErrorMessageDialog("Too much element");
					return false;
				}
			} catch (IOException ioe) {
				Utilities.showErrorMessageDialog("IOException");
				return false;
			} catch (UnsupportedFlavorException ufe) {
				Utilities.showErrorMessageDialog("Unsupported");
				return false;
			}
			return false;
		}
	}
}