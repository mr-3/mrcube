/*
 * Project Name: MR^3 (Meta-Model Management based on RDFs Revision Reflection)
 * Project Website: http://mrcube.org/
 * 
 * Copyright (C) 2003-2018 Yamaguchi Laboratory, Keio University. All rights reserved.
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

package org.mrcube.actions;

import org.mrcube.jgraph.GraphManager;
import org.mrcube.models.MR3Constants.GraphType;

import java.awt.event.ActionEvent;

/**
 * @author Takeshi Morita
 * 
 */
public class SelectNodes extends MR3AbstractAction {

    private final GraphType graphType;
    private final GraphManager graphManager;

    public SelectNodes(GraphManager gm, GraphType type, String name) {
        super(name);
        graphType = type;
        graphManager = gm;
    }

    public void actionPerformed(ActionEvent e) {
        if (graphType == GraphType.RDF) {
            graphManager.getCurrentRDFGraph().selectAllNodes();
        } else if (graphType == GraphType.CLASS) {
            graphManager.getCurrentClassGraph().selectAllNodes();
        } else if (graphType == GraphType.PROPERTY) {
            graphManager.getCurrentPropertyGraph().selectAllNodes();
        }
    }

}
