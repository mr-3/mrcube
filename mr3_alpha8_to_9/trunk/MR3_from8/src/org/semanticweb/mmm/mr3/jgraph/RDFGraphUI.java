/*
 * @(#) RDFGraphUI.java
 *
 * Copyright (C) 2003 The MMM Project
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

package org.semanticweb.mmm.mr3.jgraph;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;

import org.jgraph.plaf.basic.*;
import org.semanticweb.mmm.mr3.data.*;
import org.semanticweb.mmm.mr3.ui.*;

/**
 * @author takeshi morita
 */
public class RDFGraphUI extends BasicGraphUI {

	private RDFGraph graph;
	private AttributeDialog attrDialog;

	RDFGraphUI(RDFGraph g, AttributeDialog attrD) {
		graph = g;
		attrDialog = attrD;
	}

	public RDFGraph getRDFGraph() {
		return graph;
	}

	//
	// Override Parent Methods
	//
	// @jdk14
	/*
	 * protected TransferHandler createTransferHandler() { return new
	 * GPTransferHandler();
	 */

	private boolean isRDFSGraph() {
		return graph.getType() == GraphType.CLASS || graph.getType() == GraphType.PROPERTY;
	}

	protected boolean startEditing(Object cell, MouseEvent event) {
		if (isRDFSGraph() && graph.isEdge(cell)) {
			return super.startEditing(cell, event);
		} else {
			if (attrDialog != null) {
				attrDialog.setVisible(true);
				graph.setSelectionCell(cell);
			}
		}
		return true;
	}

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
