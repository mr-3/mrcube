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
import org.jgraph.graph.DefaultGraphCell;
import org.jgraph.graph.GraphConstants;
import org.mrcube.MR3;
import org.mrcube.jgraph.ClassGraphMarqueeHandler;
import org.mrcube.jgraph.GraphManager;
import org.mrcube.jgraph.RDFGraph;
import org.mrcube.jgraph.RDFGraphModel;
import org.mrcube.models.MR3Constants.GraphType;
import org.mrcube.models.RDFSModel;
import org.mrcube.utils.GraphUtilities;
import org.mrcube.views.ClassPanel;

import java.lang.ref.WeakReference;

/*
 *
 * @author Takeshi Morita
 *
 */
public class ClassEditor extends Editor {

    private WeakReference<ClassPanel> classPanelRef;
    private ClassGraphMarqueeHandler classGraphMarqueeHandler;

    public ClassEditor(GraphManager gm) {
        graph = new RDFGraph(gm, new RDFGraphModel(), GraphType.CLASS);
        graph.setFont(graphFont);
        graph.setDisconnectable(false);
        initEditor(graph, gm);
    }

    protected void initField(GraphManager manager) {
        super.initField(manager);
        classPanelRef = new WeakReference<>(null);
        classGraphMarqueeHandler = new ClassGraphMarqueeHandler(manager, graph);
        graph.setMarqueeHandler(classGraphMarqueeHandler);
    }

    public ClassGraphMarqueeHandler getClassGraphMarqueeHandler() {
        return classGraphMarqueeHandler;
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
            if (info instanceof RDFSModel) {
                if (gmanager.getAttrDialog().isVisible()) {
                    ClassPanel classPanel = getClassPanel();
                    classPanel.showRDFSInfo(cell);
                    gmanager.getAttrDialog().setContentPane(classPanel);
                    gmanager.getAttrDialog().validate();
                }
                MR3.STATUS_BAR.setText("URI: " + ((RDFSModel) info).getURIStr() + "  TYPE: "
                        + ((RDFSModel) info).getMetaClass());
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
            classPanelRef = new WeakReference<>(result);
        }
        return result;
    }
}
