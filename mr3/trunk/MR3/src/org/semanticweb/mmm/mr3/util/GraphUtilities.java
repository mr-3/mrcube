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

package org.semanticweb.mmm.mr3.util;

import java.awt.*;
import java.awt.geom.*;
import java.util.*;

import org.jgraph.graph.*;
import org.semanticweb.mmm.mr3.data.*;
import org.semanticweb.mmm.mr3.data.MR3Constants.*;
import org.semanticweb.mmm.mr3.jgraph.*;

import com.hp.hpl.jena.rdf.model.*;

/**
 * @author takeshi morita
 */
public class GraphUtilities {

    public static Font defaultFont = null;
    public static boolean isColor = true;

    private static Set<PrefixNSInfo> prefixNSInfoSet = new HashSet<PrefixNSInfo>();

    public static Color selectedColor = new Color(240, 240, 200);
    // public static Color selectedForegroundColor = new Color(0, 0, 128);
    public static Color selectedBorderColor = new Color(70, 70, 70);
    public static Color graphBackgroundColor = Color.white;

    public static void setPrefixNSInfoSet(Set<PrefixNSInfo> infoSet) {
        prefixNSInfoSet = infoSet;
    }

    public static Set<PrefixNSInfo> getPrefixNSInfoSet() {
        return Collections.unmodifiableSet(prefixNSInfoSet);
    }

    public static void changeAllCellColor(GraphManager gmanager) {
        RDFGraph rdfGraph = gmanager.getCurrentRDFGraph();
        RDFGraph classGraph = gmanager.getCurrentClassGraph();
        RDFGraph propertyGraph = gmanager.getCurrentPropertyGraph();

        Object[] cells = rdfGraph.getAllCells();
        for (int i = 0; i < cells.length; i++) {
            if (cells[i] instanceof RDFCellStyleChanger) {
                RDFCellStyleChanger changer = (RDFCellStyleChanger) cells[i];
                changer.changeDefaultStyle(rdfGraph);
            }
        }

        cells = classGraph.getAllCells();
        for (int i = 0; i < cells.length; i++) {
            if (cells[i] instanceof RDFCellStyleChanger) {
                RDFCellStyleChanger changer = (RDFCellStyleChanger) cells[i];
                changer.changeDefaultStyle(classGraph);
            }
        }

        cells = propertyGraph.getAllCells();
        for (int i = 0; i < cells.length; i++) {
            if (cells[i] instanceof RDFCellStyleChanger) {
                RDFCellStyleChanger changer = (RDFCellStyleChanger) cells[i];
                changer.changeDefaultStyle(propertyGraph);
            }
        }
    }

    /**
     * 
     * セルの色を変更するためのメソッド
     * 
     * @param graph
     * @param cell
     * @param backGroundColor
     * @param borderColor
     * @param lineWidth
     * 
     */
    public static void changeCellStyle(RDFGraph graph, GraphCell cell, Color backGroundColor, Color borderColor) {
        float lineWidth = 1;
        if (cell != null) {
            Map map = new AttributeMap();
            if (GraphConstants.getLineWidth(cell.getAttributes()) == EMPHASIS_WIDTH) {
                lineWidth = EMPHASIS_WIDTH;
            }
            // GraphConstants.setLineWidth(map, lineWidth);
            if (isColor) {
                if (RDFGraph.isRDFPropertyCell(cell)) {
                    GraphConstants.setLineColor(map, backGroundColor);
                } else {
                    GraphConstants.setBorderColor(map, borderColor);
                    GraphConstants.setBackground(map, backGroundColor);
                    GraphConstants.setOpaque(map, true);
                }
            } else {
                if (graph.getType() == GraphType.CLASS || graph.getType() == GraphType.PROPERTY) {
                    GraphConstants.setBorderColor(map, graphBackgroundColor);
                }
                GraphConstants.setOpaque(map, false);
            }

            map = new AttributeMap(map);
            map.remove(GraphConstants.BOUNDS);
            map.remove(GraphConstants.POINTS);

            graph.getGraphLayoutCache().edit(new Object[] { cell}, map);
        }
    }

