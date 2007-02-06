/*
 * @(#) ShowOverview.java
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

import org.semanticweb.mmm.mr3.*;
import org.semanticweb.mmm.mr3.util.*;

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
        if (type.equals(RDF_EDITOR_OVERVIEW)) {
            mr3.showRDFEditorOverview();
        } else if (type.equals(CLASS_EDITOR_OVERVIEW)) {
            mr3.showClassEditorOverview();
        } else if (type.equals(PROPERTY_EDITOR_OVERVIEW)) {
            mr3.showPropertyEditorOverview();
        }
    }
}
