/*
 * Project Name: MR^3 (Meta-Model Management based on RDFs Revision Reflection)
 * Project Website: https://mr-3.github.io/
 *
 * Copyright (C) 2003-2025 Takeshi Morita. All rights reserved.
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

package jp.ac.aoyama.it.ke.mrcube.models;

import org.apache.jena.rdf.model.*;
import org.jgraph.graph.GraphCell;
import jp.ac.aoyama.it.ke.mrcube.jgraph.GraphManager;
import jp.ac.aoyama.it.ke.mrcube.utils.GraphUtilities;

import java.io.Serializable;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * RDFSの情報を格納する
 *
 * @author Takeshi Morita
 */
public abstract class RDFSModel extends ResourceModel implements Serializable {

    private transient String uri;
    transient String metaClass;

    private transient Model model;
    transient Set<GraphCell> superRDFS;

    private static final long serialVersionUID = -2970145279588775430L;

    RDFSModel(String uri) {
        this.uri = uri;
        superRDFS = new HashSet<>();
        labelList = new ArrayList<>();
        commentList = new ArrayList<>();
        model = ModelFactory.createDefaultModel();
    }

    RDFSModel(RDFSModel info) {
        uri = info.getURIStr();
        metaClass = info.getMetaClass();
        superRDFS = new HashSet<>();
        labelList = new ArrayList<>(info.getLabelList());
        commentList = new ArrayList<>(info.getCommentList());
        model = info.getInnerModel();
    }

    public abstract Set<Resource> getRDFSSubList();

    /**
     * URIが等しければ，等しいとする
     */
    public boolean equals(Object o) {
        if (o instanceof String) { // completeediting用
            return o.equals(uri);
        }
        RDFSModel info = (RDFSModel) o;
        return info.getURIStr().equals(uri);
    }

    public boolean isSameInfo(RDFSModel rdfsModel) {
        return rdfsModel.getURIStr().equals(uri) && rdfsModel.getMetaClass().equals(metaClass);
    }

    public void addStatement(Statement stmt) {
        model.add(stmt);
    }

    private Model getInnerModel() {
        return model;
    }

    public void setInnerModel(Model m) {
        model = m;
    }

    Model getModel() {
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

    public String getMetaClassQName() {
        return GraphUtilities.getQName(ResourceFactory.createResource(metaClass));
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

    public String getQName() {
        Resource resource = ResourceFactory.createResource(uri);
        return GraphUtilities.getQName(resource);
    }

    public String getNameSpace() {
        return ResourceFactory.createResource(uri).getNameSpace();
    }

    public String getLocalName() {
        return ResourceFactory.createResource(uri).getLocalName();
    }

    String getModelString() {
        StringWriter writer = new StringWriter();
        getModel().write(writer);
        return writer.toString();
    }

    public String toString() {
        Resource resource = ResourceFactory.createResource(uri);
        switch (GraphManager.cellViewType) {
            case ID:
                if (resource.getLocalName().length() != 0) {
                    return resource.getLocalName();
                }
                break;
            case LABEL:
                if (getDefaultLabel(GraphManager.getDefaultLang()) != null) {
                    return getDefaultLabel(GraphManager.getDefaultLang()).getString();
                } else if (getFirstLabel() != null) {
                    return getFirstLabel().getString();
                }
                break;
            case URI:
                return GraphUtilities.getQName(resource);
        }
        return GraphUtilities.getQName(resource);
    }
}
