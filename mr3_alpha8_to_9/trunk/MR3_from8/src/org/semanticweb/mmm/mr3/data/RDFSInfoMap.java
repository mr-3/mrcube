/*
 * @(#) RDFSInfoMap.java
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

import javax.swing.tree.*;

import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.vocabulary.*;

/**
 * 
 * @author takeshi morita
 *
 */
public class RDFSInfoMap {

	private Map resourceInfoMap; // Resourceとメタ情報の関連づけ
	private Map cellInfoMap; // CELLとメタ情報の関連づけ
	private Map edgeInfoMap; // Edge(RDFプロパティのセル)とRDFSプロパティのセルとの関連づけ
	private Map classCellMap; // uriとClassの関連づけ 
	private Map propertyCellMap; // uriとPropertyの関連づけ
	private Set rootProperties; // subPropertyOf Propertyのセット
	private DefaultTreeModel classTreeModel;
	private DefaultTreeModel propTreeModel;
	private Model propertyLabelModel;
	private static RDFResourceInfoMap resInfoMap = RDFResourceInfoMap.getInstance();
	private static RDFSInfoMap rdfsInfoMap = new RDFSInfoMap();

	private RDFSInfoMap() {
		resourceInfoMap = new HashMap();
		cellInfoMap = new HashMap();
		classCellMap = new HashMap();
		propertyCellMap = new HashMap();
		edgeInfoMap = new HashMap();
		rootProperties = new HashSet();
		classTreeModel = new DefaultTreeModel(null);
		propTreeModel = new DefaultTreeModel(null);
		propertyLabelModel = ModelFactory.createDefaultModel();
	}

	public static RDFSInfoMap getInstance() {
		return rdfsInfoMap;
	}

	public TreeModel getClassTreeModel() {
		return classTreeModel;
	}

	public void setClassTreeModel() {
		classTreeModel.setRoot(getRootNode());
	}

	public TreeModel getPropTreeModel() {
		return propTreeModel;
	}

	public void setPropTreeModel() {
		propTreeModel.setRoot(getPropRootNode());
	}

	public Serializable getState() {
		ArrayList list = new ArrayList();
		list.add(cellInfoMap);
		list.add(classCellMap);
		list.add(propertyCellMap);
		list.add(edgeInfoMap);
		return list;
	}

	public void setState(List list) {
		setCellInfoMap((Map) list.get(0));
		classCellMap.putAll((Map) list.get(1));
		propertyCellMap.putAll((Map) list.get(2));
		edgeInfoMap.putAll((Map) list.get(3));
	}

	private void setCellInfoMap(Map map) {
		for (Iterator i = map.keySet().iterator(); i.hasNext();) {
			Object cell = i.next();
			RDFSInfo info = (RDFSInfo) map.get(cell);
			if (info instanceof ClassInfo) {
				cellInfoMap.put(cell, new ClassInfo((ClassInfo) info));
			} else if (info instanceof PropertyInfo) {
				cellInfoMap.put(cell, new PropertyInfo((PropertyInfo) info));
			}
		}
	}

	public void clear() {
		resourceInfoMap.clear();
		cellInfoMap.clear();
		classCellMap.clear();
		propertyCellMap.clear();
		edgeInfoMap.clear();
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
//		propertyLabelModel = ModelFactory.createDefaultModel();
	}

	public void addRootProperties(Resource resource) {
		rootProperties.add(resource);
	}

	public Set getRootProperties() {
		return Collections.unmodifiableSet(rootProperties);
	}

	public void putResourceInfo(Resource resource, RDFSInfo info) {
		resourceInfoMap.put(resource, info);
	}

	/** resource -> info */
	public RDFSInfo getResourceInfo(Resource resource) {
		return (RDFSInfo) resourceInfoMap.get(resource);
	}

	/** RDFSModelExtraction#extractClassModelの後にクラスのセットを得る */
	public Set getClassSet(Set classSet, Resource resource) {
		classSet.add(resource.getURI());
		RDFSInfo info = (RDFSInfo) resourceInfoMap.get(resource);
		for (Iterator i = info.getRDFSSubList().iterator(); i.hasNext();) {
			Resource res = (Resource) i.next();
			getClassSet(classSet, res);
		}
		return classSet;
	}
	
	/** RDFSModelExtraction#extractPropertyModelの後にプロパティのセットを得る */
	public Set getPropertySet(Set propertySet, Resource resource) {
		propertySet.add(resource.getURI());
		RDFSInfo info = (RDFSInfo) resourceInfoMap.get(resource);
		for (Iterator i = info.getRDFSSubList().iterator(); i.hasNext();) {
			Resource res = (Resource) i.next();
			getClassSet(propertySet, res);
		}
		return propertySet;
	}

