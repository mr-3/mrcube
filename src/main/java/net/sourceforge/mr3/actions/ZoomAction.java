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

package net.sourceforge.mr3.actions;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;

import javax.swing.*;

import net.sourceforge.mr3.editor.*;
import net.sourceforge.mr3.jgraph.*;
import net.sourceforge.mr3.util.*;

/**
 * @author Takeshi Morita
 */
public class ZoomAction extends AbstractAction {

    private String title;
    private RDFGraph graph;
    private Editor editor;

    public static final String ZOOM_STD = Translator.getString("Action.ZoomStd.Text");
    public static final String ZOOM_IN = Translator.getString("Action.ZoomIn.Text");
    public static final String ZOOM_OUT = Translator.getString("Action.ZoomOut.Text");
    public static final String ZOOM_SUITABLE = Translator.getString("Action.ZoomSuitable.Text");

    public static final ImageIcon ZOOM_STD_ICON = Utilities.getImageIcon(Translator.getString("Action.ZoomStd.Icon"));
    public static final ImageIcon ZOOM_IN_ICON = Utilities.getImageIcon(Translator.getString("Action.ZoomIn.Icon"));
    public static final ImageIcon ZOOM_OUT_ICON = Utilities.getImageIcon(Translator.getString("Action.ZoomOut.Icon"));
    public static final ImageIcon ZOOM_SUITABLE_ICON = Utilities.getImageIcon(Translator
            .getString("Action.ZoomSuitable.Icon"));

    public ZoomAction(RDFGraph g, Editor editor, String title, Icon icon) {
        super(title, icon);
        this.title = title;
        graph = g;
        this.editor = editor;
        putValue(SHORT_DESCRIPTION, title);
    }

    public void actionPerformed(ActionEvent e) {
        if (title.equals(ZOOM_STD)) {
            graph.setScale(1.0);
        } else if (title.equals(ZOOM_IN)) {
            graph.setScale(1.05 * graph.getScale());
        } else if (title.equals(ZOOM_OUT)) {
            graph.setScale(graph.getScale() / 1.05);
        } else if (title.equals(ZOOM_SUITABLE)) {
            fitWindow();
        }
    }

    // private void setValues() {
    // putValue(SHORT_DESCRIPTION, title);
    // putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_N,
    // KeyEvent.CTRL_MASK));
    // putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_N));
    // }

    public void fitWindow() {
        Rectangle2D p = graph.getCellBounds(graph.getRoots());
        if (p != null) {
            Dimension s = editor.getJScrollPane().getViewport().getExtentSize();
            double scale = 1;
            if (s.width == 0 && s.height == 0) {
                graph.setScale(scale);
                return;
            }
            if (Math.abs(s.getWidth() - (p.getX() + p.getWidth())) > Math.abs(s.getHeight()
                    - (p.getX() + p.getHeight()))) scale = s.getWidth() / (p.getX() + p.getWidth());
            else scale = s.getHeight() / (p.getY() + p.getHeight());
            scale = Math.max(Math.min(scale, 16), .01);
            graph.setScale(scale);
        }
    }

}