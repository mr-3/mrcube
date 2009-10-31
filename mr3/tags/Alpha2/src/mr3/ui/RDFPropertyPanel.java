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

import com.hp.hpl.mesa.rdf.jena.common.*;
import com.hp.hpl.mesa.rdf.jena.model.*;
import com.jgraph.graph.*;

public class RDFPropertyPanel extends JPanel implements ActionListener, ListSelectionListener {

	private JTextField uriField;
	private JButton apply;
	private JButton close;
	private JButton jumpRDFSProp;
	private GraphCell edge;

	private JList nameSpaceList;
	private JList localNameList;
	private Map propMap;
	private IconCellRenderer renderer;
	private static Object[] NULL = new Object[0];

	private List propList;
	private List validPropList;
	private AttributeDialog attrDialog;
	private GraphManager gmanager;
	private RDFSInfoMap rdfsInfoMap = RDFSInfoMap.getInstance();

	private static final int listWidth = 350;
	private static final int listHeight = 40;

	public RDFPropertyPanel(GraphManager manager, AttributeDialog pw) {
		gmanager = manager;
		attrDialog = pw;
		setBorder(BorderFactory.createTitledBorder("Property"));

		uriField = new JTextField();
		uriField.setPreferredSize(new Dimension(listWidth, listHeight));
		uriField.setMinimumSize(new Dimension(listWidth, listHeight));
		uriField.setBorder(BorderFactory.createTitledBorder("URI"));
		uriField.addActionListener(this);

		jumpRDFSProp = new JButton("Jump RDFS");
		jumpRDFSProp.addActionListener(this);

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
		c.anchor = GridBagConstraints.PAGE_START;
		c.gridwidth = GridBagConstraints.REMAINDER;
		gridbag.setConstraints(uriField, c);
		add(uriField);
		c.anchor = GridBagConstraints.EAST;
		c.gridwidth = GridBagConstraints.REMAINDER;
		gridbag.setConstraints(jumpRDFSProp, c);
		add(jumpRDFSProp);
		Component selectPropertyPanel = createSelectPropertyPanel();
		c.anchor = GridBagConstraints.CENTER;
		gridbag.setConstraints(selectPropertyPanel, c);
		add(selectPropertyPanel);
		gridbag.setConstraints(buttonGroup, c);
		add(buttonGroup);
	}

	private Component createSelectPropertyPanel() {
		nameSpaceList = new JList();
		nameSpaceList.addListSelectionListener(this);
		JScrollPane nameSpaceListScroll = new JScrollPane(nameSpaceList);
		nameSpaceListScroll.setBorder(BorderFactory.createTitledBorder("NameSpace"));
		nameSpaceListScroll.setPreferredSize(new Dimension(350, 110));

		localNameList = new JList();
		localNameList.addListSelectionListener(this);
		JScrollPane localNameListScroll = new JScrollPane(localNameList);
		localNameListScroll.setBorder(BorderFactory.createTitledBorder("LocalName"));
		localNameListScroll.setPreferredSize(new Dimension(350, 110));

		JPanel panel = new JPanel();
		panel.setBorder(BorderFactory.createTitledBorder("Select Property"));
		GridBagLayout gridbag = new GridBagLayout();
		GridBagConstraints c = new GridBagConstraints();
		panel.setLayout(gridbag);
		c.anchor = GridBagConstraints.PAGE_START;
		c.gridwidth = GridBagConstraints.REMAINDER;
		gridbag.setConstraints(nameSpaceListScroll, c);
		panel.add(nameSpaceListScroll);
		gridbag.setConstraints(localNameListScroll, c);

		renderer = new IconCellRenderer();
		localNameList.setCellRenderer(renderer);

		panel.add(localNameListScroll);

		return panel;
	}

	private static final String NULL_LOCAL_NAME = "(Null)";

	private void selectNameSpaceList() {
		if (!localNameList.isSelectionEmpty()) {
			localNameList.clearSelection();
		}
		String nameSpace = (String) nameSpaceList.getSelectedValue();
		Set localNames = (Set) propMap.get(nameSpace);
		if (nameSpace != null) {
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
			
			uriField.setText(nameSpace);
			uriField.setToolTipText(nameSpace);
		}
	}

