package mr3.ui;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.List;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;

import mr3.data.*;
import mr3.jgraph.*;

import com.hp.hpl.mesa.rdf.jena.vocabulary.*;

/**
 *
 * 名前空間と接頭辞の対応付けをテーブルで行う
 * チェックにより，Class, Property, Resourceの名前空間を接頭辞で置き換える
 * 接頭辞の名前変更はテーブルから行うことができる
 *
 * @auther takeshi morita
 */

public class NameSpaceTableDialog extends JInternalFrame implements ActionListener, TableModelListener, Serializable {

	private Map prefixNSMap;
	private JTable nsTable;
	private NSTableModel nsTableModel;

	private static final long serialVersionUID = 5974381131839067739L;

	transient private JButton addNSButton;
	transient private JButton removeNSButton;
	//	transient private JButton getNSButton;
	transient private JButton closeButton;
	transient private JTextField prefixField;
	transient private JTextField nsField;

	transient private GridBagLayout gbLayout;
	transient private GridBagConstraints gbc;
	transient private JPanel inlinePanel;

	transient private GraphManager gmanager;
	transient private SelectNameSpaceDialog nsDialog;
	transient private JCheckBoxMenuItem showNSTable;

	public NameSpaceTableDialog(GraphManager manager) {
		super("NameSpace Table", false, true, false);

		gmanager = manager;
		prefixNSMap = new HashMap();
		nsDialog = new SelectNameSpaceDialog(gmanager, prefixNSMap);
		initTable();
		inlinePanel = new JPanel();
		initGridLayout();
		setTableLayout();
		setInputLayout();
		showNSTable = new JCheckBoxMenuItem("Show NameSpace Table", true);
		showNSTable.addActionListener(new CloseNSTableAction());
		getContentPane().add(inlinePanel);

		URL nsDialogUrl = this.getClass().getClassLoader().getResource("mr3/resources/nameSpaceTableIcon.gif");
		setFrameIcon(new ImageIcon(nsDialogUrl));
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		addInternalFrameListener(new InternalFrameAdapter() {
			public void internalFrameClosing(InternalFrameEvent e) {
				setVisible(false);
			}
		});

		setDefaultNSPrefix();
		setSize(new Dimension(750, 210));
		setLocation(10, 100);
		setVisible(false);
	}

	//	baseURIがrdf, rdfs, mr3の場合があるため
	private void addDefaultNS(String prefix, String addNS) {
		if (!isValidPrefix(prefix)) {
			prefix = getMR3Prefix(addNS);
		}
		if (isValidNS(addNS)) {
			addNameSpaceTable(new Boolean(true), prefix, addNS);
		}
	}

	public void setDefaultNSPrefix() {
		addDefaultNS("mr3", MR3Resource.getURI());
		addDefaultNS("base", gmanager.getBaseURI());
		addDefaultNS("rdf", RDF.getURI());
		addDefaultNS("rdfs", RDFS.getURI());
		changeCellView();
	}

	private String getKnownPrefix(String ns) {
		if (ns.equals("http://purl.org/dc/elements/1.1/")) {
			return "dc";
		} else if (ns.equals("http://purl.org/rss/1.0/")) {
			return "rss";
		} else if (ns.equals("http://xmlns.com/foaf/0.1/")) {
			return "foaf";
		} else {
			return "prefix";
		}
	}

	private String getMR3Prefix(String ns) {
		String nextPrefix = getKnownPrefix(ns);
		for (int i = 0; true; i++) {
			String cnt = Integer.toString(i);
			if (isValidPrefix(nextPrefix + "_" + cnt)) {
				nextPrefix = nextPrefix +  "_" + cnt;
				break;
			}
		}
		return nextPrefix;
	}

	public void setCurrentNSPrefix() {
		Set allNSSet = gmanager.getAllNameSpaceSet();
		for (Iterator i = allNSSet.iterator(); i.hasNext();) {
			String ns = (String) i.next();
			if (isValidNS(ns)) {
				String knownPrefix = getKnownPrefix(ns);
				if (isValidPrefix(knownPrefix) && (!knownPrefix.equals("prefix"))) {
					addNameSpaceTable(new Boolean(true), knownPrefix, ns);
				} else {
					addNameSpaceTable(new Boolean(true), getMR3Prefix(ns), ns);
				}
			}
		}
		changeCellView();
	}

	public NSTableModel getNSTableModel() {
		return nsTableModel;
	}

	public Serializable getState() {
		ArrayList list = new ArrayList();
		list.add(prefixNSMap);
		list.add(nsTableModel);
		return list;
	}

