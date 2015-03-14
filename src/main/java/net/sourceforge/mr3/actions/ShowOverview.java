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

import java.awt.event.*;

import net.sourceforge.mr3.*;
import net.sourceforge.mr3.util.*;

/**
 * @author takeshi morita
 */
public class ShowOverview extends MR3AbstractAction {

    String type;
    public static final String RDF_EDITOR_OVERVIEW = Translator.getString("Component.Window.RDFEditorOverview.Text");
    public static final String CLASS_EDITOR_OVERVIEW = Translator
            .getString("Component.Window.ClassEditorOverview.Text");
    public static final String PROPERTY_EDITOR_OVERVIEW = Translator
            .getString("Component.Window.PropertyEditorOverview.Text");

    public ShowOverview(MR3 mr3, String name) {
        super(mr3, name);
        type = name;
    }

    public void actionPerformed(ActionEvent e) {
        if (MR3.getCurrentProject() == null) { return; }
        if (type.equals(RDF_EDITOR_OVERVIEW)) {
            mr3.showRDFEditorOverview();
        } else if (type.equals(CLASS_EDITOR_OVERVIEW)) {
            mr3.showClassEditorOverview();
        } else if (type.equals(PROPERTY_EDITOR_OVERVIEW)) {
            mr3.showPropertyEditorOverview();
        }
    }
}
