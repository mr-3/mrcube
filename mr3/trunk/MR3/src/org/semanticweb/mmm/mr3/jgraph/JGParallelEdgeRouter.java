/*
 * Copyright (c) 2004-2005 France Telecom
 * Copyright (c) 2004 Gaudenz Alder
 * Copyright (c) 2005 David Benson
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice,
 *   this list of conditions and the following disclaimer.
 * - Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 * - Neither the name of JGraph nor the names of its contributors may be used
 *   to endorse or promote products derived from this software without specific
 *   prior written permission.
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package org.semanticweb.mmm.mr3.jgraph;

import java.awt.geom.*;
import java.util.*;

import org.jgraph.graph.*;

/**
 * Algorithm which create intermediates points for parallel edges
 * 
 * @author fgendre
 */
public class JGParallelEdgeRouter implements Edge.Routing {

    /**
     * Singleton to reach parallel edge router
     */
    private static final JGParallelEdgeRouter sharedInstance = new JGParallelEdgeRouter();

    /**
     * Default model
     */
    private static final GraphModel emptyModel = new DefaultGraphModel();

    /**
     * Distance between each parallel edge
     */
    private static double edgeSeparation = 10.;

    /**
     * Distance between intermediate and source/target points
     */
    private static double edgeDeparture = 10.;

    /**
     * Getter for singleton managing parallel edges
     * 
     * @return JGraphParallelEdgeRouter Routeur for parallel edges
     */
    public static JGParallelEdgeRouter getSharedInstance() {
        return JGParallelEdgeRouter.sharedInstance;
    }

    /**
     * Calc of intermediates points
     * 
     * @param edge
     *            Edge for which routing is demanding
     * @param points
     *            List of points for this edge
     */
    public void route(EdgeView edge, java.util.List points) {
        // Check presence of source/target nodes
        if ((null == edge.getSource()) || (null == edge.getTarget()) || (null == edge.getSource().getParentView())
                || (null == edge.getTarget().getParentView())) { return; }

        // Check presence of parallel edges
        Object[] edges = getParallelEdges(edge);
        if (edges == null) { return; }

        // For one edge, no intermediate point
        if (edges.length < 2) {
            if (points.size() > 3) {
                points.remove(1);
                points.remove(2);
            } else if (points.size() > 2) {
                points.remove(1);
            }
            return;
        }

        // Looking for position of edge
        int numEdges = edges.length;
        int position = 0;
        for (int i = 0; i < edges.length; i++) {
            Object e = edges[i];
            if (e == edge.getCell()) {
                position = i + 1;
            }
        }

        // Looking for position of source/target nodes (edge=>port=>vertex)
        VertexView nodeFrom = (VertexView) edge.getSource().getParentView();
        VertexView nodeTo = (VertexView) edge.getTarget().getParentView();
        Point2D from = nodeFrom.getCenterPoint();
        Point2D to = nodeTo.getCenterPoint();

        if (from != null && to != null) {
            double dy = from.getY() - to.getY();
            double dx = from.getX() - to.getX();

            // Calc of radius
            double m = dy / dx;
            double theta = Math.atan(-1 / m);
            double rx = dx / Math.sqrt(dx * dx + dy * dy);
            double ry = dy / Math.sqrt(dx * dx + dy * dy);

            // Memorize size of source/target nodes
            double sizeFrom = Math.max(nodeFrom.getBounds().getWidth(), nodeFrom.getBounds().getHeight()) / 2.;
            double sizeTo = Math.max(nodeTo.getBounds().getWidth(), nodeTo.getBounds().getHeight()) / 2.;

            // Calc position of central point
            double edgeMiddleDeparture = (Math.sqrt(dx * dx + dy * dy) - sizeFrom - sizeTo) / 2 + sizeFrom;

            // Calc position of intermediates points
            double edgeFromDeparture = edgeDeparture + sizeFrom;
            double edgeToDeparture = edgeDeparture + sizeTo;

            // Calc distance between edge and mediane source/target
            double r = edgeSeparation * Math.floor(position / 2);
            if (0 == (position % 2)) {
                r = -r;
            }

            // Convert coordinate
            double ex = r * Math.cos(theta);
            double ey = r * Math.sin(theta);

            // Check if is not better to have only one intermediate point
            if (edgeMiddleDeparture <= edgeFromDeparture) {
                double midX = from.getX() - rx * edgeMiddleDeparture;
                double midY = from.getY() - ry * edgeMiddleDeparture;

                Point2D controlPoint = new Point2D.Double(ex + midX, ey + midY);

                // Add intermediate point
                if (points.size() == 2) {
                    points.add(1, controlPoint);
                } else if (points.size() == 3) {
                    points.set(1, controlPoint);
                } else {
                    points.set(1, controlPoint);
                    points.remove(2);
                }
            } else {
                double midXFrom = from.getX() - rx * edgeFromDeparture;
                double midYFrom = from.getY() - ry * edgeFromDeparture;
                double midXTo = to.getX() + rx * edgeToDeparture;
                double midYTo = to.getY() + ry * edgeToDeparture;

                Point2D controlPointFrom = new Point2D.Double(ex + midXFrom, ey + midYFrom);
                Point2D controlPointTo = new Point2D.Double(ex + midXTo, ey + midYTo);

                // Add intermediates points
                if (points.size() == 2) {
                    points.add(1, controlPointFrom);
                    points.add(2, controlPointTo);
                } else if (points.size() == 3) {
                    points.set(1, controlPointFrom);
                    points.add(2, controlPointTo);
                } else {
                    points.set(1, controlPointFrom);
                    points.set(2, controlPointTo);
                }
            }
        }
    }

