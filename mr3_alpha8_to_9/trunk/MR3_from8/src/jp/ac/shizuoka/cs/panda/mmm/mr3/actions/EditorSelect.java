/*
 * Created on 2003/07/20
 *
 */
package jp.ac.shizuoka.cs.panda.mmm.mr3.actions;

import java.awt.event.*;
import java.beans.*;

import javax.swing.*;

import jp.ac.shizuoka.cs.panda.mmm.mr3.*;
import jp.ac.shizuoka.cs.panda.mmm.mr3.util.*;

/**
 * @author takeshi morita
 *
 */
public class EditorSelect extends MR3AbstractAction {

	private static final String RDF_EDITOR = Translator.getString("Component.Window.RDFEditor.Text");
	private static final String CLASS_EDITOR =Translator.getString("Component.Window.ClassEditor.Text");
	private static final String PROPERTY_EDITOR =Translator.getString("Component.Window.PropertyEditor.Text");

	public EditorSelect(MR3 mr3, String name, ImageIcon icon) {
		super(mr3, name, icon);
		setValues();
	}

	private void setValues() {
		putValue(SHORT_DESCRIPTION, getName());
		if (getName().equals(RDF_EDITOR)) {
			putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_R, KeyEvent.ALT_MASK));
		} else if (getName().equals(CLASS_EDITOR)) {
			putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_C, KeyEvent.ALT_MASK));
		} else if (getName().equals(PROPERTY_EDITOR)) {
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
		if (getName().equals(RDF_EDITOR)) {
			toFrontInternalFrame(iFrame[0]);
		} else if (getName().equals(CLASS_EDITOR)) {
			toFrontInternalFrame(iFrame[1]);
		} else if (getName().equals(PROPERTY_EDITOR)) {
			toFrontInternalFrame(iFrame[2]);
		}
	}

}