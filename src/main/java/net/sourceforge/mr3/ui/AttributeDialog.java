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

package net.sourceforge.mr3.ui;

import java.awt.Dimension;
import java.awt.Frame;

import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JPanel;

import net.sourceforge.mr3.util.Translator;
import net.sourceforge.mr3.util.Utilities;

/**
 * @author takehshi morita
 * 
 */
public class AttributeDialog extends JDialog {

	private static final JPanel NULL_PANEL = new JPanel();

	private static int DIALOG_WIDTH = 500;
	private static int DIALOG_HEIGHT = 300;

	private static final ImageIcon ICON = Utilities.getImageIcon(Translator
			.getString("AttributeDialog.Icon"));

	public AttributeDialog(Frame frame) {
		super(frame, Translator.getString("AttributeDialog.Title"), false);
		setIconImage(ICON.getImage());
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
