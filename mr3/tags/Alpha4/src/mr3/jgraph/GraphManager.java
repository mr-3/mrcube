package mr3.jgraph;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.prefs.*;

import javax.swing.*;

import mr3.data.*;
import mr3.layout.*;
import mr3.ui.*;
import mr3.util.*;

import com.hp.hpl.mesa.rdf.jena.common.*;
import com.hp.hpl.mesa.rdf.jena.model.*;
import com.hp.hpl.mesa.rdf.jena.vocabulary.*;
import com.jgraph.*;
import com.jgraph.graph.*;

public class GraphManager {

	private RDFGraph rdfGraph;
	private RDFGraph realRDFGraph;
	private RDFGraph classGraph;
	private RDFGraph propGraph;

	private RDFCellMaker cellMaker;

	private RDFResourceInfoMap resInfoMap = RDFResourceInfoMap.getInstance();
	private RDFLiteralInfoMap litInfoMap = RDFLiteralInfoMap.getInstance();
	private RDFSInfoMap rdfsInfoMap = RDFSInfoMap.getInstance();

	private AttributeDialog attrDialog;
	private ReferenceListDialog refDialog;
	private Set prefixNSInfoSet;
	private CellViewType cellViewType;
	private AbstractLevelInfo abstractLevelInfo;

	private Preferences userPrefs;
	private String baseURI;

	public GraphManager(AttributeDialog attrD, Preferences prefs) {
		rdfGraph = new RDFGraph(this, attrD, GraphType.RDF);
		realRDFGraph = new RDFGraph(this, attrD, GraphType.REAL_RDF);
		classGraph = new RDFGraph(this, attrD, GraphType.CLASS);
		propGraph = new RDFGraph(this, attrD, GraphType.PROPERTY);
		registerComponent();
		cellMaker = new RDFCellMaker(this);

		userPrefs = prefs;
		attrDialog = attrD;
		refDialog = new ReferenceListDialog("Referenced Resource List", this);
		abstractLevelInfo = new AbstractLevelInfo();
		prefixNSInfoSet = new HashSet();
		baseURI = userPrefs.get(PrefConstants.BaseURI, "http://mr3");
	}

	public RDFGraph getRDFGraph() {
		return rdfGraph;
	}

	public boolean isRDFGraph(Object graph) {
		return graph == rdfGraph;
	}

	public RDFGraph getRealRDFGraph() {
		return realRDFGraph;
	}

	public RDFGraph getClassGraph() {
		return classGraph;
	}
	public boolean isClassGraph(Object graph) {
		return graph == classGraph;
	}

	public RDFGraph getPropertyGraph() {
		return propGraph;
	}

	public boolean isPropertyGraph(Object graph) {
		return graph == propGraph;
	}

	public RDFGraph getGraph(GraphType type) {
		if (type == GraphType.RDF) {
			return rdfGraph;
		} else if (type == GraphType.CLASS) {
			return classGraph;
		} else if (type == GraphType.PROPERTY) {
			return propGraph;
		}
		return null;
	}

	public void setCellViewType(CellViewType type) {
		cellViewType = type;
	}

	public void setPrefixNSInfoSet(Set infoSet) {
		prefixNSInfoSet = infoSet;
	}

	public Set getPrefixNSInfoSet() {
		return Collections.unmodifiableSet(prefixNSInfoSet);
	}

	public JComponent getRefDialog() {
		return refDialog;
	}

	public ArrayList storeState() {
		ArrayList list = new ArrayList();
		list.add(rdfsInfoMap.getState());
		list.add(resInfoMap.getState());
		list.add(litInfoMap.getState());
		list.add(rdfGraph.getRDFState());
		list.add(classGraph.getRDFState());
		list.add(propGraph.getRDFState());
		return list;
	}

	// indexを返す
	public int loadState(ArrayList list) {
		rdfsInfoMap.setState((List) list.get(0));
		resInfoMap.setState((Map) list.get(1));
		litInfoMap.setState((Map) list.get(2));
		rdfGraph.setRDFState(list.get(3));
		classGraph.setRDFState(list.get(4));
		propGraph.setRDFState(list.get(5));
		return 6;
	}

	public void selectAllRDFNodes() {
		ChangeCellAttributes.isChangedSelectedColor = false;
		rdfGraph.selectAllNodes();
		ChangeCellAttributes.isChangedSelectedColor = true;
	}

	public void selectAllClassNodes() {
		ChangeCellAttributes.isChangedSelectedColor = false;
		classGraph.selectAllNodes();
		ChangeCellAttributes.isChangedSelectedColor = true;
	}