    /**
     * Getter to obtain the distance between each parallel edge
     * 
     * @return Distance
     */
    public static double getEdgeSeparation() {
        return JGParallelEdgeRouter.edgeSeparation;
    }

    /**
     * Setter to define distance between each parallel edge
     * 
     * @param edgeSeparation
     *            New distance
     */
    public static void setEdgeSeparation(double edgeSeparation) {
        JGParallelEdgeRouter.edgeSeparation = edgeSeparation;
    }

    /**
     * Getter to obtain the distance between intermediate and source/target
     * points
     * 
     * @return Distance
     */
    public static double getEdgeDeparture() {
        return JGParallelEdgeRouter.edgeDeparture;
    }

    /**
     * Setter to define distance between intermediate and source/target points
     * 
     * @param edgeDeparture
     *            New distance
     */
    public static void setEdgeDeparture(double edgeDeparture) {
        JGParallelEdgeRouter.edgeDeparture = edgeDeparture;
    }

    /**
     * Getter to obtain the list of parallel edges
     * 
     * @param edge
     *            Edge on which one wants to know parallel edges
     * @return Object[] Array of parallel edges (include edge passed on
     *         argument)
     */
    private Object[] getParallelEdges(EdgeView edge) {
        // FIXME: The model is stored in the cells only in the default
        // implementations. Otherwise we must use the real model here.
        return DefaultGraphModel.getEdgesBetween(emptyModel, edge.getSource().getParentView().getCell(), edge
                .getTarget().getParentView().getCell(), false);
    }

    public int getPreferredLineStyle(EdgeView arg0) {
        // TODO 自動生成されたメソッド・スタブ
        return 0;
    }

    public List route(EdgeView edge) {
        List points = new ArrayList();
        // Check presence of source/target nodes
        if ((null == edge.getSource()) || (null == edge.getTarget()) || (null == edge.getSource().getParentView())
                || (null == edge.getTarget().getParentView())) { return points; }

        // Check presence of parallel edges
        Object[] edges = getParallelEdges(edge);
        if (edges == null) { return points; }

        // For one edge, no intermediate point
        if (edges.length < 2) {
            if (points.size() > 3) {
                points.remove(1);
                points.remove(2);
            } else if (points.size() > 2) {
                points.remove(1);
            }
            return points;
        }

        // Looking for position of edge
        int numEdges = edges.length;
        int position = 0;
        for (int i = 0; i < edges.length; i++) {
            Object e = edges[i];
            if (e == edge.getCell()) {
                position = i + 1;
            }
        }

        // Looking for position of source/target nodes (edge=>port=>vertex)
        VertexView nodeFrom = (VertexView) edge.getSource().getParentView();
        VertexView nodeTo = (VertexView) edge.getTarget().getParentView();
        Point2D from = nodeFrom.getCenterPoint();
        Point2D to = nodeTo.getCenterPoint();

        if (from != null && to != null) {
            double dy = from.getY() - to.getY();
            double dx = from.getX() - to.getX();

            // Calc of radius
            double m = dy / dx;
            double theta = Math.atan(-1 / m);
            double rx = dx / Math.sqrt(dx * dx + dy * dy);
            double ry = dy / Math.sqrt(dx * dx + dy * dy);

            // Memorize size of source/target nodes
            double sizeFrom = Math.max(nodeFrom.getBounds().getWidth(), nodeFrom.getBounds().getHeight()) / 2.;
            double sizeTo = Math.max(nodeTo.getBounds().getWidth(), nodeTo.getBounds().getHeight()) / 2.;

            // Calc position of central point
            double edgeMiddleDeparture = (Math.sqrt(dx * dx + dy * dy) - sizeFrom - sizeTo) / 2 + sizeFrom;

            // Calc position of intermediates points
            double edgeFromDeparture = edgeDeparture + sizeFrom;
            double edgeToDeparture = edgeDeparture + sizeTo;

            // Calc distance between edge and mediane source/target
            double r = edgeSeparation * Math.floor(position / 2);
            if (0 == (position % 2)) {
                r = -r;
            }

            // Convert coordinate
            double ex = r * Math.cos(theta);
            double ey = r * Math.sin(theta);

            // Check if is not better to have only one intermediate point
            if (edgeMiddleDeparture <= edgeFromDeparture) {
                double midX = from.getX() - rx * edgeMiddleDeparture;
                double midY = from.getY() - ry * edgeMiddleDeparture;

                Point2D controlPoint = new Point2D.Double(ex + midX, ey + midY);

                // Add intermediate point
                if (points.size() == 2) {
                    points.add(1, controlPoint);
                } else if (points.size() == 3) {
                    points.set(1, controlPoint);
                } else {
                    points.set(1, controlPoint);
                    points.remove(2);
                }
            } else {
                double midXFrom = from.getX() - rx * edgeFromDeparture;
                double midYFrom = from.getY() - ry * edgeFromDeparture;
                double midXTo = to.getX() + rx * edgeToDeparture;
                double midYTo = to.getY() + ry * edgeToDeparture;

                Point2D controlPointFrom = new Point2D.Double(ex + midXFrom, ey + midYFrom);
                Point2D controlPointTo = new Point2D.Double(ex + midXTo, ey + midYTo);

                // Add intermediates points
                if (points.size() == 2) {
                    points.add(1, controlPointFrom);
                    points.add(2, controlPointTo);
                } else if (points.size() == 3) {
                    points.set(1, controlPointFrom);
                    points.add(2, controlPointTo);
                } else {
                    points.set(1, controlPointFrom);
                    points.set(2, controlPointTo);
                }
            }
        }
        return points;
    }

    @Override
    public List route(GraphLayoutCache arg0, EdgeView arg1) {
        // TODO Auto-generated method stub
        return null;
    }

}