package mr3.jgraph;
import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.util.List;

import javax.swing.*;

import mr3.data.*;
import mr3.ui.*;
import mr3.util.*;

import com.hp.hpl.mesa.rdf.jena.model.*;
import com.jgraph.*;
import com.jgraph.event.*;
import com.jgraph.graph.*;
import com.jgraph.plaf.basic.*;

public class RDFGraph extends JGraph {

	private GraphType type;
	private GraphManager gmanager;
	private AttributeDialog attrDialog;
	private RDFResourceInfoMap resInfoMap = RDFResourceInfoMap.getInstance();
	private RDFLiteralInfoMap litInfoMap = RDFLiteralInfoMap.getInstance();
	private RDFSInfoMap rdfsInfoMap = RDFSInfoMap.getInstance();

	private GraphCopyBuffer copyBuffer;

	public RDFGraph(GraphManager manager, AttributeDialog attrD, GraphType type) {
		super(new RDFGraphModel());
		//		GraphLayoutCache cache = new GraphLayoutCache(getModel(), getGraphLayoutCache().getFactory(), false, true);
		//		setGraphLayoutCache(cache);
		initStatus();
		gmanager = manager;
		attrDialog = attrD;
		this.type = type;
	}

	public RDFGraph() {
		super(new RDFGraphModel());
		initStatus();
	}

	public GraphType getType() {
		return type;
	}

	private static final Color GRAPH_BACK_COLOR = new Color(245, 245, 245);

	private void initStatus() {
		setSelectNewCells(true); // Tell the Graph to Select new Cells upon Insertion
		setGridEnabled(true); // Use the Grid (but don't make it Visible)
		setGridSize(6);
		setTolerance(10);
		//		setMarqueeColor(Color.gray);
		setHandleColor(Color.gray); // セルの点の周りの色 
		setLockedHandleColor(Color.gray); // セルの周りの点々の色
		setHighlightColor(Color.orange); // 選択されている色 				
		setBackground(GRAPH_BACK_COLOR);
		setAntiAliased(true);
		selectionModel.setChildrenSelectable(false);
		graphModel.addGraphModelListener(new ModelListener());
	}

	public void startEditingAtCell(Object cell) {
		if (attrDialog != null) {
			attrDialog.setVisible(true);
		}
	}

	protected VertexView createVertexView(Object v, CellMapper cm) {
		if (v instanceof RDFResourceCell || v instanceof RDFSPropertyCell)
			return new EllipseView(v, this, cm);
		return super.createVertexView(v, cm);
	}

	//	protected EdgeView createEdgeView(Edge e, CellMapper cm) { // 2.1系ではduplicated
	protected EdgeView createEdgeView(Object e, CellMapper cm) {

		return new EdgeView(e, this, cm) {

			public boolean isAddPointEvent(MouseEvent event) {
				return event.isShiftDown(); // Points are Added using Shift-Click
			}

			public boolean isRemovePointEvent(MouseEvent event) {
				return event.isShiftDown(); // Points are Removed using Shift-Click
			}
		};
	}

	// modelが変更された時に呼ばれる
	class ModelListener implements GraphModelListener {
		public void graphChanged(GraphModelEvent e) {
			//System.out.println("Changed: "+e.getChange());
		}
	}

	// 状態を保存
	public Serializable getRDFState() {
		Object[] cells = getGraphLayoutCache().order(getAllCells());
		Map viewAttributes = GraphConstants.createAttributes(cells, getGraphLayoutCache());
		ArrayList list = new ArrayList();
		list.add(cells);
		list.add(viewAttributes);
		return list;
	}

	//状態を復元
	public void setRDFState(Object s) {
		if (s instanceof ArrayList) {
			ArrayList list = (ArrayList) s;
			Object[] cells = (Object[]) list.get(0);
			Map attrib = (Map) list.get(1);
			getModel().insert(cells, attrib, null, null, null);
			clearSelection();
		}
	}

	public boolean isContains(Object cell) {
		Object[] cells = getAllCells();
		for (int i = 0; i < cells.length; i++) {
			if (cells[i] == cell) {
				return true;
			}
		}
		return false;
	}

	public boolean isOneCellSelected(Object cell) {
		return (getSelectionCount() == 1 && graphModel.getChildCount(cell) <= 1);
	}

