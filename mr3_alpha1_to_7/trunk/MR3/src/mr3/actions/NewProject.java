/*
 * Created on 2003/07/19
 *
 */
package mr3.actions;

import java.awt.event.*;

import javax.swing.*;

import mr3.*;

/**
 * @author takeshi morita
 *
 */
public class NewProject extends AbstractActionFile {

	private static final String NEW_PROJECT = "New Project";

	public NewProject(MR3 mr3) {
		super(mr3, NEW_PROJECT);
	}

	public NewProject(MR3 mr3, ImageIcon icon) {
		super(mr3, NEW_PROJECT, icon);
	}

	public void actionPerformed(ActionEvent e) {
		int messageType = confirmExitProject("New Project");
		if (messageType != JOptionPane.CANCEL_OPTION) {
			newProject();
		}
	}
}
