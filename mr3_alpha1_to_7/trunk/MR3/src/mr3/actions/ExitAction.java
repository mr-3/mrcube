/*
 * Created on 2003/07/19
 *
 */
package mr3.actions;

import java.awt.event.*;

import mr3.*;

/**
 * @author takeshi morita
 *
 */
public class ExitAction extends AbstractActionFile {

	public ExitAction(MR3 mr3) {
		super(mr3, "Exit");
	}

	public void actionPerformed(ActionEvent e) {
		exitProgram();
	}

}
