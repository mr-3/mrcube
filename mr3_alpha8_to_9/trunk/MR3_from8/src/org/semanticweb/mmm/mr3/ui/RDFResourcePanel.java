package org.semanticweb.mmm.mr3.ui;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;

import org.jgraph.graph.*;
import org.semanticweb.mmm.mr3.data.*;
import org.semanticweb.mmm.mr3.jgraph.*;
import org.semanticweb.mmm.mr3.util.*;

import com.hp.hpl.jena.rdf.model.*;

public class RDFResourcePanel extends JPanel implements ActionListener {

	private JButton selectTypeButton;
	private JButton jumpRDFSClassButton;
	private JButton applyButton;
	private JButton closeButton;

	private Set prefixNSInfoSet;

	private JComboBox resPrefixBox;
	private JComboBox resTypePrefixBox;

	private JTextField uriField;
	private JLabel resNSLabel;
	//	private RDFSClassTree classTree;
	private JTextField resTypeField;
	private JLabel resTypeNSLabel;

	private JCheckBox isTypeCellCheckBox;
	private JCheckBox isAnonBox;

	private GraphCell cell;
	private RDFResourceInfo resInfo;
	private GraphManager gmanager;
	private RDFResourceInfoMap resInfoMap = RDFResourceInfoMap.getInstance();
	private RDFLiteralInfoMap litInfoMap = RDFLiteralInfoMap.getInstance();
	private RDFSInfoMap rdfsInfoMap = RDFSInfoMap.getInstance();

	private static final int BOX_WIDTH = 130;
	private static final int LIST_WIDTH = 350;
	private static final int LIST_HEIGHT = 40;

	private static final String WARNING = Translator.getString("Warning");

	public RDFResourcePanel(GraphManager manager) {
		gmanager = manager;

		setBorder(BorderFactory.createTitledBorder(Translator.getString("AttributeDialog.RDFResourceAttribute.Text")));

		resTypePrefixBox = new JComboBox();
		Utilities.initComponent(resTypePrefixBox, MR3Constants.PREFIX, BOX_WIDTH, 50);

		resTypeField = new JTextField();
		Utilities.initComponent(resTypeField, Translator.getString("ResourceType") + " ID", BOX_WIDTH, LIST_HEIGHT);
		resTypePrefixBox.addActionListener(new ChangePrefixAction());

		isTypeCellCheckBox = new JCheckBox(Translator.getString("IsType"));
		isTypeCellCheckBox.addActionListener(new IsTypeCellAction());

		JPanel resTypePanel = new JPanel();
		resTypePanel.add(resTypePrefixBox);
		resTypePanel.add(resTypeField);
		resTypePanel.add(isTypeCellCheckBox);

		resTypeNSLabel = new JLabel();
		Utilities.initComponent(resTypeNSLabel, MR3Constants.NAME_SPACE, LIST_WIDTH, LIST_HEIGHT);

		selectTypeButton = new JButton(Translator.getString("SelectType"));
		selectTypeButton.addActionListener(this);
		jumpRDFSClassButton = new JButton(Translator.getString("Jump") + " RDFS");
		jumpRDFSClassButton.addActionListener(this);

		JPanel typePanel = new JPanel();
		typePanel.add(selectTypeButton);
		typePanel.add(jumpRDFSClassButton);

		selectTypeMode(false);

		resPrefixBox = new JComboBox();
		Utilities.initComponent(resPrefixBox, MR3Constants.PREFIX, BOX_WIDTH, 50);
		resPrefixBox.addActionListener(new ChangePrefixAction());

		uriField = new JTextField();
		Utilities.initComponent(uriField, Translator.getString("RDFResource") + " ID", BOX_WIDTH, LIST_HEIGHT);
		uriField.addActionListener(this);

		isAnonBox = new JCheckBox(Translator.getString("IsBlank"));
		isAnonBox.addActionListener(new IsAnonAction());

		JPanel resPanel = new JPanel();
		resPanel.add(resPrefixBox);
		resPanel.add(uriField);
		resPanel.add(isAnonBox);

		resNSLabel = new JLabel();
		Utilities.initComponent(resNSLabel, MR3Constants.NAME_SPACE, LIST_WIDTH, LIST_HEIGHT);

		applyButton = new JButton(MR3Constants.APPLY);
		applyButton.addActionListener(this);

		closeButton = new JButton(MR3Constants.CLOSE);
		closeButton.addActionListener(this);
		JPanel buttonGroup = new JPanel();
		buttonGroup.add(applyButton);
		buttonGroup.add(closeButton);

		//classTree = new RDFSClassTree(metaResMap);
		//classTree.addTreeSelectionListener(new SelectNode());
		//JScrollPane classTreeScroll = new JScrollPane(classTree);
		//classTreeScroll.setBorder(BorderFactory.createTitledBorder("Class
		// Tree"));
		//classTreeScroll.setPreferredSize(new Dimension(200, 300));

		//add(classTreeScroll);

		GridBagLayout gridbag = new GridBagLayout();
		GridBagConstraints c = new GridBagConstraints();
		setLayout(gridbag);
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.anchor = GridBagConstraints.CENTER;
		c.weighty = c.weightx = 3;

		gridbag.setConstraints(resTypePanel, c);
		add(resTypePanel);
		gridbag.setConstraints(resTypeNSLabel, c);
		add(resTypeNSLabel);

		c.anchor = GridBagConstraints.EAST;
		gridbag.setConstraints(typePanel, c);
		add(typePanel);

		c.anchor = GridBagConstraints.CENTER;
		gridbag.setConstraints(resPanel, c);
		add(resPanel);
		gridbag.setConstraints(resNSLabel, c);
		add(resNSLabel);

		c.anchor = GridBagConstraints.CENTER;
		gridbag.setConstraints(buttonGroup, c);
		add(buttonGroup);
	}

