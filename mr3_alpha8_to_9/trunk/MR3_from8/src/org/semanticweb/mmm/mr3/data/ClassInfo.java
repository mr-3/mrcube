/*
 * @(#) ClassInfo.java
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

import java.util.*;

import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.vocabulary.*;

/**
 * @author takeshi morita
 *
 */
public class ClassInfo extends RDFSInfo {

	private static final long serialVersionUID = -3455137904632666118L;

	private transient Set subClasses;
	private transient Set supClasses;

	public ClassInfo(String uri) {
		super(uri);
		metaClass = RDFS.Class.toString();
		subClasses = new HashSet();
		supClasses = new HashSet();
	}

	public ClassInfo(ClassInfo info) {
		super(info);
		subClasses = new HashSet();
		supClasses = new HashSet();
	}

	public Set getRDFSSubList() {
		return Collections.unmodifiableSet(subClasses);
	}

	public Model getModel() {
		try {
			Model tmpModel = super.getModel();
			Resource res = getURI();

			tmpModel.add(tmpModel.createStatement(res, RDF.type, ResourceFactory.createResource(metaClass)));
			for (Iterator i = supRDFS.iterator(); i.hasNext();) {
				RDFSInfo classInfo = rdfsInfoMap.getCellInfo(i.next());
				tmpModel.add(tmpModel.createStatement(res, RDFS.subClassOf, classInfo.getURI()));
			}
			return tmpModel;
		} catch (RDFException rdfex) {
			rdfex.printStackTrace();
		}
		return model;
	}

	public void addSubClass(Resource subClass) {
		subClasses.add(subClass);
	}

	public void clearSubClass() {
		subClasses = new HashSet();
	}

	public void addSupClass(Resource supClass) {
		supClasses.add(supClass);
	}

	public Set getSupClasses() {
		return Collections.unmodifiableSet(supClasses);
	}

	public void clearSupClass() {
		supClasses = new HashSet();
	}

	public String toString() {
		String msg = super.toString();

		if (subClasses.size() > 0) {
			msg += "SubClasses: " + subClasses.toString() + "\n";
		}
		if (supClasses.size() > 0) {
			msg += "SuperClasses: " + supClasses.toString() + "\n";
		}
		msg += getModelString();

		return msg;
	}
}
