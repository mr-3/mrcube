/*
 * @(#) RDFGraphModel.java
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

import org.jgraph.graph.*;

/*
 * 
 * @author takeshi morita
 *
 */
public class RDFGraphModel extends DefaultGraphModel {

    public boolean acceptsSource(Object edge, Object port) {
        return (((Edge) edge).getTarget() != port); // Source only Valid if not Equal Target
    }

    public boolean acceptsTarget(Object edge, Object port) {
        return (((Edge) edge).getSource() != port); // Target only Valid if not Equal Source
    }       
}
