package mr3.ui;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;
import javax.swing.event.*;

import mr3.data.*;
import mr3.jgraph.*;
import mr3.util.*;

/**
 * @author takeshi morita
 *
 */
public class FindResourceDialog extends JInternalFrame {

	private JTextField findField;
	private Set prefixNSInfoSet;
	private JComboBox uriPrefixBox;
	private JButton searchButton;
	private JList resourceList;

	private ButtonGroup group;
	private JRadioButton rdfAreaButton;
	private JRadioButton classAreaButton;
	private JRadioButton propertyAreaButton;
	private GraphType searchArea;

	private GraphManager gmanager;
	private static Object[] NULL = new Object[0];

	private static final int boxWidth = 70;
	private static final int boxHeight = 30;

	public FindResourceDialog(String title, GraphManager manager) {
		//		super(title, false, true, false);
		super(title, true, true, true);
		Container contentPane = getContentPane();

		gmanager = manager;

		SearchAction searchAction = new SearchAction();

		uriPrefixBox = new JComboBox();
		uriPrefixBox.addActionListener(new ChangePrefixAction());
		uriPrefixBox.setPreferredSize(new Dimension(boxWidth, boxHeight));
		uriPrefixBox.setMinimumSize(new Dimension(boxWidth, boxHeight));

		findField = new JTextField(25);
		findField.addActionListener(searchAction);
		searchButton = new JButton("Search");
		searchButton.addActionListener(searchAction);

		SearchAreaCheck searchAreaCheck = new SearchAreaCheck();
		rdfAreaButton = new JRadioButton("RDF");
		rdfAreaButton.addItemListener(searchAreaCheck);
		classAreaButton = new JRadioButton("Class");
		classAreaButton.addItemListener(searchAreaCheck);
		propertyAreaButton = new JRadioButton("Property");
		propertyAreaButton.addItemListener(searchAreaCheck);

		resourceList = new JList();
		resourceList.addListSelectionListener(new JumpAction());
		JScrollPane resourceListScroll = new JScrollPane(resourceList);
		resourceListScroll.setPreferredSize(new Dimension(430, 100));
		resourceListScroll.setMinimumSize(new Dimension(430, 100));

		group = new ButtonGroup();
		rdfAreaButton.setSelected(true);
		group.add(rdfAreaButton);
		group.add(classAreaButton);
		group.add(propertyAreaButton);
		JPanel inlinePanel = new JPanel();
		inlinePanel.setPreferredSize(new Dimension(250, 60));
		inlinePanel.setMinimumSize(new Dimension(250, 60));
		inlinePanel.setBorder(BorderFactory.createTitledBorder("Graph Type"));
		inlinePanel.add(rdfAreaButton);
		inlinePanel.add(classAreaButton);
		inlinePanel.add(propertyAreaButton);

		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE); // デフォルトの動作を消す
		addInternalFrameListener(new InternalFrameAdapter() {
			public void internalFrameClosing(InternalFrameEvent e) {
				setVisible(false);
			}
		});

		GridBagLayout gridbag = new GridBagLayout();
		GridBagConstraints c = new GridBagConstraints();
		contentPane.setLayout(gridbag);
		c.weightx = 1;
		c.weighty = 3;
		c.gridwidth = GridBagConstraints.REMAINDER;
		gridbag.setConstraints(inlinePanel, c);
		contentPane.add(inlinePanel);
		
		c.anchor = GridBagConstraints.WEST;
		c.gridwidth = GridBagConstraints.RELATIVE;
		gridbag.setConstraints(uriPrefixBox, c);
		contentPane.add(uriPrefixBox);
		gridbag.setConstraints(findField, c);
		contentPane.add(findField);
		c.gridwidth = GridBagConstraints.REMAINDER;
		gridbag.setConstraints(searchButton, c);
		contentPane.add(searchButton);

		c.anchor = GridBagConstraints.CENTER;
		gridbag.setConstraints(resourceListScroll, c);
		contentPane.add(resourceListScroll);

		setLocation(100, 100);
		setSize(new Dimension(550, 250));
		setVisible(false);
	}

	public void setSearchArea(GraphType type) {
		findField.setText("");
		if (type == GraphType.RDF) {
			rdfAreaButton.setSelected(true);
			searchArea = GraphType.RDF;
		} else if (type == GraphType.CLASS) {
			classAreaButton.setSelected(true);
			searchArea = GraphType.CLASS;
		} else if (type == GraphType.PROPERTY) {
			propertyAreaButton.setSelected(true);
			searchArea = GraphType.PROPERTY;
		}
	}

	private void setURIPrefixBox() {
		prefixNSInfoSet = gmanager.getPrefixNSInfoSet();
		PrefixNSUtil.setPrefixNSInfoSet(prefixNSInfoSet);
		uriPrefixBox.setModel(new DefaultComboBoxModel(PrefixNSUtil.getPrefixes().toArray()));
		uriPrefixBox.insertItemAt("", 0);
		uriPrefixBox.setSelectedIndex(0);
	}

	public void setVisible(boolean aFlag) {
		if (aFlag) {
			setURIPrefixBox();
		}
		super.setVisible(aFlag);
	}
	class ChangePrefixAction extends AbstractAction {
		public void actionPerformed(ActionEvent e) {
			PrefixNSUtil.replacePrefix((String) uriPrefixBox.getSelectedItem(), findField);
		}
	}

	class SearchAreaCheck implements ItemListener {
		public void itemStateChanged(ItemEvent e) {
			if (e.getItemSelectable() == rdfAreaButton) {
				searchArea = GraphType.RDF;
			} else if (e.getItemSelectable() == classAreaButton) {
				searchArea = GraphType.CLASS;
			} else if (e.getItemSelectable() == propertyAreaButton) {
				searchArea = GraphType.PROPERTY;
			}
			resourceList.setListData(NULL);
		}
	}

	class SearchAction extends AbstractAction {
		public void actionPerformed(ActionEvent e) {
			resourceList.removeAll();
			// はじめの部分だけマッチしていれば，検索対象にするようにする
			String key = findField.getText() + ".*";
			Set resourceSet = null;
			if (searchArea == GraphType.RDF) {
				resourceSet = gmanager.getSearchRDFResult(key);
			} else if (searchArea == GraphType.CLASS) {
				resourceSet = gmanager.getSearchClassResult(key);
			} else if (searchArea == GraphType.PROPERTY) {
				resourceSet = gmanager.getSearchPropertyResult(key);
			}
			resourceList.setListData(resourceSet.toArray());
		}
	}

	class JumpAction implements ListSelectionListener {
		public void valueChanged(ListSelectionEvent e) {
			Object cell = resourceList.getSelectedValue();
			if (searchArea == GraphType.RDF) {
				gmanager.jumpRDFArea(cell);
			} else if (searchArea == GraphType.CLASS) {
				gmanager.jumpClassArea(cell);
			} else if (searchArea == GraphType.PROPERTY) {
				gmanager.jumpPropertyArea(cell);
			}
		}
	}

}
