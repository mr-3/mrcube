/*
 * @(#) NewProject.java
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

import javax.swing.*;

import org.semanticweb.mmm.mr3.*;
import org.semanticweb.mmm.mr3.util.*;

/**
 * @author takeshi morita
 *
 */
public class NewProject extends AbstractActionFile {

	private static final String TITLE = Translator.getString("Component.File.NewProject.Text");
	private static final ImageIcon ICON = Utilities.getImageIcon(Translator.getString("Component.File.NewProject.Icon")); 

	public NewProject(MR3 mr3) {
		super(mr3, TITLE, ICON);
		setValues();
	}

	private void setValues() {
		putValue(SHORT_DESCRIPTION, TITLE);
		putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_N, KeyEvent.CTRL_MASK));
//		putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_N));
	}

	public void actionPerformed(ActionEvent e) {
		int messageType = confirmExitProject("New Project");
		if (messageType != JOptionPane.CANCEL_OPTION) {
			mr3.newProject();
		}
	}
}
