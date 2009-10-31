/*
 * Created on 2003/07/30
 *
 */
package mr3.actions;

import java.awt.event.*;

import javax.swing.*;

import mr3.*;

/**
 * @author takeshi morita
 */
public class ShowLogConsole extends MR3AbstractAction {

	public ShowLogConsole(MR3 mr3, String name) {
		super(mr3, name);	
	}
	
	public void actionPerformed(ActionEvent e) {
		JFrame frame = mr3.getLogConsole(); 			
		frame.setVisible(true);
	}
	
}
