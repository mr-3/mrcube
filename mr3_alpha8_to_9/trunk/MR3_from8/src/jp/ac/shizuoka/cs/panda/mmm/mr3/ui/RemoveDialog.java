package jp.ac.shizuoka.cs.panda.mmm.mr3.ui;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;
import javax.swing.event.*;

import jp.ac.shizuoka.cs.panda.mmm.mr3.jgraph.*;

import com.jgraph.graph.*;

/**
 * @author takeshi morita
 *
 */
public class RemoveDialog extends JInternalFrame implements ListSelectionListener, ActionListener {

	private RDFGraph graph;
	private JList removeRDFSList;
	private ReferenceListPanel refListPanel;
	private GraphManager gmanager;

	private JButton apply;
	private JButton cancel;

	private static final int LIST_WIDTH = 400;
	private static final int LIST_HEIGHT = 80;

	public RemoveDialog(String title, GraphManager manager) {
		super(title, false, false);
		Container contentPane = getContentPane();
		gmanager = manager;

		removeRDFSList = new JList();
		removeRDFSList.addListSelectionListener(this);
		JScrollPane removeClassListScroll = new JScrollPane(removeRDFSList);
		removeClassListScroll.setBorder(BorderFactory.createTitledBorder("Remove List"));
		removeClassListScroll.setPreferredSize(new Dimension(LIST_WIDTH, LIST_HEIGHT));
		removeClassListScroll.setMinimumSize(new Dimension(LIST_WIDTH, LIST_HEIGHT));

		refListPanel = new ReferenceListPanel(gmanager);

		apply = new JButton("Apply");
		apply.addActionListener(this);
		cancel = new JButton("Cancel");
		cancel.addActionListener(this);

		GridBagLayout gridbag = new GridBagLayout();
		GridBagConstraints c = new GridBagConstraints();
		contentPane.setLayout(gridbag);
		c.anchor = GridBagConstraints.PAGE_START;
		c.gridwidth = GridBagConstraints.REMAINDER;

		gridbag.setConstraints(removeClassListScroll, c);
		contentPane.add(removeClassListScroll);
		gridbag.setConstraints(refListPanel, c);
		contentPane.add(refListPanel);

		JPanel inlinePanel = new JPanel();
		inlinePanel.add(apply);
		inlinePanel.add(cancel);
		gridbag.setConstraints(inlinePanel, c);
		contentPane.add(inlinePanel);

		setSize(new Dimension(450, 350));
		setVisible(false);
	}

	public void setRefListInfo(RDFGraph graph, Set cells, Map classRDFMap, Map classPropMap) {
		this.graph = graph;
		removeRDFSList.setListData(cells.toArray());
		refListPanel.setTableModelMap(cells, classRDFMap, classPropMap);
		if (removeRDFSList.getModel().getSize() != 0) {
			removeRDFSList.setSelectedIndex(0);
		}
	}

	public void valueChanged(ListSelectionEvent e) {
		Object cell = removeRDFSList.getSelectedValue();
		refListPanel.replaceTableModel(cell);
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == apply) {
			ListModel listModel = removeRDFSList.getModel(); 
			for (int i = 0; i < listModel.getSize(); i++) {
				DefaultGraphCell removeRDFSCell = (DefaultGraphCell) listModel.getElementAt(i);
				refListPanel.removeAction(removeRDFSCell);
			}
			setVisible(false);
			gmanager.retryRemoveCells();
		} else if (e.getSource() == cancel) {
			setVisible(false);
		}
	}

	public void setVisible(boolean t) {
		if (gmanager != null) {
			gmanager.setEnabled(!t);
		}
		super.setVisible(t);		
	}
}
