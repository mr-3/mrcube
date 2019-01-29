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

import org.jgraph.graph.GraphCell;
import org.mrcube.jgraph.*;
import org.mrcube.utils.Translator;
import org.mrcube.utils.Utilities;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Takeshi Morita
 */
public class FindResourceDialog extends JDialog {

    private final GraphManager gmanager;

    private JTextField keywordField;
    private final JList<GraphCell> resourceList;
    private DefaultListModel<GraphCell> resourceListModel;

    private static final int LIST_WIDTH = 400;
    private static final int LIST_HEIGHT = 300;
    private static final int FIELD_HEIGHT = 30;

    public FindResourceDialog(GraphManager gm) {
        super(gm.getRootFrame(), Translator.getString("FindResourceDialog.Title"), false);
        setIconImage(Utilities.getImageIcon(Translator.getString("FindResourceDialog.Icon")).getImage());

        gmanager = gm;
        keywordField = new JTextField();
        keywordField.getDocument().addDocumentListener(new FindAction());
        JComponent findFieldPanel = Utilities.createTitledPanel(keywordField, Translator.getString("Keyword"), LIST_WIDTH, FIELD_HEIGHT);

        resourceListModel = new DefaultListModel<>();
        resourceList = new JList(resourceListModel);
        resourceList.setCellRenderer(new ResourceListCellRenderer());
        resourceList.addListSelectionListener(new SelectResourceAction());
        JComponent resourceListPanel = Utilities.createTitledPanel(new JScrollPane(resourceList),
                Translator.getString("FindResult"), LIST_WIDTH, LIST_HEIGHT);

        Container contentPane = getContentPane();
        contentPane.setLayout(new BorderLayout());
        contentPane.add(findFieldPanel, BorderLayout.NORTH);
        contentPane.add(resourceListPanel, BorderLayout.CENTER);

        pack();
        setLocationRelativeTo(gmanager.getRootFrame());
        setVisible(false);
    }

    public void setVisible(boolean isVisible) {
        super.setVisible(isVisible);
    }

    private Set<GraphCell> findResourceSet(String keyword) {
        Set<GraphCell> resourceSet = new HashSet<>();
        resourceSet.addAll(gmanager.findRDFResourceSet(keyword));
        resourceSet.addAll(gmanager.findRDFSResourceSet(keyword, gmanager.getCurrentClassGraph()));
        resourceSet.addAll(gmanager.findRDFSResourceSet(keyword, gmanager.getCurrentPropertyGraph()));
        return resourceSet;
    }

    private void setFindedResourceSet() {
        resourceListModel.clear();
        resourceListModel.addAll(findResourceSet(keywordField.getText()));
    }

    class FindAction implements DocumentListener {

        public void changedUpdate(DocumentEvent e) {
            setFindedResourceSet();
        }

        public void insertUpdate(DocumentEvent e) {
            setFindedResourceSet();
        }

        public void removeUpdate(DocumentEvent e) {
            setFindedResourceSet();
        }
    }

    class SelectResourceAction implements ListSelectionListener {
        public void valueChanged(ListSelectionEvent e) {
            Object cell = resourceList.getSelectedValue();
            gmanager.selectRDFCell(cell);
            gmanager.selectClassCell(cell);
            gmanager.selectPropertyCell(cell);
        }
    }

    class ResourceListCellRenderer extends DefaultListCellRenderer {
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value instanceof RDFResourceCell) {
                setIcon(RDFGraphMarqueeHandler.RDF_RESOURCE_ELLIPSE_ICON);
            } else if (value instanceof OntClassCell) {
                setIcon(ClassGraphMarqueeHandler.CLASS_RECTANGLE_ICON);
            } else if (value instanceof OntPropertyCell) {
                setIcon(PropertyGraphMarqueeHandler.PROPERTY_ELLIPSE_ICON);
            }
            return this;
        }
    }

}