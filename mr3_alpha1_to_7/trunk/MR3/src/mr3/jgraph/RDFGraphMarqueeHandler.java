package mr3.jgraph;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

import javax.swing.*;

import mr3.data.*;
import mr3.ui.*;
import mr3.util.*;

import com.jgraph.graph.*;

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
			connect(source, target, MR3Resource.Nil.getURI());
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
			//			if (graph.isRDFResourceCell(cell) || graph.isRDFLiteralCell(cell)) {
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

	protected void connectAction() {
		if (connectButton.isSelected()) {
			moveButton.setSelected(true);
		} else {
			connectButton.setSelected(true);
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
		InsertRDFResDialog ird = new InsertRDFResDialog("Input Resource", list.toArray(), gmanager.getPrefixNSInfoSet());

		if (!ird.isConfirm()) {
			return null;
		}		
		String uri = ird.getURI();
		Object resTypeCell = ird.getResourceType();
		URIType uriType = ird.getURIType();

		String tmpURI = getAddedBaseURI(uri, uriType);
		if (uri == null || gmanager.isDuplicatedWithDialog(tmpURI, null, GraphType.RDF)) {
			return null;
		} else if (uri.length() == 0) {
			return cellMaker.insertRDFResource(pt, uri, resTypeCell, URIType.ANONYMOUS);
		} else {
			return cellMaker.insertRDFResource(pt, uri, resTypeCell, uriType);
		}
	}

	protected String getAddedBaseURI(String uri, URIType uriType) {
		String tmpURI = "";
		if (uriType == URIType.ID) {
			tmpURI = gmanager.getBaseURI();
		}
		tmpURI += uri;
		return tmpURI;
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
		graph.setSelectionCell(targetCell.getParent());
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
	}

	private void connectCells(Set selectedResourcePorts, Port targetPort) {
		for (Iterator i = selectedResourcePorts.iterator(); i.hasNext();) {
			Port sourcePort = (Port) i.next();
			connect(sourcePort, targetPort, MR3Resource.Nil.getURI());
		}
	}

	public void insertLiteralCell(Point pt) {
		cellMaker.insertRDFLiteral(pt);
	}

	//	
	// PopupMenu
	//
	public JPopupMenu createPopupMenu(final Point pt, final Object cell) {
		JPopupMenu menu = new JPopupMenu();

		menu.add(new AbstractAction("Insert Resource") {
			public void actionPerformed(ActionEvent ev) {
				insertResourceCell(pt);
			}
		});

		menu.add(new AbstractAction("Insert Connected Resource") {
			public void actionPerformed(ActionEvent ev) {
				insertConnectedResource(pt);
			}
		});

		menu.add(new AbstractAction("Insert Literal") {
			public void actionPerformed(ActionEvent ev) {
				insertLiteralCell(pt);
			}
		});

		menu.add(new AbstractAction("Insert Connected Literal") {
			public void actionPerformed(ActionEvent ev) {
				insertConnectedLiteralCell(pt);
			}
		});

		menu.addSeparator();

		menu.add(new AbstractAction("Connect mode") {
			public void actionPerformed(ActionEvent e) {
				connectAction();
			}
		});

		if (graph.isOneCellSelected(cell) && graph.isRDFResourceCell(cell)) {
			menu.add(new AbstractAction("Self Connect") {
				public void actionPerformed(ActionEvent e) {
					Port port = (Port) ((DefaultGraphCell) cell).getChildAt(0);
					selfConnect(port, "");
				}
			});
		}

		menu.addSeparator();

		menu.add(new AbstractAction("Copy") {
			public void actionPerformed(ActionEvent e) {
				graph.copy(pt);
			}
		});

		menu.add(new AbstractAction("Cut") {
			public void actionPerformed(ActionEvent e) {
				graph.copy(pt);
				gmanager.removeAction(graph);
			}
		});

		menu.add(new AbstractAction("Paste") {
			public void actionPerformed(ActionEvent e) {
				graph.paste(pt);
			}
		});

		// Remove
		if (cell != null || !graph.isSelectionEmpty()) {
			menu.add(new AbstractAction("Remove") {
				public void actionPerformed(ActionEvent e) {
					gmanager.removeAction(graph);
				}
			});
		}

		menu.addSeparator();
		menu.add(new AbstractAction("Attribute Dialog") {
			public void actionPerformed(ActionEvent e) {
				gmanager.setVisibleAttrDialog(true);
				graph.setSelectionCell(graph.getSelectionCell());
			}
		});

		return menu;
	}

	public void selfConnect(Port port, String edgeName) {
		DefaultEdge edge = new DefaultEdge(edgeName);
		ConnectionSet cs = new ConnectionSet(edge, port, port);
		HashMap attributes = new HashMap();
		Map map = cellMaker.getEdgeMap(edgeName);
		GraphConstants.setRouting(map, GraphConstants.ROUTING_SIMPLE);
		attributes.put(edge, map);
		graph.getModel().insert(new Object[] { edge }, attributes, cs, null, null);
	}

	public void connectSubToSups(Port sourcePort, Object[] supCells) {
		for (int i = 0; i < supCells.length; i++) {
			if (graph.isPort(supCells[i])) {
				Port targetPort = (Port) supCells[i];
				connect(sourcePort, targetPort, "");
			}
		}
	}

	public void connect(Port source, Port target, String edgeName) {
		DefaultEdge edge = new DefaultEdge(edgeName);
		ConnectionSet cs = new ConnectionSet(edge, source, target);
		HashMap attributes = new HashMap();
		Map map = cellMaker.getEdgeMap(edgeName);
		attributes.put(edge, map);
		graph.getModel().insert(new Object[] { edge }, attributes, cs, null, null);
	}
}
