/*
 * @(#)  2008/03/07
 */

package net.sourceforge.mr3.actions;

import java.awt.event.ActionEvent;

import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

import net.sourceforge.mr3.MR3;
import net.sourceforge.mr3.MR3Project;
import net.sourceforge.mr3.util.Translator;

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
