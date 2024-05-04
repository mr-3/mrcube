/*
 * Project Name: MR^3 (Meta-Model Management based on RDFs Revision Reflection)
 * Project Website: http://mrcube.org/
 *
 * Copyright (C) 2003-2022 Takeshi Morita. All rights reserved.
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
import org.mrcube.views.InsertInstanceDialog;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

/**
 * @author Takeshi Morita
 */
public class PropertyGraphMarqueeHandler extends RDFGraphMarqueeHandler {

    private final InsertPropertyAction insertPropertyAction;
    private static final String INSERT_PROPERTY_TITLE = Translator.getString("InsertPropertyDialog.Title");

    public PropertyGraphMarqueeHandler(GraphManager gm, RDFGraph propGraph) {
        super(gm, propGraph);
        insertPropertyAction = new InsertPropertyAction();
        setAction();
    }

    private void setAction() {
        ActionMap actionMap = graph.getActionMap();
        actionMap.put(insertPropertyAction.getValue(Action.NAME), insertPropertyAction);
        InputMap inputMap = graph.getInputMap(JComponent.WHEN_FOCUSED);
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_I, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()),
                insertPropertyAction.getValue(Action.NAME));
        setCopyCutPasteAction(actionMap, inputMap);
    }

    // 接続するかどうか
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
        InsertInstanceDialog dialog = getInsertRDFSResDialog(INSERT_PROPERTY_TITLE);
        if (!dialog.isConfirm()) {
            return null;
        }
        String uri = dialog.getURI();
        if (PrefixNSUtil.isValidURI(uri) && !gmanager.isDuplicatedWithDialog(uri, null, GraphType.Property)) {
            return cellMaker.insertProperty(pt, uri);
        }
        return null;
    }

    private GraphCell insertSubProperty(Point pt, Object[] supCells) {
        InsertInstanceDialog dialog = getInsertRDFSResDialog(INSERT_PROPERTY_TITLE);
        if (!dialog.isConfirm()) {
            return null;
        }
        String uri = dialog.getURI();
        if (PrefixNSUtil.isValidURI(uri) && !gmanager.isDuplicatedWithDialog(uri, null, GraphType.Property)) {
            DefaultGraphCell cell = cellMaker.insertProperty(pt, uri);
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
            menu.add(new TransformElementAction(graph, gmanager, GraphType.Property, GraphType.Instance));
            menu.add(new TransformElementAction(graph, gmanager, GraphType.Property,
                    GraphType.Class));
        }
    }

    class InsertPropertyAction extends AbstractAction {
        InsertPropertyAction() {
            super(INSERT_PROPERTY_TITLE, Editor.RESOURCE_ICON);
//            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_I,
//                    Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
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

    public JPopupMenu createPopupMenu(final Point pt, final Object cell) {
        JPopupMenu menu = new JPopupMenu();

        menu.add(insertPropertyAction);

        addTransformMenu(menu, cell);
        addEditMenu(menu, cell);
        menu.add(new ShowAttrDialog());

        return menu;
    }
}
