package mr3.layout;
import java.awt.*;
import java.awt.geom.*;
import java.util.*;

import com.jgraph.*;
import com.jgraph.graph.*;

/**
 * @author takeshi morita
 *
 */
public class GraphLayoutData {

	private Point2D.Double point;
	private Dimension dimension;
	private Rectangle rec;
	private JGraph graph;
	private GraphCell cell;
	private Set children;
	private boolean hasParent;
	TreeAlgorithmData data;

	public GraphLayoutData(GraphCell cell, JGraph graph) {
		this.cell = cell;
		this.graph = graph;
		children = new HashSet();
		Map map = cell.getAttributes();
		rec = GraphConstants.getBounds(map);
		point = new Point2D.Double(rec.x, rec.y);
		hasParent = false;
		dimension = new Dimension(rec.width, rec.height);
	}

	public Point2D.Double getPosition() {
		return point;
	}

	public GraphCell getCell() {
		return cell;
	}

	public void setPosition(double x, double y) {
		point.x = x;
		point.y = y;
	}

	public Dimension getBoundingBox() {
		return dimension;
	}

	public void addChild(Object child) {
		children.add(child);
	}

	public Set getChildren() {
		return Collections.unmodifiableSet(children);
	}

	public int getChildCount() {
		return children.size();
	}

	public void setHasParent(boolean t) {
		hasParent = t;
	}

	public boolean hasParent() {
		return hasParent;
	}

	private static final int cellHeight = 30;
	public void setRealTypePosition(GraphCell typeCell) {
		rec.x = (int) point.x;
		rec.y = (int) (point.y+cellHeight);
		rec.height = 25;
		setRealPosition(typeCell);
	}

	public void setRealResourcePosition() {
		rec.x = (int) point.x;
		rec.y = (int) point.y;
			
		setRealPosition(cell);
	}

	private void setRealPosition(GraphCell localCell) {
		Map map = localCell.getAttributes();
		GraphConstants.setBounds(map, rec);
		Map nested = new HashMap();
		nested.put(localCell, GraphConstants.cloneMap(map));
		graph.getModel().edit(nested, null, null, null);
	}

	public String toString() {
		String msg = "";

		msg += "Cell: " + graph.convertValueToString(cell);
		msg += " Position: x: " + point.x + "y: " + point.y;
		msg += " Dimension: width: " + dimension.width + "height: " + dimension.height;

		return msg;
	}
}
