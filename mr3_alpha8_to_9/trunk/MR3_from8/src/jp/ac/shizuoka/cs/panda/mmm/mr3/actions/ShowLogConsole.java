/*
 * Created on 2003/07/30
 *
 */
package jp.ac.shizuoka.cs.panda.mmm.mr3.actions;

import java.awt.event.*;

import javax.swing.*;

import jp.ac.shizuoka.cs.panda.mmm.mr3.*;

/**
 * @author takeshi morita
 */
public class ShowLogConsole extends MR3AbstractAction {

	private static final String TITLE = "Show Log Console";

	public ShowLogConsole(MR3 mr3) {
		super(mr3, TITLE);	
	}
	
	public void actionPerformed(ActionEvent e) {
		JFrame frame = mr3.getLogConsole(); 			
		frame.setVisible(true);
	}
	
}
