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

import jp.ac.aoyama.it.ke.mrcube.jgraph.GraphManager;
import jp.ac.aoyama.it.ke.mrcube.models.MR3Constants.GraphType;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

/**
 * @author Takeshi Morita
 */
public class SelectAllNodesAction extends MR3AbstractAction {

    private static final String NAME = "SelectAllNodes";
    private final GraphType graphType;
    private final GraphManager graphManager;

    public SelectAllNodesAction(GraphManager gm, GraphType type) {
        super(NAME);
        graphType = type;
        graphManager = gm;
        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_A,
                Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
    }

    public String getName() {
        return NAME;
    }

    public void actionPerformed(ActionEvent e) {
        if (graphType == GraphType.Instance) {
            graphManager.getRDFGraph().selectAllNodes();
        } else if (graphType == GraphType.Class) {
            graphManager.getClassGraph().selectAllNodes();
        } else if (graphType == GraphType.Property) {
            graphManager.getPropertyGraph().selectAllNodes();
        }
    }

}
