/*
 * Project Name: MR^3 (Meta-Model Management based on RDFs Revision Reflection)
 * Project Website: http://mr3.sourceforge.net/
 *
 * Copyright (C) 2003-2015 Yamaguchi Laboratory, Keio University. All rights reserved.
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

package net.sourceforge.mr3.jgraph;

import java.awt.*;
import java.awt.event.*;
import java.awt.print.*;
import java.text.*;
import java.util.*;
import java.util.List;

import javax.swing.*;
import javax.swing.plaf.*;

import net.sourceforge.mr3.actions.*;
import net.sourceforge.mr3.data.*;
import net.sourceforge.mr3.data.MR3Constants.*;
import net.sourceforge.mr3.util.*;

import org.jgraph.*;
import org.jgraph.graph.*;

/**
 * @author Takeshi Morita
 */
public class RDFGraph extends JGraph {

    private CopyAction copyAction;
    private CutAction cutAction;
    private PasteAction pasteAction;

    private boolean pagevisible = false;
    private transient PageFormat pageFormat = new PageFormat();

    private ImageIcon background;

    private Object[] copyCells;

    private GraphType type;
    private GraphManager gmanager;

    public RDFGraph(GraphManager gm, GraphModel model, GraphType type) {
        super(model, new GraphLayoutCache(model, new RDFCellViewFactory(), false));
        this.type = type;
        gmanager = gm;
        initStatus();
        cutAction = new CutAction(this, gmanager);
        copyAction = new CopyAction(this);
        pasteAction = new PasteAction(this, gmanager);
        SwingUtilities.replaceUIActionMap(this, createActionMap());
    }

    public void setCopyCells(Object[] cells) {
        copyCells = cells;
    }

    public Object[] getCopyCells() {
        return copyCells;
    }

    public void setAutoSize(boolean t) {
        for (Object cell : getAllCells()) {
            GraphCell graphCell = (GraphCell) cell;
            GraphConstants.setAutoSize(graphCell.getAttributes(), t);
        }
    }

    public void setBackgroundImage(ImageIcon image) {
        background = image;
    }

    // public Image getBackgroundImage() {
    public ImageIcon getBackgroundImage() {
        return background;
    }

    public void setPageFormat(PageFormat format) {
        pageFormat = format;
    }

    public PageFormat getPageFormat() {
        return pageFormat;
    }

    public void setPageVisible(boolean flag) {
        pagevisible = flag;
    }

    public boolean isPageVisible() {
        return pagevisible;
    }

    public GraphType getType() {
        return type;
    }

    /**
     * Returns true if <code>object</code> is a vertex, that is, if it is not an
     * instance of Port or Edge, and all of its children are ports, or it has no
     * children.
     */
    public boolean isGroup(Object cell) {
        // Map the Cell to its View
        CellView view = getGraphLayoutCache().getMapping(cell, false);
        if (view != null)
            return !view.isLeaf();
        return false;
    }

    /**
     * Returns true if <code>object</code> is a vertex, that is, if it is not an
     * instance of Port or Edge, and all of its children are ports, or it has no
     * children.
     */
    public boolean isVertex(Object object) {
        if (!(object instanceof Port) && !(object instanceof Edge))
            return !isGroup(object) && object != null;
        return false;
    }

    private void initStatus() {
        setGridSize(6);
        setSelectionEnabled(true);
        setGridEnabled(true);
        setTolerance(10);
        setCloneable(false);
        setDisconnectable(true);
        setAntiAliased(true);
        setEditable(true);
        RDFGraphUI rgui = new RDFGraphUI(this, gmanager);
        setUI(rgui);
        setTransferHandler(rgui.createTransferHandler());
    }

    public boolean isContains(Object c) {
        for (Object cell : getAllCells()) {
            if (cell == c) {
                return true;
            }
        }
        return false;
    }

    public boolean isOneCellSelected(Object cell) {
        return (getSelectionCount() == 1 && graphModel.getChildCount(cell) <= 1);
    }

    public GraphCell isOneRDFCellSelected(Object[] cells) {
        int count = 0;
        GraphCell rdfCell = null;
        for (Object cell : cells) {
            if (isRDFCell(cell)) {
                count++;
                rdfCell = (GraphCell) cell;
            }
        }
        if (count == 1) {
            return rdfCell;
        }
        return null;
    }

    public static boolean isEdge(Object object) {
        return (object instanceof Edge);
    }

    public static boolean isPort(Object object) {
        return (object instanceof Port);
    }

