/*
 * Project Name: MR^3 (Meta-Model Management based on RDFs Revision Reflection)
 * Project Website: https://mr-3.github.io/
 * 
 * Copyright (C) 2003-2025 Takeshi Morita. All rights reserved.
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

package jp.ac.aoyama.it.ke.mrcube.views;

import jp.ac.aoyama.it.ke.mrcube.views.common.CommentPanel;
import jp.ac.aoyama.it.ke.mrcube.views.common.LabelPanel;
import jp.ac.aoyama.it.ke.mrcube.views.common.ResourceListCellRenderer;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.jgraph.graph.DefaultGraphCell;
import org.jgraph.graph.GraphCell;
import org.jgraph.graph.GraphConstants;
import jp.ac.aoyama.it.ke.mrcube.actions.EditConceptAction;
import jp.ac.aoyama.it.ke.mrcube.jgraph.GraphManager;
import jp.ac.aoyama.it.ke.mrcube.jgraph.RDFGraph;
import jp.ac.aoyama.it.ke.mrcube.models.MR3Constants;
import jp.ac.aoyama.it.ke.mrcube.models.NamespaceModel;
import jp.ac.aoyama.it.ke.mrcube.models.RDFSModel;
import jp.ac.aoyama.it.ke.mrcube.utils.GraphUtilities;
import jp.ac.aoyama.it.ke.mrcube.utils.PrefixNSUtil;
import jp.ac.aoyama.it.ke.mrcube.utils.Translator;
import jp.ac.aoyama.it.ke.mrcube.utils.Utilities;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Takeshi Morita
 */
public abstract class OntologyPanel extends JPanel implements ListSelectionListener {

    private final EditConceptAction editConceptAction;

    public JPanel menuPanel;
    public JList menuList;
    public CardLayout cardLayout;

    public final BasePanel basePanel;
    public final LabelPanel labelPanel;
    public final CommentPanel commentPanel;

    private Set<NamespaceModel> namespaceModelSet;

    public final JButton applyButton;
    public final JButton resetButton;
    public final JButton cancelButton;

    public GraphCell cell;
    private final RDFGraph graph;
    protected RDFSModel rdfsModel;
    public final GraphManager gmanager;

    public final JList instanceJList;
    public final JScrollPane instanceListScroll;

    public static final int MENU_WIDTH = 120;

