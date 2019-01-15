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
public class ShowOptionDialog extends MR3AbstractAction {

    public ShowOptionDialog(MR3 mr3) {
        super(mr3, Translator.getString("Component.Tools.Options.Text"), Utilities.getImageIcon("cog.png"));
        putValue(SHORT_DESCRIPTION, getName());
        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_O,
                Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx() | InputEvent.SHIFT_DOWN_MASK));
    }

    public void actionPerformed(ActionEvent e) {
        mr3.showOptionDialog();
    }
}