    public static boolean isRDFsCell(Object object) {
        return (isRDFCell(object) || isRDFSCell(object));
    }

    public static boolean isRDFCell(Object object) {
        return (isRDFResourceCell(object) || isRDFPropertyCell(object) || isRDFLiteralCell(object));
    }

    public static boolean isRDFSCell(Object object) {
        return (object instanceof OntClassCell || object instanceof OntPropertyCell);
    }

    public static boolean isRDFResourceCell(Object object) {
        return (object instanceof RDFResourceCell);
    }

    public static boolean isRDFPropertyCell(Object object) {
        return isEdge(object);
    }

    public static boolean isRDFLiteralCell(Object object) {
        return (object instanceof RDFLiteralCell);
    }

    public static boolean isRDFSClassCell(Object object) {
        return (object instanceof OntClassCell);
    }

    public static boolean isRDFSPropertyCell(Object object) {
        return (object instanceof OntPropertyCell);
    }

    public static boolean isTypeCell(Object object) {
        return (object instanceof TypeViewCell);
    }

    public Object[] getAllCells() {
        return getDescendants(getRoots());
    }

    public Object[] getAllSelectedCells() {
        return getDescendants(getSelectionCells());
    }

    public void selectAllNodes() {
        GraphUtilities.isChangedSelectedColor = false;
        clearSelection();
        addSelectionCells(getRoots()); // Descendantsまでやるとばらばら．
        GraphUtilities.isChangedSelectedColor = true;
    }

    public Object getSourceVertex(Object edge) {
        Object sourcePort = graphModel.getSource(edge);
        return graphModel.getParent(sourcePort);
    }

    public Object getTargetVertex(Object edge) {
        Object targetPort = graphModel.getTarget(edge);
        return graphModel.getParent(targetPort);
    }

    /**
     * cellに接続されているエッジのtargetとなるcellのSetを返す
     */
    public Set<GraphCell> getTargetCells(DefaultGraphCell cell) {
        Set<GraphCell> supCells = new HashSet<GraphCell>();
        if (cell.getChildCount() == 0) {
            return supCells;
        }
        Object port = cell.getChildAt(0);
        for (Iterator edges = graphModel.edges(port); edges.hasNext(); ) {
            Edge edge = (Edge) edges.next();
            GraphCell target = (GraphCell) getTargetVertex(edge);
            if (target != cell) {
                supCells.add(target);
            }
        }
        return supCells;
    }

    /**
     * cellに接続されているエッジのsourceとなるcellのSetを返す
     */
    public Set getSourceCells(DefaultGraphCell cell) {
        Object port = cell.getChildAt(0);

        Set supCells = new HashSet();
        for (Iterator edges = graphModel.edges(port); edges.hasNext(); ) {
            Edge edge = (Edge) edges.next();
            Object source = getSourceVertex(edge);
            if (source != cell) {
                supCells.add(source);
            }
        }
        return supCells;
    }

    public void removeAllCells() {
        removeCellsWithEdges(getAllCells());
    }

    /**
     * 選択されたCellに接続されているEdgeを削除
     */
    public void removeEdges() {
        Set removeCells = new HashSet();
        for (Object cell : getAllCells()) {
            if (isEdge(cell)) {
                removeCells.add(cell);
            }
        }
        graphLayoutCache.remove(removeCells.toArray());
    }

    /**
     * 選択されたノードとそのノードに接続されたエッジを削除
     *
     * @param cells
     */
    public void removeCellsWithEdges(Object[] cells) {
        RDFSInfoMap rdfsInfoMap = gmanager.getCurrentRDFSInfoMap();
        Set removeCells = new HashSet();
        for (int i = 0; i < cells.length; i++) {
            GraphCell cell = (GraphCell) cells[i];
            if (isPort(cells[i])) {
                Port port = (Port) cell;
                for (Iterator edges = graphModel.edges(port); edges.hasNext(); ) {
                    removeCells.add(edges.next());
                }
            } else if (isRDFSCell(cell)) {
                rdfsInfoMap.removeCellInfo(cell);
            }
        }
        try {
            graphLayoutCache.remove(removeCells.toArray());
            graphLayoutCache.remove(cells);
        } catch (NullPointerException npe) {
            npe.printStackTrace();
        }
    }

