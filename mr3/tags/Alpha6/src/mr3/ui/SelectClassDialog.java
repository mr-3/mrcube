package mr3.ui;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

import javax.swing.*;

import mr3.data.*;
import mr3.jgraph.*;
import mr3.util.*;

import com.jgraph.event.*;
import com.jgraph.graph.*;

/**
 *
 * @auther takeshi morita
 */
public abstract class SelectClassDialog extends JDialog implements ActionListener, GraphSelectionListener {

	protected boolean isOk;
	protected JButton confirm;
	protected JButton cancel;

	private int index; // 検索のインデックス 
	private String currentKey; //現在のキー
	private List findList; // 検索リスト
	protected JComboBox uriPrefixBox;
	protected JLabel nsLabel;
	protected JTextField findField;
	protected JButton findButton;

	protected RDFGraph graph;
	protected JPanel inlinePanel = new JPanel();
	protected GridBagLayout gridbag;
	protected GridBagConstraints c;

	protected GraphManager gmanager;
	protected RDFSInfoMap rdfsMap = RDFSInfoMap.getInstance();

	public SelectClassDialog(String title, GraphManager manager) {
		super(manager.getRoot(), title, true);
		gmanager = manager;
		index = 0;
		currentKey = null;
		setResizable(false);

		initFindGroup();
		initGraph();
		initEachDialogAttr();
		initButton();

		initGridLayout();
		setFindGroupLayout();
		setGraphLayout();
		setEachDialogAttrLayout();
		setCommonLayout();
	}

	protected abstract void initEachDialogAttr();

	private static final int prefixBoxWidth = 120;
	private static final int prefixBoxHeight = 50;
	protected static final int listWidth = 450;
	protected static final int listHeight = 40;

	protected void initComponent(JComponent component, String title, int width, int height) {
		component.setPreferredSize(new Dimension(width, height));
		component.setMinimumSize(new Dimension(width, height));
		component.setBorder(BorderFactory.createTitledBorder(title));
	}

	protected void initFindGroup() {
		AbstractAction findAction = new FindAction();
		uriPrefixBox = new JComboBox();
		uriPrefixBox.addActionListener(new ChangePrefixAction());
		PrefixNSUtil.setPrefixNSInfoSet(gmanager.getPrefixNSInfoSet());
		uriPrefixBox.setModel(new DefaultComboBoxModel(PrefixNSUtil.getPrefixes().toArray()));
		initComponent(uriPrefixBox, "Prefix", prefixBoxWidth, prefixBoxHeight);
		findField = new JTextField(15);
		initComponent(findField, "ID", prefixBoxWidth, listHeight);
		findField.addActionListener(findAction);
		findButton = new JButton("Find");
		findButton.addActionListener(findAction);
		nsLabel = new JLabel("");
		initComponent(nsLabel, "NameSpace", listWidth, listHeight);
	}

	class ChangePrefixAction extends AbstractAction {
		public void actionPerformed(ActionEvent e) {
			PrefixNSUtil.replacePrefix((String) uriPrefixBox.getSelectedItem(), nsLabel);
		}
	}

	class FindAction extends AbstractAction {

		private void findNextResource(List findList) {
			if (findList != null && findList.size() > 0) {
				if (index == findList.size()) {
					index = 0;
				}
				gmanager.jumpArea(findList.get(index), graph);
				index++;
			}
		}

		public void actionPerformed(ActionEvent e) {
			String key = nsLabel.getText()+findField.getText() + ".*";
			if (currentKey == null || (!currentKey.equals(key))) {
				index = 0; // indexを元に戻す
				currentKey = key;
				findList = new ArrayList(gmanager.getFindRDFSResult(key, graph));
				findNextResource(findList);
			} else {
				findNextResource(findList);
			}
		}
	}

	protected void initButton() {
		confirm = new JButton("OK");
		confirm.addActionListener(this);
		cancel = new JButton("Cancel");
		cancel.addActionListener(this);
	}

	protected void initGridLayout() {
		gridbag = new GridBagLayout();
		c = new GridBagConstraints();
		inlinePanel.setLayout(gridbag);
		c.anchor = GridBagConstraints.PAGE_START;
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.weightx = 1;
		c.weighty = 3;
	}

	protected void setFindGroupLayout() {
		JPanel findPanel = new JPanel();
		findPanel.add(uriPrefixBox);
		findPanel.add(findField);
		findPanel.add(findButton);
		gridbag.setConstraints(findPanel, c);
		inlinePanel.add(findPanel);
		gridbag.setConstraints(nsLabel, c);
		inlinePanel.add(nsLabel);
	}

	protected void setGraphLayout() {
		JScrollPane graphScroll = new JScrollPane(graph);
		graphScroll.setPreferredSize(new Dimension(450, 250));
		graphScroll.setMinimumSize(new Dimension(450, 250));
		gridbag.setConstraints(graphScroll, c);
		inlinePanel.add(graphScroll);
	}

	protected abstract void setEachDialogAttrLayout();

	protected void setCommonLayout() {
		c.gridwidth = GridBagConstraints.RELATIVE;
		gridbag.setConstraints(confirm, c);
		inlinePanel.add(confirm);
		c.gridwidth = GridBagConstraints.REMAINDER;
		gridbag.setConstraints(cancel, c);
		inlinePanel.add(cancel);

		Container contentPane = getContentPane();
		contentPane.add(inlinePanel);
		setLocation(100, 100);
		setSize(new Dimension(550, 550));
		setVisible(false);
	}

	private void initGraph() {
		graph = new RDFGraph();
		graph.setMarqueeHandler(new BasicMarqueeHandler());
		graph.getSelectionModel().addGraphSelectionListener(this);
		graph.setCloneable(false);
		graph.setBendable(false);
		graph.setDisconnectable(false);
		graph.setPortsVisible(false);
		graph.setDragEnabled(false);
		graph.setDropEnabled(false);
		graph.setEditable(false);
	}

	public void replaceGraph(RDFGraph newGraph) {
		graph.setRDFState(newGraph.getRDFState());
		changeAllCellColor(Color.green);
	}

	public abstract void valueChanged(GraphSelectionEvent e);

	public void actionPerformed(ActionEvent e) {
		String type = (String) e.getActionCommand();
		if (type.equals("OK")) {
			isOk = true;
		} else {
			isOk = false;
		}
		setVisible(false);
	}

	protected void changeAllCellColor(Color color) {
		Object[] cells = graph.getAllCells();
		for (int i = 0; i < cells.length; i++) {
			GraphCell cell = (GraphCell) cells[i];
			if (graph.isRDFSClassCell(cell)) {
				ChangeCellAttributes.changeCellColor(graph, cell, color);
			}
		}
	}
}
