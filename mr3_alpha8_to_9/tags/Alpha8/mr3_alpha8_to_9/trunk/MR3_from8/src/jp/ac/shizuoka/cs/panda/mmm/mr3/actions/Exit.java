/*
 * Created on 2003/07/19
 *
 */
package jp.ac.shizuoka.cs.panda.mmm.mr3.actions;

import java.awt.event.*;

import javax.swing.*;

import jp.ac.shizuoka.cs.panda.mmm.mr3.*;

/**
 * @author takeshi morita
 *
 */
public class Exit extends AbstractActionFile {

	public Exit(MR3 mr3) {
		super(mr3, "Exit");
		putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_Q, KeyEvent.CTRL_MASK));
	}

	public void actionPerformed(ActionEvent e) {
		exitProgram();
	}

}
