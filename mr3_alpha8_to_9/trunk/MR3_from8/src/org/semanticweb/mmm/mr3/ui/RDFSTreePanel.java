/*
 * @(#) RDFSTreePanel.java
 * 
 * Copyright (C) 2003 The MMM Project
 * 
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation; either version 2.1 of the License, or (at your
 * option) any later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *  
 */

package org.semanticweb.mmm.mr3.ui;

import java.util.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;

import org.semanticweb.mmm.mr3.data.*;
import org.semanticweb.mmm.mr3.jgraph.*;
import org.semanticweb.mmm.mr3.util.*;

import org.jgraph.graph.*;

/*
 * 
 * @author takeshi morita
 *
 */
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
