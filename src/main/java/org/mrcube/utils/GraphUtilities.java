/*
 * Project Name: MR^3 (Meta-Model Management based on RDFs Revision Reflection)
 * Project Website: http://mrcube.org/
 *
 * Copyright (C) 2003-2018 Yamaguchi Laboratory, Keio University. All rights reserved.
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

package org.mrcube.utils;

import org.apache.jena.rdf.model.Resource;
import org.jgraph.graph.AttributeMap;
import org.jgraph.graph.GraphCell;
import org.jgraph.graph.GraphConstants;
import org.mrcube.jgraph.GraphManager;
import org.mrcube.jgraph.RDFCellStyleChanger;
import org.mrcube.jgraph.RDFGraph;
import org.mrcube.models.ClassModel;
import org.mrcube.models.MR3Constants.GraphType;
import org.mrcube.models.NamespaceModel;
import org.mrcube.models.RDFResourceModel;
import org.mrcube.models.RDFSModel;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.*;

/**
 * @author Takeshi Morita
 */
public class GraphUtilities {

    public static Font defaultFont = null;
    public static boolean isColor = true;

    private static Set<NamespaceModel> namespaceModelSet = new HashSet<>();
    public static final Color graphBackgroundColor = Color.white;

    public static void setNamespaceModelSet(Set<NamespaceModel> infoSet) {
        namespaceModelSet = infoSet;
    }

    public static Set<NamespaceModel> getNamespaceModelSet() {
        return Collections.unmodifiableSet(namespaceModelSet);
    }

    public static void changeAllCellColor(GraphManager gmanager) {
        RDFGraph rdfGraph = gmanager.getCurrentRDFGraph();
        RDFGraph classGraph = gmanager.getCurrentClassGraph();
        RDFGraph propertyGraph = gmanager.getCurrentPropertyGraph();

        if (rdfGraph == null || classGraph == null || propertyGraph == null) {
            return;
        }

        Object[] cells = rdfGraph.getAllCells();
        for (Object cell2 : cells) {
            if (cell2 instanceof RDFCellStyleChanger) {
                RDFCellStyleChanger changer = (RDFCellStyleChanger) cell2;
                changer.changeDefaultCellStyle(rdfGraph);
            }
        }

        cells = classGraph.getAllCells();
        for (Object cell1 : cells) {
            if (cell1 instanceof RDFCellStyleChanger) {
                RDFCellStyleChanger changer = (RDFCellStyleChanger) cell1;
                changer.changeDefaultCellStyle(classGraph);
            }
        }

        cells = propertyGraph.getAllCells();
        for (Object cell : cells) {
            if (cell instanceof RDFCellStyleChanger) {
                RDFCellStyleChanger changer = (RDFCellStyleChanger) cell;
                changer.changeDefaultCellStyle(propertyGraph);
            }
        }
    }

