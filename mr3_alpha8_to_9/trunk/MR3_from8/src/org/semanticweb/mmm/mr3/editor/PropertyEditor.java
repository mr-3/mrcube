/*
 * @(#) PropertyEditor.java
 *
 *
 * Copyright (C) 2003 The MMM Project
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 */

package org.semanticweb.mmm.mr3.editor;

import org.semanticweb.mmm.mr3.data.*;
import org.semanticweb.mmm.mr3.jgraph.*;
import org.semanticweb.mmm.mr3.ui.*;
import org.semanticweb.mmm.mr3.util.*;

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
		graph.setEditable(true);
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
			lastSelectionCells = ChangeCellAttrUtil.changeSelectionCellStyle(graph, lastSelectionCells);
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
