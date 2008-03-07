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

package org.semanticweb.mmm.mr3.jgraph;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import org.jgraph.graph.*;
import org.semanticweb.mmm.mr3.actions.*;
import org.semanticweb.mmm.mr3.data.MR3Constants.*;
import org.semanticweb.mmm.mr3.ui.*;
import org.semanticweb.mmm.mr3.util.*;

/*
 * 
 * @author takeshi morita
 * 
 */
public class ClassGraphMarqueeHandler extends RDFGraphMarqueeHandler {

    private InsertClassAction insertClassAction;
    private static String INSERT_CLASS_TITLE = Translator.getString("InsertClassDialog.Title");

    public ClassGraphMarqueeHandler(GraphManager gm, RDFGraph classGraph) {
        super(gm, classGraph);
        insertClassAction = new InsertClassAction();
        setAction(graph);
    }

    private void setAction(JComponent panel) {
        ActionMap actionMap = panel.getActionMap();
        actionMap.put(insertClassAction.getValue(Action.NAME), insertClassAction);
        InputMap inputMap = panel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        inputMap.put(KeyStroke.getKeyStroke("control R"), insertClassAction.getValue(Action.NAME));
    }

    // connectÇ∑ÇÈÇ©Ç«Ç§Ç©ÇÇ±Ç±Ç≈êßå‰
    public void mouseReleased(MouseEvent e) {
        if (e != null && !e.isConsumed() && port != null && firstPort != null && firstPort != port) {
            Port source = (Port) firstPort.getCell();
            DefaultPort target = (DefaultPort) port.getCell();
            cellMaker.connect(source, target, null, graph);
            // graph.setSelectionCell(graph.getModel().getParent(source));
            e.consume();
        } else {
            graph.repaint();
        }

        firstPort = port = null;
        start = current = null;

        super.mouseReleased(e);
    }

    public GraphCell insertResourceCell(Point pt) {
        InsertRDFSResDialog dialog = getInsertRDFSResDialog(INSERT_CLASS_TITLE);
        if (!dialog.isConfirm()) { return null; }
        String uri = dialog.getURI();
        if (uri == null || gmanager.isEmptyURI(uri) || gmanager.isDuplicatedWithDialog(uri, null, GraphType.CLASS)) { return null; }
        return cellMaker.insertClass(pt, uri);
    }

    public GraphCell insertSubClass(Point pt, Object[] supCells) {
        InsertRDFSResDialog dialog = getInsertRDFSResDialog(INSERT_CLASS_TITLE);
        if (!dialog.isConfirm()) { return null; }
        String uri = dialog.getURI();
        if (uri == null || gmanager.isEmptyURI(uri) || gmanager.isDuplicatedWithDialog(uri, null, GraphType.CLASS)) { return null; }
        DefaultGraphCell cell = cellMaker.insertClass(pt, uri);
        Port subPort = (Port) cell.getChildAt(0);
        cellMaker.connectSubToSups(subPort, supCells, graph);
        graph.setSelectionCell(cell);

        return cell;
    }

    private void addTransformMenu(JPopupMenu menu, Object cell) {
        if (isCellSelected(cell)) {
            menu.addSeparator();
            menu.add(new TransformElementAction(graph, gmanager, GraphType.CLASS, GraphType.RDF));
            menu.add(new TransformElementAction(graph, gmanager, GraphType.CLASS, GraphType.PROPERTY));
        }
    }

    class InsertClassAction extends AbstractAction {
        InsertClassAction() {
            super(INSERT_CLASS_TITLE, RECTANGLE_ICON);
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_R, KeyEvent.CTRL_MASK));
        }
        public void actionPerformed(ActionEvent ev) {
            Object[] supCells = graph.getSelectionCells();
            supCells = graph.getDescendants(supCells);
            GraphCell cell = insertSubClass(insertPoint, supCells);
            if (cell != null) {
                HistoryManager.saveHistory(HistoryType.INSERT_CLASS, cell);
            }
        }
    }

    /** create PopupMenu */
    public JPopupMenu createPopupMenu(final Point pt, final Object cell) {
        JPopupMenu menu = new JPopupMenu();

        menu.add(insertClassAction);
        menu.addSeparator();
        addConnectORMoveMenu(menu);

        addTransformMenu(menu, cell);
        addEditMenu(menu, cell);
        menu.add(new ShowAttrDialog());

        return menu;
    }
}
