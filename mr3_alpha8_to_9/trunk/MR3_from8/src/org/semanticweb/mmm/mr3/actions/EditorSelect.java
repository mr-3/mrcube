/*
 * @(#) EditorSelect.java
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
import java.beans.*;

import javax.swing.*;

import org.semanticweb.mmm.mr3.*;
import org.semanticweb.mmm.mr3.util.*;

/**
 * @author takeshi morita
 *
 */
public class EditorSelect extends MR3AbstractAction {

	public static final String RDF_EDITOR = Translator.getString("Component.Window.RDFEditor.Text");
	public static final String CLASS_EDITOR =Translator.getString("Component.Window.ClassEditor.Text");
	public static final String PROPERTY_EDITOR =Translator.getString("Component.Window.PropertyEditor.Text");

	public static final ImageIcon RDF_EDITOR_ICON = Utilities.getImageIcon(Translator.getString("RDFEditor.Icon"));
	public static final ImageIcon CLASS_EDITOR_ICON = Utilities.getImageIcon(Translator.getString("ClassEditor.Icon"));
	public static final ImageIcon PROPERTY_EDITOR_ICON = Utilities.getImageIcon(Translator.getString("PropertyEditor.Icon"));

	public EditorSelect(MR3 mr3, String name, ImageIcon icon) {
		super(mr3, name, icon);
		setValues();
	}

	private void setValues() {
		putValue(SHORT_DESCRIPTION, getName());
		if (getName().equals(RDF_EDITOR)) {
			putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_R, KeyEvent.ALT_MASK));
		} else if (getName().equals(CLASS_EDITOR)) {
			putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_C, KeyEvent.ALT_MASK));
		} else if (getName().equals(PROPERTY_EDITOR)) {
			putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_P, KeyEvent.ALT_MASK));
		}
	}

	private void toFrontInternalFrame(JInternalFrame iFrame) {
		try {
			iFrame.toFront();
			iFrame.setIcon(false);
			iFrame.setSelected(true);
		} catch (PropertyVetoException pve) {
			pve.printStackTrace();
		}
	}

	public void actionPerformed(ActionEvent e) {
		JInternalFrame[] iFrame = mr3.getInternalFrames();
		if (getName().equals(RDF_EDITOR)) {
			toFrontInternalFrame(iFrame[0]);
		} else if (getName().equals(CLASS_EDITOR)) {
			toFrontInternalFrame(iFrame[1]);
		} else if (getName().equals(PROPERTY_EDITOR)) {
			toFrontInternalFrame(iFrame[2]);
		}
	}

}