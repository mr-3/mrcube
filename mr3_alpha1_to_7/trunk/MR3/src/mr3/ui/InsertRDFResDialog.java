/*
 * Created on 2003/06/11
 *
 */
package mr3.ui;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;

import mr3.util.*;

/**
 * @author takeshi morita
 */
public class InsertRDFResDialog extends JDialog implements ActionListener, ItemListener {

	private boolean isConfirm;
	private JComboBox resTypeBox;
	private JTextField idField;
	private JLabel nsLabel;
	private JComboBox uriPrefixBox;
	private JButton confirm;
	private JButton cancel;
	private Object resourceType;
	private JCheckBox isAnonBox;

	private static final int boxWidth = 150;
	private static final int boxHeight = 50;

	public InsertRDFResDialog(String title, Object[] cells, Set pnis, String baseURI) {
		super((Frame) null, title, true);
		Container contentPane = getContentPane();
		PrefixNSUtil.setPrefixNSInfoSet(pnis);
		resourceType = null;

		resTypeBox = new JComboBox(new DefaultComboBoxModel(cells));
		initComponent(resTypeBox, "Resource Type", 350, 50);
		resTypeBox.addItemListener(this);

		idField = new JTextField(10);
		initComponent(idField, "LocalName", 150, 40);

		isAnonBox = new JCheckBox("isAnon");
		isAnonBox.addActionListener(new IsAnonAction());

		uriPrefixBox = new JComboBox();
		initComponent(uriPrefixBox, "Prefix", boxWidth, boxHeight);
		uriPrefixBox.addActionListener(new ChangePrefixAction());
		uriPrefixBox.setModel(new DefaultComboBoxModel(PrefixNSUtil.getPrefixes().toArray()));

		JPanel uriPanel = new JPanel();
		uriPanel.add(uriPrefixBox);
		uriPanel.add(idField);
		uriPanel.add(isAnonBox);

		nsLabel = new JLabel("");
		initComponent(nsLabel, "NameSpace", 350, 40);

		uriPrefixBox.setSelectedItem(PrefixNSUtil.getBaseURIPrefix(baseURI));
		PrefixNSUtil.replacePrefix((String) uriPrefixBox.getSelectedItem(), nsLabel);

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
		c.weightx = 1;
		c.weighty = 5;
		c.anchor = GridBagConstraints.PAGE_START;
		c.gridwidth = GridBagConstraints.REMAINDER;
		gridbag.setConstraints(resTypeBox, c);
		panel.add(resTypeBox);
		gridbag.setConstraints(uriPanel, c);
		panel.add(uriPanel);
		gridbag.setConstraints(nsLabel, c);
		panel.add(nsLabel);
		gridbag.setConstraints(buttonPanel, c);
		panel.add(buttonPanel);
		contentPane.add(panel);

		setLocation(300, 300);
		setSize(new Dimension(400, 230));
		setResizable(false);
		setVisible(true);
	}

	private void initComponent(JComponent component, String title, int width, int height) {
		component.setPreferredSize(new Dimension(width, height));
		component.setMinimumSize(new Dimension(width, height));
		component.setBorder(BorderFactory.createTitledBorder(title));
	}

	class ChangePrefixAction extends AbstractAction {
		public void actionPerformed(ActionEvent e) {
			PrefixNSUtil.replacePrefix((String) uriPrefixBox.getSelectedItem(), nsLabel);
		}
	}

	class IsAnonAction extends AbstractAction {
		public void actionPerformed(ActionEvent e) {
			if (isAnonBox.isSelected()) {
				setIDField("", false);
				uriPrefixBox.setEnabled(false);
			} else {
				setIDField("", true);
				uriPrefixBox.setEnabled(true);
			}
		}
	}

	public boolean isConfirm() {
		return isConfirm;
	}

	public boolean isAnonymous() {
		return isAnonBox.isSelected();
	}

	public Object getResourceType() {
		return resourceType;
	}

	public String getURI() {
		return nsLabel.getText()+idField.getText();
	}

	public void itemStateChanged(ItemEvent e) {
		resourceType = resTypeBox.getSelectedItem();
	}

	private void setIDField(String str, boolean t) {
		idField.setText(str);
		idField.setToolTipText(str);
		idField.setEditable(t);
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