	public void loadState(List list) {
		Map map = (Map) list.get(0);
		NSTableModel model = (NSTableModel) list.get(1);
		for (int i = 0; i < model.getRowCount(); i++) {
			Boolean isAvailable = (Boolean) model.getValueAt(i, 0);
			String prefix = (String) model.getValueAt(i, 1);
			String ns = (String) model.getValueAt(i, 2);
			if (isValidPrefix(prefix) && isValidNS(ns)) {
				addNameSpaceTable(isAvailable, prefix, ns);
			}
		}
		// ここでprefixNSMapを設定しないと，上の内容を元に戻すことができない．(non validとなる）
		prefixNSMap.putAll(map);
		changeCellView();
	}

	class CloseNSTableAction extends AbstractAction {
		public void actionPerformed(ActionEvent e) {
			setVisible(showNSTable.getState());
		}
	}

	public void setVisible(boolean b) {
		super.setVisible(b);
		if (showNSTable != null) {
			showNSTable.setState(b);
		}
	}

	public JCheckBoxMenuItem getShowNSTable() {
		return showNSTable;
	}

	public void resetNSTable() {
		prefixNSMap = new HashMap();
		// 一気にすべて削除する方法がわからない．
		while (nsTableModel.getRowCount() != 0) {
			nsTableModel.removeRow(nsTableModel.getRowCount() - 1);
		}
		gmanager.setPrefixNSInfoSet(new HashSet());
	}

	private void initTable() {
		Object[] columnNames = new Object[] { "available", "prefix", "URI" };
		nsTableModel = new NSTableModel(columnNames, 0);
		nsTableModel.addTableModelListener(this);
		nsTable = new JTable(nsTableModel);
		nsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		TableColumnModel tcModel = nsTable.getColumnModel();
		tcModel.getColumn(0).setPreferredWidth(50);
		tcModel.getColumn(1).setPreferredWidth(100);
		tcModel.getColumn(2).setPreferredWidth(450);
	}

	private void initGridLayout() {
		gbLayout = new GridBagLayout();
		gbc = new GridBagConstraints();
		inlinePanel.setLayout(gbLayout);
		gbc.gridwidth = GridBagConstraints.REMAINDER;
	}

	private void setTableLayout() {
		JScrollPane nsTableScroll = new JScrollPane(nsTable);
		nsTableScroll.setPreferredSize(new Dimension(700, 115));
		nsTableScroll.setMinimumSize(new Dimension(700, 115));
		gbLayout.setConstraints(nsTableScroll, gbc);
		inlinePanel.add(nsTableScroll);
	}