	// 以下のメソッドで，グループ化したRDFリソースを選択したときに，AttributeDialogに
	//　RDFリソースの情報を表示できると考えたが，うまくいかなかった．
	public GraphCell isOneRDFCellSelected(Object[] cells) {
		int count = 0;
		GraphCell rdfCell = null;
		for (int i = 0; i < cells.length; i++) {
			if (isRDFCell(cells[i])) {
				count++;
				rdfCell = (GraphCell) cells[i];
			}
		}

		if (count == 1) {
			return rdfCell;
		} else {
			return null;
		}
	}

	public boolean isEdge(Object object) {
		return (object instanceof Edge);
	}

	public boolean isPort(Object object) {
		return (object instanceof Port);
	}

	public boolean isRDFsCell(Object object) {
		return (isRDFCell(object) || isRDFSCell(object));
	}

	public boolean isRDFCell(Object object) {
		return (isRDFResourceCell(object) || isRDFPropertyCell(object) || isRDFLiteralCell(object));
	}

	public boolean isRDFSCell(Object object) {
		return (object instanceof RDFSClassCell || object instanceof RDFSPropertyCell);
	}

	public boolean isRDFResourceCell(Object object) {
		return (object instanceof RDFResourceCell);
	}

	public boolean isRDFPropertyCell(Object object) {
		return isEdge(object);
	}

	public boolean isRDFLiteralCell(Object object) {
		return (object instanceof RDFLiteralCell);
	}

	public boolean isRDFSClassCell(Object object) {
		return (object instanceof RDFSClassCell);
	}

	public boolean isRDFSPropertyCell(Object object) {
		return (object instanceof RDFSPropertyCell);
	}

	public boolean isTypeCell(Object object) {
		return (object instanceof TypeCell);
	}

	public Object[] getAllCells() {
		return getDescendants(getRoots());
	}

	public void selectAllNodes() {
		clearSelection();
		addSelectionCells(getRoots()); // Descendantsまでやるとばらばら．
	}

	public Object getSourceVertex(Object edge) {
		Object sourcePort = graphModel.getSource(edge);
		return graphModel.getParent(sourcePort);
	}

	public Object getTargetVertex(Object edge) {
		Object targetPort = graphModel.getTarget(edge);
		return graphModel.getParent(targetPort);
	}

	/** cellに接続されているエッジのtargetとなるcellのSetを返す */
	public Set getTargetCells(DefaultGraphCell cell) {
		Object port = cell.getChildAt(0);

		Set supCells = new HashSet();
		for (Iterator edges = graphModel.edges(port); edges.hasNext();) {
			Edge edge = (Edge) edges.next();
			Object target = getTargetVertex(edge);
			if (target != cell) {
				supCells.add(target);
			}
		}
		return supCells;
	}

	public void removeAllCells() {
		graphLayoutCache.remove(getAllCells());
	}

	public void removeEdges() {
		Object[] cells = getAllCells();
		Set removeCells = new HashSet();
		for (int i = 0; i < cells.length; i++) {
			if (isEdge(cells[i])) {
				removeCells.add(cells[i]);
			}
		}
		graphModel.remove(removeCells.toArray());
	}

	// 選択されたCellに接続されているEdgeを削除
	public void removeCellsWithEdges(Object[] cells) {
		Set removeCells = new HashSet();
		for (int i = 0; i < cells.length; i++) {
			if (isPort(cells[i])) {
				Port port = (Port) cells[i];
				for (Iterator edges = graphModel.edges(port); edges.hasNext();) {
					removeCells.add(edges.next());
				}
				//			} else if (isRDFResourceCell(cells[i])) {
			} else if (isRDFResourceCell(cells[i]) || isRDFSCell(cells[i])) {
				rdfsInfoMap.removeCellInfo(cells[i]);
				resInfoMap.removeCellInfo(cells[i]);
			}
		}
		graphLayoutCache.remove(removeCells.toArray());
		graphLayoutCache.remove(cells);
	}

	private static final int COMMENT_WIDTH = 40;

	private String getRDFSToolTipText(RDFSInfo info) {
		String msg = "<dl><dt>URI: </dt><dd>" + info.getURI() + "</dd>";
		try {
			Literal literal = info.getLabel();
			if (literal != null) {
				msg += "<dt>Label</dt><dd>Lang: " + literal.getLanguage() + "<br>" + literal.getString() + "</dd>";
				msg += "<dt>Comment</dt>";
			}
			literal = info.getComment();
			if (literal != null) {
				String comment = literal.getString();
				comment = RDFLiteralUtil.insertLineFeed(comment, COMMENT_WIDTH);
				comment = comment.replaceAll("(\n|\r)+", "<br>");
				msg += "<dd>" + "Lang: " + literal.getLanguage() + "<br>" + comment + "</dd></dt>";
			}
		} catch (RDFException e) {
			e.printStackTrace();
		}
		return msg;
	}

