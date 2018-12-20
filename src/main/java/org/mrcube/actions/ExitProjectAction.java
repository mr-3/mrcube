/*
 * @(#)  2008/03/07
 */

package org.mrcube.actions;

import org.mrcube.MR3;
import org.mrcube.MR3Project;
import org.mrcube.utils.Translator;

import javax.swing.*;
import java.awt.event.ActionEvent;

/**
 * @author Takeshi Morita
 */
public class ExitProjectAction extends AbstractActionFile {

	public ExitProjectAction(MR3 mr3, String title, ImageIcon icon) {
		super(mr3, title, icon);
	}

	public void actionPerformed(ActionEvent arg0) {
		MR3Project project = MR3.getCurrentProject();
		int messageType = JOptionPane.showConfirmDialog(mr3.getGraphManager().getRootFrame(),
				project.getTitle() + "\n" + Translator.getString("SaveChanges"), "MR^3 - "
						+ project.getTitle(), JOptionPane.YES_NO_CANCEL_OPTION,
				JOptionPane.INFORMATION_MESSAGE);
		if (messageType == JOptionPane.YES_OPTION) {
			exitProject(project);
			mr3.removeTab(project);
		}
		if (messageType != JOptionPane.CANCEL_OPTION) {
			mr3.removeTab(project);
		}
	}
}
