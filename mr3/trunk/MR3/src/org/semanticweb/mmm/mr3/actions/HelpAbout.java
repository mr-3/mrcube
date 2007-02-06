/*
 * @(#) HelpAbout.java
 *
 *
 * Copyright (C) 2003-2005 The MMM Project
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
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
            result = new HelpWindow(rootFrame, MR3Constants.LOGO);
            helpWindowRef = new WeakReference<HelpWindow>(result);
        }
        return result;
    }

    public void actionPerformed(ActionEvent e) {
        HelpWindow helpWindow = getHelpWindow();
        helpWindow.setVisible(true);
    }
}
