/*
 * @(#) Editor.java
 *
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

package org.semanticweb.mmm.mr3.editor;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.event.*;

import org.jgraph.*;
import org.jgraph.event.*;
import org.jgraph.graph.*;
import org.semanticweb.mmm.mr3.actions.*;
import org.semanticweb.mmm.mr3.data.*;
import org.semanticweb.mmm.mr3.io.*;
import org.semanticweb.mmm.mr3.jgraph.*;
import org.semanticweb.mmm.mr3.ui.*;
import org.semanticweb.mmm.mr3.util.*;

/*
 * 
 * @author takeshi morita
 *
 */
public abstract class Editor extends JInternalFrame implements GraphSelectionListener {

	protected RDFGraph graph;
	protected GraphManager gmanager;
	protected JScrollPane graphScrollPane;
	protected GraphUndoManager undoManager;

	protected Object[] lastSelectionCells;

	protected RDFCellMaker cellMaker;
	protected MR3Generator mr3Generator;
	protected MR3Parser mr3Parser;
	protected NameSpaceTableDialog nsTableDialog;
	protected AttributeDialog attrDialog;
	protected FindResourceDialog findResDialog;

	protected RDFResourceInfoMap resInfoMap = RDFResourceInfoMap.getInstance();
	protected RDFLiteralInfoMap litInfoMap = RDFLiteralInfoMap.getInstance();
	protected RDFSInfoMap rdfsInfoMap = RDFSInfoMap.getInstance();

	protected Action undo, redo, remove;

	private JInternalFrame[] internalFrames = new JInternalFrame[3];

	public void setInternalFrames(JInternalFrame[] ifs) {
		internalFrames = ifs;
	}

	protected Editor(String title) {
		super(title, true, false, true);
		setIconifiable(true);
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		try {
			setSelected(true);
		} catch (java.beans.PropertyVetoException e) {
			e.printStackTrace();
		}
		setVisible(true);
	}

	protected void initEditor(RDFGraph g, GraphManager gm, NameSpaceTableDialog nsD, FindResourceDialog findResD) {
		graph = g;
		findResDialog = findResD;
		lastSelectionCells = new Object[0];
		initField(nsD, gm);
		initListener();
		initLayout();
	}

	protected void initField(NameSpaceTableDialog nsD, GraphManager manager) {
		gmanager = manager;
		undoManager = new RDFGraphUndoManager();
		cellMaker = new RDFCellMaker(gmanager);
		attrDialog = gmanager.getAttrDialog();
		nsTableDialog = nsD;
		mr3Parser = new MR3Parser(manager);
		mr3Generator = new MR3Generator(manager);
	}

	protected void initListener() {
		graph.getModel().addUndoableEditListener(undoManager);
		graph.getSelectionModel().addGraphSelectionListener(this);
		graph.addKeyListener(new EditorKeyEvent());
	}

	protected void initLayout() {
		Container container = getContentPane();
		container.setLayout(new BorderLayout());
		container.add(createToolBar(), BorderLayout.NORTH);
		graphScrollPane = new JScrollPane(graph);
		container.add(graphScrollPane, BorderLayout.CENTER);
	}

	class RDFGraphUndoManager extends GraphUndoManager {
		public void undoableEditHappened(UndoableEditEvent e) {
			super.undoableEditHappened(e);
			updateHistoryButtons();
		}
	}

	class EditorKeyEvent extends KeyAdapter {
		public void keyPressed(KeyEvent e) {
			if (e.getKeyCode() == KeyEvent.VK_DELETE) {
				remove.actionPerformed(null);
			}
		}
	}

	// Update Undo/Redo Button State based on Undo Manager
	protected void updateHistoryButtons() {
		// The View Argument Defines the Context
		undo.setEnabled(undoManager.canUndo(graph.getGraphLayoutCache()));
		redo.setEnabled(undoManager.canRedo(graph.getGraphLayoutCache()));
	}

