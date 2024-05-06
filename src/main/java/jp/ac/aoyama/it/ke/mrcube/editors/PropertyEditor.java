/*
 * Project Name: MR^3 (Meta-Model Management based on RDFs Revision Reflection)
 * Project Website: https://mr-3.github.io/
 *
 * Copyright (C) 2003-2024 Takeshi Morita. All rights reserved.
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

package jp.ac.aoyama.it.ke.mrcube.editors;

import jp.ac.aoyama.it.ke.mrcube.MR3;
import jp.ac.aoyama.it.ke.mrcube.jgraph.*;
import jp.ac.aoyama.it.ke.mrcube.models.MR3Constants.GraphType;
import jp.ac.aoyama.it.ke.mrcube.models.RDFSModel;
import jp.ac.aoyama.it.ke.mrcube.utils.GraphUtilities;
import jp.ac.aoyama.it.ke.mrcube.views.property_editor.PropertyPanel;
import org.jgraph.event.GraphSelectionEvent;
import org.jgraph.graph.DefaultGraphCell;
import org.jgraph.graph.GraphConstants;

import java.lang.ref.WeakReference;

/**
 * @author Takeshi Morita
 */
public class PropertyEditor extends Editor {

    private WeakReference<PropertyPanel> propPanelRef;
    private PropertyGraphMarqueeHandler propertyGraphMarqueeHandler;

    public PropertyEditor(GraphManager gm) {
        graph = new RDFGraph(gm, new RDFGraphModel(), GraphType.Property);
        graph.setFont(graphFont);
        graph.setDisconnectable(false);
        initEditor(graph, gm);
    }

    protected void initField(GraphManager gm) {
        super.initField(gm);
        propPanelRef = new WeakReference<>(null);
        propertyGraphMarqueeHandler = new PropertyGraphMarqueeHandler(gm, graph);
        graph.setMarqueeHandler(propertyGraphMarqueeHandler);
    }

    public PropertyGraphMarqueeHandler getPropertyGraphMarqueeHandler() {
        return propertyGraphMarqueeHandler;
    }

    public void valueChanged(GraphSelectionEvent e) {
        if (!gmanager.isImporting()) {
            lastSelectionCells = GraphUtilities.changeSelectionCellStyle(graph, lastSelectionCells);
            setToolStatus();
            changeAttrPanel();
        }
    }

    private void changeAttrPanel() {
        DefaultGraphCell cell = (DefaultGraphCell) graph.getSelectionCell();
        if (graph.isOneCellSelected(cell) && cell instanceof OntPropertyCell) {
            RDFSModel info = (RDFSModel) GraphConstants.getValue(cell.getAttributes());
            if (info != null) {
                if (gmanager.getAttrDialog().isVisible()) {
                    PropertyPanel propPanel = getPropertyPanel();
                    gmanager.getAttrDialog().setContentPane(propPanel);
                    gmanager.getAttrDialog().validate();
                    propPanel.showRDFSInfo(cell);
                }
                MR3.STATUS_BAR.setText("URI: " + info.getURIStr() + "  TYPE: " + info.getMetaClass());
            }
        } else {
            gmanager.getAttrDialog().setNullPanel();
            MR3.STATUS_BAR.setText("");
        }
    }

    private PropertyPanel getPropertyPanel() {
        PropertyPanel result = propPanelRef.get();
        if (result == null) {
            result = new PropertyPanel(gmanager);
            propPanelRef = new WeakReference<>(result);
        }
        return result;
    }
}
