package jp.ac.shizuoka.cs.panda.mmm.mr3.jgraph;
import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import jp.ac.shizuoka.cs.panda.mmm.mr3.actions.*;
import jp.ac.shizuoka.cs.panda.mmm.mr3.data.*;
import jp.ac.shizuoka.cs.panda.mmm.mr3.ui.*;

import org.jgraph.graph.*;

public class ClassGraphMarqueeHandler extends RDFGraphMarqueeHandler {

	private RDFSInfoMap rdfsMap;
	private ClassPanel classPanel;

	public ClassGraphMarqueeHandler(GraphManager manager, ClassPanel panel) {
		super(manager, manager.getClassGraph());
		classPanel = panel;
	}

	// connectするかどうかをここで制御
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
		InsertRDFSResDialog ird = new InsertRDFSResDialog("Input Resource", gmanager);
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
		InsertRDFSResDialog ird = new InsertRDFSResDialog("Input Resource", gmanager);
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

	//
	// PopupMenu
	//
	public JPopupMenu createPopupMenu(final Point pt, final Object cell) {
		JPopupMenu menu = new JPopupMenu();

		menu.add(new AbstractAction("Insert Class") {
			public void actionPerformed(ActionEvent ev) {
				insertResourceCell(pt);
			}
		});

		// cell != nullにしないと，一つだけセルを選択したときに，メニューが表示されない．
		if (cell != null || !graph.isSelectionEmpty()) { // Insert Sub Class
			menu.add(new AbstractAction("Insert Sub Class") {
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
		menu.add(new ConnectAction("Connect Mode"));
		menu.addSeparator();
		menu.add(new CopyAction(graph, "Copy"));
		menu.add(new CutAction(graph, "Cut"));
		menu.add(new PasteAction(graph, "Paste"));

		if (cell != null || !graph.isSelectionEmpty()) {
			menu.add(new RemoveAction(graph, gmanager, "Remove"));
		}

		menu.addSeparator();		
		menu.add(new ShowAttrDialog(graph, gmanager, "Attribute Dialog"));

		return menu;
	}
}
