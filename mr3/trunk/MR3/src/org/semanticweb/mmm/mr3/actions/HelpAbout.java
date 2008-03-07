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

import java.awt.*;
import java.awt.event.*;
import java.lang.ref.*;

import javax.swing.*;

import org.semanticweb.mmm.mr3.data.*;
import org.semanticweb.mmm.mr3.ui.*;
import org.semanticweb.mmm.mr3.util.*;

/**
 * @author takeshi morita
 * 
 */
public class HelpAbout extends MR3AbstractAction {

    private Frame rootFrame;
    private WeakReference<HelpWindow> helpWindowRef;
    private static final String TITLE = Translator.getString("Component.Help.About.Text");
    private static final ImageIcon ICON = Utilities.getImageIcon(Translator.getString("Component.Help.About.Icon"));

    public HelpAbout(Frame frame) {
        super(TITLE, ICON);
        rootFrame = frame;
        helpWindowRef = new WeakReference<HelpWindow>(null);
        setValues();
    }

    private void setValues() {
        putValue(SHORT_DESCRIPTION, TITLE);
        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke("F1"));
    }

    public HelpWindow getHelpWindow() {
        HelpWindow result = helpWindowRef.get();
        if (result == null) {
            result = new HelpWindow(rootFrame, MR3Constants.SPLASH_LOGO);
            helpWindowRef = new WeakReference<HelpWindow>(result);
        }
        return result;
    }

    public void actionPerformed(ActionEvent e) {
        HelpWindow helpWindow = getHelpWindow();
        helpWindow.setVisible(true);
    }
}
