/*
 * Project Name: MR^3 (Meta-Model Management based on RDFs Revision Reflection)
 * Project Website: http://mrcube.org/
 *
 * Copyright (C) 2003-2022 Takeshi Morita. All rights reserved.
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

package org.mrcube.jgraph;

import org.jgraph.JGraph;
import org.jgraph.graph.*;
import org.mrcube.actions.CopyAction;
import org.mrcube.actions.CutAction;
import org.mrcube.actions.PasteAction;
import org.mrcube.actions.SelectAllNodesAction;
import org.mrcube.editors.Editor;
import org.mrcube.models.MR3Constants.GraphType;
import org.mrcube.models.MR3Literal;
import org.mrcube.models.RDFResourceModel;
import org.mrcube.models.RDFSModel;
import org.mrcube.models.RDFSModelMap;
import org.mrcube.utils.GraphUtilities;
import org.mrcube.utils.Translator;

import javax.swing.*;
import javax.swing.plaf.ActionMapUIResource;
import java.awt.event.MouseEvent;
import java.awt.print.PageFormat;
import java.text.BreakIterator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * @author Takeshi Morita
 */
public class RDFGraph extends JGraph {

    private final CopyAction copyAction;
    private final CutAction cutAction;
    private final PasteAction pasteAction;
    private final SelectAllNodesAction selectAllNodesAction;

    private boolean pagevisible = false;
    private transient PageFormat pageFormat = new PageFormat();

    private ImageIcon background;

    private Object[] copyCells;

    private final GraphType type;
    private final GraphManager gmanager;

    public RDFGraph(GraphManager gm, GraphModel model, GraphType type) {
        super(model, new GraphLayoutCache(model, new RDFCellViewFactory(), false));
        this.type = type;
        gmanager = gm;
        initStatus();
        cutAction = new CutAction(this, gmanager);
        copyAction = new CopyAction(this);
        pasteAction = new PasteAction(this, gmanager);
        selectAllNodesAction = new SelectAllNodesAction(gmanager, type);
        SwingUtilities.replaceUIActionMap(this, createActionMap());
    }

    public SelectAllNodesAction getSelectAllNodesAction() {
        return selectAllNodesAction;
    }

    public void setCopyCells(Object[] cells) {
        copyCells = cells;
    }

    public Object[] getCopyCells() {
        return copyCells;
    }

    public void setBackgroundImage(ImageIcon image) {
        background = image;
    }

    public ImageIcon getBackgroundImage() {
        return background;
    }