    public static void changeDefaultCellStyle(RDFGraph graph, GraphCell cell, Color backGroundColor) {
        changeCellStyle(graph, cell, backGroundColor, Color.black);
    }

    public static boolean isChangedSelectedColor = true;

    public static Object[] changeSelectionCellStyle(RDFGraph graph, Object[] lastSelectionCells) {
        if (lastSelectionCells == null) { return null; }
        if (!isChangedSelectedColor) { return lastSelectionCells; }
        for (int i = 0; i < lastSelectionCells.length; i++) {
            if (lastSelectionCells[i] instanceof RDFCellStyleChanger) {
                RDFCellStyleChanger changer = (RDFCellStyleChanger) lastSelectionCells[i];
                changer.changeDefaultStyle(graph);
            }
        }
        Object[] cells = graph.getSelectionCells();
        cells = graph.getDescendants(cells);
        for (int i = 0; i < cells.length; i++) {
            if (cells[i] instanceof RDFCellStyleChanger) {
                RDFCellStyleChanger changer = (RDFCellStyleChanger) cells[i];
                changer.changeStyle(graph);
            }
        }
        return cells;
    }

    public static void editCell(GraphCell cell, AttributeMap map, RDFGraph graph) {
        Map nested = new HashMap();
        // nested.put(cell, GraphConstants.cloneMap(map));
        nested.put(cell, map.clone());
        graph.getGraphLayoutCache().edit(nested, null, null, null);
    }

    public static final int ANON_NODE_WIDTH = 50;

    public static void resizeAllRDFResourceCell(GraphManager gm) {
        RDFGraph graph = gm.getCurrentRDFGraph();
        Object[] cells = graph.getAllCells();
        for (int i = 0; i < cells.length; i++) {
            GraphCell cell = (GraphCell) cells[i];
            if (RDFGraph.isRDFResourceCell(cell)) {
                RDFResourceInfo info = (RDFResourceInfo) GraphConstants.getValue(cell.getAttributes());
                resizeRDFResourceCell(gm, info, cell);
            }
        }
    }

    public static void resizeAllRDFSResourceCell(GraphManager gm) {
        Object[] cells = gm.getCurrentClassGraph().getAllCells();
        for (int i = 0; i < cells.length; i++) {
            GraphCell cell = (GraphCell) cells[i];
            if (RDFGraph.isRDFSCell(cell)) {
                RDFSInfo info = (RDFSInfo) GraphConstants.getValue(cell.getAttributes());
                resizeRDFSResourceCell(gm, info, cell);
            }
        }
        cells = gm.getCurrentPropertyGraph().getAllCells();
        for (int i = 0; i < cells.length; i++) {
            GraphCell cell = (GraphCell) cells[i];
            if (RDFGraph.isRDFSCell(cell)) {
                RDFSInfo info = (RDFSInfo) GraphConstants.getValue(cell.getAttributes());
                resizeRDFSResourceCell(gm, info, cell);
            }
        }
    }

    public static void resizeRDFSResourceCell(GraphManager gm, RDFSInfo rdfsInfo, GraphCell cell) {        
        String value = gm.getRDFSNodeValue(rdfsInfo.getURI(), rdfsInfo);
        Dimension size = GraphUtilities.getAutoNodeDimension(gm, value);
        if (rdfsInfo instanceof ClassInfo) {
            GraphUtilities.resizeCell(size, gm.getCurrentClassGraph(), cell);
        } else {
            GraphUtilities.resizeCell(size, gm.getCurrentPropertyGraph(), cell);
        }
    }

    public static void resizeRDFResourceCell(GraphManager gm, RDFResourceInfo resInfo, GraphCell cell) {
        String value = gm.getRDFNodeValue(resInfo.getURI(), resInfo);
        Dimension size = GraphUtilities.getAutoNodeDimension(gm, value);
        GraphUtilities.resizeCell(size, gm.getCurrentRDFGraph(), cell);
    }