	public void selectAllPropertyNodes() {
		ChangeCellAttributes.isChangedSelectedColor = false;
		propGraph.selectAllNodes();
		ChangeCellAttributes.isChangedSelectedColor = true;
	}

	public void selectAllNodes() {
		ChangeCellAttributes.isChangedSelectedColor = false;
		rdfGraph.selectAllNodes();
		classGraph.selectAllNodes();
		propGraph.selectAllNodes();
		ChangeCellAttributes.isChangedSelectedColor = true;
	}

	public void setAntialias() {
		boolean isAntialias = userPrefs.getBoolean(PrefConstants.Antialias, true);
		rdfGraph.setAntiAliased(isAntialias);
		propGraph.setAntiAliased(isAntialias);
		classGraph.setAntiAliased(isAntialias);
	}

	public String getBaseURI() {
		return baseURI;
	}

	public void setBaseURI(String base) {
		baseURI = base;
		changeCellView();
	}

	private boolean isRDFResourceDuplicated(String uri, Object cell, GraphType type) {
		Collection entrySet = resInfoMap.entrySet();
		for (Iterator i = entrySet.iterator(); i.hasNext();) {
			Map.Entry entry = (Map.Entry) i.next();
			Object infoCell = entry.getKey();
			Object info = entry.getValue();

			// RDFResourceInfoでequalsメソッドをオーバーライドして，URIと比較可能としている
			if (info.equals(uri) && infoCell != cell) {
				RDFResourceInfo resInfo = (RDFResourceInfo) info;
				RDFSInfo rdfsInfo = rdfsInfoMap.getCellInfo(resInfo.getTypeCell());
				//				System.out.println("RDF Resource Duplicated");
				/*
				 *  Classエディタ内の重複チェックをしていて，RDFエディタ内のクラス定義にかかった場合
				 *   重複とみなさないようにする．
				 */
				if (!(type == GraphType.CLASS
					&& rdfsInfo != null
					&& rdfsInfo.getURI().equals(RDFS.Class)
					|| type == GraphType.PROPERTY
					&& rdfsInfo != null
					&& rdfsInfo.getURI().equals(RDF.Property))) {
					return true;
				}
			}
		}
		return false;
	}

	public boolean isEmptyURI(String uri) {
		if (uri.equals("")) {
			JOptionPane.showMessageDialog(null, "URI is empty", "Warning", JOptionPane.ERROR_MESSAGE);
			return true;
		} else {
			return false;
		}
	}

	public boolean isDuplicatedWithDialog(String uri, Object cell, GraphType type) {
		if (isDuplicated(uri, cell, type)) {
			JOptionPane.showInternalMessageDialog(getGraph(type), "URI is duplicated", "Warning", JOptionPane.ERROR_MESSAGE);
			return true;
		} else {
			return false;
		}
	}

	public boolean isDuplicated(String uri, Object cell, GraphType type) {
		return (rdfsInfoMap.isDuplicated(uri, cell, type) || isRDFResourceDuplicated(uri, cell, type));
	}

	/** 名前空間のリストを返す */
	public Set getNameSpaceSet(GraphType type) {
		if (type == GraphType.RDF) {
			return getRDFNameSpaceSet();
		} else if (type == GraphType.CLASS) {
			return getClassNameSpaceSet();
		} else if (type == GraphType.PROPERTY) {
			return getPropertyNameSpaceSet();
		}
		return new HashSet();
	}

	public void registerComponent() {
		ToolTipManager.sharedInstance().registerComponent(rdfGraph);
		ToolTipManager.sharedInstance().registerComponent(classGraph);
		ToolTipManager.sharedInstance().registerComponent(propGraph);
	}

	public Set getRDFNameSpaceSet() {
		Set nameSpaces = new HashSet();

		Object[] rdfCells = rdfGraph.getAllCells();
		for (int i = 0; i < rdfCells.length; i++) {
			Object cell = rdfCells[i];
			if (rdfGraph.isRDFResourceCell(cell)) {
				RDFResourceInfo info = resInfoMap.getCellInfo(rdfCells[i]);
				Resource uri = info.getURI();
				nameSpaces.add(uri.getNameSpace());
			}
		}
		return nameSpaces;
	}

	public Set getClassNameSpaceSet() {
		Set nameSpaces = new HashSet();

		Object[] classCells = classGraph.getAllCells();
		for (int i = 0; i < classCells.length; i++) {
			Object cell = classCells[i];
			if (classGraph.isRDFSClassCell(cell)) {
				RDFSInfo info = rdfsInfoMap.getCellInfo(cell);
				Resource uri = info.getURI();
				nameSpaces.add(uri.getNameSpace());
			}
		}
		return nameSpaces;
	}

