/*
 * Created on 2003/06/22
 *
 */
package mr3.ui;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import mr3.data.*;

/**
 * @author takeshi morita
 */
public class InsertRDFSResDialog extends JDialog implements ActionListener {
	
	private boolean isConfirm;
	private JTextField uriField;
	private URIType uriType;
	
	private JButton confirm;
	private JButton cancel;

	private JRadioButton uriButton;
	private JRadioButton idButton;

	public InsertRDFSResDialog(String title) {
		super((Frame) null, title, true);
		Container contentPane = getContentPane();

		uriButton = new JRadioButton("URI");
		idButton = new JRadioButton("ID");
		RadioAction ra = new RadioAction();
		uriButton.addActionListener(ra);
		idButton.addActionListener(ra);
		idButton.setSelected(true);
		uriType = URIType.ID;
		ButtonGroup group = new ButtonGroup();
		group.add(uriButton);
		group.add(idButton);
		JPanel uriTypeGroupPanel = new JPanel();
		uriTypeGroupPanel.setBorder(BorderFactory.createTitledBorder("URI Type"));
		uriTypeGroupPanel.setPreferredSize(new Dimension(130, 55));
		uriTypeGroupPanel.setMinimumSize(new Dimension(130, 55));
		uriTypeGroupPanel.add(uriButton);
		uriTypeGroupPanel.add(idButton);

		uriField = new JTextField(30);
		uriField.setText("#");
		uriField.setBorder(BorderFactory.createTitledBorder("URI"));
		uriField.setPreferredSize(new Dimension(350, 40));
		confirm = new JButton("OK");
		confirm.addActionListener(this);
		cancel = new JButton("Cancel");
		cancel.addActionListener(this);
		JPanel buttonPanel = new JPanel();
		buttonPanel.add(confirm);
		buttonPanel.add(cancel);

		JPanel panel = new JPanel();
		GridBagLayout gridbag = new GridBagLayout();
		GridBagConstraints c = new GridBagConstraints();
		panel.setLayout(gridbag);
		c.weighty = 5;
		c.anchor = GridBagConstraints.WEST;
		c.gridwidth = GridBagConstraints.REMAINDER;
		gridbag.setConstraints(uriTypeGroupPanel, c);
		panel.add(uriTypeGroupPanel);
		c.anchor = GridBagConstraints.CENTER;
		gridbag.setConstraints(uriField, c);
		panel.add(uriField);
		gridbag.setConstraints(buttonPanel, c);
		panel.add(buttonPanel);
		contentPane.add(panel);

		setLocation(300, 300);
		setSize(new Dimension(400, 180));
		setResizable(false);
		setVisible(true);
	}

	public boolean isConfirm() {
		return isConfirm;
	}

	public URIType getURIType() {
		return uriType;
	}

	public String getURI() {
		return uriField.getText();
	}

	class RadioAction implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			String type = (String) e.getActionCommand();
			uriType = URIType.getURIType(type);

			if (uriType == URIType.ID) {
				uriField.setEditable(true);
				if (uriField.getText().length() == 0 || uriField.getText().charAt(0) != '#') {
					uriField.setText('#' + uriField.getText());
				}
			} else if (uriType == URIType.URI) {
				uriField.setEditable(true);
			}
		}
	}

	private void setURIField(String str, boolean t) {
		uriField.setText(str);
		uriField.setToolTipText(str);
		uriField.setEditable(t);
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == confirm) {
			isConfirm = true;
		} else {
			isConfirm = false;
		}
		setVisible(false);
	}
}
