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

import net.sourceforge.mr3.jgraph.*;
import net.sourceforge.mr3.util.*;

/**
 * @author Takeshi Morita
 */
public class RemoveAction extends AbstractAction {

    private RDFGraph graph;
    private GraphManager gmanager;
    private static final String TITLE = Translator.getString("Action.Remove.Text");
    private static final ImageIcon ICON = Utilities.getImageIcon(Translator.getString("Action.Remove.Icon"));

    public RemoveAction(RDFGraph g, GraphManager gm) {
        super(TITLE, ICON);
        graph = g;
        gmanager = gm;
        putValue(SHORT_DESCRIPTION, TITLE);
        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke("DELETE"));
    }

    public void actionPerformed(ActionEvent e) {
        gmanager.removeAction(graph);
    }
}
