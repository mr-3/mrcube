/*
 * Project Name: MR^3 (Meta-Model Management based on RDFs Revision Reflection)
 * Project Website: http://mrcube.org/
 *
 * Copyright (C) 2003-2022 Takeshi Morita. All rights reserved.
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
import org.mrcube.models.MR3Constants.GraphType;
import org.mrcube.utils.Translator;
import org.mrcube.utils.Utilities;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

/**
 * @author Takeshi Morita
 */
public class SelectEditorAction extends MR3AbstractAction {

    public static String RDF_EDITOR;
    public static String CLASS_EDITOR;
    public static String PROPERTY_EDITOR;

    public static ImageIcon RDF_EDITOR_ICON;
    public static ImageIcon CLASS_EDITOR_ICON;
    public static ImageIcon PROPERTY_EDITOR_ICON;

    public SelectEditorAction(MR3 mr3, String name, ImageIcon icon) {
        super(mr3, name, icon);
        loadResourceBundle();
        setValues();
    }

    public static void loadResourceBundle() {
        RDF_EDITOR = Translator.getString("Menu.Window.InstanceEditor.Text");
        CLASS_EDITOR = Translator.getString("Menu.Window.ClassEditor.Text");
        PROPERTY_EDITOR = Translator.getString("Menu.Window.PropertyEditor.Text");

        RDF_EDITOR_ICON = Utilities.getImageIcon(Translator.getString("InstanceEditor.Icon"));
        CLASS_EDITOR_ICON = Utilities.getImageIcon(Translator.getString("ClassEditor.Icon"));
        PROPERTY_EDITOR_ICON = Utilities.getImageIcon(Translator.getString("PropertyEditor.Icon"));
    }

    private void setValues() {
        putValue(SHORT_DESCRIPTION, getName());
        if (getName().equals(RDF_EDITOR)) {
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_R,
                    Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx() | InputEvent.SHIFT_DOWN_MASK));
        } else if (getName().equals(CLASS_EDITOR)) {
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_C,
                    Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx() | InputEvent.SHIFT_DOWN_MASK));
        } else if (getName().equals(PROPERTY_EDITOR)) {
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_P,
                    Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx() | InputEvent.SHIFT_DOWN_MASK));
        }
    }

    public void actionPerformed(ActionEvent e) {
        MR3ProjectPanel project = MR3.getProjectPanel();
        if (getName().equals(RDF_EDITOR)) {
            project.displayEditorInFront(GraphType.INSTANCE);
        } else if (getName().equals(CLASS_EDITOR)) {
            project.displayEditorInFront(GraphType.CLASS);
        } else if (getName().equals(PROPERTY_EDITOR)) {
            project.displayEditorInFront(GraphType.PROPERTY);
        }
    }
}