	public Set getPropertyNameSpaceSet() {
		Set nameSpaces = new HashSet();

		Object[] propCells = propGraph.getAllCells();
		for (int i = 0; i < propCells.length; i++) {
			Object cell = propCells[i];
			if (propGraph.isRDFSPropertyCell(cell)) {
				RDFSInfo info = rdfsInfoMap.getCellInfo(cell);
				Resource uri = info.getURI();
				nameSpaces.add(uri.getNameSpace());
			}
		}
		return nameSpaces;
	}

	public void setCellValue(GraphCell cell, String value) {
		Map map = cell.getAttributes();
		GraphConstants.setValue(map, value);
		//		cell.setAttributes(map);
		//		set->changeにしないと，cellのValueが変更されず，Cellを削除した時に表示が変化しない
		cell.changeAttributes(map);
	}

	private void setNSPrefix(Resource uri, GraphCell cell) {
		setCellValue(cell, uri.getURI());
		for (Iterator i = prefixNSInfoSet.iterator(); i.hasNext();) {
			PrefixNSInfo prefixNSInfo = (PrefixNSInfo) i.next();
			if (uri.getNameSpace().equals(prefixNSInfo.getNameSpace())) {
				if (prefixNSInfo.isAvailable()) {
					String value = prefixNSInfo.getPrefix() + ":" + uri.getLocalName();
					setCellValue(cell, value);
				}
			}
		}
	}

	private void changeClassAbstractLevel(RDFResourceInfo resInfo, Set rdfsAbstractLevelSet) {
		for (Iterator i = rdfsAbstractLevelSet.iterator(); i.hasNext();) {
			Set region = new HashSet();
			Object cell = i.next();
			addSubRegion(classGraph, getDefaultPort(cell), region);
			if (cell.equals(getClassCell(RDFS.Resource, true))) {
				cell = getClassCell(RDFS.Resource, true);
				resInfo.setTypeCellValue((GraphCell) cell);
			} else if (region.contains(resInfo.getTypeCell())) {
				resInfo.setTypeCellValue((GraphCell) cell);
			}
		}
	}

	private void changePropertyAbstractLevel(Object edge, Object propCell, Set rdfsAbstractLevelSet) {
		for (Iterator i = rdfsAbstractLevelSet.iterator(); i.hasNext();) {
			Set region = new HashSet();
			Object cell = i.next();
			addSubRegion(propGraph, getDefaultPort(cell), region);
			if (region.contains(propCell)) {
				setCellValue((GraphCell) edge, propGraph.convertValueToString(cell));
			}
		}
	}

	private String getRDFLabel(DefaultGraphCell cell) {
		String label = "";
		Port port = (Port) cell.getChildAt(0);
		for (Iterator i = port.edges(); i.hasNext();) {
			Edge edge = (Edge) i.next();
			if (rdfsInfoMap.getEdgeInfo(edge) == rdfsInfoMap.getPropertyCell(RDFS.label)) {
				GraphCell gc = (GraphCell) rdfGraph.getTargetVertex(edge);
				Map map = gc.getAttributes();
				label = (String) GraphConstants.getValue(map);
				label = RDFLiteralUtil.fixString(label);
			}
		}
		return label;
	}

	private void changeRDFCellView() {
		Object[] rdfCells = rdfGraph.getAllCells();
		for (int i = 0; i < rdfCells.length; i++) {
			GraphCell cell = (GraphCell) rdfCells[i];
			if (rdfGraph.isRDFResourceCell(cell)) {
				changeRDFResourceCellView(cell);
			} else if (rdfGraph.isRDFPropertyCell(cell)) {
				changeRDFPropertyCellView(cell);
			}
		}
		rdfGraph.getGraphLayoutCache().reload();
		rdfGraph.repaint();
	}

	private void changeRDFPropertyCellView(GraphCell cell) {
		Object propCell = rdfsInfoMap.getEdgeInfo(cell);
		if (propCell == null) {
			setCellValue(cell, "");
		} else {
			setCellValue(cell, propGraph.convertValueToString(propCell));
			Map map = cell.getAttributes();
		}
		if (abstractLevelInfo.isSelectAbstractLevelMode()) {
			changePropertyAbstractLevel(cell, propCell, abstractLevelInfo.getPropertyAbstractLevelSet());
		}
	}

