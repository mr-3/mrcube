package mr3.jgraph;
import java.awt.*;
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

public class RDFGraph extends JGraph {

	private GraphType type;
	private GraphManager gmanager;
	private AttributeDialog propWindow;
	private RDFResourceInfoMap resInfoMap = RDFResourceInfoMap.getInstance();
	private RDFLiteralInfoMap litInfoMap = RDFLiteralInfoMap.getInstance();
	private RDFSInfoMap rdfsInfoMap = RDFSInfoMap.getInstance();

	public RDFGraph(GraphManager manager, AttributeDialog pw, GraphType type) {
		super(new RDFGraphModel());
//		GraphLayoutCache cache = new GraphLayoutCache(getModel(), getGraphLayoutCache().getFactory(), false, true);
//		setGraphLayoutCache(cache);
		initStatus();
		gmanager = manager;
		propWindow = pw;
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
		//		selectionModel.setChildrenSelectable(false);
		graphModel.addGraphModelListener(new ModelListener());
	}

	public void startEditingAtCell(Object cell) {
		if (propWindow != null) {
			propWindow.setVisible(true);
		}
	}

	protected VertexView createVertexView(Object v, CellMapper cm) {
		if (v instanceof EllipseCell)
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

	public boolean isEdge(Object object) {
		return (object instanceof Edge);
	}

	public boolean isPropertyCell(Object object) {
		return isEdge(object);
	}

	public boolean isPort(Object object) {
		return (object instanceof Port);
	}

	public boolean isResourceCell(Object object) {
		return (object instanceof EllipseCell);
	}

	public boolean isLiteralCell(Object object) {
		return (object instanceof LiteralCell);
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
			} else if (isResourceCell(cells[i])) {
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
					if (isLiteralCell(cell)) {
						msg = getRDFLiteralToolTipText(cell);
					} else if (isResourceCell(cell)) {
						msg = getRDFResourceToolTipText(cell);
					} else if (isPropertyCell(cell)) {
						msg = getRDFPropertyToolTipText(cell);
					} else {
						List children = ((DefaultGraphCell) cell).getChildren();
						for (Iterator i = children.iterator(); i.hasNext();) {
							Object resCell = i.next();
							if (isResourceCell(resCell)) {
								msg = getRDFResourceToolTipText(resCell);
							}
						}
					}
				} else if (type == GraphType.CLASS) {
					if (isResourceCell(cell)) {
						msg = getClassToolTipText(cell);
					}
				} else if (type == GraphType.PROPERTY) {
					if (isResourceCell(cell)) {
						msg = getPropertyToolTipText(cell);
					}
				}
				return "<html>" + msg + "</html>";
			}
		}
		return null;
	}
}
