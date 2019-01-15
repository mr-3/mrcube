package org.mrcube.layout;

import org.jgraph.JGraph;
import org.jgraph.graph.CellView;
import org.jgraph.graph.GraphConstants;
import org.jgraph.graph.GraphModel;
import org.jgraph.graph.VertexView;

import java.awt.*;
import java.util.List;
import java.util.*;

/***************************************************************************
 * layout,attachParent,layoutLeaf,join,merge,offset and bridge methods below
 * were taken line by line from Moen's algorithm.
 *
 * Attempts to understand the above methods without reading the paper first
 * are strongly discouraged.
 *
 * http://www.computer.org/software/so1990/s4021abs.htm
 *
 *
 * modified by
 *
 * @author Takeshi Morita
 **************************************************************************/
public class TreeLayoutAlgorithm {

    private static final Object CELL_WRAPPER = new Object();
    private static final int LEFT_TO_RIGHT = 0;
    private static final int UP_TO_DOWN = 1;
    private static final int DEFAULT_ORIENTATION = LEFT_TO_RIGHT;

    // public ProgressDialog dlgProgress = new ProgressDialog((Frame) null,
    // "Progress:", false);

    private JGraph jgraph;
    private int orientation;
    private int childParentDistance;
    private final int BORDER;

    public TreeLayoutAlgorithm(int orientation, int distance, int border) {
        BORDER = border;
        setChildParentDistance(distance);
        setLayoutOrientation(orientation);
    }

    private void setLayoutOrientation(int orientation) {
        if (orientation < 0 && orientation > 1) {
            orientation = DEFAULT_ORIENTATION;
        } else {
            this.orientation = orientation;
        }
    }

    private void setChildParentDistance(int distance) {
        if (distance <= 0) throw new IllegalArgumentException("Distance has to be positive integer " + distance);
        childParentDistance = distance;
    }

    private void layout(TreeLayoutNode t, Set nodeSet) {
        TreeLayoutNode c;

        if (t == null) {
            return;
        }

        c = t.child;
        while (c != null) {
            // if (nodeSet.contains(c)) {
            // break;
            // } else {
            // nodeSet.add(c);
            // }
            layout(c, nodeSet);
            c = c.sibling;
        }

        if (t.child != null) {
            attachParent(t, join(t));
        } else {
            layoutLeaf(t);
        }
    }

    private void attachParent(TreeLayoutNode t, int h) {
        final int x;
        int y1;
        final int y2;

        x = t.border + childParentDistance;
        y2 = (h - t.height) / 2 - t.border;
        y1 = y2 + t.height + 2 * t.border - h;
        t.child.offset.x = x + t.width;
        t.child.offset.y = y1;
        t.contour.upper_head = new PolyLine(t.width, 0, new PolyLine(x, y1, t.contour.upper_head));
        t.contour.lower_head = new PolyLine(t.width, 0, new PolyLine(x, y2, t.contour.lower_head));
    }

    private void layoutLeaf(TreeLayoutNode t) {
        t.contour.upper_tail = new PolyLine(t.width + 2 * t.border, 0, null);
        t.contour.upper_head = t.contour.upper_tail;
        t.contour.lower_tail = new PolyLine(0, -t.height - 2 * t.border, null);
        t.contour.lower_head = new PolyLine(t.width + 2 * t.border, 0, t.contour.lower_tail);
    }

    private int join(TreeLayoutNode t) {
        TreeLayoutNode c;
        int d, h, sum;

        c = t.child;
        t.contour = c.contour;
        sum = h = c.height + 2 * c.border;
        c = c.sibling;
        while (c != null) {
            d = merge(t.contour, c.contour);
            c.offset.y = d + h;
            c.offset.x = 0;
            h = c.height + 2 * c.border;
            sum += d + h;
            c = c.sibling;
        }

        return sum;
    }

    private int merge(Polygon c1, Polygon c2) {
        int x, y, total, d;
        PolyLine lower, upper, b;

        x = y = total = 0;
        upper = c1.lower_head;
        lower = c2.upper_head;

        while (lower != null && upper != null) {

            d = offset(x, y, lower.dx, lower.dy, upper.dx, upper.dy);
            y += d;
            total += d;

            if (x + lower.dx <= upper.dx) {
                y += lower.dy;
                x += lower.dx;
                lower = lower.link;
            } else {
                y -= upper.dy;
                x -= upper.dx;
                upper = upper.link;
            }
        }

        if (lower != null) {
            b = bridge(c1.upper_tail, 0, 0, lower, x, y);
            c1.upper_tail = (b.link != null) ? c2.upper_tail : b;
            c1.lower_tail = c2.lower_tail;
        } else {
            b = bridge(c2.lower_tail, x, y, upper, 0, 0);
            if (b.link == null) {
                c1.lower_tail = b;
            }
        }

        c1.lower_head = c2.lower_head;

        return total;
    }

