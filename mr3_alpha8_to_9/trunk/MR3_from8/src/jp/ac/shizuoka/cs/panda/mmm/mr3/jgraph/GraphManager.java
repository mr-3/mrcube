package jp.ac.shizuoka.cs.panda.mmm.mr3.jgraph;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.prefs.*;

import javax.swing.*;

import jp.ac.shizuoka.cs.panda.mmm.mr3.data.*;
import jp.ac.shizuoka.cs.panda.mmm.mr3.layout.*;
import jp.ac.shizuoka.cs.panda.mmm.mr3.ui.*;
import jp.ac.shizuoka.cs.panda.mmm.mr3.util.*;

import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.rdf.model.impl.*;
import com.hp.hpl.jena.vocabulary.*;
import com.jgraph.*;
import com.jgraph.graph.*;

public class GraphManager {

	private JFrame root;

	private RDFGraph rdfGraph;
	private RDFGraph realRDFGraph;
	private RDFGraph classGraph;
	private RDFGraph propGraph;

	private boolean isImporting;
	private boolean isShowTypeCell;

	private RDFCellMaker cellMaker;

	private RDFResourceInfoMap resInfoMap = RDFResourceInfoMap.getInstance();
	private RDFLiteralInfoMap litInfoMap = RDFLiteralInfoMap.getInstance();
	private RDFSInfoMap rdfsInfoMap = RDFSInfoMap.getInstance();

	private AttributeDialog attrDialog;
	private RemoveDialog refDialog;
	private Set prefixNSInfoSet;
	private CellViewType cellViewType;
	private AbstractLevelInfo abstractLevelInfo;

	private Preferences userPrefs;
	private String baseURI;

	public GraphManager(AttributeDialog attrD, Preferences prefs) {
		attrDialog = attrD;
		rdfGraph = new RDFGraph(this, GraphType.RDF);
		classGraph = new RDFGraph(this, GraphType.CLASS);
		propGraph = new RDFGraph(this, GraphType.PROPERTY);
		registerComponent();
		cellMaker = new RDFCellMaker(this);

		userPrefs = prefs;
		refDialog = new RemoveDialog("Remove Dialog", this);
		abstractLevelInfo = new AbstractLevelInfo();
		prefixNSInfoSet = new HashSet();
		baseURI = userPrefs.get(PrefConstants.BaseURI, MR3Resource.getURI());
	}

	private JDesktopPane desktop;

	public void setDesktop(JDesktopPane jdp) {
		desktop = jdp;
	}

	public JDesktopPane getDesktop() {
		return desktop;
	}

	public void setRoot(JFrame mr3) {
		root = mr3;
	}

	public JFrame getRoot() {
		return root;
	}

	public boolean isImporting() {
		return isImporting;
	}

	public void setIsImporting(boolean t) {
		isImporting = t;
	}

	public boolean isShowTypeCell() {
		return isShowTypeCell;
	}

