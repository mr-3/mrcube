package jp.ac.shizuoka.cs.panda.mmm.mr3.editor;
import jp.ac.shizuoka.cs.panda.mmm.mr3.data.*;
import jp.ac.shizuoka.cs.panda.mmm.mr3.jgraph.*;
import jp.ac.shizuoka.cs.panda.mmm.mr3.ui.*;
import jp.ac.shizuoka.cs.panda.mmm.mr3.util.*;

import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.vocabulary.*;
import com.jgraph.event.*;
import com.jgraph.graph.*;

/**
 * @author takeshi morita
 *
 */
public class RDFEditor extends Editor {

	private RDFResourcePanel resPanel;
	private RDFPropertyPanel propPanel;
	private RDFLiteralPanel litPanel;

	public RDFEditor(NameSpaceTableDialog nsD, FindResourceDialog findResD, GraphManager gm) {
		super("RDF Editor");
		graph = gm.getRDFGraph();
		graph.setMarqueeHandler(new RDFGraphMarqueeHandler(gm, graph));
		initEditor(gm.getRDFGraph(), gm, nsD, findResD);
		setFrameIcon(Utilities.getImageIcon("rdfEditorIcon.gif"));
	}

	protected void initField(NameSpaceTableDialog nsD, GraphManager manager) {
		super.initField(nsD, manager);
		resPanel = new RDFResourcePanel(gmanager);
		propPanel = new RDFPropertyPanel(gmanager);
		litPanel = new RDFLiteralPanel(gmanager);
	}

	private Object getDomainType(Edge edge) {
		Object source = graph.getSourceVertex(edge);
		RDFResourceInfo sourceInfo = resInfoMap.getCellInfo(source);
		if (sourceInfo == null || sourceInfo.getTypeCell() == null) {
			return gmanager.getClassCell(RDFS.Resource, true);
		} else {
			return sourceInfo.getTypeCell();
		}
	}

	private Object getRangeType(Edge edge) {
		Object target = graph.getTargetVertex(edge);
		RDFResourceInfo resInfo = resInfoMap.getCellInfo(target);
		Literal litInfo = litInfoMap.getCellInfo(target);
		if (litInfo != null) { // infoがLiteralならば
			return gmanager.getClassCell(RDFS.Literal, true);
		} else if (litInfo == null || resInfo.getTypeCell() == null) { // TypeCellがなければ作る．
			return gmanager.getClassCell(RDFS.Resource, true);
		} else {
			return resInfo.getTypeCell();
		}
	}

	//	対応するRDFSクラスを選択
	private void selectResource(GraphCell cell) {
		RDFResourceInfo info = resInfoMap.getCellInfo(cell);
		if (info != null) {
			gmanager.jumpClassArea(info.getTypeCell());
			if (attrDialog.isVisible()) {
				resPanel.showRDFResInfo(cell);
				attrDialog.setContentPane(resPanel);
			}
		}
	}

	private void selectProperty(GraphCell cell) {
		// 対応するRDFSプロパティを選択
		GraphCell propCell = (GraphCell) rdfsInfoMap.getEdgeInfo(cell);
		gmanager.jumpPropertyArea(propCell);

		Edge edge = (Edge) cell;
		Object domainType = getDomainType(edge);
		Object rangeType = getRangeType(edge);
		PrefixNSUtil.setPrefixNSInfoSet(gmanager.getPrefixNSInfoSet());
		propPanel.setPropertyList(gmanager.getPropertyList(), gmanager.getValidPropertyList(domainType, rangeType));
		propPanel.showPropertyInfo(edge);
		attrDialog.setContentPane(propPanel);
	}

	private void selectLiteral(GraphCell cell) {
		if (attrDialog.isVisible()) {
			litPanel.showLiteralInfo(cell);
			attrDialog.setContentPane(litPanel);
		}
	}

	// From GraphSelectionListener Interface
	public void valueChanged(GraphSelectionEvent e) {
		if (!gmanager.isImporting()) {
			setToolStatus();
			lastSelectionCells = ChangeCellAttributes.changeSelectionCellColor(graph, lastSelectionCells);
			changeAttrPanel();
			attrDialog.validate(); // validateメソッドを呼ばないと再描画がうまくいかない
		}
	}

	private void changeAttrPanel() {
		Object[] cells = graph.getDescendants(graph.getSelectionCells());
		GraphCell rdfCell = graph.isOneRDFCellSelected(cells);

		if (rdfCell != null) {
			if (graph.isRDFResourceCell(rdfCell)) {
				selectResource(rdfCell);
			} else if (graph.isRDFPropertyCell(rdfCell)) {
				selectProperty(rdfCell);
			} else if (graph.isRDFLiteralCell(rdfCell)) {
				selectLiteral(rdfCell);
			}
		} else {
			attrDialog.setNullPanel();
		}
	}
}
