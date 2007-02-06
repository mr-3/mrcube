/*
 * @(#) PropertyEditor.java
 * 
 * 
 * Copyright (C) 2003 The MMM Project
 * 
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation; either version 2.1 of the License, or (at your
 * option) any later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *  
 */

package org.semanticweb.mmm.mr3.editor;

import java.lang.ref.*;

import org.jgraph.event.*;
import org.jgraph.graph.*;
import org.semanticweb.mmm.mr3.*;
import org.semanticweb.mmm.mr3.data.*;
import org.semanticweb.mmm.mr3.jgraph.*;
import org.semanticweb.mmm.mr3.ui.*;
import org.semanticweb.mmm.mr3.util.*;

/**
 * @author takeshi morita
 */
public class PropertyEditor extends Editor {

    private WeakReference propPanelRef;

    public PropertyEditor(GraphManager gm) {
        super(Translator.getString("PropertyEditor.Title"));
        graph = gm.getPropertyGraph();
        graph.setDisconnectable(false);
        initEditor(gm.getPropertyGraph(), gm);
        setFrameIcon(Utilities.getImageIcon(Translator.getString("PropertyEditor.Icon")));
    }

    protected void initField(GraphManager gm) {
        super.initField(gm);
        propPanelRef = new WeakReference<PropertyPanel>(null);
        graph.setMarqueeHandler(new PropertyGraphMarqueeHandler(gm));
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
        PropertyPanel result = (PropertyPanel) propPanelRef.get();
        if (result == null) {
            result = new PropertyPanel(gmanager);
            propPanelRef = new WeakReference<PropertyPanel>(result);
        }
        return result;
    }
}
