/*
 * Project Name: MR^3 (Meta-Model Management based on RDFs Revision Reflection)
 * Project Website: http://mrcube.org/
 *
 * Copyright (C) 2003-2019 Yamaguchi Laboratory, Keio University. All rights reserved.
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
import org.mrcube.views.HistoryManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

/**
 * @author Takeshi Morita
 */
public class ShowHistoryManager extends MR3AbstractAction {

    private static ImageIcon ICON = Utilities.getImageIcon(Translator.getString("RDFSourceCodeViewer.Icon"));
    private static final String TITLE = "History Manager";

    public ShowHistoryManager(MR3 mr3) {
        super(mr3, TITLE, ICON);
        setValues();
    }

    private void setValues() {
        putValue(SHORT_DESCRIPTION, getName());
        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_H,
                Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx() | InputEvent.SHIFT_DOWN_MASK));
    }

    public void actionPerformed(ActionEvent e) {
        HistoryManager historyManager = mr3.getHistoryManager();
        if (historyManager != null) {
            mr3.getHistoryManager().setVisible(true);
        }
    }
}
