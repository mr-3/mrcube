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

import org.jgraph.graph.*;
import org.mrcube.MR3;
import org.mrcube.actions.RemoveAction;
import org.mrcube.actions.TransformElementAction;
import org.mrcube.models.MR3Constants.GraphType;
import org.mrcube.models.MR3Constants.HistoryType;
import org.mrcube.models.MR3Constants.URIType;
import org.mrcube.models.MR3Resource;
import org.mrcube.models.PropertyInfo;
import org.mrcube.models.RDFResourceInfo;
import org.mrcube.models.RDFSInfo;
import org.mrcube.utils.MR3CellMaker;
import org.mrcube.utils.Translator;
import org.mrcube.utils.Utilities;
import org.mrcube.views.HistoryManager;
import org.mrcube.views.InsertRDFLiteralDialog;
import org.mrcube.views.InsertRDFResDialog;
import org.mrcube.views.InsertRDFSResDialog;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.lang.ref.WeakReference;
import java.util.List;
import java.util.*;

/**
 * 
 * @author Takeshi Morita
 * 
 */
public class RDFGraphMarqueeHandler extends BasicMarqueeHandler {

	protected RDFGraph graph;
	protected MR3CellMaker cellMaker;
	protected GraphManager gmanager;
	public transient JToggleButton moveButton = new JToggleButton();
	public transient JToggleButton connectButton = new JToggleButton();

	protected Rectangle bounds;
	protected Point2D start, current;
	protected PortView port, firstPort;

	private WeakReference<InsertRDFResDialog> insertRDFResDialogRef;
	private WeakReference<InsertRDFLiteralDialog> insertRDFLiteralDialogRef;
	protected WeakReference<InsertRDFSResDialog> insertRDFSResDialogRef;

	private ConnectAction moveAction;
	private ConnectAction connectAction;
	private InsertResourceAction insertResourceAction;
	private InsertLiteralAction insertLiteralAction;
	protected RemoveAction removeAction;

	private static String INSERT_RESOURCE_TITLE = Translator
			.getString("InsertResourceDialog.Title");
	private static String INSERT_LITERAL_TITLE = Translator.getString("InsertLiteralDialog.Title");
	private static Icon RDF_RESOURCE_ELLIPSE_ICON = Utilities
			.getImageIcon("rdf_resource_ellipse.png");
	private static Icon LITERAL_RECTANGLE_ICON = Utilities.getImageIcon("literal_rectangle.png");

	public RDFGraphMarqueeHandler(GraphManager manager, RDFGraph graph) {
		gmanager = manager;
		this.graph = graph;
		insertRDFResDialogRef = new WeakReference<InsertRDFResDialog>(null);
		insertRDFLiteralDialogRef = new WeakReference<InsertRDFLiteralDialog>(null);
		insertRDFSResDialogRef = new WeakReference<InsertRDFSResDialog>(null);
		cellMaker = new MR3CellMaker(gmanager);
		insertResourceAction = new InsertResourceAction();
		insertLiteralAction = new InsertLiteralAction();

		moveAction = new ConnectAction(Translator.getString("Action.Move.Text"),
				Utilities.getImageIcon(Translator.getString("Action.Move.Icon")));
		connectAction = new ConnectAction(Translator.getString("Action.Connect.Text"),
				Utilities.getImageIcon(Translator.getString("Action.Connect.Icon")));
		removeAction = new RemoveAction(graph, gmanager);
		setAction(graph);
	}

	private void setAction(JComponent panel) {
		ActionMap actionMap = panel.getActionMap();
		actionMap.put(insertResourceAction.getValue(Action.NAME), insertResourceAction);
		actionMap.put(insertLiteralAction.getValue(Action.NAME), insertLiteralAction);
		actionMap.put(moveAction.getValue(Action.NAME), moveAction);
		actionMap.put(connectAction.getValue(Action.NAME), connectAction);
		InputMap inputMap = panel.getInputMap(JComponent.WHEN_FOCUSED);
		inputMap.put(KeyStroke.getKeyStroke("control R"),
				insertResourceAction.getValue(Action.NAME));
		inputMap.put(KeyStroke.getKeyStroke("control L"), insertLiteralAction.getValue(Action.NAME));
		inputMap.put(KeyStroke.getKeyStroke("control G"), moveAction.getValue(Action.NAME));
		inputMap.put(KeyStroke.getKeyStroke("control G"), connectAction.getValue(Action.NAME));
	}

