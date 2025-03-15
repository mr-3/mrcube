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

import jp.ac.aoyama.it.ke.mrcube.utils.Translator;
import jp.ac.aoyama.it.ke.mrcube.utils.Utilities;

import javax.swing.*;
import java.awt.*;

/**
 * @author takehshi morita
 * 
 */
public class AttributeDialog extends JDialog {

	private static final JPanel NULL_PANEL = new JPanel();

	private static final ImageIcon ICON = Utilities.getImageIcon(Translator
			.getString("AttributeDialog.Icon"));

	public AttributeDialog(Frame frame) {
		super(frame, Translator.getString("AttributeDialog.Title"), false);
		setIconImage(ICON.getImage());
		int DIALOG_WIDTH = 550;
		int DIALOG_HEIGHT = 300;
		setSize(new Dimension(DIALOG_WIDTH, DIALOG_HEIGHT));
		setMinimumSize(new Dimension(DIALOG_WIDTH, DIALOG_HEIGHT));
		setLocationRelativeTo(frame);
		setResizable(true);
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
