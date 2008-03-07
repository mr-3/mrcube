package org.semanticweb.mmm.mr3.util;

/*

 * @(#)GPConverter.java 1.2 11/11/02
 * 
 * Copyright (C) 2001 Gaudenz Alder
 * 
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation; either version 2.1 of the License, or (at your
 * option) any later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 * modified by takeshi morita
 *  
 */

import java.awt.*;
import java.awt.geom.*;
import java.awt.image.*;
import java.text.*;
import java.util.*;

import org.jgraph.graph.*;
import org.semanticweb.mmm.mr3.jgraph.*;

public class GPConverter {

    /**
     * 
     * JGraphpad: package org.jgraph.pad.actions.FileExportPDF
     * 
     */
    /*
     * public static void toPDF(RDFGraph graph, File file) { Object[] cells =
     * graph.getAllCells(); if (cells.length > 0) { try { Document document =
     * new Document();
     * 
     * PdfWriter writer = PdfWriter.getInstance(document, new
     * FileOutputStream(file)); document.open();
     * 
     * Rectangle2D bounds = graph.getCellBounds(cells); graph.toScreen(bounds);
     * Dimension d = bounds.getBounds().getSize();
     * 
     * Object[] selection = graph.getSelectionCells(); boolean gridVisible =
     * graph.isGridVisible(); boolean doubleBuffered = graph.isDoubleBuffered();
     * graph.setGridVisible(false); graph.setDoubleBuffered(false);
     * graph.clearSelection();
     * 
     * PdfContentByte cb = writer.getDirectContent(); cb.saveState();
     * cb.concatCTM(1, 0, 0, 1, 50, 400); Graphics2D g2 =
     * cb.createGraphics(d.width + 10, d.height + 10);
     * 
     * g2.setColor(graph.getBackground()); g2.fillRect(0, 0, d.width + 10,
     * d.height + 10); g2.translate(-bounds.getX() + 5, -bounds.getY() + 5);
     * 
     * graph.paint(g2);
     * 
     * graph.setSelectionCells(selection); graph.setGridVisible(gridVisible);
     * graph.setDoubleBuffered(doubleBuffered);
     * 
     * g2.dispose(); cb.restoreState(); document.close(); } catch (Exception e) {
     * e.printStackTrace(); } } }
     */
    /**
     * 
     * JGraphpad: package org.jgraph.pad.actions.FileExportEPS
     * 
     */
    // public static String toEPS(RDFGraph graph) {
    // Object[] cells = graph.getAllCells();
    // if (cells.length > 0) {
    // // File Output stream
    // EpsGraphics2D graphics = new EpsGraphics2D();
    // graphics.setColor(graph.getBackground());
    // Rectangle2D bounds = graph.getCellBounds(cells);
    // graph.toScreen(bounds);
    // Dimension d = bounds.getBounds().getSize();
    // graphics.fillRect(0, 0, d.width + 10, d.height + 10);
    // graphics.translate(-bounds.getX() + 5, -bounds.getY() + 5);
    //
    // Object[] selection = graph.getSelectionCells();
    // boolean gridVisible = graph.isGridVisible();
    // boolean doubleBuffered = graph.isDoubleBuffered();
    // graph.setGridVisible(false);
    // graph.setDoubleBuffered(false);
    // graph.clearSelection();
    //
    // // TODO: Remove the unsupported method stack trace
    // graph.paint(graphics);
    //
    // graph.setSelectionCells(selection);
    // graph.setGridVisible(gridVisible);
    // graph.setDoubleBuffered(doubleBuffered);
    //
    // return graphics.toString();
    // }
    // return "";
    // }
    // //
    // // Image Converter
    // //
    // Create a buffered image of the specified graph.
    public static BufferedImage toImage(RDFGraph graph) {
        Object[] cells = graph.getRoots();

        if (cells.length > 0) {
            Rectangle2D bounds = graph.getCellBounds(cells);
            graph.toScreen(bounds);

            // Create a Buffered Image
            Dimension d = bounds.getBounds().getSize();
            BufferedImage img = new BufferedImage(d.width + 10, d.height + 10, BufferedImage.TYPE_INT_RGB);
            Graphics2D graphics = img.createGraphics();
            graphics.setColor(graph.getBackground());
            graphics.fillRect(0, 0, img.getWidth(), img.getHeight());
            graphics.translate(-bounds.getBounds().getX() + 5, -bounds.getBounds().getY() + 5);

            Object[] selection = graph.getSelectionCells();
            boolean gridVisible = graph.isGridVisible();
            graph.setGridVisible(false);
            graph.clearSelection();

            graph.paint(graphics);

            graph.setSelectionCells(selection);
            graph.setGridVisible(gridVisible);

            return img;
        }
        return null;
    }

    //
    // GXL Converter
    //

    static transient Hashtable hash;

