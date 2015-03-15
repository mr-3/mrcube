/*
 * Project Name: MR^3 (Meta-Model Management based on RDFs Revision Reflection)
 * Project Website: http://mr3.sourceforge.net/
 * 
 * Copyright (C) 2003-2015 Yamaguchi Laboratory, Keio University. All rights reserved. 
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

package net.sourceforge.mr3.layout;

import java.util.*;

import net.sourceforge.mr3.data.MR3Constants.*;

import com.hp.hpl.jena.rdf.model.*;

/**
 * @author Takeshi Morita
 */
public class VGJTreeLayout {

    public char LEFT_TO_RIGHT = 'r';
    public char UP_TO_DOWN = 'u';
    private static TreeAlgorithm treeAlgorithm = new TreeAlgorithm(GraphLayoutUtilities.getVGJRDFLayoutDirection());

    public static Map<RDFNode, GraphLayoutData> getVGJRDFCellLayoutMap(Model model) {
        if (!GraphLayoutUtilities.LAYOUT_TYPE.equals(GraphLayoutUtilities.VGJ_TREE_LAYOUT)) { return null; }

        Map<RDFNode, GraphLayoutData> cellLayoutMap = new HashMap<RDFNode, GraphLayoutData>();
        Set<GraphLayoutData> dataSet = GraphLayoutUtilities.initGraphLayoutData(model, cellLayoutMap);
        Set<RDFNode> rootNodes = new HashSet<RDFNode>();
        for (GraphLayoutData data : dataSet) {
            GraphLayoutUtilities.addChild(model, data, cellLayoutMap);
            if (!data.hasParent()) {
                rootNodes.add(data.getRDFNode());
            }
        }

        RDFNode tmpRoot = null;
        if (rootNodes.size() != 1) {
            tmpRoot = GraphLayoutUtilities.collectRoot(model, rootNodes, dataSet, cellLayoutMap);
        }

        treeAlgorithm.setOrientation(GraphLayoutUtilities.getVGJRDFLayoutDirection());
        treeAlgorithm.applyTreeAlgorithm(dataSet, null, GraphType.RDF);
        dataSet.remove(cellLayoutMap.get(tmpRoot));
        GraphLayoutUtilities.removeTemporaryRoot(model, tmpRoot);
        GraphLayoutUtilities.centralizeGraph(dataSet);

        return cellLayoutMap;
    }

    public static Map<RDFNode, GraphLayoutData> getVGJPropertyCellLayoutMap() {
        if (!GraphLayoutUtilities.LAYOUT_TYPE.equals(GraphLayoutUtilities.VGJ_TREE_LAYOUT)) { return null; }
        Map<RDFNode, GraphLayoutData> cellLayoutMap = new HashMap<RDFNode, GraphLayoutData>();
        GraphLayoutUtilities.initPropertyGraphLayoutData(cellLayoutMap);
        return getVGJCellLayoutMap(cellLayoutMap, GraphLayoutUtilities.getVGJPropertyLayoutDirection(),
                GraphType.PROPERTY);
    }

    public static Map<RDFNode, GraphLayoutData> getVGJClassCellLayoutMap() {
        if (!GraphLayoutUtilities.LAYOUT_TYPE.equals(GraphLayoutUtilities.VGJ_TREE_LAYOUT)) { return null; }
        Map<RDFNode, GraphLayoutData> cellLayoutMap = new HashMap<RDFNode, GraphLayoutData>();
        GraphLayoutUtilities.initClassGraphLayoutData(cellLayoutMap);
        return getVGJCellLayoutMap(cellLayoutMap, GraphLayoutUtilities.getVGJClassLayoutDirection(), GraphType.CLASS);
    }

    private static Map<RDFNode, GraphLayoutData> getVGJCellLayoutMap(Map<RDFNode, GraphLayoutData> cellLayoutMap, char orientation, GraphType type) {
        treeAlgorithm.setOrientation(orientation);
        treeAlgorithm.applyTreeAlgorithm(cellLayoutMap.values(), null, type);
        GraphLayoutUtilities.centralizeGraph(cellLayoutMap.values());
        return cellLayoutMap;
    }
}
