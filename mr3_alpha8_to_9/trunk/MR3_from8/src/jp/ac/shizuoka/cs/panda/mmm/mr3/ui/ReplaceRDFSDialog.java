/*
 * Created on 2003/09/23
 *
 */
package jp.ac.shizuoka.cs.panda.mmm.mr3.ui;

import java.awt.*;
import java.awt.Container;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;
import javax.swing.event.*;

import jp.ac.shizuoka.cs.panda.mmm.mr3.data.*;
import jp.ac.shizuoka.cs.panda.mmm.mr3.io.*;
import jp.ac.shizuoka.cs.panda.mmm.mr3.jgraph.*;

import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.vocabulary.*;

/**
 * @author takeshi morita
 */
public class ReplaceRDFSDialog extends JDialog implements ListSelectionListener, ActionListener {

	private GraphManager gmanager;

	private Set prevClassSet;
	private Set replaceClassSet;
	private Set prevPropertySet;
	private Set replacePropertySet;

	private JTabbedPane tabbedPane;
	private JList prevClassList;
	private DefaultListModel prevClassListModel;
	private JList replaceClassList;
	private DefaultListModel replaceClassListModel;
	private JList prevPropertyList;
	private DefaultListModel prevPropertyListModel;
	private JList replacePropertyList;
	private DefaultListModel replacePropertyListModel;

	private JButton prevClassUpButton;
	private JButton prevClassDownButton;
	private JButton replaceClassUpButton;
	private JButton replaceClassDownButton;
	private JButton prevPropertyUpButton;
	private JButton prevPropertyDownButton;
	private JButton replacePropertyUpButton;
	private JButton replacePropertyDownButton;

	private JButton applyButton;
	private JButton cancelButton;

	private boolean isApply;

	private RDFResourceInfoMap resInfoMap = RDFResourceInfoMap.getInstance();
	private RDFSInfoMap rdfsInfoMap = RDFSInfoMap.getInstance();

	private static final int LIST_WIDTH = 400;
	private static final int LIST_HEIGHT = 200;

