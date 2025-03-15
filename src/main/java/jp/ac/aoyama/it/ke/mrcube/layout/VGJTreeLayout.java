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

import jp.ac.aoyama.it.ke.mrcube.models.MR3Constants;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author Takeshi Morita
 */
public class VGJTreeLayout {

    private static final TreeAlgorithm treeAlgorithm = new TreeAlgorithm(GraphLayoutUtilities.getVGJRDFLayoutDirection());

    public static Map<RDFNode, GraphLayoutData> getVGJRDFCellLayoutMap(Model model) {
        if (!GraphLayoutUtilities.LAYOUT_TYPE.equals(GraphLayoutUtilities.VGJ_TREE_LAYOUT)) {
            return null;
        }

        Map<RDFNode, GraphLayoutData> cellLayoutMap = new HashMap<>();
        Set<GraphLayoutData> dataSet = GraphLayoutUtilities.initGraphLayoutData(model, cellLayoutMap);
        Set<RDFNode> rootNodes = new HashSet<>();
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
        treeAlgorithm.setSeparation(MR3Constants.GraphType.Instance);
        treeAlgorithm.applyTreeAlgorithm(dataSet, null, MR3Constants.GraphType.Instance);
        dataSet.remove(cellLayoutMap.get(tmpRoot));
        GraphLayoutUtilities.removeTemporaryRoot(model, tmpRoot);
        GraphLayoutUtilities.centralizeGraph(dataSet);

        return cellLayoutMap;
    }

    public static Map<RDFNode, GraphLayoutData> getVGJPropertyCellLayoutMap() {
        if (!GraphLayoutUtilities.LAYOUT_TYPE.equals(GraphLayoutUtilities.VGJ_TREE_LAYOUT)) {
            return null;
        }
        Map<RDFNode, GraphLayoutData> cellLayoutMap = new HashMap<>();
        GraphLayoutUtilities.initPropertyGraphLayoutData(cellLayoutMap);
        return getVGJCellLayoutMap(cellLayoutMap, GraphLayoutUtilities.getVGJPropertyLayoutDirection(), MR3Constants.GraphType.Property);
    }

    public static Map<RDFNode, GraphLayoutData> getVGJClassCellLayoutMap() {
        if (!GraphLayoutUtilities.LAYOUT_TYPE.equals(GraphLayoutUtilities.VGJ_TREE_LAYOUT)) {
            return null;
        }
        Map<RDFNode, GraphLayoutData> cellLayoutMap = new HashMap<>();
        GraphLayoutUtilities.initClassGraphLayoutData(cellLayoutMap);
        return getVGJCellLayoutMap(cellLayoutMap, GraphLayoutUtilities.getVGJClassLayoutDirection(), MR3Constants.GraphType.Class);
    }

    private static Map<RDFNode, GraphLayoutData> getVGJCellLayoutMap(Map<RDFNode, GraphLayoutData> cellLayoutMap, char orientation, MR3Constants.GraphType type) {
        treeAlgorithm.setOrientation(orientation);
        treeAlgorithm.setSeparation(type);
        treeAlgorithm.applyTreeAlgorithm(cellLayoutMap.values(), null, type);
        GraphLayoutUtilities.centralizeGraph(cellLayoutMap.values());
        return cellLayoutMap;
    }
}
