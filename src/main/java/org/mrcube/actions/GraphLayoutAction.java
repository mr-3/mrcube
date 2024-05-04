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

import org.mrcube.jgraph.GraphManager;
import org.mrcube.layout.GraphLayoutUtilities;
import org.mrcube.models.MR3Constants.GraphType;
import org.mrcube.utils.Translator;
import org.mrcube.utils.Utilities;

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
    private static final ImageIcon LEFT_TO_RIGHT_LAYOUT_GRAPH_ICON = Utilities.getImageIcon("left_to_right_ic_share_black_18dp.png");
    private static final ImageIcon UP_TO_DOWN_LAYOUT_GRAPH_ICON = Utilities.getImageIcon("up_to_down_ic_share_black_18dp.png");

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
        if (type == GraphType.CLASS) {
            return Translator.getString("Class");
        } else if (type == GraphType.PROPERTY) {
            return Translator.getString("Property");
        } else if (type == GraphType.INSTANCE){
            return Translator.getString("Instance");
        }
        return Translator.getString("Instance");
    }

    public void actionPerformed(ActionEvent arg0) {
        switch (graphType) {
            case INSTANCE -> GraphLayoutUtilities.RDF_LAYOUT_DIRECTION = direction;
            case CLASS -> GraphLayoutUtilities.CLASS_LAYOUT_DIRECTION = direction;
            case PROPERTY -> GraphLayoutUtilities.PROPERTY_LAYOUT_DIRECTION = direction;
        }
        gmanager.applyLayout(graphType);
    }
}
