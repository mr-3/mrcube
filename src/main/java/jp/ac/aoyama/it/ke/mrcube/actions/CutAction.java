/*
 * Project Name: MR^3 (Meta-Model Management based on RDFs Revision Reflection)
 * Project Website: https://mr-3.github.io/
 *
 * Copyright (C) 2003-2024 Takeshi Morita. All rights reserved.
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

package jp.ac.aoyama.it.ke.mrcube.actions;

import jp.ac.aoyama.it.ke.mrcube.views.HistoryManager;
import org.jgraph.graph.GraphCell;
import org.jgraph.graph.GraphConstants;
import jp.ac.aoyama.it.ke.mrcube.jgraph.GraphManager;
import jp.ac.aoyama.it.ke.mrcube.jgraph.RDFGraph;
import jp.ac.aoyama.it.ke.mrcube.models.ClassModel;
import jp.ac.aoyama.it.ke.mrcube.models.MR3Constants.GraphType;
import jp.ac.aoyama.it.ke.mrcube.models.MR3Constants.HistoryType;
import jp.ac.aoyama.it.ke.mrcube.models.PropertyModel;
import jp.ac.aoyama.it.ke.mrcube.models.RDFSModelMap;
import jp.ac.aoyama.it.ke.mrcube.utils.Translator;
import jp.ac.aoyama.it.ke.mrcube.utils.Utilities;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

/**
 * @author Takeshi Morita
 */
public class CutAction extends AbstractAction {
    private final RDFGraph graph;
    private final GraphManager gmanager;
    private static final String TITLE = Translator.getString("Action.Cut.Text");
    private static final ImageIcon ICON = Utilities.getImageIcon(Translator.getString("Action.Cut.Icon"));

    public CutAction(RDFGraph g, GraphManager gm) {
        super(TITLE, ICON);
        graph = g;
        gmanager = gm;
        putValue(SHORT_DESCRIPTION, TITLE);
        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_X,
                Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
    }

    public void actionPerformed(ActionEvent e) {
        if (graph.getType() == GraphType.Instance) {
            HistoryManager.saveHistory(HistoryType.CUT_RDF_GRAPH);
        } else if (graph.getType() == GraphType.Class) {
            RDFSModelMap rdfsModelMap = gmanager.getRDFSInfoMap();
            for (Object cell : graph.getSelectionCells()) {
                GraphCell gcell = (GraphCell) cell;
                if (RDFGraph.isRDFSClassCell(gcell)) {
                    ClassModel orgInfo = (ClassModel) GraphConstants.getValue(gcell.getAttributes());
                    rdfsModelMap.removeURICellMap(orgInfo);
                    rdfsModelMap.removeCellInfo(gcell);
                }
            }
            HistoryManager.saveHistory(HistoryType.CUT_CLASS_GRAPH);
        } else if (graph.getType() == GraphType.Property) {
            RDFSModelMap rdfsModelMap = gmanager.getRDFSInfoMap();
            for (Object cell : graph.getSelectionCells()) {
                GraphCell gcell = (GraphCell) cell;
                if (RDFGraph.isRDFSPropertyCell(gcell)) {
                    PropertyModel orgInfo = (PropertyModel) GraphConstants.getValue(gcell
                            .getAttributes());
                    rdfsModelMap.removeURICellMap(orgInfo);
                    rdfsModelMap.removeCellInfo(gcell);
                }
            }
            HistoryManager.saveHistory(HistoryType.CUT_PROPERTY_GRAPH);
        }
        TransferHandler.getCutAction().actionPerformed(new ActionEvent(graph, e.getID(), e.getActionCommand()));
    }
}
