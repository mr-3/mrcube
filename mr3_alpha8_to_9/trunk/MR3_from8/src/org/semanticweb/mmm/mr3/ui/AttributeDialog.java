/*
 * @(#) AttributeDialog.java
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

import javax.swing.*;
import javax.swing.event.*;

import org.semanticweb.mmm.mr3.util.*;

/**
 * @author takehshi morita
 *
 */
public class AttributeDialog extends JInternalFrame {

	private static final JPanel NULL_PANEL = new JPanel();

	private static int DIALOG_WIDTH = 430;
	private static int DIALOG_HEIGHT = 360; // 変更すると，コメントが削除できなくなる可能性がある 

	private static final ImageIcon ICON = Utilities.getImageIcon(Translator.getString("AttributeDialog.Icon")); 
	
	public AttributeDialog() {
		super(Translator.getString("AttributeDialog.Title"), false, true);
		setFrameIcon(ICON);
		addInternalFrameListener(new InternalFrameAdapter() {
			public void internalFrameClosing(InternalFrameEvent e) {	
				setVisible(false);
			}
		});
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		setSize(new Dimension(DIALOG_WIDTH, DIALOG_HEIGHT));
		setLocation(50, 50);
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
