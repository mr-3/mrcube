package jp.ac.shizuoka.cs.panda.mmm.mr3.ui;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;
import javax.swing.event.*;

import jp.ac.shizuoka.cs.panda.mmm.mr3.data.*;
import jp.ac.shizuoka.cs.panda.mmm.mr3.jgraph.*;
import jp.ac.shizuoka.cs.panda.mmm.mr3.util.*;

/**
 * @author takeshi morita
 *
 */
public class FindResourceDialog extends JInternalFrame {

	private JTextField findField;
	private JLabel nsLabel;
	private Set prefixNSInfoSet;
	private JComboBox uriPrefixBox;
	private JButton findButton;
	private JList resourceList;

	private ButtonGroup group;
	private JRadioButton rdfAreaButton;
	private JRadioButton classAreaButton;
	private JRadioButton propertyAreaButton;
	private GraphType findArea;

	private GraphManager gmanager;
	private static Object[] NULL = new Object[0];

	private static final int BOX_WIDTH = 120;
	private static final int BOX_HEIGHT = 50;
	private static final int LIST_WIDTH = 350;
	private static final int LIST_HEIGHT = 30;
	private static final int FIELD_HEIGHT = 40;

	public FindResourceDialog(String title, GraphManager manager) {
		super(title, false, true, false);
		Container contentPane = getContentPane();

		gmanager = manager;
		JComponent buttonGroupPanel = getButtonGroupPanel();
		JComponent findAreaPanel = getFindAreaPanel();

		nsLabel = new JLabel("");
		initComponent(nsLabel, "NameSpace", LIST_WIDTH, FIELD_HEIGHT);

		resourceList = new JList();
		resourceList.addListSelectionListener(new JumpAction());
		JScrollPane resourceListScroll = new JScrollPane(resourceList);
		initComponent(resourceListScroll, "Find List", LIST_WIDTH, 150);

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
		c.anchor = GridBagConstraints.CENTER;
		gridbag.setConstraints(buttonGroupPanel, c);
		contentPane.add(buttonGroupPanel);

		gridbag.setConstraints(findAreaPanel, c);
		contentPane.add(findAreaPanel);

		gridbag.setConstraints(nsLabel, c);
		contentPane.add(nsLabel);

		gridbag.setConstraints(resourceListScroll, c);
		contentPane.add(resourceListScroll);

		setLocation(100, 100);
		setSize(new Dimension(400, 350));
		setVisible(false);
	}

	private void initComponent(JComponent component, String title, int width, int height) {
		component.setPreferredSize(new Dimension(width, height));
		component.setMinimumSize(new Dimension(width, height));
		component.setBorder(BorderFactory.createTitledBorder(title));
	}

	private JComponent getButtonGroupPanel() {
		FindAreaCheck findAreaCheck = new FindAreaCheck();
		rdfAreaButton = new JRadioButton("RDF");
		rdfAreaButton.setSelected(true);
		rdfAreaButton.addItemListener(findAreaCheck);
		classAreaButton = new JRadioButton("Class");
		classAreaButton.addItemListener(findAreaCheck);
		propertyAreaButton = new JRadioButton("Property");
		propertyAreaButton.addItemListener(findAreaCheck);

		group = new ButtonGroup();
		group.add(rdfAreaButton);
		group.add(classAreaButton);
		group.add(propertyAreaButton);
		JPanel buttonGroupPanel = new JPanel();
		initComponent(buttonGroupPanel, "GraphType", 250, 60);
		buttonGroupPanel.add(rdfAreaButton);
		buttonGroupPanel.add(classAreaButton);
		buttonGroupPanel.add(propertyAreaButton);

		return buttonGroupPanel;
	}

	private JComponent getFindAreaPanel() {
		JPanel inlinePanel = new JPanel();
		FindAction findAction = new FindAction();
		uriPrefixBox = new JComboBox();
		uriPrefixBox.addActionListener(new ChangePrefixAction());
		initComponent(uriPrefixBox, "Prefix", BOX_WIDTH, BOX_HEIGHT);

		findField = new JTextField(15);
		initComponent(findField, "ID", BOX_WIDTH, FIELD_HEIGHT);
		findField.addActionListener(findAction);
		findButton = new JButton("Find");
		findButton.addActionListener(findAction);
		inlinePanel.add(uriPrefixBox);
		inlinePanel.add(findField);
		inlinePanel.add(findButton);
		return inlinePanel;
	}

	public void setSearchArea(GraphType type) {
		findField.setText("");
		if (type == GraphType.RDF) {
			rdfAreaButton.setSelected(true);
			findArea = GraphType.RDF;
		} else if (type == GraphType.CLASS) {
			classAreaButton.setSelected(true);
			findArea = GraphType.CLASS;
		} else if (type == GraphType.PROPERTY) {
			propertyAreaButton.setSelected(true);
			findArea = GraphType.PROPERTY;
		}
	}

	private void setURIPrefixBox() {
		prefixNSInfoSet = gmanager.getPrefixNSInfoSet();
		PrefixNSUtil.setPrefixNSInfoSet(prefixNSInfoSet);
		uriPrefixBox.setModel(new DefaultComboBoxModel(PrefixNSUtil.getPrefixes().toArray()));
	}

	public void setVisible(boolean aFlag) {
		if (aFlag) {
			setURIPrefixBox();
		}
		super.setVisible(aFlag);
	}
	
	class ChangePrefixAction extends AbstractAction {
		public void actionPerformed(ActionEvent e) {
			PrefixNSUtil.replacePrefix((String) uriPrefixBox.getSelectedItem(), nsLabel);
		}
	}

	class FindAreaCheck implements ItemListener {
		public void itemStateChanged(ItemEvent e) {
			if (e.getItemSelectable() == rdfAreaButton) {
				findArea = GraphType.RDF;
			} else if (e.getItemSelectable() == classAreaButton) {
				findArea = GraphType.CLASS;
			} else if (e.getItemSelectable() == propertyAreaButton) {
				findArea = GraphType.PROPERTY;
			}
			resourceList.setListData(NULL);
		}
	}

	class FindAction extends AbstractAction {
		public void actionPerformed(ActionEvent e) {
			resourceList.removeAll();
			// はじめの部分だけマッチしていれば，検索対象にするようにする
			String key = nsLabel.getText()+findField.getText() + ".*";
			Set resourceSet = new HashSet();
			if (findArea == GraphType.RDF) {
				resourceSet = gmanager.getFindRDFResult(key);
			} else if (findArea == GraphType.CLASS) {
				resourceSet = gmanager.getFindClassResult(key);
			} else if (findArea == GraphType.PROPERTY) {
				resourceSet = gmanager.getFindPropertyResult(key);
			}
			resourceList.setListData(resourceSet.toArray());
		}
	}

	class JumpAction implements ListSelectionListener {
		public void valueChanged(ListSelectionEvent e) {
			Object cell = resourceList.getSelectedValue();
			if (findArea == GraphType.RDF) {
				gmanager.jumpRDFArea(cell);
			} else if (findArea == GraphType.CLASS) {
				gmanager.jumpClassArea(cell);
			} else if (findArea == GraphType.PROPERTY) {
				gmanager.jumpPropertyArea(cell);
			}
		}
	}

}