	private String getClassToolTipText(Object cell) {
		ClassInfo info = (ClassInfo) rdfsInfoMap.getCellInfo(cell);
		String msg = "<center><strong>Class</strong></center>";
		msg += getRDFSToolTipText(info);
		msg += "<strong>SuperClasses: </strong>" + info.getSupRDFS() + "<br>";
		return msg;
	}

	private String getPropertyToolTipText(Object cell) {
		PropertyInfo info = (PropertyInfo) rdfsInfoMap.getCellInfo(cell);
		String msg = "<center><strong>Property</strong></center>";
		msg += getRDFSToolTipText(info);
		msg += "<strong>SuperProperties: </strong>" + info.getSupRDFS() + "<br>";
		return msg;
	}

	private String getRDFResourceToolTipText(Object cell) {
		String msg = "";
		RDFResourceInfo info = resInfoMap.getCellInfo(cell);
		msg += "<h3>Resource</h3>";
		if (info.getURIType() == URIType.ID) {
			msg += "<strong>URI: </strong>" + gmanager.getBaseURI() + info.getURI() + "<br>";
		} else {
			msg += "<strong>URI: </strong>" + info.getURI() + "<br>";
		}
		msg += "<strong>Type: </strong>" + info.getType() + "<br>";
		return msg;
	}

	private String getRDFPropertyToolTipText(Object cell) {
		String msg = "";
		Object propCell = rdfsInfoMap.getEdgeInfo(cell);
		return getPropertyToolTipText(propCell);
	}

	private String getRDFLiteralToolTipText(Object cell) {
		String msg = "<h3>Literal</h3>";
		Literal literal = litInfoMap.getCellInfo(cell);
		msg += "<strong>Lang: </strong>" + literal.getLanguage() + "<br>";
		try {
			msg += RDFLiteralUtil.insertLineFeed(literal.getString(), COMMENT_WIDTH);
		} catch (RDFException e) {
			e.printStackTrace();
		}
		msg = msg.replaceAll("(\n|\r)+", "<br>");
		return msg;
	}

	public JToolTip createToolTip() {
		return new GraphToolTip();
	}

	private static final Color TOOLTIP_BACK_COLOR = new Color(225, 225, 225);

	class GraphToolTip extends JToolTip {
		public void paint(Graphics g) {
			setBackground(TOOLTIP_BACK_COLOR);
			super.paint(g);
		}
	}

	public String getToolTipText(MouseEvent event) {
		if (event != null) {
			Object cell = getFirstCellForLocation(event.getX(), event.getY());
			if (cell != null) {
				String msg = "";
				if (type == GraphType.RDF) {
					if (isRDFLiteralCell(cell)) {
						msg = getRDFLiteralToolTipText(cell);
					} else if (isRDFResourceCell(cell)) {
						msg = getRDFResourceToolTipText(cell);
					} else if (isRDFPropertyCell(cell)) {
						msg = getRDFPropertyToolTipText(cell);
					} else {
						List children = ((DefaultGraphCell) cell).getChildren();
						for (Iterator i = children.iterator(); i.hasNext();) {
							Object resCell = i.next();
							if (isRDFResourceCell(resCell)) {
								msg = getRDFResourceToolTipText(resCell);
							}
						}
					}
				} else if (type == GraphType.CLASS) {
					if (isRDFSClassCell(cell)) {
						msg = getClassToolTipText(cell);
					}
				} else if (type == GraphType.PROPERTY) {
					if (isRDFSPropertyCell(cell)) {
						msg = getPropertyToolTipText(cell);
					}
				}
				return "<html>" + msg + "</html>";
			}
		}
		return null;
	}

	private boolean isContain(Object[] cells, Object cell) {
		for (int i = 0; i < cells.length; i++) {
			if (cells[i] == cell) {
				return true;
			}
		}
		return false;
	}

	private Object[] getValidCopyList(JGraph graph) {
		List copyList = new ArrayList();
		Object[] cells = graph.getSelectionCells();
		for (int i = 0; i < cells.length; i++) {
			if (isEdge(cells[i])) {
				Edge edge = (Edge) cells[i];
				if (isContain(cells, getSourceVertex(edge)) && isContain(cells, getTargetVertex(edge))) {
					copyList.add(cells[i]);
				}
			} else {
				copyList.add(cells[i]);
			}
		}
		return copyList.toArray();
	}

