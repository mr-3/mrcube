package mr3.ui;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;
import javax.swing.event.*;

import mr3.data.*;
import mr3.jgraph.*;

/**
 * @author take
 *
 */
public class SearchResourceDialog extends JInternalFrame {

	private JTextField searchField;
	private JButton searchButton;
	private JList resourceList;

	private ButtonGroup group;
	private JRadioButton rdfArea;
	private JRadioButton classArea;
	private JRadioButton propertyArea;
	private GraphType searchArea;

	private JButton closeButton;
	 
	private GraphManager gmanager;
	private static Object[] NULL = new Object[0];

	public SearchResourceDialog(String title, GraphManager manager) {
		super(title, false, false);
		Container contentPane = getContentPane();

		gmanager = manager;

		SearchAction searchAction = new SearchAction();
		searchField = new JTextField(15);
		searchField.addActionListener(searchAction);
		searchButton = new JButton("Search");
		searchButton.addActionListener(searchAction);

		SearchAreaCheck searchAreaCheck = new SearchAreaCheck();
		rdfArea = new JRadioButton("RDF");
		rdfArea.addItemListener(searchAreaCheck);
		classArea = new JRadioButton("Class");
		classArea.addItemListener(searchAreaCheck);
		propertyArea = new JRadioButton("Property");
		propertyArea.addItemListener(searchAreaCheck);

		resourceList = new JList();
		resourceList.addListSelectionListener(new JumpAction());
		JScrollPane resourceListScroll = new JScrollPane(resourceList);
		resourceListScroll.setPreferredSize(new Dimension(350, 70));
		resourceListScroll.setMinimumSize(new Dimension(350, 70));

		group = new ButtonGroup();
		rdfArea.setSelected(true);
		group.add(rdfArea);
		group.add(classArea);
		group.add(propertyArea);
		JPanel inlinePanel = new JPanel();
		inlinePanel.add(rdfArea);
		inlinePanel.add(classArea);
		inlinePanel.add(propertyArea);

		closeButton = new JButton("Close");
		closeButton.addActionListener(new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				setVisible(false);		
			}
		});

		// ‚¤‚Ü‚­“®‚©‚È‚¢
		addInternalFrameListener(new InternalFrameAdapter() {
			public void internalFrameClosed(InternalFrameEvent e) {
				setVisible(false);
			}
		});
	
		GridBagLayout gridbag = new GridBagLayout();
		GridBagConstraints c = new GridBagConstraints();
		contentPane.setLayout(gridbag);
		c.anchor = GridBagConstraints.PAGE_START;
		c.gridwidth = GridBagConstraints.RELATIVE;
		gridbag.setConstraints(searchField, c);
		contentPane.add(searchField);
		c.gridwidth = GridBagConstraints.REMAINDER;
		gridbag.setConstraints(searchButton, c);
		contentPane.add(searchButton);

		gridbag.setConstraints(inlinePanel, c);
		contentPane.add(inlinePanel);

		gridbag.setConstraints(resourceListScroll, c);
		contentPane.add(resourceListScroll);
		
		gridbag.setConstraints(closeButton, c);
		contentPane.add(closeButton);
	
		setLocation(100, 100);
		setSize(new Dimension(400, 200));
		setVisible(false);
	}

	class SearchAreaCheck implements ItemListener {
		public void itemStateChanged(ItemEvent e) {
			if (e.getItemSelectable() == rdfArea) {
				searchArea = GraphType.RDF;
			} else if (e.getItemSelectable() == classArea) {
				searchArea = GraphType.CLASS;
			} else if (e.getItemSelectable() == propertyArea) {
				searchArea = GraphType.PROPERTY;
			}

			resourceList.setListData(NULL);
		}
	}

	class SearchAction extends AbstractAction {
		public void actionPerformed(ActionEvent e) {
			resourceList.removeAll();
			String key = searchField.getText();
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
