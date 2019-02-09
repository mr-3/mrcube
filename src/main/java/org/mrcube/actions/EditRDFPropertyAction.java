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

import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.jgraph.graph.GraphCell;
import org.jgraph.graph.GraphConstants;
import org.mrcube.MR3;
import org.mrcube.jgraph.GraphManager;
import org.mrcube.models.*;
import org.mrcube.models.MR3Constants.CreateRDFSType;
import org.mrcube.models.MR3Constants.GraphType;
import org.mrcube.models.MR3Constants.HistoryType;
import org.mrcube.utils.GraphUtilities;
import org.mrcube.utils.Translator;
import org.mrcube.views.HistoryManager;
import org.mrcube.views.MetaModelManagementDialog;

import javax.swing.*;

/**
 * @author Takeshi Morita
 */
public class EditRDFPropertyAction {

    private String uriStr;
    private GraphCell edge;
    private final GraphManager gmanager;
    private static final String WARNING = Translator.getString("Warning");

    public EditRDFPropertyAction(GraphManager gm) {
        gmanager = gm;
    }

    public void setURIString(String str) {
        uriStr = str;
    }

    public void setEdge(GraphCell edge) {
        this.edge = edge;
    }

    public boolean editRDFProperty() {
        GraphCell propertyCell = null;
        Resource uri = ResourceFactory.createResource(uriStr);
        RDFSModelMap rdfsModelMap = gmanager.getCurrentRDFSInfoMap();
        if (rdfsModelMap.isPropertyCell(uri) || uri.equals(MR3Resource.Nil)) {
            propertyCell = gmanager.getPropertyCell(uri, false);
        } else {
            if (gmanager.isDuplicatedWithDialog(uri.getURI(), null, GraphType.PROPERTY)) {
                return false;
            }
            if (MR3.OFF_META_MODEL_MANAGEMENT) {
                return false;
            }

            RDFSModel propInfo = (RDFSModel) GraphConstants.getValue(edge.getAttributes());
            if (propInfo.getURI().equals(MR3Resource.Nil)) {
                int ans = JOptionPane.showConfirmDialog(MR3.getCurrentProject(),
                        Translator.getString("Warning.Message10"), WARNING, JOptionPane.YES_NO_OPTION);
                if (ans == JOptionPane.YES_OPTION) {
                    propertyCell = (GraphCell) gmanager.insertSubRDFS(uri, null, gmanager.getCurrentPropertyGraph());
                    HistoryManager.saveHistory(HistoryType.META_MODEL_MANAGEMNET_REPLACE_PROPERTY_WITH_CREATE_ONT_PROPERTY);
                }
            } else {
                // OntManagementDialog dialog = new
                // OntManagementDialog(gmanager);
                MetaModelManagementDialog dialog = new MetaModelManagementDialog(gmanager);
                // dialog.replaceGraph(gmanager.getPropertyGraph());
                // dialog.setRegionSet(new HashSet());
                dialog.setVisible(true);

                CreateRDFSType createType = dialog.getCreateRDFSType();
                if (createType == CreateRDFSType.CREATE) {
                    // Set supProps = dialog.getSupRDFSSet();
                    // propertyCell = (GraphCell) gmanager.insertSubRDFS(uri,
                    // supProps, gmanager.getPropertyGraph());
                    propertyCell = (GraphCell) gmanager.insertSubRDFS(uri, null, gmanager.getCurrentPropertyGraph());
                    HistoryManager
                            .saveHistory(HistoryType.META_MODEL_MANAGEMNET_REPLACE_PROPERTY_WITH_CREATE_ONT_PROPERTY);
                } else if (createType == CreateRDFSType.RENAME) {
                    propInfo = (RDFSModel) GraphConstants.getValue(edge.getAttributes());
                    propertyCell = gmanager.getPropertyCell(propInfo.getURI(), false);
                    rdfsModelMap.removeURICellMap(propInfo);
                    propInfo.setURI(uri.getURI());
                    GraphUtilities.resizeRDFSResourceCell(gmanager, propInfo, propertyCell);
                    rdfsModelMap.putURICellMap(propInfo, propertyCell);
                    gmanager.selectChangedRDFCells(propInfo);
                    HistoryManager
                            .saveHistory(HistoryType.META_MODEL_MANAGEMNET_REPLACE_PROPERTY_WITH_REPLACE_ONT_PROPERTY);
                } else if (createType == null) {
                    return false;
                }
            }
        }

        if (propertyCell != null) {
            gmanager.selectPropertyCell(propertyCell);
        }

        PropertyModel propInfo = (PropertyModel) GraphConstants.getValue(propertyCell.getAttributes());
        if (MR3.OFF_META_MODEL_MANAGEMENT) {
            propInfo = new PropertyModel(propInfo.getURIStr());
        }
        GraphConstants.setValue(edge.getAttributes(), propInfo);
        GraphUtilities.editCell(edge, edge.getAttributes(), gmanager.getCurrentRDFGraph());
        return true;
    }
}
