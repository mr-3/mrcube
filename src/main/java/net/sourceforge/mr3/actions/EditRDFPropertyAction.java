/*
 * Project Name: MR^3 (Meta-Model Management based on RDFs Revision Reflection)
 * Project Website: http://mr3.sourceforge.net/
 * 
 * Copyright (C) 2003-2009 Yamaguchi Laboratory, Keio University. All rights reserved. 
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

package net.sourceforge.mr3.actions;

import javax.swing.*;

import net.sourceforge.mr3.*;
import net.sourceforge.mr3.data.*;
import net.sourceforge.mr3.data.MR3Constants.*;
import net.sourceforge.mr3.jgraph.*;
import net.sourceforge.mr3.ui.*;
import net.sourceforge.mr3.util.*;

import org.jgraph.graph.*;

import com.hp.hpl.jena.rdf.model.*;

/**
 * @author Takeshi Morita
 */
public class EditRDFPropertyAction {

	private String uriStr;
	private GraphCell edge;
	private GraphManager gmanager;
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
		RDFSInfoMap rdfsInfoMap = gmanager.getCurrentRDFSInfoMap();
		if (rdfsInfoMap.isPropertyCell(uri) || uri.equals(MR3Resource.Nil)) {
			propertyCell = gmanager.getPropertyCell(uri, false);
		} else {
			if (gmanager.isDuplicatedWithDialog(uri.getURI(), null, GraphType.PROPERTY)) {
				return false;
			}
			if (MR3.OFF_META_MODEL_MANAGEMENT) {
				return false;
			}

			RDFSInfo propInfo = (RDFSInfo) GraphConstants.getValue(edge.getAttributes());
			if (propInfo.getURI().equals(MR3Resource.Nil)) {
				int ans = JOptionPane.showConfirmDialog(gmanager.getDesktopTabbedPane(),
						Translator.getString("Warning.Message10"), WARNING, JOptionPane.YES_NO_OPTION);
				if (ans == JOptionPane.YES_OPTION) {
					propertyCell = (GraphCell) gmanager.insertSubRDFS(uri, null, gmanager.getCurrentPropertyGraph());
					HistoryManager
							.saveHistory(HistoryType.META_MODEL_MANAGEMNET_REPLACE_PROPERTY_WITH_CREATE_ONT_PROPERTY);
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
					propInfo = (RDFSInfo) GraphConstants.getValue(edge.getAttributes());
					propertyCell = gmanager.getPropertyCell(propInfo.getURI(), false);
					rdfsInfoMap.removeURICellMap(propInfo);
					propInfo.setURI(uri.getURI());
					GraphUtilities.resizeRDFSResourceCell(gmanager, propInfo, propertyCell);
					rdfsInfoMap.putURICellMap(propInfo, propertyCell);
					gmanager.selectChangedRDFCells(propInfo);
					HistoryManager
							.saveHistory(HistoryType.META_MODEL_MANAGEMNET_REPLACE_PROPERTY_WITH_REPLACE_ONT_PROPERTY);
				} else if (createType == null) {
					return false;
				}
			}
		}

		if (!gmanager.getCurrentRDFEditor().isEditMode() && propertyCell != null) {
			gmanager.selectPropertyCell(propertyCell); 
		}

		PropertyInfo propInfo = (PropertyInfo) GraphConstants.getValue(propertyCell.getAttributes());
		if (MR3.OFF_META_MODEL_MANAGEMENT) {
			propInfo = new PropertyInfo(propInfo.getURIStr());
		}
		GraphConstants.setValue(edge.getAttributes(), propInfo);
		GraphUtilities.editCell(edge, edge.getAttributes(), gmanager.getCurrentRDFGraph());
		return true;
	}
}
