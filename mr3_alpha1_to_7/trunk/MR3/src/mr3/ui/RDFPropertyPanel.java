package mr3.ui;
import java.awt.*;
import java.awt.event.*;
import java.net.*;
import java.util.*;
import java.util.List;

import javax.swing.*;
import javax.swing.event.*;

import mr3.data.*;
import mr3.jgraph.*;
import mr3.util.*;

import com.hp.hpl.mesa.rdf.jena.common.*;
import com.hp.hpl.mesa.rdf.jena.model.*;
import com.jgraph.graph.*;

public class RDFPropertyPanel extends JPanel implements ActionListener, ListSelectionListener {

	private JComboBox uriPrefixBox;
	private JTextField idField;
	private JLabel nsLabel;
	private JButton apply;
	private JButton close;
	private JButton jumpRDFSProp;
	private GraphCell edge;

	private JList localNameList;
	private Map propMap;
	private Set propNameSpaceSet;
	private IconCellRenderer renderer;
	private static Object[] NULL = new Object[0];

	private List propList;
	private List validPropList;
	private GraphManager gmanager;
	private RDFSInfoMap rdfsInfoMap = RDFSInfoMap.getInstance();

	private static final int boxWidth = 120;
	private static final int boxHeight = 50;
	private static final int listWidth = 350;
	private static final int listHeight = 40;

	public RDFPropertyPanel(GraphManager manager) {
		gmanager = manager;
		setBorder(BorderFactory.createTitledBorder("RDF Property Attributes"));

		uriPrefixBox = new JComboBox();
		uriPrefixBox.addActionListener(new ChangePrefixAction());
		initComponent(uriPrefixBox, "Prefix", boxWidth, boxHeight);

		idField = new JTextField();
		initComponent(idField, "ID", boxWidth, listHeight);
		idField.addActionListener(this);

		jumpRDFSProp = new JButton("Jump RDFS");
		jumpRDFSProp.addActionListener(this);

		JPanel uriPanel = new JPanel();
		uriPanel.add(uriPrefixBox);
		uriPanel.add(idField);
		uriPanel.add(jumpRDFSProp);

		nsLabel = new JLabel();
		initComponent(nsLabel, "NameSpace", listWidth, listHeight);

		apply = new JButton("Apply");
		apply.addActionListener(this);
		close = new JButton("Close");
		close.addActionListener(this);
		JPanel buttonGroup = new JPanel();
		buttonGroup.add(apply);
		buttonGroup.add(close);

		GridBagLayout gridbag = new GridBagLayout();
		GridBagConstraints c = new GridBagConstraints();
		setLayout(gridbag);
		c.weighty = 3;
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.anchor = GridBagConstraints.CENTER;
		gridbag.setConstraints(uriPanel, c);
		add(uriPanel);
		gridbag.setConstraints(nsLabel, c);
		add(nsLabel);
		Component selectPropertyPanel = createSelectPropertyPanel();
		gridbag.setConstraints(selectPropertyPanel, c);
		add(selectPropertyPanel);
		gridbag.setConstraints(buttonGroup, c);
		add(buttonGroup);
	}

	private void initComponent(JComponent component, String title, int width, int height) {
		component.setPreferredSize(new Dimension(width, height));
		component.setMinimumSize(new Dimension(width, height));
		component.setBorder(BorderFactory.createTitledBorder(title));
	}

	class ChangePrefixAction extends AbstractAction {
		public void actionPerformed(ActionEvent e) {
			PrefixNSUtil.replacePrefix((String) uriPrefixBox.getSelectedItem(), nsLabel);
			selectNameSpaceList();
		}
	}

	private Component createSelectPropertyPanel() {
		localNameList = new JList();
		localNameList.addListSelectionListener(this);
		JScrollPane localNameListScroll = new JScrollPane(localNameList);
		initComponent(localNameListScroll, "Property ID", 350, 200);

		JPanel panel = new JPanel();
		GridBagLayout gridbag = new GridBagLayout();
		GridBagConstraints c = new GridBagConstraints();
		panel.setLayout(gridbag);
		c.anchor = GridBagConstraints.PAGE_START;
		c.gridwidth = GridBagConstraints.REMAINDER;
		gridbag.setConstraints(localNameListScroll, c);

		renderer = new IconCellRenderer();
		localNameList.setCellRenderer(renderer);

		panel.add(localNameListScroll);

		return panel;
	}

