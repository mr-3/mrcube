package org.semanticweb.mmm.mr3.jgraph;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

import javax.swing.*;

import org.semanticweb.mmm.mr3.actions.*;
import org.semanticweb.mmm.mr3.data.*;
import org.semanticweb.mmm.mr3.ui.*;
import org.semanticweb.mmm.mr3.util.*;

import org.jgraph.graph.*;

//
// Custom MarqueeHandler
// MarqueeHandler that Connects Vertices and Displays PopupMenus
public class RDFGraphMarqueeHandler extends BasicMarqueeHandler {

	protected RDFGraph graph;
	protected RDFCellMaker cellMaker;
	protected GraphManager gmanager;
	public transient JToggleButton moveButton = new JToggleButton();
	public transient JToggleButton connectButton = new JToggleButton();

	protected Rectangle bounds;
	protected Point start, current; // Holds the Start and the Current Point
	protected PortView port, firstPort; // Holds the First and the Current Port

	private RDFSInfoMap rdfsInfoMap = RDFSInfoMap.getInstance();

	public RDFGraphMarqueeHandler(GraphManager manager, RDFGraph graph) {
		gmanager = manager;
		this.graph = graph;
		cellMaker = new RDFCellMaker(gmanager);
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
			Point loc = graph.fromScreen(e.getPoint());
			Object cell = graph.getFirstCellForLocation(loc.x, loc.y);
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
		super.overlay(g);
		paintPort(graph.getGraphics());
		if (start != null) {
			if (connectButton.isSelected() && current != null) {
				g.drawLine(start.x, start.y, current.x, current.y);
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
		return v instanceof EllipseView;
	}

	public void mouseReleased(MouseEvent e) {
		if (e != null && !e.isConsumed() && port != null && firstPort != null && firstPort != port && isEllipseView(firstPort.getParentView())) {
			Port source = (Port) firstPort.getCell();
			Port target = (Port) port.getCell();
			cellMaker.connect(source, target, MR3Resource.Nil.getURI(), graph);
			e.consume(); // Consume Event
		} else {
			graph.repaint();
		}
		firstPort = port = null;
		start = current = null;
		super.mouseReleased(e);
	}

	public PortView getPortViewAt(int x, int y, boolean jump) {
		Point sp = graph.fromScreen(new Point(x, y));
		PortView port = graph.getPortViewAt(sp.x, sp.y);
		// Shift Jumps to "Default" Port (child index 0)
		if (port == null && jump) {
			Object cell = graph.getFirstCellForLocation(x, y);
			if (graph.isRDFSCell(cell) || graph.isRDFResourceCell(cell) || graph.isRDFLiteralCell(cell)) {
				Object firstChild = graph.getModel().getChild(cell, 0);
				CellView firstChildView = graph.getGraphLayoutCache().getMapping(firstChild, false);
				if (firstChildView instanceof PortView)
					port = (PortView) firstChildView;
			}
		}
		return port;
	}

	public void mouseMoved(MouseEvent event) {
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

	protected void paintPort(Graphics g) {
		if (port != null) {
			boolean offset = (GraphConstants.getOffset(port.getAllAttributes()) != null);
			Rectangle r = (offset) ? port.getBounds() : port.getParentView().getBounds();
			r = graph.toScreen(new Rectangle(r));
			int s = 3;
			r.translate(-s, -s);
			r.setSize(r.width + 2 * s, r.height + 2 * s);
			graph.getUI().paintCell(g, port, r, true);
		}
	}

	protected class ConnectAction extends AbstractAction {

		protected ConnectAction() {
			super(Translator.getString("Action.Connect.Text"));
			setValues(Translator.getString("Action.Connect.Text"));
		}

		private void setValues(String title) {
			putValue(SHORT_DESCRIPTION, title);
//			putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_I, KeyEvent.CTRL_MASK));
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
		Object[] cells = gmanager.getClassGraph().getAllCells();
		List list = new ArrayList();
		list.add(null); // リソースのタイプが空の場合
		for (int i = 0; i < cells.length; i++) {
			if (graph.isRDFSClassCell(cells[i])) {
				list.add(cells[i]);
			}
		}
		
		InsertRDFResDialog ird = new InsertRDFResDialog(list.toArray(), gmanager);

		if (!ird.isConfirm()) {
			return null;
		}
		String uri = ird.getURI();
		Object resTypeCell = ird.getResourceType();

		if (uri == null || gmanager.isDuplicatedWithDialog(uri, null, GraphType.RDF)) {
			return null;
		} else if (ird.isAnonymous()) {
			return cellMaker.insertRDFResource(pt, uri, resTypeCell, URIType.ANONYMOUS);
		} else {
			return cellMaker.insertRDFResource(pt, uri, resTypeCell, URIType.URI);
		}
	}

	private Set getSelectedResourcePorts() {
		Object[] cells = graph.getSelectionCells();
		cells = graph.getDescendants(cells);
		Set selectedResourcePorts = new HashSet();
		for (int i = 0; i < cells.length; i++) {
			if (graph.isRDFResourceCell(cells[i])) {
				DefaultGraphCell cell = (DefaultGraphCell) cells[i];
				selectedResourcePorts.add(cell.getChildAt(0));
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
		gmanager.changeCellView();
	}

	public void insertConnectedLiteralCell(Point pt) {
		Set selectedResourcePorts = getSelectedResourcePorts();
		DefaultGraphCell targetCell = (DefaultGraphCell) cellMaker.insertRDFLiteral(pt);
		if (targetCell == null) {
			return;
		}
		Port targetPort = (Port) targetCell.getChildAt(0);
		connectCells(selectedResourcePorts, targetPort);
		graph.setSelectionCell(targetCell);
		gmanager.changeCellView();
	}

	private void connectCells(Set selectedResourcePorts, Port targetPort) {
		for (Iterator i = selectedResourcePorts.iterator(); i.hasNext();) {
			Port sourcePort = (Port) i.next();
			Edge edge = cellMaker.connect(sourcePort, targetPort, MR3Resource.Nil.getURI(), graph);
			Object rdfsCell = gmanager.getPropertyCell(MR3Resource.Nil, false);
			rdfsInfoMap.putEdgeInfo(edge, rdfsCell);
		}
	}

	public void insertLiteralCell(Point pt) {
		cellMaker.insertRDFLiteral(pt);
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
		menu.add(new CopyAction(graph));
		menu.add(new CutAction(graph));
		menu.add(new PasteAction(graph));

		if (isCellSelected(cell)) {
			menu.addSeparator();
			menu.add(new RemoveAction(graph, gmanager));
		}
		menu.addSeparator();
	}

	/** create PopupMenu */
	public JPopupMenu createPopupMenu(final Point pt, final Object cell) {
		JPopupMenu menu = new JPopupMenu();

		menu.add(new AbstractAction(Translator.getString("InsertResourceDialog.Title")) {
			public void actionPerformed(ActionEvent ev) {
				insertResourceCell(pt);
			}
		});

		if (isCellSelected(cell)) {
			menu.add(new AbstractAction(Translator.getString("InsertConnectedResource.Title")) {
				public void actionPerformed(ActionEvent ev) {
					insertConnectedResource(pt);
				}
			});
		}

		menu.add(new AbstractAction(Translator.getString("InsertLiteralDialog.Title")) {
			public void actionPerformed(ActionEvent ev) {
				insertLiteralCell(pt);
			}
		});

		if (isCellSelected(cell)) {
			menu.add(new AbstractAction(Translator.getString("InsertConnectedLiteral.Title")) {
				public void actionPerformed(ActionEvent ev) {
					insertConnectedLiteralCell(pt);
				}
			});
		}

		menu.addSeparator();
		menu.add(new ConnectAction());

		if (graph.isOneCellSelected(cell) && graph.isRDFResourceCell(cell)) {
			menu.add(new AbstractAction("Self Connect") {
				public void actionPerformed(ActionEvent e) {
					Port port = (Port) ((DefaultGraphCell) cell).getChildAt(0);
					cellMaker.selfConnect(port, "", graph);
				}
			});
		}
		addTransformMenu(menu, cell);
		addEditMenu(menu, cell);
		menu.add(new ShowAttrDialog());

		return menu;
	}

	protected class ShowAttrDialog extends AbstractAction {

		public ShowAttrDialog() {
			super(Translator.getString("Component.Window.AttrDialog.Text"), Utilities.getImageIcon(Translator.getString("AttributeDialog.Icon")));
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
