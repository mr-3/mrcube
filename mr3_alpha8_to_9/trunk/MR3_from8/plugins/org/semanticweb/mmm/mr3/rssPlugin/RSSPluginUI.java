package org.semanticweb.mmm.mr3.rssPlugin;

import java.util.*;

import javax.swing.*;
import javax.swing.table.*;

/*
 * Created on 2003/08/11
 *
 */

/**
 * @author takeshi morita
 */
public class RSSPluginUI extends JPanel {

	private JLabel channelURI;
	private JLabel channelDescription;
	private JLabel channelLanguage;
	private JLabel channelTitle;
	private JLabel channelLink;

	private JTextField channelURIField;
	private JTextField channelLanguageField;
	private JTextField channelLinkField;
	private JTextField channelTitleField;

	private JTextArea channelDescriptionArea;

	private JTable table;
	private ItemTableModel tblModel;

	private ItemInfo channelInfo;
	private Map itemInfoMap;

	public RSSPluginUI(ItemInfo info, Map map) {
		channelInfo = info;
		itemInfoMap = map;

		initField();
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

		add(getInnerPanel(channelURI, channelURIField));
		add(getInnerPanel(channelTitle, channelTitleField));
		add(getInnerPanel(channelLink, channelLinkField));
		add(getInnerPanel(channelLanguage, channelLanguageField));
		add(channelDescription);
		add(channelDescriptionArea);
		initTable();
		add(new JScrollPane(table));
	}

	public DefaultTableModel getTableModel() {
		return tblModel;
	}

	public String getChannelURI() {
		return channelURIField.getText();
	}

	public String getChannelTitle() {
		return channelTitleField.getText();
	}

	public String getChannelLink() {
		return channelLinkField.getText();
	}

	public String getChannelDescription() {
		return channelDescriptionArea.getText();
	}

	public String getChannelLanguage() {
		return channelLanguageField.getText();
	}

	private JPanel getInnerPanel(JLabel label, JTextField text) {
		JPanel innerPanel = new JPanel();
		innerPanel.add(label);
		innerPanel.add(text);

		return innerPanel;
	}

	private void initField() {
		channelURI = new JLabel("Channel URI");
		channelTitle = new JLabel("Channel Title");
		channelDescription = new JLabel("Channel Description");
		channelLanguage = new JLabel("Channel Language");
		channelLink = new JLabel("Channel Link");

		channelURIField = new JTextField(20);
		channelURIField.setText(channelInfo.getURI().toString());
		channelURIField.setEditable(false);
		channelTitleField = new JTextField(20);
		channelTitleField.setText(channelInfo.getTitle());
		channelDescriptionArea = new JTextArea(10, 10);
		channelDescriptionArea.setText(channelInfo.getDescription());
		channelLanguageField = new JTextField(20);
		channelLanguageField.setText(channelInfo.getLanguage());
		channelLinkField = new JTextField(20);
		channelLinkField.setText(channelInfo.getLink());
	}

	private void initTable() {
		table = new JTable();
		Object[] columnNames = new Object[] { "Available", "Item URI" };
		tblModel = new ItemTableModel(columnNames, 0);

		for (Iterator i = itemInfoMap.keySet().iterator(); i.hasNext();) {
			ItemInfo info = (ItemInfo) itemInfoMap.get(i.next());
			Object[] list = new Object[] { new Boolean(true), info.getURI()};
			tblModel.insertRow(tblModel.getRowCount(), list);
		}
		table.setModel(tblModel);
		TableColumnModel tcModel = table.getColumnModel();
		tcModel.getColumn(0).setPreferredWidth(50);
		tcModel.getColumn(1).setPreferredWidth(350);
	}

	public class ItemTableModel extends DefaultTableModel {

		public ItemTableModel(Object[] columnNames, int rowCount) {
			super(columnNames, rowCount);
		}

		public boolean isCellEditable(int row, int col) {
			if (col == 0) {
				return true;
			} else {
				return false;
			}
		}

		public Class getColumnClass(int column) {
			Vector v = (Vector) dataVector.elementAt(0);
			return v.elementAt(column).getClass();
		}
	}
}
