package mr3.ui;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import mr3.jgraph.*;

/**
 * @author takeshi morita
 *
 */
public class PrefDialog extends JDialog {

	private JCheckBox isAntialiasButton;
	private JLabel baseURILabel;
	private JTextField baseURIField;
	private GraphManager gmanager;

	private JButton applyButton;
	private JButton cancelButton;

	public PrefDialog(GraphManager manager) {
		super((Frame) null, "Preference", true);
		gmanager = manager;

		isAntialiasButton = new JCheckBox("Antialias");

		baseURILabel = new JLabel("Base URI:   ");
		baseURIField = new JTextField(20);
		baseURIField.setPreferredSize(new Dimension(250, 20));
		baseURIField.setMinimumSize(new Dimension(250, 20));

		applyButton = new JButton("Apply");
		applyButton.addActionListener(new DesideAction());
		cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(new DesideAction());
		JPanel buttonGroup = new JPanel();
		buttonGroup.add(applyButton);
		buttonGroup.add(cancelButton);

		Container contentPane = getContentPane();
		GridBagLayout gridbag = new GridBagLayout();
		GridBagConstraints c = new GridBagConstraints();
		contentPane.setLayout(gridbag);
		c.anchor = GridBagConstraints.WEST;
		c.gridwidth = GridBagConstraints.REMAINDER;
		gridbag.setConstraints(isAntialiasButton, c);
		contentPane.add(isAntialiasButton);
		c.gridwidth = GridBagConstraints.RELATIVE;
		gridbag.setConstraints(baseURILabel, c);
		contentPane.add(baseURILabel);
		c.gridwidth = GridBagConstraints.REMAINDER;
		gridbag.setConstraints(baseURIField, c);
		contentPane.add(baseURIField);
		c.anchor = GridBagConstraints.CENTER;
		gridbag.setConstraints(buttonGroup, c);
		contentPane.add(buttonGroup);

		initParameter();

		setSize(new Dimension(300, 150));
		setLocation(100, 100);
		setVisible(true);
	}

	private void initParameter() {
		isAntialiasButton.setSelected(gmanager.isAntialias());
//		String baseURI = gmanager.getBaseURI();
//		baseURIField.setText(baseURI.substring(0, baseURI.length()-1));
		baseURIField.setText(gmanager.getBaseURI());
	}

	class DesideAction extends AbstractAction {
		public void actionPerformed(ActionEvent e) {
			if (e.getSource() == applyButton) {
				gmanager.setAntialias(isAntialiasButton.isSelected());
				gmanager.setBaseURI(baseURIField.getText());
			}
			setVisible(false);
		}
	}
}
