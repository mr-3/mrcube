/*
 * @(#) GraphType.java
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

package org.semanticweb.mmm.mr3.data;

/*
 * 
 * @author takeshi morita
 */
public class GraphType {

    private String type;
    
    private GraphType(String t) {
        type = t;
    }

	public static final GraphType RDF = new GraphType("RDF");
	public static final GraphType REAL_RDF = new GraphType("REAL_RDF");
    public static final GraphType CLASS = new GraphType("Class");
    public static final GraphType PROPERTY = new GraphType("Property");

    public String toString() {
        return type;
    }
}
