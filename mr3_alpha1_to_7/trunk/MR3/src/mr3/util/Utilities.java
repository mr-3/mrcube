/*
 * Created on 2003/08/02
 *
 */
package mr3.util;

import java.awt.*;

/**
 * @author takeshi morita
 */
public class Utilities {

	public static void center(Window frame) {
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		Dimension frameSize = frame.getSize();
		frame.setLocation(screenSize.width / 2 - (frameSize.width / 2), screenSize.height / 2 - (frameSize.height / 2));
	}
	
}
