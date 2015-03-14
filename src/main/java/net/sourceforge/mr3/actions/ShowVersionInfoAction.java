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

package net.sourceforge.mr3.actions;

import java.awt.*;
import java.awt.event.*;
import java.lang.ref.*;

import javax.swing.*;

import net.sourceforge.mr3.ui.*;
import net.sourceforge.mr3.util.*;

/**
 * @author takeshi morita
 * 
 */
public class ShowVersionInfoAction extends MR3AbstractAction {

    private Frame rootFrame;
    private WeakReference<VersionInfoDialog> versionInfoDialogRef;
    private static final String TITLE = Translator.getString("Component.Help.About.Text");
    private static final ImageIcon ICON = Utilities.getImageIcon(Translator.getString("Component.Help.About.Icon"));

    public ShowVersionInfoAction(Frame frame) {
        super(TITLE, ICON);
        rootFrame = frame;
        versionInfoDialogRef = new WeakReference<VersionInfoDialog>(null);
        setValues();
    }

    private void setValues() {
        putValue(SHORT_DESCRIPTION, TITLE);
        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke("F1"));
    }

    public VersionInfoDialog getVersionInfoDialog() {
        VersionInfoDialog result = versionInfoDialogRef.get();
        if (result == null) {
            result = new VersionInfoDialog(rootFrame, TITLE, ICON);
            versionInfoDialogRef = new WeakReference<VersionInfoDialog>(result);
        }
        return result;
    }

    public void actionPerformed(ActionEvent e) {
        getVersionInfoDialog().setVisible(true);
    }
}