	private void changeRDFResourceCellView(GraphCell cell) {
		RDFResourceInfo info = resInfoMap.getCellInfo(cell);
		info.setTypeCellValue(); // Layoutのために必要
		Resource uri = info.getURI();

		if (info.getURIType() == URIType.ANONYMOUS) {
			setCellValue(cell, "ANON");
		} else {
			if (info.getURIType() == URIType.ID) {
				uri = new ResourceImpl(baseURI + uri);
			}
			if (cellViewType == CellViewType.URI) {
				setNSPrefix(uri, cell);
			} else if (cellViewType == CellViewType.ID) {
				if (uri.getLocalName().length() != 0) {
					setCellValue(cell, uri.getLocalName());
				} else {
					setNSPrefix(uri, cell);
				}
			} else if (cellViewType == CellViewType.LABEL) {
				// リソースのEdge集合からrdf:labelを取り出して設定する
				String label = getRDFLabel((DefaultGraphCell) cell);
				if (label.length() != 0) {
					setCellValue(cell, label);
				} else {
					setNSPrefix(uri, cell);
				}
			}
			if (abstractLevelInfo.isSelectAbstractLevelMode()) {
				changeClassAbstractLevel(info, abstractLevelInfo.getClassAbstractLevelSet());
			}
		}
	}

	private void changeClassCellView() {
		Object[] classCells = classGraph.getAllCells();
		changeRDFSCellView(classCells);
		classGraph.getGraphLayoutCache().reload();
		classGraph.repaint();
	}

	private void changePropertyCellView() {
		Object[] propCells = propGraph.getAllCells();
		changeRDFSCellView(propCells);
		propGraph.getGraphLayoutCache().reload();
		propGraph.repaint();
	}

	private void changeRDFSCellView(Object[] cells) {
		for (int i = 0; i < cells.length; i++) {
			GraphCell cell = (GraphCell) cells[i];
			if (classGraph.isRDFSCell(cell)) {
				RDFSInfo info = rdfsInfoMap.getCellInfo(cell);
				Resource uri = info.getURI();
				if (cellViewType == CellViewType.URI) {
					setNSPrefix(uri, cell);
				} else if (cellViewType == CellViewType.ID) {
					if (uri.getLocalName().length() != 0) {
						setCellValue(cell, uri.getLocalName());
					} else { // LocalNameがなければ，URIを表示する
						setNSPrefix(uri, cell);
					}
				} else if (cellViewType == CellViewType.LABEL) {
					if (info.getLabel() != null) {
						setCellValue(cell, info.getLabel().getString());
					} else { // labelがnullだったら、URIを表示する
						setNSPrefix(uri, cell);
					}
				}
			}
		}
	}

	public void changeCellView() {
		changeClassCellView();
		changePropertyCellView();
		changeRDFCellView(); // ClassとPropertyを変換したあとで，RDFのType, Propertyを変換
	}

	private Set getSolitudeCells(RDFGraph graph) {
		Object[] cells = graph.getAllCells();
		GraphModel model = graph.getModel();
		Set region = new HashSet();
		for (int i = 0; i < cells.length; i++) {
			DefaultGraphCell cell = (DefaultGraphCell) cells[i];
			if (graph.isRDFSClassCell(cells[i])) {
				DefaultPort port = (DefaultPort) cell.getChildAt(0);
				if (port.getEdges().isEmpty()) {
					RDFSInfo info = rdfsInfoMap.getCellInfo(cell);
					region.add(info.getURI());
				}
			}
		}
		return region;
	}

	// domainとrangeのサブクラスも含めたSetを返す
	private Set getSubRegion(RDFGraph graph, Set set) {
		Set region = new HashSet(set);
		Object rdfsResourceCell = getClassCell(RDFS.Resource, false);

		if (region.isEmpty()) {
			region.add(rdfsResourceCell);
		}

		for (Iterator i = region.iterator(); i.hasNext();) {
			DefaultPort port = getDefaultPort(i.next());
			addSubRegion(graph, port, region);
		}
		// 孤立しているクラスのsubRegionを調べる必要はないので，subRegionのチェック後におく
		if (region.contains(rdfsResourceCell)) {
			region.addAll(getSolitudeCells(graph));
		}

		return region;
	}

	private DefaultPort getDefaultPort(Object cell) {
		return (DefaultPort) ((DefaultGraphCell) cell).getChildAt(0);
	}

