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

package org.mrcube.actions;

import org.mrcube.MR3;
import org.mrcube.views.MR3ProjectPanel;
import org.mrcube.jgraph.RDFGraph;
import org.mrcube.models.MR3Constants.GraphType;
import org.mrcube.utils.Translator;
import org.mrcube.utils.Utilities;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

/**
 * @author Takeshi Morita
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
        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_A, KeyEvent.ALT_DOWN_MASK));
    }

    public void actionPerformed(ActionEvent e) {
        RDFGraph graph = null;
        Object selectionCell = null;
        MR3ProjectPanel project = MR3.getCurrentProject();
        if (project == null) { return; }

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
