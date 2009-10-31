package jp.ac.shizuoka.cs.panda.mmm.mr3.editor;
import jp.ac.shizuoka.cs.panda.mmm.mr3.data.*;
import jp.ac.shizuoka.cs.panda.mmm.mr3.jgraph.*;
import jp.ac.shizuoka.cs.panda.mmm.mr3.ui.*;
import jp.ac.shizuoka.cs.panda.mmm.mr3.util.*;

import com.jgraph.event.*;
import com.jgraph.graph.*;

public class ClassEditor extends Editor {

	private ClassPanel classPanel;

	public ClassEditor(NameSpaceTableDialog nsD, FindResourceDialog findResD, GraphManager gm) {
		super("Class Editor");
		graph = gm.getClassGraph();
		graph.setDisconnectable(false);
		initEditor(gm.getClassGraph(), gm, nsD, findResD);
		setFrameIcon(Utilities.getImageIcon("classEditorIcon.gif"));
	}

	protected void initField(NameSpaceTableDialog nsD, GraphManager manager) {
		super.initField(nsD, manager);
		classPanel = new ClassPanel(graph, manager);
		graph.setMarqueeHandler(new ClassGraphMarqueeHandler(manager, classPanel));
	}

	public void valueChanged(GraphSelectionEvent e) {
		if (!gmanager.isImporting()) {
			lastSelectionCells = ChangeCellAttributes.changeSelectionCellColor(graph, lastSelectionCells);
			if (gmanager.isSelectAbstractLevelMode()) {
				Object[] cells = graph.getSelectionCells();
				gmanager.setClassAbstractLevelSet(cells);
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
				classPanel.showRDFSInfo(cell);
				attrDialog.setContentPane(classPanel);
			}
		} else {
			attrDialog.setNullPanel();
		}
	}
}
