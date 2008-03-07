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

package org.semanticweb.mmm.mr3.jgraph;

import java.awt.*;

import org.jgraph.graph.*;
import org.semanticweb.mmm.mr3.util.*;

/**
 * @author takeshi morita
 */
public class OntClassCell extends DefaultGraphCell implements RDFCellStyleChanger {

    public static Color classColor = Color.green;

    public OntClassCell() {
        this(null);
    }

    public OntClassCell(Object userObject) {
        super(userObject);
    }

    public void changeStyle(RDFGraph graph) {
        GraphUtilities.changeCellStyle(graph, this, GraphUtilities.selectedColor, GraphUtilities.selectedBorderColor);
    }

    public void changeDefaultStyle(RDFGraph graph) {
        GraphUtilities.changeDefaultCellStyle(graph, this, classColor);
    }
}
