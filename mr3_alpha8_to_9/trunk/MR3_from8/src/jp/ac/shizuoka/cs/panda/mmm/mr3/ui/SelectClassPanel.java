/*
 * Created on 2003/09/25
 *
 */
package jp.ac.shizuoka.cs.panda.mmm.mr3.ui;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

import javax.swing.*;

import jp.ac.shizuoka.cs.panda.mmm.mr3.data.*;
import jp.ac.shizuoka.cs.panda.mmm.mr3.jgraph.*;
import jp.ac.shizuoka.cs.panda.mmm.mr3.util.*;

import org.jgraph.event.*;
import org.jgraph.graph.*;

/**
 * @author takeshi morita
 */
public abstract class SelectClassPanel extends JPanel implements GraphSelectionListener {
	private int index; // 検索のインデックス 
	private String currentKey; //現在のキー
	private List findList; // 検索リスト
	protected JComboBox uriPrefixBox;
	protected JLabel nsLabel;
	protected JTextField findField;
	protected JButton findButton;

	protected RDFGraph graph;

	protected GraphManager gmanager;
	protected RDFSInfoMap rdfsMap = RDFSInfoMap.getInstance();

	public SelectClassPanel(GraphManager gm) {
		index = 0;
		gmanager = gm;
		currentKey = null;
		
		initFindGroup();
		initGraph();
		initEachDialogAttr();

		setLayout(new BorderLayout());
		setFindGroupLayout();
		setGraphLayout();
		setEachDialogAttrLayout();
	}

	protected abstract void initEachDialogAttr();

	private static final int PREFIX_BOX_WIDTH = 120;
	private static final int PREFIX_BOX_HEIGHT = 50;
	protected static final int LIST_WIDTH = 450;
	protected static final int LIST_HEIGHT = 40;

	protected void initFindGroup() {
		AbstractAction findAction = new FindAction();
		uriPrefixBox = new JComboBox();
		uriPrefixBox.addActionListener(new ChangePrefixAction());
		PrefixNSUtil.setPrefixNSInfoSet(gmanager.getPrefixNSInfoSet());
		uriPrefixBox.setModel(new DefaultComboBoxModel(PrefixNSUtil.getPrefixes().toArray()));
		Utilities.initComponent(uriPrefixBox, Translator.getString("Prefix"), PREFIX_BOX_WIDTH, PREFIX_BOX_HEIGHT);
		findField = new JTextField(15);
		Utilities.initComponent(findField, "ID", PREFIX_BOX_WIDTH, LIST_HEIGHT);
		findField.addActionListener(findAction);
		findButton = new JButton(Translator.getString("Find"));
		findButton.addActionListener(findAction);
		nsLabel = new JLabel("");
		Utilities.initComponent(nsLabel, Translator.getString("NameSpace"), LIST_WIDTH, LIST_HEIGHT);
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
			String key = nsLabel.getText() + findField.getText() + ".*";
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

	protected void setFindGroupLayout() {
		JPanel findPanel = new JPanel();
		findPanel.add(uriPrefixBox);
		findPanel.add(findField);
		findPanel.add(findButton);
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		panel.add(findPanel, BorderLayout.CENTER);
		panel.add(nsLabel, BorderLayout.SOUTH);
		add(panel, BorderLayout.NORTH);
	}

	protected void setGraphLayout() {
		JScrollPane graphScroll = new JScrollPane(graph);
		Utilities.initComponent(graphScroll, "", 450, 250);
		add(graphScroll, BorderLayout.CENTER);
	}

	protected abstract void setEachDialogAttrLayout();

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
		if (gmanager.isClassGraph(newGraph)) {
			changeAllCellColor(ChangeCellAttributes.classColor);
		} else if (gmanager.isPropertyGraph(newGraph)) {
			changeAllCellColor(ChangeCellAttributes.propertyColor);
		}
	}

	public abstract void valueChanged(GraphSelectionEvent e);

	public RDFGraph getGraph() {
		return graph;
	}

	protected void changeAllCellColor(Color color) {
		Object[] cells = graph.getAllCells();
		for (int i = 0; i < cells.length; i++) {
			GraphCell cell = (GraphCell) cells[i];
			if (graph.isRDFSCell(cell)) {
				ChangeCellAttributes.changeCellColor(graph, cell, color);
			}
		}
	}
}
