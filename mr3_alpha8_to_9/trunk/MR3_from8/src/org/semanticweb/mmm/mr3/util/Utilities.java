/*
 * @(#) Utilities.java
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

package org.semanticweb.mmm.mr3.util;

import java.awt.*;
import java.net.*;

import javax.swing.*;

/**
 * @author takeshi morita
 */
public class Utilities {

	private static final String RESOURCE_DIR = "org/semanticweb/mmm/mr3/resources/";

	public static void center(Window frame) {
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		Dimension frameSize = frame.getSize();
		frame.setLocation(screenSize.width / 2 - (frameSize.width / 2), screenSize.height / 2 - (frameSize.height / 2));
	}

	public static ImageIcon getImageIcon(String image) {
		return new ImageIcon(Utilities.class.getClassLoader().getResource(RESOURCE_DIR + image));
	}

	public static URL getResourceDir() {
		return Utilities.class.getClassLoader().getResource(RESOURCE_DIR);
	}
	
	public static URL getURL(String obj) {
		return Utilities.class.getClassLoader().getResource(RESOURCE_DIR + obj);
	}
		
	public static void initComponent(JComponent component, String title, int width, int height) {
		component.setPreferredSize(new Dimension(width, height));
		component.setMinimumSize(new Dimension(width, height));
		component.setBorder(BorderFactory.createTitledBorder(title));
	}
}
