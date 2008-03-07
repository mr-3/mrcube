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

package org.semanticweb.mmm.mr3.actions;

import java.awt.event.*;

import javax.swing.*;

import org.semanticweb.mmm.mr3.*;
import org.semanticweb.mmm.mr3.data.MR3Constants.*;
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
        MR3Project project = MR3.getCurrentProject();

        if (project.getFocusedEditorType() == GraphType.RDF) {
            graph = mr3.getRDFGraph();
            selectionCell = graph.getSelectionCell();
        } else if (project.getFocusedEditorType() == GraphType.CLASS) {
            graph = mr3.getClassGraph();
            selectionCell = graph.getSelectionCell();
        } else if (project.getFocusedEditorType() == GraphType.PROPERTY) {
            graph = mr3.getPropertyGraph();
            selectionCell = graph.getSelectionCell();
        }
        mr3.getGraphManager().getAttrDialog().setVisible(true);

        if (graph != null && selectionCell != null) {
            graph.setSelectionCell(selectionCell);
        }
    }
}