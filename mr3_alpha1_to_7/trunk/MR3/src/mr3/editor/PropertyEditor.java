package mr3.editor;
import java.net.*;

import javax.swing.*;

import mr3.data.*;
import mr3.jgraph.*;
import mr3.ui.*;
import mr3.util.*;

import com.jgraph.event.*;
import com.jgraph.graph.*;

/**
 *
 * @auther takeshi morita
 */
public class PropertyEditor extends Editor {

	private PropertyPanel propPanel;

	public PropertyEditor(NameSpaceTableDialog nsD, FindResourceDialog findResD, GraphManager gm) {
		super("Property Editor");
		graph = gm.getPropertyGraph();
		graph.setDisconnectable(false);
		initEditor(gm.getPropertyGraph(), gm, nsD, findResD);
		URL propertyEditorUrl = this.getClass().getClassLoader().getResource("mr3/resources/propertyEditorIcon.gif");
		setFrameIcon(new ImageIcon(propertyEditorUrl));
	}

	protected void initField(NameSpaceTableDialog nsD, GraphManager manager) {
		super.initField(nsD, manager);
		propPanel = new PropertyPanel(gmanager);
		graph.setMarqueeHandler(new PropertyGraphMarqueeHandler(manager, propPanel));
	}

	public void valueChanged(GraphSelectionEvent e) {
		if (!gmanager.isImporting()) {
			lastSelectionCells = ChangeCellAttributes.changeSelectionCellColor(graph, lastSelectionCells);
			if (gmanager.isSelectAbstractLevelMode()) {
				Object[] cells = graph.getSelectionCells();
				gmanager.setPropertyAbstractLevelSet(cells);
				gmanager.changeCellView();
			} else {
				setToolStatus();
				if (attrDialog.isVisible()) {
					changeAttrPanel();
					attrDialog.validate();
				}
			}
		}
	}

	private void changeAttrPanel() {
		DefaultGraphCell cell = (DefaultGraphCell) graph.getSelectionCell();
		if (graph.isOneCellSelected(cell)) {
			RDFSInfo info = rdfsInfoMap.getCellInfo(cell);
			if (info != null) {
				propPanel.showRDFSInfo(cell);
				attrDialog.setContentPane(propPanel);
			}
		} else {
			attrDialog.setNullPanel();
		}
	}
}
