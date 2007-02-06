/*
 * @(#) HelpWindow.java
 * 
 * Copyright (C) 2003 The MMM Project
 * 
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation; either version 2.1 of the License, or (at your
 * option) any later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *  
 */

package org.semanticweb.mmm.mr3.ui;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import org.semanticweb.mmm.mr3.util.*;

/**
 * @author takeshi morita
 */
public class HelpWindow extends JWindow {

    private static final int FONT_SIZE = 14;
    private static final String TOOL_NAME = "MR<sup>3</sup> (Meta-Model Management <br> based on RDFs Revision Reflection)";
    private static final String VERSION = "   Version: 1.0 RC5 (2005-12-29)";
    private static final String MR3_URL = "   http://mmm.semanticweb.org/mr3/";
    private static final String COPY_RIGHT = "   Copyright (C) 2003-2005 MMM Project";
    private static final Color HELP_BACK_COLOR = Color.WHITE;
    private static final int WINDOW_WIDTH = 350;
    private static final int WINDOW_HEIGHT = 180;

    public HelpWindow(Frame root, ImageIcon logo) {
        super(root);
        JLabel logoLabel = new JLabel("<html>" + TOOL_NAME + "<br></html>", logo, SwingConstants.LEFT);
        logoLabel.setFont(logoLabel.getFont().deriveFont(Font.PLAIN, 14));
        JLabel urlLabel = new JLabel(MR3_URL);
        urlLabel.setFont(urlLabel.getFont().deriveFont(Font.PLAIN, FONT_SIZE));
        JLabel versionLabel = new JLabel(VERSION);
        versionLabel.setFont(versionLabel.getFont().deriveFont(Font.PLAIN, FONT_SIZE));
        JLabel copyrightLabel = new JLabel(COPY_RIGHT);
        copyrightLabel.setFont(versionLabel.getFont().deriveFont(Font.PLAIN, FONT_SIZE));

        JPanel messagePanel = new JPanel();
        messagePanel.setBackground(HELP_BACK_COLOR);
        messagePanel.setLayout(new BoxLayout(messagePanel, BoxLayout.Y_AXIS));
        messagePanel.add(versionLabel);
        messagePanel.add(copyrightLabel);
        messagePanel.add(urlLabel);

        JPanel helpPanel = new JPanel();
        helpPanel.setBackground(HELP_BACK_COLOR);
        helpPanel.setLayout(new BorderLayout());
        helpPanel.add(logoLabel, BorderLayout.NORTH);
        helpPanel.add(messagePanel, BorderLayout.CENTER);
        helpPanel.setBorder(BorderFactory.createEtchedBorder());

        Container contentPane = getContentPane();
        contentPane.setLayout(new BorderLayout());
        contentPane.add(helpPanel, BorderLayout.CENTER);

        addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                dispose();
            }
        });
        setSize(new Dimension(WINDOW_WIDTH, WINDOW_HEIGHT));
        Utilities.center(this);
        setVisible(true);
    }
}