    public OntologyPanel(RDFGraph g, GraphManager gm) {
        graph = g;
        gmanager = gm;

        basePanel = new BasePanel();
        labelPanel = new LabelPanel();
        commentPanel = new CommentPanel(gmanager.getRootFrame());
        instanceJList = new JList();
        instanceJList.setCellRenderer(new ResourceListCellRenderer());
        instanceJList.addListSelectionListener(new EditInstanceAction());
        instanceListScroll = new JScrollPane(instanceJList);

        applyButton = new JButton(MR3Constants.APPLY);
        applyButton.setMnemonic('a');
        editConceptAction = new EditConceptAction(basePanel, graph, gmanager);
        applyButton.addActionListener(editConceptAction);
        resetButton = new JButton(MR3Constants.RESET);
        resetButton.setMnemonic('s');
        resetButton.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                graph.setSelectionCell(cell);
            }
        });
        cancelButton = new JButton(MR3Constants.CANCEL);
        cancelButton.setMnemonic('c');
        cancelButton.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                gmanager.setVisibleAttrDialog(false);
            }
        });
    }

    public void valueChanged(ListSelectionEvent e) {
        cardLayout.show(menuPanel, menuList.getSelectedValue().toString());
    }

    public class BasePanel extends JPanel {

        final JComboBox metaClassBox;
        final JComboBox uriPrefixBox;
        final JTextField idField;
        final JLabel nsLabel;

        BasePanel() {
            metaClassBox = new JComboBox();
            JComponent metaClassBoxP = Utilities.createTitledPanel(metaClassBox, Translator.getString("Type"));

            uriPrefixBox = new JComboBox();
            uriPrefixBox.addActionListener(new ChangePrefixAction());
            JComponent uriPrefixBoxP = Utilities.createTitledPanel(uriPrefixBox, MR3Constants.PREFIX);
            idField = new JTextField();
            JComponent idFieldP = Utilities.createTitledPanel(idField, "ID");

            JPanel uriPanel = new JPanel();
            uriPanel.setLayout(new GridLayout(1, 2));
            uriPanel.add(uriPrefixBoxP);
            uriPanel.add(idFieldP);

            nsLabel = new JLabel("");
            JComponent nsLabelP = Utilities.createTitledPanel(nsLabel, MR3Constants.NAMESPACE);

            JPanel basePanel = new JPanel();
            basePanel.setLayout(new GridLayout(3, 1, 5, 0));
            basePanel.add(metaClassBoxP);
            basePanel.add(uriPanel);
            basePanel.add(nsLabelP);

            setLayout(new BorderLayout());
            add(basePanel, BorderLayout.NORTH);
        }

        public String getURIString() {
            return nsLabel.getText() + idField.getText();
        }

        public String getMetaClassString() {
            return metaClassBox.getSelectedItem().toString();
        }

        public String toString() {
            return "URI";
        }

        public void setMetaClassList(Set<Resource> metaClassList) {
            setMetaClassBox(metaClassList);
        }

        JTextField getIDField() {
            return idField;
        }

        class ChangePrefixAction extends AbstractAction {
            public void actionPerformed(ActionEvent e) {
                PrefixNSUtil.replacePrefix((String) uriPrefixBox.getSelectedItem(), nsLabel);
            }
        }

        void initURIPrefixBox() {
            uriPrefixBox.setModel(new DefaultComboBoxModel(PrefixNSUtil.getPrefixes().toArray()));
        }

        void setMetaClassBox(Set<Resource> metaClassList) {
            ComboBoxModel model = new DefaultComboBoxModel(metaClassList.toArray());
            metaClassBox.setModel(model);
            metaClassBox.setSelectedItem(ResourceFactory.createResource(rdfsModel.getMetaClass()));
            metaClassBox.setEnabled(!metaClassList.contains(rdfsModel.getURI()));
        }

        void setPrefix() {
            for (NamespaceModel prefNSInfo : namespaceModelSet) {
                if (prefNSInfo.getNameSpace().equals(rdfsModel.getURI().getNameSpace())) {
                    uriPrefixBox.setSelectedItem(prefNSInfo.getPrefix());
                    nsLabel.setText(prefNSInfo.getNameSpace());
                    break;
                }
            }
        }
    }

    private void setCell(GraphCell cell) {
        this.cell = cell;
    }

    protected void setValue() {
        labelPanel.clearField();
        labelPanel.setResourceInfo(rdfsModel);
        commentPanel.setResourceInfo(rdfsModel);
        basePanel.getIDField().setText(rdfsModel.getLocalName());
    }

    protected abstract void setValue(Set<GraphCell> supCellSet);

    /** スーパークラスまたは、スーパープロパティの名前のセットを返す */
    public Object[] getTargetInfo(Set<GraphCell> supCellSet) {
        Set<GraphCell> result = new HashSet<>();
        for (GraphCell cell : supCellSet) {
            result.add(cell);
//            RDFSModel supInfo = (RDFSModel) GraphConstants.getValue(cell.getAttributes());
//            result.add(supInfo.getURIStr());
        }
        return result.toArray();
    }

    protected abstract void setInstanceList();

    class EditInstanceAction implements ListSelectionListener {
        public void valueChanged(ListSelectionEvent e) {
            Object cell = instanceJList.getSelectedValue();
            gmanager.selectRDFCell(cell);
        }
    }

    public void showRDFSInfo(DefaultGraphCell cell) {
        if (RDFGraph.isRDFSCell(cell)) {
            rdfsModel = (RDFSModel) GraphConstants.getValue(cell.getAttributes());
            if (rdfsModel != null) {
                editConceptAction.setGraphCell(cell);
                editConceptAction.setRDFSInfo(rdfsModel);
                setCell(cell);
                namespaceModelSet = GraphUtilities.getNamespaceModelSet();
                PrefixNSUtil.setNamespaceModelSet(namespaceModelSet);
                basePanel.initURIPrefixBox();
                basePanel.setPrefix();
                setInstanceList();
                Set<GraphCell> targetCells = graph.getTargetCells(cell);
                setValue(targetCells);
                rdfsModel.setSuperRDFS(targetCells);
                SwingUtilities.invokeLater(() -> basePanel.getIDField().requestFocus());
            }
        }
    }
}
