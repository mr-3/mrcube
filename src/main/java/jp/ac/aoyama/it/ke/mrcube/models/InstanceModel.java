/*
 * Project Name: MR^3 (Meta-Model Management based on RDFs Revision Reflection)
 * Project Website: https://mr-3.github.io/
 *
 * Copyright (C) 2003-2024 Takeshi Morita. All rights reserved.
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

import jp.ac.aoyama.it.ke.mrcube.MR3;
import org.apache.jena.rdf.model.AnonId;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.impl.ResourceImpl;
import org.jgraph.graph.GraphCell;
import org.jgraph.graph.GraphConstants;
import jp.ac.aoyama.it.ke.mrcube.jgraph.GraphManager;
import jp.ac.aoyama.it.ke.mrcube.jgraph.RDFGraph;
import jp.ac.aoyama.it.ke.mrcube.utils.GraphUtilities;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Objects;

public class InstanceModel extends ResourceModel implements Serializable {

    private GraphCell typeCell;
    // private Set typeCells; // RDFリソースのタイプを複数保持する．
    private GraphCell typeViewCell; // RDF Resourceにつく矩形のCellを保持する

    private String uri;
    private MR3Constants.URIType uriType;
    private static final long serialVersionUID = -2998293866936983365L;

    // 実験用
    private Resource typeRes;

    public InstanceModel(MR3Constants.URIType type, String uri) {
        setURIType(type);
        this.uri = uri;
        labelList = new ArrayList<>();
        commentList = new ArrayList<>();
    }

    public InstanceModel(InstanceModel info) {
        setURIType(info.getURIType());
        if (info.getURIType() == MR3Constants.URIType.ANONYMOUS) {
            uri = ResourceFactory.createResource().toString();
        } else {
            uri = info.getURIStr();
        }
        typeCell = info.getTypeCell();
        labelList = new ArrayList<>(info.getLabelList());
        commentList = new ArrayList<>(info.getCommentList());
    }

    public RDFSModel getTypeInfo() {
        if (MR3.OFF_META_MODEL_MANAGEMENT) {
            if (typeRes != null) {
                ClassModel tmpInfo = new ClassModel("");
                tmpInfo.setURI(typeRes.getURI());
                return tmpInfo;
            }
            return NULL_INFO;
        }

        if (typeCell == null) {
            return NULL_INFO;
        }
        return (RDFSModel) GraphConstants.getValue(typeCell.getAttributes());
    }

    public boolean equals(Object o) {
        if (o instanceof String) {
            return o.equals(uri);
        }
        InstanceModel info = (InstanceModel) o;
        return info.getURIStr().equals(uri);
    }

    public boolean isSameInfo(InstanceModel resInfo) {
        return resInfo.getURIType().equals(uriType) && resInfo.getURIStr().equals(uri)
                && resInfo.getType().equals(getType());
    }

    private static final ClassModel NULL_INFO = new ClassModel("");

    public void setTypeCell(GraphCell cell, RDFGraph graph) {
        if (MR3.OFF_META_MODEL_MANAGEMENT) {
            if (cell != null) {
                RDFSModel info = (RDFSModel) GraphConstants.getValue(cell.getAttributes());
                typeRes = info.getURI();
            } else {
                typeRes = null;
            }
            if (typeViewCell != null) { // 仮ルートを作る時，typeViewCellを作らないため
                GraphConstants.setValue(typeViewCell.getAttributes(), Objects.requireNonNullElse(typeRes, ""));
                graph.getGraphLayoutCache().editCell(typeViewCell, typeViewCell.getAttributes());
            }
            return;
        }

        typeCell = cell;
        if (typeViewCell != null) { // 仮ルートを作る時，typeViewCellを作らないため
            GraphConstants.setValue(typeViewCell.getAttributes(), getTypeInfo());
            graph.getGraphLayoutCache().editCell(typeViewCell, typeViewCell.getAttributes());
        }
    }

    public boolean hasType() {
        return getTypeInfo() != NULL_INFO;
    }

    public Resource getType() {
        return getTypeInfo().getURI();
    }

    public String getTypeQName() {
        return GraphUtilities.getQName(getTypeInfo().getURI());
    }

    public GraphCell getTypeCell() {
        return typeCell;
    }

    public void setTypeViewCell(GraphCell cell) {
        typeViewCell = cell;
        if (typeViewCell != null) {
            GraphConstants.setValue(typeViewCell.getAttributes(), getTypeInfo());
            typeViewCell.getAttributes().applyMap(typeViewCell.getAttributes());
        }
    }

    public Object getTypeViewCell() {
        return typeViewCell;
    }

    public void setURIType(MR3Constants.URIType type) {
        uriType = type;
    }

    public MR3Constants.URIType getURIType() {
        return uriType;
    }

    public void setURI(String str) {
        uri = str;
    }

    public Resource getURI() {
        if (uriType == MR3Constants.URIType.ANONYMOUS) {
            return new ResourceImpl(new AnonId(uri));
        }
        return ResourceFactory.createResource(uri);
    }

    public String getURIStr() {
        return uri;
    }

    public String getQName() {
        return GraphUtilities.getQName(getURI());
    }

    public String getStatus() {
        String msg = "URIType: " + uriType + "\n";
        msg += "URI: " + uri + "\n";
        msg += "Type: " + getTypeInfo().getURIStr() + "\n";
        return msg;
    }

    public String toString() {
        if (uriType == MR3Constants.URIType.ANONYMOUS) {
            return "";
        }
        switch (GraphManager.cellViewType) {
            case LABEL:
                if (getDefaultLabel(GraphManager.getDefaultLang()) != null) {
                    return getDefaultLabel(GraphManager.getDefaultLang()).getString();
                } else if (getFirstLabel() != null) {
                    return getFirstLabel().getString();
                }
                break;
            case ID:
                Resource resource = getURI();
                if (resource.getLocalName().length() != 0) {
                    return resource.getLocalName();
                }
                break;
            case URI:
                return GraphUtilities.getQName(getURI());
        }
        return GraphUtilities.getQName(getURI());
    }
}
