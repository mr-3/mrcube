/*
 * Created on 2003/07/20
 *
 */
package mr3.ui;

import java.awt.*;
import java.awt.event.*;
import java.net.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;

/**
 * @author takeshi morita
 *
 */
public class SourceFrame extends JInternalFrame {

	private JTextArea srcArea;
	private JCheckBoxMenuItem showSrcWindowBox;
	private static final int FRAME_HEIGHT = 400;
	private static final int FRAME_WIDTH = 600;

	public SourceFrame(String title) {
		super(title, true, true, true);
		URL srcAreaUrl = this.getClass().getClassLoader().getResource("mr3/resources/source_window.gif");
		setFrameIcon(new ImageIcon(srcAreaUrl));
		setIconifiable(true);

		srcArea = new JTextArea();
		srcArea.setEditable(false);
		setContentPane(new JScrollPane(srcArea));

		showSrcWindowBox = new JCheckBoxMenuItem("Show Source Window", false);
		showSrcWindowBox.addActionListener(new ShowViewAction());

		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		addInternalFrameListener(new CloseInternalFrameAction());
		setBounds(new Rectangle(0, 0, FRAME_WIDTH, FRAME_HEIGHT));
		setVisible(false);
	}

	class CloseInternalFrameAction extends InternalFrameAdapter {
		public void internalFrameClosing(InternalFrameEvent e) {
			showSrcWindowBox.setSelected(false);
			setVisible(false);
		}
	}

	class ShowViewAction extends AbstractAction {
		public void actionPerformed(ActionEvent e) {
			setVisible(showSrcWindowBox.getState());
			toFront();
		}
	}

	public JCheckBoxMenuItem getShowSrcWindowBox() {
		return showSrcWindowBox;
	}
	
	public JTextComponent getSourceArea() {
		return srcArea;
	}

}
