package jp.ac.shizuoka.cs.panda.mmm.mr3.ui;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

import javax.swing.*;
import javax.swing.event.*;

import jp.ac.shizuoka.cs.panda.mmm.mr3.data.*;
import jp.ac.shizuoka.cs.panda.mmm.mr3.jgraph.*;
import jp.ac.shizuoka.cs.panda.mmm.mr3.util.*;

import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.rdf.model.impl.*;
import com.hp.hpl.jena.vocabulary.*;
import com.jgraph.graph.*;

public class RDFPropertyPanel extends JPanel implements ActionListener, ListSelectionListener {

	private JCheckBox isContainerBox;
	private JSpinner numSpinner;

	private JCheckBox propOnlyCheck;
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

	private static final int BOX_WIDTH = 120;
	private static final int BOX_HEIGHT = 50;
	private static final int LIST_WIDTH = 350;
	private static final int LIST_HEIGHT = 40;

	public RDFPropertyPanel(GraphManager manager) {
		gmanager = manager;
		setBorder(BorderFactory.createTitledBorder("RDF Property Attributes"));

		isContainerBox = new JCheckBox("is Container");
		isContainerBox.addActionListener(new ContainerBoxAction());
		isContainerBox.setSelected(false);
		numSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 99, 1));
		//		numSpinner.setEditor(new JSpinner.NumberEditor(numSpinner, "00"));
		numSpinner.setEnabled(false);
		JPanel containerPanel = new JPanel();
		containerPanel.add(isContainerBox);
		containerPanel.add(numSpinner);

		propOnlyCheck = new JCheckBox("Show property prefix only");
		propOnlyCheck.addActionListener(this);
		propOnlyCheck.setSelected(true);
		uriPrefixBox = new JComboBox();
		uriPrefixBox.addActionListener(new ChangePrefixAction());
		initComponent(uriPrefixBox, "Prefix", BOX_WIDTH, BOX_HEIGHT);

		idField = new JTextField();
		initComponent(idField, "ID", BOX_WIDTH, LIST_HEIGHT);
		idField.addActionListener(this);

		jumpRDFSProp = new JButton("Jump RDFS");
		jumpRDFSProp.addActionListener(this);

		JPanel uriPanel = new JPanel();
		uriPanel.add(uriPrefixBox);
		uriPanel.add(idField);
		uriPanel.add(jumpRDFSProp);

		nsLabel = new JLabel();
		initComponent(nsLabel, "NameSpace", LIST_WIDTH, LIST_HEIGHT);

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
		c.anchor = GridBagConstraints.WEST;
		gridbag.setConstraints(containerPanel, c);
		add(containerPanel);
		gridbag.setConstraints(propOnlyCheck, c);
		add(propOnlyCheck);
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

	private void setContainer(boolean t) {
		numSpinner.setEnabled(t);
		propOnlyCheck.setEnabled(!t);
		uriPrefixBox.setEnabled(!t);
		idField.setEnabled(!t);
		nsLabel.setEnabled(!t);
		jumpRDFSProp.setEnabled(!t);
		localNameList.setEnabled(!t);
	}

	private boolean isContainer() {
		return isContainerBox.isSelected();
	}

	class ContainerBoxAction extends AbstractAction {
		public void actionPerformed(ActionEvent e) {
			setContainer(isContainer());
		}
	}

	class ChangePrefixAction extends AbstractAction {
		public void actionPerformed(ActionEvent e) {
			selectNameSpaceList();
		}
	}

	private Component createSelectPropertyPanel() {
		localNameList = new JList();
		localNameList.addListSelectionListener(this);
		JScrollPane localNameListScroll = new JScrollPane(localNameList);
		initComponent(localNameListScroll, "Property ID", 350, 120);

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

	private void setURIPrefixBoxModel() {
		if (propList != null && propOnlyCheck.isSelected()) {
			uriPrefixBox.setModel(new DefaultComboBoxModel(PrefixNSUtil.getPropPrefixes(propList).toArray()));
		} else {
			uriPrefixBox.setModel(new DefaultComboBoxModel(PrefixNSUtil.getPrefixes().toArray()));
		}
	}

	private void setPrefix() {
		setURIPrefixBoxModel();
		for (Iterator i = gmanager.getPrefixNSInfoSet().iterator(); i.hasNext();) {
			PrefixNSInfo prefNSInfo = (PrefixNSInfo) i.next();
			if (prefNSInfo.getNameSpace().equals(nsLabel.getText())) {
				uriPrefixBox.setSelectedItem(prefNSInfo.getPrefix());
				break;
			}
		}
	}

	private void selectNameSpaceList() {
		PrefixNSUtil.replacePrefix((String) uriPrefixBox.getSelectedItem(), nsLabel);

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
		Set modifyLocalNames = new TreeSet();
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
		localNameList.setSelectedValue(idField.getText(), true);
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
			PropertyInfo info = (PropertyInfo) rdfsInfoMap.getCellInfo(propertyCell);
			isContainerBox.setSelected(info.isContainer());
			setContainer(info.isContainer());
			if (info.isContainer()) {				
				numSpinner.setValue(new Integer(info.getNum()));
			} else {
				setNSLabel(info.getNameSpace());
				idField.setText(info.getLocalName());
			}
		}
		setPrefix();
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

		selectNameSpaceList();
	}

	private static final String selectSupPropertiesTitle = "Select Super Properties";

	private String getURI() {
		return nsLabel.getText() + idField.getText();
	}

	private void changeProperty() {
		GraphCell propertyCell = null;
		Resource uri = ResourceFactory.createResource(getURI());

		if (rdfsInfoMap.isPropertyCell(uri) || uri.equals(MR3Resource.Nil)) {
			propertyCell = (GraphCell) gmanager.getPropertyCell(uri, false);
		} else {
			if (gmanager.isDuplicatedWithDialog(uri.getURI(), null, GraphType.PROPERTY)) {
				return;
			}
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
		gmanager.jumpPropertyArea(propertyCell); // 対応するRDFSプロパティを選択する
		rdfsInfoMap.putEdgeInfo(edge, propertyCell);
		changePropView(propertyCell);
	}

	private void changePropView(Object propertyCell) {
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
		if (edge == null) {
			return;
		}
		if (e.getSource() == apply || e.getSource() == idField) {
			if (isContainer()) {
				Integer num = (Integer) numSpinner.getValue();
				Resource resource = ResourceFactory.createResource(RDF.getURI() + "_" + num.intValue());
				GraphCell propertyCell = (GraphCell) gmanager.getPropertyCell(resource, false);
				rdfsInfoMap.putEdgeInfo(edge, propertyCell);
				changePropView(propertyCell);
			} else {
				changeProperty();
				gmanager.jumpRDFArea(edge);
			}
		} else if (e.getSource() == propOnlyCheck) {
			setURIPrefixBoxModel();
			selectNameSpaceList();
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
				setIcon(Utilities.getImageIcon("valid.gif"));
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
