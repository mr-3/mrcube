package org.semanticweb.mmm.mr3.jgraph;
import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import org.semanticweb.mmm.mr3.actions.*;
import org.semanticweb.mmm.mr3.data.*;
import org.semanticweb.mmm.mr3.ui.*;
import org.semanticweb.mmm.mr3.util.*;

import org.jgraph.graph.*;

public class ClassGraphMarqueeHandler extends RDFGraphMarqueeHandler {

	private RDFSInfoMap rdfsMap;
	private ClassPanel classPanel;

	public ClassGraphMarqueeHandler(GraphManager manager, ClassPanel panel) {
		super(manager, manager.getClassGraph());
		classPanel = panel;
	}

	// connectÇ∑ÇÈÇ©Ç«Ç§Ç©ÇÇ±Ç±Ç≈êßå‰
	public void mouseReleased(MouseEvent e) {
		if (e != null && !e.isConsumed() && port != null && firstPort != null && firstPort != port) {
			Port source = (Port) firstPort.getCell();
			DefaultPort target = (DefaultPort) port.getCell();
			cellMaker.connect(source, target, "", graph);
			classPanel.showRDFSInfo((DefaultGraphCell) graph.getModel().getParent(source));
			e.consume();
		} else {
			graph.repaint();
		}

		firstPort = port = null;
		start = current = null;

		super.mouseReleased(e);
	}

	public GraphCell insertResourceCell(Point pt) {
		InsertRDFSResDialog ird = new InsertRDFSResDialog(Translator.getString("InsertClassDialog.Title"), gmanager);
		if (!ird.isConfirm()) {
			return null;
		}
		String uri = ird.getURI();
		if (uri == null || gmanager.isEmptyURI(uri) || gmanager.isDuplicatedWithDialog(uri, null, GraphType.CLASS)) {
			return null;
		} else {
			return cellMaker.insertClass(pt, uri);
		}
	}

	public void insertSubClass(Point pt, Object[] supCells) {
		InsertRDFSResDialog ird = new InsertRDFSResDialog(Translator.getString("InsertSubClassDialog.Title"), gmanager);
		if (!ird.isConfirm()) {
			return;
		}
		String uri = ird.getURI();
		if (uri == null || gmanager.isEmptyURI(uri) || gmanager.isDuplicatedWithDialog(uri, null, GraphType.CLASS)) {
			return;
		} else {
			cellMaker.insertClass(pt, uri);
			DefaultGraphCell cell = (DefaultGraphCell) graph.getSelectionCell();
			Port subPort = (Port) cell.getChildAt(0);
			cellMaker.connectSubToSups(subPort, supCells, graph);
			graph.setSelectionCell(cell);
		}
	}

	private void addTransformMenu(JPopupMenu menu, Object cell) {
		if (isCellSelected(cell)) {
			menu.addSeparator();
			menu.add(new TransformElementAction(graph, gmanager, GraphType.CLASS, GraphType.RDF));
			menu.add(new TransformElementAction(graph, gmanager, GraphType.CLASS, GraphType.PROPERTY));
		}
	}

	/**  create PopupMenu */
	public JPopupMenu createPopupMenu(final Point pt, final Object cell) {
		JPopupMenu menu = new JPopupMenu();

		menu.add(new AbstractAction(Translator.getString("InsertClassDialog.Title")) {
			public void actionPerformed(ActionEvent ev) {
				insertResourceCell(pt);
			}
		});

		if (isCellSelected(cell)) { // Insert Sub Class
			menu.add(new AbstractAction(Translator.getString("InsertSubClassDialog.Title")) {
				public void actionPerformed(ActionEvent e) {
					if (!graph.isSelectionEmpty()) {
						Object[] supCells = graph.getSelectionCells();
						supCells = graph.getDescendants(supCells);
						insertSubClass(pt, supCells);
					}
				}
			});
		}
		menu.addSeparator();
		menu.add(new ConnectAction());
		addTransformMenu(menu, cell);
		addEditMenu(menu, cell);
		menu.add(new ShowAttrDialog());

		return menu;
	}
}
