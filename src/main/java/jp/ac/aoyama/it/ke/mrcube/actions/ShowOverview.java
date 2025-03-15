/*
 * Project Name: MR^3 (Meta-Model Management based on RDFs Revision Reflection)
 * Project Website: https://mr-3.github.io/
 *
 * Copyright (C) 2003-2025 Takeshi Morita. All rights reserved.
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
import java.awt.event.ActionEvent;

/**
 * @author Takeshi Morita
 */
public class ShowOverview extends MR3AbstractAction {

    private final String type;
    public static final String INSTANCE_EDITOR_OVERVIEW = Translator.getString("Menu.Window.InstanceEditorOverview.Text");
    public static final String CLASS_EDITOR_OVERVIEW = Translator.getString("Menu.Window.ClassEditorOverview.Text");
    public static final String PROPERTY_EDITOR_OVERVIEW = Translator.getString("Menu.Window.PropertyEditorOverview.Text");

    public static final ImageIcon INSTANCE_EDITOR_OVERVIEW_ICON = Utilities.getSVGIcon(Translator.getString("InstanceEditorOverview.Icon"));
    public static final ImageIcon CLASS_EDITOR_OVERVIEW_ICON = Utilities.getSVGIcon(Translator.getString("ClassEditorOverview.Icon"));
    public static final ImageIcon PROPERTY_EDITOR_OVERVIEW_ICON = Utilities.getSVGIcon(Translator.getString("PropertyEditorOverview.Icon"));

    public ShowOverview(MR3 mr3, String name, ImageIcon icon) {
        super(mr3, name, icon);
        type = name;
    }

    public void actionPerformed(ActionEvent e) {
        if (type.equals(INSTANCE_EDITOR_OVERVIEW)) {
            mr3.showInstanceEditorOverview();
        } else if (type.equals(CLASS_EDITOR_OVERVIEW)) {
            mr3.showClassEditorOverview();
        } else if (type.equals(PROPERTY_EDITOR_OVERVIEW)) {
            mr3.showPropertyEditorOverview();
        }
    }
}