    private int offset(int p1, int p2, int a1, int a2, int b1, int b2) {
        int d, s, t;

        if (b1 <= p1 || p1 + a1 <= 0) {
            return 0;
        }

        t = b1 * a2 - a1 * b2;
        if (t > 0) {
            if (p1 < 0) {
                s = p1 * a2;
                d = s / a1 - p2;
            } else if (p1 > 0) {
                s = p1 * b2;
                d = s / b1 - p2;
            } else {
                d = -p2;
            }
        } else if (b1 < p1 + a1) {
            s = (b1 - p1) * a2;
            d = b2 - (p2 + s / a1);
        } else if (b1 > p1 + a1) {
            s = (a1 + p1) * b2;
            d = s / b1 - (p2 + a2);
        } else {
            d = b2 - (p2 + a2);
        }

        if (d > 0) {
            return d;
        }
        return 0;
    }

    private PolyLine bridge(PolyLine line1, int x1, int y1, PolyLine line2, int x2, int y2) {
        int dy, dx, s;
        PolyLine r;

        dx = x2 + line2.dx - x1;
        if (line2.dx == 0) {
            dy = line2.dy;
        } else {
            s = dx * line2.dy;
            dy = s / line2.dx;
        }

        r = new PolyLine(dx, dy, line2.link);
        line1.link = new PolyLine(0, y2 + line2.dy - dy - y1, r);

        return r;
    }

    private void leftRightNodeLayout(TreeLayoutNode node, int off_x, int off_y) {
        TreeLayoutNode child, s;
        int siblingOffest;

        node.pos.translate(off_x + node.offset.x, off_y + node.offset.y);
        child = node.child; // topmost child

        if (child != null) {
            leftRightNodeLayout(child, node.pos.x, node.pos.y);
            s = child.sibling;
            siblingOffest = node.pos.y + child.offset.y;
            while (s != null) {
                leftRightNodeLayout(s, node.pos.x + child.offset.x, siblingOffest);
                siblingOffest += s.offset.y;
                s = s.sibling;
            }
        }
    }

    private void upDownNodeLayout(TreeLayoutNode node, int off_x, int off_y) {
        TreeLayoutNode child, s;
        int siblingOffset;

        node.pos.translate(off_x + (-1 * node.offset.y), off_y + node.offset.x);
        child = node.child; // rightmost child
        if (child != null) {
            upDownNodeLayout(child, node.pos.x, node.pos.y);
            s = child.sibling;
            siblingOffset = node.pos.x - child.offset.y; // add -child.width
            while (s != null) {
                upDownNodeLayout(s, siblingOffset, node.pos.y + child.offset.x);
                siblingOffset -= s.offset.y;
                s = s.sibling;
            }
        }
    }

    // うまく動かない
    // private Point2D.Double getStartPoint(Collection roots) {
    // Point2D.Double startPoint = new Point2D.Double(0, 0);
    // for (Iterator i = roots.iterator(); i.hasNext();) {
    // CellView cellView = (CellView) i.next();
    // Rectangle rect = cellView.getBounds();
    // Point dataPoint = rect.getLocation();
    // if (dataPoint.x < startPoint.x) {
    // startPoint.x = dataPoint.x;
    // }
    // if (dataPoint.y < startPoint.y) {
    // startPoint.y = dataPoint.y;
    // }
    // }
    // return startPoint;
    // }

    // うまく動かない
    // public void centralizeGraph(List roots) {
    // Point2D.Double startPoint = getStartPoint(roots);
    // Point2D.Double revisePoint = new Point2D.Double(0, 0);
    // GraphLayoutUtilities.setRevisePoint(revisePoint, startPoint);
    // for (Iterator i = roots.iterator(); i.hasNext();) {
    // VertexView vertexView = (VertexView)i.next();
    // Rectangle rect = vertexView.getBounds();
    // Point point = rect.getLocation();
    // Point newPoint = new Point((int)(point.x+revisePoint.x),
    // (int)(point.y+revisePoint.y));
    // TreeLayoutNode node = getTreeLayoutNode(vertexView);
    // node.pos.x = newPoint.x;
    // node.pos.y = newPoint.y;
    // }
    // }

    public void perform(JGraph jgraph, Object[] selectedCells) {

        this.jgraph = jgraph;
        CellView[] selectedCellViews = jgraph.getGraphLayoutCache().getMapping(selectedCells);

        // dlgProgress.setVisible(true);

        List roots = Arrays.asList(selectedCellViews);

        // ---------------------------------------------------
        // Addition from Sven Luzar
        // ---------------------------------------------------
        // roots = VertexViews + EdgeViews
        // remove the Non Vertex Views to make the
        // Algorithm runnable
        // for (int i = roots.size() - 1; i >= 0; i--) {
        // Object o = roots.get(i);
        // if (!(o instanceof VertexView)) {
        // roots.remove(i);
        // }
        // }
        // ---------------------------------------------------

        buildLayoutHelperTree(roots);
        layoutTrees(roots);
        display(roots);

        // dlgProgress.setVisible(false);
    }