	private void addSubRegion(RDFGraph graph, DefaultPort port, Set region) {
		GraphModel model = graph.getModel();
		Object cell = model.getParent(port);
		if (port == null) {
			return;
		}
		for (Iterator i = port.edges(); i.hasNext();) {
			Edge edge = (Edge) i.next();
			DefaultPort sourcePort = (DefaultPort) edge.getSource();
			Object sourceCell = model.getParent(sourcePort);

			if (sourceCell != cell) {
				region.add(sourceCell);
				addSubRegion(graph, sourcePort, region);
			}
		}
	}

	public List getPropertyList() {
		List list = new ArrayList();
		Object[] cells = propGraph.getAllCells();
		for (int i = 0; i < cells.length; i++) {
			if (propGraph.isRDFSPropertyCell(cells[i])) {
				list.add(cells[i]);
			}
		}
		return list;
	}

	public List getValidPropertyList(Object domainType, Object rangeType) {
		List list = new ArrayList();
		Object[] cells = propGraph.getAllCells();

		for (int i = 0; i < cells.length; i++) {
			Object cell = cells[i];
			if (propGraph.isRDFSPropertyCell(cell)) {
				PropertyInfo info = (PropertyInfo) rdfsInfoMap.getCellInfo(cell);

				Set domainSet = getSubRegion(classGraph, info.getDomain());
				Set rangeSet = getSubRegion(classGraph, info.getRange());
				if (domainSet.contains(domainType) && rangeSet.contains(rangeType)) {
					list.add(info.getURI());
				}
			}
		}
		return list;
	}

	public Object getClassCell(Resource uri, boolean isCheck) {
		Object cell = rdfsInfoMap.getClassCell(uri);
		if (cell != null) {
			return cell;
		} else {
			if (isCheck && isDuplicated(uri.getURI(), null, classGraph.getType())) {
				return null;
			} else {
				return cellMaker.insertClass(new Point(50, 50), uri.getURI());
			}
		}
	}

	public Object getPropertyCell(Resource uri, boolean isCheck) {
		Object cell = rdfsInfoMap.getPropertyCell(uri);
		if (cell != null) {
			return cell;
		} else {
			if (isCheck && isDuplicated(uri.getURI(), null, propGraph.getType())) {
				return null;
			} else {
				return cellMaker.insertProperty(new Point(50, 50), uri.getURI());
			}
		}
	}

	public void setSelectAbstractLevelMode(boolean t) {
		abstractLevelInfo.setSelectAbstractLevelMode(t);
		changeCellView();
	}

	public boolean isSelectAbstractLevelMode() {
		return abstractLevelInfo.isSelectAbstractLevelMode();
	}

	public void setClassAbstractLevelSet(Object[] cells) {
		abstractLevelInfo.setClassAbstractLevelSet(cells);
	}

	public void setPropertyAbstractLevelSet(Object[] cells) {
		abstractLevelInfo.setPropertyAbstractLevelSet(cells);
	}

	public void clearAbstractLevelSet() {
		abstractLevelInfo.clearAbstractLevelSet();
	}

	private boolean isMatches(String key, Resource uri) {
		return uri.getURI() != null && uri.getURI().matches(key);
	}

	public Set getClassInstanceSet(Object type) {
		Set instanceSet = new HashSet();
		Object[] cells = rdfGraph.getAllCells();
		for (int i = 0; i < cells.length; i++) {
			if (rdfGraph.isRDFResourceCell(cells[i])) {
				RDFResourceInfo info = resInfoMap.getCellInfo(cells[i]);
				if (info.getTypeCell() == type) {
					instanceSet.add(cells[i]);
				}
			}
		}
		return instanceSet;
	}

	public Set getPropertyInstanceSet(Object rdfsPropCell) {
		Set instanceSet = new HashSet();
		Object[] cells = rdfGraph.getAllCells();
		for (int i = 0; i < cells.length; i++) {
			if (rdfGraph.isRDFPropertyCell(cells[i])) {
				Object propCell = rdfsInfoMap.getEdgeInfo(cells[i]);
				if (propCell == rdfsPropCell) {
					Object sourceCell = rdfGraph.getSourceVertex(cells[i]);
					instanceSet.add(sourceCell);
				}
			}
		}
		return instanceSet;
	}

	public Set getSearchRDFResult(String key) {
		Set result = new HashSet();
		Object[] cells = rdfGraph.getAllCells();
		for (int i = 0; i < cells.length; i++) {
			if (rdfGraph.isRDFResourceCell(cells[i])) {
				RDFResourceInfo info = resInfoMap.getCellInfo(cells[i]);
				if (isMatches(key, info.getURI())) {
					result.add(cells[i]);
				}
			} else if (rdfGraph.isRDFPropertyCell(cells[i])) {
				Object propCell = rdfsInfoMap.getEdgeInfo(cells[i]);
				RDFSInfo info = rdfsInfoMap.getCellInfo(propCell);
				if (isMatches(key, info.getURI())) {
					result.add(cells[i]);
				}
			}
		}
		return result;
	}

