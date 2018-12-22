/*
 * Project Name: MR^3 (Meta-Model Management based on RDFs Revision Reflection)
 * Project Website: http://mrcube.org/
 * 
 * Copyright (C) 2003-2018 Yamaguchi Laboratory, Keio University. All rights reserved.
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

package org.mrcube.views;

import org.apache.jena.rdf.model.*;
import org.apache.jena.vocabulary.RDF;
import org.jgraph.graph.GraphModel;
import org.mrcube.jgraph.GraphManager;
import org.mrcube.models.NamespaceModel;
import org.mrcube.utils.Utilities;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreeNode;
import java.awt.*;
import java.util.*;

/**
 * 
 * @author Takeshi Morita
 * 
 */
public class MR3TreePanel extends JPanel {

    private JTree rdfsTree;
    private GraphManager gmanager;
    private DefaultTreeModel treeModel;
    private MR3TreeCellRenderer rdfTreeCellRenderer;
    private MR3TreeCellRenderer classTreeCellRenderer;
    private MR3TreeCellRenderer propertyTreeCellRenderer;

    public MR3TreePanel(GraphManager manager) {
        gmanager = manager;
        treeModel = new DefaultTreeModel(null);
        rdfsTree = new JTree(treeModel);
        rdfTreeCellRenderer = new MR3TreeCellRenderer(Utilities.getImageIcon("rdfIcon.gif"), Utilities
                .getImageIcon("classIcon.gif"));
        classTreeCellRenderer = new MR3TreeCellRenderer(Utilities.getImageIcon("classIcon.gif"), null);
        propertyTreeCellRenderer = new MR3TreeCellRenderer(Utilities.getImageIcon("propertyIcon.gif"), null);
        JScrollPane rdfsTreeScroll = new JScrollPane(rdfsTree);
        Utilities.initComponent(rdfsTreeScroll, "", 200, 500);
        setLayout(new BorderLayout());
        add(rdfsTreeScroll, BorderLayout.CENTER);
    }

    public Object getRoot() {
        return treeModel.getRoot();
    }

    public void addActionListener(TreeSelectionListener tsl) {
        rdfsTree.addTreeSelectionListener(tsl);
    }

    public void replaceNameSpace(Object parent, Set prefixNSInfoSet) {
        if (treeModel.getChildCount(parent) == 0) { return; }
        for (int i = 0; i < treeModel.getChildCount(parent); i++) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) treeModel.getChild(parent, i);
            Resource resource = ResourceFactory.createResource(node.getUserObject().toString());
            String ns = resource.getNameSpace();
            String id = resource.getLocalName();
            for (Iterator j = prefixNSInfoSet.iterator(); j.hasNext();) {
                NamespaceModel info = (NamespaceModel) j.next();
                if (info.getNameSpace().equals(ns)) {
                    node.setUserObject(info.getPrefix() + ":" + id);
                    break;
                }
            }
            replaceNameSpace(node, prefixNSInfoSet);
        }
    }

    public void setNonRoot(TreeNode rootNode) {
        treeModel.setRoot(rootNode);
        rdfsTree.setRootVisible(false);
    }

    public void setRoot(TreeNode rootNode) {
        treeModel.setRoot(rootNode);
        rdfsTree.setRootVisible(true);
    }

    public void setRDFTreeCellRenderer() {
        rdfsTree.setCellRenderer(rdfTreeCellRenderer);
    }

    public void setClassTreeCellRenderer() {
        rdfsTree.setCellRenderer(classTreeCellRenderer);
    }

    public void setPropertyTreeCellRenderer() {
        rdfsTree.setCellRenderer(propertyTreeCellRenderer);
    }

    public void addTreeSelectionListener(TreeSelectionListener tsl) {
        rdfsTree.addTreeSelectionListener(tsl);
    }

    private static Map resSubResSetMap;

    public static TreeNode getRDFSTreeRoot(Model model, Resource rootResource, Property subRDFSOf) {
        resSubResSetMap = new HashMap();
        for (ResIterator i = model.listSubjectsWithProperty(RDF.type); i.hasNext();) {
            Resource resource = i.nextResource();
            for (StmtIterator j = resource.listProperties(subRDFSOf); j.hasNext();) {
                Statement stmt = j.nextStatement();
                Resource supResource = (Resource) stmt.getObject();
                Set subResourceSet = (Set) resSubResSetMap.get(supResource);
                if (subResourceSet == null) {
                    subResourceSet = new HashSet();
                }
                subResourceSet.add(resource);
                resSubResSetMap.put(supResource, subResourceSet);
            }
        }
        setRootSubResource(model, rootResource, subRDFSOf);
        DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode(rootResource);
        if (resSubResSetMap.get(rootResource) != null) {
            createRDFSNodes(rootResource, rootNode);
        }
        return rootNode;
    }

    public void setRDFSTreeRoot(Model model, Resource rootResource, Property subRDFSOf) {
        setRoot(getRDFSTreeRoot(model, rootResource, subRDFSOf));
    }

    private static void setRootSubResource(Model model, Resource rootResource, Property subRDFSOf) {
        // 手抜き．RDF.typeではなく，メタクラスリストを利用しないといけない
        for (ResIterator i = model.listSubjectsWithProperty(RDF.type); i.hasNext();) {
            Resource resource = i.nextResource();
            if (!resource.listProperties(subRDFSOf).hasNext() && !resource.equals(rootResource)) {
                // System.out.println("res: " + resource);
                Set rootSubResourceSet = (Set) resSubResSetMap.get(rootResource);
                if (rootSubResourceSet == null) {
                    rootSubResourceSet = new HashSet();
                }
                rootSubResourceSet.add(resource);
                resSubResSetMap.put(rootResource, rootSubResourceSet);
            }
        }
    }

    private static void createRDFSNodes(Resource resource, DefaultMutableTreeNode node) {
        Set subRDFSSet = (Set) resSubResSetMap.get(resource);
        for (Iterator i = subRDFSSet.iterator(); i.hasNext();) {
            Resource subRDFS = (Resource) i.next();
            DefaultMutableTreeNode subNode = new DefaultMutableTreeNode(subRDFS);
            if (resSubResSetMap.get(subRDFS) != null) {
                createRDFSNodes(subRDFS, subNode);
            }
            node.add(subNode);
        }
    }

    public void valueChanged(TreeSelectionEvent e) {
        JTree tree = (JTree) e.getSource();
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
        if (node != null) {
            Object cell = node.getUserObject();
            if (gmanager.getCurrentClassGraph().isContains(cell)) {
                gmanager.selectClassCell(cell);
            } else {
                gmanager.selectPropertyCell(cell);
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

    public class MR3TreeCellRenderer extends JLabel implements TreeCellRenderer {

        private Icon imageIcon;
        private Icon typeIcon;

        public MR3TreeCellRenderer(ImageIcon icon, ImageIcon type) {
            imageIcon = icon;
            typeIcon = type;
            setOpaque(true);
        }

        public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded,
                boolean leaf, int row, boolean hasFocus) {

            setText(value.toString());

            if (selected) {
                setBackground(new Color(0, 0, 128));
                setForeground(Color.white);
            } else {
                setBackground(Color.white);
                setForeground(Color.black);
            }

            if (typeIcon != null) {
                if (leaf) {
                    setIcon(imageIcon);
                } else {
                    setIcon(typeIcon);
                }
            } else {
                setIcon(imageIcon);
            }
            return this;
        }
    }
}
