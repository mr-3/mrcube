/*
 * @(#) PropertyInfo.java
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

import org.jgraph.graph.*;

import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.vocabulary.*;

/**
 * @author takeshi morita
 * 
 */
public class PropertyInfo extends RDFSInfo {

    private Set<Object> domainSet;
    private Set<Object> rangeSet;
    private boolean isContainer;
    private int num;
    private transient Set<Resource> subProperties;
    private transient Set<RDFNode> supProperties;

    private static final long serialVersionUID = -1326136347122640640L;

    public PropertyInfo(String uri) {
        super(uri);
        metaClass = RDF.Property.toString();
        domainSet = new HashSet<Object>();
        rangeSet = new HashSet<Object>();
        isContainer = false;
        subProperties = new HashSet<Resource>();
        supProperties = new HashSet<RDFNode>();
    }

    public PropertyInfo(PropertyInfo info) {
        super(info);
        domainSet = new HashSet<Object>(info.getDomain());
        rangeSet = new HashSet<Object>(info.getRange());
        isContainer = info.isContainer();
        num = info.getNum();
        subProperties = new HashSet<Resource>();
        supProperties = new HashSet<RDFNode>();
    }

    public Set<Resource> getRDFSSubList() {
        return Collections.unmodifiableSet(subProperties);
    }

    public Model getModel() {
        try {
            Model tmpModel = super.getModel();
            Resource res = getURI();
            tmpModel.add(tmpModel.createStatement(res, RDF.type, ResourceFactory.createResource(metaClass)));
            for (Iterator i = superRDFS.iterator(); i.hasNext();) {
                RDFSInfo propInfo = (RDFSInfo) GraphConstants.getValue(((GraphCell) i.next()).getAttributes());
                if (!propInfo.getURI().equals(MR3Resource.Property)) {
                    tmpModel.add(tmpModel.createStatement(res, RDFS.subPropertyOf, propInfo.getURI()));
                }
            }
            for (Iterator i = domainSet.iterator(); i.hasNext();) {
                RDFSInfo classInfo = (RDFSInfo) GraphConstants.getValue(((GraphCell) i.next()).getAttributes());
                tmpModel.add(tmpModel.createStatement(res, RDFS.domain, classInfo.getURI()));
            }
            for (Iterator i = rangeSet.iterator(); i.hasNext();) {
                RDFSInfo classInfo = (RDFSInfo) GraphConstants.getValue(((GraphCell) i.next()).getAttributes());
                tmpModel.add(tmpModel.createStatement(res, RDFS.range, classInfo.getURI()));
            }
            return tmpModel;
        } catch (RDFException e) {
            e.printStackTrace();
        }
        return model;
    }

    public void addDomain(Object resource) {
        domainSet.add(resource);
    }

    public void addAllDomain(Set<Object> set) {
        domainSet.addAll(set);
    }

    public void removeNullDomain() {
        for (Iterator i = domainSet.iterator(); i.hasNext();) {
            GraphCell cell = (GraphCell) i.next();
            if (GraphConstants.getValue(cell.getAttributes()) == null) {
                domainSet.remove(cell);
            }
        }
    }

    public void removeDomain(Object obj) {
        domainSet.remove(obj);
    }

    public void clearDomain() {
        domainSet = new HashSet<Object>();
    }

    public Set<Object> getDomain() {
        return Collections.unmodifiableSet(domainSet);
    }

    public void addRange(Object resource) {
        rangeSet.add(resource);
    }

    public void addAllRange(Set<Object> set) {
        rangeSet.addAll(set);
    }

    public void removeNullRange() {
        for (Iterator i = rangeSet.iterator(); i.hasNext();) {
            GraphCell cell = (GraphCell) i.next();
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
        rangeSet = new HashSet<Object>();
    }

    public Set<Object> getRange() {
        return Collections.unmodifiableSet(rangeSet);
    }

    public void addSubProperty(Resource resource) {
        subProperties.add(resource);
    }

    public void clearSubProperty() {
        subProperties = new HashSet<Resource>();
    }

    public void addSupProperty(RDFNode resource) {
        supProperties.add(resource);
    }

    public Set<RDFNode> getSupProperties() {
        return Collections.unmodifiableSet(supProperties);
    }

    public void clearSupProperty() {
        supProperties = new HashSet<RDFNode>();
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
