/*
 * @(#) FindResAction.java
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
import org.semanticweb.mmm.mr3.ui.*;
import org.semanticweb.mmm.mr3.util.*;

/**
 * @author takeshi morita
 */
public class FindResAction extends AbstractAction {

    private GraphType graphType;
    private GraphManager gmanager;
    private static final String TITLE = Translator.getString("Component.Edit.FindResource.Text");
    private static final ImageIcon ICON = Utilities.getImageIcon("find.gif");

    public FindResAction(RDFGraph g, GraphManager gm) {
        super(TITLE, ICON);
        gmanager = gm;
        setValues();
        if (g == null) {
            graphType = null;
        } else {
            graphType = g.getType();
        }
    }

    private void setValues() {
        putValue(SHORT_DESCRIPTION, TITLE);
        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_F, KeyEvent.CTRL_MASK));
    }

    public void actionPerformed(ActionEvent e) {
        FindResourceDialog findResDialog = gmanager.getFindResourceDialog();
        findResDialog.setFindArea(graphType);
        findResDialog.setVisible(true);
    }
}
