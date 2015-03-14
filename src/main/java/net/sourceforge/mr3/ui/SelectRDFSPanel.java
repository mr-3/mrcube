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

package net.sourceforge.mr3.ui;

import java.util.*;

import javax.swing.*;

import net.sourceforge.mr3.jgraph.*;
import net.sourceforge.mr3.util.*;

import org.jgraph.event.*;

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
        for (Object cell : graph.getSelectionCells()) {
            if (RDFGraph.isRDFSCell(cell)) {
                newRegionSet.add(cell);
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