	class ChangePrefixAction extends AbstractAction {
		public void actionPerformed(ActionEvent e) {
			if (e.getSource() == resPrefixBox) {
				PrefixNSUtil.replacePrefix((String) resPrefixBox.getSelectedItem(), resNSLabel);
			} else if (e.getSource() == resTypePrefixBox) {
				PrefixNSUtil.replacePrefix((String) resTypePrefixBox.getSelectedItem(), resTypeNSLabel);
			}
		}
	}

	// RDFリソースのタイプが存在すればチェックボタンにチェックする．
	private void selectTypeMode(boolean t) {
		isTypeCellCheckBox.setSelected(t);
		if (t) {
			RDFSInfo info = rdfsInfoMap.getCellInfo(resInfo.getTypeCell());
			setResourceTypeField(resInfo.getType().getLocalName());
		} else {
			setResourceTypeField("");
		}
		resTypeNSLabel.setEnabled(t);
		resTypePrefixBox.setEnabled(t);
		resTypeField.setEditable(t);
		selectTypeButton.setEnabled(t);
		jumpRDFSClassButton.setEnabled(t);
	}

	class IsTypeCellAction extends AbstractAction {
		public void actionPerformed(ActionEvent e) {
			selectTypeMode(isTypeCellCheckBox.isSelected());
		}
	}

	//	class SelectNode implements TreeSelectionListener {
	//		public void valueChanged(TreeSelectionEvent e) {
	//			JTree tree = (JTree) e.getSource();
	//			DefaultMutableTreeNode node = (DefaultMutableTreeNode)
	// tree.getLastSelectedPathComponent();
	//
	//			if (node != null && tree.getSelectionCount() == 1 &&
	// resInfo.getURIType() != URIType.ANONYMOUS) {
	//				classType.setText(node.toString());
	//				classType.setToolTipText(node.toString());
	//			}
	//		}
	//	}

	class IsAnonAction extends AbstractAction {
		public void actionPerformed(ActionEvent e) {
			setURIField("", !isAnonBox.isSelected());
			resNSLabel.setEnabled(!isAnonBox.isSelected());
		}
	}

	private boolean isTypeURI() {
		return resInfo.getType().getURI().length() != 0;
	}

	private void setResourceTypeField(String uri) {
		resTypeField.setText(uri);
		resTypeField.setToolTipText(uri);
	}

