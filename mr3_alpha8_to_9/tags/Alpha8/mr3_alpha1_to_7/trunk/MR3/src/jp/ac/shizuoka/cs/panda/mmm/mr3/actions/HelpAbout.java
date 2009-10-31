/*
 * Created on 2003/07/19
 *
 */
package jp.ac.shizuoka.cs.panda.mmm.mr3.actions;

import java.awt.*;
import java.awt.event.*;

import jp.ac.shizuoka.cs.panda.mmm.mr3.ui.*;
import jp.ac.shizuoka.cs.panda.mmm.mr3.util.*;

/**
 * @author takeshi morita
 *
 */
public class HelpAbout extends MR3AbstractAction {
	
	Frame supFrame;
	
	public HelpAbout(Frame frame) {
		super("About MR^3");
		supFrame = frame;
	}

	public void actionPerformed(ActionEvent e) {
		new HelpWindow(supFrame, Utilities.getImageIcon("mr3_logo.png"));
	}

}
