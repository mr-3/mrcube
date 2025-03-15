/*
 * Project Name: MR^3 (Meta-Model Management based on RDFs Revision Reflection)
 * Project Website: https://mr-3.github.io/
 *
 * Copyright (C) 2003-2025 Takeshi Morita. All rights reserved.
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

package jp.ac.aoyama.it.ke.mrcube.actions;

import jp.ac.aoyama.it.ke.mrcube.MR3;
import jp.ac.aoyama.it.ke.mrcube.views.MR3ProjectPanel;
import jp.ac.aoyama.it.ke.mrcube.jgraph.RDFGraph;
import jp.ac.aoyama.it.ke.mrcube.models.MR3Constants.GraphType;
import jp.ac.aoyama.it.ke.mrcube.utils.Translator;
import jp.ac.aoyama.it.ke.mrcube.utils.Utilities;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

/**
 * @author Takeshi Morita
 */
public class ShowAttrDialog extends MR3AbstractAction {

    private static final String TITLE = Translator.getString("Menu.Window.AttrDialog.Text");
    private static final ImageIcon ICON = Utilities.getSVGIcon(Translator.getString("AttributeDialog.Icon"));

    public ShowAttrDialog(MR3 mr3) {
        super(mr3, TITLE, ICON);
        setValues();
    }

    private void setValues() {
        putValue(SHORT_DESCRIPTION, getName());
        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_A,
                Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx() | InputEvent.SHIFT_DOWN_MASK));
    }

    public void actionPerformed(ActionEvent e) {
        RDFGraph graph = null;
        Object selectionCell = null;
        MR3ProjectPanel project = MR3.getProjectPanel();
        if (project == null) {
            return;
        }

        if (project.getFocusedEditorType() == GraphType.Instance) {
            graph = mr3.getRDFGraph();
            selectionCell = graph.getSelectionCell();
        } else if (project.getFocusedEditorType() == GraphType.Class) {
            graph = mr3.getClassGraph();
            selectionCell = graph.getSelectionCell();
        } else if (project.getFocusedEditorType() == GraphType.Property) {
            graph = mr3.getPropertyGraph();
            selectionCell = graph.getSelectionCell();
        }
        mr3.getGraphManager().getAttrDialog().setVisible(true);

        if (graph != null && selectionCell != null) {
            graph.setSelectionCell(selectionCell);
        }
    }
}
