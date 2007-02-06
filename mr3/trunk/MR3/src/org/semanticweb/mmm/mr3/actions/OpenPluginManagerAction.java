/*
 * @(#)  OpenPluginManager 2004/01/24
 *
 *
 * Copyright (C) 2003 The MMM Project
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

import java.awt.event.*;
import java.lang.ref.*;

import javax.swing.*;

import org.semanticweb.mmm.mr3.*;
import org.semanticweb.mmm.mr3.ui.*;
import org.semanticweb.mmm.mr3.util.*;

/**
 * @author takeshi morita
 */
public class OpenPluginManagerAction extends MR3AbstractAction {

    private WeakReference<PluginManager> pluginManagerRef;
    private static final ImageIcon ICON = Utilities.getImageIcon(Translator.getString("Component.Tools.Plugins.Icon"));

    public OpenPluginManagerAction(MR3 mr3, String name) {
        super(mr3, name, ICON);
        pluginManagerRef = new WeakReference<PluginManager>(null);
        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_P, KeyEvent.CTRL_MASK));
    }

    private PluginManager getPluginManager() {
        PluginManager result = pluginManagerRef.get();
        if (result == null) {
            result = new PluginManager(mr3);
            pluginManagerRef = new WeakReference<PluginManager>(result);
        }
        return result;
    }

    public void actionPerformed(ActionEvent arg0) {
        PluginManager pluginManager = getPluginManager();
        pluginManager.setVisible(true);
        pluginManager.toFront();
    }
}
