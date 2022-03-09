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

import org.jgraph.graph.*;
import org.mrcube.MR3;
import org.mrcube.actions.RemoveAction;
import org.mrcube.actions.TransformElementAction;
import org.mrcube.editors.Editor;
import org.mrcube.models.MR3Constants.GraphType;
import org.mrcube.models.MR3Constants.HistoryType;
import org.mrcube.models.MR3Constants.URIType;
import org.mrcube.models.MR3Resource;
import org.mrcube.models.PropertyModel;
import org.mrcube.models.RDFResourceModel;
import org.mrcube.models.RDFSModel;
import org.mrcube.utils.MR3CellMaker;
import org.mrcube.utils.PrefixNSUtil;
import org.mrcube.utils.Translator;
import org.mrcube.utils.Utilities;
import org.mrcube.views.HistoryManager;
import org.mrcube.views.InsertRDFSResourceDialog;
import org.mrcube.views.rdf_editor.InsertRDFLiteralDialog;
import org.mrcube.views.rdf_editor.InsertRDFResourceDialog;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Takeshi Morita
 */
public class RDFGraphMarqueeHandler extends BasicMarqueeHandler {

    final RDFGraph graph;
    final MR3CellMaker cellMaker;
    final GraphManager gmanager;

    Point2D start;
    Point2D current;
    PortView port;
    PortView firstPort;

    private WeakReference<InsertRDFResourceDialog> insertRDFResDialogRef;
    private WeakReference<InsertRDFLiteralDialog> insertRDFLiteralDialogRef;
    private WeakReference<InsertRDFSResourceDialog> insertRDFSResDialogRef;

    protected boolean isConnectMode;

    private final InsertResourceAction insertResourceAction;
    private final InsertLiteralAction insertLiteralAction;
    private final RemoveAction removeAction;

    private static final String INSERT_RESOURCE_TITLE = Translator.getString("InsertResourceDialog.Title");
    private static final String INSERT_LITERAL_TITLE = Translator.getString("InsertLiteralDialog.Title");

    public RDFGraphMarqueeHandler(GraphManager manager, RDFGraph graph) {
        gmanager = manager;
        this.graph = graph;
        insertRDFResDialogRef = new WeakReference<>(null);
        insertRDFLiteralDialogRef = new WeakReference<>(null);
        insertRDFSResDialogRef = new WeakReference<>(null);
        cellMaker = new MR3CellMaker(gmanager);
        insertResourceAction = new InsertResourceAction();
        insertLiteralAction = new InsertLiteralAction();
        removeAction = new RemoveAction(graph, gmanager);
        setAction(graph);
    }

    public MR3CellMaker getCellMaker() {
        return cellMaker;
    }