	protected void replaceGraph(RDFGraph newGraph) { // Object‚ð“Ç‚Ý‘‚«‚·‚é‚Ì‚ÆA“¯—l
		graph.setRDFState(newGraph.getRDFState());
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

	protected void setToolStatus() {
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
	
	/** _Create ToolBar */
	public JToolBar createToolBar() {
		JToolBar toolbar = new JToolBar();
		toolbar.setFloatable(false);

		if (graph.getMarqueeHandler() instanceof RDFGraphMarqueeHandler) {
			RDFGraphMarqueeHandler mh = (RDFGraphMarqueeHandler) graph.getMarqueeHandler();

			ButtonGroup group = new ButtonGroup();
			mh.moveButton.setSelected(true);
			group.add(mh.moveButton);
			group.add(mh.connectButton);

			mh.moveButton.setIcon(Utilities.getImageIcon("move.gif")); // move
			toolbar.add(mh.moveButton);

			ImageIcon connectIcon = Utilities.getImageIcon(Translator.getString("Action.Connect.Icon")); // Toggle Connect Mode
			mh.connectButton.setIcon(connectIcon);
			toolbar.add(mh.connectButton);

			if (gmanager.isRDFGraph(graph)) { // Toggle Self Connect Mode
				toolbar.add(new AbstractAction("", connectIcon) {
					public void actionPerformed(ActionEvent e) {
						GraphCell cell = (GraphCell) graph.getSelectionCell();
						if (graph.isOneCellSelected(cell) && graph.isRDFResourceCell(cell)) {
							Port port = (Port) ((DefaultGraphCell) cell).getChildAt(0);
							RDFGraphMarqueeHandler mh = (RDFGraphMarqueeHandler) graph.getMarqueeHandler();
							cellMaker.selfConnect(port, "", graph);
						}
					}
				});
			}
		}

		toolbar.addSeparator();

		ImageIcon insertIcon = Utilities.getImageIcon("ellipse.gif");
		if (gmanager.isRDFGraph(graph) || gmanager.isPropertyGraph(graph)) {
			toolbar.add(new AbstractAction("", insertIcon) {
				public void actionPerformed(ActionEvent e) {
					RDFGraphMarqueeHandler mh = (RDFGraphMarqueeHandler) graph.getMarqueeHandler();
					mh.insertResourceCell(INSERT_POINT);
				}
			});
		}

		insertIcon = Utilities.getImageIcon("rectangle.gif");
		if (!gmanager.isPropertyGraph(graph)) {
			toolbar.add(new AbstractAction("", insertIcon) {
				public void actionPerformed(ActionEvent e) {
					RDFGraphMarqueeHandler mh = (RDFGraphMarqueeHandler) graph.getMarqueeHandler();
					if (gmanager.isRDFGraph(graph)) {
						mh.insertLiteralCell(INSERT_POINT);
					} else if (gmanager.isClassGraph(graph)) {
						mh.insertResourceCell(INSERT_POINT);
					}
				}
			});
		}

		// toolbar.addSeparator();
		undo = new UndoAction();
		undo.setEnabled(false);
		// toolbar.add(undo);
		redo = new RedoAction();
		redo.setEnabled(false);
		// toolbar.add(redo);

		toolbar.addSeparator();
		toolbar.add(new CopyAction(graph));
		toolbar.add(new CutAction(graph));
		toolbar.add(new PasteAction(graph));

		toolbar.addSeparator();
		remove = new RemoveAction(graph, gmanager);
		remove.setEnabled(false);
		toolbar.add(remove);

		toolbar.addSeparator();
		toolbar.add(new FindResAction(graph, findResDialog));

		toolbar.addSeparator();
		toolbar.add(new ZoomAction(graph, this, ZoomAction.ZOOM_STD, ZoomAction.ZOOM_STD_ICON));
		toolbar.add(new ZoomAction(graph, this, ZoomAction.ZOOM_IN, ZoomAction.ZOOM_IN_ICON));
		toolbar.add(new ZoomAction(graph, this, ZoomAction.ZOOM_OUT, ZoomAction.ZOOM_OUT_ICON));
		toolbar.add(new ZoomAction(graph, this, ZoomAction.ZOOM_SUITABLE, ZoomAction.ZOOM_SUITABLE_ICON));
		
		toolbar.addSeparator();
		toolbar.add(new GroupAction(graph));
		toolbar.add(new UnGroupAction(graph));

		return toolbar;
	}

	class UndoAction extends AbstractAction {
		UndoAction() {
			super("undo", Utilities.getImageIcon("undo.gif"));
		}

		// Undo the last Change to the Model or the View
		public void actionPerformed(ActionEvent e) {
			try {
				undoManager.undo(graph.getGraphLayoutCache());
			} catch (Exception ex) {
				ex.printStackTrace();
			} finally {
				updateHistoryButtons();
			}
		}
	}

	class RedoAction extends AbstractAction {
		RedoAction() {
			super("redo", Utilities.getImageIcon("redo.gif"));
		}

		// Redo the last Change to the Model or the View
		public void actionPerformed(ActionEvent e) {
			try {
				undoManager.redo(graph.getGraphLayoutCache());
			} catch (Exception ex) {
				ex.printStackTrace();
			} finally {
				updateHistoryButtons();
			}
		}
	}

	public JScrollPane getJScrollPane() {
		return graphScrollPane;
	}

//	private void toFrontInternFrame(int i) {
//		try {
//			internalFrames[i].toFront();
//			internalFrames[i].setIcon(false);
//			internalFrames[i].setSelected(true);
//		} catch (PropertyVetoException pve) {
//			pve.printStackTrace();
//		}
//	}

	// This will change the source of the actionevent to graph.
	protected class EventRedirector extends AbstractAction {

		protected Action action;

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
