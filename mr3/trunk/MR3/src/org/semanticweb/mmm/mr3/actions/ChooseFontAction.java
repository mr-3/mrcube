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

import java.awt.*;
import java.awt.event.*;
import java.lang.ref.*;

import org.jgraph.graph.*;
import org.semanticweb.mmm.mr3.*;
import org.semanticweb.mmm.mr3.jgraph.*;
import org.semanticweb.mmm.mr3.util.*;

import say.swing.*;

/**
 * @author takeshi morita
 */
public class ChooseFontAction extends MR3AbstractAction {

    private WeakReference<JFontChooser> jfontChooserRef;

    public ChooseFontAction(MR3 mr3, String name) {
        super(mr3, name);
        jfontChooserRef = new WeakReference<JFontChooser>(null);
    }

    private JFontChooser getJFontChooser() {
        JFontChooser result = jfontChooserRef.get();
        if (result == null) {
            result = new JFontChooser();
            jfontChooserRef = new WeakReference<JFontChooser>(result);
        }
        return result;
    }

    public void actionPerformed(ActionEvent arg0) {
        JFontChooser jfontChooser = getJFontChooser();
        jfontChooser.setSelectedFont(mr3.getFont());
        int result = jfontChooser.showDialog(mr3.getGraphManager().getRootFrame());
        if (result == JFontChooser.OK_OPTION) {
            // System.out.println(jfontChooser.getSelectedFont());
            Font font = jfontChooser.getSelectedFont();
            GraphUtilities.defaultFont = font;
            mr3.setFont(font);
            setGraphFont(mr3.getRDFGraph(), font);
            setGraphFont(mr3.getClassGraph(), font);
            setGraphFont(mr3.getPropertyGraph(), font);
            if (mr3.getExportDialog() != null) {
                mr3.getExportDialog().setFont(font);
            }
        }
    }

    private void setGraphFont(RDFGraph graph, Font font) {
        Object[] cells = graph.getAllCells();
        for (int i = 0; i < cells.length; i++) {
            if (cells[i] instanceof GraphCell) {
                GraphCell cell = (GraphCell) cells[i];
                AttributeMap map = cell.getAttributes();
                GraphConstants.setFont(map, font);
                GraphUtilities.editCell(cell, map, graph);
            }
        }
    }
}
