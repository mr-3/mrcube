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

package org.mrcube.editors;

import org.jgraph.event.GraphSelectionEvent;
import org.jgraph.graph.Edge;
import org.jgraph.graph.GraphCell;
import org.jgraph.graph.GraphConstants;
import org.mrcube.MR3;
import org.mrcube.jgraph.GraphManager;
import org.mrcube.jgraph.RDFGraph;
import org.mrcube.jgraph.RDFGraphMarqueeHandler;
import org.mrcube.jgraph.RDFGraphModel;
import org.mrcube.models.MR3Constants.GraphType;
import org.mrcube.models.MR3Resource;
import org.mrcube.models.PropertyModel;
import org.mrcube.models.RDFResourceModel;
import org.mrcube.utils.GraphUtilities;
import org.mrcube.utils.PrefixNSUtil;
import org.mrcube.views.RDFLiteralPanel;
import org.mrcube.views.RDFPropertyPanel;
import org.mrcube.views.RDFResourcePanel;

import javax.swing.*;
import java.lang.ref.WeakReference;

/**
 * @author Takeshi Morita
 */
public class RDFEditor extends Editor {

    private RDFGraphMarqueeHandler rdfGraphMarqueeHandler;
    private static WeakReference<RDFResourcePanel> resPanelRef;
    private static WeakReference<RDFPropertyPanel> propPanelRef;
    private static WeakReference<RDFLiteralPanel> litPanelRef;

    public RDFEditor(GraphManager gm) {
        graph = new RDFGraph(gm, new RDFGraphModel(), GraphType.RDF);
        graph.setFont(graphFont);
        graph.getSelectionModel().setChildrenSelectable(false);
        rdfGraphMarqueeHandler = new RDFGraphMarqueeHandler(gm, graph);
        graph.setMarqueeHandler(rdfGraphMarqueeHandler);
        initEditor(graph, gm);
    }

    public RDFGraphMarqueeHandler getRdfGraphMarqueeHandler() {
        return rdfGraphMarqueeHandler;
    }

    public static void updateComponents() {
        if (resPanelRef != null && resPanelRef.get() != null) {
            RDFResourcePanel.loadResourceBundle();
            SwingUtilities.updateComponentTreeUI(resPanelRef.get());
        } else if (propPanelRef != null && propPanelRef.get() != null) {
            SwingUtilities.updateComponentTreeUI(propPanelRef.get());
        } else if (litPanelRef != null && litPanelRef.get() != null) {
            SwingUtilities.updateComponentTreeUI(litPanelRef.get());
        }
    }

    protected void initField(GraphManager manager) {
        super.initField(manager);
        resPanelRef = new WeakReference<>(null);
        propPanelRef = new WeakReference<>(null);
        litPanelRef = new WeakReference<>(null);
    }

    // 対応するRDFSクラスを選択
    private void selectResource(GraphCell cell) {
        RDFResourceModel info = (RDFResourceModel) GraphConstants.getValue(cell.getAttributes());
        if (info != null) {
            if (!isEditMode()) {
                gmanager.selectClassCell(info.getTypeCell());
            }
            if (gmanager.getAttrDialog().isVisible()) {
                RDFResourcePanel resPanel = getRDFResourcePanel();
                resPanel.setValue(cell);
                gmanager.getAttrDialog().setContentPane(resPanel);
            }
            String resourceType = "none";
            if (info.hasType()) {
                resourceType = info.getType().getURI();
            }
            MR3.STATUS_BAR.setText("URI: " + info.getURIStr() + "  TYPE: " + resourceType);
        }
    }

    private void selectProperty(GraphCell cell) {
        PropertyModel propertyInfo = (PropertyModel) GraphConstants.getValue(cell.getAttributes());
        if (!propertyInfo.getURI().equals(MR3Resource.Nil)) {
            Object propCell = gmanager.getPropertyCell(propertyInfo.getURI(), false);
            if (propCell != null && !isEditMode()) {
                gmanager.selectPropertyCell(propCell); // 対応するRDFSプロパティを選択
            }
        }
        Edge edge = (Edge) cell;
        PrefixNSUtil.setNamespaceModelSet(GraphUtilities.getNamespaceModelSet());
        RDFPropertyPanel propPanel = getRDFPropertyPanel();
        propPanel.setPropertyList(gmanager.getPropertyList());
        propPanel.setValue(edge, propertyInfo);
        gmanager.getAttrDialog().setContentPane(propPanel);
        if (propertyInfo != null) {
            MR3.STATUS_BAR.setText("URI: " + propertyInfo.getURIStr());
        }
    }

    private void selectLiteral(GraphCell cell) {
        if (gmanager.getAttrDialog().isVisible()) {
            RDFLiteralPanel litPanel = getRDFLiteralPanel();
            litPanel.setValue(cell);
            gmanager.getAttrDialog().setContentPane(litPanel);
        }
    }

    // From GraphSelectionListener Interface
    public void valueChanged(GraphSelectionEvent e) {
        if (!gmanager.isImporting()) {
            setToolStatus();
            lastSelectionCells = GraphUtilities.changeSelectionCellStyle(graph, lastSelectionCells);
            changeAttrPanel();
            gmanager.getAttrDialog().validate(); // validateメソッドを呼ばないと再描画がうまくいかない
            graph.repaint();
        }
    }

    private void changeAttrPanel() {
        Object[] cells = graph.getDescendants(graph.getSelectionCells());
        GraphCell rdfCell = graph.isOneRDFCellSelected(cells);

        if (rdfCell != null) {
            if (RDFGraph.isRDFResourceCell(rdfCell)) {
                selectResource(rdfCell);
            } else if (RDFGraph.isRDFPropertyCell(rdfCell)) {
                selectProperty(rdfCell);
            } else if (RDFGraph.isRDFLiteralCell(rdfCell)) {
                selectLiteral(rdfCell);
            }
        } else {
            gmanager.getAttrDialog().setNullPanel();
            MR3.STATUS_BAR.setText("");
        }
    }

    private RDFResourcePanel getRDFResourcePanel() {
        RDFResourcePanel result = resPanelRef.get();
        if (result == null) {
            result = new RDFResourcePanel(gmanager);
            resPanelRef = new WeakReference<>(result);
        }
        return result;
    }

    private RDFPropertyPanel getRDFPropertyPanel() {
        RDFPropertyPanel result = propPanelRef.get();
        if (result == null) {
            result = new RDFPropertyPanel(gmanager);
            propPanelRef = new WeakReference<>(result);
        }
        return result;
    }

    private RDFLiteralPanel getRDFLiteralPanel() {
        RDFLiteralPanel result = litPanelRef.get();
        if (result == null) {
            result = new RDFLiteralPanel(gmanager);
            litPanelRef = new WeakReference<>(result);
        }
        return result;
    }
}
