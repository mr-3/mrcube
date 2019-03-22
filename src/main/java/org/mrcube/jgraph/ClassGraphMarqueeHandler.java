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

package org.mrcube.jgraph;

import org.jgraph.graph.DefaultGraphCell;
import org.jgraph.graph.DefaultPort;
import org.jgraph.graph.GraphCell;
import org.jgraph.graph.Port;
import org.mrcube.actions.TransformElementAction;
import org.mrcube.editors.Editor;
import org.mrcube.models.MR3Constants.GraphType;
import org.mrcube.models.MR3Constants.HistoryType;
import org.mrcube.utils.PrefixNSUtil;
import org.mrcube.utils.Translator;
import org.mrcube.views.HistoryManager;
import org.mrcube.views.InsertRDFSResourceDialog;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

/*
 *
 * @author Takeshi Morita
 *
 */
public class ClassGraphMarqueeHandler extends RDFGraphMarqueeHandler {

    private final InsertClassAction insertClassAction;
    private static final String INSERT_CLASS_TITLE = Translator.getString("InsertClassDialog.Title");

    public ClassGraphMarqueeHandler(GraphManager gm, RDFGraph classGraph) {
        super(gm, classGraph);
        insertClassAction = new InsertClassAction();
        setAction(graph);
    }

    private void setAction(JComponent panel) {
        ActionMap actionMap = panel.getActionMap();
        actionMap.put(insertClassAction.getValue(Action.NAME), insertClassAction);
        InputMap inputMap = panel.getInputMap(JComponent.WHEN_FOCUSED);
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_I, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()),
                insertClassAction.getValue(Action.NAME));
        setCopyCutPasteAction(actionMap, inputMap);
    }

    // connectするかどうかをここで制御
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
        InsertRDFSResourceDialog dialog = getInsertRDFSResDialog(INSERT_CLASS_TITLE);
        if (!dialog.isConfirm()) {
            return null;
        }
        String uri = dialog.getURI();
        if (PrefixNSUtil.isValidURI(uri) && !gmanager.isDuplicatedWithDialog(uri, null, GraphType.CLASS)) {
            return cellMaker.insertClass(pt, uri);
        }
        return null;
    }

    private GraphCell insertSubClass(Point pt, Object[] supCells) {
        InsertRDFSResourceDialog dialog = getInsertRDFSResDialog(INSERT_CLASS_TITLE);
        if (!dialog.isConfirm()) {
            return null;
        }
        String uri = dialog.getURI();
        if (PrefixNSUtil.isValidURI(uri) && !gmanager.isDuplicatedWithDialog(uri, null, GraphType.CLASS)) {
            DefaultGraphCell cell = cellMaker.insertClass(pt, uri);
            Port subPort = (Port) cell.getChildAt(0);
            cellMaker.connectSubToSups(subPort, supCells, graph);
            graph.setSelectionCell(cell);
            return cell;
        }
        return null;
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
            super(INSERT_CLASS_TITLE, Editor.RESOURCE_ICON);
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_I,
                    Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
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

    /**
     * create PopupMenu
     */
    public JPopupMenu createPopupMenu(final Point pt, final Object cell) {
        JPopupMenu menu = new JPopupMenu();

        menu.add(insertClassAction);

        addTransformMenu(menu, cell);
        addEditMenu(menu, cell);
        menu.add(new ShowAttrDialog());

        return menu;
    }
}