    protected void setCopyCutPasteAction(ActionMap actionMap, InputMap inputMap) {
        actionMap.put(graph.getCopyAction().getValue(Action.NAME), graph.getCopyAction());
        actionMap.put(graph.getCutAction().getValue(Action.NAME), graph.getCutAction());
        actionMap.put(graph.getPasteAction().getValue(Action.NAME), graph.getPasteAction());
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_C, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()),
                graph.getCopyAction().getValue(Action.NAME));
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_X, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()),
                graph.getCutAction().getValue(Action.NAME));
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_V, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()),
                graph.getPasteAction().getValue(Action.NAME));
    }

    private void setAction(JComponent panel) {
        ActionMap actionMap = panel.getActionMap();
        actionMap.put(insertResourceAction.getValue(Action.NAME), insertResourceAction);
        actionMap.put(insertLiteralAction.getValue(Action.NAME), insertLiteralAction);
        InputMap inputMap = panel.getInputMap(JComponent.WHEN_FOCUSED);
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_I, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()),
                insertResourceAction.getValue(Action.NAME));
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_L, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()),
                insertLiteralAction.getValue(Action.NAME));
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_A, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()),
                graph.getSelectAllNodesAction().getValue(Action.NAME));
        setCopyCutPasteAction(actionMap, inputMap);
    }

    private InsertRDFResourceDialog getInsertRDFResDialog(Object[] cells) {
        InsertRDFResourceDialog result = insertRDFResDialogRef.get();
        if (result == null) {
            result = new InsertRDFResourceDialog(gmanager);
            insertRDFResDialogRef = new WeakReference<>(result);
        }
        result.initData(cells);
        return result;
    }

    private InsertRDFLiteralDialog getInsertRDFLiteralDialog() {
        InsertRDFLiteralDialog result = insertRDFLiteralDialogRef.get();
        if (result == null) {
            result = new InsertRDFLiteralDialog(gmanager.getRootFrame());
            insertRDFLiteralDialogRef = new WeakReference<>(result);
        }
        result.initData();
        return result;
    }

    InsertRDFSResourceDialog getInsertRDFSResDialog(String title) {
        InsertRDFSResourceDialog result = insertRDFSResDialogRef.get();
        if (result == null) {
            result = new InsertRDFSResourceDialog(gmanager);
            insertRDFSResDialogRef = new WeakReference<>(result);
        }
        result.initData(title);
        return result;
    }

    private boolean isPopupTrigger(MouseEvent e) {
        return SwingUtilities.isRightMouseButton(e) && !e.isShiftDown();
    }

    public boolean isForceMarqueeEvent(MouseEvent e) {
        return isConnectMode || isPopupTrigger(e) || super.isForceMarqueeEvent(e);
    }

    // Display PopupMenu or Remember Start Location and First Port
    public void mousePressed(final MouseEvent e) {
        if (isPopupTrigger(e)) { // If Right Mouse Button
            Point2D loc = graph.fromScreen(e.getPoint());
            Object cell = graph.getFirstCellForLocation(loc.getX(), loc.getY());
            JPopupMenu menu = createPopupMenu(e.getPoint(), cell);
            menu.show(graph, e.getX(), e.getY());
        } else if (port != null && !e.isConsumed() && isConnectMode) {
            start = graph.snap(e.getPoint());
            firstPort = port;
            if (firstPort != null) {
                start = graph.toScreen(firstPort.getLocation(null));
            }
            e.consume();
        } else {
            super.mousePressed(e);
        }
    }

    private void overlay(Graphics g) {
        super.overlay(graph, g, true);
        if (start != null) {
            if (isConnectMode && current != null) {
                g.drawLine((int) start.getX(), (int) start.getY(), (int) current.getX(),
                        (int) current.getY());
            }
        }
    }

    private boolean isConnectMode(MouseEvent event) {
        var sp = graph.fromScreen(new Point2D.Double(event.getX(), event.getY()));
        var connectPort = graph.getPortViewAt(sp.getX(), sp.getY());
        return connectPort != null;
    }

    private void setCursor(MouseEvent event) {
        if (isConnectMode(event)) {
            graph.setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));
        } else {
            graph.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        }
    }

    // Find Port under Mouse and Repaint Connector
    public void mouseDragged(MouseEvent event) {
        setCursor(event);
        if (!event.isConsumed() && isConnectMode) {
            Graphics g = graph.getGraphics();
            Color bg = graph.getBackground();
            Color fg = Color.black;
            g.setColor(fg);
            g.setXORMode(bg);
            overlay(g);
            current = graph.snap(event.getPoint());
            if (isConnectMode) {
                port = getPortViewAt(event.getX(), event.getY(), !event.isShiftDown());
                if (port != null) {
                    current = graph.toScreen(port.getLocation(null));
                }
            }
            g.setColor(bg);
            g.setXORMode(fg);
            overlay(g);
            event.consume();
        } else {
            graph.setMarqueeColor(Color.black);
            super.mouseDragged(event);
        }
    }

    /*
     * Resource(Ellipse)のときにtrue、Literal(Veretex)のときfalse
     */
    boolean isEllipseView(CellView v) {
        return v instanceof JGraphEllipseView;
    }

    public void mouseReleased(MouseEvent e) {
        setCursor(e);
        if (e != null && !e.isConsumed() && port != null && firstPort != null && firstPort != port
                && isEllipseView(firstPort.getParentView())) {
            Port source = (Port) firstPort.getCell();
            Port target = (Port) port.getCell();

            Set<Port> portSet = new HashSet<>();
            portSet.add(source);
            connectCells(portSet, target);
            e.consume(); // Consume Event
        }

        firstPort = port = null;
        start = current = null;
        super.mouseReleased(e);
    }

    private PortView getPortViewAt(int x, int y, boolean jump) {
        Point2D sp = graph.fromScreen(new Point2D.Double(x, y));
        PortView port = graph.getPortViewAt(sp.getX(), sp.getY());
        // Shift Jumps to "Default" Port (child index 0)
        if (port == null && jump) {
            Object cell = graph.getFirstCellForLocation(x, y);
            if (RDFGraph.isRDFSCell(cell) || RDFGraph.isRDFResourceCell(cell) || RDFGraph.isRDFLiteralCell(cell)) {
                Object firstChild = graph.getModel().getChild(cell, 0);
                CellView firstChildView = graph.getGraphLayoutCache().getMapping(firstChild, false);
                if (firstChildView instanceof PortView)
                    port = (PortView) firstChildView;
            }
        }
        return port;
    }

    Point insertPoint = new Point(10, 10);

    public void mouseMoved(MouseEvent event) {
        MR3.getProjectPanel().displayEditorInFront(graph.getType());

        insertPoint = event.getPoint();
        setCursor(event);
        isConnectMode = isConnectMode(event);
        if (isConnectMode) {
            if (!event.isConsumed()) {
                event.consume();
                PortView oldPort = port;
                PortView newPort = getPortViewAt(event.getX(), event.getY(), !event.isShiftDown());
                if (oldPort != newPort) {
                    Graphics g = graph.getGraphics();
                    Color bg = graph.getBackground();
                    Color fg = graph.getMarqueeColor();
                    g.setColor(fg);
                    g.setXORMode(bg);
                    overlay(g);
                    port = newPort;
                    g.setColor(bg);
                    overlay(g);
                }
            }
        } else {
            super.mouseMoved(event);
        }
    }

    public GraphCell insertResourceCell(Point pt) {
        List<Object> list = new ArrayList<>();
        list.add(null); // リソースのタイプが空の場合
        for (Object cell : gmanager.getClassGraph().getAllCells()) {
            if (RDFGraph.isRDFSClassCell(cell)) {
                list.add(cell);
            }
        }
        InsertRDFResourceDialog dialog = getInsertRDFResDialog(Utilities.getSortedCellSet(list.toArray()));
        if (!dialog.isConfirm()) {
            return null;
        }

        String uri = dialog.getURI();
        Object resTypeCell = dialog.getResourceType();

        if (dialog.isAnonymous()) {
            return cellMaker.insertRDFResource(pt, uri, resTypeCell, URIType.ANONYMOUS);
        } else if (PrefixNSUtil.isValidURI(uri) && !gmanager.isDuplicatedWithDialog(uri, null, GraphType.RDF)) {
            return cellMaker.insertRDFResource(pt, uri, resTypeCell, URIType.URI);
        }
        return null;
    }

    public Set getSelectedResourcePorts() {
        Object[] cells = graph.getSelectionCells();
        cells = graph.getDescendants(cells);
        Set<Object> selectedResourcePorts = new HashSet<>();
        for (Object cell : cells) {
            if (RDFGraph.isRDFResourceCell(cell)) {
                DefaultGraphCell gcell = (DefaultGraphCell) cell;
                selectedResourcePorts.add(gcell.getChildAt(0));
            }
        }
        return selectedResourcePorts;
    }

    private void insertConnectedResource(Point pt) {
        Set selectedResourcePorts = getSelectedResourcePorts();
        DefaultGraphCell targetCell = (DefaultGraphCell) insertResourceCell(pt);
        if (targetCell == null) {
            return;
        }
        Port targetPort = (Port) targetCell.getChildAt(0);
        connectCells(selectedResourcePorts, targetPort);
        if (targetCell.getParent() != null) {
            graph.setSelectionCell(targetCell.getParent());
        } else {
            graph.setSelectionCell(targetCell);
        }

        if (graph.getType() == GraphType.RDF) {
            if (selectedResourcePorts.size() == 0) {
                HistoryManager.saveHistory(HistoryType.INSERT_RESOURCE, targetCell);
            } else if (0 < selectedResourcePorts.size()) {
                HistoryManager.saveHistory(HistoryType.INSERT_CONNECTED_RESOURCE, targetCell);
            }
        } else {
            HistoryManager.saveHistory(HistoryType.INSERT_CONNECTED_ONT_PROPERTY, targetCell);
        }
    }

    private GraphCell insertConnectedLiteral(Point pt) {
        Set selectedResourcePorts = getSelectedResourcePorts();
        DefaultGraphCell targetCell = (DefaultGraphCell) insertLiteralCell(pt);
        if (targetCell == null) {
            return null;
        }
        Port targetPort = (Port) targetCell.getChildAt(0);
        connectCells(selectedResourcePorts, targetPort);
        graph.setSelectionCell(targetCell);

        return targetCell;
    }

    public void connectCells(Set selectedResourcePorts, Port targetPort) {
        for (Object selectedResourcePort : selectedResourcePorts) {
            Port sourcePort = (Port) selectedResourcePort;
            GraphCell rdfsPropCell = null;
            Object[] rdfsPropertyCells = gmanager.getPropertyGraph().getSelectionCells();

            RDFSModel info = null;
            if (rdfsPropertyCells.length == 1 && RDFGraph.isRDFSPropertyCell(rdfsPropertyCells[0])) {
                rdfsPropCell = (GraphCell) rdfsPropertyCells[0];
                info = (RDFSModel) GraphConstants.getValue(rdfsPropCell.getAttributes());
                if (MR3.OFF_META_MODEL_MANAGEMENT) {
                    PropertyModel pInfo = (PropertyModel) info;
                    info = new PropertyModel(pInfo.getURIStr());
                }
            } else {
                info = new PropertyModel(MR3Resource.Nil.getURI());
            }
            cellMaker.connect(sourcePort, targetPort, info, graph);
            HistoryManager.saveHistory(HistoryType.INSERT_PROPERTY, info, (GraphCell) graph.getModel().getParent(sourcePort),
                    (GraphCell) graph.getModel().getParent(targetPort));
        }
    }

    public GraphCell insertLiteralCell(Point pt) {
        InsertRDFLiteralDialog insertLiteralDialog = getInsertRDFLiteralDialog();
        if (!insertLiteralDialog.isConfirm()) {
            return null;
        }
        return cellMaker.insertRDFLiteral(pt, insertLiteralDialog.getLiteral());
    }

    boolean isCellSelected(Object cell) {
        // cell != nullの判定がないと，一つだけセルを選択したときに，メニューが表示されない．
        return (cell != null || !graph.isSelectionEmpty());
    }

    private void addTransformMenu(JPopupMenu menu, Object cell) {
        if (isCellSelected(cell)) {
            menu.addSeparator();
            menu.add(new TransformElementAction(graph, gmanager, GraphType.RDF, GraphType.CLASS));
            menu.add(new TransformElementAction(graph, gmanager, GraphType.RDF, GraphType.PROPERTY));
        }
    }

    void addEditMenu(JPopupMenu menu, Object cell) {
        menu.addSeparator();
        menu.add(graph.getCopyAction());
        menu.add(graph.getCutAction());
        menu.add(graph.getPasteAction());

        if (isCellSelected(cell)) {
            menu.addSeparator();
            menu.add(removeAction);
        }
        menu.addSeparator();
    }

    class InsertResourceAction extends AbstractAction {

        InsertResourceAction() {
            super(INSERT_RESOURCE_TITLE, Editor.RESOURCE_ICON);
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_I,
                    Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
        }

        public void actionPerformed(ActionEvent ev) {
            insertConnectedResource(insertPoint);
        }
    }

    class InsertLiteralAction extends AbstractAction {

        InsertLiteralAction() {
            super(INSERT_LITERAL_TITLE, Editor.LITERAL_ICON);
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_L,
                    Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
        }

        public void actionPerformed(ActionEvent ev) {
            GraphCell cell = insertConnectedLiteral(insertPoint);
            if (cell != null) {
                HistoryManager.saveHistory(HistoryType.INSERT_CONNECTED_LITERAL, cell);
            }
        }
    }

    /**
     * create PopupMenu
     */
    JPopupMenu createPopupMenu(final Point pt, final Object cell) {
        JPopupMenu menu = new JPopupMenu();

        menu.add(insertResourceAction);
        menu.add(insertLiteralAction);

        addChangeResourceTypeMenu(menu);
        addChangePropertyMenu(menu);

        if (graph.isOneCellSelected(cell) && RDFGraph.isRDFResourceCell(cell)) {
            menu.add(new AbstractAction("Self Connect") {
                public void actionPerformed(ActionEvent e) {
                    Port port = (Port) ((DefaultGraphCell) cell).getChildAt(0);
                    cellMaker.selfConnect(port, null, graph);
                }
            });
        }
        addTransformMenu(menu, cell);
        addEditMenu(menu, cell);
        menu.add(new ShowAttrDialog());
        return menu;
    }


    private Object[] getSelectedRDFResourceCells() {
        RDFGraph rdfGraph = gmanager.getRDFGraph();
        Set<Object> resourceCells = new HashSet<>();
        for (Object rdfCell : rdfGraph.getDescendants(rdfGraph.getSelectionCells())) {
            if (RDFGraph.isRDFResourceCell(rdfCell)) {
                resourceCells.add(rdfCell);
            }
        }
        return resourceCells.toArray();
    }

    private Object[] getSelectedRDFPropertyCells() {
        RDFGraph rdfGraph = gmanager.getRDFGraph();
        Set<Object> rdfPropCells = new HashSet<>();

        for (Object rdfCell : rdfGraph.getDescendants(rdfGraph.getSelectionCells())) {
            if (RDFGraph.isRDFPropertyCell(rdfCell)) {
                rdfPropCells.add(rdfCell);
            }
        }
        return rdfPropCells.toArray();
    }

    private void addChangeResourceTypeMenu(JPopupMenu menu) {
        Object[] classCells = gmanager.getClassGraph().getSelectionCells();
        Object[] resCells = getSelectedRDFResourceCells();
        if (resCells.length != 0 && classCells.length == 1) {
            menu.addSeparator();
            menu.add(new AbstractAction(Translator.getString("Action.ChangeResourceType.Text")) {
                public void actionPerformed(ActionEvent ev) {
                    GraphCell typeCell = (GraphCell) gmanager.getClassGraph().getSelectionCell();
                    Object[] resCells = getSelectedRDFResourceCells();
                    for (Object resCell : resCells) {
                        RDFResourceModel info = (RDFResourceModel) GraphConstants.getValue(((GraphCell) resCell).getAttributes());
                        info.setTypeCell(typeCell, gmanager.getRDFGraph());
                    }
                    gmanager.repaintRDFGraph();
                }
            });
        }
    }

    private void addChangePropertyMenu(JPopupMenu menu) {
        Object[] rdfsPropCells = gmanager.getPropertyGraph().getSelectionCells();
        Object[] rdfPropCells = getSelectedRDFPropertyCells();
        if (rdfPropCells.length != 0 && rdfsPropCells.length == 1) {
            menu.addSeparator();
            menu.add(new AbstractAction(Translator.getString("Action.ChangeProperty.Text")) {
                public void actionPerformed(ActionEvent ev) {
                    GraphCell rdfsPropCell = (GraphCell) gmanager.getPropertyGraph().getSelectionCell();
                    Object[] rdfPropCells = getSelectedRDFPropertyCells();
                    for (Object rdfPropCell1 : rdfPropCells) {
                        GraphCell rdfPropCell = (GraphCell) rdfPropCell1;
                        RDFSModel rdfsModel = (RDFSModel) GraphConstants.getValue(rdfsPropCell.getAttributes());
                        GraphConstants.setValue(rdfPropCell.getAttributes(), rdfsModel);
                        graph.getGraphLayoutCache().editCell(rdfPropCell, rdfPropCell.getAttributes());
                    }
                }
            });
        }
    }

    class ShowAttrDialog extends AbstractAction {

        public ShowAttrDialog() {
            super(Translator.getString("Menu.Window.AttrDialog.Text"), Utilities
                    .getImageIcon(Translator.getString("AttributeDialog.Icon")));
            setValues();
        }

        private void setValues() {
            putValue(SHORT_DESCRIPTION, Translator.getString("Menu.Window.AttrDialog.Text"));
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_A,
                    Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx() | InputEvent.SHIFT_DOWN_MASK));
        }

        public void actionPerformed(ActionEvent e) {
            gmanager.setVisibleAttrDialog(true);
            graph.setSelectionCell(graph.getSelectionCell());
        }
    }
}