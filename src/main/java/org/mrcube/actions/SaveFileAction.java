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
import org.mrcube.models.MR3Constants.HistoryType;
import org.mrcube.utils.Translator;
import org.mrcube.utils.Utilities;
import org.mrcube.views.HistoryManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.File;

/**
 * @author Takeshi Morita
 */
public class SaveFileAction extends AbstractActionFile {

    public static final String SAVE_PROJECT = Translator.getString("Component.File.SaveProject.Text");
    public static final String SAVE_AS_PROJECT = Translator.getString("Component.File.SaveAsProject.Text");

    public static final ImageIcon SAVE_PROJECT_ICON = Utilities.getImageIcon(Translator.getString("Component.File.SaveProject.Icon"));
    public static final ImageIcon SAVE_AS_PROJECT_ICON = Utilities.getImageIcon(Translator.getString("Component.File.SaveAsProject.Icon"));

    public SaveFileAction(MR3 mr3, String name, ImageIcon icon) {
        super(mr3, name, icon);
        setValues(name);
        initializeJFileChooser();
    }

    private void setValues(String shortDescription) {
        putValue(SHORT_DESCRIPTION, shortDescription);
        if (shortDescription.equals(SAVE_PROJECT)) {
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_S,
                    Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
        } else if (shortDescription.equals(SAVE_AS_PROJECT)) {
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_S,
                    Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx() | InputEvent.SHIFT_DOWN_MASK));
        }
    }

    public void actionPerformed(ActionEvent e) {
        if (getName().equals(SAVE_PROJECT)) {
            File currentProjectFile = MR3.getCurrentProject().getCurrentProjectFile();
            String basePath = null;
            File newFile = new File(basePath, Translator.getString("Component.File.NewProject.Text"));
            if (newFile.getAbsolutePath().equals(currentProjectFile.getAbsolutePath())) {
                saveFileAs();
            } else {
                saveFile(currentProjectFile);
                HistoryManager.saveHistory(HistoryType.SAVE_PROJECT, currentProjectFile.getAbsolutePath());
            }
        } else {
            saveFileAs();
        }
    }
}
