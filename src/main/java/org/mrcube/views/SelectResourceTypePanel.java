/*
 * Project Name: MR^3 (Meta-Model Management based on RDFs Revision Reflection)
 * Project Website: http://mrcube.org/
 * 
 * Copyright (C) 2003-2018 Yamaguchi Laboratory, Keio University. All rights reserved.
 * 
 * This file is part of MR^3.
 * 
 * MR^3 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * MR^3 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with MR^3.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */

package org.mrcube.views;

import org.apache.jena.rdf.model.Resource;
import org.jgraph.event.GraphSelectionEvent;
import org.jgraph.graph.GraphCell;
import org.jgraph.graph.GraphConstants;
import org.mrcube.jgraph.GraphManager;
import org.mrcube.jgraph.OntClassCell;
import org.mrcube.jgraph.RDFGraph;
import org.mrcube.models.MR3Constants.URIType;
import org.mrcube.models.RDFSInfo;
import org.mrcube.utils.GraphUtilities;
import org.mrcube.utils.Utilities;

import javax.swing.*;

/**
 * @author Takeshi Morita
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
