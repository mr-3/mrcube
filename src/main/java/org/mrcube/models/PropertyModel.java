/*
 * Project Name: MR^3 (Meta-Model Management based on RDFs Revision Reflection)
 * Project Website: http://mrcube.org/
 * 
 * Copyright (C) 2003-2019 Yamaguchi Laboratory, Keio University. All rights reserved.
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
import org.apache.jena.rdf.model.RDFNode;
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
public class PropertyModel extends RDFSModel {

	private Set<GraphCell> domainSet;
	private Set<GraphCell> rangeSet;
	private boolean isContainer;
	private int num;
	private transient Set<Resource> subProperties;
	private transient Set<RDFNode> supProperties;

	private static final long serialVersionUID = -1326136347122640640L;

	public PropertyModel(String uri) {
		super(uri);
		metaClass = RDF.Property.toString();
		domainSet = new HashSet<>();
		rangeSet = new HashSet<>();
		isContainer = false;
		subProperties = new HashSet<>();
		supProperties = new HashSet<>();
	}

	public PropertyModel(PropertyModel info) {
		super(info);
		domainSet = new HashSet<>(info.getDomain());
		rangeSet = new HashSet<>(info.getRange());
		isContainer = info.isContainer();
		num = info.getNum();
		subProperties = new HashSet<>();
		supProperties = new HashSet<>();
	}

	public Set<Resource> getRDFSSubList() {
		return Collections.unmodifiableSet(subProperties);
	}

	public Model getModel() {
		Model tmpModel = super.getModel();
		Resource res = getURI();
		tmpModel.add(tmpModel.createStatement(res, RDF.type,
				ResourceFactory.createResource(metaClass)));
		for (GraphCell supRDFSCell : superRDFS) {
			RDFSModel propInfo = (RDFSModel) GraphConstants.getValue(supRDFSCell.getAttributes());
			if (!propInfo.getURI().equals(MR3Resource.Property)) {
				tmpModel.add(tmpModel.createStatement(res, RDFS.subPropertyOf, propInfo.getURI()));
			}
		}
		for (GraphCell domainClassCell : domainSet) {
			RDFSModel classInfo = (RDFSModel) GraphConstants
					.getValue(domainClassCell.getAttributes());
			tmpModel.add(tmpModel.createStatement(res, RDFS.domain, classInfo.getURI()));
		}
		for (GraphCell rangeClassCell : rangeSet) {
			RDFSModel classInfo = (RDFSModel) GraphConstants.getValue(rangeClassCell.getAttributes());
			tmpModel.add(tmpModel.createStatement(res, RDFS.range, classInfo.getURI()));
		}
		return tmpModel;
	}

	public void addDomain(GraphCell resource) {
		domainSet.add(resource);
	}

	public void addAllDomain(Set<GraphCell> set) {
		domainSet.addAll(set);
	}

	public void removeNullDomain() {
		for (GraphCell cell : domainSet) {
			if (GraphConstants.getValue(cell.getAttributes()) == null) {
				domainSet.remove(cell);
			}
		}
	}

	public void removeDomain(Object obj) {
		domainSet.remove(obj);
	}

	public void clearDomain() {
		domainSet = new HashSet<>();
	}

	public Set<GraphCell> getDomain() {
		return Collections.unmodifiableSet(domainSet);
	}

	public void addRange(GraphCell resource) {
		rangeSet.add(resource);
	}

	public void addAllRange(Set<GraphCell> set) {
		rangeSet.addAll(set);
	}

	public void removeNullRange() {
		for (GraphCell cell : rangeSet) {
			// if (rdfsInfoMap.getCellInfo(cell) == null) {
			if (cell.getAttributes() == null) {
				rangeSet.remove(cell);
			}
		}
	}

	public void removeRange(Object obj) {
		rangeSet.remove(obj);
	}

	public void clearRange() {
		rangeSet = new HashSet<>();
	}

	public Set<GraphCell> getRange() {
		return Collections.unmodifiableSet(rangeSet);
	}

	public void addSubProperty(Resource resource) {
		subProperties.add(resource);
	}

	public void clearSubProperty() {
		subProperties = new HashSet<>();
	}

	public void addSupProperty(RDFNode resource) {
		supProperties.add(resource);
	}

	public Set<RDFNode> getSupProperties() {
		return Collections.unmodifiableSet(supProperties);
	}

	public void clearSupProperty() {
		supProperties = new HashSet<>();
	}

	public boolean isContainer() {
		return isContainer;
	}

	public void setContainer(boolean t) {
		isContainer = t;
	}

	public int getNum() {
		return num;
	}

	public void setNum(int n) {
		num = n;
	}

	public String getStatus() {
		String msg = super.toString();

		if (domainSet.size() > 0) {
			msg += "domain: " + domainSet.toString() + "\n";
		}
		if (rangeSet.size() > 0) {
			msg += "range: " + rangeSet.toString() + "\n";
		}
		if (subProperties.size() > 0) {
			msg += "SubProperty: " + subProperties.toString() + "\n";
		}
		if (supProperties.size() > 0) {
			msg += "SuperProperty: " + supProperties.toString() + "\n";
		}
		msg += getModelString();
		return msg;
	}

	public String toString() {
		return super.toString();
	}
}