	/*
	 * 登録しようとしているinfoのURIが重複していればtrue．重複していなければfalse  
	 * RDFSInfo#equalsでは，Resource(uri)の重複をチェックしている．
	 */
	public boolean isDuplicated(String uri, Object cell, GraphType type, String baseURI) {
		Collection entrySet = cellInfoMap.entrySet();
		for (Iterator i = entrySet.iterator(); i.hasNext();) {
			Map.Entry entry = (Map.Entry) i.next();
			Object infoCell = entry.getKey();
			//			Object info = entry.getValue();
			RDFSInfo rdfsInfo = (RDFSInfo) entry.getValue();
			String tmpURI = rdfsInfo.getURIStr();
			if (tmpURI.equals(uri) && infoCell != cell) {
				if (type == GraphType.RDF) {
					RDFResourceInfo resInfo = resInfoMap.getCellInfo(cell);
					// 今から作ろうとしてるRDFCellの場合，resInfoは存在しない．
					// 名前を変更しようとしているCellの場合は，resInfoが存在する
					if (resInfo == null || resInfo.getTypeCell() == null) {
						return true;
					}
					RDFSInfo typeInfo = rdfsInfoMap.getCellInfo(resInfo.getTypeCell());
					if (!((typeInfo.getURI().equals(RDFS.Class)) || typeInfo.getURI().equals(RDF.Property))) {
						return true;
					}
				} else {
					return true;
				}
			}
		}
		return false;
	}

	public void putCellInfo(Object cell, RDFSInfo info) {
		cellInfoMap.put(cell, info);
		putURICellMap(info, cell);
	}

	public void putURICellMap(RDFSInfo info, Object cell) {
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

	public void removeCellInfo(Object cell) {
		RDFSInfo info = (RDFSInfo) cellInfoMap.get(cell);
		if (info != null) {
			classCellMap.remove(info.getURIStr());
			propertyCellMap.remove(info.getURIStr());
			cellInfoMap.remove(cell);
		}
	}

	public boolean isClassCell(Resource uri) {
		return getClassCell(uri) != null;
	}

	public Object getClassCell(Resource uri) {
		return classCellMap.get(uri.getURI());
	}

	public boolean isPropertyCell(Resource uri) {
		return getPropertyCell(uri) != null;
	}

	public Object getPropertyCell(Resource uri) {
		return propertyCellMap.get(uri.getURI());
	}

	public Object getRDFSCell(Resource uri) {
		if (isClassCell(uri)) {
			return getClassCell(uri);
		} else {
			return getPropertyCell(uri);
		}
	}

	public Set getCellSet() {
		return Collections.unmodifiableSet(cellInfoMap.keySet());
	}

	/** cell -> info */
	public RDFSInfo getCellInfo(Object cell) {
		return (RDFSInfo) cellInfoMap.get(cell);
	}

	public void putEdgeInfo(Object edge, Object propCell) {
		edgeInfoMap.put(edge, propCell);
	}

	public Object getEdgeInfo(Object edge) {
		return edgeInfoMap.get(edge);
	}

	public DefaultMutableTreeNode getRootNode() {
		DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode(getClassCell(RDFS.Resource));
		//		DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode(RDFS.Resource);
		createRDFSNodes(RDFS.Resource, rootNode);
		return rootNode;
	}

	public DefaultMutableTreeNode getPropRootNode() {
		DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode();
		for (Iterator i = rootProperties.iterator(); i.hasNext();) {
			Resource property = (Resource) i.next();
			RDFSInfo info = rdfsInfoMap.getResourceInfo(property);
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
		if (info == null)
			return;

		for (Iterator i = info.getRDFSSubList().iterator(); i.hasNext();) {
			Resource subRDFS = (Resource) i.next();

			DefaultMutableTreeNode subNode = new DefaultMutableTreeNode(getRDFSCell(subRDFS));
			//			DefaultMutableTreeNode subNode = new DefaultMutableTreeNode(subRDFS);
			RDFSInfo subInfo = getResourceInfo(subRDFS);

			if (subInfo.getRDFSSubList().size() > 0) {
				createRDFSNodes(subRDFS, subNode);
			}
			node.add(subNode);
		}
	}

	private void cloneRDFSInfo(RDFSInfo orgInfo, RDFSInfo newInfo) {
		newInfo.setURI(orgInfo.getURIStr());
		// Labelをコピーする
		for (Iterator i = orgInfo.getLabelList().iterator(); i.hasNext();) {
			MR3Literal lit = (MR3Literal) i.next();
			newInfo.addLabel(lit);
		}
		// コメントをコピーする
		for (Iterator i = orgInfo.getCommentList().iterator(); i.hasNext();) {
			MR3Literal comment = (MR3Literal) i.next();
			newInfo.addComment(comment);
		}
		newInfo.setLastLabel(orgInfo.getLastLabel());
		newInfo.setLastComment(orgInfo.getLastComment());
		newInfo.setIsDefinedby(orgInfo.getIsDefinedBy().toString());
		newInfo.setInnerModel(orgInfo.getInnerModel());
	}

	public ClassInfo cloneClassInfo(ClassInfo orgInfo) {
		ClassInfo newInfo = new ClassInfo("");
		cloneRDFSInfo(orgInfo, newInfo);
		return newInfo;
	}

	public PropertyInfo clonePropertyInfo(PropertyInfo orgInfo) {
		PropertyInfo newInfo = new PropertyInfo("");
		cloneRDFSInfo(orgInfo, newInfo);
		newInfo.addAllDomain(orgInfo.getDomain());
		newInfo.addAllRange(orgInfo.getRange());
		return newInfo;
	}

	public String toString() {
		String msg = "";

		msg += "\n************** ResourceInfoMap *****************\n";
		if (resourceInfoMap != null)
			msg += resourceInfoMap;
		msg += "\n**************** CellInfoMap *********************\n";
		if (cellInfoMap != null)
			msg += cellInfoMap;

		return msg;
	}
}
