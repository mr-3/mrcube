/*
 * @(#) SourceDialog.java
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

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;

import org.semanticweb.mmm.mr3.util.*;
import org.semanticweb.mmm.mr3.util.Utilities;

/**
 * @author takeshi morita
 *
 */
public class SourceDialog extends JInternalFrame {

	private JTextArea srcArea;
	private static final int FRAME_HEIGHT = 400;
	private static final int FRAME_WIDTH = 600;

	public SourceDialog() {
		super(Translator.getString("SourceDialog.Title"), true, true, true);
		setFrameIcon(Utilities.getImageIcon(Translator.getString("SourceDialog.Icon")));
		setIconifiable(true);

		srcArea = new JTextArea();
		srcArea.setEditable(false);
		setContentPane(new JScrollPane(srcArea));

		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		addInternalFrameListener(new CloseInternalFrameAction());
		setBounds(new Rectangle(0, 0, FRAME_WIDTH, FRAME_HEIGHT));
		setVisible(false);
	}

	class CloseInternalFrameAction extends InternalFrameAdapter {
		public void internalFrameClosing(InternalFrameEvent e) {
			setVisible(false);
		}
	}

	public JTextComponent getSourceArea() {
		return srcArea;
	}

}