	public ReplaceRDFSDialog(GraphManager gm, Model replaceModel) {
		super(gm.getRoot(), "Replace RDFS Dialog", true);
		gmanager = gm;
		tabbedPane = new JTabbedPane();

		RDFSModelExtraction rdfsModelExtraction = new RDFSModelExtraction(gm);
		Model classModel = rdfsModelExtraction.extractClassModel(replaceModel);
		Model propertyModel = rdfsModelExtraction.extractPropertyModel(replaceModel);

		prevClassListModel = new DefaultListModel();
		prevClassSet = gm.getClassSet();
		setListData(prevClassListModel, prevClassSet);
		prevClassList = new JList(prevClassListModel);
		prevClassList.addListSelectionListener(this);
		JScrollPane prevClassListScroll = new JScrollPane(prevClassList);
		initComponent(prevClassListScroll, "Prev Class List", LIST_WIDTH, LIST_HEIGHT);

		replaceClassListModel = new DefaultListModel();
		replaceClassSet = rdfsInfoMap.getClassSet(new HashSet(), RDFS.Resource);
		setListData(replaceClassListModel, replaceClassSet);
		replaceClassList = new JList(replaceClassListModel);
		replaceClassList.addListSelectionListener(this);
		JScrollPane replaceClassListScroll = new JScrollPane(replaceClassList);
		initComponent(replaceClassListScroll, "Replace Class List", LIST_WIDTH, LIST_HEIGHT);

		prevPropertyListModel = new DefaultListModel();
		prevPropertySet = gm.getPropertySet();
		setListData(prevPropertyListModel, prevPropertySet);
		prevPropertyList = new JList(prevPropertyListModel);
		prevPropertyList.addListSelectionListener(this);
		JScrollPane prevPropertyListScroll = new JScrollPane(prevPropertyList);
		initComponent(prevPropertyListScroll, "Prev Property List", LIST_WIDTH, LIST_HEIGHT);

		replacePropertyListModel = new DefaultListModel();
		replacePropertySet = new HashSet();
		for (Iterator i = rdfsInfoMap.getRootProperties().iterator(); i.hasNext();) {
			Resource res = (Resource) i.next();
			replacePropertySet.add(res.getURI());
			rdfsInfoMap.getPropertySet(replacePropertySet, res);
		}
		setListData(replacePropertyListModel, replacePropertySet);
		replacePropertyList = new JList(replacePropertyListModel);
		replacePropertyList.addListSelectionListener(this);
		JScrollPane replacePropertyListScroll = new JScrollPane(replacePropertyList);
		initComponent(replacePropertyListScroll, "Replace Property List", LIST_WIDTH, LIST_HEIGHT);

		prevClassUpButton = new JButton("Up");
		prevClassUpButton.addActionListener(this);
		prevClassDownButton = new JButton("Down");
		prevClassDownButton.addActionListener(this);
		JPanel prevClassButtonPanel = new JPanel();
		prevClassButtonPanel.setLayout(new BoxLayout(prevClassButtonPanel, BoxLayout.Y_AXIS));
		prevClassButtonPanel.add(prevClassUpButton);
		prevClassButtonPanel.add(prevClassDownButton);

		replaceClassUpButton = new JButton("Up");
		replaceClassUpButton.addActionListener(this);
		replaceClassDownButton = new JButton("Down");
		replaceClassDownButton.addActionListener(this);
		JPanel replaceClassButtonPanel = new JPanel();
		replaceClassButtonPanel.setLayout(new BoxLayout(replaceClassButtonPanel, BoxLayout.Y_AXIS));
		replaceClassButtonPanel.add(replaceClassUpButton);
		replaceClassButtonPanel.add(replaceClassDownButton);

		prevPropertyUpButton = new JButton("Up");
		prevPropertyUpButton.addActionListener(this);
		prevPropertyDownButton = new JButton("Down");
		prevPropertyDownButton.addActionListener(this);
		JPanel prevPropertyButtonPanel = new JPanel();
		prevPropertyButtonPanel.setLayout(new BoxLayout(prevPropertyButtonPanel, BoxLayout.Y_AXIS));
		prevPropertyButtonPanel.add(prevPropertyUpButton);
		prevPropertyButtonPanel.add(prevPropertyDownButton);

		replacePropertyUpButton = new JButton("Up");
		replacePropertyUpButton.addActionListener(this);
		replacePropertyDownButton = new JButton("Down");
		replacePropertyDownButton.addActionListener(this);
		JPanel replacePropertyButtonPanel = new JPanel();
		replacePropertyButtonPanel.setLayout(new BoxLayout(replacePropertyButtonPanel, BoxLayout.Y_AXIS));
		replacePropertyButtonPanel.add(replacePropertyUpButton);
		replacePropertyButtonPanel.add(replacePropertyDownButton);

		applyButton = new JButton("Apply");
		applyButton.addActionListener(this);
		cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(this);

		JPanel prevClassPanel = new JPanel();
		prevClassPanel.add(prevClassListScroll);
		prevClassPanel.add(prevClassButtonPanel);

		JPanel replaceClassPanel = new JPanel();
		replaceClassPanel.add(replaceClassListScroll);
		replaceClassPanel.add(replaceClassButtonPanel);

		JPanel classPanel = new JPanel();
		classPanel.add(prevClassPanel);
		classPanel.add(replaceClassPanel);

		JPanel prevPropertyPanel = new JPanel();
		prevPropertyPanel.add(prevPropertyListScroll);
		prevPropertyPanel.add(prevPropertyButtonPanel);

		JPanel replacePropertyPanel = new JPanel();
		replacePropertyPanel.add(replacePropertyListScroll);
		replacePropertyPanel.add(replacePropertyButtonPanel);

		JPanel propertyPanel = new JPanel();
		propertyPanel.add(prevPropertyPanel);
		propertyPanel.add(replacePropertyPanel);

		JPanel decisionPanel = new JPanel();
		decisionPanel.add(applyButton);
		decisionPanel.add(cancelButton);

		tabbedPane.add(classPanel, "Class");
		tabbedPane.add(propertyPanel, "Property");

		Container contentPane = getContentPane();
		contentPane.add(tabbedPane, BorderLayout.CENTER);
		contentPane.add(decisionPanel, BorderLayout.SOUTH);

		setSize(new Dimension(500, 550));
		setLocation(100, 100);
		setVisible(true);
	}

