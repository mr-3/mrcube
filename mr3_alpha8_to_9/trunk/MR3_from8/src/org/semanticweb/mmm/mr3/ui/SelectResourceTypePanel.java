/*
 * @(#) SelectResourceTypePanel.java
 * 
 * Copyright (C) 2003 The MMM Project
 * 
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation; either version 2.1 of the License, or (at your
 * option) any later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *  
 */

package org.semanticweb.mmm.mr3.ui;

import java.awt.*;

import javax.swing.*;

import org.semanticweb.mmm.mr3.data.*;
import org.semanticweb.mmm.mr3.jgraph.*;
import org.semanticweb.mmm.mr3.util.*;

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
					ChangeCellAttrUtil.changeDefaultCellStye(graph, cell, ChangeCellAttrUtil.selectedColor);
					prevCell = cell;
					graph.setSelectionCell(cell);
					break;
				}
			}
		}
	}

	public void setInitCell(Object typeCell) {
		changeAllCellColor(ChangeCellAttrUtil.classColor);
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
				ChangeCellAttrUtil.changeDefaultCellStye(graph, prevCell, ChangeCellAttrUtil.classColor);
				ChangeCellAttrUtil.changeCellStyle(graph, cell, ChangeCellAttrUtil.selectedColor, ChangeCellAttrUtil.selectedBorderColor, 2);
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