    private String getRDFSToolTipText(RDFSInfo info) {
        String msg = "<dl><dt>URI: </dt><dd>" + info.getURI() + "</dd>";
        MR3Literal literal = info.getFirstLabel();
        if (literal != null) {
            msg += "<dt>Label</dt><dd>Lang: " + literal.getLanguage() + "<br>"
                    + literal.getString() + "</dd>";
            msg += "<dt>Comment</dt>";
        }
        literal = info.getFirstComment();
        if (literal != null) {
            String comment = literal.getString();
            comment = insertLineFeed(comment);
            comment = comment.replaceAll("(\n|\r)+", "<br>");
            msg += "<dd>" + "Lang: " + literal.getLanguage() + "<br>" + comment + "</dd></dt>";
        }
        return msg;
    }

    private String getClassToolTipText(Object cell) {
        GraphCell gcell = (GraphCell) cell;
        ClassInfo info = (ClassInfo) GraphConstants.getValue(gcell.getAttributes());
        String msg = "<center><strong>Class</strong></center>";
        msg += getRDFSToolTipText(info);
        msg += "<strong>SuperClasses: </strong>" + info.getSuperRDFS() + "<br>";
        return msg;
    }

    private String getPropertyToolTipText(GraphCell cell) {
        PropertyInfo info = (PropertyInfo) GraphConstants.getValue(cell.getAttributes());
        if (info == null) {
            return "";
        }
        String msg = "<center><strong>Property</strong></center>";
        msg += getRDFSToolTipText(info);
        msg += "<strong>SuperProperties: </strong>" + info.getSuperRDFS() + "<br>";
        return msg;
    }

    private String getRDFResourceToolTipText(GraphCell cell) {
        String msg = "";
        RDFResourceInfo info = (RDFResourceInfo) GraphConstants.getValue(cell.getAttributes());
        if (info != null) {
            msg += "<h3>Resource</h3>";
            msg += "<strong>URI: </strong>" + info.getURI() + "<br>";
            msg += "<strong>Type: </strong>" + info.getType() + "<br>";
        }
        return msg;
    }

    private String getRDFPropertyToolTipText(GraphCell cell) {
        return getPropertyToolTipText(cell);
    }

    public String insertLineFeed(String str) {
        int COMMENT_WIDTH = 30;
        str = str.replaceAll("(\n|\r)+", "");
        BreakIterator i = BreakIterator.getLineInstance();
        i.setText(str);

        StringBuilder buf = new StringBuilder();
        for (int cnt = 0, start = i.first(), end = i.next(); end != BreakIterator.DONE; start = end, end = i
                .next()) {
            if (COMMENT_WIDTH < (cnt + (end - start))) {
                buf.append("\n");
                cnt = 0;
            }
            buf.append(str.substring(start, end));
            cnt += str.substring(start, end).length();
        }

        return buf.toString();
    }

    private String getRDFLiteralToolTipText(Object cell) {
        String msg = "<h3>Literal</h3>";
        MR3Literal literal = (MR3Literal) GraphConstants.getValue(((GraphCell) cell)
                .getAttributes());
        msg += "<strong>Lang: </strong>" + literal.getLanguage() + "<br>";
        msg += "<strong>Datatype: </strong>" + literal.getDatatype() + "<br>";
        msg += "<strong>Value: </strong><br>";
        msg += insertLineFeed(literal.getString());
        msg = msg.replaceAll("(\n|\r)+", "<br>");

        return msg;
    }

    public JToolTip createToolTip() {
        return new GraphToolTip();
    }

    private static final Color TOOLTIP_BACK_COLOR = new Color(240, 240, 200);

    class GraphToolTip extends JToolTip {
        public void paint(Graphics g) {
            setBackground(TOOLTIP_BACK_COLOR);
            super.paint(g);
        }
    }

    public String getToolTipText(MouseEvent event) {
        if (event != null) {
            GraphCell cell = (GraphCell) getFirstCellForLocation(event.getX(), event.getY());
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
                        for (Iterator i = children.iterator(); i.hasNext(); ) {
                            GraphCell resCell = (GraphCell) i.next();
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

    public CutAction getCutAction() {
        return cutAction;
    }

    public CopyAction getCopyAction() {
        return copyAction;
    }

    public PasteAction getPasteAction() {
        return pasteAction;
    }

    public ActionMap createActionMap() {
        ActionMap map = new ActionMapUIResource();
        map.put(TransferHandler.getCutAction().getValue(Action.NAME), cutAction);
        map.put(TransferHandler.getCopyAction().getValue(Action.NAME), copyAction);
        map.put(TransferHandler.getPasteAction().getValue(Action.NAME), pasteAction);
        map.put("selectAll", new SelectNodes(gmanager, type, "selectAll"));

        return map;
    }
}