	public Set getSearchRDFSResult(String key, RDFGraph graph) {
		Set result = new HashSet();
		Object[] cells = graph.getAllCells();

		for (int i = 0; i < cells.length; i++) {
			if (graph.isRDFSCell(cells[i])) {
				RDFSInfo info = rdfsInfoMap.getCellInfo(cells[i]);
				if (isMatches(key, info.getURI())) {
					result.add(cells[i]);
				}
			}
		}
		return result;
	}

	public Set getSearchClassResult(String key) {
		return getSearchRDFSResult(key, classGraph);
	}

	public Set getSearchPropertyResult(String key) {
		return getSearchRDFSResult(key, propGraph);
	}

	public void jumpArea(Object cell, JGraph graph) {
		graph.scrollCellToVisible(cell);
		if (graph == rdfGraph) {
			Object parent = graph.getModel().getParent(cell);
			graph.setSelectionCell(parent);
		} else {
			graph.setSelectionCell(cell);
		}
	}

	public void jumpRDFArea(Object cell) {
		jumpArea(cell, rdfGraph);
	}

	public void jumpClassArea(Object cell) {
		jumpArea(cell, classGraph);
	}

	public void jumpPropertyArea(Object cell) {
		jumpArea(cell, propGraph);
	}

	private Map checkNotRmablePropCells(Object[] cells, List notRmCells, Set notRmList, Object graph) {
		Map classPropMap = new HashMap();
		if (isPropertyGraph(graph)) {
			return classPropMap;
		}

		for (int i = 0; i < cells.length; i++) {
			Object cell = cells[i];
			Object[] propCells = propGraph.getAllCells();
			Set propSet = new HashSet();

			if (propGraph.isRDFSClassCell(cell)) { // 削除したクラスのセル				
				//　cellを参照していないかどうかすべてのプロパティに対して調べる
				// domainかrangeに含まれている可能性がある．
				for (int j = 0; j < propCells.length; j++) {
					Object propCell = propCells[j];
					if (propGraph.isRDFSPropertyCell(propCell)) {
						PropertyInfo info = (PropertyInfo) rdfsInfoMap.getCellInfo(propCell);
						Set domain = info.getDomain();
						Set range = info.getRange();
						if (domain.contains(cell) || range.contains(cell)) {
							propSet.add(propCell);
						}
					}
				}

				classPropMap.put(cell, propSet);
				if (!propSet.isEmpty()) {
					notRmCells.add(cell); // 削除することができないリストにcellを追加
					notRmList.add(cell); // JListでnullを表示しないようにするため
					List children = ((DefaultGraphCell) cell).getChildren();
					notRmCells.addAll(children);
				}
			}
		}

		return classPropMap;
	}

	private Map checkNotRmableRDFCells(Object[] cells, List notRmCells, Set notRmList) {
		Map classRDFMap = new HashMap();

		for (int i = 0; i < cells.length; i++) {
			Object cell = cells[i];
			Object[] rdfCells = rdfGraph.getAllCells();
			Set rdfSet = new HashSet();
			for (int j = 0; j < rdfCells.length; j++) {
				Object rdfCell = rdfCells[j];
				if (rdfGraph.isRDFResourceCell(rdfCell)) {
					RDFResourceInfo rdfInfo = resInfoMap.getCellInfo(rdfCell);
					if (rdfInfo.getTypeCell() == cell) {
						rdfSet.add(rdfCell);
					}
				} else if (rdfGraph.isEdge(rdfCell)) {
					Object propCell = rdfsInfoMap.getEdgeInfo(rdfCell);
					if (propCell == cell) {
						rdfSet.add(rdfCell);
					}
				}
			}

			classRDFMap.put(cell, rdfSet);
			if (!rdfSet.isEmpty()) {
				notRmCells.add(cell); // 削除することができないリストにcellを追加
				notRmList.add(cell); // JListでnullを表示しないようにするため
				List children = ((DefaultGraphCell) cell).getChildren();
				notRmCells.addAll(children);
			}
		}

		return classRDFMap;
	}

	public void setEnabled(boolean t) {
		rdfGraph.setEnabled(t);
		classGraph.setEnabled(t);
		propGraph.setEnabled(t);
	}

