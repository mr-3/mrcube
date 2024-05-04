/*
 * Project Name: MR^3 (Meta-Model Management based on RDFs Revision Reflection)
 * Project Website: http://mrcube.org/
 *
 * Copyright (C) 2003-2020 Takeshi Morita. All rights reserved.
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
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.jgraph.graph.GraphCell;
import org.jgraph.graph.GraphConstants;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import java.util.*;

/**
 * @author Takeshi Morita
 */
public class RDFSModelMap {

    private final Map<Resource, RDFSModel> resourceInfoMap; // Resourceとメタ情報の関連づけ
    private final Map<String, GraphCell> classCellMap; // uriとClassの関連づけ
    private final Map<String, GraphCell> propertyCellMap; // uriとPropertyの関連づけ
    private final Set<Resource> rootProperties; // subPropertyOf Propertyのセット
    private final DefaultTreeModel classTreeModel;
    private final DefaultTreeModel propTreeModel;
    private final Model propertyLabelModel;

    public RDFSModelMap() {
        resourceInfoMap = new HashMap<>();
        classCellMap = new HashMap<>();
        propertyCellMap = new HashMap<>();
        rootProperties = new HashSet<>();
        classTreeModel = new DefaultTreeModel(null);
        propTreeModel = new DefaultTreeModel(null);
        propertyLabelModel = ModelFactory.createDefaultModel();
    }

    public TreeModel getClassTreeModel() {
        return classTreeModel;
    }

    public void setClassTreeModel() {
        classTreeModel.setRoot(getRootNode());
    }

    public TreeModel getPropertyTreeModel() {
        return propTreeModel;
    }

    public void setPropTreeModel() {
        propTreeModel.setRoot(getPropRootNode());
    }

    public void clear() {
        resourceInfoMap.clear();
        classCellMap.clear();
        propertyCellMap.clear();
        rootProperties.clear();
        classTreeModel.setRoot(null);
    }

    public void addPropertyLabelModel(Statement stmt) {
        propertyLabelModel.add(stmt);
    }

    public Model getPropertyLabelModel() {
        return propertyLabelModel;
    }

    public void clearTemporaryObject() {
        resourceInfoMap.clear();
        rootProperties.clear();
    }

    public void addRootProperties(Resource resource) {
        rootProperties.add(resource);
    }

    public Set<Resource> getRootProperties() {
        return Collections.unmodifiableSet(rootProperties);
    }

    public void putResourceInfo(Resource resource, RDFSModel model) {
        resourceInfoMap.put(resource, model);
    }

    public RDFSModel getResourceInfo(Resource resource) {
        return resourceInfoMap.get(resource);
    }

    /**
     * RDFSModelExtraction#extractClassModelの後にクラスのセットを得る
     */
    public Set<String> getClassSet(Set<String> classSet, Resource resource) {
        classSet.add(resource.getURI());
        RDFSModel model = resourceInfoMap.get(resource);
        if (model == null) {
            return classSet;
        }
        for (Resource res : model.getRDFSSubList()) {
            getClassSet(classSet, res);
        }
        return classSet;
    }

    /**
     * RDFSModelExtraction#extractPropertyModelの後にプロパティのセットを得る
     */
    public Set<String> getPropertySet(Set<String> propertySet, Resource resource) {
        propertySet.add(resource.getURI());
        RDFSModel model = resourceInfoMap.get(resource);
        if (model == null) {
            return propertySet;
        }
        for (Resource res : model.getRDFSSubList()) {
            getClassSet(propertySet, res);
        }
        return propertySet;
    }

