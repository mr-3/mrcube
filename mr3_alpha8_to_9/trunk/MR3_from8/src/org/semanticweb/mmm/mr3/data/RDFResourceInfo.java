/*
 * @(#) RDFResourceInfo.java
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
import com.hp.hpl.jena.rdf.model.impl.*;

public class RDFResourceInfo implements Serializable {

	private Object typeCell; // RDFS Classに対応するCellを保持する
	private Set typeCells; // RDFリソースのタイプを複数保持する．
	private GraphCell typeViewCell; // RDF Resourceにつく矩形のCellを保持する

	private String uri;
	private URIType uriType;
	private String uriTypeStr;
	transient private RDFSInfoMap rdfsInfoMap = RDFSInfoMap.getInstance();
	private static final long serialVersionUID = -2998293866936983365L;

	public RDFResourceInfo(URIType type, String uri, GraphCell typeCell) {
		setURIType(type);
		this.uri = uri;
		this.typeViewCell = typeCell;
	}

	public RDFResourceInfo(RDFResourceInfo info) {
		uri = info.getURIStr();
		setURIType(info.getURIType());
		typeViewCell = (GraphCell) info.getTypeViewCell();
		typeCell = info.getTypeCell();
	}

	private static Model anonModel = ModelFactory.createDefaultModel();

	/** RDFのTypeの見せ方を変える */
	public void setTypeCellValue(GraphCell cell) {
		Object value = null;
		if (cell != null) {
			Map map = cell.getAttributes();
			value = GraphConstants.getValue(map);
		} else {
			value = "";
		}

		Map typeMap = typeViewCell.getAttributes();
		GraphConstants.setValue(typeMap, value);
		typeViewCell.changeAttributes(typeMap);
	}

	public boolean equals(Object o) {
		String uriStr = (String) o;
		return uriStr.equals(uri);
	}

	public void setTypeCellValue() {
		if (typeViewCell != null) { // 仮ルートを作る時，typeViewCellを作らないため
			setTypeCellValue((GraphCell) typeCell);
		}
	}

	public void setTypeCell(Object type) {
		this.typeCell = type;
		setTypeCellValue();
	}

	public Resource getType() {
		RDFSInfo info = rdfsInfoMap.getCellInfo(typeCell);
		if (info == null || info.getURI() == null) {
			return ResourceFactory.createResource("");
		} else {
			return info.getURI();
		}
	}

	public Object getTypeCell() {
		return typeCell;
	}

	public void setTypeViewCell(GraphCell cell) {
		typeViewCell = cell;
		setTypeCellValue();
	}

	public Object getTypeViewCell() {
		return typeViewCell;
	}

	public void setURIType(URIType type) {
		uriType = type;
		uriTypeStr = type.toString();
	}

	public URIType getURIType() {
		return uriType;
	}

	public void setURI(String str) {
		uri = str;
	}

	public Resource getURI() {
		if (uriType == URIType.ANONYMOUS) {
			return new ResourceImpl(new AnonId(uri));
		} else {
			return ResourceFactory.createResource(uri);
		}
	}

	public String getURIStr() {
		return uri;
	}

	public String toString() {
		String msg = "URIType: " + uriType + "\n";
		msg += "URI: " + uri + "\n";
		msg += "Type: " + typeCell + "\n";
		return msg;
	}
}
