package mr3.ui;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;
import javax.swing.table.*;

import mr3.data.*;
import mr3.jgraph.*;

/**
 * @author takeshi morita
 *
 */
public class ReferenceListPanel extends JPanel {

	private JTable rdfRefTable;
	private JTable propRefTable;
	private JTabbedPane tab;
	private GraphManager gmanager;
	private RDFResourceInfoMap resInfoMap = RDFResourceInfoMap.getInstance();
	private RDFSInfoMap rdfsInfoMap = RDFSInfoMap.getInstance();
	private static TableModel nullTableModel;

	private JButton selectAllButton;
	private JButton clearAllButton;
	private JButton reverseButton;
	private JButton jumpButton;

	private GridBagLayout gbLayout;
	private GridBagConstraints gbc;

	private Map rdfTableModelMap;
	private Map propTableModelMap;

	private static final int listWidth = 380;
	private static final int listHeight = 70;

	ReferenceListPanel(GraphManager manager) {
		gmanager = manager;
		rdfTableModelMap = new HashMap();
		propTableModelMap = new HashMap();

		initTable();
		initGridLayout();
		tab = new JTabbedPane();
		setTableLayout(rdfRefTable, "RDF");
		setTableLayout(propRefTable, "Property");
		gbLayout.setConstraints(tab, gbc);
		add(tab);
		setButtonLayout();
		setBorder(BorderFactory.createTitledBorder("Reference List"));
		setSize(new Dimension(400, 300));
	}

	private void initGridLayout() {
		gbLayout = new GridBagLayout();
		gbc = new GridBagConstraints();
		setLayout(gbLayout);
		gbc.gridwidth = GridBagConstraints.REMAINDER;
	}

	private void initTable() {
		nullTableModel = getTableModel();
		rdfRefTable = new JTable(getTableModel());
		rdfRefTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		propRefTable = new JTable(getTableModel());
		propRefTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		setTableColumnModel(rdfRefTable);
		setTableColumnModel(propRefTable);
	}

	private void setTableColumnModel(JTable table) {
		TableColumnModel tcModel = table.getColumnModel();
		tcModel.getColumn(0).setPreferredWidth(50);
		tcModel.getColumn(1).setPreferredWidth(300);
	}

	private void setButtonLayout() {
		ButtonAction buttonAction = new ButtonAction();
		selectAllButton = new JButton("Select All");
		selectAllButton.addActionListener(buttonAction);
		clearAllButton = new JButton("Clear All");
		clearAllButton.addActionListener(buttonAction);
		reverseButton = new JButton("Reverse");
		reverseButton.addActionListener(buttonAction);
		jumpButton = new JButton("Jump");
		jumpButton.addActionListener(buttonAction);
		JPanel inlinePanel = new JPanel();
		inlinePanel.add(selectAllButton);
		inlinePanel.add(clearAllButton);
		inlinePanel.add(reverseButton);
		inlinePanel.add(jumpButton);
		gbLayout.setConstraints(inlinePanel, gbc);
		add(inlinePanel);
	}

	private void setCheck(TableModel tableModel, boolean t) {
		if (tableModel.getRowCount() <= 0) {
			return;
		}
		for (int i = 0; i < tableModel.getRowCount(); i++) {
			tableModel.setValueAt(new Boolean(t), i, 0);
		}
	}

	private void checkAll() {
		setCheck(rdfRefTable.getModel(), true);
		setCheck(propRefTable.getModel(), true);
	}

	private void clearCheckAll() {
		setCheck(rdfRefTable.getModel(), false);
		setCheck(propRefTable.getModel(), false);
	}

	private void checkReverse(TableModel tableModel) {
		if (tableModel.getRowCount() <= 0) {
			return;
		}
		for (int i = 0; i < tableModel.getRowCount(); i++) {
			Boolean t = (Boolean) tableModel.getValueAt(i, 0);
			tableModel.setValueAt(new Boolean(!t.booleanValue()), i, 0);
		}
	}

	private void rdfJump() {
		int row = rdfRefTable.getSelectedRow();
		if (row == -1) { //選択されていない場合
			return;
		}
		Object cell = rdfRefTable.getValueAt(row, 1);
		gmanager.jumpRDFArea(cell);
		gmanager.setVisiblePropWindow(true);
	}

	private void propJump() {
		int row = propRefTable.getSelectedRow();
		if (row == -1) { //選択されていない場合
			return;
		}
		Object cell = propRefTable.getValueAt(row, 1);
		gmanager.jumpPropertyArea(cell);
		gmanager.setVisiblePropWindow(true);
	}

