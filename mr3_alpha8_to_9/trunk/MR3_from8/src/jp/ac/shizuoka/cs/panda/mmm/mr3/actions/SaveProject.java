/*
 * Created on 2003/07/19
 *
 */
package jp.ac.shizuoka.cs.panda.mmm.mr3.actions;

import java.awt.event.*;
import java.io.*;

import javax.swing.*;

import jp.ac.shizuoka.cs.panda.mmm.mr3.*;
import jp.ac.shizuoka.cs.panda.mmm.mr3.util.*;

/**
 * @author takeshi morita
 *
 */
public class SaveProject extends AbstractActionFile {

	private static final String SAVE_PROJECT = Translator.getString("Component.File.SaveProject.Text");
	private static final String SAVE_AS_PROJECT = Translator.getString("Component.File.SaveAsProject.Text");
	
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
		if (shortDescription.equals(SAVE_PROJECT)) {
			putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_MASK));
		} else if (shortDescription.equals(SAVE_AS_PROJECT)) {
			putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_MASK+KeyEvent.SHIFT_MASK));
		}
	}

	public void actionPerformed(ActionEvent e) {
		if (getName().equals(SAVE_PROJECT)) {
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