	public void setIsShowTypeCell(boolean t) {
		isShowTypeCell = t;
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

	public void clearSelection() {
		rdfGraph.clearSelection();
		classGraph.clearSelection();
		propGraph.clearSelection();
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

	public void setGraphBackground(Color color) {
		rdfGraph.setBackground(color);
		classGraph.setBackground(color);
		propGraph.setBackground(color);
	}

	private Set getMetaClassList(String[] list) {
		Set metaClassList = new HashSet();
		for (int i = 0; i < list.length; i++) {
			metaClassList.add(new ResourceImpl(list[i]));
		}
		return metaClassList;
	}

	public Set getClassClassList() {
		return getMetaClassList(userPrefs.get(PrefConstants.ClassClassList, RDFS.Class.toString()).split(" "));
	}

	public Set getPropertyClassList() {
		return getMetaClassList(userPrefs.get(PrefConstants.PropClassList, RDF.Property.toString()).split(" "));
	}

	public Set getMetaClassList() {
		Set metaClassList = getClassClassList();
		metaClassList.addAll(getPropertyClassList());
		return metaClassList;
	}

	public void findMetaClass(Model orgModel, Resource supClass, Set set) {
		try {
			for (ResIterator i = orgModel.listSubjectsWithProperty(RDFS.subClassOf, supClass); i.hasNext();) {
				Resource subject = i.nextResource();
				set.add(subject);
				findMetaClass(orgModel, subject, set);
			}
		} catch (RDFException e) {
			e.printStackTrace();
		}
	}

	public void setClassClassList(Set classClassList) {
		String classClassListStr = "";
		for (Iterator i = classClassList.iterator(); i.hasNext();) {
			classClassListStr += i.next().toString() + " ";
		}
		userPrefs.put(PrefConstants.ClassClassList, classClassListStr);
	}

	public void setPropertyClassList(Set propClassList) {
		String propClassListStr = "";
		for (Iterator i = propClassList.iterator(); i.hasNext();) {
			propClassListStr += i.next().toString() + " ";
		}
		userPrefs.put(PrefConstants.PropClassList, propClassListStr);
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

	public void addTypeCells() {
		Object[] rdfCells = rdfGraph.getAllCells();
		RDFCellMaker cellMaker = new RDFCellMaker(this);
		for (int i = 0; i < rdfCells.length; i++) {
			if (rdfGraph.isRDFResourceCell(rdfCells[i])) {
				DefaultGraphCell cell = (DefaultGraphCell) rdfCells[i];
				RDFResourceInfo info = resInfoMap.getCellInfo(cell);
				Map map = cell.getAttributes();
				Rectangle rec = GraphConstants.getBounds(map);
				GraphCell typeCell = cellMaker.addTypeCell(cell, new HashMap(), rec);
				info.setTypeViewCell(typeCell);
			}
		}
		changeCellView();
	}

	public void removeTypeCells() {
		Object[] rdfCells = rdfGraph.getAllCells();
		List typeCellList = new ArrayList();
		for (int i = 0; i < rdfCells.length; i++) {
			GraphCell cell = (GraphCell) rdfCells[i];
			if (rdfGraph.isTypeCell(cell)) {
				typeCellList.add(cell);
			} //else if (cell.getClass().equals(DefaultGraphCell.class)) {
			//	System.out.println(cell.getClass());
			//	typeCellList.add(cell);
			//}
		}
		rdfGraph.removeCellsWithEdges(typeCellList.toArray());
	}

	private boolean isRDFResourceDuplicated(String uri, Object cell, GraphType type) {
		Collection entrySet = resInfoMap.entrySet();
		for (Iterator i = entrySet.iterator(); i.hasNext();) {
			Map.Entry entry = (Map.Entry) i.next();
			Object infoCell = entry.getKey();
			RDFResourceInfo resInfo = (RDFResourceInfo) entry.getValue();
			String tmpURI = resInfo.getURIStr();

			if (tmpURI.equals(uri) && infoCell != cell) {
				RDFSInfo rdfsInfo = rdfsInfoMap.getCellInfo(resInfo.getTypeCell());
				/*
				 *  RDFエディタ内のクラス定義は，重複とみなさないようにする．
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
			JOptionPane.showInternalMessageDialog(getDesktop(), "URI is empty", "Warning", JOptionPane.ERROR_MESSAGE);
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
		return (rdfsInfoMap.isDuplicated(uri, cell, type, baseURI) || isRDFResourceDuplicated(uri, cell, type));
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

	public Set getAllNameSpaceSet() {
		Set allNSSet = new HashSet();
		allNSSet.addAll(getRDFNameSpaceSet());
		allNSSet.addAll(getClassNameSpaceSet());
		allNSSet.addAll(getPropertyNameSpaceSet());

		return allNSSet;
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

	public void setNodeBounds(Map uriNodeInfoMap) {
		for (Iterator i = uriNodeInfoMap.keySet().iterator(); i.hasNext();) {
			Resource uri = (Resource) i.next();
			MR3Literal rec = (MR3Literal) uriNodeInfoMap.get(uri);

			GraphCell cell = (GraphCell) getRDFResourceCell(uri);
			if (cell != null) {
				setCellBounds(rdfGraph, cell, rec.getRectangle());
			} else if (rdfsInfoMap.isClassCell(uri)) {
				cell = (GraphCell) getClassCell(uri, false);
				setCellBounds(classGraph, cell, rec.getRectangle());
			} else if (rdfsInfoMap.isPropertyCell(uri)) {
				cell = (GraphCell) getPropertyCell(uri, false);
				setCellBounds(propGraph, cell, rec.getRectangle());
			} else if (uri.getURI().matches(MR3Resource.Literal.getURI() + ".*")) {
				DefaultGraphCell litCell = (DefaultGraphCell) cellMaker.insertRDFLiteral(rec.getLocation());
				litInfoMap.putCellInfo(litCell, RDFLiteralUtil.createLiteral(rec.getString(), rec.getLanguage(), rec.getDatatype()));
				setCellBounds(rdfGraph, litCell, rec.getRectangle());
				setCellValue(litCell, rec.getString());
				DefaultGraphCell source = (DefaultGraphCell) getRDFResourceCell(rec.getResource());
				Edge edge = cellMaker.connect((Port) source.getChildAt(0), (Port) litCell.getChildAt(0), "", rdfGraph);
				rdfsInfoMap.putEdgeInfo(edge, rdfsInfoMap.getPropertyCell(rec.getProperty()));
			}
		}
	}

	private void setCellBounds(RDFGraph graph, GraphCell cell, Rectangle rec) {
		Map map = cell.getAttributes();
		GraphConstants.setBounds(map, rec);
		editCell(cell, map, graph);
	}

	private void editCell(GraphCell cell, Map map, RDFGraph graph) {
		Map nested = new HashMap();
		nested.put(cell, GraphConstants.cloneMap(map));
		graph.getModel().edit(nested, null, null, null);
	}

	public void setCellValue(GraphCell cell, String value) {
		Map map = cell.getAttributes();
		GraphConstants.setValue(map, value);
		//			cell.setAttributes(map);
		//		set->changeにしないと，cellのValueが変更されず，Cellを削除した時に表示が変化しない
		cell.changeAttributes(map);
	}

	private void setNSPrefix(Resource uri, GraphCell cell) {
		setCellValue(cell, uri.toString());
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
				if (cellViewType == CellViewType.URI && !uri.isAnon()) {
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
		if (!isShowTypeCell) {
			removeTypeCells();
		}
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

	public Object getRDFResourceCell(Resource uri) {
		Object[] cells = rdfGraph.getAllCells();
		for (int i = 0; i < cells.length; i++) {
			if (rdfGraph.isRDFResourceCell(cells[i])) {
				RDFResourceInfo info = resInfoMap.getCellInfo(cells[i]);
				/*
				 * Resourceクラスのインスタンスをequalsで比べるとisAnon()
				 * によって，true,falseを決められる．文字列を保存しているuri
				 * は，isAnonは必ずfalseとなるため，文字列比較をするために
				 * toStringを用いている．toStringを消すな．
				 */
				if (info.getURI().toString().equals(uri.toString())) {
					return cells[i];
				}
			}
		}
		return null;
	}

	public Object getRDFPropertyCell(Resource uri) {
		Object[] cells = rdfGraph.getAllCells();
		for (int i = 0; i < cells.length; i++) {
			if (rdfGraph.isRDFPropertyCell(cells[i])) {
				Object propCell = rdfsInfoMap.getEdgeInfo(cells[i]);
				RDFSInfo info = rdfsInfoMap.getCellInfo(propCell);
				if (info == null) {
					return null;
				}
				if (info.getURI().equals(uri)) {
					return cells[i];
				}
			}
		}
		return null;
	}

	public Object getClassCell(Resource uri, boolean isCheck) {
		Object cell = rdfsInfoMap.getClassCell(uri);
		if (cell != null) {
			return cell;
		} else if (isCheck && isDuplicated(uri.getURI(), null, classGraph.getType())) {
			return null;
		} else {
			return cellMaker.insertClass(new Point(50, 50), uri.getURI());
		}
	}

	public Set getSupRDFS(RDFGraph graph, String title) {
		if (graph.getAllCells().length == 0) {
			return new HashSet();
		}
		SelectRDFSDialog selectSupRDFSDialog = new SelectRDFSDialog(title, this);
		selectSupRDFSDialog.replaceGraph(graph);
		selectSupRDFSDialog.setVisible(true);
		selectSupRDFSDialog.setRegionSet(new HashSet());
		return (Set) selectSupRDFSDialog.getValue();
	}

	public Object insertSubRDFS(Resource uri, Set supRDFS, RDFGraph graph) {
		Point point = cellMaker.calcInsertPoint(supRDFS);
		DefaultGraphCell cell = null;
		if (isClassGraph(graph)) {
			cell = (DefaultGraphCell) cellMaker.insertClass(point, uri.getURI());
		} else if (isPropertyGraph(graph)) {
			cell = (DefaultGraphCell) cellMaker.insertProperty(point, uri.getURI());
		}
		Port sourcePort = (Port) cell.getChildAt(0);
		Object[] supCells = graph.getDescendants(supRDFS.toArray());
		cellMaker.connectSubToSups(sourcePort, supCells, graph);
		return cell;
	}

	public Object getPropertyCell(Resource uri, boolean isCheck) {
		Object cell = rdfsInfoMap.getPropertyCell(uri);
		if (cell != null) {
			return cell;
		} else if (isCheck && isDuplicated(uri.getURI(), null, propGraph.getType())) {
			return null;
		} else {
			return cellMaker.insertProperty(new Point(50, 50), uri.getURI());
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
		// TreeSet を使うためにはcompratorを適切に実装しないといけない．
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

	public Set getFindRDFResult(String key) {
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

	public Set getFindRDFSResult(String key, RDFGraph graph) {
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

	public Set getFindClassResult(String key) {
		return getFindRDFSResult(key, classGraph);
	}

	public Set getFindPropertyResult(String key) {
		return getFindRDFSResult(key, propGraph);
	}

	public void jumpArea(Object cell, JGraph graph) {
		if (!graph.getModel().contains(cell)) {
			return;
		}
		graph.scrollCellToVisible(cell);
		if (graph == rdfGraph) {
			Object parent = graph.getModel().getParent(cell);
			if (parent == null) {
				graph.setSelectionCell(cell);
			} else {
				graph.setSelectionCell(parent);
			}
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

	public AttributeDialog getAttrDialog() {
		return attrDialog;
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

			if (targetCell != cell) { // 自身がTargetの場合
				data.addChild(map.get(targetCell));
			}
			if (targetCell == cell && targetCell != sourceCell) {
				data.setHasParent(true);
			}
		}
	}

	private Object collectRoot(Set rootCells, Set dataSet, Map cellLayoutMap) {
		DefaultGraphCell rootCell = new RDFResourceCell("");
		DefaultPort rootPort = new DefaultPort();
		rootCell.add(rootPort);

		Map attributes = new HashMap();
		attributes.put(rootCell, cellMaker.getResourceMap(new Point(50, 50), ChangeCellAttributes.rdfResourceColor));
		resInfoMap.putCellInfo(rootCell, new RDFResourceInfo(URIType.ANONYMOUS, new AnonId().toString(), null));
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
		removeTypeCells();
		//		applySugiyamaLayout(rdfGraph, new Point(200, 200));
		applyTreeLayout(rdfGraph, 'r');
		//		applyTreeLayout(rdfGraph, TreeLayoutAlgorithm.LEFT_TO_RIGHT, 200, 20);
		addTypeCells();
		//		applySugiyamaLayout(classGraph, new Point(200, 200));
		applyTreeLayout(classGraph, 'u');
		//		applyTreeLayout(classGraph, TreeLayoutAlgorithm.UP_TO_DOWN, 30, 50);
		//		applySugiyamaLayout(propGraph, new Point(200, 200));
		applyTreeLayout(propGraph, 'u');
		//		applyTreeLayout(propGraph, TreeLayoutAlgorithm.UP_TO_DOWN, 30, 50);
		changeCellView();
		clearSelection();
	}

	public void applySugiyamaLayout(RDFGraph graph, Point space) {
		SugiyamaLayoutAlgorithm sugiyamaLayout = new SugiyamaLayoutAlgorithm();
		sugiyamaLayout.perform(graph, true, space);
		centerCellsInGraph(graph);
	}

	public void applyTreeLayout(RDFGraph graph, int orientation, int distance, int border) {
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
		TreeLayoutAlgorithm treeLayout = new TreeLayoutAlgorithm(orientation, distance, border);
		if (tmpRoot != null) {
			treeLayout.perform(graph, new Object[] { tmpRoot });
		} else {
			treeLayout.perform(graph, rootCells.toArray());
		}
		dataSet.remove(cellLayoutMap.get(tmpRoot));
		removeTemporaryRoot((DefaultGraphCell) tmpRoot);

		centerCellsInGraph(graph);
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
		}
		centerCellsInGraph(graph);
	}

	private void centerCellsInGraph(RDFGraph graph) {
		int margine = 50;
		Object[] cells = graph.getAllCells();
		if (cells.length == 0) {
			return;
		}
		Rectangle rec = graph.getCellBounds(cells);

		int reviseX = 0;
		int reviseY = 0;
		if (rec.x <= 0) {
			reviseX = (-rec.x) + margine;
		} else if (margine < rec.x) {
			reviseX = margine - rec.x;
		}
		if (rec.y <= 0) {
			reviseY = (-rec.y) + margine;
		} else if (margine < rec.y) {
			reviseY = margine - rec.y;
		}

		for (int i = 0; i < cells.length; i++) {
			if (graph.isRDFsCell(cells[i]) || graph.isTypeCell(cells[i])) {
				GraphCell cell = (GraphCell) cells[i];
				Map map = cell.getAttributes();
				Rectangle cellRec = GraphConstants.getBounds(map);
				cellRec.x += reviseX;
				cellRec.y += reviseY;
				GraphConstants.setBounds(map, cellRec);
				editCell(cell, map, graph);
			}
		}
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
