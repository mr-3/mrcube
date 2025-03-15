/*
 * Project Name: MR^3 (Meta-Model Management based on RDFs Revision Reflection)
 * Project Website: https://mr-3.github.io/
 *
 * Copyright (C) 2003-2025 Takeshi Morita. All rights reserved.
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

package jp.ac.aoyama.it.ke.mrcube.utils;

import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.jgraph.graph.AttributeMap;
import org.jgraph.graph.GraphCell;
import org.jgraph.graph.GraphConstants;
import jp.ac.aoyama.it.ke.mrcube.editors.Editor;
import jp.ac.aoyama.it.ke.mrcube.jgraph.GraphManager;
import jp.ac.aoyama.it.ke.mrcube.jgraph.RDFCellStyleChanger;
import jp.ac.aoyama.it.ke.mrcube.jgraph.RDFGraph;
import jp.ac.aoyama.it.ke.mrcube.models.ClassModel;
import jp.ac.aoyama.it.ke.mrcube.models.NamespaceModel;
import jp.ac.aoyama.it.ke.mrcube.models.InstanceModel;
import jp.ac.aoyama.it.ke.mrcube.models.RDFSModel;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.*;

/**
 * @author Takeshi Morita
 */
public class GraphUtilities {

    public static boolean isBlackAndWhite;
    public static Font defaultFont = null;
    private static final float EMPHASIS_WIDTH = 3;

    private static Set<NamespaceModel> namespaceModelSet = new HashSet<>();
    public static Color graphForegroundColor = Color.black;
    public static Color graphBackgroundColor = Color.white;

    public static void setNamespaceModelSet(Set<NamespaceModel> infoSet) {
        namespaceModelSet = infoSet;
    }

    public static Set<NamespaceModel> getNamespaceModelSet() {
        return Collections.unmodifiableSet(namespaceModelSet);
    }

    /**
     * セルの色を変更するためのメソッド
     *
     * @param graph
     * @param cell
     * @param fgColor
     * @param bgColor
     * @param borderColor
     */
    public static void changeCellStyle(RDFGraph graph, GraphCell cell, Color fgColor, Color bgColor, Color borderColor) {
        if (cell != null) {
            Map map = new AttributeMap();
            if (isBlackAndWhite) {
                if (RDFGraph.isRDFPropertyCell(cell)) {
                    GraphConstants.setForeground(map, graphForegroundColor);
                    GraphConstants.setLineColor(map, graphForegroundColor);
                    GraphConstants.setLineWidth(map, RDFCellStyleChanger.LINE_WIDTH);
                } else {
                    GraphConstants.setForeground(map, graphForegroundColor);
                    GraphConstants.setBackground(map, graphBackgroundColor);
                    GraphConstants.setBorderColor(map, graphForegroundColor);
                    GraphConstants.setOpaque(map, false);
                }
            } else {
                if (RDFGraph.isRDFPropertyCell(cell)) {
                    GraphConstants.setForeground(map, fgColor);
                    GraphConstants.setLineColor(map, borderColor);
                    GraphConstants.setLineWidth(map, RDFCellStyleChanger.LINE_WIDTH);
                } else {
                    GraphConstants.setForeground(map, fgColor);
                    if (borderColor != null) {
                        GraphConstants.setBorderColor(map, borderColor);
                    }
                    GraphConstants.setBackground(map, bgColor);
                    GraphConstants.setFont(map, new Font("SansSerif", Font.PLAIN, RDFCellStyleChanger.FONT_SIZE));
                    GraphConstants.setOpaque(map, true);
                }
            }

            map = new AttributeMap(map);
            map.remove(GraphConstants.BOUNDS);
            map.remove(GraphConstants.POINTS);

            graph.getGraphLayoutCache().edit(new Object[]{cell}, map);
        }
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
            if (lastSelectionCell instanceof RDFCellStyleChanger changer) {
                changer.changeDefaultCellStyle(graph);
            }
        }
        Object[] cells = graph.getSelectionCells();
        cells = graph.getDescendants(cells);
        for (Object cell : cells) {
            if (cell instanceof RDFCellStyleChanger changer) {
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
        RDFGraph graph = gm.getInstanceGraph();
        Object[] cells = graph.getAllCells();
        for (Object cell1 : cells) {
            GraphCell cell = (GraphCell) cell1;
            if (RDFGraph.isRDFResourceCell(cell)) {
                InstanceModel info = (InstanceModel) GraphConstants.getValue(cell.getAttributes());
                resizeRDFResourceCell(gm, info, cell);
            }
        }
    }

    public static void resizeAllRDFSResourceCell(GraphManager gm) {
        Object[] cells = gm.getClassGraph().getAllCells();
        for (Object cell2 : cells) {
            GraphCell cell = (GraphCell) cell2;
            if (RDFGraph.isRDFSCell(cell)) {
                RDFSModel info = (RDFSModel) GraphConstants.getValue(cell.getAttributes());
                resizeRDFSResourceCell(gm, info, cell);
            }
        }
        cells = gm.getPropertyGraph().getAllCells();
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
            GraphUtilities.resizeCell(size, gm.getClassGraph(), cell);
        } else {
            GraphUtilities.resizeCell(size, gm.getPropertyGraph(), cell);
        }
    }