	class GraphCopyBuffer {
		private Point copyPoint; // コピーを行った位置
		private Object[] copyList;

		private ConnectionSet orgCs;
		private ConnectionSet csClone; // cloneのConnectionSet

		private Map orgAttributesMap;
		private Map cloneAttributes; // cloneのAttributes

		private Map cloneMap;
		private Map cloneInfoMap;

		private Map copyRDFSInfoMap;

		GraphCopyBuffer(Point pt, Object[] list, GraphTransferable gt, Map map) {
			copyPoint = pt;
			copyList = list;
			orgCs = gt.getConnectionSet();
			orgAttributesMap = gt.getAttributeMap();
			copyRDFSInfoMap = map;
		}

		public ConnectionSet getCloneConnectionSet() {
			return csClone;
		}

		public Map getCloneAttributes() {
			return cloneAttributes;
		}

		public Object get(Object clone) {
			return cloneInfoMap.get(clone);
		}

		public Point getCopyPoint() {
			return copyPoint;
		}

		public Set keySet() {
			return cloneInfoMap.keySet();
		}

		public Map getCloneMap() {
			return cloneMap;
		}

		private void setCellPosition(Object cell) {
			// 元のセルとコピー位置との差を求める
			GraphCell orgCell = (GraphCell) cell;
			Map orgMap = orgCell.getAttributes();
			Map newMap = ((GraphCell) cloneMap.get(cell)).getAttributes();
			Rectangle orgRec = GraphConstants.getBounds(orgMap);
			Rectangle newRec = new Rectangle(orgRec);
			newRec.x = orgRec.x - copyPoint.x;
			newRec.y = orgRec.y - copyPoint.y;
			GraphConstants.setBounds(newMap, newRec);
			Map nested = new HashMap();
			nested.put(cloneMap.get(cell), GraphConstants.cloneMap(newMap));
			getModel().edit(nested, null, null, null);
		}

		private void createClassCellClones(Object cell) {
			ClassInfo orgInfo = (ClassInfo) copyRDFSInfoMap.get(cell);
			ClassInfo newInfo = rdfsInfoMap.cloneClassInfo(orgInfo);
			cloneInfoMap.put(cloneMap.get(cell), newInfo);
			setCellPosition(cell);
		}

		private void createPropertyCellClones(Object cell) {
			PropertyInfo orgInfo = (PropertyInfo) copyRDFSInfoMap.get(cell);
			PropertyInfo newInfo = rdfsInfoMap.clonePropertyInfo(orgInfo);
			cloneInfoMap.put(cloneMap.get(cell), newInfo);
			setCellPosition(cell);
		}

		public void createClones() {
			cloneMap = cloneCells(copyList);
			cloneAttributes = GraphConstants.cloneMap(orgAttributesMap);

			csClone = orgCs.clone(cloneMap);
			cloneInfoMap = new HashMap();
			for (Iterator i = cloneMap.keySet().iterator(); i.hasNext();) {
				Object cell = i.next();
				if (isRDFSClassCell(cell)) {
					createClassCellClones(cell);
				} else if (isRDFSPropertyCell(cell)) {
					createPropertyCellClones(cell);
				}
			}
		}
	}

	private GraphTransferable getGraphTransferable(JGraph graph) {
		TransferHandler th = graph.getTransferHandler();
		GraphTransferable gt = null;
		if (th instanceof BasicGraphUI.GraphTransferHandler) {
			BasicGraphUI.GraphTransferHandler gth = (BasicGraphUI.GraphTransferHandler) th;
			Transferable t = gth.createTransferable();
			if (t instanceof GraphTransferable) {
				gt = (GraphTransferable) t;
			}
		}
		return gt;
	}

	private RDFGraph getBufferGraph(Map clones, GraphTransferable gt) {
		ConnectionSet cs = gt.getConnectionSet().clone(clones);
		Map attributes = gt.getAttributeMap();
		attributes = GraphConstants.replaceKeys(clones, attributes);
		Object[] cells = clones.values().toArray();
		RDFGraph bufferGraph = new RDFGraph();
		bufferGraph.getModel().insert(cells, attributes, cs, null, null);
		return bufferGraph;
	}

