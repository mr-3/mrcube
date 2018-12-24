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
import org.mrcube.views.PluginManager;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.lang.ref.WeakReference;

/**
 * @author Takeshi Morita
 */
public class OpenPluginManagerAction extends MR3AbstractAction {

    private WeakReference<PluginManager> pluginManagerRef;
    private static final ImageIcon ICON = Utilities.getImageIcon(Translator.getString("Component.Tools.Plugins.Icon"));

    public OpenPluginManagerAction(MR3 mr3, String name) {
        super(mr3, name, ICON);
        pluginManagerRef = new WeakReference<PluginManager>(null);
        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_P, KeyEvent.CTRL_DOWN_MASK));
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