    public PageFormat getPageFormat() {
        return pageFormat;
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
    private boolean isGroup(Object cell) {
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
        setBackground(Editor.backgroundColor);
    }

    public void resetBackground() {
        setBackground(Editor.backgroundColor);
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

    private static boolean isRDFCell(Object object) {
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
        Set<GraphCell> supCells = new HashSet<>();
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
        RDFSModelMap rdfsModelMap = gmanager.getRDFSInfoMap();
        Set removeCells = new HashSet();
        for (Object cell1 : cells) {
            GraphCell cell = (GraphCell) cell1;
            if (isPort(cell1)) {
                Port port = (Port) cell;
                for (Iterator edges = graphModel.edges(port); edges.hasNext(); ) {
                    removeCells.add(edges.next());
                }
            } else if (isRDFSCell(cell)) {
                rdfsModelMap.removeCellInfo(cell);
            }
        }
        try {
            graphLayoutCache.remove(removeCells.toArray());
            graphLayoutCache.remove(cells);
        } catch (NullPointerException npe) {
            npe.printStackTrace();
        }
    }

    private String getLabelToolTipText(MR3Literal label) {
        if (label != null) {
            return String.format("<b>%s: </b>%s (%s)<br>",
                    Translator.getString("Label"), label.getString(), label.getLanguage());
        }
        return "";
    }

    private String getCommentToolTipText(MR3Literal commentLiteral) {
        if (commentLiteral != null) {
            String comment = commentLiteral.getString();
            comment = insertLineFeed(comment);
            comment = comment.replaceAll("(\n|\r)+", "<br>");
            return String.format("<b>%s: </b>%s (%s)<br>",
                    Translator.getString("Comment"), comment, commentLiteral.getLanguage());
        }
        return "";
    }

    private String getRDFSToolTipText(Object cell, String rdfsTypeLabel, String superConceptLabel) {
        GraphCell gcell = (GraphCell) cell;
        RDFSModel info = (RDFSModel) GraphConstants.getValue(gcell.getAttributes());
        StringBuilder toolTipTextBuilder = new StringBuilder();
        toolTipTextBuilder.append(String.format("<h3>%s</h3>", rdfsTypeLabel));
        toolTipTextBuilder.append(String.format("<b>URI: </b>%s<br>", info.getURI()));
        toolTipTextBuilder.append(getLabelToolTipText(info.getFirstLabel()));
        toolTipTextBuilder.append(getCommentToolTipText(info.getFirstComment()));
        if (!info.getSuperRDFS().isEmpty()) {
            toolTipTextBuilder.append(String.format("<b>%s: </b>%s<br>",
                    superConceptLabel, info.getSuperRDFS()));
        }
        return toolTipTextBuilder.toString();
    }

    private String getClassToolTipText(Object cell) {
        return getRDFSToolTipText(cell, Translator.getString("Class"),
                Translator.getString("SuperClasses"));
    }

    private String getPropertyToolTipText(GraphCell cell) {
        return getRDFSToolTipText(cell, Translator.getString("Property"),
                Translator.getString("SuperProperties"));
    }

    private String getRDFResourceToolTipText(GraphCell cell) {
        StringBuilder toolTipTextBuilder = new StringBuilder();
        RDFResourceModel info = (RDFResourceModel) GraphConstants.getValue(cell.getAttributes());
        if (info != null) {
            toolTipTextBuilder.append(String.format("<h3>%s</h3>", Translator.getString("RDFResource")));
            toolTipTextBuilder.append(String.format("<b>URI: </b>%s<br>", info.getURI()));
            toolTipTextBuilder.append(String.format("<b>%s: </b>%s<br>", Translator.getString("ResourceType"), info.getType()));
            toolTipTextBuilder.append(getLabelToolTipText(info.getFirstLabel()));
            toolTipTextBuilder.append(getCommentToolTipText(info.getFirstComment()));
        }
        return toolTipTextBuilder.toString();
    }

    private String getRDFPropertyToolTipText(GraphCell cell) {
        return getPropertyToolTipText(cell);
    }

    private String insertLineFeed(String str) {
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
            buf.append(str, start, end);
            cnt += str.substring(start, end).length();
        }

        return buf.toString();
    }

    private String getRDFLiteralToolTipText(Object cell) {
        StringBuilder toolTipTextBuilder = new StringBuilder();
        toolTipTextBuilder.append(String.format("<h3>%s</h3>", Translator.getString("Literal")));
        MR3Literal literal = (MR3Literal) GraphConstants.getValue(((GraphCell) cell).getAttributes());
        toolTipTextBuilder.append(String.format("<b>%s: </b>%s<br>",
                Translator.getString("Lang"), literal.getLanguage()));
        toolTipTextBuilder.append(String.format("<b>%s: </b>%s<br>",
                Translator.getString("DataType"), literal.getDatatype()));
        toolTipTextBuilder.append(String.format("<b>%s: </b><br>", Translator.getString("LiteralValue")));
        toolTipTextBuilder.append(insertLineFeed(literal.getString()
                .replaceAll("(\n|\r)+", "<br>")));
        return toolTipTextBuilder.toString();
    }

    @Override
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
                        for (Object child : children) {
                            GraphCell resCell = (GraphCell) child;
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
                return "<html><body>" + msg + "</body></html>";
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

    private ActionMap createActionMap() {
        ActionMap map = new ActionMapUIResource();
        map.put(TransferHandler.getCutAction().getValue(Action.NAME), cutAction);
        map.put(TransferHandler.getCopyAction().getValue(Action.NAME), copyAction);
        map.put(TransferHandler.getPasteAction().getValue(Action.NAME), pasteAction);
        map.put(selectAllNodesAction.getName(), selectAllNodesAction);

        return map;
    }
}