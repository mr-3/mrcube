/*
 * @(#) ClassTreeCellRenderer.java
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
import javax.swing.tree.*;

import org.semanticweb.mmm.mr3.util.*;

/*
 * 
 * @author takeshi morita
 *
 */
public class ClassTreeCellRenderer extends JLabel implements TreeCellRenderer {

	public ClassTreeCellRenderer() {
		setOpaque(true);
	}

	public Component getTreeCellRendererComponent(
		JTree tree,
		Object value,
		boolean selected,
		boolean expanded,
		boolean leaf,
		int row,
		boolean hasFocus) {

		setText(value.toString());

		if (selected) {
			setBackground(new Color(0, 0, 128));
			setForeground(Color.white);
		} else {
			setBackground(Color.white);
			setForeground(Color.black);
		}
		
		setIcon(Utilities.getImageIcon("classIcon.gif"));
//		if (leaf) {
//			setIcon(null);
//			//if (selected){
//			//    setIcon(new ImageIcon("./img/open.gif"));
//			//}else{
//			//    setIcon(null);
//			//}
//		} else {
//			if (expanded) {
//				setIcon(new ImageIcon(MRCUBE.getImageIcon("open.gif")));
//			} else {
//				setIcon(new ImageIcon(MRCUBE.getImageIcon("close.gif")));
//			}
//		}

		return this;
	}
}
