/*
 * Project Name: MR^3 (Meta-Model Management based on RDFs Revision Reflection)
 * Project Website: http://mr3.sourceforge.net/
 * 
 * Copyright (C) 2003-2015 Yamaguchi Laboratory, Keio University. All rights reserved. 
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

package net.sourceforge.mr3.actions;

import java.awt.event.*;

import javax.swing.*;

import net.sourceforge.mr3.data.MR3Constants.*;
import net.sourceforge.mr3.jgraph.*;
import net.sourceforge.mr3.ui.*;
import net.sourceforge.mr3.util.*;

/**
 * @author Takeshi Morita
 */
public class CopyAction extends AbstractAction {

    private RDFGraph graph;
    private static final String TITLE = Translator.getString("Action.Copy.Text");
    private static final ImageIcon ICON = Utilities.getImageIcon(Translator.getString("Action.Copy.Icon"));

    public CopyAction(RDFGraph g) {
        super(TITLE, ICON);
        graph = g;
        putValue(SHORT_DESCRIPTION, TITLE);
        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_C, KeyEvent.CTRL_MASK));
    }

    public void actionPerformed(ActionEvent e) {
        TransferHandler.getCopyAction().actionPerformed(new ActionEvent(graph, e.getID(), e.getActionCommand()));
        if (graph.getType() == GraphType.RDF) {
            HistoryManager.saveHistory(HistoryType.COPY_RDF_GRAPH);
        } else if (graph.getType() == GraphType.CLASS) {
            HistoryManager.saveHistory(HistoryType.COPY_CLASS_GRAPH);
        } else if (graph.getType() == GraphType.PROPERTY) {
            HistoryManager.saveHistory(HistoryType.COPY_PROPERTY_GRAPH);
        }
    }
}
