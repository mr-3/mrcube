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

	public void insertResourceCell(Point pt) {
		String uri = JOptionPane.showInputDialog("Please input URI");
		if (uri == null || gmanager.isEmptyURI(uri) || gmanager.isDuplicatedWithDialog(uri, null, GraphType.PROPERTY)) {
			return;
		} else {
			cellMaker.insertProperty(pt, uri);
			DefaultGraphCell cell = (DefaultGraphCell) graph.getSelectionCell();
			if (graph.isOneCellSelected(cell)) {
				propertyPanel.displayRDFSInfo(cell);
			}
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

		menu.addSeparator();

		// connect mode
		menu.add(new AbstractAction("Connect mode") {
			public void actionPerformed(ActionEvent e) {
				connectAction();
			}
		});

		// Remove
		if (!graph.isSelectionEmpty()) {
			menu.add(new AbstractAction("Remove") {
				public void actionPerformed(ActionEvent e) {
					if (!graph.isSelectionEmpty()) {
						Object[] cells = graph.getSelectionCells();
						cells = graph.getDescendants(cells);
						gmanager.removeCells(cells, graph);
					}
				}
			});
		}

		return menu;
	}
}
