package mr3.editor;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.*;

import javax.swing.*;
import javax.swing.event.*;

import mr3.data.*;
import mr3.jgraph.*;
import mr3.ui.*;
import mr3.util.*;

import com.hp.hpl.mesa.rdf.jena.common.*;
import com.hp.hpl.mesa.rdf.jena.model.*;
import com.jgraph.event.*;
import com.jgraph.graph.*;

public abstract class Editor extends JPanel implements GraphSelectionListener {

	protected RDFGraph graph;
	protected GraphManager gmanager;
	protected JScrollPane graphScrollPane;
	protected GraphUndoManager undoManager;

	protected RDFCellMaker cellMaker;
	protected JGraphToRDF graphToRDF;
	protected RDFToJGraph rdfToGraph;
	protected AttributeDialog attrDialog;

	protected RDFResourceInfoMap resInfoMap = RDFResourceInfoMap.getInstance();
	protected RDFLiteralInfoMap litInfoMap = RDFLiteralInfoMap.getInstance();
	protected RDFSInfoMap rdfsInfoMap = RDFSInfoMap.getInstance();

	protected Action undo, redo, remove, group, ungroup, tofront, toback, cut, copy, paste;

	protected void initEditor(RDFGraph g, GraphManager manager, AttributeDialog attrD) {
		graph = g;
		initField(attrD, manager);
		initListener();
		initLayout();
	}

	protected void initField(AttributeDialog attrD, GraphManager manager) {
		gmanager = manager;
		undoManager = new RDFGraphUndoManager();
		cellMaker = new RDFCellMaker(gmanager);
		attrDialog = attrD;
		rdfToGraph = new RDFToJGraph(manager);
		graphToRDF = new JGraphToRDF(manager);
	}

	protected void initListener() {
		graph.getModel().addUndoableEditListener(undoManager);
		graph.getSelectionModel().addGraphSelectionListener(this);
		graph.addKeyListener(new EditorKeyEvent());
	}