	private void setResPrefix() {
		if (resInfo.getURIType() == URIType.URI) {
			for (Iterator i = prefixNSInfoSet.iterator(); i.hasNext();) {
				PrefixNSInfo prefNSInfo = (PrefixNSInfo) i.next();
				if (prefNSInfo.getNameSpace().equals(resInfo.getURI().getNameSpace())) {
					resPrefixBox.setSelectedItem(prefNSInfo.getPrefix());
					break;
				}
			}
		}
	}

	private void setResTypePrefix() {
		RDFSInfo rdfsInfo = rdfsInfoMap.getCellInfo(resInfo.getTypeCell());
		if (rdfsInfo == null) {
			setResTypePrefix(gmanager.getBaseURI());
			PrefixNSUtil.setNSLabel(resTypeNSLabel, gmanager.getBaseURI());
			return;
		}
		setResTypePrefix(rdfsInfo.getURI().getNameSpace());
	}

	private void setResTypePrefix(String ns) {
		for (Iterator i = prefixNSInfoSet.iterator(); i.hasNext();) {
			PrefixNSInfo prefNSInfo = (PrefixNSInfo) i.next();
			if (prefNSInfo.getNameSpace().equals(ns)) {
				resTypePrefixBox.setSelectedItem(prefNSInfo.getPrefix());
				break;
			}
		}
	}

	public void showRDFResInfo(GraphCell c) {
		cell = c;
		resInfo = resInfoMap.getCellInfo(cell);
		prefixNSInfoSet = gmanager.getPrefixNSInfoSet();
		PrefixNSUtil.setPrefixNSInfoSet(prefixNSInfoSet);
		resPrefixBox.setModel(new DefaultComboBoxModel(PrefixNSUtil.getPrefixes().toArray()));
		resTypePrefixBox.setModel(new DefaultComboBoxModel(PrefixNSUtil.getPrefixes().toArray()));
		setResPrefix();
		setResTypePrefix();
		setURI(resInfo.getURI().getLocalName());
		selectTypeMode(isTypeURI());
	}

	public void setURI(String resURI) {
		if (resInfo.getURIType() == URIType.ANONYMOUS) {
			setURIField("", false);
		} else {
			setURIField(resURI, true);
		}
	}

	private void setURIField(String uri, boolean t) {
		uriField.setText(uri);
		uriField.setToolTipText(uri);
		uriField.setEditable(t);
		resPrefixBox.setEnabled(t);
		isAnonBox.setSelected(!t);
	}

	private void setCellValue() {
		// setURI()をする前にURIタイプを変更する必要あり．URIタイプによって処理を分けているため
		if (isAnonBox.isSelected()) {
			resInfo.setURIType(URIType.ANONYMOUS);
			resInfo.setURI(new AnonId().toString());
		} else {
			resInfo.setURIType(URIType.URI);
			resInfo.setURI(getResourceURI());
		}
		gmanager.changeCellView();
	}

	private String getResourceURI() {
		return resNSLabel.getText() + uriField.getText();
	}

	private String getResourceTypeURI() {
		return resTypeNSLabel.getText() + resTypeField.getText();
	}

	// URIの重複と，空のチェックをする．
	private boolean isErrorResource() {
		String uri = getResourceURI();
		if (isAnonBox.isSelected()) {
			return false;
		}
		if (isLocalDuplicated(uri) || gmanager.isEmptyURI(uri) || gmanager.isDuplicatedWithDialog(uri, cell, GraphType.RDF)) {
			return true;
		}
		return false;
	}

	private boolean isLocalDuplicated(String tmpURI) {
		String tmpResTypeURI = "";
		if (isTypeCellCheckBox.isSelected()) {
			tmpResTypeURI = getResourceTypeURI();
		}
		if (tmpURI.equals(tmpResTypeURI)) {
			JOptionPane.showInternalMessageDialog(this, Translator.getString("Warning.Message1"), WARNING, JOptionPane.ERROR_MESSAGE);
			return true;
		} else {
			return false;
		}
	}

