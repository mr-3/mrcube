/*
 * @(#) PropertyGraphMarqueeHandler
 *
 * Copyright (C) 2003 The MMM Project
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 */

package org.semanticweb.mmm.mr3.jgraph;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import org.jgraph.graph.*;
import org.semanticweb.mmm.mr3.actions.*;
import org.semanticweb.mmm.mr3.data.*;
import org.semanticweb.mmm.mr3.data.MR3Constants.*;
import org.semanticweb.mmm.mr3.ui.*;
import org.semanticweb.mmm.mr3.util.*;

/**
 * @author takeshi morita
 */
public class PropertyGraphMarqueeHandler extends RDFGraphMarqueeHandler {

    private InsertPropertyAction insertPropertyAction;
    private static String INSERT_PROPERTY_TITLE = Translator.getString("InsertPropertyDialog.Title");

    public PropertyGraphMarqueeHandler(GraphManager gm) {
        super(gm, gm.getPropertyGraph());
        insertPropertyAction = new InsertPropertyAction();
        setAction(graph);
    }

    private void setAction(JComponent panel) {
        ActionMap actionMap = panel.getActionMap();
        actionMap.put(insertPropertyAction.getValue(Action.NAME), insertPropertyAction);
        InputMap inputMap = panel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        inputMap.put(KeyStroke.getKeyStroke("control R"), insertPropertyAction.getValue(Action.NAME));
    }

    // ê⁄ë±Ç∑ÇÈÇ©Ç«Ç§Ç©
    public void mouseReleased(MouseEvent e) {
        if (e != null && !e.isConsumed() && port != null && firstPort != null && firstPort != port
                && isEllipseView(firstPort.getParentView())) {
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
        InsertRDFSResDialog dialog = getInsertRDFSResDialog(INSERT_PROPERTY_TITLE);
        if (!dialog.isConfirm()) { return null; }
        String uri = dialog.getURI();
        if (uri == null || gmanager.isEmptyURI(uri) || gmanager.isDuplicatedWithDialog(uri, null, GraphType.PROPERTY)) { return null; }
        return cellMaker.insertProperty(pt, uri);
    }

    public GraphCell insertSubProperty(Point pt, Object[] supCells) {
        InsertRDFSResDialog dialog = getInsertRDFSResDialog(INSERT_PROPERTY_TITLE);
        if (!dialog.isConfirm()) { return null; }
        String uri = dialog.getURI();
        if (uri == null || gmanager.isEmptyURI(uri) || gmanager.isDuplicatedWithDialog(uri, null, GraphType.CLASS)) { return null; }
        DefaultGraphCell cell = cellMaker.insertProperty(pt, uri);
        Port subPort = (Port) cell.getChildAt(0);
        cellMaker.connectSubToSups(subPort, supCells, graph);
        graph.setSelectionCell(cell);
        return cell;
    }

    private void addTransformMenu(JPopupMenu menu, Object cell) {
        if (isCellSelected(cell)) {
            menu.addSeparator();
            menu.add(new TransformElementAction(graph, gmanager, GraphType.PROPERTY, GraphType.RDF));
            menu.add(new TransformElementAction(graph, gmanager, GraphType.PROPERTY, GraphType.CLASS));
        }
    }

    class InsertPropertyAction extends AbstractAction {
        InsertPropertyAction() {
            super(INSERT_PROPERTY_TITLE, ELLIPSE_ICON);
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_R, KeyEvent.CTRL_MASK));
        }

        public void actionPerformed(ActionEvent ev) {
            Object[] supCells = graph.getSelectionCells();
            supCells = graph.getDescendants(supCells);
            GraphCell cell = insertSubProperty(insertPoint, supCells);
            if (cell != null) {
                HistoryManager.saveHistory(HistoryType.INSERT_ONT_PROPERTY, cell);
            }
        }
    }

    /** create PopupMenu */
    public JPopupMenu createPopupMenu(final Point pt, final Object cell) {
        JPopupMenu menu = new JPopupMenu();

        menu.add(insertPropertyAction);
        menu.addSeparator();
        addConnectORMoveMenu(menu);

        addTransformMenu(menu, cell);
        addEditMenu(menu, cell);
        menu.add(new ShowAttrDialog());

        return menu;
    }
}
