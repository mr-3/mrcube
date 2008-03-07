/*
 * Project Name: MR^3 (Meta-Model Management based on RDFs Revision Reflection)
 * Project Website: http://mr3.sourceforge.net/
 * 
 * Copyright (C) 2003-2008 Yamaguchi Laboratory, Keio University. All rights reserved. 
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

package org.semanticweb.mmm.mr3.ui;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;
import javax.swing.event.*;

import org.semanticweb.mmm.mr3.data.*;
import org.semanticweb.mmm.mr3.jgraph.*;
import org.semanticweb.mmm.mr3.util.*;

import org.jgraph.graph.*;

/**
 * @author takeshi morita
 * 
 */
public class RemoveDialog extends JInternalFrame implements ListSelectionListener, ActionListener {

    private JButton applyButton;
    private JButton cancelButton;

    private JList removeRDFSList;
    private ReferenceListPanel refListPanel;
    private GraphManager gmanager;

    private static final int LIST_WIDTH = 400;
    private static final int LIST_HEIGHT = 100;

    public RemoveDialog(GraphManager manager) {
        super(Translator.getString("RemoveDialog.Title"), true, false);
        gmanager = manager;

        removeRDFSList = new JList();
        removeRDFSList.addListSelectionListener(this);
        JScrollPane removeRDFSListScroll = new JScrollPane(removeRDFSList);
        Utilities.initComponent(removeRDFSListScroll, Translator.getString("RemoveDialog.Label.RemoveList"),
                LIST_WIDTH, LIST_HEIGHT);
        refListPanel = new ReferenceListPanel(gmanager);

        Container contentPane = getContentPane();
        contentPane.add(removeRDFSListScroll, BorderLayout.NORTH);
        contentPane.add(refListPanel, BorderLayout.CENTER);
        contentPane.add(getButtonPanel(), BorderLayout.SOUTH);

        setLocation(100, 100);
        setSize(new Dimension(500, 350));
        setVisible(false);
    }

    private JComponent getButtonPanel() {
        applyButton = new JButton(MR3Constants.APPLY);
        applyButton.setMnemonic('a');
        applyButton.addActionListener(this);
        cancelButton = new JButton(MR3Constants.CANCEL);
        cancelButton.setMnemonic('c');
        cancelButton.addActionListener(this);
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(1, 2, 5, 5));
        buttonPanel.add(applyButton);
        buttonPanel.add(cancelButton);
        return Utilities.createEastPanel(buttonPanel);
    }

    public void setRefListInfo(Set cells, Map classRDFMap, Map classPropMap) {
        removeRDFSList.setListData(cells.toArray());
        refListPanel.setTableModelMap(cells, classRDFMap, classPropMap);
        if (removeRDFSList.getModel().getSize() != 0) {
            removeRDFSList.setSelectedIndex(0);
        }
    }

    public void valueChanged(ListSelectionEvent e) {
        Object cell = removeRDFSList.getSelectedValue();
        refListPanel.replaceTableModel(cell);
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == applyButton) {
            ListModel listModel = removeRDFSList.getModel();
            for (int i = 0; i < listModel.getSize(); i++) {
                DefaultGraphCell removeRDFSCell = (DefaultGraphCell) listModel.getElementAt(i);
                refListPanel.removeAction(removeRDFSCell);
            }
            setVisible(false);
            gmanager.retryRemoveCells();
        } else if (e.getSource() == cancelButton) {
            setVisible(false);
        }
    }

    public void setVisible(boolean t) {
        if (gmanager != null) {
            gmanager.setEnabled(!t);
        }
        super.setVisible(t);
    }
}
