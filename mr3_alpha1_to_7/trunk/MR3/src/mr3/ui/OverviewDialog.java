/*
 * Created on 2003/08/02
 *
 */
package mr3.ui;

import java.awt.*;

import javax.swing.*;
import javax.swing.event.*;

import mr3.jgraph.*;

import com.jgraph.*;

/**
 * @author takeshi morita
 */
public class OverviewDialog extends JInternalFrame {

	private static final int LENGH = 150;

	public OverviewDialog(String title, JGraph graph, JViewport viewport) {
		super(title, true, true);
		JPanel panel = new MR3OverviewPanel(graph, viewport);
		getContentPane().add(panel);
		addInternalFrameListener(new InternalFrameAdapter() {
			public void internalFrameClosing(InternalFrameEvent e) {
				setVisible(false);
			}
		});
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		setBounds(new Rectangle(100, 100, LENGH, LENGH));
		setVisible(false);
	}

}
