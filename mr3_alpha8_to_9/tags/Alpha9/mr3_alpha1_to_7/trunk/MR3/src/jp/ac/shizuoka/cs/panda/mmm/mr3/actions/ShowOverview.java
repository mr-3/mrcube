/*
 * Created on 2003/08/02
 *
 */
package jp.ac.shizuoka.cs.panda.mmm.mr3.actions;

import java.awt.event.*;

import javax.swing.*;

import jp.ac.shizuoka.cs.panda.mmm.mr3.*;

/**
 * @author takeshi morita
 */
public class ShowOverview extends MR3AbstractAction {

	private JInternalFrame overviewDialog;

	public ShowOverview(MR3 mr3, JInternalFrame frame, String name) {
		super(mr3, name);
		this.overviewDialog = frame;
	}

	public void actionPerformed(ActionEvent e) {
		overviewDialog.setVisible(true);
	}
}