	private Object[] removeCells;
	private RDFGraph removeGraph;

	public void retryRemoveCells() {
		removeCells(removeCells, removeGraph);
	}

	public void removeAction(RDFGraph graph) {
		if (!graph.isSelectionEmpty()) {
			Object[] cells = graph.getSelectionCells();
			cells = graph.getDescendants(cells);
			removeCells(cells, graph);
		}
	}

	private void removeCells(Object[] cells, RDFGraph graph) {
		removeCells = cells;
		removeGraph = graph;

		if (isRDFGraph(graph)) {
			graph.removeCellsWithEdges(cells);
			return;
		}

		Set rmableCells = new HashSet();
		List notRmableCells = new ArrayList();
		Set notRmableResCells = new HashSet();

		Map rdfMap = checkNotRmableRDFCells(cells, notRmableCells, notRmableResCells);
		Map propMap = checkNotRmablePropCells(cells, notRmableCells, notRmableResCells, graph);
		
		if (notRmableCells.isEmpty()) {
			graph.removeCellsWithEdges(cells);
		} else {
			for (int i = 0; i < cells.length; i++) {
				if (!notRmableCells.contains(cells[i])) {
					rmableCells.add(cells[i]);
				}
			}
			graph.removeCellsWithEdges(rmableCells.toArray());
			refDialog.setRefListInfo(graph, notRmableResCells, rdfMap, propMap);
			refDialog.setVisible(true);
		}
	}

	public void setVisibleAttrDialog(boolean t) {
		attrDialog.setVisible(t);
	}

	private boolean isGraphEmpty() {
		return rdfGraph.getAllCells().length == 0 && classGraph.getAllCells().length == 0 && propGraph.getAllCells().length == 0;
	}

	public void removeAllCells() {
		rdfGraph.removeAllCells();
		classGraph.removeAllCells();
		propGraph.removeAllCells();
		if (!isGraphEmpty()) {
			System.out.println("error cell is not clear complete");
			removeAllCells();
		}
	}

	private Set initGraphLayoutData(RDFGraph graph, Map cellLayoutMap) {
		Object[] cells = graph.getAllCells();
		Set dataSet = new HashSet();
		for (int i = 0; i < cells.length; i++) {
			GraphCell cell = (GraphCell) cells[i];
			if (!graph.isTypeCell(cell) && (graph.isRDFSCell(cell) || graph.isRDFResourceCell(cell) || graph.isRDFLiteralCell(cell))) {
				GraphLayoutData data = new GraphLayoutData(cell, graph);
				cellLayoutMap.put(cell, data);
				dataSet.add(data);
			}
		}
		return dataSet;
	}

	private void addChild(JGraph graph, DefaultGraphCell cell, GraphLayoutData data, Map map) {
		Port port = (Port) cell.getChildAt(0);

		for (Iterator i = port.edges(); i.hasNext();) {
			Edge edge = (Edge) i.next();
			GraphCell sourceCell = (GraphCell) rdfGraph.getSourceVertex(edge);
			GraphCell targetCell = (GraphCell) rdfGraph.getTargetVertex(edge);

			if (isClassGraph(graph) || isPropertyGraph(graph)) {
				if (sourceCell != cell) { // 自身がTargetの場合
					data.addChild(map.get(sourceCell));
				}
				// 自身がソースになる場合(ただし，自己参照は除く）
				if (sourceCell == cell && sourceCell != targetCell) {
					data.setHasParent(true);
				}
			} else {
				if (targetCell != cell) { // 自身がTargetの場合
					data.addChild(map.get(targetCell));
				}
				if (targetCell == cell && targetCell != sourceCell) {
					data.setHasParent(true);
				}
			}
		}
	}

	private Object collectRoot(Set rootCells, Set dataSet, Map cellLayoutMap) {
		DefaultGraphCell rootCell = new RDFResourceCell("");
		DefaultPort rootPort = new DefaultPort();
		rootCell.add(rootPort);

		Map attributes = new HashMap();
		attributes.put(rootCell, cellMaker.getResourceMap(new Point(50, 50)));
		resInfoMap.putCellInfo(rootCell, new RDFResourceInfo(URIType.ANONYMOUS, "", null));
		rdfGraph.getModel().insert(new Object[] { rootCell }, attributes, null, null, null);
		GraphLayoutData rootData = new GraphLayoutData(rootCell, rdfGraph);
		rootData.setHasParent(false);

		for (Iterator i = rootCells.iterator(); i.hasNext();) {
			DefaultGraphCell cell = (DefaultGraphCell) i.next();
			Port port = (Port) cell.getChildAt(0);
			DefaultEdge edge = new DefaultEdge("");
			ConnectionSet cs = new ConnectionSet(edge, rootPort, port);
			rdfGraph.getModel().insert(new Object[] { edge }, null, cs, null, null);
			GraphLayoutData data = (GraphLayoutData) cellLayoutMap.get(cell);
			data.setHasParent(true);
			rootData.addChild(data);
		}
		dataSet.add(rootData);

		return rootCell;
	}

