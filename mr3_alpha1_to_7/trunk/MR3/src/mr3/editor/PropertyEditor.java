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

/**
 *
 * @auther takeshi morita
 */
public class PropertyEditor extends Editor {

	private PropertyPanel propPanel;

	public PropertyEditor(NameSpaceTableDialog nsD, FindResourceDialog findResD, GraphManager manager) {
		graph = manager.getPropertyGraph();
		graph.setDisconnectable(false);
		initEditor(manager.getPropertyGraph(), manager, nsD, findResD);
	}

	protected void initField(NameSpaceTableDialog nsD, GraphManager manager) {
		super.initField(nsD, manager);
		propPanel = new PropertyPanel(gmanager);
		graph.setMarqueeHandler(new PropertyGraphMarqueeHandler(manager, propPanel));
	}

	public void convertSRC(JTextComponent area, boolean isSelected) {
		Writer output = new StringWriter();
		try {
			Model model = null;
			if (isSelected) {
				model = graphToRDF.getSelectedPropertyModel();
			} else {
				model = graphToRDF.getPropertyModel();
			}
			RDFWriter writer = new RDFWriterFImpl().getWriter("RDF/XML-ABBREV");
			writeModel(model, output, writer);
		} catch (RDFException e) {
			e.printStackTrace();
		}

		area.setText(output.toString());
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
