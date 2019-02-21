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

import javax.swing.*;
import java.awt.event.ActionEvent;

/**
 * @author Takeshi Morita
 */
public class ShowOverview extends MR3AbstractAction {

    private final String type;
    public static final String RDF_EDITOR_OVERVIEW = Translator.getString("Menu.Window.RDFEditorOverview.Text");
    public static final String CLASS_EDITOR_OVERVIEW = Translator.getString("Menu.Window.ClassEditorOverview.Text");
    public static final String PROPERTY_EDITOR_OVERVIEW = Translator.getString("Menu.Window.PropertyEditorOverview.Text");

    public static final ImageIcon RDF_EDITOR_OVERVIEW_ICON = Utilities.getImageIcon(Translator.getString("RDFEditorOverview.Icon"));
    public static final ImageIcon CLASS_EDITOR_OVERVIEW_ICON = Utilities.getImageIcon(Translator.getString("ClassEditorOverview.Icon"));
    public static final ImageIcon PROPERTY_EDITOR_OVERVIEW_ICON = Utilities.getImageIcon(Translator.getString("PropertyEditorOverview.Icon"));

    public ShowOverview(MR3 mr3, String name, ImageIcon icon) {
        super(mr3, name, icon);
        type = name;
    }

    public void actionPerformed(ActionEvent e) {
        if (type.equals(RDF_EDITOR_OVERVIEW)) {
            mr3.showRDFEditorOverview();
        } else if (type.equals(CLASS_EDITOR_OVERVIEW)) {
            mr3.showClassEditorOverview();
        } else if (type.equals(PROPERTY_EDITOR_OVERVIEW)) {
            mr3.showPropertyEditorOverview();
        }
    }
}
