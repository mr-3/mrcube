package mr3.data;
import java.io.*;
import java.util.*;

import javax.swing.tree.*;

import com.hp.hpl.mesa.rdf.jena.model.*;
import com.hp.hpl.mesa.rdf.jena.vocabulary.*;

public class RDFSInfoMap {

	private Map resourceInfoMap; // Resourceとメタ情報の関連づけ
	private Map cellInfoMap; // CELLとメタ情報の関連づけ
	private Map edgeInfoMap; // Edge(RDFプロパティのセル)とRDFSプロパティのセルとの関連づけ
	private Map classCellMap; // uriとClassの関連づけ 
	private Map propertyCellMap; // uriとPropertyの関連づけ
	private Set rootProperties; // subPropertyOf Propertyのセット
	private DefaultTreeModel classTreeModel;
	private DefaultTreeModel propTreeModel;

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
		list.add(resourceInfoMap);
		list.add(cellInfoMap);
		list.add(classCellMap);
		list.add(propertyCellMap);
		list.add(edgeInfoMap);
		return list;
	}

	public void setState(List list) {
		resourceInfoMap.putAll((Map) list.get(0));
		cellInfoMap.putAll((Map) list.get(1));
		classCellMap.putAll((Map) list.get(3));
		propertyCellMap.putAll((Map) list.get(4));
		edgeInfoMap.putAll((Map) list.get(5));
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

	public void clearTemporaryMap() {
		resourceInfoMap.clear();
		rootProperties.clear();
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

	/** 登録しようとしているinfoのURIが重複していればtrue．重複していなければfalse */
	/*
	 * RDFSInfo#equalsでは，Resource(uri)の重複をチェックしている．
	 */
	public boolean isDuplicated(String uri, Object cell, GraphType type) {
		Collection entrySet = cellInfoMap.entrySet();
		for (Iterator i = entrySet.iterator(); i.hasNext();) {
			Map.Entry entry = (Map.Entry) i.next();
			Object infoCell = entry.getKey();
			Object info = entry.getValue();
			if (info.equals(uri) && infoCell != cell) {
				if (type == GraphType.RDF) {
					RDFResourceInfo resInfo = resInfoMap.getCellInfo(cell);
					// 今から作ろうとしてるRDFCellの場合，resInfoは存在しない．
					// 名前を変更しようとしているCellの場合は，resInfoが存在する
					if (resInfo == null || resInfo.getTypeCell() == null) {
						//						System.out.println("1 duplicated rdfs");
						return true;
					}
					RDFSInfo typeInfo = rdfsInfoMap.getCellInfo(resInfo.getTypeCell());
					if (!((typeInfo.getURI().equals(RDFS.Class)) || typeInfo.getURI().equals(RDF.Property))) {
						//						System.out.println("2 duplicated rdfs");
						return true;
					}
				} else {
					//					System.out.println("3 duplicated rdfs");
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
			classCellMap.put(info.getURI(), cell);
		} else if (info instanceof PropertyInfo) {
			propertyCellMap.put(info.getURI(), cell);
		}
	}

	public void removeURICellMap(RDFSInfo info) {
		if (info instanceof ClassInfo) {
			classCellMap.remove(info.getURI());
		} else {
			propertyCellMap.remove(info.getURI());
		}
	}

	public void removeCellInfo(Object cell) {
		RDFSInfo info = (RDFSInfo) cellInfoMap.get(cell);
		if (info != null) {
			classCellMap.remove(info.getURI());
			propertyCellMap.remove(info.getURI());
			cellInfoMap.remove(cell);
		}
	}

	public boolean isClassCell(Resource uri) {
		return getClassCell(uri) != null;
	}

	public Object getClassCell(Resource uri) {
		return classCellMap.get(uri);
	}

	public boolean isPropertyCell(Resource uri) {
		return getPropertyCell(uri) != null;
	}

	public Object getPropertyCell(Resource uri) {
		return propertyCellMap.get(uri);
	}

	public Object getRDFSCell(Resource uri) {
		if (isClassCell(uri)) {
			return getClassCell(uri);
		} else {
			return getPropertyCell(uri);
		}
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
			Literal lit = (Literal) i.next();
			newInfo.addLabel(lit);
		}
		// コメントをコピーする
		for (Iterator i = orgInfo.getCommentList().iterator(); i.hasNext();) {
			Literal comment = (Literal) i.next();
			newInfo.addComment(comment);
		}
		newInfo.setLabel(orgInfo.getLabel());
		newInfo.setComment(orgInfo.getComment());
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
		return newInfo;
	}

	public String toString() {
		String msg = "";

		msg += "\n************** resourceInfoMap *****************\n";
		if (resourceInfoMap != null)
			msg += resourceInfoMap;
		msg += "\n**************** cellInfoMap *********************\n";
		if (cellInfoMap != null)
			msg += cellInfoMap;

		return msg;
	}
}
