/*
 * Project Name: MR^3 (Meta-Model Management based on RDFs Revision Reflection)
 * Project Website: https://mr-3.github.io/
 *
 * Copyright (C) 2003-2024 Takeshi Morita. All rights reserved.
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

package jp.ac.aoyama.it.ke.mrcube.editors;

import org.jgraph.JGraph;
import org.jgraph.event.GraphSelectionListener;
import org.jgraph.graph.GraphCell;
import org.jgraph.graph.GraphConstants;
import org.jgraph.graph.GraphUndoManager;
import jp.ac.aoyama.it.ke.mrcube.MR3;
import jp.ac.aoyama.it.ke.mrcube.actions.GraphLayoutAction;
import jp.ac.aoyama.it.ke.mrcube.actions.OpenResourceAction;
import jp.ac.aoyama.it.ke.mrcube.actions.RemoveAction;
import jp.ac.aoyama.it.ke.mrcube.actions.SaveGraphImageAction;
import jp.ac.aoyama.it.ke.mrcube.jgraph.GraphManager;
import jp.ac.aoyama.it.ke.mrcube.jgraph.RDFGraph;
import jp.ac.aoyama.it.ke.mrcube.jgraph.RDFGraphMarqueeHandler;
import jp.ac.aoyama.it.ke.mrcube.layout.GraphLayoutUtilities;
import jp.ac.aoyama.it.ke.mrcube.models.MR3Constants.GraphType;
import jp.ac.aoyama.it.ke.mrcube.models.MR3Constants.HistoryType;
import jp.ac.aoyama.it.ke.mrcube.models.InstanceModel;
import jp.ac.aoyama.it.ke.mrcube.models.RDFSModel;
import jp.ac.aoyama.it.ke.mrcube.models.RDFSModelMap;
import jp.ac.aoyama.it.ke.mrcube.utils.Translator;
import jp.ac.aoyama.it.ke.mrcube.utils.Utilities;
import jp.ac.aoyama.it.ke.mrcube.views.HistoryManager;

import javax.swing.*;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import java.awt.*;
import java.awt.event.*;
import java.util.Arrays;
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

    private Action undo;
    private Action redo;
    private Action remove;

    Font graphFont;

    public static final ImageIcon INSTANCE_NODE_ICON = Utilities.getImageIcon("instance_node.png");
    public static final ImageIcon CLASS_NODE_ICON = Utilities.getImageIcon("class_node.png");
    public static final ImageIcon PROPERTY_NODE_ICON = Utilities.getImageIcon("property_node.png");
    public static final ImageIcon LITERAL_NODE_ICON = Utilities.getImageIcon("literal_node.png");

    public static final Color DEFAUlT_BACKGROUND_COLOR = Color.white;
    public static Color backgroundColor = DEFAUlT_BACKGROUND_COLOR;

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
        graphFont = new Font("SansSerif", Font.PLAIN, 14);
        graph = g;
        lastSelectionCells = new Object[0];
        initField(gm);
        initListener();
        initLayout();
    }

    void initField(GraphManager gm) {
        gmanager = gm;
        undoManager = new GraphUndoManager();
    }

    private void initListener() {
        graph.getModel().addUndoableEditListener(undoManager);
        graph.getSelectionModel().addGraphSelectionListener(this);
        graph.addKeyListener(new EditorKeyEvent());
    }

    private void initLayout() {
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

    class InsertEllipseResourceAction extends AbstractAction {

        InsertEllipseResourceAction(ImageIcon icon) {
            super("", icon);
        }

        public void actionPerformed(ActionEvent e) {
            RDFGraphMarqueeHandler mh = (RDFGraphMarqueeHandler) graph.getMarqueeHandler();
            GraphCell insertCell = mh.insertResourceCell(INSERT_POINT);
            if (insertCell != null) {
                if (graph.getType() == GraphType.Instance) {
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
            if (gmanager.isInstanceGraph(graph)) {
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

    class OpenSelectedResourceAction extends AbstractAction {
        public OpenSelectedResourceAction() {
            super(OpenResourceAction.TITLE, Utilities.getImageIcon("baseline_open_in_browser_black_18dp.png"));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (graph.getSelectionCount() == 1) {
                Object selectedCell = graph.getSelectionCell();
                if (graph.getType() == GraphType.Instance) {
                    for (Object cell : graph.getAllSelectedCells()) {
                        if (RDFGraph.isRDFResourceCell(cell)) {
                            InstanceModel model = (InstanceModel) GraphConstants.getValue(((GraphCell) cell).getAttributes());
                            MR3.ResourcePathTextField.setText(model.getURIStr());
                        }
                    }
                } else if (RDFGraph.isRDFsCell(selectedCell)) {
                    RDFSModel model = (RDFSModel) GraphConstants.getValue(((GraphCell) selectedCell).getAttributes());
                    MR3.ResourcePathTextField.setText(model.getURIStr());
                }
            }
        }
    }

    private JToolBar createToolBar() {
        var toolbar = new JToolBar();

        if (graph.getType() == GraphType.Instance) {
            toolbar.add(new InsertEllipseResourceAction(INSTANCE_NODE_ICON));
            toolbar.add(new InsertRectangleResourceAction(LITERAL_NODE_ICON));
        } else if (graph.getType() == GraphType.Class) {
            toolbar.add(new InsertRectangleResourceAction(CLASS_NODE_ICON));
        } else if (graph.getType() == GraphType.Property) {
            toolbar.add(new InsertEllipseResourceAction(PROPERTY_NODE_ICON));
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
        var zoomComboBoxModel = new DefaultComboBoxModel<String>();
        var zoomComboBox = new JComboBox<>(zoomComboBoxModel);
        zoomComboBox.addActionListener(e -> {
            String selectedItem = zoomComboBox.getItemAt(zoomComboBox.getSelectedIndex());
            double scale = Double.parseDouble(selectedItem.replace("%", "")) / 100;
            graph.setScale(scale);
        });
        zoomComboBoxModel.addAll(Arrays.asList("300%", "200%", "150%", "100%", "75%", "50%"));
        zoomComboBox.setSelectedIndex(3);
        toolbar.add(zoomComboBox);
        toolbar.addSeparator();

        toolbar.add(new SaveGraphImageAction(gmanager, graph.getType()));

        toolbar.addSeparator();
        if (graph.getType() == GraphType.Instance) {
            toolbar.add(new GraphLayoutAction(gmanager, graph.getType(), GraphLayoutUtilities.LEFT_TO_RIGHT));
        } else if (graph.getType() == GraphType.Class) {
            toolbar.add(new GraphLayoutAction(gmanager, graph.getType(), GraphLayoutUtilities.LEFT_TO_RIGHT));
            toolbar.add(new GraphLayoutAction(gmanager, graph.getType(), GraphLayoutUtilities.UP_TO_DOWN));
        } else if (graph.getType() == GraphType.Property) {
            toolbar.add(new GraphLayoutAction(gmanager, graph.getType(), GraphLayoutUtilities.LEFT_TO_RIGHT));
            toolbar.add(new GraphLayoutAction(gmanager, graph.getType(), GraphLayoutUtilities.UP_TO_DOWN));
        }
        toolbar.addSeparator();

        toolbar.add(new OpenSelectedResourceAction());
        toolbar.setFloatable(false);

        return toolbar;
    }

    // Update Undo/Redo Button State based on Undo Manager
    private void updateHistoryButtons() {
        // The View Argument Defines the Context
        undo.setEnabled(undoManager.canUndo(graph.getGraphLayoutCache()));
        redo.setEnabled(undoManager.canRedo(graph.getGraphLayoutCache()));
    }

    private void uriConsistencyCheck(Object[] orgAllCells) {
        if (graph.getType() == GraphType.Instance) {
            return;
        }
        RDFSModelMap rdfsModelMap = gmanager.getRDFSInfoMap();
        Object[] newAllCells = graph.getAllCells();
        Set<GraphCell> newRDFSCellSet = new HashSet<>();
        for (Object newAllCell : newAllCells) { // undo/redo前よりもセル数が増えた場合
            GraphCell cell = (GraphCell) newAllCell;
            if (RDFGraph.isRDFSCell(cell)) {
                newRDFSCellSet.add(cell);
                RDFSModel info = (RDFSModel) GraphConstants.getValue(cell.getAttributes());
                if (graph.getType() == GraphType.Class && !rdfsModelMap.isClassCell(info.getURI())) {
                    rdfsModelMap.putURICellMap(info, cell);
                } else if (graph.getType() == GraphType.Property
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
