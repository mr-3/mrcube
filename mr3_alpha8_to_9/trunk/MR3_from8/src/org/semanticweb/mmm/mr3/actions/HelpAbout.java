/*
 * Created on 2003-07-19
 *
 */
package org.semanticweb.mmm.mr3.actions;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import org.semanticweb.mmm.mr3.data.*;
import org.semanticweb.mmm.mr3.ui.*;
import org.semanticweb.mmm.mr3.util.*;

/**
 * @author takeshi morita
 *
 */
public class HelpAbout extends MR3AbstractAction {

	private Frame supFrame;
	private static final String TITLE = Translator.getString("Component.Help.About.Text");
	
	public HelpAbout(Frame frame) {
		super(TITLE);
		supFrame = frame;
//		setValues();
	}

	private void setValues() {
		putValue(SHORT_DESCRIPTION, TITLE);
		putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0));
	}

	public void actionPerformed(ActionEvent e) {
		new HelpWindow(supFrame, MR3Constants.LOGO);
	}

}
