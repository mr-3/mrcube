package mr3.jgraph;
import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import mr3.data.*;
import mr3.ui.*;

import com.jgraph.graph.*;

/**
 *
 * @auther takeshi morita
 */
public class PropertyGraphMarqueeHandler extends RDFGraphMarqueeHandler {

	private PropertyPanel propertyPanel;
	private RDFSInfoMap rdfsMap = RDFSInfoMap.getInstance();

	public PropertyGraphMarqueeHandler(GraphManager manager, PropertyPanel panel) {
		super(manager, manager.getPropertyGraph());
		propertyPanel = panel;
	}

	// Ç±Ç±Ç≈connectÇ∑ÇÈÇ©Ç«Ç§Ç©Çêßå‰Ç∑ÇÈ
	public void mouseReleased(MouseEvent e) {
		if (e != null && !e.isConsumed() && port != null && firstPort != null && firstPort != port && isEllipseView(firstPort.getParentView())) {
			Port source = (Port) firstPort.getCell();
			DefaultPort target = (DefaultPort) port.getCell();
			connect(source, target, "");
			propertyPanel.displayRDFSInfo((DefaultGraphCell) graph.getModel().getParent(source));
			e.consume();
		} else {
			graph.repaint();
		}

		firstPort = port = null;
		start = current = null;
		super.mouseReleased(e);
	}

	public GraphCell insertResourceCell(Point pt) {
		InsertRDFSResDialog ird = new InsertRDFSResDialog("Input Resource");
		if (!ird.isConfirm()) {
			return null;
		}
		String uri = ird.getURI();
		URIType uriType = ird.getURIType();
		String tmpURI = getAddedBaseURI(uri, uriType);
		if (tmpURI == null || gmanager.isEmptyURI(tmpURI) || gmanager.isDuplicatedWithDialog(tmpURI, null, GraphType.PROPERTY)) {
			return null;
		} else {
			return cellMaker.insertProperty(pt, uri, uriType);
		}
	}

	public void insertSubProperty(Point pt, Object[] supCells) {
		InsertRDFSResDialog ird = new InsertRDFSResDialog("Input Resource");
		if (!ird.isConfirm()) {
			return;
		}
		String uri = ird.getURI();
		URIType uriType = ird.getURIType();
		String tmpURI = getAddedBaseURI(uri, uriType);
		if (tmpURI == null || gmanager.isEmptyURI(tmpURI) || gmanager.isDuplicatedWithDialog(tmpURI, null, GraphType.CLASS)) {
			return;
		} else {
			pt.y += 100;
			cellMaker.insertProperty(pt, uri, uriType);
			DefaultGraphCell cell = (DefaultGraphCell) graph.getSelectionCell();
			Port sourcePort = (Port) cell.getChildAt(0);
			connectSubToSups(sourcePort, supCells);
			graph.setSelectionCell(cell);
		}
	}

	//
	// PopupMenu
	//
	public JPopupMenu createPopupMenu(final Point pt, final Object cell) {
		JPopupMenu menu = new JPopupMenu();

		menu.add(new AbstractAction("Insert Property") {
			public void actionPerformed(ActionEvent ev) {
				insertResourceCell(pt);
			}
		});

		if (cell != null || !graph.isSelectionEmpty()) {
			// Insert Sub Property
			menu.add(new AbstractAction("Insert Sub Property") {
				public void actionPerformed(ActionEvent e) {
					if (!graph.isSelectionEmpty()) {
						Object[] supCells = graph.getSelectionCells();
						supCells = graph.getDescendants(supCells);
						insertSubProperty(pt, supCells);
					}
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

		if (cell != null || !graph.isSelectionEmpty()) {
			menu.add(new AbstractAction("Remove") {
				public void actionPerformed(ActionEvent e) {
					gmanager.removeAction(graph);
				}
			});
		}

		menu.addSeparator();

		menu.add(new AbstractAction("Connect mode") {
			public void actionPerformed(ActionEvent e) {
				connectAction();
			}
		});

		menu.addSeparator();
		menu.add(new AbstractAction("Attribute Dialog") {
			public void actionPerformed(ActionEvent e) {
				gmanager.setVisibleAttrDialog(true);
			}
		});

		return menu;
	}
}
