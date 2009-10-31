/*
 * Created on 2003/08/02
 *
 */
package jp.ac.shizuoka.cs.panda.mmm.mr3.util;

import java.awt.*;

import javax.swing.*;

/**
 * @author takeshi morita
 */
public class Utilities {

	public static void center(Window frame) {
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		Dimension frameSize = frame.getSize();
		frame.setLocation(screenSize.width / 2 - (frameSize.width / 2), screenSize.height / 2 - (frameSize.height / 2));
	}

	public static ImageIcon getImageIcon(String image) {
		String resourceDir = "jp/ac/shizuoka/cs/panda/mmm/mr3/resources/";
		return new ImageIcon(Utilities.class.getClassLoader().getResource(resourceDir + image));
	}

}
