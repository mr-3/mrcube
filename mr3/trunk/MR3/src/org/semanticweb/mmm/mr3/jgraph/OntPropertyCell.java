/*
 * @(#) RDFSPropertyCell.java
 *
 * Copyright (C) 2003 The MMM Project
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 */

package org.semanticweb.mmm.mr3.jgraph;

import java.awt.*;

import org.jgraph.graph.*;
import org.semanticweb.mmm.mr3.util.*;

/**
 * @author takeshi morita
 */
public class OntPropertyCell extends DefaultGraphCell implements RDFCellStyleChanger {

    public static Color propertyColor = new Color(255, 200, 90);

    public OntPropertyCell() {
        this(null);
    }

    public OntPropertyCell(Object userObject) {
        super(userObject);
    }

    public void changeStyle(RDFGraph graph) {
        GraphUtilities
                .changeCellStyle(graph, this, GraphUtilities.selectedColor, GraphUtilities.selectedBorderColor);
    }

    public void changeDefaultStyle(RDFGraph graph) {
        GraphUtilities.changeDefaultCellStyle(graph, this, propertyColor);
    }
}