	private InsertRDFResDialog getInsertRDFResDialog(Object[] cells) {
		InsertRDFResDialog result = insertRDFResDialogRef.get();
		if (result == null) {
			result = new InsertRDFResDialog(gmanager);
			insertRDFResDialogRef = new WeakReference<InsertRDFResDialog>(result);
		}
		result.initData(cells);
		return result;
	}

	private InsertRDFLiteralDialog getInsertRDFLiteralDialog() {
		InsertRDFLiteralDialog result = insertRDFLiteralDialogRef.get();
		if (result == null) {
			result = new InsertRDFLiteralDialog(gmanager.getRootFrame());
			insertRDFLiteralDialogRef = new WeakReference<InsertRDFLiteralDialog>(result);
		}
		result.initData();
		return result;
	}

	protected InsertRDFSResDialog getInsertRDFSResDialog(String title) {
		InsertRDFSResDialog result = insertRDFSResDialogRef.get();
		if (result == null) {
			result = new InsertRDFSResDialog(gmanager);
			insertRDFSResDialogRef = new WeakReference<InsertRDFSResDialog>(result);
		}
		result.initData(title);
		return result;
	}

	protected boolean isPopupTrigger(MouseEvent e) {
		return SwingUtilities.isRightMouseButton(e) && !e.isShiftDown();
	}

	public boolean isForceMarqueeEvent(MouseEvent e) {
		return !moveButton.isSelected() || isPopupTrigger(e) || super.isForceMarqueeEvent(e);
	}

	// Display PopupMenu or Remember Start Location and First Port
	public void mousePressed(final MouseEvent e) {
		if (isPopupTrigger(e)) { // If Right Mouse Button
			Point2D loc = graph.fromScreen(e.getPoint());
			Object cell = graph.getFirstCellForLocation(loc.getX(), loc.getY());
			JPopupMenu menu = createPopupMenu(e.getPoint(), cell);
			menu.show(graph, e.getX(), e.getY());
		} else if (port != null && !e.isConsumed() && connectButton.isSelected()) {
			start = graph.snap(e.getPoint());
			firstPort = port;
			if (firstPort != null) {
				start = graph.toScreen(firstPort.getLocation(null));
			}
			e.consume();
		} else {
			super.mousePressed(e);
		}
	}

	public void overlay(Graphics g) {
		super.overlay(graph, g, true);
		if (start != null) {
			if (connectButton.isSelected() && current != null) {
				g.drawLine((int) start.getX(), (int) start.getY(), (int) current.getX(),
						(int) current.getY());
			}
		}
	}

	// Find Port under Mouse and Repaint Connector
	public void mouseDragged(MouseEvent event) {
		if (!event.isConsumed() && !moveButton.isSelected()) {
			Graphics g = graph.getGraphics();
			Color bg = graph.getBackground();
			Color fg = Color.black;
			g.setColor(fg);
			g.setXORMode(bg);
			overlay(g);
			current = graph.snap(event.getPoint());
			if (connectButton.isSelected()) {
				port = getPortViewAt(event.getX(), event.getY(), !event.isShiftDown());
				if (port != null)
					current = graph.toScreen(port.getLocation(null));
			}
			g.setColor(bg);
			g.setXORMode(fg);
			overlay(g);
			event.consume();
		}
		super.mouseDragged(event);
	}

	/*
	 * Resource(Ellipse)のときにtrue、Literal(Veretex)のときfalse
	 */
	protected boolean isEllipseView(CellView v) {
		return v instanceof JGraphEllipseView;
	}

	public void mouseReleased(MouseEvent e) {
		if (e != null && !e.isConsumed() && port != null && firstPort != null && firstPort != port
				&& isEllipseView(firstPort.getParentView())) {
			Port source = (Port) firstPort.getCell();
			Port target = (Port) port.getCell();

			Set<Port> portSet = new HashSet<Port>();
			portSet.add(source);
			connectCells(portSet, target);
			e.consume(); // Consume Event
		}

		firstPort = port = null;
		start = current = null;
		super.mouseReleased(e);
	}

