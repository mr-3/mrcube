package mr3.ui;
import java.awt.*;

import javax.swing.*;

import mr3.data.*;
import mr3.util.*;

import com.hp.hpl.mesa.rdf.jena.model.*;
import com.jgraph.event.*;
import com.jgraph.graph.*;

/**
 *
 * @auther takeshi morita 
 */
public class SelectResourceTypeDialog extends SelectClassDialog {

	private JLabel dspURI;
	private Resource uri;
	private GraphCell cell;
	private GraphCell prevCell;

	public SelectResourceTypeDialog() {
		super("Select Resource Type");
	}

	protected void initEachDialogAttr() {
		dspURI = new JLabel();
		dspURI.setBorder(BorderFactory.createTitledBorder("URI"));
		dspURI.setPreferredSize(new Dimension(450, 50));
	}

	protected void setEachDialogAttrLayout() {
		gridbag.setConstraints(dspURI, c);
		inlinePanel.add(dspURI);
	}

	public void setInitCell(Object typeCell) {
		if (typeCell == null) {
			if (prevCell != null) {
				ChangeCellAttributes.changeCellColor(graph, prevCell, Color.green);
			}
			prevCell = null;
			dspURI.setText("");
			return;
		} else {
			Object[] cells = graph.getAllCells();
			for (int i = 0; i < cells.length; i++) {
				GraphCell cell = (GraphCell) cells[i];
				//				if (graph.isRDFResourceCell(cell)) {
				if (graph.isRDFSClassCell(cell)) {
					if (cell == typeCell) {
						ChangeCellAttributes.changeCellColor(graph, cell, Color.yellow);
						prevCell = cell;
						graph.setSelectionCell(cell);
						break;
					}
				}
			}
		}
	}

	public void valueChanged(GraphSelectionEvent e) {
		cell = (GraphCell) graph.getSelectionCell();
		if (graph.getSelectionCount() == 1 && graph.getModel().getChildCount(cell) <= 1) {
			//			if (graph.isRDFResourceCell(cell)) {	
			if (graph.isRDFSClassCell(cell)) {
				ChangeCellAttributes.changeCellColor(graph, prevCell, Color.green);
				ChangeCellAttributes.changeCellColor(graph, cell, Color.yellow);
				RDFSInfo info = rdfsMap.getCellInfo(cell);
				dspURI.setText(info.getURIStr());
				dspURI.setToolTipText(info.getURIStr());
				uri = info.getURI();
				prevCell = cell;
			}
		}
	}

	public Object getValue() {
		if (prevCell != null) {
			if (isOk) {
				isOk = false;
				ChangeCellAttributes.changeCellColor(graph, prevCell, Color.green);
				return uri;
			} else {
				ChangeCellAttributes.changeCellColor(graph, prevCell, Color.green);
				return null;
			}
		} else {
			return null;
		}
	}
}