	private void setListData(DefaultListModel listModel, Set dataSet) {
		for (Iterator i = dataSet.iterator(); i.hasNext();) {
			listModel.addElement(i.next());
		}
	}

	private void initComponent(JComponent component, String title, int width, int height) {
		component.setPreferredSize(new Dimension(width, height));
		component.setMinimumSize(new Dimension(width, height));
		component.setBorder(BorderFactory.createTitledBorder(title));
	}

	public void valueChanged(ListSelectionEvent e) {
		if (e.getSource() == prevClassList) {
			replaceClassList.setSelectedIndex(prevClassList.getSelectedIndex());
		} else if (e.getSource() == replaceClassList) {
			prevClassList.setSelectedIndex(replaceClassList.getSelectedIndex());
		} else if (e.getSource() == prevPropertyList) {
			replacePropertyList.setSelectedIndex(prevPropertyList.getSelectedIndex());
		} else if (e.getSource() == replacePropertyList) {
			prevPropertyList.setSelectedIndex(replacePropertyList.getSelectedIndex());
		}
	}

	private void listUp(JList jList) {
		int selectedIndex = jList.getSelectedIndex();
		DefaultListModel listModel = (DefaultListModel) jList.getModel();
		if (0 < selectedIndex && selectedIndex < listModel.getSize()) {
			listUpDown(-1, selectedIndex, jList, listModel);
		}
	}

	private void listDown(JList jList) {
		int selectedIndex = jList.getSelectedIndex();
		DefaultListModel listModel = (DefaultListModel) jList.getModel();
		if (0 <= selectedIndex && selectedIndex < listModel.getSize() - 1) {
			listUpDown(+1, selectedIndex, jList, listModel);
		}
	}

	private void listUpDown(int num, int selectedIndex, JList jList, DefaultListModel listModel) {
		Object current = listModel.getElementAt(selectedIndex);
		Object prev = listModel.getElementAt(selectedIndex + num);
		listModel.setElementAt(current, selectedIndex + num);
		listModel.setElementAt(prev, selectedIndex);
		jList.setSelectedIndex(selectedIndex + num);
	}

	private Map getPrevReplaceMap(ListModel prevListModel, ListModel replaceListModel) {
		Map prevReplaceMap = new HashMap();
		for (int i = 0; i < prevListModel.getSize(); i++) {
			Object prevObject = prevListModel.getElementAt(i);
			if (i < replaceListModel.getSize()) {
				prevReplaceMap.put(prevObject, replaceListModel.getElementAt(i));
			}
		}
		return prevReplaceMap;
	}

	private void replaceRDFResourceType(Object cell, Map prevReplaceMap) {
		RDFResourceInfo resInfo = resInfoMap.getCellInfo(cell);
		RDFSInfo rdfsInfo = rdfsInfoMap.getCellInfo(resInfo.getTypeCell());
		String res = (String) prevReplaceMap.get(rdfsInfo.getURIStr());
		if (res == null) {
			resInfo.setTypeCell(null);
		} else {
			Resource resource = ResourceFactory.createResource(res);
			resInfo.setTypeCell(gmanager.getClassCell(resource, false));
		}
	}

	private void replaceRDFProperty(Object cell, Map prevReplaceMap) {
		Object propCell = rdfsInfoMap.getEdgeInfo(cell);
		RDFSInfo info = rdfsInfoMap.getCellInfo(propCell);
		String prop = (String) prevReplaceMap.get(info.getURIStr());
		if (prop == null) {
			rdfsInfoMap.putEdgeInfo(cell, gmanager.getPropertyCell(MR3Resource.Nil, false));
		} else {
			rdfsInfoMap.putEdgeInfo(cell, gmanager.getPropertyCell(ResourceFactory.createResource(prop), false));
		}
	}

