/*
 * Created on 2003/09/27
 *
 */
package jp.ac.shizuoka.cs.panda.mmm.mr3.actions;

import java.awt.event.*;

import javax.swing.*;

import jp.ac.shizuoka.cs.panda.mmm.mr3.*;
import jp.ac.shizuoka.cs.panda.mmm.mr3.util.*;

/**
 * @author takeshi morita
 */
public class ShowSrcDialog extends MR3AbstractAction {

	private static final String TITLE = Translator.getString("Component.Window.SrcDialog.Text");
	private static final ImageIcon ICON = Utilities.getImageIcon("source_window.gif");

	public ShowSrcDialog(MR3 mr3) {
		super(mr3, TITLE, ICON);
		setValues();
	}

	private void setValues() {
		putValue(SHORT_DESCRIPTION, getName());
		putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.ALT_MASK));
	}

	public void actionPerformed(ActionEvent e) {
		mr3.getSrcDialog().setVisible(true);
	}
}
