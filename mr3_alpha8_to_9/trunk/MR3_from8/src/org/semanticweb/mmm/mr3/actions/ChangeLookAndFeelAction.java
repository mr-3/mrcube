/*
 * @(#) ChangeLookAndFeelAction.java
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
 */
public class ChangeLookAndFeelAction extends MR3AbstractAction {

	public static final String METAL = Translator.getString("Component.View.LookAndFeel.Metal.Text");
	public static final String WINDOWS = Translator.getString("Component.View.LookAndFeel.Windows.Text");
	public static final String MOTIF = Translator.getString("Component.View.LookAndFeel.Motif.Text");

	public ChangeLookAndFeelAction(MR3 mr3, String title) {
		super(mr3, title);
	}

	public void actionPerformed(ActionEvent e) {
		try {
			if (getName().equals(METAL)) {
				UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");
			} else if (getName().equals(WINDOWS)) {
				UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
			} else if (getName().equals(MOTIF)) {	
				UIManager.setLookAndFeel("com.sun.java.swing.plaf.motif.MotifLookAndFeel");			
			}
			SwingUtilities.updateComponentTreeUI(mr3.getContentPane());
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

}
