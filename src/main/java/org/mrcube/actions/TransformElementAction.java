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

import org.jgraph.graph.GraphCell;
import org.jgraph.graph.GraphConstants;
import org.mrcube.jgraph.GraphManager;
import org.mrcube.jgraph.RDFGraph;
import org.mrcube.models.MR3Constants.GraphType;
import org.mrcube.models.MR3Constants.URIType;
import org.mrcube.models.RDFResourceModel;
import org.mrcube.models.RDFSModel;
import org.mrcube.utils.MR3CellMaker;
import org.mrcube.utils.Translator;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Takeshi Morita
 */
public class TransformElementAction extends AbstractAction {

    private Set<String> uriSet;
    private final RDFGraph graph;
    private final GraphType fromGraphType;
    private final GraphType toGraphType;
    private final GraphManager gmanager;

    // private RDFSModelMap rdfsInfoMap = RDFSModelMap.getInstance();

    public TransformElementAction(RDFGraph g, GraphManager gm, GraphType fromType, GraphType toType) {
        super(Translator.getString("Action.TransformElement." + fromType + "To" + toType + ".Text"));
        graph = g;
        gmanager = gm;
        fromGraphType = fromType;
        toGraphType = toType;
    }

    private void setURISet() {
        uriSet = new HashSet<>();
        Object[] cells = graph.getDescendants(graph.getSelectionCells());
        for (Object cell1 : cells) {
            GraphCell cell = (GraphCell) cell1;

            if (fromGraphType == GraphType.RDF && RDFGraph.isRDFResourceCell(cell)) {
                RDFResourceModel info = (RDFResourceModel) GraphConstants.getValue(cell.getAttributes());
                uriSet.add(info.getURIStr());
            } else if (fromGraphType == GraphType.CLASS && RDFGraph.isRDFSClassCell(cell)) {
                RDFSModel info = (RDFSModel) GraphConstants.getValue(cell.getAttributes());
                uriSet.add(info.getURIStr());
            } else if (fromGraphType == GraphType.PROPERTY && RDFGraph.isRDFSPropertyCell(cell)) {
                RDFSModel info = (RDFSModel) GraphConstants.getValue(cell.getAttributes());
                uriSet.add(info.getURIStr());
            }
            // RDFSプロパティとクラスが重複してしまうため，複雑な処理が必要．
            // else if (graph.isRDFPropertyCell(cell)) {
            // Object propCell = rdfsInfoMap.getEdgeInfo(cell);
            // RDFSModel info = rdfsInfoMap.getCellInfo(propCell);
            // resSet.add(info.getURI());
            // }
        }
        // System.out.println(uriSet);
    }

    private void insertElements(Set<String> uriSet) {
        Point pt = new Point(100, 100);
        MR3CellMaker cellMaker = new MR3CellMaker(gmanager);
        for (String uri : uriSet) {
            switch (toGraphType) {
            case RDF:
                cellMaker.insertRDFResource(pt, uri, null, URIType.URI);
                break;
            case CLASS:
                cellMaker.insertClass(pt, uri);
                break;
            case PROPERTY:
                cellMaker.insertProperty(pt, uri);
                break;
            }
            pt.x += 20;
            pt.y += 20;
        }
    }

    class TransformThread extends Thread {
        public void run() {
            while (gmanager.getRemoveDialog().isVisible()) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException ie) {
                    ie.printStackTrace();
                }
            }
            if (isRmCellsRemoved()) {
                insertElements(uriSet);
            }
        }
    }

    private boolean isRmCellsRemoved() {
        for (Object cell : gmanager.getRemoveCells()) {
            if (gmanager.getCurrentRDFGraph().getModel().contains(cell)
                    || gmanager.getCurrentClassGraph().getModel().contains(cell)
                    || gmanager.getCurrentPropertyGraph().getModel().contains(cell)) { return false; }
        }
        return true;
    }

    public void actionPerformed(ActionEvent e) {
        setURISet();
        gmanager.removeAction(graph);
        // 削除した時に，メタモデル管理が行われるが，その間にinsertされないようにするための仕掛け
        // モーダルにできれば，いいが．．．
        new TransformThread().start();
    }

}
