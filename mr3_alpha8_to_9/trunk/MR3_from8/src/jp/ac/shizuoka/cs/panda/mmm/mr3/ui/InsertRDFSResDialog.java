/*
 * Created on 2003/06/22
 *
 */
package jp.ac.shizuoka.cs.panda.mmm.mr3.ui;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import jp.ac.shizuoka.cs.panda.mmm.mr3.jgraph.*;
import jp.ac.shizuoka.cs.panda.mmm.mr3.util.*;

/**
 * @author takeshi morita
 */
public class InsertRDFSResDialog extends JDialog implements ActionListener {

	private boolean isConfirm;
	private JTextField idField;
	private JLabel nsLabel;
	private JComboBox uriPrefixBox;

	private JButton confirm;
	private JButton cancel;

	private static final int LIST_WIDTH = 300;
	private static final int LIST_HEIGHT = 40;
	private static final int BOX_WIDTH = 120;
	private static final int BOX_HEIGHT = 50;
	private static final int ID_WIDTH = 180;

	public InsertRDFSResDialog(String title, GraphManager gmanager) {
		super(gmanager.getRoot(), title, true);
		Container contentPane = getContentPane();
		PrefixNSUtil.setPrefixNSInfoSet(gmanager.getPrefixNSInfoSet());

		idField = new JTextField();
		idField.setText("");
		initComponent(idField, "ID", ID_WIDTH, LIST_HEIGHT);

		uriPrefixBox = new JComboBox();
		initComponent(uriPrefixBox, Translator.getString("Prefix"), BOX_WIDTH, BOX_HEIGHT);
		uriPrefixBox.setModel(new DefaultComboBoxModel(PrefixNSUtil.getPrefixes().toArray()));
		uriPrefixBox.addActionListener(new ChangePrefixAction());

		nsLabel = new JLabel("");
		initComponent(nsLabel, Translator.getString("NameSpace"), LIST_WIDTH, LIST_HEIGHT);
		
		uriPrefixBox.setSelectedItem(PrefixNSUtil.getBaseURIPrefix(gmanager.getBaseURI()));
		PrefixNSUtil.replacePrefix((String) uriPrefixBox.getSelectedItem(), nsLabel);

		JPanel uriPanel = new JPanel();
		uriPanel.add(uriPrefixBox);
		uriPanel.add(idField);

		confirm = new JButton(Translator.getString("OK"));
		confirm.addActionListener(this);
		cancel = new JButton(Translator.getString("Cancel"));
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
		setSize(new Dimension(350, 180));
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
