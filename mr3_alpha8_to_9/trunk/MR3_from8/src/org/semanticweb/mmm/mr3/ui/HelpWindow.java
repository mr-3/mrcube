/*
 * @(#) HelpWindow.java
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
import java.awt.event.*;

import javax.swing.*;

import org.semanticweb.mmm.mr3.data.*;
import org.semanticweb.mmm.mr3.util.*;

/**
 * @author takeshi morita
 */
public class HelpWindow extends JWindow {

	private static final String TOOL_NAME = "<h1>MR<sup>3</sup></h1> (Meta-Model Management based on <br> RDFs Revision Reflection)";
	private static final String MR3_URL = "http://mmm.semanticweb.org/mr3/";
	private static final String VERSION = "É¿1 (2003-12-01)";
	private static final Color DESKTOP_BACK_COLOR = new Color(245, 245, 245);

	public HelpWindow(Frame root, ImageIcon logo) {
		super(root);
		JLabel logoLabel = new JLabel("<html>" + TOOL_NAME + "</html>", logo, SwingConstants.LEFT);
		logoLabel.setFont(logoLabel.getFont().deriveFont(Font.PLAIN, 16));
		JLabel brLabel = new JLabel("<html><br></html>");
		JLabel urlLabel = new JLabel("   HP: " + MR3_URL);
		urlLabel.setFont(urlLabel.getFont().deriveFont(Font.PLAIN, 14));
		JLabel versionLabel = new JLabel("   Version: " + VERSION);
		versionLabel.setFont(versionLabel.getFont().deriveFont(Font.PLAIN, 14));

		JPanel messagePanel = new JPanel();
		messagePanel.setLayout(new BoxLayout(messagePanel, BoxLayout.Y_AXIS));
		messagePanel.add(brLabel);
		messagePanel.add(urlLabel);
		messagePanel.add(versionLabel);

		Container contentPane = getContentPane();
		contentPane.setLayout(new BorderLayout());
		contentPane.add(logoLabel, BorderLayout.NORTH);
		contentPane.add(messagePanel, BorderLayout.CENTER);
		if (root != null) {
			JButton confirmButton = new JButton(MR3Constants.OK);
			confirmButton.addActionListener(new AbstractAction() {
				public void actionPerformed(ActionEvent e) {
					dispose();
				}
			});
			contentPane.add(confirmButton, BorderLayout.SOUTH);
		}
		contentPane.setBackground(DESKTOP_BACK_COLOR);
		messagePanel.setBackground(DESKTOP_BACK_COLOR);

		setBounds(300, 300, 360, 220);
		Utilities.center(this);
		setVisible(true);
	}
}
