/*
 * Project Name: MR^3 (Meta-Model Management based on RDFs Revision Reflection)
 * Project Website: https://mr-3.github.io/
 *
 * Copyright (C) 2003-2024 Takeshi Morita. All rights reserved.
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
import jp.ac.aoyama.it.ke.mrcube.utils.Translator;
import jp.ac.aoyama.it.ke.mrcube.utils.Utilities;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;

/**
 * @author Takeshi Morita
 */
public class OpenFileAction extends OpenResourceAction {

    public OpenFileAction(MR3 mr3) {
        super(mr3);
        putValue(Action.SMALL_ICON, Utilities.getImageIcon(Translator.getString("Menu.File.Open.Icon")));
        setValues();
    }

    @Override
    protected void setValues() {
        super.setValues();
        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_O, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
    }

    public void actionPerformed(ActionEvent e) {
        var message = confirmExitProject();
        if (message == JOptionPane.CANCEL_OPTION) {
            return;
        }
        File file = selectOpenFile();
        if (file == null) {
            return;
        }
        openResource(file.getAbsolutePath());
        MR3.getProjectPanel().setProjectFile(file);
        mr3.ResourcePathTextField.setText(file.getAbsolutePath());
    }
}
