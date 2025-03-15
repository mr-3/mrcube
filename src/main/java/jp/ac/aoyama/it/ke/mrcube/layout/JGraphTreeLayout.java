/*
 * Project Name: MR^3 (Meta-Model Management based on RDFs Revision Reflection)
 * Project Website: https://mr-3.github.io/
 *
 * Copyright (C) 2003-2025 Takeshi Morita. All rights reserved.
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

package jp.ac.aoyama.it.ke.mrcube.layout;

import org.jgraph.graph.DefaultGraphCell;
import jp.ac.aoyama.it.ke.mrcube.jgraph.GraphManager;
import jp.ac.aoyama.it.ke.mrcube.jgraph.RDFGraph;
import jp.ac.aoyama.it.ke.mrcube.utils.MR3CellMaker;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author Takeshi Morita
 */
public class JGraphTreeLayout {

    private final GraphManager gmanager;
    private final MR3CellMaker cellMaker;

    public JGraphTreeLayout(GraphManager gm) {
        gmanager = gm;
        cellMaker = new MR3CellMaker(gmanager);
    }

    public void performJGraphTreeLayout() {
        gmanager.removeTypeCells();
        performJGraphTreeLayout(gmanager.getInstanceGraph(),
                GraphLayoutUtilities.getJGraphRDFLayoutDirection(),
                GraphLayoutUtilities.RDF_VERTICAL_SPACE, GraphLayoutUtilities.RDF_HORIZONTAL_SPACE);
        gmanager.addTypeCells();
        performJGraphRDFSTreeLayout();
    }

    public void performJGraphRDFSTreeLayout() {
        RDFGraph classGraph = gmanager.getClassGraph();
        RDFGraph propGraph = gmanager.getPropertyGraph();
        GraphLayoutUtilities.reverseArc(cellMaker, classGraph);
        GraphLayoutUtilities.reverseArc(cellMaker, propGraph);
        performJGraphTreeLayout(classGraph, GraphLayoutUtilities.getJGraphClassLayoutDirection(),
                GraphLayoutUtilities.CLASS_VERTICAL_SPACE,
                GraphLayoutUtilities.CLASS_HORIZONTAL_SPACE);
        performJGraphTreeLayout(propGraph, GraphLayoutUtilities.getJGraphPropertyLayoutDirection(),
                GraphLayoutUtilities.PROPERTY_VERTICAL_SPACE,
                GraphLayoutUtilities.PROPERTY_HORIZONTAL_SPACE);
        GraphLayoutUtilities.reverseArc(cellMaker, classGraph);
        GraphLayoutUtilities.reverseArc(cellMaker, propGraph);
        gmanager.clearSelection();
    }

    public void performJGraphTreeLayout(RDFGraph graph, int orientation, int distance, int border) {
        Map<Object, GraphLayoutData> cellLayoutMap = new HashMap<>();
        Set<GraphLayoutData> dataSet = GraphLayoutUtilities.initGraphLayoutData(graph, cellLayoutMap);
        Set<DefaultGraphCell> rootCells = new HashSet<>();

        for (GraphLayoutData data : dataSet) {
            DefaultGraphCell cell = (DefaultGraphCell) data.getCell();
            GraphLayoutUtilities.addChild(graph, cell, data, cellLayoutMap);
            if (!data.hasParent()) {
                rootCells.add(cell);
            }
        }
        RDFGraph rdfGraph = gmanager.getInstanceGraph();

        Object tmpRoot = null;
        if (rootCells.size() != 1) {
            tmpRoot = GraphLayoutUtilities.collectRoot(rdfGraph, cellMaker, rootCells, dataSet, cellLayoutMap);
        }

        TreeLayoutAlgorithm treeLayout = new TreeLayoutAlgorithm(orientation, distance, border);
        if (tmpRoot != null) {
            treeLayout.perform(graph, new Object[]{tmpRoot});
        } else {
            treeLayout.perform(graph, rootCells.toArray());
        }
        dataSet.remove(cellLayoutMap.get(tmpRoot));
        GraphLayoutUtilities.removeTemporaryRoot(rdfGraph, (DefaultGraphCell) tmpRoot);
        GraphLayoutUtilities.centralizeGraph(graph);
    }
}