/*
 * Project Name: MR^3 (Meta-Model Management based on RDFs Revision Reflection)
 * Project Website: http://mr3.sourceforge.net/
 * 
 * Copyright (C) 2003-2008 Yamaguchi Laboratory, Keio University. All rights reserved. 
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

package org.semanticweb.mmm.mr3.actions;

import java.awt.event.*;

import javax.swing.*;

import org.jgraph.graph.*;
import org.semanticweb.mmm.mr3.jgraph.*;
import org.semanticweb.mmm.mr3.util.*;

/**
 * @author takeshi morita
 * 
 */
public class GroupAction extends AbstractAction {

    private RDFGraph graph;
    private static final String TITLE = Translator.getString("Action.Group.Text");
    private static final ImageIcon ICON = Utilities.getImageIcon(Translator.getString("Action.Group.Icon"));

    public GroupAction(RDFGraph g) {
        super(TITLE, ICON);
        graph = g;
        putValue(SHORT_DESCRIPTION, TITLE);
        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_G, KeyEvent.CTRL_MASK));
    }

    // Returns the total number of cells in a graph
    private static int getCellCount(RDFGraph graph) {
        Object[] cells = graph.getAllCells();
        return cells.length;
    }

    public static void group(RDFGraph graph) {
        Object[] cells = graph.getSelectionCells();
        cells = graph.order(cells);

        if (cells != null && cells.length > 0) {
            int count = getCellCount(graph);
            DefaultGraphCell group = new DefaultGraphCell(new Integer(count - 1));
            ParentMap map = new ParentMap();
            for (int i = 0; i < cells.length; i++) {
                map.addEntry(cells[i], group);
            }
            // graph.getGraphLayoutCache().insert(new Object[] { group }, null,
            // null, map, null);
            graph.getGraphLayoutCache().insert(new Object[] { group}, null, null, map);
        }
    }

    // Create a Group that Contains the Cells
    public void actionPerformed(ActionEvent e) {
        group(graph);
    }
}