    /**
     * セルの色を変更するためのメソッド
     *
     * @param graph
     * @param cell
     * @param backGroundColor
     * @param borderColor
     */
    public static void changeCellStyle(RDFGraph graph, GraphCell cell, Color backGroundColor, Color borderColor, float lineWidth) {
        if (cell != null) {
            Map map = new AttributeMap();
            if (GraphConstants.getLineWidth(cell.getAttributes()) == EMPHASIS_WIDTH) {
                lineWidth = EMPHASIS_WIDTH;
            }
            if (isColor) {
                GraphConstants.setLineWidth(map, lineWidth);
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

            graph.getGraphLayoutCache().edit(new Object[]{cell}, map);
        }
    }

    public static void changeDefaultCellStyle(RDFGraph graph, GraphCell cell, Color backGroundColor) {
        changeCellStyle(graph, cell, backGroundColor, Color.black, RDFCellStyleChanger.LINE_WIDTH);
    }

    public static boolean isChangedSelectedColor = true;

    public static Object[] changeSelectionCellStyle(RDFGraph graph, Object[] lastSelectionCells) {
        if (lastSelectionCells == null) {
            return null;
        }
        if (!isChangedSelectedColor) {
            return lastSelectionCells;
        }
        for (Object lastSelectionCell : lastSelectionCells) {
            if (lastSelectionCell instanceof RDFCellStyleChanger) {
                RDFCellStyleChanger changer = (RDFCellStyleChanger) lastSelectionCell;
                changer.changeDefaultCellStyle(graph);
            }
        }
        Object[] cells = graph.getSelectionCells();
        cells = graph.getDescendants(cells);
        for (Object cell : cells) {
            if (cell instanceof RDFCellStyleChanger) {
                RDFCellStyleChanger changer = (RDFCellStyleChanger) cell;
                changer.changeSelectedCellStyle(graph);
            }
        }
        return cells;
    }

    public static void editCell(GraphCell cell, AttributeMap map, RDFGraph graph) {
        Map nested = new HashMap();
        nested.put(cell, map.clone());
        graph.getGraphLayoutCache().edit(nested, null, null, null);
    }

    private static final int ANON_NODE_WIDTH = 50;

    public static void resizeAllRDFResourceCell(GraphManager gm) {
        RDFGraph graph = gm.getCurrentRDFGraph();
        Object[] cells = graph.getAllCells();
        for (Object cell1 : cells) {
            GraphCell cell = (GraphCell) cell1;
            if (RDFGraph.isRDFResourceCell(cell)) {
                RDFResourceModel info = (RDFResourceModel) GraphConstants.getValue(cell.getAttributes());
                resizeRDFResourceCell(gm, info, cell);
            }
        }
    }

    public static void resizeAllRDFSResourceCell(GraphManager gm) {
        Object[] cells = gm.getCurrentClassGraph().getAllCells();
        for (Object cell2 : cells) {
            GraphCell cell = (GraphCell) cell2;
            if (RDFGraph.isRDFSCell(cell)) {
                RDFSModel info = (RDFSModel) GraphConstants.getValue(cell.getAttributes());
                resizeRDFSResourceCell(gm, info, cell);
            }
        }
        cells = gm.getCurrentPropertyGraph().getAllCells();
        for (Object cell1 : cells) {
            GraphCell cell = (GraphCell) cell1;
            if (RDFGraph.isRDFSCell(cell)) {
                RDFSModel info = (RDFSModel) GraphConstants.getValue(cell.getAttributes());
                resizeRDFSResourceCell(gm, info, cell);
            }
        }
    }

    public static void resizeRDFSResourceCell(GraphManager gm, RDFSModel rdfsModel, GraphCell cell) {
        String value = gm.getRDFSNodeValue(rdfsModel.getURI(), rdfsModel);
        Dimension size = GraphUtilities.getAutoNodeDimension(gm, value);
        if (rdfsModel instanceof ClassModel) {
            GraphUtilities.resizeCell(size, gm.getCurrentClassGraph(), cell);
        } else {
            GraphUtilities.resizeCell(size, gm.getCurrentPropertyGraph(), cell);
        }
    }

    public static void resizeRDFResourceCell(GraphManager gm, RDFResourceModel resInfo, GraphCell cell) {
        String value = gm.getRDFNodeValue(resInfo.getURI(), resInfo);
        Dimension size = GraphUtilities.getAutoNodeDimension(gm, value);
        GraphUtilities.resizeCell(size, gm.getCurrentRDFGraph(), cell);
    }

    public static void resizeCell(Dimension size, RDFGraph graph, GraphCell cell) {
        Rectangle2D rect = GraphConstants.getBounds(cell.getAttributes());
        if (rect != null) {
            AttributeMap map = cell.getAttributes();
            GraphConstants.setBounds(map,
                    map.createRect(rect.getX(), rect.getY(), size.getWidth(), size.getHeight()));
            graph.getGraphLayoutCache().editCell(cell, map);
        }
    }

    public static Dimension getAutoLiteralNodeDimention(GraphManager gmanager, String value) {
        if (!gmanager.isAutoNodeSize()) {
            return new Dimension(MR3CellMaker.CELL_WIDTH, MR3CellMaker.CELL_HEIGHT);
        }
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
        if (uri.isAnon()) {
            return "";
        }
        for (NamespaceModel namespaceModel : namespaceModelSet) {
            if (Utilities.getNameSpace(uri).equals(namespaceModel.getNameSpace())) {
                if (namespaceModel.isAvailable()) {
                    return namespaceModel.getPrefix() + ":" + Utilities.getLocalName(uri);
                }
            }
        }
        return uri.toString();
    }

    public static Rectangle2D getTypeCellRectangle(GraphCell cell, RDFSModel info, GraphManager gm) {
        Dimension typeDim = getAutoNodeDimension(gm, gm.getRDFSNodeValue(info.getURI(), info));
        Rectangle2D rect = GraphConstants.getBounds(cell.getAttributes());
        return new Rectangle2D.Double(rect.getX(),
                rect.getY() - rect.getHeight(), typeDim.getWidth(), typeDim.getHeight());
    }

    public static Dimension getAutoNodeDimension(GraphManager gmanager, String value) {
        if (!gmanager.isAutoNodeSize()) {
            return new Dimension(MR3CellMaker.CELL_WIDTH, MR3CellMaker.CELL_HEIGHT);
        }
        if (defaultFont == null) {
            defaultFont = gmanager.getCurrentRDFGraph().getFont();
        }
        FontMetrics fm = gmanager.getCurrentRDFGraph().getFontMetrics(defaultFont);
        int width = fm.stringWidth(value) * 2;
        if (value.length() == 0) {
            width = ANON_NODE_WIDTH;
        }
        int height = fm.getHeight() + MR3CellMaker.CELL_MARGIN;
        return new Dimension(width, height);
    }

    private static final float EMPHASIS_WIDTH = 3;

    public static void emphasisNodes(RDFGraph graph) {
        Object[] cells = graph.getSelectionCells();
        for (Object cell1 : cells) {
            GraphCell cell = (GraphCell) cell1;
            AttributeMap map = cell.getAttributes();
            // GraphConstants.setLineWidth(map, EMPHASIS_WIDTH);
            editCell(cell, map, graph);
        }
    }
}