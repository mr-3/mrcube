/*
 * Created on 2003/04/04
 *
 */
package jp.ac.shizuoka.cs.panda.mmm.mr3.ui;

import java.awt.*;

import javax.swing.*;
import javax.swing.tree.*;

import jp.ac.shizuoka.cs.panda.mmm.mr3.util.*;

/**
 * @author takeshi morita
 */
public class PropertyTreeCellRenderer extends JLabel implements TreeCellRenderer {

	public PropertyTreeCellRenderer() {
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
		
		setIcon(Utilities.getImageIcon("propertyIcon.gif"));
		
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
