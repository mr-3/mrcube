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

import org.jgraph.JGraph;
import org.jgraph.event.GraphSelectionListener;
import org.jgraph.graph.GraphCell;
import org.jgraph.graph.GraphConstants;
import org.jgraph.graph.GraphUndoManager;
import org.mrcube.actions.*;
import org.mrcube.io.MR3Generator;
import org.mrcube.io.MR3Parser;
import org.mrcube.jgraph.GraphManager;
import org.mrcube.jgraph.RDFGraph;
import org.mrcube.jgraph.RDFGraphMarqueeHandler;
import org.mrcube.models.MR3Constants.GraphType;
import org.mrcube.models.MR3Constants.HistoryType;
import org.mrcube.models.RDFSModel;
import org.mrcube.models.RDFSModelMap;
import org.mrcube.utils.MR3CellMaker;
import org.mrcube.utils.Translator;
import org.mrcube.utils.Utilities;
import org.mrcube.views.HistoryManager;

import javax.swing.*;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import java.awt.*;
import java.awt.event.*;
import java.util.HashSet;
import java.util.Set;

/*
 *
 * @author Takeshi Morita
 *
 */
public abstract class Editor extends JPanel implements GraphSelectionListener, MouseWheelListener {

    RDFGraph graph;
    GraphManager gmanager;
    private JScrollPane graphScrollPane;
    private GraphUndoManager undoManager;

    Object[] lastSelectionCells;

    private MR3CellMaker cellMaker;
    private MR3Generator mr3Generator;
    private MR3Parser mr3Parser;

    private Action undo;
    private Action redo;
    private Action remove;

    Font graphFont;

    Editor() {
    }

    public void mouseWheelMoved(MouseWheelEvent e) {
        int up = -1;
        int down = 1;
        if (e.isMetaDown()) {
            if (e.getWheelRotation() == up) {
                graph.setScale(1.1 * graph.getScale());
            } else if (e.getWheelRotation() == down) {
                graph.setScale(graph.getScale() / 1.1);
            }
        }
    }

    void initEditor(RDFGraph g, GraphManager gm) {
        graphFont = new Font("SansSerif", Font.PLAIN, 11); // デフォルト．
        graph = g;
        lastSelectionCells = new Object[0];
        initField(gm);
        initListener();
        initLayout();
    }

    void initField(GraphManager gm) {
        gmanager = gm;
        undoManager = new GraphUndoManager();
        cellMaker = new MR3CellMaker(gmanager);
        mr3Parser = new MR3Parser(gmanager);
        mr3Generator = new MR3Generator(gmanager);
    }

    private void initListener() {
        graph.getModel().addUndoableEditListener(undoManager);
        graph.getSelectionModel().addGraphSelectionListener(this);
        graph.addKeyListener(new EditorKeyEvent());
    }

    private void initLayout() {
        // Container container = getContentPane();
        // container.setLayout(new BorderLayout());
        // container.add(createToolBar(), BorderLayout.NORTH);
        // graphScrollPane = new JScrollPane(graph);
        // container.add(graphScrollPane, BorderLayout.CENTER);
        setLayout(new BorderLayout());
        add(createToolBar(), BorderLayout.NORTH);
        graphScrollPane = new JScrollPane(graph);
        graphScrollPane.addMouseWheelListener(this);
        add(graphScrollPane, BorderLayout.CENTER);
    }

    class EditorKeyEvent extends KeyAdapter {
        public void keyPressed(KeyEvent e) {
            if (e.getKeyCode() == KeyEvent.VK_DELETE) {
                remove.actionPerformed(null);
            }
        }
    }

    // Brings the Specified Cells to Front
    public void toFront(Object[] c) {
        if (c != null && c.length > 0)
            graph.getGraphLayoutCache().toFront(graph.getGraphLayoutCache().getMapping(c));
    }

    // Sends the Specified Cells to Back
    public void toBack(Object[] c) {
        if (c != null && c.length > 0)
            graph.getGraphLayoutCache().toBack(graph.getGraphLayoutCache().getMapping(c));
    }

    void setToolStatus() {
        boolean enabled = !graph.isSelectionEmpty();
        remove.setEnabled(enabled);
    }

    public JViewport getJViewport() {
        return graphScrollPane.getViewport();
    }

    public JGraph getGraph() {
        return graph;
    }

    private static final Point INSERT_POINT = new Point(10, 10);
    private final JToggleButton editModeButton = new JToggleButton();

    public boolean isEditMode() {
        return editModeButton.isSelected();
    }

    class InsertEllipseResourceAction extends AbstractAction {

