/*
 * Created on 2003/09/25
 *
 */
package jp.ac.shizuoka.cs.panda.mmm.mr3.ui;

import java.awt.*;

import javax.swing.*;

import jp.ac.shizuoka.cs.panda.mmm.mr3.data.*;
import jp.ac.shizuoka.cs.panda.mmm.mr3.jgraph.*;
import jp.ac.shizuoka.cs.panda.mmm.mr3.util.*;

import org.jgraph.event.*;
import org.jgraph.graph.*;

import com.hp.hpl.jena.rdf.model.*;

/**
 * @author takeshi morita
 */
public class SelectResourceTypePanel extends SelectClassPanel {

	private JLabel dspURI;
	private Resource uri;
	private URIType uriType;
	private GraphCell cell;
	private GraphCell prevCell;

	SelectResourceTypePanel(GraphManager gm) {
		super(gm);	
	}
	
	protected void initEachDialogAttr() {
		dspURI = new JLabel();
		Utilities.initComponent(dspURI, "URI", LIST_WIDTH, LIST_HEIGHT);
	}

	protected void setEachDialogAttrLayout() {
		add(dspURI, BorderLayout.SOUTH);
	}

	private void changeTypeCellColor(Object typeCell) {
		Object[] cells = graph.getAllCells();
		for (int i = 0; i < cells.length; i++) {
			GraphCell cell = (GraphCell) cells[i];
			if (graph.isRDFSClassCell(cell)) {
				if (cell == typeCell) {
					ChangeCellAttributes.changeCellColor(graph, cell, ChangeCellAttributes.selectedColor);
					prevCell = cell;
					graph.setSelectionCell(cell);
					break;
				}
			}
		}
	}

	public void setInitCell(Object typeCell) {
		changeAllCellColor(ChangeCellAttributes.classColor);
		if (typeCell == null) {
			prevCell = null;
			dspURI.setText("");
			return;
		} else {
			changeTypeCellColor(typeCell);
		}
	}

	public void valueChanged(GraphSelectionEvent e) {
		cell = (GraphCell) graph.getSelectionCell();
		if (graph.getSelectionCount() == 1 && graph.getModel().getChildCount(cell) <= 1) {
			if (graph.isRDFSClassCell(cell)) {
				ChangeCellAttributes.changeCellColor(graph, prevCell, ChangeCellAttributes.classColor);
				ChangeCellAttributes.changeCellColor(graph, cell, ChangeCellAttributes.selectedColor);
				RDFSInfo info = rdfsMap.getCellInfo(cell);
				dspURI.setText(info.getURIStr());
				dspURI.setToolTipText(info.getURIStr());
				uri = info.getURI();
				prevCell = cell;
			}
		}
	}

	public GraphCell getPrevCell() {
		return prevCell;
	}

	public Resource getURI() {
		return uri;
	}

	public URIType getURIType() {
		return uriType;
	}

}
