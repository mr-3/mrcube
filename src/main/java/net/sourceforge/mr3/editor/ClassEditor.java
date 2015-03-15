/*
 * Project Name: MR^3 (Meta-Model Management based on RDFs Revision Reflection)
 * Project Website: http://mr3.sourceforge.net/
 * 
 * Copyright (C) 2003-2015 Yamaguchi Laboratory, Keio University. All rights reserved. 
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

package net.sourceforge.mr3.editor;

import java.lang.ref.*;

import net.sourceforge.mr3.*;
import net.sourceforge.mr3.data.*;
import net.sourceforge.mr3.data.MR3Constants.*;
import net.sourceforge.mr3.jgraph.*;
import net.sourceforge.mr3.ui.*;
import net.sourceforge.mr3.util.*;

import org.jgraph.event.*;
import org.jgraph.graph.*;

/*
 * 
 * @author Takeshi Morita
 * 
 */
public class ClassEditor extends Editor {

    private WeakReference<ClassPanel> classPanelRef;

    public ClassEditor(GraphManager gm) {
        graph = new RDFGraph(gm, new RDFGraphModel(), GraphType.CLASS);
        graph.setFont(graphFont);
        graph.setDisconnectable(false);
        initEditor(graph, gm);
    }

    protected void initField(GraphManager manager) {
        super.initField(manager);
        classPanelRef = new WeakReference<ClassPanel>(null);
        graph.setMarqueeHandler(new ClassGraphMarqueeHandler(manager, graph));
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
        ClassPanel result = classPanelRef.get();
        if (result == null) {
            result = new ClassPanel(gmanager);
            classPanelRef = new WeakReference<ClassPanel>(result);
        }
        return result;
    }
}