	private Map getCopyRDFSInfoMap(Map clones) {
		Map copyRDFSInfoMap = new HashMap();
		for (Iterator i = clones.keySet().iterator(); i.hasNext();) {
			Object cell = i.next();
			if (isRDFSClassCell(cell)) {
				ClassInfo orgInfo = (ClassInfo) rdfsInfoMap.getCellInfo(cell);
				ClassInfo newInfo = rdfsInfoMap.cloneClassInfo(orgInfo);
				copyRDFSInfoMap.put(clones.get(cell), newInfo);
			} else if (isRDFSPropertyCell(cell)) {
				PropertyInfo orgInfo = (PropertyInfo) rdfsInfoMap.getCellInfo(cell);
				PropertyInfo newInfo = rdfsInfoMap.clonePropertyInfo(orgInfo);
				copyRDFSInfoMap.put(clones.get(cell), newInfo);
			}
		}
		return copyRDFSInfoMap;
	}

	private void copyRDFSCells(Point copyPoint) {
		GraphTransferable gt = getGraphTransferable(this);
		if (gt == null) {
			return;
		}
		Map clones = cloneCells(gt.getCells());
		RDFGraph bufferGraph = getBufferGraph(clones, gt);
		gt = getGraphTransferable(bufferGraph);
		if (gt == null) {
			return;
		}
		Object[] copyList = getValidCopyList(bufferGraph);
		Map copyRDFSInfoMap = getCopyRDFSInfoMap(clones);
		copyBuffer = new GraphCopyBuffer(copyPoint, copyList, gt, copyRDFSInfoMap);
	}

	private void copyRDFCell(Point pt) {
	}

	public void copy(Point pt) {
		if (type == GraphType.CLASS || type == GraphType.PROPERTY) {
			copyRDFSCells(pt);
		} else if (type == GraphType.RDF) {
			copyRDFCell(pt);
		}
	}

	private void setPastePosition(GraphCell cell, String value, Point pastePoint) {
		Map map = cell.getAttributes();
		Rectangle rec = GraphConstants.getBounds(map);
		//		System.out.println(rec);
		//		System.out.println("paste: "+pastePoint);
		rec.x = pastePoint.x + rec.x;
		rec.y = pastePoint.y + rec.y;
		GraphConstants.setBounds(map, rec);
		GraphConstants.setValue(map, value);
		Map nested = new HashMap();
		nested.put(cell, GraphConstants.cloneMap(map));
		getGraphLayoutCache().edit(nested, null, null, null);
	}

	private void pasteRDFSCell(Point pastePoint) {
		if (copyBuffer == null) {
			return;
		}
		copyBuffer.createClones();
		// URIの重複を防ぐ
		for (Iterator i = copyBuffer.keySet().iterator(); i.hasNext();) {
			GraphCell cell = (GraphCell) i.next();
			if (isRDFSClassCell(cell)) {
				ClassInfo info = (ClassInfo) copyBuffer.get(cell);
				if (gmanager.isDuplicated(info.getURIStr(), null, GraphType.CLASS)) {
					for (int j = 1; true; j++) {
						String copyURI = info.getURIStr() + "-copy" + j;
						if (!gmanager.isDuplicated(copyURI, null, GraphType.CLASS)) {
							info.setURI(copyURI);
							break;
						}
					}
				}
				rdfsInfoMap.putCellInfo(cell, info);
				setPastePosition(cell, info.getURIStr(), pastePoint);
			} else if (isRDFSPropertyCell(cell)) {
				PropertyInfo info = (PropertyInfo) copyBuffer.get(cell);
				if (gmanager.isDuplicated(info.getURIStr(), null, GraphType.PROPERTY)) {
					for (int j = 1; true; j++) {
						String copyURI = info.getURIStr() + "-copy" + j;
						if (!gmanager.isDuplicated(copyURI, null, GraphType.PROPERTY)) {
							info.setURI(copyURI);
							break;
						}
					}
				}
				rdfsInfoMap.putCellInfo(cell, info);
				setPastePosition(cell, info.getURIStr(), pastePoint);
			}
		}

		getGraphLayoutCache().insert(
			copyBuffer.getCloneMap().values().toArray(),
			copyBuffer.getCloneAttributes(),
			copyBuffer.getCloneConnectionSet(),
			null,
			null);
		gmanager.changeCellView();
	}

	private void pasteRDFCell(Point pt) {

	}

	public void paste(Point pt) {
		if (type == GraphType.CLASS || type == GraphType.PROPERTY) {
			pasteRDFSCell(pt);
		} else if (type == GraphType.RDF) {
			pasteRDFCell(pt);
		}
	}
}
