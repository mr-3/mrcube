package mr3.editor;
import java.io.*;

import javax.swing.text.*;

import mr3.data.*;
import mr3.jgraph.*;
import mr3.ui.*;
import mr3.util.*;

import com.hp.hpl.mesa.rdf.jena.common.*;
import com.hp.hpl.mesa.rdf.jena.model.*;
import com.hp.hpl.mesa.rdf.jena.vocabulary.*;
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

	public RDFEditor(AttributeDialog pw, FindResourceDialog findResD, GraphManager manager) {
		graph = manager.getRDFGraph();
		graph.setMarqueeHandler(new RDFGraphMarqueeHandler(manager, graph));
		initEditor(manager.getRDFGraph(), manager, pw, findResD);
	}

	protected void initField(AttributeDialog pw, GraphManager manager) {
		super.initField(pw, manager);
		resPanel = new RDFResourcePanel(gmanager, pw);
		propPanel = new RDFPropertyPanel(gmanager, pw);
		litPanel = new RDFLiteralPanel(gmanager, pw);
	}

	public void convertNTripleSRC(JTextComponent area, boolean isSelected) {
		Model model = null;
		if (isSelected) {
			model = graphToRDF.getSelectedRDFModel();
		} else {
			model = graphToRDF.getRDFModel();
		}
		Writer output = new StringWriter();
		RDFWriter writer = new NTripleWriter();
		writeModel(model, output, writer);
		area.setText(output.toString());
	}

	public void convertRDFSRC(JTextComponent area, boolean isSelected) {
		try {
			Model model = null;
			if (isSelected) {
				model = graphToRDF.getSelectedRDFModel();
			} else {
				model = graphToRDF.getRDFModel();
			}
			Writer output = new StringWriter();
			RDFWriter writer = new RDFWriterFImpl().getWriter("RDF/XML-ABBREV");
			writeModel(model, output, writer);
			area.setText(output.toString());
		} catch (RDFException e) {
			e.printStackTrace();
		}
	}

	private Object getDomainType(Edge edge) {
		Object source = graph.getSourceVertex(edge);
		RDFResourceInfo sourceInfo = resInfoMap.getCellInfo(source);
		if (sourceInfo == null || sourceInfo.getTypeCell() == null) {
			return gmanager.getClassCell(RDFS.Resource, URIType.URI, true);
		} else {
			return sourceInfo.getTypeCell();
		}
	}

	private Object getRangeType(Edge edge) {
		Object target = graph.getTargetVertex(edge);
		RDFResourceInfo resInfo = resInfoMap.getCellInfo(target);
		Literal litInfo = litInfoMap.getCellInfo(target);
		if (litInfo != null) { // infoがLiteralならば
			return gmanager.getClassCell(RDFS.Literal, URIType.URI, true);
		} else if (litInfo == null || resInfo.getTypeCell() == null) { // TypeCellがなければ作る．
			return gmanager.getClassCell(RDFS.Resource, URIType.URI, true);
		} else {
			return resInfo.getTypeCell();
		}
	}

	private void selectResource(GraphCell cell) {
		// 対応するRDFSクラスを選択
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

		if (attrDialog.isVisible()) {
			propPanel.dspPropertyInfo(cell);
			Edge edge = (Edge) cell;
			Object domainType = getDomainType(edge);
			Object rangeType = getRangeType(edge);
			propPanel.setPropertyList(gmanager.getPropertyList(), gmanager.getValidPropertyList(domainType, rangeType));
			attrDialog.setContentPane(propPanel);
		}
	}

	private void selectLiteral(GraphCell cell) {
		if (attrDialog.isVisible()) {
			litPanel.dspLiteralInfo(cell);
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
