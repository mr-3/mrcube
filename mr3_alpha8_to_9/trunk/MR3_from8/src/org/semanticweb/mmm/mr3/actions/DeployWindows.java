/*
 * @(#) DeployAction.java
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

import java.awt.*;
import java.awt.event.*;
import java.beans.*;

import javax.swing.*;

import org.semanticweb.mmm.mr3.*;
import org.semanticweb.mmm.mr3.util.*;

/**
 * @author takeshi morita
 *
 */
public class DeployWindows extends MR3AbstractAction {

	public DeployWindows(MR3 mr3) {
		super(mr3, Translator.getString("Component.Window.DeployWindows.Text"));
	}

	public void actionPerformed(ActionEvent e) {
		Component desktop = mr3.getDesktopPane();
		JInternalFrame[] internalFrames = mr3.getInternalFrames();
		
		try {
			int width = desktop.getWidth();
			int height = desktop.getHeight();
			internalFrames[0].setBounds(new Rectangle(0, height / 2, width, height / 2)); // RDF
			internalFrames[0].setIcon(false);
			internalFrames[1].setBounds(new Rectangle(0, 0, width / 2, height / 2)); // Class
			internalFrames[1].setIcon(false);
			internalFrames[2].setBounds(new Rectangle(width / 2, 0, width / 2, height / 2)); //Property
			internalFrames[2].setIcon(false);
		} catch (PropertyVetoException e1) {
			e1.printStackTrace();
		}
	}

}