        InsertEllipseResourceAction(ImageIcon icon) {
            super("", icon);
        }

        public void actionPerformed(ActionEvent e) {
            RDFGraphMarqueeHandler mh = (RDFGraphMarqueeHandler) graph.getMarqueeHandler();
            GraphCell insertCell = mh.insertResourceCell(INSERT_POINT);
            if (insertCell != null) {
                if (graph.getType() == GraphType.RDF) {
                    HistoryManager.saveHistory(HistoryType.INSERT_RESOURCE, insertCell);
                } else {
                    HistoryManager.saveHistory(HistoryType.INSERT_ONT_PROPERTY, insertCell);
                }
            }
        }
    }

    class InsertRectangleResourceAction extends AbstractAction {

        InsertRectangleResourceAction(ImageIcon icon) {
            super("", icon);
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_L,
                    Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
        }

        public void actionPerformed(ActionEvent e) {
            RDFGraphMarqueeHandler mh = (RDFGraphMarqueeHandler) graph.getMarqueeHandler();
            if (gmanager.isRDFGraph(graph)) {
                GraphCell cell = mh.insertLiteralCell(INSERT_POINT);
                if (cell != null) {
                    HistoryManager.saveHistory(HistoryType.INSERT_LITERAL, cell);
                }
            } else if (gmanager.isClassGraph(graph)) {
                GraphCell cell = mh.insertResourceCell(INSERT_POINT);
                if (cell != null) {
                    HistoryManager.saveHistory(HistoryType.INSERT_CLASS, cell);
                }
            }
        }
    }

    /**
     * Create ToolBar
     */
    private JToolBar createToolBar() {
        JToolBar toolbar = new JToolBar();
        toolbar.setFloatable(false);

        if (graph.getMarqueeHandler() instanceof RDFGraphMarqueeHandler) {
            if (graph.getType() == GraphType.RDF) {
                toolbar.addSeparator();
                editModeButton.setIcon(Utilities.getImageIcon("link.png"));
                editModeButton.addActionListener(e -> {
                    if (editModeButton.isSelected()) {
                        editModeButton.setIcon(Utilities.getImageIcon("link_break.png"));
                    } else {
                        editModeButton.setIcon(Utilities.getImageIcon("link.png"));
                    }
                });
                toolbar.add(editModeButton);
            }
        }

        toolbar.addSeparator();

        if (graph.getType() == GraphType.RDF) {
            toolbar.add(new InsertEllipseResourceAction(Utilities.getImageIcon("rdf_resource_ellipse.png")));
        } else if (graph.getType() == GraphType.PROPERTY) {
            toolbar.add(new InsertEllipseResourceAction(Utilities.getImageIcon("property_ellipse.png")));
        }

        if (graph.getType() == GraphType.RDF) {
            toolbar.add(new InsertRectangleResourceAction(Utilities.getImageIcon("literal_rectangle.png")));
        } else if (graph.getType() == GraphType.CLASS) {
            toolbar.add(new InsertRectangleResourceAction(Utilities.getImageIcon("class_rectangle.png")));
        }


        toolbar.addSeparator();
        toolbar.add(graph.getCopyAction());
        toolbar.add(graph.getCutAction());
        toolbar.add(graph.getPasteAction());

        toolbar.addSeparator();
        remove = new RemoveAction(graph, gmanager);
        remove.setEnabled(false);
        toolbar.add(remove);

        toolbar.addSeparator();
        undo = new UndoAction();
        undo.setEnabled(true);
        toolbar.add(undo);
        redo = new RedoAction();
        redo.setEnabled(true);
        toolbar.add(redo);

        toolbar.addSeparator();
        toolbar.add(new FindResAction(graph, gmanager));

        toolbar.addSeparator();
        toolbar.add(new ZoomAction(graph, this, ZoomAction.ZOOM_STD, ZoomAction.ZOOM_STD_ICON));
        toolbar.add(new ZoomAction(graph, this, ZoomAction.ZOOM_IN, ZoomAction.ZOOM_IN_ICON));
        toolbar.add(new ZoomAction(graph, this, ZoomAction.ZOOM_OUT, ZoomAction.ZOOM_OUT_ICON));
        toolbar.add(new ZoomAction(graph, this, ZoomAction.ZOOM_SUITABLE,
                ZoomAction.ZOOM_SUITABLE_ICON));

        toolbar.addSeparator();
        GraphLayoutAction graphLayoutAction = null;
        if (graph.getType() == GraphType.RDF) {
            graphLayoutAction = new GraphLayoutAction(gmanager, graph.getType(),
                    GraphLayoutAction.layoutRDFGraphIcon);
        } else if (graph.getType() == GraphType.CLASS) {
            graphLayoutAction = new GraphLayoutAction(gmanager, graph.getType(),
                    GraphLayoutAction.layoutClassGraphIcon);
        } else if (graph.getType() == GraphType.PROPERTY) {
            graphLayoutAction = new GraphLayoutAction(gmanager, graph.getType(),
                    GraphLayoutAction.layoutPropertyGraphIcon);
        }
        toolbar.add(graphLayoutAction);

        return toolbar;
    }

