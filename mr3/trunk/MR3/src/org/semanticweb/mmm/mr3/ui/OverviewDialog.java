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

package org.semanticweb.mmm.mr3.ui;

import java.awt.*;

import javax.swing.*;
import javax.swing.event.*;

import org.semanticweb.mmm.mr3.jgraph.*;
import org.semanticweb.mmm.mr3.util.*;

import org.jgraph.*;

/**
 * @author takeshi morita
 */
public class OverviewDialog extends JInternalFrame {

    private static final int LENGH = 200;

    public static final String RDF_EDITOR_OVERVIEW = Translator.getString("RDFEditorOverview.Title");
    public static final String CLASS_EDITOR_OVERVIEW = Translator.getString("ClassEditorOverview.Title");
    public static final String PROPERTY_EDITOR_OVERVIEW = Translator.getString("PropertyEditorOverview.Title");

    public static final ImageIcon RDF_EDITOR_ICON = Utilities.getImageIcon(Translator.getString("RDFEditor.Icon"));
    public static final ImageIcon CLASS_EDITOR_ICON = Utilities.getImageIcon(Translator.getString("ClassEditor.Icon"));
    public static final ImageIcon PROPERTY_EDITOR_ICON = Utilities.getImageIcon(Translator
            .getString("PropertyEditor.Icon"));

    public OverviewDialog(String title, JGraph graph, JViewport viewport) {
        super(title, true, true);
        JPanel panel = new MR3OverviewPanel(graph, viewport);
        getContentPane().add(panel);
        addInternalFrameListener(new InternalFrameAdapter() {
            public void internalFrameClosing(InternalFrameEvent e) {
                setVisible(false);
            }
        });
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        setBounds(new Rectangle(100, 100, LENGH, LENGH));
        setVisible(false);
    }

}
