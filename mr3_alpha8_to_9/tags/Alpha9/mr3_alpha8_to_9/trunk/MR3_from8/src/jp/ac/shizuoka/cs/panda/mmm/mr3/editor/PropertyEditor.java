package jp.ac.shizuoka.cs.panda.mmm.mr3.editor;
import jp.ac.shizuoka.cs.panda.mmm.mr3.data.*;
import jp.ac.shizuoka.cs.panda.mmm.mr3.jgraph.*;
import jp.ac.shizuoka.cs.panda.mmm.mr3.ui.*;
import jp.ac.shizuoka.cs.panda.mmm.mr3.util.*;

import org.jgraph.event.*;
import org.jgraph.graph.*;

/**
 *
 * @author takeshi morita
 */
public class PropertyEditor extends Editor {

	private PropertyPanel propPanel;

	public PropertyEditor(NameSpaceTableDialog nsD, FindResourceDialog findResD, GraphManager gm) {
		super(Translator.getString("PropertyEditor.Title"));
		graph = gm.getPropertyGraph();
		graph.setDisconnectable(false);
		initEditor(gm.getPropertyGraph(), gm, nsD, findResD);
		setFrameIcon(Utilities.getImageIcon(Translator.getString("PropertyEditor.Icon")));
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