	protected void initLayout() {
		setLayout(new BorderLayout());
		add(createToolBar(), BorderLayout.NORTH);
		graphScrollPane = new JScrollPane(graph);
		add(graphScrollPane, BorderLayout.CENTER);
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
			System.err.println(ex);
		} finally {
			updateHistoryButtons();
		}
	}

	// Redo the last Change to the Model or the View
	public void redo() {
		try {
			undoManager.redo(graph.getGraphLayoutCache());
		} catch (Exception ex) {
			System.err.println(ex);
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

	protected void replaceGraph(RDFGraph newGraph) { // Objectを読み書きするのと、同様
		graph.setRDFState(newGraph.getRDFState());
	}

	protected void setNsPrefix(RDFWriter writer) {
		Set prefixNsInfoSet = gmanager.getPrefixNSInfoSet();
		for (Iterator i = prefixNsInfoSet.iterator(); i.hasNext();) {
			PrefixNSInfo info = (PrefixNSInfo) i.next();
			if (info.isAvailable()) {
				writer.setNsPrefix(info.getPrefix(), info.getNameSpace());
			}
		}
	}

	public Writer writeModel(Model model, Writer output, RDFWriter writer) throws RDFException {
		setNsPrefix(writer);
		writer.write(model, output, null);
		return output;
	}

	/** 　デバッグ用メソッド */
	public void printModel(Model model) {
		try {
			model.write(new PrintWriter(System.out));
		} catch (Exception e) {
		}
	}

	public void exportRDFSFile(Model model) {
		try {
			JFileChooser fc = new JFileChooser();
			FileWriter output = null;
			int fd = fc.showSaveDialog(Editor.this);
			if (fd == JFileChooser.APPROVE_OPTION) {
				File file = fc.getSelectedFile();
				output = new FileWriter(file);
				RDFWriter writer = new RDFWriterFImpl().getWriter("RDF/XML-ABBREV");
				writeModel(model, output, writer);
			}
		} catch (RDFException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// Create a Group that Contains the Cells
	public void group(Object[] cells) {
		cells = graph.getGraphLayoutCache().order(cells);

		if (cells != null && cells.length > 0) {
			int count = getCellCount(graph);
			DefaultGraphCell group = new DefaultGraphCell(new Integer(count - 1));
//			ParentMap map = new ParentMap();
			ParentMap map = new ParentMap(graph.getModel());
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
		// Group Button only Enabled if more than One Cell Selected
		//group.setEnabled(graph.getSelectionCount() > 1);
		// Update Button States based on Current Selection
		boolean enabled = !graph.isSelectionEmpty();
		remove.setEnabled(enabled);
		//copy.setEnabled(enabled);
		//cut.setEnabled(enabled);
		//ungroup.setEnabled(enabled);
		//tofront.setEnabled(enabled);
		//toback.setEnabled(enabled);
	}

//	public void fitWindow(RDFGraph graph) {
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
		return this.getClass().getClassLoader().getResource("mr3/resources/"+image);
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
			URL moveUrl = getImageIcon("move.gif");
			ImageIcon moveIcon = new ImageIcon(moveUrl);
			mh.moveButton.setIcon(moveIcon);
			toolbar.add(mh.moveButton);

			// Toggle Connect Mode
			URL connectUrl = getImageIcon("arrow.gif");
			ImageIcon connectIcon = new ImageIcon(connectUrl);
			mh.connectButton.setIcon(connectIcon);
			toolbar.add(mh.connectButton);


			// Toggle Self Connect Mode
			URL selfConnectUrl = getImageIcon("arrow.gif");
			ImageIcon selfConnectIcon = new ImageIcon(connectUrl);
			if (gmanager.isRDFGraph(graph)) {
				toolbar.add(new AbstractAction("", connectIcon) {
					public void actionPerformed(ActionEvent e) {
						GraphCell cell = (GraphCell) graph.getSelectionCell();
						if (graph.isOneCellSelected(cell) && graph.isRDFResourceCell(cell)) {
							Port port = (Port) ((DefaultGraphCell) cell).getChildAt(0);
							RDFGraphMarqueeHandler mh = (RDFGraphMarqueeHandler) graph.getMarqueeHandler();
							mh.selfConnect(port, "");
						}
					}
				});
			}
		}

		toolbar.addSeparator();

		// insert Resource
		URL insertResUrl = getImageIcon("ellipse.gif");
		ImageIcon insertIcon = new ImageIcon(insertResUrl);
		toolbar.add(new AbstractAction("", insertIcon) {
			public void actionPerformed(ActionEvent e) {
				RDFGraphMarqueeHandler mh = (RDFGraphMarqueeHandler) graph.getMarqueeHandler();
				mh.insertResourceCell(new Point(10, 10));
			}
		});

		// insert Literal
		URL insertLitUrl = getImageIcon("rectangle.gif");
		insertIcon = new ImageIcon(insertLitUrl);
		if (gmanager.isRDFGraph(graph)) {
			toolbar.add(new AbstractAction("", insertIcon) {
				public void actionPerformed(ActionEvent e) {
					RDFGraphMarqueeHandler mh = (RDFGraphMarqueeHandler) graph.getMarqueeHandler();
					mh.insertLiteralCell(new Point(10, 10));
				}
			});
		}
//
//		toolbar.addSeparator();

		URL undoUrl = getImageIcon("undo.gif");
		ImageIcon undoIcon = new ImageIcon(undoUrl);
		undo = new AbstractAction("", undoIcon) {
			public void actionPerformed(ActionEvent e) {
				undo();
			}
		};
		undo.setEnabled(false);
//		toolbar.add(undo);
//
//		// Redo
		URL redoUrl = getImageIcon("redo.gif");
		ImageIcon redoIcon = new ImageIcon(redoUrl);
		redo = new AbstractAction("", redoIcon) {
			public void actionPerformed(ActionEvent e) {
				redo();
			}
		};
		redo.setEnabled(false);
//		toolbar.add(redo);

		//
		// Edit Block
		//
		toolbar.addSeparator();
		Action action;
		URL url;

		//		// Copy
		//		action = graph.getTransferHandler().getCopyAction();
		//		url = getClass().getClassLoader().getResource("img/copy.gif");
		//		action.putValue(Action.SMALL_ICON, new ImageIcon(url));
		//		toolbar.add(copy = new EventRedirector(action));
		//
		//		// Paste
		//		action = graph.getTransferHandler().getPasteAction();
		//		url = getClass().getClassLoader().getResource("img/paste.gif");
		//		action.putValue(Action.SMALL_ICON, new ImageIcon(url));
		//		toolbar.add(paste = new EventRedirector(action));
		//
		//		// Cut
		//		action = graph.getTransferHandler().getCutAction();
		//		url = getClass().getClassLoader().getResource("img/cut.gif");
		//		action.putValue(Action.SMALL_ICON, new ImageIcon(url));
		//		toolbar.add(cut = new EventRedirector(action));
		//
		// Remove
		URL removeUrl = getImageIcon("delete.gif");
		ImageIcon removeIcon = new ImageIcon(removeUrl);
		remove = new AbstractAction("", removeIcon) {
			public void actionPerformed(ActionEvent e) {
				if (!graph.isSelectionEmpty()) {
					Object[] cells = graph.getSelectionCells();
					cells = graph.getDescendants(cells);
					gmanager.removeCells(cells, graph);
				}
			}
		};
		remove.setEnabled(false);
		toolbar.add(remove);

		// Zoom Std
		toolbar.addSeparator();
		URL zoomUrl = getImageIcon("zoom.gif");
		ImageIcon zoomIcon = new ImageIcon(zoomUrl);
		toolbar.add(new AbstractAction("", zoomIcon) {
			public void actionPerformed(ActionEvent e) {
				graph.setScale(1.0);
			}
		});
		// Zoom In
		URL zoomInUrl = getImageIcon("zoomin.gif");
		ImageIcon zoomInIcon = new ImageIcon(zoomInUrl);
		toolbar.add(new AbstractAction("", zoomInIcon) {
			public void actionPerformed(ActionEvent e) {
				graph.setScale(1.5 * graph.getScale());
			}
		});
		// Zoom Out
		URL zoomOutUrl = getImageIcon("zoomout.gif");
		ImageIcon zoomOutIcon = new ImageIcon(zoomOutUrl);
		toolbar.add(new AbstractAction("", zoomOutIcon) {
			public void actionPerformed(ActionEvent e) {
				graph.setScale(graph.getScale() / 1.5);
			}
		});

		URL fitWindowUrl = getImageIcon("zoomout.gif");
		ImageIcon fitWindowIcon = new ImageIcon(fitWindowUrl);
		toolbar.add(new AbstractAction("", fitWindowIcon) {
			public void actionPerformed(ActionEvent e) {
//				fitWindow(graph);
				fitWindow();
			}
		});

		// Group
		//		toolbar.addSeparator();
		//		URL groupUrl = getClass().getClassLoader().getResource("img/group.gif");
		//		ImageIcon groupIcon = new ImageIcon(groupUrl);
		//		group = new AbstractAction("", groupIcon) {
		//			public void actionPerformed(ActionEvent e) {
		//				group(graph.getSelectionCells());
		//			}
		//		};
		//		group.setEnabled(false);
		//		toolbar.add(group);
		//
		//		// Ungroup
		//		URL ungroupUrl = getClass().getClassLoader().getResource("img/ungroup.gif");
		//		ImageIcon ungroupIcon = new ImageIcon(ungroupUrl);
		//		ungroup = new AbstractAction("", ungroupIcon) {
		//			public void actionPerformed(ActionEvent e) {
		//				ungroup(graph.getSelectionCells());
		//			}
		//		};
		//		ungroup.setEnabled(false);
		//		toolbar.add(ungroup);

		// To Front
		//		toolbar.addSeparator();
		//		URL toFrontUrl = getClass().getClassLoader().getResource("img/tofront.gif");
		//		ImageIcon toFrontIcon = new ImageIcon(toFrontUrl);
		//		tofront = new AbstractAction("", toFrontIcon) {
		//			public void actionPerformed(ActionEvent e) {
		//				if (!graph.isSelectionEmpty())
		//					toFront(graph.getSelectionCells());
		//			}
		//		};
		//		tofront.setEnabled(false);
		//		toolbar.add(tofront);
		//
		//		// To Back
		//		URL toBackUrl = getClass().getClassLoader().getResource("img/toback.gif");
		//		ImageIcon toBackIcon = new ImageIcon(toBackUrl);
		//		toback = new AbstractAction("", toBackIcon) {
		//			public void actionPerformed(ActionEvent e) {
		//				if (!graph.isSelectionEmpty())
		//					toBack(graph.getSelectionCells());
		//			}
		//		};
		//		toback.setEnabled(false);
		//		toolbar.add(toback);

		return toolbar;
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
