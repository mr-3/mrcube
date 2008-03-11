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
import java.io.*;

import javax.swing.*;

import org.semanticweb.mmm.mr3.*;
import org.semanticweb.mmm.mr3.data.MR3Constants.*;
import org.semanticweb.mmm.mr3.ui.*;
import org.semanticweb.mmm.mr3.util.*;

/**
 * @author takeshi morita
 * 
 */
public class SaveProject extends AbstractActionFile {

    public static final String SAVE_PROJECT = Translator.getString("Component.File.SaveProject.Text");
    public static final String SAVE_AS_PROJECT = Translator.getString("Component.File.SaveAsProject.Text");

    public static final ImageIcon SAVE_PROJECT_ICON = Utilities.getImageIcon(Translator
            .getString("Component.File.SaveProject.Icon"));
    public static final ImageIcon SAVE_AS_PROJECT_ICON = Utilities.getImageIcon(Translator
            .getString("Component.File.SaveAsProject.Icon"));

    public SaveProject(MR3 mr3, String name) {
        super(mr3, name);
        setValues(name);
    }

    public SaveProject(MR3 mr3, String name, ImageIcon icon) {
        super(mr3, name, icon);
        setValues(name);
    }

    private void setValues(String shortDescription) {
        putValue(SHORT_DESCRIPTION, shortDescription);
        if (shortDescription.equals(SAVE_PROJECT)) {
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_MASK));
        } else if (shortDescription.equals(SAVE_AS_PROJECT)) {
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_MASK + KeyEvent.SHIFT_MASK));
        }
    }

    public void actionPerformed(ActionEvent e) {
        if (getName().equals(SAVE_PROJECT)) {
            File currentProject = MR3.getCurrentProject().getCurrentProjectFile();
            if (currentProject == null) {
                saveProjectAs();
            } else {
                saveProject(currentProject);
                HistoryManager.saveHistory(HistoryType.SAVE_PROJECT, currentProject.getAbsolutePath());
            }
        } else {
            saveProjectAs();
        }
    }
}
