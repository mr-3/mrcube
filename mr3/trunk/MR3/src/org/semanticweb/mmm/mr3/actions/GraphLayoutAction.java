/*
 * @(#) GraphLayoutAction 2004/01/09
 * 
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

package org.semanticweb.mmm.mr3.actions;

import java.awt.event.*;

import javax.swing.*;

import org.semanticweb.mmm.mr3.data.MR3Constants.*;
import org.semanticweb.mmm.mr3.jgraph.*;
import org.semanticweb.mmm.mr3.util.*;

/**
 * @author takeshi morita
 */
public class GraphLayoutAction extends AbstractAction {

    private GraphType graphType;
    private GraphManager gmanager;
    private static final ImageIcon ICON = Utilities.getImageIcon(Translator
            .getString("Component.View.ApplyLayout.Icon"));

    public GraphLayoutAction(GraphManager gm, GraphType type) {
        super(getString(type), ICON);
        gmanager = gm;
        graphType = type;
        putValue(SHORT_DESCRIPTION, type.toString() + "Graph Layout");
    }

    private static String getString(GraphType type) {
        if (type == GraphType.CLASS) {
            return Translator.getString("Class");
        } else if (type == GraphType.PROPERTY) {
            return Translator.getString("Property");
        } else {
            return type.toString();
        }
    }

    public void actionPerformed(ActionEvent arg0) {
        gmanager.applyLayout(graphType);
    }
}