    public static void resizeRDFResourceCell(GraphManager gm, InstanceModel resInfo, GraphCell cell) {
        String value = gm.getRDFNodeValue(resInfo.getURI(), resInfo);
        Dimension size = GraphUtilities.getAutoNodeDimension(gm, value);
        GraphUtilities.resizeCell(size, gm.getInstanceGraph(), cell);
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

    public static Dimension getAutoLiteralNodeDimension(GraphManager gmanager, String value) {
        if (!gmanager.isAutoNodeSize()) {
            return new Dimension(MR3CellMaker.CELL_WIDTH, MR3CellMaker.CELL_HEIGHT);
        }
        if (defaultFont == null) {
            defaultFont = gmanager.getInstanceGraph().getFont();
        }
        StringTokenizer tokenizer = new StringTokenizer(value, "\n");
        int width = MR3CellMaker.DEFAULT_CELL_WIDTH / 3;
        int height = MR3CellMaker.CELL_MARGIN;
        FontMetrics fm = gmanager.getInstanceGraph().getFontMetrics(defaultFont);
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

    public static String getQName(Resource uri) {
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
            defaultFont = gmanager.getInstanceGraph().getFont();
        }
        FontMetrics fm = gmanager.getInstanceGraph().getFontMetrics(defaultFont);
        int width = fm.stringWidth(value) * 2;
        if (value.length() == 0) {
            width = ANON_NODE_WIDTH;
        }
        int height = fm.getHeight() + MR3CellMaker.CELL_MARGIN;
        return new Dimension(width, height);
    }


    public static void emphasisNodes(RDFGraph graph) {
        Object[] cells = graph.getSelectionCells();
        for (Object cell1 : cells) {
            GraphCell cell = (GraphCell) cell1;
            AttributeMap map = cell.getAttributes();
            editCell(cell, map, graph);
        }
    }

    public static void resetEditorBackgroudColor(GraphManager gmanager) {
        if (GraphUtilities.isBlackAndWhite) {
            gmanager.getInstanceGraph().setBackground(Editor.DEFAUlT_BACKGROUND_COLOR);
            gmanager.getClassGraph().setBackground(Editor.DEFAUlT_BACKGROUND_COLOR);
            gmanager.getPropertyGraph().setBackground(Editor.DEFAUlT_BACKGROUND_COLOR);
        } else {
            gmanager.getInstanceGraph().resetBackground();
            gmanager.getClassGraph().resetBackground();
            gmanager.getPropertyGraph().resetBackground();
        }
    }

    public static void changeAllCellColor(GraphManager gmanager) {
        RDFGraph rdfGraph = gmanager.getInstanceGraph();
        RDFGraph classGraph = gmanager.getClassGraph();
        RDFGraph propertyGraph = gmanager.getPropertyGraph();

        if (rdfGraph == null || classGraph == null || propertyGraph == null) {
            return;
        }

        Object[] cells = rdfGraph.getAllCells();
        for (Object cell2 : cells) {
            if (cell2 instanceof RDFCellStyleChanger changer) {
                changer.changeDefaultCellStyle(rdfGraph);
            }
        }

        cells = classGraph.getAllCells();
        for (Object cell1 : cells) {
            if (cell1 instanceof RDFCellStyleChanger changer) {
                changer.changeDefaultCellStyle(classGraph);
            }
        }

        cells = propertyGraph.getAllCells();
        for (Object cell : cells) {
            if (cell instanceof RDFCellStyleChanger changer) {
                changer.changeDefaultCellStyle(propertyGraph);
            }
        }
        if (gmanager.isShowTypeCell()) {
            gmanager.removeTypeCells();
            gmanager.addTypeCells();
        }
    }

    public static void selectCellSet(GraphManager gmanager, Set<RDFNode> rdfNodeSet) {
        gmanager.getInstanceGraph().clearSelection();
        gmanager.getClassGraph().clearSelection();
        gmanager.getPropertyGraph().clearSelection();
        Set<GraphCell> cellSet = new HashSet<>();
        for (RDFNode node : rdfNodeSet) {
            if (node.isResource()) {
                cellSet.addAll(gmanager.findRDFResourceSet(node.asResource().getURI()));
                cellSet.addAll(gmanager.findRDFSResourceSet(node.asResource().getURI(),
                        gmanager.getClassGraph()));
                cellSet.addAll(gmanager.findRDFSResourceSet(node.asResource().getURI(),
                        gmanager.getPropertyGraph()));
            } else if (node.isLiteral()) {
                cellSet.addAll(gmanager.findRDFResourceSet(node.asLiteral().getString()));
                cellSet.addAll(gmanager.findRDFSResourceSet(node.asLiteral().getString(),
                        gmanager.getClassGraph()));
                cellSet.addAll(gmanager.findRDFSResourceSet(node.asLiteral().getString(),
                        gmanager.getPropertyGraph()));
            }
        }
        cellSet.forEach(c -> {
            gmanager.selectCell(c, gmanager.getInstanceGraph());
            gmanager.selectCell(c, gmanager.getClassGraph());
            gmanager.selectCell(c, gmanager.getPropertyGraph());
        });
    }

}