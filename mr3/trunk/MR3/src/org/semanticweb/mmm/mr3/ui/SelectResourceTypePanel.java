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

import javax.swing.*;

import org.jgraph.event.*;
import org.jgraph.graph.*;
import org.semanticweb.mmm.mr3.data.*;
import org.semanticweb.mmm.mr3.data.MR3Constants.*;
import org.semanticweb.mmm.mr3.jgraph.*;
import org.semanticweb.mmm.mr3.util.*;

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

    protected JComponent getEachDialogComponent() {
        dspURI = new JLabel("");
        return Utilities.createTitledPanel(dspURI, "URI", LIST_WIDTH, LIST_HEIGHT);
    }

    private void changeTypeCellColor(Object typeCell) {
        Object[] cells = graph.getAllCells();
        for (int i = 0; i < cells.length; i++) {
            GraphCell cell = (GraphCell) cells[i];
            if (RDFGraph.isRDFSClassCell(cell)) {
                if (cell == typeCell) {
                    GraphUtilities.changeDefaultCellStyle(graph, cell, GraphUtilities.selectedColor);
                    prevCell = cell;
                    graph.setSelectionCell(cell);
                    break;
                }
            }
        }
    }

    public void setInitCell(Object typeCell) {
        changeAllCellColor(OntClassCell.classColor);
        if (typeCell == null) {
            prevCell = null;
            dspURI.setText("");
            return;
        }
        changeTypeCellColor(typeCell);
    }

    public void valueChanged(GraphSelectionEvent e) {
        cell = (GraphCell) graph.getSelectionCell();
        if (graph.getSelectionCount() == 1 && graph.getModel().getChildCount(cell) <= 1) {
            if (RDFGraph.isRDFSClassCell(cell)) {
                GraphUtilities.changeDefaultCellStyle(graph, prevCell, OntClassCell.classColor);
                GraphUtilities.changeCellStyle(graph, cell, GraphUtilities.selectedColor,
                        GraphUtilities.selectedBorderColor);
                RDFSInfo info = (RDFSInfo) GraphConstants.getValue(cell.getAttributes());
                if (info != null) {
                    dspURI.setText(info.getURIStr());
                    dspURI.setToolTipText(info.getURIStr());
                    uri = info.getURI();
                    prevCell = cell;
                }
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
