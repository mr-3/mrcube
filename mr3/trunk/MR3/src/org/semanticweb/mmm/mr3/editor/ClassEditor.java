/*
 * @(#) ClassEditor.java
 * 
 * 
 * Copyright (C) 2003-2005 The MMM Project
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

/*
 * 
 * @author takeshi morita
 * 
 */
public class ClassEditor extends Editor {

    private WeakReference classPanelRef;

    public ClassEditor(GraphManager gm) {
        super(Translator.getString("ClassEditor.Title"));
        graph = gm.getClassGraph();
        graph.setDisconnectable(false);
        initEditor(gm.getClassGraph(), gm);
        setFrameIcon(Utilities.getImageIcon(Translator.getString("ClassEditor.Icon")));
    }

    protected void initField(GraphManager manager) {
        super.initField(manager);
        classPanelRef = new WeakReference<ClassPanel>(null);
        graph.setMarqueeHandler(new ClassGraphMarqueeHandler(manager));
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
        if (graph.isOneCellSelected(cell)) {
            Object info = GraphConstants.getValue(cell.getAttributes());
            if (info instanceof RDFSInfo) {
                if (gmanager.getAttrDialog().isVisible()) {
                    ClassPanel classPanel = getClassPanel();
                    classPanel.showRDFSInfo(cell);
                    gmanager.getAttrDialog().setContentPane(classPanel);
                    gmanager.getAttrDialog().validate();
                }
                MR3.STATUS_BAR.setText("URI: " + ((RDFSInfo) info).getURIStr() + "  TYPE: "
                        + ((RDFSInfo) info).getMetaClass());
            }
        } else {
            gmanager.getAttrDialog().setNullPanel();
            MR3.STATUS_BAR.setText("");
        }
    }

    private ClassPanel getClassPanel() {
        ClassPanel result = (ClassPanel) classPanelRef.get();
        if (result == null) {
            result = new ClassPanel(gmanager);
            classPanelRef = new WeakReference<ClassPanel>(result);
        }
        return result;
    }
}
