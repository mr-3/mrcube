/*
 * Created on 2003/06/21
 *
 */
package mr3.ui;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import mr3.util.*;

/**
 * @author takeshi morita
 */
public class HelpWindow extends JWindow {

	private static final String TOOL_NAME = "<h1>MR<sup>3</sup></h1> (Meta-Model Management based on <br> RDFs Revision Reflection)";
	private static final String MR3_URL = "http://panda.cs.inf.shizuoka.ac.jp/mmm/mr3/";
	private static final String VERSION = "ƒ¿8 (2003-08-)";
	private static final Color DESKTOP_BACK_COLOR = new Color(235, 235, 235);

	public HelpWindow(Frame root, ImageIcon logo) {
		super(root);
		JLabel logoLabel = new JLabel("<html>" + TOOL_NAME + "</html>", logo, SwingConstants.LEFT);
		logoLabel.setFont(logoLabel.getFont().deriveFont(Font.PLAIN, 16));
		JLabel brLabel = new JLabel("<html><br></html>");
		JLabel urlLabel = new JLabel("   HP: " + MR3_URL);
		urlLabel.setFont(urlLabel.getFont().deriveFont(Font.PLAIN, 14));
		JLabel versionLabel = new JLabel("   Version: " + VERSION);
		versionLabel.setFont(versionLabel.getFont().deriveFont(Font.PLAIN, 14));

		JPanel messagePanel = new JPanel();
		messagePanel.setLayout(new BoxLayout(messagePanel, BoxLayout.Y_AXIS));
		messagePanel.add(brLabel);
		messagePanel.add(urlLabel);
		messagePanel.add(versionLabel);

		Container contentPane = getContentPane();
		contentPane.setLayout(new BorderLayout());
		contentPane.add(logoLabel, BorderLayout.NORTH);
		contentPane.add(messagePanel, BorderLayout.CENTER);
		if (root != null) {
			JButton confirmButton = new JButton("OK");
			confirmButton.addActionListener(new AbstractAction() {
				public void actionPerformed(ActionEvent e) {
					dispose();
				}
			});
			contentPane.add(confirmButton, BorderLayout.SOUTH);
		}
		contentPane.setBackground(DESKTOP_BACK_COLOR);
		messagePanel.setBackground(DESKTOP_BACK_COLOR);

		setBounds(300, 300, 360, 220);
		Utilities.center(this);
		setVisible(true);
	}
}