	private static final String NULL_LOCAL_NAME = "(Null)";

	private void setPrefix() {
		PrefixNSUtil.setPrefixNSInfoSet(gmanager.getPrefixNSInfoSet());
		uriPrefixBox.setModel(new DefaultComboBoxModel(PrefixNSUtil.getPrefixes().toArray()));
		for (Iterator i = gmanager.getPrefixNSInfoSet().iterator(); i.hasNext();) {
			PrefixNSInfo prefNSInfo = (PrefixNSInfo) i.next();
			if (prefNSInfo.getNameSpace().equals(nsLabel.getText())) {
				uriPrefixBox.setSelectedItem(prefNSInfo.getPrefix());
				break;
			}
		}
	}

	private void selectNameSpaceList() {

		if (propMap == null) {
			return;
		}

		if (!localNameList.isSelectionEmpty()) {
			localNameList.clearSelection();
		}

		String nameSpace = nsLabel.getText();
		Set localNames = (Set) propMap.get(nameSpace);
		if (localNames == null) {
			localNameList.setListData(NULL);
			return;
		}
		Set modifyLocalNames = new HashSet();
		for (Iterator i = localNames.iterator(); i.hasNext();) {
			String localName = (String) i.next();
			if (localName.length() == 0) { // localNameがない場合，Nullを表示
				modifyLocalNames.add(NULL_LOCAL_NAME);
			} else {
				modifyLocalNames.add(localName);
			}
		}
		setRenderer(nameSpace, modifyLocalNames);
		localNameList.setListData(modifyLocalNames.toArray());
	}

	private void setNSLabel(String str) {
		nsLabel.setText(str);
		nsLabel.setToolTipText(str);
	}

	private void selectLocalNameList() {
		if (localNameList.getSelectedValue() != null) {
			String ln = localNameList.getSelectedValue().toString();
			if (ln.equals(NULL_LOCAL_NAME)) {
				ln = "";
			}
			idField.setText(ln);
		}
	}

	private static Boolean TRUE = new Boolean(true);
	private static Boolean FALSE = new Boolean(false);

	private void setRenderer(String nameSpace, Set localNames) {
		List list = new ArrayList();
		for (Iterator i = localNames.iterator(); i.hasNext();) {
			String uri = nameSpace + i.next();
			Resource res = new ResourceImpl(uri);
			if (validPropList.contains(res)) {
				list.add(TRUE);
			} else {
				list.add(FALSE);
			}
		}
		renderer.setValidConfirmList(list);
	}

	public void valueChanged(ListSelectionEvent e) {
		try {
			if (e.getSource() == localNameList) {
				selectLocalNameList();
			}
		} catch (NullPointerException np) { //あとではずす
			np.printStackTrace();
		}
	}

	public void showPropertyInfo(GraphCell c) {
		edge = c;
		Object propertyCell = rdfsInfoMap.getEdgeInfo(edge);
		if (propertyCell == null) {
			setNSLabel(MR3Resource.Nil.getNameSpace());
			idField.setText(MR3Resource.Nil.getLocalName());
			changeProperty();
		} else {
			RDFSInfo info = rdfsInfoMap.getCellInfo(propertyCell);
			setNSLabel(info.getNameSpace());
			idField.setText(info.getLocalName());
		}
		setPrefix();
	}

	public void setValidPropertyList(List list) {
		validPropList = list;
	}

	public void setPropertyList(List plist, List vlist) {
		propList = plist;
		validPropList = vlist;
		propMap = new HashMap();
		propNameSpaceSet = new HashSet();

		for (Iterator i = propList.iterator(); i.hasNext();) {
			RDFSInfo info = rdfsInfoMap.getCellInfo(i.next());
			Resource uri = info.getURI();
			propNameSpaceSet.add(uri.getNameSpace());
			Set localNames = (Set) propMap.get(uri.getNameSpace());
			if (localNames == null) {
				localNames = new HashSet();
				propMap.put(uri.getNameSpace(), localNames);
			}
			localNames.add(uri.getLocalName());
		}

		//		localNameList.setListData(NULL);
		localNameList.setSelectedValue(idField.getText(), true);
	}

