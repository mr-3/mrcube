/*
 * Created on 2003/06/21
 *
 */
package mr3.ui;

import java.awt.*;

import javax.swing.*;

/**
 * @author takeshi morita
 */
public class HelpDialog extends JDialog {

	private static final String TOOL_NAME = "<h2>MR<sup>3</sup> <br> (Meta-Model Management based on <br> RDFs Revision Reflection)</h2>";
	private static final String VERSION = "ƒ¿5 (2003-06-23)";

	public HelpDialog(ImageIcon logo) {
		super((Frame) null, "About MR^3", true);
		JPanel messagePanel = new JPanel();
		JLabel logoLabel = new JLabel(logo);
		logoLabel.setFont(logoLabel.getFont().deriveFont(Font.PLAIN, 24));
		JLabel toolNameLabel = new JLabel("<html>" + TOOL_NAME + "</html>");
		toolNameLabel.setFont(toolNameLabel.getFont().deriveFont(Font.PLAIN, 24));
		JLabel versionLabel = new JLabel("Version " + VERSION);
		versionLabel.setFont(versionLabel.getFont().deriveFont(Font.PLAIN, 16));

		messagePanel.setLayout(new BoxLayout(messagePanel, BoxLayout.Y_AXIS));
		messagePanel.add(toolNameLabel);
		messagePanel.add(versionLabel);

		Container contentPane = getContentPane();
		contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.X_AXIS));
		contentPane.add(logoLabel);
		contentPane.add(messagePanel);
		setBounds(300, 300, 450, 200);
		setResizable(false);
		setVisible(true);
	}

}
