/*
 * Created on 2003/06/21
 *
 */
package mr3.ui;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

/**
 * @author takeshi morita
 */
public class HelpDialog extends JDialog {

	private static final String TOOL_NAME = "<h1>MR<sup>3</sup></h1> (Meta-Model Management based on <br> RDFs Revision Reflection)";
	private static final String MR3_URL = "http://panda.cs.inf.shizuoka.ac.jp/mmm/mr3/";
	private static final String VERSION = "ƒ¿7 (2003-07-31)";
	private static final Color DESKTOP_BACK_COLOR = new Color(225, 225, 225);
	
	public HelpDialog(Frame root, ImageIcon logo) {
		super(root, "About MR^3", true);	
		JLabel logoLabel = new JLabel("<html>"+TOOL_NAME+"</html>",logo, SwingConstants.LEFT);
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

		JButton confirmButton = new JButton("OK");
		confirmButton.addActionListener(new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				setVisible(false);
			}
		});

		Container contentPane = getContentPane();
		contentPane.setLayout(new BorderLayout());
		contentPane.add(logoLabel, BorderLayout.NORTH);
		contentPane.add(messagePanel, BorderLayout.CENTER);
		contentPane.add(confirmButton, BorderLayout.SOUTH);		
		contentPane.setBackground(DESKTOP_BACK_COLOR);
		messagePanel.setBackground(DESKTOP_BACK_COLOR);

		setBounds(300, 300, 400, 250);
		setResizable(false);
		setVisible(true);
	}
}
