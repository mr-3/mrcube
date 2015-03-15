/*
 * Project Name: MR^3 (Meta-Model Management based on RDFs Revision Reflection)
 * Project Website: http://mr3.sourceforge.net/
 * 
 * Copyright (C) 2003-2015 Yamaguchi Laboratory, Keio University. All rights reserved. 
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

package net.sourceforge.mr3.ui;

import java.awt.*;

import javax.swing.*;

import net.sourceforge.mr3.editor.*;
import net.sourceforge.mr3.jgraph.*;
import net.sourceforge.mr3.util.*;

/**
 * @author Takeshi Morita
 */
public class OverviewDialog extends JDialog {

    private MR3OverviewPanel overviewPanel;
    private static final int LENGH = 200;

    public static final String RDF_EDITOR_OVERVIEW = Translator.getString("RDFEditorOverview.Title");
    public static final String CLASS_EDITOR_OVERVIEW = Translator.getString("ClassEditorOverview.Title");
    public static final String PROPERTY_EDITOR_OVERVIEW = Translator.getString("PropertyEditorOverview.Title");

    public static final ImageIcon RDF_EDITOR_ICON = Utilities.getImageIcon(Translator.getString("RDFEditor.Icon"));
    public static final ImageIcon CLASS_EDITOR_ICON = Utilities.getImageIcon(Translator.getString("ClassEditor.Icon"));
    public static final ImageIcon PROPERTY_EDITOR_ICON = Utilities.getImageIcon(Translator
            .getString("PropertyEditor.Icon"));

    public OverviewDialog(Frame owner, String title, Editor editor) {
        super(owner, title);
        overviewPanel = new MR3OverviewPanel(editor);
        getContentPane().add(overviewPanel);
        setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
        setBounds(new Rectangle(100, 100, LENGH, LENGH));
        setLocationRelativeTo(owner);
        setVisible(false);
    }

    public void setEditor(Editor editor) {
        overviewPanel.setEditor(editor);
    }

}
