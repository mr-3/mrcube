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

package net.sourceforge.mr3.jgraph;

import java.awt.*;

import net.sourceforge.mr3.util.*;

import org.jgraph.graph.*;

/**
 * @author takeshi morita
 */
public class RDFPropertyCell extends DefaultEdge implements RDFCellStyleChanger {

    public static Color rdfPropertyColor = new Color(100, 100, 100);

    public void changeDefaultStyle(RDFGraph graph) {
        GraphUtilities.changeDefaultCellStyle(graph, this, rdfPropertyColor);
    }

    public void changeStyle(RDFGraph graph) {
        GraphUtilities.changeCellStyle(graph, this, GraphUtilities.selectedBorderColor,
                GraphUtilities.selectedBorderColor);
    }

    public RDFPropertyCell() {
        this(null);
    }

    public RDFPropertyCell(Object userObject) {
        super(userObject);
    }
}
