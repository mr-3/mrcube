/*
 * Created on 2003/06/22
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
public class InsertRDFSResDialog extends JDialog implements ActionListener {

	private boolean isConfirm;
	private JTextField idField;
	private JLabel nsLabel;
	private JComboBox uriPrefixBox;

	private String baseURI;

	private JButton confirm;
	private JButton cancel;

	private static final int listWidth = 300;
	private static final int boxWidth = 120;
	private static final int boxHeight = 50;

	public InsertRDFSResDialog(String title, Set pnis, String base) {
		super((Frame) null, title, true);
		Container contentPane = getContentPane();
		baseURI = base;
		PrefixNSUtil.setPrefixNSInfoSet(pnis);

		idField = new JTextField(15);
		idField.setText("");
		initComponent(idField, "ID", 150, 40);

		uriPrefixBox = new JComboBox();
		initComponent(uriPrefixBox, "Prefix", boxWidth, boxHeight);
		uriPrefixBox.setModel(new DefaultComboBoxModel(PrefixNSUtil.getPrefixes().toArray()));
		uriPrefixBox.addActionListener(new ChangePrefixAction());

		nsLabel = new JLabel("");
		initComponent(nsLabel, "NameSpace", listWidth, 40);
		
		uriPrefixBox.setSelectedItem(PrefixNSUtil.getBaseURIPrefix(baseURI));
		PrefixNSUtil.replacePrefix((String) uriPrefixBox.getSelectedItem(), nsLabel);

		JPanel uriPanel = new JPanel();
		uriPanel.add(uriPrefixBox);
		uriPanel.add(idField);

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
		c.anchor = GridBagConstraints.CENTER;
		c.gridwidth = GridBagConstraints.REMAINDER;
		gridbag.setConstraints(uriPanel, c);
		panel.add(uriPanel);
		gridbag.setConstraints(nsLabel, c);
		panel.add(nsLabel);
		gridbag.setConstraints(buttonPanel, c);
		panel.add(buttonPanel);
		contentPane.add(panel);

		setLocation(300, 300);
		setSize(new Dimension(400, 180));
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

	public boolean isConfirm() {
		return isConfirm;
	}

	public String getURI() {
		return nsLabel.getText() + idField.getText();
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