    private boolean isRDFDucplicatedCheck(MR3Constants.GraphType type, GraphCell cell) {
        if (type == MR3Constants.GraphType.INSTANCE) {
            if (cell == null) {
                return true;
            }
            InstanceModel resInfo = (InstanceModel) GraphConstants.getValue(cell
                    .getAttributes());
            // 今から作ろうとしてるRDFCellの場合，resInfoは存在しない．
            // 名前を変更しようとしているCellの場合は，resInfoが存在する
            if (resInfo.getTypeCell() == null) {
                return true;
            }
            RDFSModel typeInfo = resInfo.getTypeInfo();
            if (!((typeInfo.getURI().equals(RDFS.Class)) || typeInfo.getURI().equals(RDF.Property))) {
                return true;
            }
        } else {
            return true;
        }
        return false;
    }

    /**
     * 登録しようとしているinfoのURIが重複していればtrue． 重複していなければfalse
     * RDFSModel#equalsでは，Resource(uri)の重複をチェックしている．
     */
    public boolean isDuplicated(String uri, Object cell, MR3Constants.GraphType type) {
        GraphCell classCell = classCellMap.get(uri);
        GraphCell propertyCell = propertyCellMap.get(uri);
        if (classCell == null && propertyCell == null) {
            return false;
        } else if (classCell != null && classCell != cell) {
            return isRDFDucplicatedCheck(type, (GraphCell) cell);
        } else if (propertyCell != null && propertyCell != cell) {
            return isRDFDucplicatedCheck(type, (GraphCell) cell);
        } else {
            return false;
        }
    }

    public void putURICellMap(RDFSModel model, GraphCell cell) {
        if (model instanceof ClassModel) {
            classCellMap.put(model.getURIStr(), cell);
        } else if (model instanceof PropertyModel) {
            propertyCellMap.put(model.getURIStr(), cell);
        }
    }

    public void removeURICellMap(RDFSModel info) {
        if (info instanceof ClassModel) {
            classCellMap.remove(info.getURIStr());
        } else {
            propertyCellMap.remove(info.getURIStr());
        }
    }

    public void removeCellInfo(GraphCell cell) {
        RDFSModel info = (RDFSModel) GraphConstants.getValue(cell.getAttributes());
        if (info != null) {
            removeURICellMap(info);
        }
    }

    public boolean isClassCell(Resource uri) {
        return getClassCell(uri) != null;
    }

    public GraphCell getClassCell(Resource uri) {
        return classCellMap.get(uri.getURI());
    }

    public boolean isPropertyCell(Resource uri) {
        return getPropertyCell(uri) != null;
    }

    public Object getPropertyCell(Resource uri) {
        return propertyCellMap.get(uri.getURI());
    }

    private Object getRDFSCell(Resource uri) {
        if (isClassCell(uri)) {
            return getClassCell(uri);
        }
        return getPropertyCell(uri);
    }

    private DefaultMutableTreeNode getRootNode() {
        DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode(getClassCell(RDFS.Resource));
        createRDFSNodes(RDFS.Resource, rootNode);
        return rootNode;
    }

    private DefaultMutableTreeNode getPropRootNode() {
        DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode();
        for (Resource property : rootProperties) {
            RDFSModel info = getResourceInfo(property);
            DefaultMutableTreeNode node = new DefaultMutableTreeNode(getRDFSCell(property));
            if (info.getRDFSSubList().size() > 0) {
                createRDFSNodes(property, node);
            }
            rootNode.add(node);
        }
        return rootNode;
    }

    private void createRDFSNodes(Resource resource, DefaultMutableTreeNode node) {
        RDFSModel info = getResourceInfo(resource);
        if (info == null)
            return;

        for (Resource subRDFS : info.getRDFSSubList()) {
            DefaultMutableTreeNode subNode = new DefaultMutableTreeNode(getRDFSCell(subRDFS));
            RDFSModel subInfo = getResourceInfo(subRDFS);

            if (subInfo.getRDFSSubList().size() > 0) {
                createRDFSNodes(subRDFS, subNode);
            }
            node.add(subNode);
        }
    }

    public String toString() {
        String msg = "";
        msg += "\n##### ResourceInfoMap #####\n";
        if (resourceInfoMap != null) {
            msg += resourceInfoMap;
        }
        return msg;
    }
}