    private List getChildren(VertexView node) {
        ArrayList children = new ArrayList();
        Object vertex = node.getCell();
        GraphModel model = jgraph.getModel();
        int portCount = model.getChildCount(vertex);

        // iterate any NodePort
        for (int i = 0; i < portCount; i++) {
            Object port = model.getChild(vertex, i);

            // iterate any Edge in the port
            Iterator itrEdges = model.edges(port);

            while (itrEdges.hasNext()) {
                Object edge = itrEdges.next();

                // if the Edge is a forward edge we should follow this edge
                if (port == model.getSource(edge)) {
                    Object targetPort = model.getTarget(edge);
                    Object targetVertex = model.getParent(targetPort);
                    VertexView targetVertexView = (VertexView) jgraph.getGraphLayoutCache().getMapping(targetVertex,
                            false);
                    children.add(targetVertexView);
                }
            }
        }
        return children;
    }

    private void layoutTrees(Collection roots) {
        for (Object root1 : roots) {
            VertexView view = (VertexView) root1;
            TreeLayoutNode root = getTreeLayoutNode(view);

            // kick off Moen's algorithm
            Set nodeSet = new HashSet();
            layout(root, nodeSet);
            Point rootPos = view.getBounds().getBounds().getLocation();
            switch (orientation) {
                case LEFT_TO_RIGHT:
                    leftRightNodeLayout(root, rootPos.x, rootPos.y);
                    break;
                case UP_TO_DOWN:
                    upDownNodeLayout(root, rootPos.x, rootPos.y);
                    break;
                default:
                    leftRightNodeLayout(root, rootPos.x, rootPos.y);
            }
        }
    }

    private void buildLayoutHelperTree(Collection roots) {
        for (Object root : roots) {
            VertexView vv = (VertexView) root;
            decorateNode(vv);
        }
    }

    private void decorateNode(VertexView node) {
        List cl = getChildren(node);
        TreeLayoutNode parent = getTreeLayoutNode(node);
        if (cl.size() > 0) {
            // Decorate children with Moen's nodes, parent has a reference
            // to a first child only. Each child has a reference to a parent
            // and possible next sibling
            for (int i = 0; i < cl.size(); i++) {
                VertexView currentChild = (VertexView) cl.get(i);
                TreeLayoutNode cln = getTreeLayoutNode(currentChild);
                if (i == 0) {
                    parent.child = cln;
                } else {
                    VertexView previousChild = (VertexView) cl.get(i - 1);
                    TreeLayoutNode pln = getTreeLayoutNode(previousChild);
                    pln.sibling = cln;
                }
                cln.parent = parent;
                decorateNode(currentChild);
            }
        }
    }

    private TreeLayoutNode getTreeLayoutNode(VertexView view) {
        return getTreeLayoutNode(view, true);
    }

    private TreeLayoutNode getTreeLayoutNode(VertexView view, boolean createIfNotPresent) {
        TreeLayoutNode decor = (TreeLayoutNode) view.getAttributes().get(CELL_WRAPPER);
        if (decor == null && createIfNotPresent) {
            TreeLayoutNode n = new TreeLayoutNode(view);
            view.getAttributes().put(CELL_WRAPPER, n);
            return n;
        }
        return decor;
    }

    private void display(Collection roots) {
        for (Object root : roots) {
            VertexView vertexView = (VertexView) root;
            displayHelper(vertexView);
        }
    }

    private void displayHelper(VertexView view) {
        TreeLayoutNode node = getTreeLayoutNode(view);

        Object cell = view.getCell();
        Map attributes = GraphConstants.createAttributes(cell, GraphConstants.BOUNDS, new Rectangle(node.pos,
                new Dimension(node.width, node.height)));

        jgraph.getGraphLayoutCache().edit(attributes, null, null, null);

        List c = getChildren(view);
        for (Object o : c) {
            VertexView vertexView = (VertexView) o;
            displayHelper(vertexView);
        }
        view.getAttributes().remove(CELL_WRAPPER);
    }

    private class TreeLayoutNode {
        TreeLayoutNode parent, child, sibling;
        final int width;
        final int height;
        final int border;
        final Point pos;
        final Point offset;
        Polygon contour;

        TreeLayoutNode(VertexView node) {
            width = (int) node.getBounds().getWidth();
            height = (int) node.getBounds().getHeight();
            // border = 5;
            border = BORDER;
            pos = new Point();
            offset = new Point();
            contour = new Polygon();
        }
    }

    private static class Polygon {
        PolyLine lower_head, lower_tail;
        PolyLine upper_head, upper_tail;
    }

    private static class PolyLine {
        final int dx;
        final int dy;
        PolyLine link;

        PolyLine(int dx, int dy, PolyLine link) {
            this.dx = dx;
            this.dy = dy;
            this.link = link;
        }
    }
}