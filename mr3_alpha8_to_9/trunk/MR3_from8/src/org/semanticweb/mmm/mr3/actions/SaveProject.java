/*
 * @(#) SaveProject.java
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
import java.io.*;

import javax.swing.*;

import org.semanticweb.mmm.mr3.*;
import org.semanticweb.mmm.mr3.util.*;

/**
 * @author takeshi morita
 *
 */
public class SaveProject extends AbstractActionFile {

	public static final String SAVE_PROJECT = Translator.getString("Component.File.SaveProject.Text");
	public static final String SAVE_AS_PROJECT = Translator.getString("Component.File.SaveAsProject.Text");

	public static final ImageIcon SAVE_PROJECT_ICON = Utilities.getImageIcon(Translator.getString("Component.File.SaveProject.Icon"));
	public static final ImageIcon SAVE_AS_PROJECT_ICON = Utilities.getImageIcon(Translator.getString("Component.File.SaveAsProject.Icon"));

	public SaveProject(MR3 mr3, String name) {
		super(mr3, name);
		setValues(name);
	}

	public SaveProject(MR3 mr3, String name, ImageIcon icon) {
		super(mr3, name, icon);
		setValues(name);
	}

	private void setValues(String shortDescription) {
		putValue(SHORT_DESCRIPTION, shortDescription);
		if (shortDescription.equals(SAVE_PROJECT)) {
			putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_MASK));
		} else if (shortDescription.equals(SAVE_AS_PROJECT)) {
			putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_MASK + KeyEvent.SHIFT_MASK));
		}
	}

	public void actionPerformed(ActionEvent e) {
		if (getName().equals(SAVE_PROJECT)) {
			File currentProject = mr3.getCurrentProject();
			if (currentProject == null) {
				saveProjectAs();
			} else {
				saveProject(currentProject);
			}
		} else {
			saveProjectAs();
		}
	}

}
