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
 */
public class PropertyEditor extends Editor {

    private WeakReference<PropertyPanel> propPanelRef;

    public PropertyEditor(GraphManager gm) {
        graph = new RDFGraph(gm, new RDFGraphModel(), GraphType.PROPERTY);
        graph.setFont(graphFont);
        graph.setDisconnectable(false);
        initEditor(graph, gm);
    }

    protected void initField(GraphManager gm) {
        super.initField(gm);
        propPanelRef = new WeakReference<PropertyPanel>(null);
        graph.setMarqueeHandler(new PropertyGraphMarqueeHandler(gm, graph));
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
            RDFSInfo info = (RDFSInfo) GraphConstants.getValue(cell.getAttributes());
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
            propPanelRef = new WeakReference<PropertyPanel>(result);
        }
        return result;
    }
}
