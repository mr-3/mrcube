/*
 * Created on 2003/06/11
 *
 */
package mr3.ui;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

/**
 * @author takeshi morita
 */
public class InsertResourceDialog extends JDialog implements ActionListener, ItemListener {

	private boolean isConfirm;
	private JComboBox resTypeBox;
	private JTextField uri;
	private JButton confirm;
	private JButton cancel;
	private Object resourceType;

	public InsertResourceDialog(String title, Object[] cells) {
		super((Frame) null, title, true);
		Container contentPane = getContentPane();

		resourceType = null;

		resTypeBox = new JComboBox(new DefaultComboBoxModel(cells));
		resTypeBox.setBorder(BorderFactory.createTitledBorder("Resource Type"));
		resTypeBox.setPreferredSize(new Dimension(350, 50));
		resTypeBox.addItemListener(this);
		uri = new JTextField(30);
		uri.setBorder(BorderFactory.createTitledBorder("URI"));
		uri.setPreferredSize(new Dimension(350, 40));
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
		gridbag.setConstraints(resTypeBox, c);
		panel.add(resTypeBox);
		gridbag.setConstraints(uri, c);
		panel.add(uri);
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

	public Object getResourceType() {
		return resourceType;
	}

	public String getURI() {
		return uri.getText();
	}

	public void itemStateChanged(ItemEvent e) {
		resourceType = resTypeBox.getSelectedItem();
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
