/*
 * Project Name: MR^3 (Meta-Model Management based on RDFs Revision Reflection)
 * Project Website: https://mr-3.github.io/
 *
 * Copyright (C) 2003-2025 Takeshi Morita. All rights reserved.
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

package jp.ac.aoyama.it.ke.mrcube.views.instance_editor;

import org.apache.jena.rdf.model.Resource;
import org.jgraph.event.GraphSelectionEvent;
import org.jgraph.graph.GraphCell;
import org.jgraph.graph.GraphConstants;
import jp.ac.aoyama.it.ke.mrcube.jgraph.GraphManager;
import jp.ac.aoyama.it.ke.mrcube.jgraph.OntClassCell;
import jp.ac.aoyama.it.ke.mrcube.jgraph.RDFGraph;
import jp.ac.aoyama.it.ke.mrcube.models.MR3Constants.URIType;
import jp.ac.aoyama.it.ke.mrcube.models.RDFSModel;
import jp.ac.aoyama.it.ke.mrcube.utils.GraphUtilities;
import jp.ac.aoyama.it.ke.mrcube.utils.Utilities;

import javax.swing.*;

/**
 * @author Takeshi Morita
 */
public class SelectInstanceTypePanel extends SelectClassPanel {

    private JLabel dspURI;
    private Resource uri;
    private URIType uriType;
    private GraphCell prevCell;

    SelectInstanceTypePanel(GraphManager gm) {
        super(gm);
    }

    protected JComponent getEachDialogComponent() {
        dspURI = new JLabel("");
        return Utilities.createTitledPanel(dspURI, "URI", LIST_WIDTH, LIST_HEIGHT);
    }

    private void changeTypeCellColor(Object typeCell) {
        Object[] cells = graph.getAllCells();
        for (Object cell1 : cells) {
            GraphCell cell = (GraphCell) cell1;
            if (RDFGraph.isRDFSClassCell(cell)) {
                if (cell == typeCell) {
                    GraphUtilities.changeCellStyle(graph, cell, OntClassCell.foregroundColor, GraphUtilities.graphBackgroundColor, null);
                    prevCell = cell;
                    graph.setSelectionCell(cell);
                    break;
                }
            }
        }
    }

    public void setInitCell(Object typeCell) {
        changeAllCellColor(OntClassCell.foregroundColor, OntClassCell.backgroundColor, OntClassCell.borderColor);
        if (typeCell == null) {
            prevCell = null;
            dspURI.setText("");
            return;
        }
        changeTypeCellColor(typeCell);
    }

    public void valueChanged(GraphSelectionEvent e) {
        GraphCell cell = (GraphCell) graph.getSelectionCell();
        if (graph.getSelectionCount() == 1 && graph.getModel().getChildCount(cell) <= 1) {
            if (RDFGraph.isRDFSClassCell(cell)) {
                GraphUtilities.changeCellStyle(graph, prevCell, OntClassCell.foregroundColor, OntClassCell.backgroundColor, OntClassCell.borderColor);
                RDFSModel info = (RDFSModel) GraphConstants.getValue(cell.getAttributes());
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
