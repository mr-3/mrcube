/*
 * Created on 2003/07/19
 *
 */
package mr3.actions;

import java.awt.event.*;
import java.io.*;

import javax.swing.*;

import mr3.*;

/**
 * @author takeshi morita
 *
 */
public class SaveProject extends AbstractActionFile {

	public SaveProject(MR3 mr3, String name) {
		super(mr3, name);
		setValues(name);
	}
	
	public SaveProject(MR3 mr3, String name, ImageIcon icon) {
		super(mr3, name, icon);
		setValues(name); 
	}
	
	private void setValues(String shortDescription) {
			putValue(SHORT_DESCRIPTION, shortDescription);
//			putValue(MNEMONIC_KEY, Event.CTRL_MASK);
//			putValue(ACTION_COMMAND_KEY, KeyEvent.VK_N);
		}	

	public void actionPerformed(ActionEvent e) {
		if (getName().equals("Save Project")) {
			File currentProject = mr3.getCurrentProject();
			if (currentProject == null) {
				saveProjectAs();
			} else {
				saveProject(currentProject);
			}
		} else {
			saveProjectAs();
		}
	}

}