	private void removeTemporaryRoot(DefaultGraphCell tmpRoot) {
		if (tmpRoot == null) {
			return;
		}
		Set removeCells = new HashSet();
		Port port = (Port) tmpRoot.getChildAt(0);
		removeCells.add(tmpRoot);
		removeCells.add(port);
		for (Iterator edges = rdfGraph.getModel().edges(port); edges.hasNext();) {
			removeCells.add(edges.next());
		}
		rdfGraph.getModel().remove(removeCells.toArray());
	}

	public void applyTreeLayout() {
		applyTreeLayout(rdfGraph, 'r');
		applyTreeLayout(classGraph, 'u');
		applyTreeLayout(propGraph, 'u');
	}

	public void applyTreeLayout(RDFGraph graph, char arc) {
		Map cellLayoutMap = new HashMap();
		Set dataSet = initGraphLayoutData(graph, cellLayoutMap);
		Set rootCells = new HashSet();
		for (Iterator i = dataSet.iterator(); i.hasNext();) {
			GraphLayoutData data = (GraphLayoutData) i.next();
			DefaultGraphCell cell = (DefaultGraphCell) data.getCell();
			addChild(graph, cell, data, cellLayoutMap);
			if (!data.hasParent()) {
				rootCells.add(cell);
			}
		}

		Object tmpRoot = null;
		if (rootCells.size() != 1) {
			tmpRoot = collectRoot(rootCells, dataSet, cellLayoutMap);
		}

		TreeAlgorithm treeAlgorithm = new TreeAlgorithm(arc);
		treeAlgorithm.applyTreeAlgorithm(dataSet, null);
		dataSet.remove(cellLayoutMap.get(tmpRoot));
		removeTemporaryRoot((DefaultGraphCell) tmpRoot);

		for (Iterator i = dataSet.iterator(); i.hasNext();) {
			GraphLayoutData data = (GraphLayoutData) i.next();
			data.setRealResourcePosition();
			if (isRDFGraph(graph) && arc == 'r' && graph.isRDFResourceCell(data.getCell())) {
				RDFResourceInfo info = resInfoMap.getCellInfo(data.getCell());
				GraphCell typeCell = (GraphCell) info.getTypeViewCell();
				//		System.out.print(typeCell.getClass());
				//		System.out.println(typeCell);
				if (typeCell != null)
					data.setRealTypePosition(typeCell);
			}
		}
		changeCellView();
	}

	class AbstractLevelInfo {
		private boolean isSelectAbstractLevelMode;
		private Set classAbstractLevelSet;
		private Set propAbstractLevelSet;

		AbstractLevelInfo() {
			classAbstractLevelSet = new HashSet();
			propAbstractLevelSet = new HashSet();
		}

		public boolean isSelectAbstractLevelMode() {
			return isSelectAbstractLevelMode;
		}

		private Set getSelectedCellSet(Object[] cells) {
			Set selectedCellSet = new HashSet();
			for (int i = 0; i < cells.length; i++) {
				if (rdfGraph.isRDFResourceCell(cells[i])) {
					selectedCellSet.add(cells[i]);
				}
			}
			return selectedCellSet;
		}

		public void setClassAbstractLevelSet(Object[] cells) {
			classAbstractLevelSet = getSelectedCellSet(cells);
		}

		public void setPropertyAbstractLevelSet(Object[] cells) {
			propAbstractLevelSet = getSelectedCellSet(cells);
		}

		public Set getClassAbstractLevelSet() {
			return Collections.unmodifiableSet(classAbstractLevelSet);
		}

		public Set getPropertyAbstractLevelSet() {
			return Collections.unmodifiableSet(propAbstractLevelSet);
		}

		public void clearAbstractLevelSet() {
			classAbstractLevelSet.clear();
			propAbstractLevelSet.clear();
		}

		public void setSelectAbstractLevelMode(boolean t) {
			isSelectAbstractLevelMode = t;
			if (t) {
				attrDialog.setVisible(false);
			} else {
				clearAbstractLevelSet();
			}
		}
	}
}
