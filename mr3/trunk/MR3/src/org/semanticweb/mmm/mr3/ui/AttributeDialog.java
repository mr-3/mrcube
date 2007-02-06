/*
 * @(#) AttributeDialog.java
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

package org.semanticweb.mmm.mr3.ui;

import java.awt.*;

import javax.swing.*;

import org.semanticweb.mmm.mr3.util.*;

/**
 * @author takehshi morita
 *  
 */
public class AttributeDialog extends JDialog {

    private static final JPanel NULL_PANEL = new JPanel();

    private static int DIALOG_WIDTH = 440;
    private static int DIALOG_HEIGHT = 280;

    //private static final ImageIcon ICON =
    // Utilities.getImageIcon(Translator.getString("AttributeDialog.Icon"));

    public AttributeDialog(Frame frame) {
        super(frame, Translator.getString("AttributeDialog.Title"), false);
        //setFrameIcon(ICON);
        setSize(new Dimension(DIALOG_WIDTH, DIALOG_HEIGHT));
        setLocationRelativeTo(frame);
        setResizable(false);
        setVisible(false);
    }

    public void setNullPanel() {
        setContentPane(NULL_PANEL);
    }

    public void setVisible(boolean b) {
        super.setVisible(b);
        if (!b) {
            setNullPanel();
        }
    }
}
