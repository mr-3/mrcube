/*
 * @(#)  2004/12/21
 *
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
