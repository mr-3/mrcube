/*
 * @(#) ShowAttrDialog.java
 *
 *
 * Copyright (C) 2003 The MMM Project
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 */

package org.semanticweb.mmm.mr3.actions;

import java.awt.event.*;

import javax.swing.*;

import org.semanticweb.mmm.mr3.*;
import org.semanticweb.mmm.mr3.jgraph.*;
import org.semanticweb.mmm.mr3.util.*;

/**
 * @author takeshi morita
 */
public class ShowAttrDialog extends MR3AbstractAction {

    private static final String TITLE = Translator.getString("Component.Window.AttrDialog.Text");
    private static final ImageIcon ICON = Utilities.getImageIcon(Translator.getString("AttributeDialog.Icon"));

    public ShowAttrDialog(MR3 mr3) {
        super(mr3, TITLE, ICON);
        setValues();
    }

    private void setValues() {
        putValue(SHORT_DESCRIPTION, getName());
        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_A, KeyEvent.ALT_MASK));
    }

    public void actionPerformed(ActionEvent e) {
        RDFGraph graph = null;
        Object selectionCell = null;
        JInternalFrame[] iFrames = mr3.getInternalFrames();

        if (!mr3.isActiveFrames()) { return; }
        
        if (iFrames[0].isSelected()) {
            graph = mr3.getRDFGraph();
            selectionCell = graph.getSelectionCell();
        } else if (iFrames[1].isSelected()) {
            graph = mr3.getClassGraph();
            selectionCell = graph.getSelectionCell();
        } else if (iFrames[2].isSelected()) {
            graph = mr3.getPropertyGraph();
            selectionCell = graph.getSelectionCell();
        }
        mr3.getGraphManager().getAttrDialog().setVisible(true);
        
        if (graph != null && selectionCell != null) {
            graph.setSelectionCell(selectionCell);
        }
    }
}
