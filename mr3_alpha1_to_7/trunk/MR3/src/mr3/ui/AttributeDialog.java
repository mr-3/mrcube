package mr3.ui;
import java.awt.*;
import java.awt.event.*;
import java.net.*;

import javax.swing.*;
import javax.swing.event.*;

/**
 * @author takehshi morita
 *
 */
public class AttributeDialog extends JInternalFrame implements ActionListener {
	private static final JPanel NULL_PANEL = new JPanel();
	private JCheckBoxMenuItem showPropDialog;

	public AttributeDialog() {
		super("Attribute Dialog", false, true);
		URL attrDialogUrl = this.getClass().getClassLoader().getResource("mr3/resources/attrDialogIcon.gif");
		setFrameIcon(new ImageIcon(attrDialogUrl));
		showPropDialog = new JCheckBoxMenuItem("Show Attribute Dialog", true);
		showPropDialog.addActionListener(this);
		addInternalFrameListener(new InternalFrameAdapter() {
			public void internalFrameClosing(InternalFrameEvent e) {
				setVisible(false);
			}
		});
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		setSize(new Dimension(400, 430));
		setLocation(50, 50);
		setVisible(false);
	}

	public void actionPerformed(ActionEvent e) {
		setVisible(showPropDialog.getState());
	}

	public void setNullPanel() {
		setContentPane(NULL_PANEL);
	}

	public void setVisible(boolean b) {
		super.setVisible(b);
		if (showPropDialog != null) {
			showPropDialog.setState(b);
		}
	}

	public JCheckBoxMenuItem getShowPropWindow() {
		return showPropDialog;
	}
}