    // Update Undo/Redo Button State based on Undo Manager
    private void updateHistoryButtons() {
        // The View Argument Defines the Context
        undo.setEnabled(undoManager.canUndo(graph.getGraphLayoutCache()));
        redo.setEnabled(undoManager.canRedo(graph.getGraphLayoutCache()));
    }

    private void uriConsistencyCheck(Object[] orgAllCells) {
        if (graph.getType() == GraphType.RDF) {
            return;
        }
        RDFSModelMap rdfsModelMap = gmanager.getCurrentRDFSInfoMap();
        Object[] newAllCells = graph.getAllCells();
        Set<GraphCell> newRDFSCellSet = new HashSet<>();
        for (Object newAllCell : newAllCells) { // undo/redo前よりもセル数が増えた場合
            GraphCell cell = (GraphCell) newAllCell;
            if (RDFGraph.isRDFSCell(cell)) {
                newRDFSCellSet.add(cell);
                RDFSModel info = (RDFSModel) GraphConstants.getValue(cell.getAttributes());
                if (graph.getType() == GraphType.CLASS && !rdfsModelMap.isClassCell(info.getURI())) {
                    rdfsModelMap.putURICellMap(info, cell);
                } else if (graph.getType() == GraphType.PROPERTY
                        && !rdfsModelMap.isPropertyCell(info.getURI())) {
                    rdfsModelMap.putURICellMap(info, cell);
                }
            }
        }
        for (Object orgAllCell : orgAllCells) { // undo/redo前よりもセル数が減った場合
            GraphCell cell = (GraphCell) orgAllCell;
            if (RDFGraph.isRDFSCell(cell) && !newRDFSCellSet.contains(cell)) {
                rdfsModelMap.removeCellInfo(cell);
            }
        }
    }

    class UndoAction extends AbstractAction {
        UndoAction() {
            super(Translator.getString("Action.Undo.Text"), Utilities.getImageIcon(Translator
                    .getString("Action.Undo.Icon")));
        }

        // Undo the last Change to the Model or the View
        public void actionPerformed(ActionEvent e) {
            try {
                Object[] orgAllCells = graph.getAllCells();
                int orgCellCnt = orgAllCells.length;
                while (true) {
                    undoManager.undo(graph.getGraphLayoutCache());
                    Object[] newAllCells = graph.getAllCells();
                    if (orgCellCnt != newAllCells.length) {
                        uriConsistencyCheck(orgAllCells);
                        break;
                    }
                }
            } catch (CannotUndoException ex) {
                // ex.printStackTrace();
            } finally {
                updateHistoryButtons();
            }
        }
    }

    class RedoAction extends AbstractAction {
        RedoAction() {
            super(Translator.getString("Action.Redo.Text"), Utilities.getImageIcon(Translator
                    .getString("Action.Redo.Icon")));
        }

        // Redo the last Change to the Model or the View
        public void actionPerformed(ActionEvent e) {
            try {
                Object[] orgAllCells = graph.getAllCells();
                int orgCellCnt = orgAllCells.length;
                while (true) {
                    undoManager.redo(graph.getGraphLayoutCache());
                    Object[] newAllCells = graph.getAllCells();
                    if (orgCellCnt != newAllCells.length) {
                        uriConsistencyCheck(orgAllCells);
                        break;
                    }
                }
            } catch (CannotRedoException ex) {
                // ex.printStackTrace();
            } finally {
                updateHistoryButtons();
            }
        }
    }

    public JScrollPane getJScrollPane() {
        return graphScrollPane;
    }

    // This will change the source of the actionevent to graph.
    class EventRedirector extends AbstractAction {

        final Action action;

        // Construct the "Wrapper" Action
        public EventRedirector(Action a) {
            super("", (ImageIcon) a.getValue(Action.SMALL_ICON));
            this.action = a;
        }

        // Redirect the Actionevent
        public void actionPerformed(ActionEvent e) {
            e = new ActionEvent(graph, e.getID(), e.getActionCommand(), e.getModifiers());
            action.actionPerformed(e);
        }
    }
}
