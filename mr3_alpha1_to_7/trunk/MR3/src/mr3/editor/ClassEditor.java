package mr3.editor;
import java.io.*;

import javax.swing.text.*;

import mr3.data.*;
import mr3.jgraph.*;
import mr3.ui.*;
import mr3.util.*;

import com.hp.hpl.mesa.rdf.jena.common.*;
import com.hp.hpl.mesa.rdf.jena.model.*;
import com.jgraph.event.*;
import com.jgraph.graph.*;

public class ClassEditor extends Editor {

	private ClassPanel classPanel;

	public ClassEditor(AttributeDialog pw, FindResourceDialog findResD, GraphManager manager) {
		graph = manager.getClassGraph();
		graph.setDisconnectable(false);
		initEditor(manager.getClassGraph(), manager, pw, findResD);
	}

	protected void initField(AttributeDialog pw, GraphManager manager) {
		super.initField(pw, manager);
		classPanel = new ClassPanel(graph, manager);
		graph.setMarqueeHandler(new ClassGraphMarqueeHandler(manager, classPanel));
	}

	public void convertSRC(JTextComponent area, boolean isSelected) {
		Writer output = new StringWriter();
		try {
			Model model = null;
			if (isSelected) {
				model = graphToRDF.getSelectedClassModel();
			} else {
				model = graphToRDF.getClassModel();
			}
			RDFWriter writer = new RDFWriterFImpl().getWriter("RDF/XML-ABBREV");
			writeModel(model, output, writer);
		} catch (RDFException e) {
			e.printStackTrace();
		}
		area.setText(output.toString());
	}

	public void valueChanged(GraphSelectionEvent e) {
		lastSelectionCells = ChangeCellAttributes.changeSelectionCellColor(graph, lastSelectionCells);
		if (gmanager.isSelectAbstractLevelMode()) {
			Object[] cells = graph.getSelectionCells();
			gmanager.setClassAbstractLevelSet(cells);
			gmanager.changeCellView();
		} else {
			setToolStatus();
			changeAttrPanel();
			attrDialog.validate();
		}
	}

	private void changeAttrPanel() {
		DefaultGraphCell cell = (DefaultGraphCell) graph.getSelectionCell();
		if (graph.isOneCellSelected(cell)) {
			RDFSInfo info = rdfsInfoMap.getCellInfo(cell);
			if (info != null) {
				classPanel.displayRDFSInfo(cell);
				attrDialog.setContentPane(classPanel);
			}
		} else {
			attrDialog.setNullPanel();
		}
	}
}
