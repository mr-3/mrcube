/*
 * Created on 2003/07/19
 *
 */
package mr3.actions;

import java.awt.*;
import java.awt.event.*;
import java.net.*;

import javax.swing.*;

import mr3.ui.*;

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
		URL logoUrl = this.getClass().getClassLoader().getResource("mr3/resources/mr3_logo.png");
		new HelpDialog(supFrame, new ImageIcon(logoUrl));
	}

}
