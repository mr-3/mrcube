/*
 * Project Name: MR^3 (Meta-Model Management based on RDFs Revision Reflection)
 * Project Website: http://mrcube.org/
 * 
 * Copyright (C) 2003-2019 Yamaguchi Laboratory, Keio University. All rights reserved.
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

import org.jgraph.graph.GraphCell;
import org.mrcube.jgraph.GraphManager;
import org.mrcube.jgraph.RDFGraph;
import org.mrcube.models.ClassModel;
import org.mrcube.models.MR3Constants.GraphType;
import org.mrcube.models.MR3Constants.HistoryType;
import org.mrcube.models.PropertyModel;
import org.mrcube.models.RDFSModel;
import org.mrcube.models.RDFSModelMap;
import org.mrcube.utils.GraphUtilities;
import org.mrcube.views.HistoryManager;
import org.mrcube.views.OntologyPanel.BasePanel;

import javax.swing.*;
import java.awt.event.ActionEvent;

/**
 * @author Takeshi Morita
 */
public class EditConceptAction extends AbstractAction {

    private String uri;
    private BasePanel basePanel;
    private RDFSModel rdfsModel;
    private GraphCell graphCell;
    private final RDFGraph graph;
    private final GraphManager gmanager;

    public EditConceptAction(RDFGraph g, GraphManager gm) {
        graph = g;
        gmanager = gm;
    }

    public EditConceptAction(BasePanel bp, RDFGraph g, GraphManager gm) {
        basePanel = bp;
        graph = g;
        gmanager = gm;
    }

    public void setURIString(String uri) {
        this.uri = uri;
    }

    public void setGraphCell(GraphCell gc) {
        graphCell = gc;
    }

    public void setRDFSInfo(RDFSModel info) {
        rdfsModel = info;
    }

    private RDFSModel editConcept() {
        RDFSModel beforeRDFSModel = null;
        if (rdfsModel instanceof ClassModel) {
            beforeRDFSModel = new ClassModel((ClassModel) rdfsModel);
        } else if (rdfsModel instanceof PropertyModel) {
            beforeRDFSModel = new PropertyModel((PropertyModel) rdfsModel);
        }

        // ここで，URIとセルのマッピングを削除する
        RDFSModelMap rdfsModelMap = gmanager.getCurrentRDFSInfoMap();
        rdfsModelMap.removeURICellMap(rdfsModel);
        rdfsModel.setURI(uri);
        GraphUtilities.resizeRDFSResourceCell(gmanager, rdfsModel, graphCell);
        rdfsModelMap.putURICellMap(rdfsModel, graphCell);
        gmanager.selectChangedRDFCells(rdfsModel);

        return beforeRDFSModel;
    }

    private void editWithDialog() {
        if (graphCell != null) {
            uri = basePanel.getURIString();
            if (gmanager.isEmptyURI(uri) || gmanager.isDuplicatedWithDialog(uri, graphCell, graph.getType())) { return; }
            RDFSModel beforeRDFSModel = editConcept();
            rdfsModel.setMetaClass(basePanel.getMetaClassString());
            if (graph.getType() == GraphType.CLASS) {
                HistoryManager.saveHistory(HistoryType.EDIT_CLASS_WITH_DIAGLOG, beforeRDFSModel, rdfsModel);
            } else if (graph.getType() == GraphType.PROPERTY) {
                HistoryManager.saveHistory(HistoryType.EDIT_ONT_PROPERTY_WITH_DIAGLOG, beforeRDFSModel, rdfsModel);
            }
            graph.clearSelection();
            graph.setSelectionCell(graphCell);
        }
    }

    private boolean isValidResource(String newRes, String oldRes) {
        return !newRes.equals(oldRes) && !gmanager.isDuplicatedWithDialog(newRes, null, graph.getType());
    }

    public void editWithGraph(String uri, RDFSModel info, GraphCell cell) {
        rdfsModel = info;
        graphCell = cell;
        this.uri = uri;
        if (!isValidResource(uri, rdfsModel.getURIStr())) {
            graph.getGraphLayoutCache().editCell(cell, cell.getAttributes());
            return;
        }
        RDFSModel beforeInfo = editConcept();
        if (RDFGraph.isRDFSClassCell(graphCell)) {
            HistoryManager.saveHistory(HistoryType.EDIT_CLASS_WITH_GRAPH, beforeInfo, rdfsModel);
        } else if (RDFGraph.isRDFSPropertyCell(graphCell)) {
            HistoryManager.saveHistory(HistoryType.EDIT_ONT_PROPERTY_WITH_GRAPH, beforeInfo, rdfsModel);
        }
    }

    public void actionPerformed(ActionEvent e) {
        editWithDialog();
    }
}
