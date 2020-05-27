/*
 * Project Name: MR^3 (Meta-Model Management based on RDFs Revision Reflection)
 * Project Website: http://mrcube.org/
 *
 * Copyright (C) 2003-2020 Takeshi Morita. All rights reserved.
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

import javax.swing.*;
import java.util.prefs.Preferences;

/**
 * @author Takeshi Morita
 */
abstract class MR3AbstractAction extends AbstractAction {

    MR3 mr3;

    MR3AbstractAction() {
    }

    public MR3AbstractAction(MR3 mr3) {
        this.mr3 = mr3;
    }

    MR3AbstractAction(String name) {
        super(name);
    }

    MR3AbstractAction(String name, ImageIcon icon) {
        super(name, icon);
    }

    MR3AbstractAction(MR3 mr3, String name) {
        super(name);
        this.mr3 = mr3;
    }

    MR3AbstractAction(MR3 mr3, String name, ImageIcon icon) {
        super(name, icon);
        this.mr3 = mr3;
    }

    String getName() {
        return (String) getValue(NAME);
    }

    protected Preferences getUserPrefs() {
        return mr3.getUserPrefs();
    }
}
