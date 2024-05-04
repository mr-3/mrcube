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

package org.mrcube.jgraph;

import org.jgraph.graph.DefaultCellViewFactory;
import org.jgraph.graph.EdgeView;
import org.jgraph.graph.VertexView;

import java.awt.event.MouseEvent;

/**
 * @author Takeshi Morita
 */
class RDFCellViewFactory extends DefaultCellViewFactory {

    public VertexView createVertexView(Object v) {
        if (v instanceof InstanceCell || v instanceof OntClassCell || v instanceof OntPropertyCell) {
            return new JGraphEllipseView(v);
        }
        if (v instanceof LiteralCell) {
            return new JGraphMultilineView(v);
        }
        return super.createVertexView(v);
    }

    public EdgeView createEdgeView(Object e) {
        return new EdgeView(e) {

            public boolean isAddPointEvent(MouseEvent event) {
                return event.isShiftDown(); // Points are Added using
                // Shift-Click
            }

            public boolean isRemovePointEvent(MouseEvent event) {
                return event.isShiftDown(); // Points are Removed using
                // Shift-Click
            }
        };
    }
}
