/*
 * Created on 2003/05/25
 *
 */
package mr3.util;

import java.awt.*;
import java.util.*;

import mr3.jgraph.*;

import com.jgraph.*;
import com.jgraph.graph.*;

/**
 * @author takeshi morita
 */
public class ChangeCellAttributes {

	/**
	 *  
	 * セルの色を変更するためのメソッド
	 * @param graph
	 * @param cell
	 * @param color
	 */
	public static void changeCellColor(JGraph graph, GraphCell cell, Color color) {
		if (cell != null) {
			Map map = cell.getAttributes();
			GraphConstants.setBackground(map, color);
			map = GraphConstants.cloneMap(map);
			map.remove(GraphConstants.BOUNDS);
			map.remove(GraphConstants.POINTS);

			Map nested = new HashMap();
			nested.put(cell, GraphConstants.cloneMap(map));
			graph.getGraphLayoutCache().edit(nested, null, null, null);
		}
	}

	private static final Color PROPERTY_COLOR = new Color(255, 158, 62);
	private static final Color SELECTED_COLOR = new Color(255, 255, 50);
	public static boolean isChangedSelectedColor = true;

	public static Object[] changeSelectionCellColor(RDFGraph graph, Object[] lastSelectionCells) {
		if (!isChangedSelectedColor) {
			return lastSelectionCells;
		}
		for (int i = 0; i < lastSelectionCells.length; i++) {
			GraphCell cell = (GraphCell) lastSelectionCells[i];
			if (graph.isRDFResourceCell(cell)) {
				changeCellColor(graph, cell, Color.pink);
			} else if (graph.isRDFPropertyCell(cell)) {
				changeCellColor(graph, cell, Color.black);
			} else if (graph.isRDFLiteralCell(cell)) {
				changeCellColor(graph, cell, Color.orange);
			} else if (graph.isRDFSClassCell(cell)) {
				changeCellColor(graph, cell, Color.green);
			} else if (graph.isRDFSPropertyCell(cell)) {
				changeCellColor(graph, cell, PROPERTY_COLOR);
			}
		}
		Object[] cells = graph.getSelectionCells();
		cells = graph.getDescendants(cells);
		for (int i = 0; i < cells.length; i++) {
			GraphCell cell = (GraphCell) cells[i];
			if (graph.isRDFsCell(cell)) {
				changeCellColor(graph, cell, SELECTED_COLOR);
			}
		}
		return cells;
	}

}
