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
public class NewProject extends AbstractActionFile {

	private static final String NEW_PROJECT = "New Project";

	public NewProject(MR3 mr3) {
		super(mr3, NEW_PROJECT);
		setValues();
	}

	public NewProject(MR3 mr3, ImageIcon icon) {
		super(mr3, NEW_PROJECT, icon);
		setValues();
	}

	private void setValues() {
		putValue(SHORT_DESCRIPTION, "New Project");
		putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_N, KeyEvent.CTRL_MASK));
//		putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_N));
	}

	public void actionPerformed(ActionEvent e) {
		int messageType = confirmExitProject("New Project");
		if (messageType != JOptionPane.CANCEL_OPTION) {
			mr3.newProject();
		}
	}
}
