/*
 * Created on 2003/08/02
 *
 */
package jp.ac.shizuoka.cs.panda.mmm.mr3.util;

import java.awt.*;
import java.net.*;

import javax.swing.*;

/**
 * @author takeshi morita
 */
public class Utilities {

	private static final String RESOURCE_DIR = "jp/ac/shizuoka/cs/panda/mmm/mr3/resources/";

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