	class ButtonAction extends AbstractAction {
		public void actionPerformed(ActionEvent e) {

			if (e.getSource() == selectAllButton) {
				checkAll();
			} else if (e.getSource() == clearAllButton) {
				clearCheckAll();
			} else if (e.getSource() == reverseButton) {
				checkReverse(rdfRefTable.getModel());
				checkReverse(propRefTable.getModel());
			} else if (e.getSource() == jumpButton) {
//				System.out.println(tab.getSelectedIndex());
				if (tab.getSelectedIndex() == 0) {
					rdfJump();
				} else if (tab.getSelectedIndex() == 1){
					propJump();
				}				
			}
		}
	}

	private void setTableLayout(JTable table, String title) {
		JScrollPane tableScroll = new JScrollPane(table);
		tableScroll.setPreferredSize(new Dimension(listWidth, listHeight));
		tableScroll.setMinimumSize(new Dimension(listWidth, listHeight));
		tab.addTab(title, tableScroll);
	}

	private ReferenceTableModel getTableModel() {
		Object[] columnNames = new Object[] { "check", "List" };
		ReferenceTableModel tModel = new ReferenceTableModel(columnNames, 0);
		return tModel;
	}

	private void replaceRDFRefTableModel(Object cell) {
		TableModel tableModel = (TableModel) rdfTableModelMap.get(cell);
		if (tableModel != null) {
			rdfRefTable.setModel(tableModel);
			setTableColumnModel(rdfRefTable);
		}
	}

	private void replacePropRefTableModel(Object cell) {
		TableModel tableModel = (TableModel) propTableModelMap.get(cell);
		if (tableModel != null) {
			propRefTable.setModel(tableModel);
			setTableColumnModel(propRefTable);
		}
	}

	public void replaceTableModel(Object cell) {
		replaceRDFRefTableModel(cell);
		replacePropRefTableModel(cell);
	}

	public void removeRefRDFAction(Object cell) {
		TableModel tableModel = (TableModel) rdfTableModelMap.get(cell);
		if (tableModel == null) {
			return;
		}
		for (int i = 0; i < tableModel.getRowCount(); i++) {
			if (isAvailable(tableModel, i, 0)) {
				Object rdfCell = tableModel.getValueAt(i, 1);
				RDFResourceInfo info = resInfoMap.getCellInfo(rdfCell);
				if (info != null) { // Typeをnullに変更
					info.setType(null);
					gmanager.changeCellView();
				} else {
					rdfsInfoMap.putEdgeInfo(rdfCell, null);
					gmanager.changeCellView();
				}
			}
		}
	}

	public void removeRefPropAction(Object cell) {
		TableModel tableModel = (TableModel) propTableModelMap.get(cell);
		if (tableModel == null) {
			return;
		}

		for (int i = 0; i < tableModel.getRowCount(); i++) {
			if (isAvailable(tableModel, i, 0)) {
				Object propertyCell = tableModel.getValueAt(i, 1);
				PropertyInfo info = (PropertyInfo) rdfsInfoMap.getCellInfo(propertyCell);
//				System.out.println("PropertyCell: " + propertyCell);
//				System.out.println("RDFSCell: " + cell);
				info.removeDomain(cell);
				info.removeRange(cell);
			}
		}
	}

	//	チェックがついているものにデフォルトの処理	
	public void removeAction(Object cell) {
		removeRefRDFAction(cell);
		removeRefPropAction(cell);
	}

	private Object getTableModel(Object cell, Map map) {
		DefaultTableModel tableModel = getTableModel();
		Set set = (Set) map.get(cell);
		if (set == null) {
			return null;
		}
		for (Iterator i = set.iterator(); i.hasNext();) {
			Object[] list = new Object[] { new Boolean(true), i.next()};
			tableModel.addRow(list);
		}
		return tableModel;
	}

	public void setTableModelMap(Set cells, Map classRDFMap, Map classPropMap) {
		rdfRefTable.setModel(nullTableModel);
		propRefTable.setModel(nullTableModel);
		for (Iterator i = cells.iterator(); i.hasNext();) {
			Object cell = i.next();

			Object tModel = getTableModel(cell, classRDFMap);
			rdfTableModelMap.put(cell, tModel);

			tModel = getTableModel(cell, classPropMap);
			propTableModelMap.put(cell, tModel);
		}
	}

	private boolean isAvailable(TableModel tableModel, int row, int column) {
		Boolean isAvailable = (Boolean) tableModel.getValueAt(row, column);
		return isAvailable.booleanValue();
	}

	class ReferenceTableModel extends DefaultTableModel {

		public ReferenceTableModel(Object[] columnNames, int rowCount) {
			super(columnNames, rowCount);
		}

		public boolean isCellEditable(int row, int col) {
			return col == 0 ? true : false;
		}

		public Class getColumnClass(int column) {
			Vector v = (Vector) dataVector.elementAt(0);
			return v.elementAt(column).getClass();
		}
	}
}
