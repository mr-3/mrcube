/*
 * Created on 2003/06/11
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
public class InsertRDFResDialog extends JDialog implements ActionListener, ItemListener {

	private boolean isConfirm;
	private JComboBox resTypeBox;
	private JTextField uriField;
	private JButton confirm;
	private JButton cancel;
	private Object resourceType;

	private JRadioButton uriButton;
	private JRadioButton anonymousButton;
	private JRadioButton idButton;

	private URIType uriType;

	public InsertRDFResDialog(String title, Object[] cells) {
		super((Frame) null, title, true);
		Container contentPane = getContentPane();

		resourceType = null;

		uriButton = new JRadioButton("URI");
		idButton = new JRadioButton("ID");
		anonymousButton = new JRadioButton("ANONYMOUS");
		RadioAction ra = new RadioAction();
		uriButton.addActionListener(ra);
		idButton.addActionListener(ra);
		anonymousButton.addActionListener(ra);
		idButton.setSelected(true);
		uriType = URIType.ID;
		ButtonGroup group = new ButtonGroup();
		group.add(uriButton);
		group.add(idButton);
		group.add(anonymousButton);
		JPanel uriTypeGroupPanel = new JPanel();
		uriTypeGroupPanel.setBorder(BorderFactory.createTitledBorder("URI Type"));
		uriTypeGroupPanel.setPreferredSize(new Dimension(350, 55));
		uriTypeGroupPanel.add(uriButton);
		uriTypeGroupPanel.add(idButton);
		uriTypeGroupPanel.add(anonymousButton);

		resTypeBox = new JComboBox(new DefaultComboBoxModel(cells));
		resTypeBox.setBorder(BorderFactory.createTitledBorder("Resource Type"));
		resTypeBox.setPreferredSize(new Dimension(350, 50));
		resTypeBox.addItemListener(this);
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
		c.anchor = GridBagConstraints.PAGE_START;
		c.gridwidth = GridBagConstraints.REMAINDER;
		gridbag.setConstraints(uriTypeGroupPanel, c);
		panel.add(uriTypeGroupPanel);
		gridbag.setConstraints(resTypeBox, c);
		panel.add(resTypeBox);
		gridbag.setConstraints(uriField, c);
		panel.add(uriField);
		gridbag.setConstraints(buttonPanel, c);
		panel.add(buttonPanel);
		contentPane.add(panel);

		setLocation(300, 300);
		setSize(new Dimension(400, 220));
		setResizable(false);
		setVisible(true);
	}

	public boolean isConfirm() {
		return isConfirm;
	}

	public Object getResourceType() {
		return resourceType;
	}

	public URIType getURIType() {
		return uriType;
	}

	public String getURI() {
		return uriField.getText();
	}

	public void itemStateChanged(ItemEvent e) {
		resourceType = resTypeBox.getSelectedItem();
	}

	class RadioAction implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			String type = (String) e.getActionCommand();
			uriType = URIType.getURIType(type);

			if (uriType == URIType.ANONYMOUS) {
				setURIField("", false);
			} else if (uriType == URIType.ID) {
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
