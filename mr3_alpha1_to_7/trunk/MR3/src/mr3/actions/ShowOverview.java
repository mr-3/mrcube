/*
 * Created on 2003/08/02
 *
 */
package mr3.actions;

import java.awt.event.*;

import javax.swing.*;

import mr3.*;

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
