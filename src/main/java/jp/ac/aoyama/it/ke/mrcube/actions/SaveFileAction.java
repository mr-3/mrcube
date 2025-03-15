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
import jp.ac.aoyama.it.ke.mrcube.views.HistoryManager;
import jp.ac.aoyama.it.ke.mrcube.models.MR3Constants.HistoryType;
import jp.ac.aoyama.it.ke.mrcube.utils.Translator;
import jp.ac.aoyama.it.ke.mrcube.utils.Utilities;

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

    public static final String SAVE_PROJECT = Translator.getString("Menu.File.Save.Text");
    public static final String SAVE_AS_PROJECT = Translator.getString("Menu.File.SaveAs.Text");

    public static final ImageIcon SAVE_PROJECT_ICON = Utilities.getSVGIcon(Translator.getString("Menu.File.Save.Icon"));
    public static final ImageIcon SAVE_AS_PROJECT_ICON = Utilities.getSVGIcon(Translator.getString("Menu.File.SaveAs.Icon"));

    public SaveFileAction(MR3 mr3, String name, ImageIcon icon) {
        super(mr3, name, icon);
        setValues(name);
        initializeJFileChooser();
    }

    private void setValues(String shortDescription) {
        putValue(SHORT_DESCRIPTION, shortDescription);
        if (shortDescription.equals(SAVE_PROJECT)) {
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_S, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
        } else if (shortDescription.equals(SAVE_AS_PROJECT)) {
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_S,
                    Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx() | InputEvent.SHIFT_DOWN_MASK));
        }
    }

    public void actionPerformed(ActionEvent e) {
        if (getName().equals(SAVE_PROJECT)) {
            File currentProjectFile = MR3.getProjectPanel().getProjectFile();
            String basePath = null;
            File newFile = new File(basePath, Translator.getString("Menu.File.New.Text"));
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