	private void replaceClassList() {
		Map prevReplaceMap = getPrevReplaceMap(prevClassListModel, replaceClassListModel);
		RDFGraph graph = gmanager.getRDFGraph();
		Object[] cells = graph.getAllCells();
		for (int i = 0; i < cells.length; i++) {
			Object cell = cells[i];
			if (graph.isRDFResourceCell(cell)) {
				replaceRDFResourceType(cell, prevReplaceMap);
			}
		}
	}

	private void replacePropertyList() {
		Map prevReplaceMap = getPrevReplaceMap(prevPropertyListModel, replacePropertyListModel);
		RDFGraph graph = gmanager.getRDFGraph();
		Object[] cells = graph.getAllCells();
		for (int i = 0; i < cells.length; i++) {
			Object cell = cells[i];
			if (graph.isRDFPropertyCell(cell)) {
				replaceRDFProperty(cell, prevReplaceMap);
			}
		}
	}

	private boolean isContainsListModel(String uri, DefaultListModel prevListModel, DefaultListModel replaceListModel) {
		return prevListModel.contains(uri) && !replaceListModel.contains(uri);
	}

	private void removeCurrentClass() {
		RDFGraph graph = gmanager.getClassGraph();
		Object[] cells = graph.getAllCells();
		graph.clearSelection();
		for (int i = 0; i < cells.length; i++) {
			Object cell = cells[i];
			if (graph.isRDFSClassCell(cell)) {
				ClassInfo info = (ClassInfo) rdfsInfoMap.getCellInfo(cell);
				//				ラベルとコメントを消すべきか？
				info.clearSubClass();
				info.clearSupClass();
				if (isContainsListModel(info.getURIStr(), prevClassListModel, replaceClassListModel)) {
					graph.addSelectionCell(cell);
				}
			} else if (graph.isEdge(cell)) {
				graph.addSelectionCell(cell);
			}
		}
		graph.removeCellsWithEdges(graph.getAllSelectedCells());
	}

	private void removeCurrentProperty() {
		RDFGraph graph = gmanager.getPropertyGraph();
		Object[] cells = graph.getAllCells();
		graph.clearSelection();
		for (int i = 0; i < cells.length; i++) {
			Object cell = cells[i];
			if (graph.isRDFSPropertyCell(cell)) {
				PropertyInfo info = (PropertyInfo) rdfsInfoMap.getCellInfo(cell);
				info.clearSubProperty();
				info.clearSupProperty();
				info.clearDomain();
				info.clearRange();
				if (isContainsListModel(info.getURIStr(), prevPropertyListModel, replacePropertyListModel)) {
					graph.addSelectionCell(cell);
				}
			} else if (graph.isEdge(cell)) {
				graph.addSelectionCell(cell);
			}
		}
		graph.removeCellsWithEdges(graph.getAllSelectedCells());
	}

	private void apply() {
		replaceClassList();
		replacePropertyList();
		removeCurrentClass();
		removeCurrentProperty();
		isApply = true;
		setVisible(false);
	}

	private void cancel() {
		isApply = false;
		setVisible(false);
	}

	public boolean isApply() {
		return isApply;
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == prevClassUpButton) {
			listUp(prevClassList);
		} else if (e.getSource() == prevClassDownButton) {
			listDown(prevClassList);
		} else if (e.getSource() == replaceClassUpButton) {
			listUp(replaceClassList);
		} else if (e.getSource() == replaceClassDownButton) {
			listDown(replaceClassList);
		} else if (e.getSource() == prevPropertyUpButton) {
			listUp(prevPropertyList);
		} else if (e.getSource() == prevPropertyDownButton) {
			listDown(prevPropertyList);
		} else if (e.getSource() == replacePropertyUpButton) {
			listUp(replacePropertyList);
		} else if (e.getSource() == replacePropertyDownButton) {
			listDown(replacePropertyList);
		} else if (e.getSource() == applyButton) {
			apply();
		} else if (e.getSource() == cancelButton) {
			cancel();
		}
	}

}
