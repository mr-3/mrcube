/*
 * @(#)EllipseView.java	1.0 1/1/02
 *
 * Copyright (C) 2001 Gaudenz Alder
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 */

package jp.ac.shizuoka.cs.panda.mmm.mr3.jgraph;
import java.awt.*;

import org.jgraph.*;
import org.jgraph.graph.*;

public class EllipseView extends VertexView {

	public static EllipseRenderer renderer = new EllipseRenderer();

	public EllipseView(Object cell, JGraph graph, CellMapper cm) {
		super(cell, graph, cm);
	}

	/**
	 * Returns the intersection of the bounding rectangle and the
	 * straight line between the source and the specified point p.
	 * The specified point is expected not to intersect the bounds.
	 */
	public Point getPerimeterPoint(Point source, Point p) {
		// Compute relative bounds
		Rectangle r = getBounds();
		int x = r.x;
		int y = r.y;
		int a = (r.width + 1) / 2;
		int b = (r.height + 1) / 2;

		// Get center
		int xCenter = (int) (x + a);
		int yCenter = (int) (y + b);

		// Compute angle
		int dx = p.x - xCenter;
		int dy = p.y - yCenter;
		double t = Math.atan2(dy, dx);

		// Compute Perimeter Point
		int xout = xCenter + (int) (a * Math.cos(t)) - 1;
		int yout = yCenter + (int) (b * Math.sin(t)) - 1;

		// Return perimeter point
		return new Point(xout, yout);
	}

	public CellViewRenderer getRenderer() {
		return renderer;
	}

	public static class EllipseRenderer extends VertexRenderer {

		public void paint(Graphics g) {
			int b = borderWidth;
			Graphics2D g2 = (Graphics2D) g;
			Dimension d = getSize();
			boolean tmp = selected;
			if (super.isOpaque()) {
				g.setColor(super.getBackground());
				g.fillOval(b - 1, b - 1, d.width - b, d.height - b);
			}
			try {
				setBorder(null);
				setOpaque(false);
				selected = false;
				super.paint(g);
			} finally {
				selected = tmp;
			}
			if (bordercolor != null) {
				g.setColor(bordercolor);
				g2.setStroke(new BasicStroke(b));
				g.drawOval(b - 1, b - 1, d.width - b, d.height - b);
			}
			if (selected) {
				g2.setStroke(GraphConstants.SELECTION_STROKE);
				g.setColor(graph.getHighlightColor());
				g.drawOval(b - 1, b - 1, d.width - b, d.height - b);
			}
		}
	}
}
