package mr3.ui;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;
import javax.swing.event.*;

import mr3.data.*;
import mr3.jgraph.*;

/**
 * @author takeshi morita
 *
 */
public class FindResourceDialog extends JInternalFrame {

	private JTextField findField;
	private JButton searchButton;
	private JList resourceList;

	private ButtonGroup group;
	private JRadioButton rdfAreaButton;
	private JRadioButton classAreaButton;
	private JRadioButton propertyAreaButton;
	private GraphType searchArea;

//	private JButton closeButton;

	private GraphManager gmanager;
	private static Object[] NULL = new Object[0];

	public FindResourceDialog(String title, GraphManager manager) {
		super(title, false, true, false);
		Container contentPane = getContentPane();

		gmanager = manager;

		SearchAction searchAction = new SearchAction();
		findField = new JTextField(20);
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
		resourceListScroll.setPreferredSize(new Dimension(400, 100));
		resourceListScroll.setMinimumSize(new Dimension(400, 100));

		group = new ButtonGroup();
		rdfAreaButton.setSelected(true);
		group.add(rdfAreaButton);
		group.add(classAreaButton);
		group.add(propertyAreaButton);
		JPanel inlinePanel = new JPanel();
		inlinePanel.add(rdfAreaButton);
		inlinePanel.add(classAreaButton);
		inlinePanel.add(propertyAreaButton);

//		closeButton = new JButton("Close");
//		closeButton.addActionListener(new AbstractAction() {
//			public void actionPerformed(ActionEvent e) {
//				setVisible(false);
//			}
//		});

		// デフォルトの動作を消す
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		// 閉じるときに，setVisible(false）にする
		addInternalFrameListener(new InternalFrameAdapter() {
			public void internalFrameClosing(InternalFrameEvent e) {
				setVisible(false);
			}
		});

		GridBagLayout gridbag = new GridBagLayout();
		GridBagConstraints c = new GridBagConstraints();
		contentPane.setLayout(gridbag);
		c.anchor = GridBagConstraints.PAGE_START;
		c.gridwidth = GridBagConstraints.RELATIVE;
		gridbag.setConstraints(findField, c);
		contentPane.add(findField);
		c.gridwidth = GridBagConstraints.REMAINDER;
		gridbag.setConstraints(searchButton, c);
		contentPane.add(searchButton);

		gridbag.setConstraints(inlinePanel, c);
		contentPane.add(inlinePanel);

		gridbag.setConstraints(resourceListScroll, c);
		contentPane.add(resourceListScroll);

//		gridbag.setConstraints(closeButton, c);
//		contentPane.add(closeButton);

		setLocation(100, 100);
		setSize(new Dimension(450, 250));
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
			String key = findField.getText()+".*";
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
