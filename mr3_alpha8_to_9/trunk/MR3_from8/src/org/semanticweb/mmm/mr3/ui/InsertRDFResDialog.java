/*
 * @(#) InsertRDFResDialog.java
 *
 * Copyright (C) 2003 The MMM Project
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 */

package org.semanticweb.mmm.mr3.ui;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import org.semanticweb.mmm.mr3.data.*;
import org.semanticweb.mmm.mr3.jgraph.*;
import org.semanticweb.mmm.mr3.util.*;

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

	private static final int BOX_WIDTH = 150;
	private static final int BOX_HEIGHT = 50;
	private static final int ID_WIDTH = 120;

	public InsertRDFResDialog(Object[] cells, GraphManager gmanager) {
		super(gmanager.getRoot(), Translator.getString("InsertResourceDialog.Title"), true);
		Container contentPane = getContentPane();
		PrefixNSUtil.setPrefixNSInfoSet(gmanager.getPrefixNSInfoSet());
		resourceType = null;

		resTypeBox = new JComboBox(new DefaultComboBoxModel(cells));
		Utilities.initComponent(resTypeBox, Translator.getString("ResourceType"), 350, 50);
		resTypeBox.addItemListener(this);

		idField = new JTextField();
		Utilities.initComponent(idField, "ID", ID_WIDTH, 40);

		isAnonBox = new JCheckBox(Translator.getString("IsBlank"));
		isAnonBox.addActionListener(new IsAnonAction());

		uriPrefixBox = new JComboBox();
		Utilities.initComponent(uriPrefixBox, MR3Constants.PREFIX, BOX_WIDTH, BOX_HEIGHT);
		uriPrefixBox.addActionListener(new ChangePrefixAction());
		uriPrefixBox.setModel(new DefaultComboBoxModel(PrefixNSUtil.getPrefixes().toArray()));

		JPanel uriPanel = new JPanel();
		uriPanel.add(uriPrefixBox);
		uriPanel.add(idField);
		uriPanel.add(isAnonBox);

		nsLabel = new JLabel("");
		Utilities.initComponent(nsLabel, MR3Constants.NAME_SPACE, 350, 40);

		JPanel centerPanel = new JPanel();
		centerPanel.setLayout(new BorderLayout());
		centerPanel.add(uriPanel, BorderLayout.CENTER);
		centerPanel.add(nsLabel, BorderLayout.SOUTH);

		uriPrefixBox.setSelectedItem(PrefixNSUtil.getBaseURIPrefix(gmanager.getBaseURI()));
		PrefixNSUtil.replacePrefix((String) uriPrefixBox.getSelectedItem(), nsLabel);

		confirm = new JButton(MR3Constants.OK);
		confirm.addActionListener(this);
		cancel = new JButton(MR3Constants.CANCEL);
		cancel.addActionListener(this);
		JPanel buttonPanel = new JPanel();
		buttonPanel.add(confirm);
		buttonPanel.add(cancel);

		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		panel.add(resTypeBox, BorderLayout.NORTH);
		panel.add(centerPanel, BorderLayout.CENTER);
		panel.add(buttonPanel, BorderLayout.SOUTH);
		contentPane.add(panel);

		setLocation(300, 300);
		setSize(new Dimension(400, 220));
		setResizable(false);
		setVisible(true);
	}

	class ChangePrefixAction extends AbstractAction {
		public void actionPerformed(ActionEvent e) {
			PrefixNSUtil.replacePrefix((String) uriPrefixBox.getSelectedItem(), nsLabel);
		}
	}

	class IsAnonAction extends AbstractAction {
		public void actionPerformed(ActionEvent e) {
			setIDField("", !isAnonBox.isSelected());
			nsLabel.setEnabled(!isAnonBox.isSelected());
			uriPrefixBox.setEnabled(!isAnonBox.isSelected());
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
		return nsLabel.getText() + idField.getText();
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
