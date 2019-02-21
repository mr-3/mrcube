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

import org.mrcube.utils.Translator;
import org.mrcube.utils.Utilities;
import org.mrcube.views.VersionInfoDialog;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.lang.ref.WeakReference;

/**
 * @author Takeshi Morita
 * 
 */
public class ShowVersionInfoAction extends MR3AbstractAction {

    private final Frame rootFrame;
    private WeakReference<VersionInfoDialog> versionInfoDialogRef;
    private static final String TITLE = Translator.getString("Menu.Help.About.Text");
    private static final ImageIcon ICON = Utilities.getImageIcon(Translator.getString("Menu.Help.About.Icon"));

    public ShowVersionInfoAction(Frame frame) {
        super(TITLE, ICON);
        rootFrame = frame;
        versionInfoDialogRef = new WeakReference<>(null);
        setValues();
    }

    private void setValues() {
        putValue(SHORT_DESCRIPTION, TITLE);
        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke("F1"));
    }

    private VersionInfoDialog getVersionInfoDialog() {
        VersionInfoDialog result = versionInfoDialogRef.get();
        if (result == null) {
            result = new VersionInfoDialog(rootFrame, TITLE, ICON);
            versionInfoDialogRef = new WeakReference<>(result);
        }
        return result;
    }

    public void actionPerformed(ActionEvent e) {
        getVersionInfoDialog().setVisible(true);
    }
}
