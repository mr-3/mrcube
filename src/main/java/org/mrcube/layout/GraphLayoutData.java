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

package org.mrcube.layout;

import org.apache.jena.rdf.model.RDFNode;
import org.jgraph.graph.AttributeMap;
import org.jgraph.graph.GraphCell;
import org.jgraph.graph.GraphConstants;
import org.mrcube.jgraph.RDFGraph;
import org.mrcube.utils.GraphUtilities;
import org.mrcube.utils.MR3CellMaker;

import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author Takeshi Morita
 * 
 */
public class GraphLayoutData {

    private RDFNode rdfNode;
    private final Point2D.Double point;
    private final Dimension dimension;
    private Rectangle2D rec;
    private RDFGraph graph;
    private GraphCell cell;
    private final Set<GraphLayoutData> children;
    private boolean hasParent;
    TreeAlgorithm.TreeAlgorithmData data;

    public GraphLayoutData(RDFNode node, Dimension dim) {
        rdfNode = node;
        hasParent = false;
        children = new HashSet<>();
        point = new Point2D.Double(0, 0);
        dimension = dim;
    }

    public GraphLayoutData(RDFNode node) {
        rdfNode = node;
        hasParent = false;
        children = new HashSet<>();
        point = new Point2D.Double(0, 0);
        dimension = new Dimension(MR3CellMaker.CELL_WIDTH, MR3CellMaker.CELL_HEIGHT);
    }

    public RDFNode getRDFNode() {
        return rdfNode;
    }

    public GraphLayoutData(GraphCell cell, RDFGraph graph) {
        this.cell = cell;
        this.graph = graph;
        children = new HashSet<>();
        Map map = cell.getAttributes();
        rec = GraphConstants.getBounds(map);
        point = new Point2D.Double(rec.getX(), rec.getY());
        hasParent = false;
        dimension = new Dimension((int) rec.getWidth(), (int) rec.getHeight());
    }

    public Point2D.Double getPosition() {
        return point;
    }

    public GraphCell getCell() {
        return cell;
    }

    public void setPosition(double x, double y) {
        point.x = x;
        point.y = y;
    }

    public void setBoundingBox(int width, int height) {
        dimension.setSize(width, height);
    }

    public Dimension getBoundingBox() {
        return dimension;
    }

    public Rectangle getRectangle() {
        return new Rectangle((int) point.x, (int) point.y, dimension.width, dimension.height);
    }

    public void addChild(GraphLayoutData child) {
        children.add(child);
    }

    public Set<GraphLayoutData> getChildren() {
        return Collections.unmodifiableSet(children);
    }

    public int getChildCount() {
        return children.size();
    }

    public void setHasParent(boolean t) {
        hasParent = t;
    }

    public boolean hasParent() {
        return hasParent;
    }

    private static final int cellHeight = 25;

    public void setRealTypePosition(GraphCell typeCell) {
        rec.getBounds().x = (int) point.x;
        rec.getBounds().y = (int) (point.y + cellHeight);
        rec.getBounds().height = cellHeight;
        setRealPosition(typeCell);
    }

    public void setRealResourcePosition() {
        rec.getBounds().x = (int) point.x;
        rec.getBounds().y = (int) point.y;

        setRealPosition(cell);
    }

    private void setRealPosition(GraphCell localCell) {
        AttributeMap map = localCell.getAttributes();
        GraphConstants.setBounds(map, rec);
        GraphUtilities.editCell(localCell, map, graph);
    }

    public String toString() {
        String msg = "";
        msg += " Position: x: " + point.x + "y: " + point.y;
        msg += " Dimension: width: " + dimension.width + "height: " + dimension.height;

        return msg;
    }
}