/*
 * @(#)  2008/03/07
 */

package org.semanticweb.mmm.mr3.actions;

import java.awt.event.*;

import javax.swing.*;

import org.semanticweb.mmm.mr3.*;

/**
 * @author takeshi morita
 */
public class ExitProjectAction extends AbstractActionFile {

    public ExitProjectAction(MR3 mr3, String title, ImageIcon icon) {
        super(mr3, title, icon);
    }

    @Override
    public void actionPerformed(ActionEvent arg0) {
        MR3Project project = MR3.getCurrentProject();
        int messageType = confirmExitProject(null, project.getTitle());
        if (messageType != JOptionPane.CANCEL_OPTION) {
            mr3.removeTab(project);
        }
    }
}