	private GraphCell getResourceType() {
		GraphCell typeCell = null;
		Resource uri = ResourceFactory.createResource(getResourceTypeURI());

		if (gmanager.isEmptyURI(uri.getURI())) {
			return null;
		}
		if (rdfsInfoMap.isClassCell(uri)) {
			typeCell = (GraphCell) gmanager.getClassCell(uri, false);
		} else {
			if (gmanager.isDuplicatedWithDialog(uri.getURI(), null, GraphType.CLASS))
				return null;
			if (resInfo.getTypeCell() == null) {
				int ans =
					JOptionPane.showInternalConfirmDialog(
						gmanager.getDesktop(),
						Translator.getString("Warning.Message2"),
						WARNING,
						JOptionPane.YES_NO_OPTION);
				if (ans == JOptionPane.YES_OPTION) {
					Set supClasses = gmanager.getSupRDFS(gmanager.getClassGraph(), Translator.getString("SelectSupClassesDialog.Title"));
					if (supClasses == null) {
						return null;
					}
					typeCell = (GraphCell) gmanager.insertSubRDFS(uri, supClasses, gmanager.getClassGraph());
				}
			} else {
				RDFSManagementDialog dialog = new RDFSManagementDialog(gmanager);
				dialog.replaceGraph(gmanager.getClassGraph());
				dialog.setRegionSet(new HashSet());
				dialog.setVisible(true);
				CreateRDFSType createType = dialog.getType();
				if (createType == CreateRDFSType.CREATE) {
					Set supClasses = dialog.getSupRDFSSet();
					typeCell = (GraphCell) gmanager.insertSubRDFS(uri, supClasses, gmanager.getClassGraph());
				} else if (createType == CreateRDFSType.RENAME) {
					typeCell = (GraphCell) resInfo.getTypeCell();
					RDFSInfo rdfsInfo = rdfsInfoMap.getCellInfo(typeCell);
					rdfsInfoMap.removeURICellMap(rdfsInfo);
					rdfsInfo.setURI(uri.getURI());
					rdfsInfoMap.putURICellMap(rdfsInfo, typeCell);
				} else if (createType == null) {
					return null;
				}
			}
		}
		return typeCell;
	}

	private void setResourceType(GraphCell typeCell) {
		resInfo.setTypeCell(typeCell);
		String typeValue = resInfo.getType().getURI();
		setResourceTypeField(typeValue);
	}

	private void jumpRDFSClass() {
		Resource uri = ResourceFactory.createResource(getResourceTypeURI());
		if (gmanager.isEmptyURI(uri.getURI())) {
			return;
		}
		if (rdfsInfoMap.isClassCell(uri)) {
			Object classCell = (GraphCell) rdfsInfoMap.getClassCell(uri);
			gmanager.jumpClassArea(classCell);
		} else {
			JOptionPane.showInternalMessageDialog(
				gmanager.getDesktop(),
				Translator.getString("Warning.Message3"),
				WARNING,
				JOptionPane.ERROR_MESSAGE);
		}
	}

	private void apply() {
		if (isErrorResource()) {
			return;
		}
		if (isTypeCellCheckBox.isSelected()) {
			GraphCell resTypeCell = getResourceType();
			if (resTypeCell != null) {
				setResourceType(resTypeCell);
				gmanager.jumpClassArea(resTypeCell); // 対応するクラスを選択する
			}
		} else {
			setResourceType(null);
		}
		setCellValue();
		// このタイミングでジャンプしないと，URIを反映できない．
		gmanager.jumpRDFArea(cell); // AttributeDialogの表示をRDFリソースに戻す
	}

	private void selectResourceType() {
		SelectResourceTypeDialog selectResDialog = new SelectResourceTypeDialog(gmanager);
		selectResDialog.replaceGraph(gmanager.getClassGraph());
		selectResDialog.setInitCell(resInfo.getTypeCell());
		selectResDialog.setVisible(true);
		Resource uri = (Resource) selectResDialog.getValue();
		if (uri != null) {
			setResTypePrefix(uri.getNameSpace());
			PrefixNSUtil.setNSLabel(resTypeNSLabel, uri.getNameSpace());
			setResourceTypeField(uri.getLocalName());
		}
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == applyButton) {
			apply();
		} else if (e.getSource() == jumpRDFSClassButton) {
			jumpRDFSClass();
		} else if (e.getSource() == selectTypeButton) {
			selectResourceType();
		} else if (e.getSource() == closeButton) {
			gmanager.setVisibleAttrDialog(false);
		}
	}
}
