/*
 * Created on 2003/07/29
 *
 */
package org.semanticweb.mmm.mr3.jgraph;

import java.awt.*;
import java.awt.geom.*;

import org.jgraph.plaf.basic.*;

/**
 * @author takeshi morita
 */
public class RDFGraphUI extends BasicGraphUI {

	public RDFGraph getRDFGraph() {
		return (RDFGraph) graph;
	}

	//
	// Override Parent Methods
	//
	// @jdk14
	/*
	protected TransferHandler createTransferHandler() {
		return new GPTransferHandler();
	}*/

	/**
	 * Paint the background of this graph. Calls paintGrid.
	 */
	protected void paintBackground(Graphics g) {
		Rectangle pageBounds = graph.getBounds();
		if (getRDFGraph().getBackgroundImage() != null) {
			// Use clip and pageBounds
			double s = graph.getScale();
			Graphics2D g2 = (Graphics2D) g;
			AffineTransform tmp = g2.getTransform();
			g2.scale(s, s);
			g.drawImage(getRDFGraph().getBackgroundImage(), 0, 0, graph);
			g2.setTransform(tmp);
		} else if (getRDFGraph().isPageVisible()) { // FIX: Use clip
			int w = (int) (getRDFGraph().getPageFormat().getWidth());
			int h = (int) (getRDFGraph().getPageFormat().getHeight());
			Point p = graph.toScreen(new Point(w, h));
			w = p.x;
			h = p.y;
			g.setColor(graph.getHandleColor());
			g.fillRect(0, 0, graph.getWidth(), graph.getHeight());
			g.setColor(Color.darkGray);
			g.fillRect(3, 3, w, h);
			g.setColor(graph.getBackground());
			g.fillRect(1, 1, w - 1, h - 1);
			pageBounds = new Rectangle(0, 0, w, h);
		}
		if (graph.isGridVisible())
			paintGrid(graph.getGridSize(), g, pageBounds);
	}
}
