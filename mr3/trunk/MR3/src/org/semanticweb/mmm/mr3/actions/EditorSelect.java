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
import org.semanticweb.mmm.mr3.util.*;

/**
 * @author takeshi morita
 * 
 */
public class EditorSelect extends MR3AbstractAction {

    public static String RDF_EDITOR;
    public static String CLASS_EDITOR;
    public static String PROPERTY_EDITOR;

    public static ImageIcon RDF_EDITOR_ICON;
    public static ImageIcon CLASS_EDITOR_ICON;
    public static ImageIcon PROPERTY_EDITOR_ICON;

    public EditorSelect(MR3 mr3, String name, ImageIcon icon) {
        super(mr3, name, icon);
        loadResourceBundle();
        setValues();
    }

    public static void loadResourceBundle() {
        RDF_EDITOR = Translator.getString("Component.Window.RDFEditor.Text");
        CLASS_EDITOR = Translator.getString("Component.Window.ClassEditor.Text");
        PROPERTY_EDITOR = Translator.getString("Component.Window.PropertyEditor.Text");

        RDF_EDITOR_ICON = Utilities.getImageIcon(Translator.getString("RDFEditor.Icon"));
        CLASS_EDITOR_ICON = Utilities.getImageIcon(Translator.getString("ClassEditor.Icon"));
        PROPERTY_EDITOR_ICON = Utilities.getImageIcon(Translator.getString("PropertyEditor.Icon"));
    }

    private void setValues() {
        putValue(SHORT_DESCRIPTION, getName());
        if (getName().equals(RDF_EDITOR)) {
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_R, KeyEvent.ALT_MASK));
        } else if (getName().equals(CLASS_EDITOR)) {
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_C, KeyEvent.ALT_MASK));
        } else if (getName().equals(PROPERTY_EDITOR)) {
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_P, KeyEvent.ALT_MASK));
        }
    }

    public void actionPerformed(ActionEvent e) {
        MR3Project project = MR3.getCurrentProject();
        if (project == null) { return; }
        if (getName().equals(RDF_EDITOR)) {
            project.frontEditor(GraphType.RDF);
        } else if (getName().equals(CLASS_EDITOR)) {
            project.frontEditor(GraphType.CLASS);
        } else if (getName().equals(PROPERTY_EDITOR)) {
            project.frontEditor(GraphType.PROPERTY);
        }
    }
}