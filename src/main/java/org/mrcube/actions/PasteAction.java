/*
 * Project Name: MR^3 (Meta-Model Management based on RDFs Revision Reflection)
 * Project Website: http://mrcube.org/
 *
 * Copyright (C) 2003-2020 Takeshi Morita. All rights reserved.
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

package org.mrcube.actions;

import org.jgraph.graph.GraphCell;
import org.jgraph.graph.GraphConstants;
import org.mrcube.jgraph.GraphManager;
import org.mrcube.jgraph.RDFGraph;
import org.mrcube.models.*;
import org.mrcube.models.MR3Constants.GraphType;
import org.mrcube.models.MR3Constants.HistoryType;
import org.mrcube.utils.GraphUtilities;
import org.mrcube.utils.Translator;
import org.mrcube.utils.Utilities;
import org.mrcube.views.HistoryManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Takeshi Morita
 */

public class PasteAction extends AbstractAction {

    private final RDFGraph graph;
    private final GraphManager gmanager;
    private static final String TITLE = Translator.getString("Action.Paste.Text");
    private static final ImageIcon ICON = Utilities.getImageIcon(Translator.getString("Action.Paste.Icon"));

    public PasteAction(RDFGraph g, GraphManager gm) {
        super(TITLE, ICON);
        graph = g;
        gmanager = gm;
        putValue(SHORT_DESCRIPTION, TITLE);
        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_V,
                Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
    }

    public void actionPerformed(ActionEvent e) {
        TransferHandler.getPasteAction().actionPerformed(new ActionEvent(graph, e.getID(), e.getActionCommand()));
        Object[] copyCells = graph.getCopyCells();
        if (copyCells == null) {
            return;
        }
        Set<GraphCell> pasteGraphCellSet = new HashSet<>();
        Set<GraphCell> removeGraphCellSet = new HashSet<>();
        for (Object copyCell : copyCells) {
            GraphCell cell = (GraphCell) copyCell;
            if (graph.getType() == GraphType.Class && RDFGraph.isRDFSClassCell(cell)) {
                pasteGraphCellSet.add(cell);
                cloneRDFSClassCell(cell);
                gmanager.selectClassCell(cell);
            } else if (graph.getType() == GraphType.Property && RDFGraph.isRDFSPropertyCell(cell)) {
                pasteGraphCellSet.add(cell);
                cloneRDFSPropertyCell(cell);
                gmanager.selectPropertyCell(cell);
            } else if (graph.getType() == GraphType.Instance) {
                if (RDFGraph.isRDFResourceCell(cell)) {
                    pasteGraphCellSet.add(cell);
                    cloneRDFResourceCell(cell);
                } else if (RDFGraph.isRDFPropertyCell(cell)) {
                    pasteGraphCellSet.add(cell);
                } else if (RDFGraph.isRDFLiteralCell(cell)) {
                    pasteGraphCellSet.add(cell);
                    cloneRDFLiteralCell(cell);
                    gmanager.selectRDFCell(cell);
                } else {
                    if (!(RDFGraph.isPort(cell) || RDFGraph.isEdge(cell))) {
                        removeGraphCellSet.add(cell);
                    } else {
                        pasteGraphCellSet.add(cell);
                    }
                }
            } else {
                if (!(RDFGraph.isPort(cell) || RDFGraph.isEdge(cell))) {
                    removeGraphCellSet.add(cell);
                } else {
                    pasteGraphCellSet.add(cell);
                }
            }
        }
        graph.getGraphLayoutCache().remove(removeGraphCellSet.toArray());
        gmanager.resetTypeCells();
        if (graph.getType() == GraphType.Instance) {
            HistoryManager.saveHistory(HistoryType.PASTE_RDF_GRAPH);
        } else if (graph.getType() == GraphType.Class) {
            HistoryManager.saveHistory(HistoryType.PASTE_CLASS_GRAPH);
        } else if (graph.getType() == GraphType.Property) {
            HistoryManager.saveHistory(HistoryType.PASTE_PROPERTY_GRAPH);
        }
    }

    /**
     * @param cell
     */
    private void cloneRDFLiteralCell(GraphCell cell) {
        MR3Literal orgLiteral = (MR3Literal) GraphConstants.getValue(cell.getAttributes());
        MR3Literal newLiteral = new MR3Literal(orgLiteral);
        GraphConstants.setValue(cell.getAttributes(), newLiteral);
        graph.getGraphLayoutCache().editCell(cell, cell.getAttributes());
    }

    /**
     * @param cell
     */
    private void cloneRDFResourceCell(GraphCell cell) {
        InstanceModel orgInfo = (InstanceModel) GraphConstants.getValue(cell.getAttributes());
        InstanceModel newInfo = new InstanceModel(orgInfo);
        newInfo.setURI(cloneRDFURI(newInfo));
        GraphConstants.setValue(cell.getAttributes(), newInfo);
        graph.getGraphLayoutCache().editCell(cell, cell.getAttributes());
        GraphUtilities.resizeRDFResourceCell(gmanager, newInfo, cell);
        gmanager.selectRDFCell(cell);
    }

    private void cloneRDFSCell(GraphCell cell, RDFSModel newInfo) {
        RDFSModelMap rdfsModelMap = gmanager.getRDFSInfoMap();
        rdfsModelMap.putURICellMap(newInfo, cell);
        GraphConstants.setValue(cell.getAttributes(), newInfo);
        graph.getGraphLayoutCache().editCell(cell, cell.getAttributes());
        GraphUtilities.resizeRDFSResourceCell(gmanager, newInfo, cell);
    }

    /**
     * @param cell
     */
    private void cloneRDFSPropertyCell(GraphCell cell) {
        PropertyModel orgInfo = (PropertyModel) GraphConstants.getValue(cell.getAttributes());
        PropertyModel newInfo = new PropertyModel(orgInfo);
        newInfo.setURI(cloneRDFSURI(newInfo, GraphType.Property));
        cloneRDFSCell(cell, newInfo);
    }

    /**
     * @param cell
     */
    private void cloneRDFSClassCell(GraphCell cell) {
        ClassModel orgInfo = (ClassModel) GraphConstants.getValue(cell.getAttributes());
        ClassModel newInfo = new ClassModel(orgInfo);
        newInfo.setURI(cloneRDFSURI(newInfo, GraphType.Class));
        cloneRDFSCell(cell, newInfo);
    }

    private String cloneRDFSURI(RDFSModel info, GraphType graphType) {
        if (gmanager.isDuplicated(info.getURIStr(), null, graphType)) {
            for (int j = 1; true; j++) {
                String compURI = info.getURIStr() + "-copy" + j;
                if (!gmanager.isDuplicated(compURI, null, graphType)) {
                    return info.getURIStr() + "-copy" + j;
                }
            }
        }
        return info.getURIStr();
    }

    /*
     * リソースが重複しないように，-copy番号をローカル名に追加する
     */
    private String cloneRDFURI(InstanceModel info) {
        if (gmanager.isDuplicated(info.getURIStr(), null, GraphType.Instance)) {
            for (int j = 1; true; j++) {
                String compURI = info.getURIStr() + "-copy" + j;
                if (!gmanager.isDuplicated(compURI, null, GraphType.Instance)) {
                    return info.getURIStr() + "-copy" + j;
                }
            }
        }
        return info.getURIStr();
    }
}
