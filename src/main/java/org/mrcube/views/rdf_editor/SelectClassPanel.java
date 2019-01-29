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

package org.mrcube.views.rdf_editor;

import org.jgraph.event.GraphSelectionEvent;
import org.jgraph.event.GraphSelectionListener;
import org.jgraph.graph.BasicMarqueeHandler;
import org.jgraph.graph.GraphCell;
import org.mrcube.jgraph.GraphManager;
import org.mrcube.jgraph.RDFGraph;
import org.mrcube.jgraph.RDFGraphModel;
import org.mrcube.models.MR3Constants;
import org.mrcube.utils.GraphUtilities;
import org.mrcube.utils.PrefixNSUtil;
import org.mrcube.utils.Translator;
import org.mrcube.utils.Utilities;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Takeshi Morita
 */
public abstract class SelectClassPanel extends JPanel implements GraphSelectionListener {

    private int index; // 検索のインデックス
    private String currentKey; // 現在のキー
    private List findList; // 検索リスト
    protected JLabel nsLabel;

    protected JTextField findField;
    protected JButton findButton;
    protected JComboBox uriPrefixBox;

    protected RDFGraph graph;

    private final GraphManager gmanager;

    public SelectClassPanel(GraphManager gm) {
        index = 0;
        gmanager = gm;
        currentKey = null;

        setLayout(new BorderLayout());
        add(getFindGroupPanel(), BorderLayout.NORTH);
        add(getGraphComponent(), BorderLayout.CENTER);
        add(getEachDialogComponent(), BorderLayout.SOUTH);
    }

    public static final int LIST_WIDTH = 450;
    public static final int LIST_HEIGHT = 20;

    class ChangePrefixAction extends AbstractAction {
        public void actionPerformed(ActionEvent e) {
            PrefixNSUtil.replacePrefix((String) uriPrefixBox.getSelectedItem(), nsLabel);
        }
    }

    private void findResource() {
        if (findField.getText().length() == 0) {
            findList = null;
            return;
        }
        String key = nsLabel.getText() + findField.getText() + ".*";
        if (currentKey == null || (!currentKey.equals(key))) {
            index = 0; // indexを元に戻す
            currentKey = key;
            findList = new ArrayList(gmanager.findRDFSResourceSet(key, graph));
            findNextResource(findList);
        } else {
            findNextResource(findList);
        }
    }

    private void findNextResource(List findList) {
        if (findList != null && findList.size() > 0) {
            if (index == findList.size()) {
                index = 0;
            }
            gmanager.selectCell(findList.get(index), graph);
            index++;
        }
    }

    class TypedFindAction implements DocumentListener {
        public void changedUpdate(DocumentEvent e) {
            findResource();
        }

        public void insertUpdate(DocumentEvent e) {
            findResource();
        }

        public void removeUpdate(DocumentEvent e) {
            findResource();
        }
    }

    class FindAction implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            findResource();
        }
    }

    private JComponent getFindGroupPanel() {
        uriPrefixBox = new JComboBox();
        uriPrefixBox.addActionListener(new ChangePrefixAction());
        PrefixNSUtil.setNamespaceModelSet(GraphUtilities.getNamespaceModelSet());
        uriPrefixBox.setModel(new DefaultComboBoxModel(PrefixNSUtil.getPrefixes().toArray()));
        JComponent uriPrefixBoxP = Utilities.createTitledPanel(uriPrefixBox, MR3Constants.PREFIX);
        findField = new JTextField(15);
        JComponent findFieldP = Utilities.createTitledPanel(findField, "ID");
        FindAction findAction = new FindAction();
        findField.addActionListener(findAction);
        findField.getDocument().addDocumentListener(new TypedFindAction());
        findButton = new JButton(Translator.getString("Find"));
        findButton.addActionListener(findAction);
        nsLabel = new JLabel("");
        JComponent nsLabelP = Utilities.createTitledPanel(nsLabel, MR3Constants.NAME_SPACE);
        uriPrefixBox.setSelectedItem(PrefixNSUtil.getBaseURIPrefix(gmanager.getBaseURI()));

        JPanel findPanel = new JPanel();
        findPanel.setLayout(new BoxLayout(findPanel, BoxLayout.X_AXIS));
        findPanel.add(uriPrefixBoxP);
        findPanel.add(findFieldP);
        findPanel.add(findButton);
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.add(findPanel, BorderLayout.CENTER);
        panel.add(nsLabelP, BorderLayout.SOUTH);
        return panel;
    }

    private JComponent getGraphComponent() {
        graph = new RDFGraph(gmanager, new RDFGraphModel(), null);
        graph.setMarqueeHandler(new BasicMarqueeHandler());
        graph.getSelectionModel().addGraphSelectionListener(this);
        graph.setCloneable(false);
        graph.setBendable(false);
        graph.setDisconnectable(false);
        graph.setPortsVisible(false);
        graph.setDragEnabled(false);
        graph.setDropEnabled(false);
        graph.setEditable(false);

        JScrollPane graphScroll = new JScrollPane(graph);
        Utilities.initComponent(graphScroll, "", LIST_WIDTH, 150);
        return graphScroll;
    }

    protected abstract JComponent getEachDialogComponent();

    public void replaceGraph(RDFGraph newGraph) {
        graph.setModel(newGraph.getModel());
    }

    public abstract void valueChanged(GraphSelectionEvent e);

    public RDFGraph getGraph() {
        return graph;
    }

    void changeAllCellColor(Color color) {
        Object[] cells = graph.getAllCells();
        for (Object cell1 : cells) {
            GraphCell cell = (GraphCell) cell1;
            if (RDFGraph.isRDFSCell(cell)) {
                GraphUtilities.changeDefaultCellStyle(graph, cell, color);
            }
        }
    }
}
