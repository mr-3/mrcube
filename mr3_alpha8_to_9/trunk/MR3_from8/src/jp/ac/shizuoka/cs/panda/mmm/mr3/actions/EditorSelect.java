/*
 * Created on 2003/07/20
 *
 */
package jp.ac.shizuoka.cs.panda.mmm.mr3.actions;

import java.awt.event.*;
import java.beans.*;

import javax.swing.*;

import jp.ac.shizuoka.cs.panda.mmm.mr3.*;

/**
 * @author takeshi morita
 *
 */
public class EditorSelect extends MR3AbstractAction {

	public EditorSelect(MR3 mr3, String name, ImageIcon icon) {
		super(mr3, name, icon);
		setValues();
	}

	private static final String TO_FRONT_RDF_EDITOR = "To Front RDF Editor";
	private static final String TO_FRONT_CLASS_EDITOR = "To Front Class Editor";
	private static final String TO_FRONT_PROPERTY_EDITOR = "To Front Property Editor";

	private void setValues() {
		putValue(SHORT_DESCRIPTION, getName());
		if (getName().equals(TO_FRONT_RDF_EDITOR)) {
			putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_R, KeyEvent.ALT_MASK));
		} else if (getName().equals(TO_FRONT_CLASS_EDITOR)) {
			putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_C, KeyEvent.ALT_MASK));
		} else if (getName().equals(TO_FRONT_PROPERTY_EDITOR)) {
			putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_P, KeyEvent.ALT_MASK));
		}
	}

	private void toFrontInternalFrame(JInternalFrame iFrame) {
		try {
			iFrame.toFront();
			iFrame.setIcon(false);
			iFrame.setSelected(true);
		} catch (PropertyVetoException pve) {
			pve.printStackTrace();
		}
	}

	public void actionPerformed(ActionEvent e) {
		JInternalFrame[] iFrame = mr3.getInternalFrames();
		if (getName().equals(TO_FRONT_RDF_EDITOR)) {
			toFrontInternalFrame(iFrame[0]);
		} else if (getName().equals(TO_FRONT_CLASS_EDITOR)) {
			toFrontInternalFrame(iFrame[1]);
		} else if (getName().equals(TO_FRONT_PROPERTY_EDITOR)) {
			toFrontInternalFrame(iFrame[2]);
		}
	}

}