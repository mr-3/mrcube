/**
 * GraphLayoutData.java
 * 
 * Copyright (C) 2003 The MMM Project 
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package org.semanticweb.mmm.mr3.layout;

import java.awt.*;
import java.awt.geom.*;
import java.util.*;

import org.jgraph.graph.*;
import org.semanticweb.mmm.mr3.jgraph.*;
import org.semanticweb.mmm.mr3.util.*;

import com.hp.hpl.jena.rdf.model.*;

/**
 * @author takeshi morita
 * 
 */
public class GraphLayoutData {

    private RDFNode rdfNode;
    private Point2D.Double point;
    private Dimension dimension;
    private Rectangle2D rec;
    private RDFGraph graph;
    private GraphCell cell;
    private Set<GraphLayoutData> children;
    private boolean hasParent;
    TreeAlgorithm.TreeAlgorithmData data;

    public GraphLayoutData(RDFNode node, Dimension dim) {
        rdfNode = node;
        hasParent = false;
        children = new HashSet<GraphLayoutData>();
        point = new Point2D.Double(0, 0);
        dimension = dim;
    }

    public GraphLayoutData(RDFNode node) {
        rdfNode = node;
        hasParent = false;
        children = new HashSet<GraphLayoutData>();
        point = new Point2D.Double(0, 0);
        dimension = new Dimension(MR3CellMaker.CELL_WIDTH, MR3CellMaker.CELL_HEIGHT);
    }

    public RDFNode getRDFNode() {
        return rdfNode;
    }

    public GraphLayoutData(GraphCell cell, RDFGraph graph) {
        this.cell = cell;
        this.graph = graph;
        children = new HashSet<GraphLayoutData>();
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