	public PortView getPortViewAt(int x, int y, boolean jump) {
		Point2D sp = graph.fromScreen(new Point2D.Double(x, y));
		PortView port = graph.getPortViewAt(sp.getX(), sp.getY());
		// Shift Jumps to "Default" Port (child index 0)
		if (port == null && jump) {
			Object cell = graph.getFirstCellForLocation(x, y);
			if (RDFGraph.isRDFSCell(cell) || RDFGraph.isRDFResourceCell(cell)
					|| RDFGraph.isRDFLiteralCell(cell)) {
				Object firstChild = graph.getModel().getChild(cell, 0);
				CellView firstChildView = graph.getGraphLayoutCache().getMapping(firstChild, false);
				if (firstChildView instanceof PortView)
					port = (PortView) firstChildView;
			}
		}
		return port;
	}

	protected Point insertPoint = new Point(10, 10);

	public void mouseMoved(MouseEvent event) {
		insertPoint = event.getPoint();
		if (!moveButton.isSelected() && !event.isConsumed()) {
			graph.setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));
			event.consume();
			if (connectButton.isSelected()) {
				PortView oldPort = port;
				PortView newPort = getPortViewAt(event.getX(), event.getY(), !event.isShiftDown());
				if (oldPort != newPort) {
					Graphics g = graph.getGraphics();
					Color bg = graph.getBackground();
					Color fg = graph.getMarqueeColor();
					g.setColor(fg);
					g.setXORMode(bg);
					overlay(g);
					port = newPort;
					g.setColor(bg);
					g.setXORMode(fg);
					overlay(g);
				}
			}
		}
		super.mouseMoved(event);
	}

	protected class ConnectAction extends AbstractAction {
		protected ConnectAction(String title, ImageIcon icon) {
			super(title, icon);
			setValues(title);
		}

		private void setValues(String title) {
			putValue(SHORT_DESCRIPTION, title);
			putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_G, KeyEvent.CTRL_MASK));
		}

		public void actionPerformed(ActionEvent e) {
			if (connectButton.isSelected()) {
				moveButton.setSelected(true);
			} else {
				connectButton.setSelected(true);
			}
		}
	}

	public GraphCell insertResourceCell(Point pt) {
		List<Object> list = new ArrayList<Object>();
		list.add(null); // リソースのタイプが空の場合
		for (Object cell : gmanager.getCurrentClassGraph().getAllCells()) {
			if (RDFGraph.isRDFSClassCell(cell)) {
				list.add(cell);
			}
		}
		InsertRDFResDialog dialog = getInsertRDFResDialog(Utilities
				.getSortedCellSet(list.toArray()));
		if (!dialog.isConfirm()) {
			return null;
		}

		String uri = dialog.getURI();
		Object resTypeCell = dialog.getResourceType();

		if (uri == null || gmanager.isDuplicatedWithDialog(uri, null, GraphType.RDF)) {
			return null;
		} else if (dialog.isAnonymous()) {
			return cellMaker.insertRDFResource(pt, uri, resTypeCell, URIType.ANONYMOUS);
		} else {
			return cellMaker.insertRDFResource(pt, uri, resTypeCell, URIType.URI);
		}
	}

	private Set getSelectedResourcePorts() {
		Object[] cells = graph.getSelectionCells();
		cells = graph.getDescendants(cells);
		Set<Object> selectedResourcePorts = new HashSet<Object>();
		for (Object cell : cells) {
			if (RDFGraph.isRDFResourceCell(cell)) {
				DefaultGraphCell gcell = (DefaultGraphCell) cell;
				selectedResourcePorts.add(gcell.getChildAt(0));
			}
		}
		return selectedResourcePorts;
	}

	public void insertConnectedResource(Point pt) {
		Set selectedResourcePorts = getSelectedResourcePorts();
		DefaultGraphCell targetCell = (DefaultGraphCell) insertResourceCell(pt);
		if (targetCell == null) {
			return;
		}
		Port targetPort = (Port) targetCell.getChildAt(0);
		connectCells(selectedResourcePorts, targetPort);
		if (targetCell.getParent() != null) {
			graph.setSelectionCell(targetCell.getParent());
		} else {
			graph.setSelectionCell(targetCell);
		}

		if (graph.getType() == GraphType.RDF) {
			HistoryManager.saveHistory(HistoryType.INSERT_CONNECTED_RESOURCE, targetCell);
		} else {
			HistoryManager.saveHistory(HistoryType.INSERT_CONNECTED_ONT_PROPERTY, targetCell);
		}
	}

	public GraphCell insertConnectedLiteral(Point pt) {
		Set selectedResourcePorts = getSelectedResourcePorts();
		DefaultGraphCell targetCell = (DefaultGraphCell) insertLiteralCell(pt);
		if (targetCell == null) {
			return null;
		}
		Port targetPort = (Port) targetCell.getChildAt(0);
		connectCells(selectedResourcePorts, targetPort);
		graph.setSelectionCell(targetCell);

		return targetCell;
	}

	private void connectCells(Set selectedResourcePorts, Port targetPort) {
		for (Iterator i = selectedResourcePorts.iterator(); i.hasNext();) {
			Port sourcePort = (Port) i.next();
			GraphCell rdfsPropCell = null;
			Object[] rdfsPropertyCells = gmanager.getCurrentPropertyGraph().getSelectionCells();

			RDFSInfo info = null;
			if (rdfsPropertyCells.length == 1 && RDFGraph.isRDFSPropertyCell(rdfsPropertyCells[0])) {
				rdfsPropCell = (GraphCell) rdfsPropertyCells[0];
				info = (RDFSInfo) GraphConstants.getValue(rdfsPropCell.getAttributes());
				if (MR3.OFF_META_MODEL_MANAGEMENT) {
					PropertyInfo pInfo = (PropertyInfo) info;
					info = new PropertyInfo(pInfo.getURIStr());
				}
			} else {
				info = new PropertyInfo(MR3Resource.Nil.getURI());
			}
			cellMaker.connect(sourcePort, targetPort, info, graph);
			HistoryManager.saveHistory(HistoryType.INSERT_PROPERTY, info, (GraphCell) graph
					.getModel().getParent(sourcePort),
					(GraphCell) graph.getModel().getParent(targetPort));
		}
	}

	public GraphCell insertLiteralCell(Point pt) {
		InsertRDFLiteralDialog insertLiteralDialog = getInsertRDFLiteralDialog();
		if (!insertLiteralDialog.isConfirm()) {
			return null;
		}
		Point2D point = pt;
		point = graph.snap(new Point2D.Double(point.getX(), point.getY()));
		return cellMaker.insertRDFLiteral(pt, insertLiteralDialog.getLiteral());
	}

	protected boolean isCellSelected(Object cell) {
		// cell != nullの判定がないと，一つだけセルを選択したときに，メニューが表示されない．
		return (cell != null || !graph.isSelectionEmpty());
	}

	private void addTransformMenu(JPopupMenu menu, Object cell) {
		if (isCellSelected(cell)) {
			menu.addSeparator();
			menu.add(new TransformElementAction(graph, gmanager, GraphType.RDF, GraphType.CLASS));
			menu.add(new TransformElementAction(graph, gmanager, GraphType.RDF, GraphType.PROPERTY));
		}
	}

	protected void addEditMenu(JPopupMenu menu, Object cell) {
		menu.addSeparator();
		menu.add(graph.getCopyAction());
		menu.add(graph.getCutAction());
		menu.add(graph.getPasteAction());

		if (isCellSelected(cell)) {
			menu.addSeparator();
			menu.add(removeAction);
		}
		menu.addSeparator();
	}

	class InsertResourceAction extends AbstractAction {

		InsertResourceAction() {
			super(INSERT_RESOURCE_TITLE, RDF_RESOURCE_ELLIPSE_ICON);
		}

		public void actionPerformed(ActionEvent ev) {
			insertConnectedResource(insertPoint);
		}
	}

	class InsertLiteralAction extends AbstractAction {

		InsertLiteralAction() {
			super(INSERT_LITERAL_TITLE, LITERAL_RECTANGLE_ICON);
		}

		public void actionPerformed(ActionEvent ev) {
			GraphCell cell = insertConnectedLiteral(insertPoint);
			if (cell != null) {
				HistoryManager.saveHistory(HistoryType.INSERT_CONNECTED_LITERAL, cell);
			}
		}
	}

	/** create PopupMenu */
	public JPopupMenu createPopupMenu(final Point pt, final Object cell) {
		JPopupMenu menu = new JPopupMenu();

		menu.add(insertResourceAction);
		menu.add(insertLiteralAction);

		menu.addSeparator();
		addChangeResourceTypeMenu(menu);
		addChangePropertyMenu(menu);
		addConnectORMoveMenu(menu);

		if (graph.isOneCellSelected(cell) && RDFGraph.isRDFResourceCell(cell)) {
			menu.add(new AbstractAction("Self Connect") {
				public void actionPerformed(ActionEvent e) {
					Port port = (Port) ((DefaultGraphCell) cell).getChildAt(0);
					cellMaker.selfConnect(port, null, graph);
				}
			});
		}
		addTransformMenu(menu, cell);
		addEditMenu(menu, cell);
		menu.add(new ShowAttrDialog());

		return menu;
	}

	private Object[] getSelectedRDFResourceCells() {
		RDFGraph rdfGraph = gmanager.getCurrentRDFGraph();
		Set<Object> resourceCells = new HashSet<Object>();
		for (Object rdfCell : rdfGraph.getDescendants(rdfGraph.getSelectionCells())) {
			if (RDFGraph.isRDFResourceCell(rdfCell)) {
				resourceCells.add(rdfCell);
			}
		}
		return resourceCells.toArray();
	}

	private Object[] getSelectedRDFPropertyCells() {
		RDFGraph rdfGraph = gmanager.getCurrentRDFGraph();
		Set<Object> rdfPropCells = new HashSet<Object>();

		for (Object rdfCell : rdfGraph.getDescendants(rdfGraph.getSelectionCells())) {
			if (RDFGraph.isRDFPropertyCell(rdfCell)) {
				rdfPropCells.add(rdfCell);
			}
		}
		return rdfPropCells.toArray();
	}

	private void addChangeResourceTypeMenu(JPopupMenu menu) {
		Object[] classCells = gmanager.getCurrentClassGraph().getSelectionCells();
		Object[] resCells = getSelectedRDFResourceCells();
		if (resCells.length != 0 && classCells.length == 1) {
			menu.add(new AbstractAction(Translator.getString("Action.ChangeResourceType.Text")) {
				public void actionPerformed(ActionEvent ev) {
					GraphCell typeCell = (GraphCell) gmanager.getCurrentClassGraph()
							.getSelectionCell();
					Object[] resCells = getSelectedRDFResourceCells();
					for (int i = 0; i < resCells.length; i++) {
						RDFResourceInfo info = (RDFResourceInfo) GraphConstants
								.getValue(((GraphCell) resCells[i]).getAttributes());
						info.setTypeCell(typeCell, gmanager.getCurrentRDFGraph());
					}
					gmanager.repaintRDFGraph();
				}
			});
			menu.addSeparator();
		}
	}

	private void addChangePropertyMenu(JPopupMenu menu) {
		Object[] rdfsPropCells = gmanager.getCurrentPropertyGraph().getSelectionCells();
		Object[] rdfPropCells = getSelectedRDFPropertyCells();
		if (rdfPropCells.length != 0 && rdfsPropCells.length == 1) {
			menu.add(new AbstractAction(Translator.getString("Action.ChangeProperty.Text")) {
				public void actionPerformed(ActionEvent ev) {
					GraphCell rdfsPropCell = (GraphCell) gmanager.getCurrentPropertyGraph()
							.getSelectionCell();
					Object[] rdfPropCells = getSelectedRDFPropertyCells();
					for (int i = 0; i < rdfPropCells.length; i++) {
						GraphCell rdfPropCell = (GraphCell) rdfPropCells[i];
						RDFSInfo rdfsInfo = (RDFSInfo) GraphConstants.getValue(rdfsPropCell
								.getAttributes());
						GraphConstants.setValue(rdfPropCell.getAttributes(), rdfsInfo);
						graph.getGraphLayoutCache().editCell(rdfPropCell,
								rdfPropCell.getAttributes());
					}
				}
			});
			menu.addSeparator();
		}
	}

	protected void addConnectORMoveMenu(JPopupMenu menu) {
		if (connectButton.isSelected()) {
			menu.add(new ConnectAction(Translator.getString("Action.Move.Text"), Utilities
					.getImageIcon(Translator.getString("Action.Move.Icon"))));
		} else {
			menu.add(new ConnectAction(Translator.getString("Action.Connect.Text"), Utilities
					.getImageIcon(Translator.getString("Action.Connect.Icon"))));
		}
	}

	protected class ShowAttrDialog extends AbstractAction {

		public ShowAttrDialog() {
			super(Translator.getString("Component.Window.AttrDialog.Text"), Utilities
					.getImageIcon(Translator.getString("AttributeDialog.Icon")));
			setValues();
		}

		private void setValues() {
			putValue(SHORT_DESCRIPTION, Translator.getString("Component.Window.AttrDialog.Text"));
			putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_A, KeyEvent.ALT_MASK));
		}

		public void actionPerformed(ActionEvent e) {
			gmanager.setVisibleAttrDialog(true);
			graph.setSelectionCell(graph.getSelectionCell());
		}
	}
}