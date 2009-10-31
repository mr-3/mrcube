/*
 * Created on 2003/08/02
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
public class ShowOverview extends MR3AbstractAction {

	private JInternalFrame overviewDialog;
	public static final String RDF_EDITOR_OVERVIEW = Translator.getString("Component.Window.RDFEditorOverview.Text");
	public static final String CLASS_EDITOR_OVERVIEW = Translator.getString("Component.Window.ClassEditorOverview.Text");
	public static final String PROPERTY_EDITOR_OVERVIEW = Translator.getString("Component.Window.PropertyEditorOverview.Text");

	public ShowOverview(MR3 mr3, JInternalFrame frame, String name) {
		super(mr3, name);
		this.overviewDialog = frame;
	}

	public void actionPerformed(ActionEvent e) {
		overviewDialog.setVisible(true);
	}
}
