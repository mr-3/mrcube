/*
 * Project Name: MR^3 (Meta-Model Management based on RDFs Revision Reflection)
 * Project Website: http://mr3.sourceforge.net/
 * 
 * Copyright (C) 2003-2008 Yamaguchi Laboratory, Keio University. All rights reserved. 
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

package org.semanticweb.mmm.mr3.data;

import java.util.*;

import javax.swing.tree.*;

import org.jgraph.graph.*;
import org.semanticweb.mmm.mr3.data.MR3Constants.*;

import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.vocabulary.*;

/**
 * @author takeshi morita
 * 
 */
public class RDFSInfoMap {

    private Map<Resource, RDFSInfo> resourceInfoMap; // Resourceとメタ情報の関連づけ
    private Map<String, GraphCell> classCellMap; // uriとClassの関連づけ
    private Map<String, GraphCell> propertyCellMap; // uriとPropertyの関連づけ
    private Set<Resource> rootProperties; // subPropertyOf Propertyのセット
    private DefaultTreeModel classTreeModel;
    private DefaultTreeModel propTreeModel;
    private Model propertyLabelModel;

    public RDFSInfoMap() {
        resourceInfoMap = new HashMap<Resource, RDFSInfo>();
        classCellMap = new HashMap<String, GraphCell>();
        propertyCellMap = new HashMap<String, GraphCell>();
        rootProperties = new HashSet<Resource>();
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

    public void putResourceInfo(Resource resource, RDFSInfo info) {
        resourceInfoMap.put(resource, info);
    }

    /**
     * resource -> info
     */
    public RDFSInfo getResourceInfo(Resource resource) {
        return resourceInfoMap.get(resource);
    }

    /**
     * 
     * RDFSModelExtraction#extractClassModelの後にクラスのセットを得る
     */
    public Set<String> getClassSet(Set<String> classSet, Resource resource) {
        classSet.add(resource.getURI());
        RDFSInfo info = resourceInfoMap.get(resource);
        if (info == null) { return classSet; }
        for (Resource res : info.getRDFSSubList()) {
            getClassSet(classSet, res);
        }
        return classSet;
    }

    /**
     * 
     * RDFSModelExtraction#extractPropertyModelの後にプロパティのセットを得る
     */
    public Set<String> getPropertySet(Set<String> propertySet, Resource resource) {
        propertySet.add(resource.getURI());
        RDFSInfo info = resourceInfoMap.get(resource);
        if (info == null) { return propertySet; }
        for (Resource res : info.getRDFSSubList()) {
            getClassSet(propertySet, res);
        }
        return propertySet;
    }

    private boolean isRDFDucplicatedCheck(GraphType type, GraphCell cell) {
        if (type == GraphType.RDF) {
            if (cell == null) { return true; }
            RDFResourceInfo resInfo = (RDFResourceInfo) GraphConstants.getValue(cell.getAttributes());
            // 今から作ろうとしてるRDFCellの場合，resInfoは存在しない．
            // 名前を変更しようとしているCellの場合は，resInfoが存在する
            if (resInfo.getTypeCell() == null) { return true; }
            RDFSInfo typeInfo = resInfo.getTypeInfo();
            if (!((typeInfo.getURI().equals(RDFS.Class)) || typeInfo.getURI().equals(RDF.Property))) { return true; }
        } else {
            return true;
        }
        return false;
    }

    /**
     * 
     * 登録しようとしているinfoのURIが重複していればtrue． 重複していなければfalse
     * RDFSInfo#equalsでは，Resource(uri)の重複をチェックしている．
     */
    public boolean isDuplicated(String uri, Object cell, GraphType type) {
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

    public void putURICellMap(RDFSInfo info, GraphCell cell) {
        if (info instanceof ClassInfo) {
            classCellMap.put(info.getURIStr(), cell);
        } else if (info instanceof PropertyInfo) {
            propertyCellMap.put(info.getURIStr(), cell);
        }
    }

    public void removeURICellMap(RDFSInfo info) {
        if (info instanceof ClassInfo) {
            classCellMap.remove(info.getURIStr());
        } else {
            propertyCellMap.remove(info.getURIStr());
        }
    }

    public void removeCellInfo(GraphCell cell) {
        RDFSInfo info = (RDFSInfo) GraphConstants.getValue(cell.getAttributes());
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

    public Object getRDFSCell(Resource uri) {
        if (isClassCell(uri)) { return getClassCell(uri); }
        return getPropertyCell(uri);
    }

    public DefaultMutableTreeNode getRootNode() {
        DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode(getClassCell(RDFS.Resource));
        createRDFSNodes(RDFS.Resource, rootNode);
        return rootNode;
    }

    public DefaultMutableTreeNode getPropRootNode() {
        DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode();
        for (Resource property : rootProperties) {
            RDFSInfo info = getResourceInfo(property);
            DefaultMutableTreeNode node = new DefaultMutableTreeNode(getRDFSCell(property));
            if (info.getRDFSSubList().size() > 0) {
                createRDFSNodes(property, node);
            }
            rootNode.add(node);
        }
        return rootNode;
    }

    private void createRDFSNodes(Resource resource, DefaultMutableTreeNode node) {
        RDFSInfo info = getResourceInfo(resource);
        if (info == null) return;

        for (Resource subRDFS : info.getRDFSSubList()) {
            DefaultMutableTreeNode subNode = new DefaultMutableTreeNode(getRDFSCell(subRDFS));
            RDFSInfo subInfo = getResourceInfo(subRDFS);

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
