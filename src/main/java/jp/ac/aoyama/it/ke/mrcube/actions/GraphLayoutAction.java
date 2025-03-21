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

package jp.ac.aoyama.it.ke.mrcube.actions;

import jp.ac.aoyama.it.ke.mrcube.jgraph.GraphManager;
import jp.ac.aoyama.it.ke.mrcube.layout.GraphLayoutUtilities;
import jp.ac.aoyama.it.ke.mrcube.models.MR3Constants.GraphType;
import jp.ac.aoyama.it.ke.mrcube.utils.Translator;
import jp.ac.aoyama.it.ke.mrcube.utils.Utilities;

import javax.swing.*;
import java.awt.event.ActionEvent;

/**
 * @author Takeshi Morita
 */
public class GraphLayoutAction extends AbstractAction {

    private final String direction;
    private final GraphType graphType;
    private final GraphManager gmanager;
    private static final String TITLE = Translator.getString("Menu.View.ApplyLayout.Text");
    private static final ImageIcon LEFT_TO_RIGHT_LAYOUT_GRAPH_ICON = Utilities.getSVGIcon("left_to_right.svg");
    private static final ImageIcon UP_TO_DOWN_LAYOUT_GRAPH_ICON = Utilities.getSVGIcon("up_to_down.svg");

    public GraphLayoutAction(GraphManager gm, GraphType type, String direction) {
        super(getString(type) + TITLE + " (" + direction + ")");
        gmanager = gm;
        graphType = type;
        this.direction = direction;
        putValue(SHORT_DESCRIPTION, getString(type) + TITLE + " (" + direction + ")");
        if (direction.equals(GraphLayoutUtilities.LEFT_TO_RIGHT)) {
            putValue(Action.SMALL_ICON, LEFT_TO_RIGHT_LAYOUT_GRAPH_ICON);
        } else {
            putValue(Action.SMALL_ICON, UP_TO_DOWN_LAYOUT_GRAPH_ICON);
        }
        GraphLayoutUtilities.LAYOUT_TYPE = GraphLayoutUtilities.VGJ_TREE_LAYOUT;
    }

    private static String getString(GraphType type) {
        if (type == GraphType.Class) {
            return Translator.getString("Class");
        } else if (type == GraphType.Property) {
            return Translator.getString("Property");
        } else if (type == GraphType.Instance){
            return Translator.getString("Instance");
        }
        return Translator.getString("Instance");
    }

    public void actionPerformed(ActionEvent arg0) {
        switch (graphType) {
            case Instance -> GraphLayoutUtilities.RDF_LAYOUT_DIRECTION = direction;
            case Class -> GraphLayoutUtilities.CLASS_LAYOUT_DIRECTION = direction;
            case Property -> GraphLayoutUtilities.PROPERTY_LAYOUT_DIRECTION = direction;
        }
        gmanager.applyLayout(graphType);
    }
}
