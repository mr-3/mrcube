/*
 * @(#) RDFLiteralInfoMap.java
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

import org.semanticweb.mmm.mr3.util.*;

import com.hp.hpl.jena.rdf.model.*;

/**
 * @author takeshi morita
 *
 */
public class RDFLiteralInfoMap {

	private Map cellInfoMap;
	private static RDFLiteralInfoMap litInfoMap = new RDFLiteralInfoMap();

	private RDFLiteralInfoMap() {
		cellInfoMap = new HashMap();
	}

	public static RDFLiteralInfoMap getInstance() {
		return litInfoMap;
	}

	public Literal cloneRDFLiteralInfo(Literal orgInfo) {
		return RDFLiteralUtil.createLiteral(orgInfo.getString(), orgInfo.getLanguage(), orgInfo.getDatatype());
	}

	public void putCellInfo(Object cell, Literal info) {
		cellInfoMap.put(cell, info);
	}

	public Literal getCellInfo(Object cell) {
		return (Literal) cellInfoMap.get(cell);
	}

	public Serializable getState() {
		HashMap map = new HashMap();
		try {
			for (Iterator i = cellInfoMap.keySet().iterator(); i.hasNext();) {
				Object cell = i.next();
				Literal lit = getCellInfo(cell);
				map.put(cell, new MR3Literal(lit.getString(), lit.getLanguage(), lit.getDatatypeURI()));
			}
		} catch (RDFException e) {
			e.printStackTrace();
		}
		return map;
	}

	public void setState(Map newMap) {
		for (Iterator i = newMap.keySet().iterator(); i.hasNext();) {
			Object cell = i.next();
			MR3Literal lit = (MR3Literal) newMap.get(cell);
			putCellInfo(cell, RDFLiteralUtil.createLiteral(lit.getString(), lit.getLanguage(), lit.getDatatype()));
		}
	}

	public void clear() {
		cellInfoMap.clear();
	}

	public Collection values() {
		return cellInfoMap.values();
	}
}
