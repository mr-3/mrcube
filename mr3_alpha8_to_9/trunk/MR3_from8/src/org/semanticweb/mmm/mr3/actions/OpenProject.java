/*
 * @(#) OpenProject.java
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
import org.semanticweb.mmm.mr3.jgraph.*;
import org.semanticweb.mmm.mr3.util.*;

import com.hp.hpl.jena.rdf.model.*;

/**
 * @author takeshi morita
 *
 */
public class OpenProject extends AbstractActionFile {

	private static final String TITLE = Translator.getString("Component.File.OpenProject.Text");
	private static final ImageIcon ICON = Utilities.getImageIcon(Translator.getString("Component.File.OpenProject.Icon"));

	public OpenProject(MR3 mr3) {
		super(mr3, TITLE, ICON);
		setValues();
	}

	private void setValues() {
		putValue(SHORT_DESCRIPTION, TITLE);
		putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_O, KeyEvent.CTRL_MASK));
	}

	public void actionPerformed(ActionEvent e) {
		GraphManager gmanager = mr3.getGraphManager();
		gmanager.setIsImporting(true);
		Model model = readModel(getReader("mr3", "UTF8"), gmanager.getBaseURI(), "RDF/XML");

		if (model != null) {
			File tmp = mr3.getCurrentProject(); // New Project‚æ‚è‚à‘O‚Ì‚ð•Û‘¶
			mr3.replaceProjectModel(model);
			mr3.setCurrentProject(tmp);
			mr3.setTitle("MR^3 - " + mr3.getCurrentProject().getAbsolutePath());
		}
		gmanager.clearSelection();
		gmanager.setIsImporting(false);
	}

}
