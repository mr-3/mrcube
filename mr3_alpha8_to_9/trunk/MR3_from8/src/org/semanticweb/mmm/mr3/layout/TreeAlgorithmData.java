package org.semanticweb.mmm.mr3.layout;
/**
 * @author take
 *
 */
public class TreeAlgorithmData {

	public int level;
	public GraphLayoutData parent;
	public GraphLayoutData leftChild;
	public GraphLayoutData rightChild;
	public GraphLayoutData leftSibling;
	public GraphLayoutData rightSibling;
	public GraphLayoutData leftNeighbor;
	public GraphLayoutData rightNeighbor;
	public boolean isLeaf;
	public double modifier;
	public double prelim;
	public GraphLayoutData group;

	public TreeAlgorithmData() {
		level = -1;
		parent = leftChild = rightChild = leftSibling = rightSibling = leftNeighbor = rightNeighbor = null;
		isLeaf = false;
		modifier = prelim = 0;
		group = null;
	}
}

