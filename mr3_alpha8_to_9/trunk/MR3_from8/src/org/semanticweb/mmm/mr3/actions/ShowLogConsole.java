/*
 * Created on 2003/07/30
 *
 */
package org.semanticweb.mmm.mr3.actions;

import java.awt.event.*;

import javax.swing.*;

import org.semanticweb.mmm.mr3.*;
import org.semanticweb.mmm.mr3.util.*;

/**
 * @author takeshi morita
 */
public class ShowLogConsole extends MR3AbstractAction {

	private static final String TITLE = Translator.getString("Component.Window.LogConsole.Text");

	public ShowLogConsole(MR3 mr3) {
		super(mr3, TITLE);	
	}
	
	public void actionPerformed(ActionEvent e) {
		JFrame frame = mr3.getLogConsole(); 			
		frame.setVisible(true);
	}
	
}
