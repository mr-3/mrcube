package mr3.editor;
import java.awt.*;
import java.awt.Container;
import java.awt.event.*;
import java.beans.*;
import java.io.*;
import java.net.*;
import java.util.*;

import javax.swing.*;
import javax.swing.event.*;

import mr3.data.*;
import mr3.jgraph.*;
import mr3.ui.*;
import mr3.util.*;

import com.hp.hpl.mesa.rdf.jena.model.*;
import com.jgraph.event.*;
import com.jgraph.graph.*;

public abstract class Editor extends JInternalFrame implements GraphSelectionListener {

	protected RDFGraph graph;
	protected GraphManager gmanager;
	protected JScrollPane graphScrollPane;
	protected GraphUndoManager undoManager;

	protected Object[] lastSelectionCells;

	protected RDFCellMaker cellMaker;
	protected JGraphToRDF graphToRDF;
	protected RDFToJGraph rdfToGraph;
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

	Editor(String title) {
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
		rdfToGraph = new RDFToJGraph(manager);
		graphToRDF = new JGraphToRDF(manager);
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

	// Undo the last Change to the Model or the View
	public void undo() {
		try {
			undoManager.undo(graph.getGraphLayoutCache());
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			updateHistoryButtons();
		}
	}

	// Redo the last Change to the Model or the View
	public void redo() {
		try {
			undoManager.redo(graph.getGraphLayoutCache());
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			updateHistoryButtons();
		}
	}

	// Update Undo/Redo Button State based on Undo Manager
	protected void updateHistoryButtons() {
		// The View Argument Defines the Context
		undo.setEnabled(undoManager.canUndo(graph.getGraphLayoutCache()));
		redo.setEnabled(undoManager.canRedo(graph.getGraphLayoutCache()));
	}

	protected void replaceGraph(RDFGraph newGraph) { // Object��ǂݏ�������̂ƁA���l
		graph.setRDFState(newGraph.getRDFState());
	}

	/** �@�f�o�b�O�p���\�b�h */
	public void printModel(Model model) {
		try {
			model.write(new PrintWriter(System.out));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// Create a Group that Contains the Cells
	public void group(Object[] cells) {
		cells = graph.getGraphLayoutCache().order(cells);

		if (cells != null && cells.length > 0) {
			int count = getCellCount(graph);
			DefaultGraphCell group = new DefaultGraphCell(new Integer(count - 1));
			ParentMap map = new ParentMap();
			for (int i = 0; i < cells.length; i++) {
				map.addEntry(cells[i], group);
			}
			graph.getModel().insert(new Object[] { group }, null, null, map, null);
		}
	}

	// Returns the total number of cells in a graph
	protected int getCellCount(RDFGraph graph) {
		Object[] cells = graph.getAllCells();
		return cells.length;
	}

	// Ungroup the Groups in Cells and Select the Children
	public void ungroup(Object[] cells) {
		if (cells != null && cells.length > 0) {
			ArrayList groups = new ArrayList();
			ArrayList children = new ArrayList();
			for (int i = 0; i < cells.length; i++) {
				if (isGroup(cells[i])) {
					groups.add(cells[i]);
					for (int j = 0; j < graph.getModel().getChildCount(cells[i]); j++) {
						Object child = graph.getModel().getChild(cells[i], j);
						if (!graph.isPort(child)) {
							children.add(child);
						}
					}
				}
			}
			graph.getModel().remove(groups.toArray());
			graph.setSelectionCells(children.toArray());
		}
	}

	// Determines if a Cell is a Group
	public boolean isGroup(Object cell) {
		// Map the Cell to its View
		CellView view = graph.getGraphLayoutCache().getMapping(cell, false);
		if (view != null)
			return !view.isLeaf();
		return false;
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

	public void fitWindow() {
		Rectangle p = graph.getCellBounds(graph.getRoots());
		if (p != null) {
			Dimension s = graphScrollPane.getViewport().getExtentSize();
			double scale = 1;
			if (s.width == 0 && s.height == 0) {
				graph.setScale(scale);
				return;
			}
			if (Math.abs(s.getWidth() - (p.x + p.getWidth())) > Math.abs(s.getHeight() - (p.x + p.getHeight())))
				scale = (double) s.getWidth() / (p.x + p.getWidth());
			else
				scale = (double) s.getHeight() / (p.y + p.getHeight());
			scale = Math.max(Math.min(scale, 16), .01);
			graph.setScale(scale);
		}
	}

	private URL getImageIcon(String image) {
		return this.getClass().getClassLoader().getResource("mr3/resources/" + image);
	}

	//
	// ToolBar
	//
	public JToolBar createToolBar() {
		JToolBar toolbar = new JToolBar();
		toolbar.setFloatable(false);

		if (graph.getMarqueeHandler() instanceof RDFGraphMarqueeHandler) {
			RDFGraphMarqueeHandler mh = (RDFGraphMarqueeHandler) graph.getMarqueeHandler();

			ButtonGroup group = new ButtonGroup();
			mh.moveButton.setSelected(true);
			group.add(mh.moveButton);
			group.add(mh.connectButton);

			// move
			ImageIcon moveIcon = new ImageIcon(getImageIcon("move.gif"));
			mh.moveButton.setIcon(moveIcon);
			toolbar.add(mh.moveButton);

			// Toggle Connect Mode
			ImageIcon connectIcon = new ImageIcon(getImageIcon("arrow.gif"));
			mh.connectButton.setIcon(connectIcon);
			toolbar.add(mh.connectButton);

			// Toggle Self Connect Mode
			if (gmanager.isRDFGraph(graph)) {
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

		ImageIcon insertIcon = new ImageIcon(getImageIcon("ellipse.gif"));
		if (gmanager.isRDFGraph(graph) || gmanager.isPropertyGraph(graph)) {
			toolbar.add(new AbstractAction("", insertIcon) {
				public void actionPerformed(ActionEvent e) {
					RDFGraphMarqueeHandler mh = (RDFGraphMarqueeHandler) graph.getMarqueeHandler();
					mh.insertResourceCell(new Point(10, 10));
				}
			});
		}

		insertIcon = new ImageIcon(getImageIcon("rectangle.gif"));
		if (!gmanager.isPropertyGraph(graph)) {
			toolbar.add(new AbstractAction("", insertIcon) {
				public void actionPerformed(ActionEvent e) {
					RDFGraphMarqueeHandler mh = (RDFGraphMarqueeHandler) graph.getMarqueeHandler();
					if (gmanager.isRDFGraph(graph)) {
						mh.insertLiteralCell(new Point(10, 10));
					} else if (gmanager.isClassGraph(graph)) {
						mh.insertResourceCell(new Point(10, 10));
					}
				}
			});
		}

		// toolbar.addSeparator();
		ImageIcon undoIcon = new ImageIcon(getImageIcon("undo.gif"));
		undo = new AbstractAction("", undoIcon) {
			public void actionPerformed(ActionEvent e) {
				undo();
			}
		};
		undo.setEnabled(false);
		//				toolbar.add(undo);

		// Redo
		ImageIcon redoIcon = new ImageIcon(getImageIcon("redo.gif"));
		redo = new AbstractAction("", redoIcon) {
			public void actionPerformed(ActionEvent e) {
				redo();
			}
		};
		redo.setEnabled(false);
		//		toolbar.add(redo);

		toolbar.addSeparator();
		Action action;
		URL url;

		// Copy
		ImageIcon copyIcon = new ImageIcon(getImageIcon("copy.gif"));
		toolbar.add(new AbstractAction("", copyIcon) {
			public void actionPerformed(ActionEvent e) {
				graph.copy(new Point(10, 10));
			}
		});

		// Cut
		ImageIcon cutIcon = new ImageIcon(getImageIcon("cut.gif"));
		toolbar.add(new AbstractAction("", cutIcon) {
			public void actionPerformed(ActionEvent e) {
				graph.cut(new Point(10, 10));
			}
		});

		// Paste
		ImageIcon pasteIcon = new ImageIcon(getImageIcon("paste.gif"));
		toolbar.add(new AbstractAction("", pasteIcon) {
			public void actionPerformed(ActionEvent e) {
				graph.paste(new Point(30, 30));
			}
		});

		// Remove
		ImageIcon removeIcon = new ImageIcon(getImageIcon("delete.gif"));
		remove = new AbstractAction("", removeIcon) {
			public void actionPerformed(ActionEvent e) {
				gmanager.removeAction(graph);
			}
		};
		remove.setEnabled(false);
		toolbar.add(remove);

		// Find Resource
		toolbar.addSeparator();
		ImageIcon findIcon = new ImageIcon(getImageIcon("find.gif"));
		toolbar.add(new AbstractAction("", findIcon) {
			public void actionPerformed(ActionEvent e) {
				findResDialog.setSearchArea(graph.getType());
				findResDialog.setVisible(true);
			}
		});

		// Zoom Std
		toolbar.addSeparator();
		ImageIcon zoomIcon = new ImageIcon(getImageIcon("zoom100.gif"));
		toolbar.add(new AbstractAction("", zoomIcon) {
			public void actionPerformed(ActionEvent e) {
				graph.setScale(1.0);
			}
		});

		// Zoom In
		ImageIcon zoomInIcon = new ImageIcon(getImageIcon("zoomin.gif"));
		toolbar.add(new AbstractAction("", zoomInIcon) {
			public void actionPerformed(ActionEvent e) {
				graph.setScale(1.5 * graph.getScale());
			}
		});

		// Zoom Out
		ImageIcon zoomOutIcon = new ImageIcon(getImageIcon("zoomout.gif"));
		toolbar.add(new AbstractAction("", zoomOutIcon) {
			public void actionPerformed(ActionEvent e) {
				graph.setScale(graph.getScale() / 1.5);
			}
		});

		ImageIcon fitWindowIcon = new ImageIcon(getImageIcon("zoom.gif"));
		toolbar.add(new AbstractAction("", fitWindowIcon) {
			public void actionPerformed(ActionEvent e) {
				fitWindow();
			}
		});

		return toolbar;
	}

	private void toFrontInternFrame(int i) {
		try {
			internalFrames[i].toFront();
			internalFrames[i].setIcon(false);
			internalFrames[i].setSelected(true);
		} catch (PropertyVetoException pve) {
			pve.printStackTrace();
		}
	}

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