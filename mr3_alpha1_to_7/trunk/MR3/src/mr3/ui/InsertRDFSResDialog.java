/*
 * Created on 2003/06/22
 *
 */
package mr3.ui;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;

import mr3.data.*;
import mr3.util.*;

/**
 * @author takeshi morita
 */
public class InsertRDFSResDialog extends JDialog implements ActionListener {

	private boolean isConfirm;
	private JTextField uriField;
	private Set prefixNSInfoSet;
	private JComboBox uriPrefixBox;
	private URIType uriType;

	private JButton confirm;
	private JButton cancel;

	private JRadioButton uriButton;
	private JRadioButton idButton;

	private static final int boxWidth = 70;
	private static final int boxHeight = 30;

	public InsertRDFSResDialog(String title, Set pnis) {
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

		uriField = new JTextField(27);
		uriField.setText("#");
		uriField.setBorder(BorderFactory.createTitledBorder("URI"));
		uriField.setPreferredSize(new Dimension(300, 40));
		uriPrefixBox = new JComboBox();
		uriPrefixBox.addActionListener(new ChangePrefixAction());
		uriPrefixBox.setPreferredSize(new Dimension(boxWidth, boxHeight));
		uriPrefixBox.setMinimumSize(new Dimension(boxWidth, boxHeight));
		uriPrefixBox.setEnabled(false);
		prefixNSInfoSet = pnis;
		PrefixNSUtil.setPrefixNSInfoSet(pnis);
		uriPrefixBox.setModel(new DefaultComboBoxModel(PrefixNSUtil.getPrefixes().toArray()));
		uriPrefixBox.insertItemAt("", 0);
		uriPrefixBox.setSelectedIndex(0);

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
		c.weightx = 2;
		c.weighty = 5;
		c.anchor = GridBagConstraints.WEST;
		c.gridwidth = GridBagConstraints.REMAINDER;
		gridbag.setConstraints(uriTypeGroupPanel, c);
		panel.add(uriTypeGroupPanel);
		c.anchor = GridBagConstraints.CENTER;
		c.gridwidth = GridBagConstraints.RELATIVE;
		gridbag.setConstraints(uriPrefixBox, c);
		panel.add(uriPrefixBox);
		c.gridwidth = GridBagConstraints.REMAINDER;
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

	class ChangePrefixAction extends AbstractAction {
		public void actionPerformed(ActionEvent e) {
			PrefixNSUtil.replacePrefix((String) uriPrefixBox.getSelectedItem(), uriField);
		}
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
				if (uriField.getText().length() == 0 || uriField.getText().charAt(0) != '#') {
					uriField.setText('#' + uriField.getText());
				}
				uriPrefixBox.setEnabled(false);
			} else if (uriType == URIType.URI) {
				uriPrefixBox.setEnabled(true);
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
