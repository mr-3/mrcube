/*
 * @(#) RDFResourceInfoMap.java
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

import java.io.*;
import java.util.*;

import org.jgraph.graph.*;

import com.hp.hpl.jena.rdf.model.*;

/**
 * @author takeshi morita
 *
 */
public class RDFResourceInfoMap {

	private Map cellInfoMap;
	private static RDFResourceInfoMap resInfoMap = new RDFResourceInfoMap();

	private RDFResourceInfoMap() {
		cellInfoMap = new HashMap();
	}

	public static RDFResourceInfoMap getInstance() {
		return resInfoMap;
	}

	public RDFResourceInfo cloneRDFResourceInfo(RDFResourceInfo orgInfo, GraphCell typeCell) {
		RDFResourceInfo newInfo = null;
		if (orgInfo.getURIType() == URIType.ANONYMOUS) {
//			newInfo = new RDFResourceInfo(orgInfo.getURIType(), new AnonId().toString(), typeCell); 
			newInfo = new RDFResourceInfo(orgInfo.getURIType(), ResourceFactory.createResource().toString(), typeCell); 
		} else {
			newInfo = new RDFResourceInfo(orgInfo.getURIType(), orgInfo.getURIStr(), typeCell);
		}
		newInfo.setTypeCell(orgInfo.getTypeCell());
		return newInfo;
	}

	public void putCellInfo(Object cell, RDFResourceInfo info) {
		cellInfoMap.put(cell, info);
	}

	public RDFResourceInfo getCellInfo(Object cell) {
		return (RDFResourceInfo) cellInfoMap.get(cell);
	}

	public void removeCellInfo(Object cell) {
		cellInfoMap.remove(cell);
	}

	public Serializable getState() {
		return (Serializable) cellInfoMap;
	}

	public void setState(Map newMap) {
		for (Iterator i = newMap.keySet().iterator(); i.hasNext();) {
			Object cell = i.next();
			RDFResourceInfo info = (RDFResourceInfo) newMap.get(cell);
			putCellInfo(cell, new RDFResourceInfo(info));
		}
	}

	public void clear() {
		cellInfoMap.clear();
	}

	public Collection entrySet() {
		return cellInfoMap.entrySet();
	}
}