    public static void resizeCell(Dimension size, RDFGraph graph, GraphCell cell) {
        Rectangle2D rect = GraphConstants.getBounds(cell.getAttributes());
        if (rect != null) {
            AttributeMap map = cell.getAttributes();
            GraphConstants.setBounds(map, map.createRect(rect.getX(), rect.getY(), size.getWidth(), size.getHeight()));
            graph.getGraphLayoutCache().editCell(cell, map);
        }
    }

    public static Dimension getAutoLiteralNodeDimention(GraphManager gmanager, String value) {
        if (!gmanager.isAutoNodeSize()) { return new Dimension(MR3CellMaker.CELL_WIDTH, MR3CellMaker.CELL_HEIGHT); }
        if (defaultFont == null) {
            defaultFont = gmanager.getCurrentRDFGraph().getFont();
        }
        StringTokenizer tokenizer = new StringTokenizer(value, "\n");
        int width = MR3CellMaker.DEFAULT_CELL_WIDTH / 3;
        int height = MR3CellMaker.CELL_MARGIN;
        FontMetrics fm = gmanager.getCurrentRDFGraph().getFontMetrics(defaultFont);
        while (tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken();
            if (width < fm.stringWidth(token)) {
                width = fm.stringWidth(token);
            }
            height += fm.getHeight();
        }
        if (height == MR3CellMaker.CELL_MARGIN) {
            height = MR3CellMaker.DEFAULT_CELL_HEIGHT;
        }
        width += MR3CellMaker.CELL_MARGIN;
        return new Dimension(width, height);
    }

    public static String getNSPrefix(Resource uri) {
        if (uri.isAnon()) { return ""; }
        for (PrefixNSInfo prefixNSInfo : prefixNSInfoSet) {
            // if (uri.getNameSpace().equals(prefixNSInfo.getNameSpace())) {
            if (Utilities.getNameSpace(uri).equals(prefixNSInfo.getNameSpace())) {
                // if (prefixNSInfo.isAvailable()) { return
                // prefixNSInfo.getPrefix() + ":" + uri.getLocalName(); }
                if (prefixNSInfo.isAvailable()) { return prefixNSInfo.getPrefix() + ":" + Utilities.getLocalName(uri); }
            }
        }
        return uri.toString();
    }

    public static Rectangle2D getTypeCellRectangle(GraphCell cell, RDFSInfo info, GraphManager gm) {
        Dimension typeDim = getAutoNodeDimension(gm, gm.getRDFSNodeValue(info.getURI(), info));
        Rectangle2D rect = GraphConstants.getBounds(cell.getAttributes());
        Rectangle2D.Double typeRect = new Rectangle2D.Double(rect.getX() + (rect.getWidth() / 3), rect.getY()
                - rect.getHeight(), typeDim.getWidth() * 1.2, typeDim.getHeight());
        return typeRect;
    }

    public static Dimension getAutoNodeDimension(GraphManager gmanager, String value) {
        if (!gmanager.isAutoNodeSize()) { return new Dimension(MR3CellMaker.CELL_WIDTH, MR3CellMaker.CELL_HEIGHT); }
        if (defaultFont == null) {
            defaultFont = gmanager.getCurrentRDFGraph().getFont();
        }
        FontMetrics fm = gmanager.getCurrentRDFGraph().getFontMetrics(defaultFont);
        int width = fm.stringWidth(value) + MR3CellMaker.CELL_MARGIN;
        if (value.length() == 0) {
            width = ANON_NODE_WIDTH;
        }
        int height = fm.getHeight() + MR3CellMaker.CELL_MARGIN;
        return new Dimension(width, height);
    }

    private static final float EMPHASIS_WIDTH = 3;

    public static void emphasisNodes(RDFGraph graph) {
        Object[] cells = graph.getSelectionCells();
        for (int i = 0; i < cells.length; i++) {
            GraphCell cell = (GraphCell) cells[i];
            AttributeMap map = cell.getAttributes();
            // GraphConstants.setLineWidth(map, EMPHASIS_WIDTH);
            editCell(cell, map, graph);
        }
    }
}