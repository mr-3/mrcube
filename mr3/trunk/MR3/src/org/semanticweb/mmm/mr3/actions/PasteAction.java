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

package org.semanticweb.mmm.mr3.actions;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;

import org.jgraph.graph.*;
import org.semanticweb.mmm.mr3.data.*;
import org.semanticweb.mmm.mr3.data.MR3Constants.*;
import org.semanticweb.mmm.mr3.jgraph.*;
import org.semanticweb.mmm.mr3.ui.*;
import org.semanticweb.mmm.mr3.util.*;

/**
 * @author takeshi morita
 */

public class PasteAction extends AbstractAction {

    private RDFGraph graph;
    private GraphManager gmanager;
    private static final String TITLE = Translator.getString("Action.Paste.Text");
    private static final ImageIcon ICON = Utilities.getImageIcon(Translator.getString("Action.Paste.Icon"));

    public PasteAction(RDFGraph g, GraphManager gm) {
        super(TITLE, ICON);
        graph = g;
        gmanager = gm;
        putValue(SHORT_DESCRIPTION, TITLE);
        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_V, KeyEvent.CTRL_MASK));
    }

    private Object[] removeWrongCopyCells(Object[] cells, RDFGraph graph) {
        Set removeCells = new HashSet();
        Set correctCells = new HashSet();
        for (int i = 0; i < cells.length; i++) {
            Object cell = cells[i];
            if (graph.getType() == GraphType.CLASS) {
                if (RDFGraph.isRDFSClassCell(cell)) {
                    correctCells.add(cell);
                } else {
                    removeCells.add(cell);
                }
            } else if (graph.getType() == GraphType.PROPERTY) {
                if (RDFGraph.isRDFSPropertyCell(cell)) {
                    correctCells.add(cell);
                } else {
                    removeCells.add(cell);
                }
            } else if (graph.getType() == GraphType.RDF) {
                if (RDFGraph.isRDFCell(cell)) {
                    correctCells.add(cell);
                } else {
                    removeCells.add(cell);
                }
            }
        }
        graph.getGraphLayoutCache().removeCells(removeCells.toArray());
        return correctCells.toArray();
    }

    public void actionPerformed(ActionEvent e) {
        TransferHandler.getPasteAction().actionPerformed(new ActionEvent(graph, e.getID(), e.getActionCommand()));
        Object[] cells = graph.getDescendants(graph.getSelectionCells());
        cells = removeWrongCopyCells(cells, graph);
        for (int i = 0; i < cells.length; i++) {
            GraphCell cell = (GraphCell) cells[i];
            if (RDFGraph.isRDFSClassCell(cell)) {
                cloneRDFSClassCell(cell);
            } else if (RDFGraph.isRDFSPropertyCell(cell)) {
                cloneRDFSPropertyCell(cell);
            } else if (RDFGraph.isRDFResourceCell(cell)) {
                cloneRDFResourceCell(cell);
            } else if (RDFGraph.isRDFPropertyCell(cell)) {
                System.out.println(GraphConstants.getValue(cell.getAttributes()));
                // do nothing
            } else if (RDFGraph.isRDFLiteralCell(cell)) {
                cloneRDFLiteralCell(cell);
            }
        }
        graph.getGraphLayoutCache().reload(); // サイズの自動調節を反映させるため
        graph.clearSelection(); // 一度選択を解除しないと，サイズが自動調節されないため．
        graph.setSelectionCells(cells); // コピー後にセルを選択するため．
        if (graph.getType() == GraphType.RDF) {
            HistoryManager.saveHistory(HistoryType.PASTE_RDF_GRAPH);
        } else if (graph.getType() == GraphType.CLASS) {
            HistoryManager.saveHistory(HistoryType.PASTE_CLASS_GRAPH);
        } else if (graph.getType() == GraphType.PROPERTY) {
            HistoryManager.saveHistory(HistoryType.PASTE_PROPERTY_GRAPH);
        }
    }

    /**
     * @param cell
     */
    private void cloneRDFLiteralCell(GraphCell cell) {
        MR3Literal orgLiteral = (MR3Literal) GraphConstants.getValue(cell.getAttributes());
        MR3Literal newLiteral = new MR3Literal(orgLiteral);
        GraphConstants.setValue(cell.getAttributes(), newLiteral);
        Dimension dim = GraphUtilities.getAutoLiteralNodeDimention(gmanager, newLiteral.getString());
        GraphUtilities.resizeCell(dim, graph, cell);
    }

    /**
     * @param cell
     */
    private void cloneRDFResourceCell(GraphCell cell) {
        RDFResourceInfo orgInfo = (RDFResourceInfo) GraphConstants.getValue(cell.getAttributes());
        Object typeViewCell = orgInfo.getTypeViewCell();
        if (typeViewCell != null) {
            // RDFリソースのタイプを示す矩形セルのクローンを得る
            Map clones = graph.cloneCells(new Object[] { typeViewCell});
            typeViewCell = clones.get(typeViewCell);
        }
        RDFResourceInfo newInfo = new RDFResourceInfo(orgInfo);
        if (orgInfo.getURIType() != URIType.ANONYMOUS) {
            newInfo.setURI(cloneRDFURI(orgInfo));
        }
        newInfo.setTypeViewCell((GraphCell) typeViewCell);
        GraphConstants.setValue(cell.getAttributes(), newInfo);
        GraphUtilities.resizeRDFResourceCell(gmanager, newInfo, cell);
    }

    /**
     * @param cell
     */
    private void cloneRDFSPropertyCell(GraphCell cell) {
        PropertyInfo orgInfo = (PropertyInfo) GraphConstants.getValue(cell.getAttributes());
        PropertyInfo newInfo = new PropertyInfo(orgInfo);
        newInfo.setURI(cloneRDFSURI(newInfo, GraphType.PROPERTY));
        RDFSInfoMap rdfsInfoMap = gmanager.getCurrentRDFSInfoMap();
        rdfsInfoMap.putURICellMap(newInfo, cell);
        GraphConstants.setValue(cell.getAttributes(), newInfo);
        GraphUtilities.resizeRDFSResourceCell(gmanager, newInfo, cell);
    }

    /**
     * @param cell
     */
    private void cloneRDFSClassCell(GraphCell cell) {
        ClassInfo orgInfo = (ClassInfo) GraphConstants.getValue(cell.getAttributes());
        ClassInfo newInfo = new ClassInfo(orgInfo);
        newInfo.setURI(cloneRDFSURI(newInfo, GraphType.CLASS));
        RDFSInfoMap rdfsInfoMap = gmanager.getCurrentRDFSInfoMap();
        rdfsInfoMap.putURICellMap(newInfo, cell);
        GraphConstants.setValue(cell.getAttributes(), newInfo);
        GraphUtilities.resizeRDFSResourceCell(gmanager, newInfo, cell);
    }

    private String cloneRDFSURI(RDFSInfo info, GraphType graphType) {
        if (gmanager.isDuplicated(info.getURIStr(), null, graphType)) {
            for (int j = 1; true; j++) {
                String compURI = info.getURIStr() + "-copy" + j;
                if (!gmanager.isDuplicated(compURI, null, graphType)) { return info.getURIStr() + "-copy" + j; }
            }
        }
        return info.getURIStr();
    }

    /*
     * リソースが重複しないように，-copy番号をローカル名に追加する
     */
    private String cloneRDFURI(RDFResourceInfo info) {
        if (gmanager.isDuplicated(info.getURIStr(), null, GraphType.RDF)) {
            for (int j = 1; true; j++) {
                String compURI = info.getURIStr() + "-copy" + j;
                if (!gmanager.isDuplicated(compURI, null, GraphType.RDF)) { return info.getURIStr() + "-copy" + j; }
            }
        }
        return info.getURIStr();
    }
}
