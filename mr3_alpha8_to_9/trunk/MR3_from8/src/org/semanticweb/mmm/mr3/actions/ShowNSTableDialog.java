/*
 * Created on 2003/09/27
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
public class ShowNSTableDialog extends MR3AbstractAction {

	private static final String TITLE = Translator.getString("Component.Window.NSTable.Text");
	private static final ImageIcon ICON = Utilities.getImageIcon("nameSpaceTableIcon.gif");

	public ShowNSTableDialog(MR3 mr3) {
		super(mr3, TITLE, ICON);
		setValues();
	}

	private void setValues() {
		putValue(SHORT_DESCRIPTION, getName());
		putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_N, KeyEvent.ALT_MASK));
	}

	public void actionPerformed(ActionEvent e) {
		mr3.getNSTableDialog().setVisible(true);
	}
}