	private static final String selectSupPropertiesTitle = "Select Super Properties";

	private String getURI() {
		return nsLabel.getText() + idField.getText();
	}

	private void changeProperty() {
		GraphCell propertyCell = null;
		Resource uri = new ResourceImpl(getURI());

		if (gmanager.isEmptyURI(uri.getURI())) {
			return;
		}
		if (rdfsInfoMap.isPropertyCell(uri)) {
			propertyCell = (GraphCell) gmanager.getPropertyCell(uri, false);
		} else {
			if (gmanager.isDuplicatedWithDialog(uri.getURI(), null, GraphType.PROPERTY)) {
				return;
			}
			if (uri.equals(MR3Resource.Nil)) {
				propertyCell = (GraphCell) gmanager.getPropertyCell(uri, false);
			} else {
				SelectRDFSCheckDialog dialog = new SelectRDFSCheckDialog("Choose One Select");
				CreateRDFSType createType = (CreateRDFSType) dialog.getValue();
				if (createType == CreateRDFSType.CREATE) {
					Set supProps = gmanager.getSupRDFS(gmanager.getPropertyGraph(), selectSupPropertiesTitle);
					propertyCell = (GraphCell) gmanager.insertSubRDFS(uri, supProps, gmanager.getPropertyGraph());
				} else if (createType == CreateRDFSType.RENAME) {
					propertyCell = (GraphCell) rdfsInfoMap.getEdgeInfo(edge);
					RDFSInfo rdfsInfo = rdfsInfoMap.getCellInfo(propertyCell);
					rdfsInfoMap.removeURICellMap(rdfsInfo);
					rdfsInfo.setURI(uri.getURI());
					rdfsInfoMap.putURICellMap(rdfsInfo, propertyCell);
				} else {
					return;
				}
			}
		}

		gmanager.jumpPropertyArea(propertyCell); // 対応するRDFSプロパティを選択する

		rdfsInfoMap.putEdgeInfo(edge, propertyCell);
		String propValue = gmanager.getPropertyGraph().convertValueToString(propertyCell);
		gmanager.setCellValue(edge, propValue);
		gmanager.changeCellView();
	}

	private void jumpRDFSProperty() {
		Resource uri = new ResourceImpl(nsLabel.getText() + idField.getText());
		if (gmanager.isEmptyURI(uri.getURI())) {
			return;
		}
		if (rdfsInfoMap.isPropertyCell(uri)) {
			Object propertyCell = (GraphCell) rdfsInfoMap.getPropertyCell(uri);
			gmanager.jumpPropertyArea(propertyCell);
		} else {
			JOptionPane.showInternalMessageDialog(gmanager.getDesktop(), "Not Defined", "Warning", JOptionPane.ERROR_MESSAGE);
		}
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == apply || e.getSource() == idField) {
			if (edge != null) {
				changeProperty();
				gmanager.getRDFGraph().setSelectionCell(edge); // jumpだとうまくいかなかった．
			}
		} else if (e.getSource() == jumpRDFSProp) {
			jumpRDFSProperty();
		} else if (e.getSource() == close) {
			gmanager.setVisibleAttrDialog(false);
		}
	}

	/** イメージ付きリストを描画 */
	class IconCellRenderer extends JLabel implements ListCellRenderer {

		private List validConfirmList;

		IconCellRenderer() {
			setOpaque(true);
		}

		public void setValidConfirmList(List list) {
			validConfirmList = list;
		}

		public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {

			String str = value.toString();
			setText(str);

			Boolean isValid = (Boolean) validConfirmList.get(index);
			if (isValid.booleanValue()) {
				URL valid = this.getClass().getClassLoader().getResource("mr3/resources/valid.gif");
				setIcon(new ImageIcon(valid));
			} else {
				setIcon(null);
			}

			if (isSelected) {
				setBackground(list.getSelectionBackground());
				setForeground(list.getSelectionForeground());
			} else {
				setBackground(list.getBackground());
				setForeground(list.getForeground());
			}
			setEnabled(list.isEnabled());
			setFont(list.getFont());

			return this;
		}
	}
}
