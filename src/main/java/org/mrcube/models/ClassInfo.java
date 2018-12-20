/*
 * Project Name: MR^3 (Meta-Model Management based on RDFs Revision Reflection)
 * Project Website: http://mr3.sourceforge.net/
 * 
 * Copyright (C) 2003-2018 Yamaguchi Laboratory, Keio University. All rights reserved.
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

package org.mrcube.models;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.jgraph.graph.GraphCell;
import org.jgraph.graph.GraphConstants;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Takeshi Morita
 * 
 */
public class ClassInfo extends RDFSInfo {

	private static final long serialVersionUID = -3455137904632666118L;

	private transient Set<Resource> subClasses;
	private transient Set<Resource> supClasses;

	public ClassInfo(String uri) {
		super(uri);
		metaClass = RDFS.Class.toString();
		subClasses = new HashSet<>();
		supClasses = new HashSet<>();
	}

	public ClassInfo(ClassInfo info) {
		super(info);
		subClasses = new HashSet<>();
		supClasses = new HashSet<>();
	}

	public Set<Resource> getRDFSSubList() {
		return Collections.unmodifiableSet(subClasses);
	}

	public Model getModel() {
		Model tmpModel = super.getModel();
		Resource res = getURI();

		tmpModel.add(tmpModel.createStatement(res, RDF.type,
				ResourceFactory.createResource(metaClass)));
		for (GraphCell classGraphCell : superRDFS) {
			RDFSInfo classInfo = (RDFSInfo) GraphConstants.getValue(classGraphCell.getAttributes());
			tmpModel.add(tmpModel.createStatement(res, RDFS.subClassOf, classInfo.getURI()));
		}
		return tmpModel;
	}

	public void addSubClass(Resource subClass) {
		subClasses.add(subClass);
	}

	public void clearSubClass() {
		subClasses = new HashSet<>();
	}

	public void addSupClass(Resource supClass) {
		supClasses.add(supClass);
	}

	public Set<Resource> getSupClasses() {
		return Collections.unmodifiableSet(supClasses);
	}

	public void clearSupClass() {
		supClasses = new HashSet<>();
	}

	public String getStatus() {
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

	public String toString() {
		return super.toString();
	}
}
