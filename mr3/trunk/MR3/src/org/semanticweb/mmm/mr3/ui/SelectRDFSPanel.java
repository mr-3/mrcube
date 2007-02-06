/*
 * @(#) SelectRDFSPanel.java
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

import org.jgraph.event.*;
import org.semanticweb.mmm.mr3.jgraph.*;
import org.semanticweb.mmm.mr3.util.*;

/**
 * @author takeshi morita
 */
public class SelectRDFSPanel extends SelectClassPanel {
    private Set orgRegionSet;
    private Set newRegionSet;
    private JList regionList;
    private JScrollPane regionListScroll;

    public SelectRDFSPanel(GraphManager gm) {
        super(gm);
        newRegionSet = new HashSet();
    }

    public void setRegionSet(Set set) {
        orgRegionSet = set;
        regionList.setListData(orgRegionSet.toArray());
    }

    protected JComponent getEachDialogComponent() {
        regionList = new JList();
        regionListScroll = new JScrollPane(regionList);
        Utilities.initComponent(regionListScroll, Translator.getString("SelectedList"), LIST_WIDTH, 100);
        return regionListScroll;
    }

    public void valueChanged(GraphSelectionEvent e) {
        newRegionSet.removeAll(newRegionSet);
        Object[] cells = graph.getSelectionCells();
        for (int i = 0; i < cells.length; i++) {
            if (RDFGraph.isRDFSCell(cells[i])) {
                newRegionSet.add(cells[i]);
            }
        }
        if (orgRegionSet != null) {
            newRegionSet.addAll(orgRegionSet);
        }
        regionList.setListData(newRegionSet.toArray());
    }

    public Set getRegionSet() {
        return newRegionSet;
    }

    public void setEnabled(boolean flag) {
        super.setEnabled(flag);
        uriPrefixBox.setEnabled(flag);
        nsLabel.setEnabled(flag);
        findField.setEnabled(flag);
        findButton.setEnabled(flag);
        graph.setEnabled(flag);
        regionList.setEnabled(flag);
    }
}
