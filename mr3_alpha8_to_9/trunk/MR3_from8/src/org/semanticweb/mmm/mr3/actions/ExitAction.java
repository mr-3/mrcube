/*
 * Created on 2003/07/19
 *
 */
package org.semanticweb.mmm.mr3.actions;

import java.awt.event.*;

import javax.swing.*;

import org.semanticweb.mmm.mr3.*;
import org.semanticweb.mmm.mr3.util.*;

/**
 * @author takeshi morita
 *
 */
public class ExitAction extends AbstractActionFile {

	private static final String TITLE = Translator.getString("Component.File.Exit.Text");
	private static final ImageIcon ICON = Utilities.getImageIcon(Translator.getString("Component.File.Exit.Icon"));
		
	public ExitAction(MR3 mr3) {
		super(mr3, TITLE, ICON);
		putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_Q, KeyEvent.CTRL_MASK));
	}

	public void actionPerformed(ActionEvent e) {
		exitProgram();
	}

}