    // Create a GXL-representation for the specified cells.
    public static String toGXL(RDFGraph graph, Object[] cells) {
        hash = new Hashtable();
        String gxl = "<gxl><graph>";

        // Create external keys for nodes
        for (int i = 0; i < cells.length; i++)
            if (graph.isVertex(cells[i])) hash.put(cells[i], new Integer(hash.size()));

        // Convert Nodes
        Iterator it = hash.keySet().iterator();
        while (it.hasNext()) {
            Object node = it.next();
            gxl += vertexGXL(graph, hash.get(node), node);
        }

        // Convert Edges
        int edges = 0;
        for (int i = 0; i < cells.length; i++)
            if (RDFGraph.isEdge(cells[i])) gxl += edgeGXL(graph, new Integer(edges++), cells[i]);

        // Close main tags
        gxl += "\n</graph></gxl>";
        return gxl;
    }

    public static String vertexGXL(RDFGraph graph, Object id, Object vertex) {
        String label = graph.convertValueToString(vertex);
        return "\n\t<node id=\"node" + id.toString() + "\">" + "\n\t\t<attr name=\"Label\">" + "\n\t\t\t<string>"
                + label + "</string>" + "\n\t\t</attr>" + "\n\t</node>";
    }

    public static String edgeGXL(RDFGraph graph, Object id, Object edge) {
        GraphModel model = graph.getModel();
        String from = "";
        if (model.getSource(edge) != null) {
            Object source = hash.get(model.getParent(model.getSource(edge)));
            if (source != null) from = "node" + source.toString();
        }
        String to = "";
        if (model.getTarget(edge) != null) {
            Object target = hash.get(model.getParent(model.getTarget(edge)));
            if (target != null) to = "node" + target.toString();
        }
        if (from != null && to != null) {
            String label = graph.convertValueToString(edge);
            return "\n\t<edge id=\"edge" + id.toString() + "\"" + " from=\"" + from + "\"" + " to=\"" + to + "\">"
                    + "\n\t\t<attr name=\"Label\">" + "\n\t\t\t<string>" + label + "</string>" + "\n\t\t</attr>"
                    + "\n\t</edge>";
        }
        return "";
    }

    // Graphviz converter ===============================================
    /*
     * sample syntax: digraph G { subgraph cluster0 { node
     * [style=filled,color=white]; style=filled; color=lightgrey; a0 -> a1 -> a2 ->
     * a3; label = "process #1"; } subgraph cluster1 { node [style=filled]; b0 ->
     * b1 -> b2 -> b3; label = "process #2"; color=blue } start -> a0; start ->
     * b0; a1 -> b3; b2 -> a3; a3 -> a0; a3 -> end; b3 -> end; start
     * [shape=Mdiamond]; end [shape=Msquare]; }
     */
    public static String toGraphviz(RDFGraph graph, Object[] cells) {
        hash = new Hashtable();
        DateFormat dateformat = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG);
        String date = dateformat.format(new Date());
        StringBuffer gv = new StringBuffer("/* Graphviz file generated by " + "JGraph - " + date + " */"
                + "\n\ndigraph G {");

        // Create external keys for nodes
        for (int i = 0; i < cells.length; i++)
            if (graph.isVertex(cells[i])) hash.put(cells[i], new Integer(hash.size()));

        // Process Nodes
        Iterator it = hash.keySet().iterator();
        while (it.hasNext()) {
            Object node = it.next();
            gv.append(vertexGraphviz(graph, hash.get(node), node));
        }

        // Process Edges
        int edges = 0;
        for (int i = 0; i < cells.length; i++)
            if (RDFGraph.isEdge(cells[i])) gv.append(edgeGraphviz(graph, new Integer(edges++), cells[i]));

        // Close main tags
        gv.append("\n}");
        return gv.toString();
    }

    private static String vertexGraphviz(RDFGraph graph, Object id, Object vertex) {
        if (id == null) return "";
        String label = graph.convertValueToString(vertex);
        if (label == null) label = "";
        return "\n\t" + id.toString() + " [label=\"" + label + "\", " + "shape=\"box\"];";
    }

    private static String edgeGraphviz(RDFGraph graph, Object id, Object edge) {
        GraphModel model = graph.getModel();
        String from = null;

        Object es = model.getSource(edge);
        if (es != null) {
            Object ps = hash.get(model.getParent(es));
            if (ps != null) from = ps.toString();
            // if (es != null)
            // debug from = graph.convertValueToString(model.getParent(es));

            String to = null;
            Object et = model.getTarget(edge);
            if (et != null) {
                Object tp = hash.get(model.getParent(et));
                if (tp != null) to = tp.toString();
                // debug if (et != null)
                // to = graph.convertValueToString(model.getParent(et));
            }

            if (from != null && to != null) return "\n\t" + from + " -> " + to + ";";
        }
        return "";
    }
}