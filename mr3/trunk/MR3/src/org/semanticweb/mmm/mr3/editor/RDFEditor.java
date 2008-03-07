/*
 * Project Name: MR^3 (Meta-Model Management based on RDFs Revision Reflection)
 * Project Website: http://mr3.sourceforge.net/
 * 
 * Copyright (C) 2003-2008 Yamaguchi Laboratory, Keio University. All rights reserved. 
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

package org.semanticweb.mmm.mr3.editor;

import java.lang.ref.*;

import javax.swing.*;

import org.jgraph.event.*;
import org.jgraph.graph.*;
import org.semanticweb.mmm.mr3.*;
import org.semanticweb.mmm.mr3.data.*;
import org.semanticweb.mmm.mr3.data.MR3Constants.*;
import org.semanticweb.mmm.mr3.jgraph.*;
import org.semanticweb.mmm.mr3.ui.*;
import org.semanticweb.mmm.mr3.util.*;

/**
 * @author takeshi morita
 * 
 */
public class RDFEditor extends Editor {

    private static WeakReference resPanelRef;
    private static WeakReference propPanelRef;
    private static WeakReference litPanelRef;

    public RDFEditor(GraphManager gm) {
        graph = new RDFGraph(gm, new RDFGraphModel(), GraphType.RDF);
        graph.setFont(graphFont);
        graph.getSelectionModel().setChildrenSelectable(false);
        graph.setMarqueeHandler(new RDFGraphMarqueeHandler(gm, graph));
        initEditor(graph, gm);
    }

    public static void updateComponents() {
        if (resPanelRef != null && resPanelRef.get() != null) {
            RDFResourcePanel.loadResourceBundle();
            SwingUtilities.updateComponentTreeUI((JComponent) resPanelRef.get());
        } else if (propPanelRef != null && propPanelRef.get() != null) {
            SwingUtilities.updateComponentTreeUI((JComponent) propPanelRef.get());
        } else if (litPanelRef != null && litPanelRef.get() != null) {
            SwingUtilities.updateComponentTreeUI((JComponent) litPanelRef.get());
        }
    }

    protected void initField(GraphManager manager) {
        super.initField(manager);
        resPanelRef = new WeakReference<RDFResourcePanel>(null);
        propPanelRef = new WeakReference<RDFPropertyPanel>(null);
        litPanelRef = new WeakReference<RDFLiteralPanel>(null);
    }

    // 対応するRDFSクラスを選択
    private void selectResource(GraphCell cell) {
        RDFResourceInfo info = (RDFResourceInfo) GraphConstants.getValue(cell.getAttributes());
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
        // PropertyInfo propertyInfo = (PropertyInfo)
        // graph.getModel().getValue(cell.getAttributes());
        PropertyInfo propertyInfo = (PropertyInfo) GraphConstants.getValue(cell.getAttributes());
        if (!propertyInfo.getURI().equals(MR3Resource.Nil)) {
            Object propCell = gmanager.getPropertyCell(propertyInfo.getURI(), false);
            if (propCell != null && !isEditMode()) {
                gmanager.selectPropertyCell(propCell); // 対応するRDFSプロパティを選択
            }
        }
        Edge edge = (Edge) cell;
        PrefixNSUtil.setPrefixNSInfoSet(GraphUtilities.getPrefixNSInfoSet());
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
        RDFResourcePanel result = (RDFResourcePanel) resPanelRef.get();
        if (result == null) {
            result = new RDFResourcePanel(gmanager);
            resPanelRef = new WeakReference<RDFResourcePanel>(result);
        }
        return result;
    }

    private RDFPropertyPanel getRDFPropertyPanel() {
        RDFPropertyPanel result = (RDFPropertyPanel) propPanelRef.get();
        if (result == null) {
            result = new RDFPropertyPanel(gmanager);
            propPanelRef = new WeakReference<RDFPropertyPanel>(result);
        }
        return result;
    }

    private RDFLiteralPanel getRDFLiteralPanel() {
        RDFLiteralPanel result = (RDFLiteralPanel) litPanelRef.get();
        if (result == null) {
            result = new RDFLiteralPanel(gmanager);
            litPanelRef = new WeakReference<RDFLiteralPanel>(result);
        }
        return result;
    }
}