	private void selectLocalNameList() {
		if (nameSpaceList.getSelectedValue() != null && localNameList.getSelectedValue() != null) {
			String ns = nameSpaceList.getSelectedValue().toString();
			String ln = localNameList.getSelectedValue().toString();
			if (ln.equals(NULL_LOCAL_NAME)) {
				ln = "";
			}
			uriField.setText(ns + ln);
			uriField.setToolTipText(ns + ln);

//		jumpした際に，リストを再構築してしまい，もともと選んでいたのがわからなくなってしまう．			
//			Resource uri = new ResourceImpl(uriField.getText());
//			Object propCell = (GraphCell) gmanager.getPropertyCell(uri, false);
//			gmanager.jumpPropertyArea(propCell); // 対応するRDFSプロパティを選択する
//			gmanager.jumpRDFArea(edge); // AttributeDialogの表示をRDFプロパティに戻す
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
			if (e.getSource() == nameSpaceList) {
				selectNameSpaceList();
			} else if (e.getSource() == localNameList) {
				selectLocalNameList();
			}
		} catch (NullPointerException np) { //あとではずす
			np.printStackTrace();
		}
	}

	public void dspPropertyInfo(GraphCell c) {
		edge = c;
		Object propertyCell = rdfsInfoMap.getEdgeInfo(edge);
		if (propertyCell == null) {
			String defaultProperty = MR3Resource.Nil.getURI();
			setValue(defaultProperty);
			changeProperty();
		} else {
			RDFSInfo info = rdfsInfoMap.getCellInfo(propertyCell);
			setValue(info.getURI().toString());
		}
	}

	public void setValidPropertyList(List list) {
		validPropList = list;
	}

	public void setPropertyList(List plist, List vlist) {
		propList = plist;
		validPropList = vlist;
		Map pMap = new HashMap();
		Set propNameSpaces = new HashSet();

		for (Iterator i = propList.iterator(); i.hasNext();) {
			RDFSInfo info = rdfsInfoMap.getCellInfo(i.next());
			Resource uri = info.getURI();
			propNameSpaces.add(uri.getNameSpace());

			Set localNames = (Set) pMap.get(uri.getNameSpace());
			if (localNames == null) {
				localNames = new HashSet();
				pMap.put(uri.getNameSpace(), localNames);
			}
			localNames.add(uri.getLocalName());
		}

		for (Iterator i = propNameSpaces.iterator(); i.hasNext();) {
			String nameSpace = (String) i.next();
			Set localNames = (Set) pMap.get(nameSpace);
		}

		nameSpaceList.setListData(propNameSpaces.toArray());
		propMap = pMap;
	}

	public void setValue(String s) {
		if (s != null) {
			uriField.setText(s);
		}
	}

	private void changeProperty() {
		GraphCell propertyCell = null;
		Resource uri = new ResourceImpl(uriField.getText());

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
//			JOptionPane.showMessageDialog(null, "Create Default Property.", "Warning", JOptionPane.ERROR_MESSAGE);
			} else {
				SelectRDFSCheckDialog dialog = new SelectRDFSCheckDialog("Choose One Select");
				CreateRDFSType createType = (CreateRDFSType) dialog.getValue();
				if (createType == CreateRDFSType.CREATE) {
					propertyCell = (GraphCell) gmanager.getPropertyCell(uri, false);
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
		Resource uri = new ResourceImpl(uriField.getText());
		if (gmanager.isEmptyURI(uri.getURI())) {
			return;
		}
		if (rdfsInfoMap.isPropertyCell(uri)) {
			Object propertyCell = (GraphCell) rdfsInfoMap.getPropertyCell(uri);
			gmanager.jumpPropertyArea(propertyCell);
		} else {
			JOptionPane.showMessageDialog(null, "Not Defined", "Warning", JOptionPane.ERROR_MESSAGE);
		}
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == apply || e.getSource() == uriField) {
			if (edge != null) {
				changeProperty();
				gmanager.jumpRDFArea(edge); // AttributeDialogの表示をRDFプロパティに戻す
			}
		} else if (e.getSource() == jumpRDFSProp) {
			jumpRDFSProperty();
		} else if (e.getSource() == close) {
			attrDialog.setVisible(false);
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
				//					URL valid = MR3Resource.getImageIcon("valid.gif");
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
