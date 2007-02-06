/*
 * @(#) RDFSInfo.java
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
import org.semanticweb.mmm.mr3.data.MR3Constants.*;
import org.semanticweb.mmm.mr3.jgraph.*;
import org.semanticweb.mmm.mr3.util.*;

import com.hp.hpl.jena.rdf.model.*;

/**
 * 
 * RDFSÇÃèÓïÒÇäiî[Ç∑ÇÈ
 * 
 * @author takeshi morita
 * 
 */
public abstract class RDFSInfo extends ResourceInfo implements Serializable {

    protected transient String uri;
    protected transient String metaClass;

    transient protected Model model;
    transient protected Set<GraphCell> superRDFS;
    transient protected RDFSInfoMap rdfsInfoMap = RDFSInfoMap.getInstance();

    private static final long serialVersionUID = -2970145279588775430L;

    RDFSInfo() {
    }

    RDFSInfo(String uri) {
        this.uri = uri;
        superRDFS = new HashSet<GraphCell>();
        labelList = new ArrayList<MR3Literal>();
        commentList = new ArrayList<MR3Literal>();
        model = ModelFactory.createDefaultModel();
    }

    RDFSInfo(RDFSInfo info) {
        uri = info.getURIStr();
        metaClass = info.getMetaClass();
        superRDFS = new HashSet<GraphCell>();
        labelList = new ArrayList<MR3Literal>(info.getLabelList());
        commentList = new ArrayList<MR3Literal>(info.getCommentList());
        model = info.getInnerModel();
    }

    public abstract Set<Resource> getRDFSSubList();

    /**
     * URIÇ™ìôÇµÇØÇÍÇŒÅCìôÇµÇ¢Ç∆Ç∑ÇÈ
     */
    public boolean equals(Object o) {
        if (o instanceof String) { // completeeditingóp
            return o.equals(uri);
        }
        RDFSInfo info = (RDFSInfo) o;
        return info.getURIStr().equals(uri);
    }

    public boolean isSameInfo(RDFSInfo rdfsInfo) {
        return rdfsInfo.getURIStr().equals(uri) && rdfsInfo.getMetaClass().equals(metaClass);
    }

    public void addStatement(Statement stmt) {
        try {
            model.add(stmt);
        } catch (RDFException e) {
            e.printStackTrace();
        }
    }

    public Model getInnerModel() {
        return model;
    }

    public void setInnerModel(Model m) {
        model = m;
    }

    public Model getModel() throws RDFException {
        Model tmpModel = ModelFactory.createDefaultModel();
        tmpModel.add(model);
        tmpModel.add(super.getModel(ResourceFactory.createResource(uri)));
        return tmpModel;
    }

    public void setMetaClass(String metaClass) {
        this.metaClass = metaClass;
    }

    public String getMetaClass() {
        return metaClass;
    }

    public void setSuperRDFS(Set<GraphCell> set) {
        superRDFS.clear();
        if (set != null) {
            superRDFS.addAll(set);
        }
    }

    public void addSupRDFS(GraphCell rdfs) {
        superRDFS.add(rdfs);
    }

    public Set<GraphCell> getSuperRDFS() {
        return Collections.unmodifiableSet(superRDFS);
    }

    public void setURI(String str) {
        uri = str;
    }

    public Resource getURI() {
        return ResourceFactory.createResource(uri);
    }

    public String getURIStr() {
        return uri;
    }

    public String getNameSpace() {
        return ResourceFactory.createResource(uri).getNameSpace();
    }

    public String getLocalName() {
        return ResourceFactory.createResource(uri).getLocalName();
    }

    public String getModelString() {
        StringWriter writer = new StringWriter();
        try {
            getModel().write(writer);
        } catch (RDFException e) {
            e.printStackTrace();
        }
        return writer.toString();
    }

    public String toString() {
        Resource resource = ResourceFactory.createResource(uri);
        switch (GraphManager.cellViewType) {
        case URI:
            return GraphUtilities.getNSPrefix(resource);
        case ID:
            if (resource.getLocalName().length() != 0) { return resource.getLocalName(); }
            break;
        case LABEL:
            if (getDefaultLabel(GraphManager.getDefaultLang()) != null) {
                return getDefaultLabel(GraphManager.getDefaultLang()).getString();
            } else if (getFirstLabel() != null) { return getFirstLabel().getString(); }
            break;
        }
        return GraphUtilities.getNSPrefix(resource);
    }
}
