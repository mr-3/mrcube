package mr3.ui;
import java.awt.*;
import java.util.*;

import javax.swing.*;

import mr3.data.*;

import com.hp.hpl.mesa.rdf.jena.model.*;
import com.jgraph.event.*;
import com.jgraph.graph.*;

/**
 *
 * @auther takeshi morita 
 */
public class SelectTypeDialog extends SelectClassDialog {

	private JLabel dspURI;
	private Resource uri;
	private GraphCell cell;
	private GraphCell prevCell;

	public SelectTypeDialog() {
		super("Select Resource Type");
	}

	protected void initEachDialogAttr() {
		dspURI = new JLabel();
		dspURI.setBorder(BorderFactory.createTitledBorder("URI"));
		dspURI.setPreferredSize(new Dimension(450, 50));
	}

	protected void setEachDialogAttrLayout() {
		gridbag.setConstraints(dspURI, c);
		inlinePanel.add(dspURI);
	}

	public void setInitCell(Object typeCell) {
		if (typeCell == null) {
			if (prevCell != null) {
				setCellColor(prevCell, Color.green);
			}
			prevCell = null;
			dspURI.setText("");	
			return;
		} else {
			Object[] cells = graph.getAllCells();
			for (int i = 0; i < cells.length; i++) {
				GraphCell cell = (GraphCell) cells[i];
				if (graph.isRDFResourceCell(cell)) {
					if (cell == typeCell) {
						setCellColor(cell, Color.yellow);
						prevCell = cell;
						graph.setSelectionCell(cell);
						break;
					}
				}
			}
		}
	}

	private void setCellColor(GraphCell cell, Color color) {
		if (cell != null) {
			Map map = cell.getAttributes();
			GraphConstants.setBackground(map, color);
			setSelectionAttributes(map, cell);
		}
	}

	private void setSelectionAttributes(Map map, GraphCell cell) {
		map = GraphConstants.cloneMap(map);
		map.remove(GraphConstants.BOUNDS);
		map.remove(GraphConstants.POINTS);

		Map nested = new HashMap();
		nested.put(cell, GraphConstants.cloneMap(map));
		graph.getGraphLayoutCache().edit(nested, null, null, null);
	}

	public void valueChanged(GraphSelectionEvent e) {
		cell = (GraphCell) graph.getSelectionCell();
		if (graph.getSelectionCount() == 1 && graph.getModel().getChildCount(cell) <= 1) {
			if (graph.isRDFResourceCell(cell)) {	
				setCellColor(prevCell, Color.green);
				setCellColor(cell, Color.yellow);
				RDFSInfo info = rdfsMap.getCellInfo(cell);
				dspURI.setText(info.getURIStr());
				dspURI.setToolTipText(info.getURIStr());
				uri = info.getURI();
				prevCell = cell;
			}
		}
	}

	public Object getValue() {
		if (prevCell != null) {
			if (isOk) {
				isOk = false;
				setCellColor(prevCell, Color.green);
				return uri;
			} else {
				setCellColor(prevCell, Color.green);
				return null;
			}
		} else {
			return null;
		}
	}
}