	private void setInputLayout() {
		addNSButton = new JButton("＋");
		addNSButton.addActionListener(this);

		removeNSButton = new JButton("―");
		removeNSButton.addActionListener(this);

		//		getNSButton = new JButton("GetNS");
		//		getNSButton.addActionListener(this);

		closeButton = new JButton("Close");
		closeButton.addActionListener(this);

		prefixField = new JTextField(8);
		prefixField.setBorder(BorderFactory.createTitledBorder("Prefix"));
		prefixField.setPreferredSize(new Dimension(50, 40));
		prefixField.setMinimumSize(new Dimension(50, 40));

		nsField = new JTextField(30);
		nsField.setBorder(BorderFactory.createTitledBorder("NameSpace"));
		nsField.setPreferredSize(new Dimension(400, 40));
		nsField.setMinimumSize(new Dimension(400, 40));

		JPanel inline = new JPanel();
		inline.add(prefixField);
		inline.add(nsField);
		inline.add(addNSButton);
		inline.add(removeNSButton);
		//		inline.add(getNSButton);
		inline.add(closeButton);
		gbLayout.setConstraints(inline, gbc);
		inlinePanel.add(inline);
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == addNSButton) {
			addNameSpaceTable(new Boolean(true), prefixField.getText(), nsField.getText());
			changeCellView();
		} else if (e.getSource() == removeNSButton) {
			removeNameSpaceTable();
			//		} else if (e.getSource() == getNSButton) {
			//			getNameSpace();
		} else if (e.getSource() == closeButton) {
			setVisible(false);
		}
	}

	private boolean isValidPrefix(String prefix) {
		Set keySet = prefixNSMap.keySet();
		return (!keySet.contains(prefix) && !prefix.equals(""));
	}

	private boolean isValidNS(String ns) {
		Collection values = prefixNSMap.values();
		return (ns != null && !ns.equals("") && !values.contains(ns));
	}

	/** prefix が空でなくかつ，すでに登録されていない場合true */
	private boolean isValidPrefixWithWarning(String prefix) {
		if (isValidPrefix(prefix)) {
			return true;
		} else {
			JOptionPane.showInternalMessageDialog(gmanager.getDesktop(), "Prefix is not valid.", "Warning", JOptionPane.ERROR_MESSAGE);
			return false;
		}
	}

	/** nsが空でもnullでもなく，すでに登録されてない場合 true */
	private boolean isValidNSWithWarning(String ns) {
		if (isValidNS(ns)) {
			return true;
		} else {
			JOptionPane.showInternalMessageDialog(gmanager.getDesktop(), "NameSpace is not valid.", "Warning", JOptionPane.ERROR_MESSAGE);
			return false;
		}
	}

	public void addNameSpaceTable(Boolean isAvailable, String prefix, String ns) {
		if (isValidPrefixWithWarning(prefix) && isValidNSWithWarning(ns)) {
			prefixNSMap.put(prefix, ns);
			Object[] list = new Object[] { isAvailable, prefix, ns };
			nsTableModel.insertRow(nsTableModel.getRowCount(), list);
			prefixField.setText("");
			nsField.setText("");
		}
	}

	private void removeNameSpaceTable() {
		int[] removeList = nsTable.getSelectedRows();
		int length = removeList.length;
		// どうやったら，複数のrowを消すせるのかがよくわからない．
		// modelから消した時点でrow番号が変わってしまうのが原因
		if (length == 0) {
			return;
		}
		int row = removeList[0];
		String rmPrefix = (String) nsTableModel.getValueAt(row, 1);
		String rmNS = (String) nsTableModel.getValueAt(row, 2);
		if (rmNS.equals(gmanager.getBaseURI())) {
			JOptionPane.showInternalMessageDialog(gmanager.getDesktop(), "This NameSpace is baseURI.", "Warning", JOptionPane.ERROR_MESSAGE);
			return;
		}
		if (rmNS.equals(MR3Resource.Default_URI.getNameSpace())) {
			JOptionPane.showInternalMessageDialog(gmanager.getDesktop(), "This NameSpace is System URI.", "Warning", JOptionPane.ERROR_MESSAGE);
			return;
		}
		if (!gmanager.getAllNameSpaceSet().contains(rmNS)) {
			prefixNSMap.remove(rmPrefix);
			nsTableModel.removeRow(row);
			changeCellView();
		} else {
			JOptionPane.showInternalMessageDialog(gmanager.getDesktop(), "This NameSpace is used.", "Warning", JOptionPane.ERROR_MESSAGE);
		}
	}

	private void changeCellView() {
		gmanager.setPrefixNSInfoSet(getPrefixNSInfoSet());
		gmanager.changeCellView();
	}

	private void getNameSpace() {
		nsDialog.setVisible(true);
		String uri = (String) nsDialog.getValue();
		if (uri != null) {
			nsField.setText(uri);
			nsField.setToolTipText(uri);
		}
	}

	private boolean isCheckBoxChanged(int type, int column) {
		return (type == TableModelEvent.UPDATE && column == 0);
	}

	/** テーブルのチェックボックスがチェックされたかどうか */
	private boolean isPrefixAvailable(int row, int column) {
		Boolean isPrefixAvailable = (Boolean) nsTableModel.getValueAt(row, column);
		return isPrefixAvailable.booleanValue();
	}

	public void tableChanged(TableModelEvent e) {
		int row = e.getFirstRow();
		int column = e.getColumn();
		int type = e.getType();

		if (isCheckBoxChanged(type, column)) {
			changeCellView();
		}
	}

	private Set getPrefixNSInfoSet() {
		Set infoSet = new HashSet();
		for (int i = 0; i < nsTableModel.getRowCount(); i++) {
			String prefix = (String) nsTableModel.getValueAt(i, 1);
			String ns = (String) nsTableModel.getValueAt(i, 2);
			infoSet.add(new PrefixNSInfo(prefix, ns, isPrefixAvailable(i, 0)));
		}
		return infoSet;
	}

	public class NSTableModel extends DefaultTableModel implements Serializable {

		private static final long serialVersionUID = -5977304717491874293L;

		public NSTableModel(Object[] columnNames, int rowCount) {
			super(columnNames, rowCount);
		}

		public boolean isCellEditable(int row, int col) {
			if (col == 2)
				return false;
			return true;
		}

		public Class getColumnClass(int column) {
			Vector v = (Vector) dataVector.elementAt(0);
			return v.elementAt(column).getClass();
		}

		public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
			if (aValue instanceof String) {
				String prefix = (String) aValue;
				// 多分prefixのチェックはいらない．
				String oldPrefix = (String) nsTableModel.getValueAt(rowIndex, columnIndex);
				prefixNSMap.remove(oldPrefix);
				String ns = (String) nsTableModel.getValueAt(rowIndex, 2);
				prefixNSMap.put(prefix, ns);
			}
			super.setValueAt(aValue, rowIndex, columnIndex);
			changeCellView();
		}
	}

}
