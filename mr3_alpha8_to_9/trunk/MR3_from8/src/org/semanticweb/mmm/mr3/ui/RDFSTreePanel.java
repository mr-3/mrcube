package org.semanticweb.mmm.mr3.ui;
import java.util.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;

import org.semanticweb.mmm.mr3.data.*;
import org.semanticweb.mmm.mr3.jgraph.*;
import org.semanticweb.mmm.mr3.util.*;

import org.jgraph.graph.*;

public class RDFSTreePanel extends JPanel implements TreeSelectionListener {

	private JTree rdfsTree;
	private TreeModel treeModel;
	private GraphManager gmanager;
	private RDFSInfoMap rdfsInfoMap = RDFSInfoMap.getInstance();

	public RDFSTreePanel(GraphManager manager, TreeModel model, TreeCellRenderer renderer) {
		treeModel = model;
		gmanager = manager;
		rdfsTree = new JTree(treeModel);
		rdfsTree.setCellRenderer(renderer);

		rdfsTree.addTreeSelectionListener(this);
		JScrollPane classTreeScroll = new JScrollPane(rdfsTree);
		Utilities.initComponent(classTreeScroll, "", 200, 500);
		add(classTreeScroll);
	}

	public void addTreeSelectionListener(TreeSelectionListener tsl) {
		rdfsTree.addTreeSelectionListener(tsl);
	}

	public void valueChanged(TreeSelectionEvent e) {
		JTree tree = (JTree) e.getSource();
		DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
		if (node != null) {
			Object cell = node.getUserObject();
			if (gmanager.getClassGraph().isContains(cell)) {
				gmanager.jumpClassArea(cell);
			} else {
				gmanager.jumpPropertyArea(cell);
			}
		}
	}

	class GraphModelTreeNode implements TreeNode {

		protected GraphModel model;

		public GraphModelTreeNode(GraphModel model) {
			this.model = model;
		}

		public Enumeration children() {
			Vector v = new Vector();
			for (int i = 0; i < model.getRootCount(); i++)
				v.add(model.getRootAt(i));
			return v.elements();
		}

		public boolean getAllowsChildren() {
			return true;
		}

		public TreeNode getChildAt(int childIndex) {
			return (TreeNode) model.getRootAt(childIndex);
		}

		public int getChildCount() {
			return model.getRootCount();
		}

		public int getIndex(TreeNode node) {
			return model.getIndexOfRoot(node);
		}

		public TreeNode getParent() {
			return null;
		}

		public boolean isLeaf() {
			return false;
		}

		public String toString() {
			return model.toString();
		}
	}
}
