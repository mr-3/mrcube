/*
 * Project Name: MR^3 (Meta-Model Management based on RDFs Revision Reflection)
 * Project Website: http://mrcube.org/
 *
 * Copyright (C) 2003-2022 Takeshi Morita. All rights reserved.
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

package org.mrcube.jgraph;

import org.jgraph.graph.DefaultEdge;
import org.mrcube.utils.GraphUtilities;

import java.awt.*;

/**
 * @author Takeshi Morita
 */
public class InstancePropertyCell extends DefaultEdge implements RDFCellStyleChanger {

    public static final Color DEFAULT_FG_COLOR = OntPropertyCell.DEFAULT_BG_COLOR;
    public static final Color DEFAULT_BORDER_COLOR = new Color(43, 43, 43);
    public static final Color DEFAULT_SELECTED_BORDER_COLOR =  new Color(15, 35, 80);

    public static Color foregroundColor = DEFAULT_FG_COLOR;
    public static Color borderColor = DEFAULT_BORDER_COLOR;
    public static Color selectedBorderColor = DEFAULT_SELECTED_BORDER_COLOR;

    public void changeDefaultCellStyle(RDFGraph graph) {
        GraphUtilities.changeCellStyle(graph, this, foregroundColor, null, borderColor);
    }

    public void changeSelectedCellStyle(RDFGraph graph) {
        GraphUtilities.changeCellStyle(graph, this, foregroundColor, null, selectedBorderColor);
    }

    public InstancePropertyCell() {
        this(null);
    }

    public InstancePropertyCell(Object userObject) {
        super(userObject);
    }
}
