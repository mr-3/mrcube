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

package jp.ac.aoyama.it.ke.mrcube.views;

import jp.ac.aoyama.it.ke.mrcube.editors.Editor;
import jp.ac.aoyama.it.ke.mrcube.jgraph.MR3OverviewPanel;
import jp.ac.aoyama.it.ke.mrcube.utils.Translator;
import jp.ac.aoyama.it.ke.mrcube.utils.Utilities;

import javax.swing.*;
import java.awt.*;

/**
 * @author Takeshi Morita
 */
public class OverviewDialog extends JDialog {

    private final MR3OverviewPanel overviewPanel;
    private static final int WIDTH = 300;
    private static final int HEIGHT = 300;

    public static final String INSTANCE_EDITOR_OVERVIEW = Translator.getString("InstanceEditorOverview.Title");
    public static final String CLASS_EDITOR_OVERVIEW = Translator.getString("ClassEditorOverview.Title");
    public static final String PROPERTY_EDITOR_OVERVIEW = Translator.getString("PropertyEditorOverview.Title");

    public static final ImageIcon INSTANCE_EDITOR_ICON = Utilities.getSVGIcon(Translator.getString("InstanceEditor.Icon"));
    public static final ImageIcon CLASS_EDITOR_ICON = Utilities.getSVGIcon(Translator.getString("ClassEditor.Icon"));
    public static final ImageIcon PROPERTY_EDITOR_ICON = Utilities.getSVGIcon(Translator.getString("PropertyEditor.Icon"));

    public OverviewDialog(Frame owner, String title, Editor editor) {
        super(owner, title);
        overviewPanel = new MR3OverviewPanel(editor);
        getContentPane().add(overviewPanel);
        setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
        setBounds(new Rectangle(100, 100, WIDTH, HEIGHT));
        setLocationRelativeTo(owner);
        setVisible(false);
    }

    public void setEditor(Editor editor) {
        overviewPanel.setEditor(editor);
    }

}
