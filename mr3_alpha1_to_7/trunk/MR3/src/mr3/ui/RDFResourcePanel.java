package mr3.ui;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;

import mr3.data.*;
import mr3.jgraph.*;
import mr3.util.*;

import com.hp.hpl.mesa.rdf.jena.common.*;
import com.hp.hpl.mesa.rdf.jena.model.*;
import com.jgraph.graph.*;

public class RDFResourcePanel extends JPanel implements ActionListener {

	private JButton selectTypeButton;
	private JButton jumpRDFSClassButton;
	private JButton applyButton;
	private JButton closeButton;

	private Set prefixNSInfoSet;

	private JComboBox resPrefixBox;
	private JComboBox resTypePrefixBox;

	private JTextField uriField;
	//	private RDFSClassTree classTree;
	private JTextField resTypeField;

	private boolean isAnonymous;

	private JCheckBox isTypeCellCheck;

	private JRadioButton resTypeURIButton;
	private JRadioButton resTypeIDButton;

	private JRadioButton rdfURIButton;
	private JRadioButton rdfAnonymousButton;
	private JRadioButton rdfIDButton;

	private URIType tmpURIType;
	private URIType tmpResTypeURIType;

	private GraphCell cell;
	private RDFResourceInfo resInfo;
	private GraphManager gmanager;
	private RDFResourceInfoMap resInfoMap = RDFResourceInfoMap.getInstance();
	private RDFLiteralInfoMap litInfoMap = RDFLiteralInfoMap.getInstance();
	private RDFSInfoMap rdfsInfoMap = RDFSInfoMap.getInstance();

	private static final int boxWidth = 80;
	private static final int listWidth = 290;
	private static final int listHeight = 40;

	public RDFResourcePanel(GraphManager manager) {
		gmanager = manager;

		setBorder(BorderFactory.createTitledBorder("RDF Resource Attributes"));

		JPanel resTypeURITypeGroupPanel = initResTypeURITypeGroupPanel();

		resTypePrefixBox = new JComboBox();
		resTypePrefixBox.setPreferredSize(new Dimension(boxWidth, 30));
		resTypePrefixBox.setMinimumSize(new Dimension(boxWidth, 30));

		resTypeField = new JTextField();
		resTypePrefixBox.addActionListener(new ChangePrefixAction());
		resTypeField.setPreferredSize(new Dimension(listWidth, listHeight));
		resTypeField.setMinimumSize(new Dimension(listWidth, listHeight));
		resTypeField.setBorder(BorderFactory.createTitledBorder("Resource Type"));

		isTypeCellCheck = new JCheckBox("is Type");
		isTypeCellCheck.addActionListener(new IsTypeCellAction());

		selectTypeButton = new JButton("Select Type");
		selectTypeButton.addActionListener(this);
		jumpRDFSClassButton = new JButton("Jump RDFS");
		jumpRDFSClassButton.addActionListener(this);

		JPanel typePanel = new JPanel();
		typePanel.add(isTypeCellCheck);
		typePanel.add(selectTypeButton);
		typePanel.add(jumpRDFSClassButton);

		selectTypeMode(false);

		JPanel rdfURITypeGroupPanel = initRDFURITypeGroupPanel();

		resPrefixBox = new JComboBox();
		resPrefixBox.addActionListener(new ChangePrefixAction());
		resPrefixBox.setPreferredSize(new Dimension(boxWidth, 30));
		resPrefixBox.setMinimumSize(new Dimension(boxWidth, 30));

		uriField = new JTextField();
		uriField.setPreferredSize(new Dimension(listWidth, listHeight));
		uriField.setMinimumSize(new Dimension(listWidth, listHeight));
		uriField.setBorder(BorderFactory.createTitledBorder("URI"));
		uriField.addActionListener(this);

		applyButton = new JButton("Apply");
		applyButton.addActionListener(this);

		closeButton = new JButton("Close");
		closeButton.addActionListener(this);
		JPanel buttonGroup = new JPanel();
		buttonGroup.add(applyButton);
		buttonGroup.add(closeButton);

		//classTree = new RDFSClassTree(metaResMap);     
		//classTree.addTreeSelectionListener(new SelectNode());
		//JScrollPane classTreeScroll = new JScrollPane(classTree);
		//classTreeScroll.setBorder(BorderFactory.createTitledBorder("Class Tree"));
		//classTreeScroll.setPreferredSize(new Dimension(200, 300)); 

		//add(classTreeScroll);

		GridBagLayout gridbag = new GridBagLayout();
		GridBagConstraints c = new GridBagConstraints();
		setLayout(gridbag);
		c.anchor = GridBagConstraints.PAGE_START;
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.anchor = GridBagConstraints.WEST;
		c.weighty = 3;
		c.weightx = 2;
		gridbag.setConstraints(resTypeURITypeGroupPanel, c);
		add(resTypeURITypeGroupPanel);

		c.anchor = GridBagConstraints.CENTER;
		c.gridwidth = GridBagConstraints.RELATIVE;
		gridbag.setConstraints(resTypePrefixBox, c);
		add(resTypePrefixBox);
		c.gridwidth = GridBagConstraints.REMAINDER;
		gridbag.setConstraints(resTypeField, c);
		add(resTypeField);

		c.anchor = GridBagConstraints.WEST;
		gridbag.setConstraints(typePanel, c);
		add(typePanel);
		gridbag.setConstraints(rdfURITypeGroupPanel, c);
		add(rdfURITypeGroupPanel);
		c.anchor = GridBagConstraints.CENTER;

		c.gridwidth = GridBagConstraints.RELATIVE;
		gridbag.setConstraints(resPrefixBox, c);
		add(resPrefixBox);
		c.gridwidth = GridBagConstraints.REMAINDER;
		gridbag.setConstraints(uriField, c);
		add(uriField);

		gridbag.setConstraints(buttonGroup, c);
		add(buttonGroup);
	}

	class ChangePrefixAction extends AbstractAction {
		public void actionPerformed(ActionEvent e) {
			if (e.getSource() == resPrefixBox) {
				PrefixNSUtil.replacePrefix((String) resPrefixBox.getSelectedItem(), uriField);
			} else if (e.getSource() == resTypePrefixBox) {
				PrefixNSUtil.replacePrefix((String) resTypePrefixBox.getSelectedItem(), resTypeField);
			}
		}
	}

	private JPanel initResTypeURITypeGroupPanel() {
		resTypeURIButton = new JRadioButton("URI");
		resTypeIDButton = new JRadioButton("ID");
		resTypeURIButton.setSelected(true);
		ChangeResTypeURITypeAction action = new ChangeResTypeURITypeAction();
		resTypeURIButton.addActionListener(action);
		resTypeIDButton.addActionListener(action);
		ButtonGroup group = new ButtonGroup();
		group.add(resTypeURIButton);
		group.add(resTypeIDButton);
		JPanel resTypeURITypeGroupPanel = new JPanel();
		resTypeURITypeGroupPanel.setBorder(BorderFactory.createTitledBorder("Resource Type URI Type"));
		resTypeURITypeGroupPanel.setPreferredSize(new Dimension(200, 55));
		resTypeURITypeGroupPanel.add(resTypeURIButton);
		resTypeURITypeGroupPanel.add(resTypeIDButton);

		return resTypeURITypeGroupPanel;
	}

	private JPanel initRDFURITypeGroupPanel() {
		rdfURIButton = new JRadioButton("URI");
		rdfIDButton = new JRadioButton("ID");
		rdfAnonymousButton = new JRadioButton("ANONYMOUS");
		ChangeRDFURITypeAction ra = new ChangeRDFURITypeAction();
		rdfURIButton.addActionListener(ra);
		rdfIDButton.addActionListener(ra);
		rdfAnonymousButton.addActionListener(ra);
		ButtonGroup group = new ButtonGroup();
		group.add(rdfURIButton);
		group.add(rdfIDButton);
		group.add(rdfAnonymousButton);
		JPanel uriTypeGroupPanel = new JPanel();
		uriTypeGroupPanel.setBorder(BorderFactory.createTitledBorder("URI Type"));
		uriTypeGroupPanel.add(rdfURIButton);
		uriTypeGroupPanel.add(rdfIDButton);
		uriTypeGroupPanel.add(rdfAnonymousButton);

		return uriTypeGroupPanel;
	}

	// RDFリソースのタイプが存在すればチェックボタンにチェックする．
	private void selectTypeMode(boolean t) {
		isTypeCellCheck.setSelected(t);
		if (t) {
			RDFSInfo info = rdfsInfoMap.getCellInfo(resInfo.getTypeCell());
			if (info != null) {
				tmpResTypeURIType = info.getURIType();
				if (info.getURIType() == URIType.URI) {
					resTypeURIButton.setSelected(true);
					resTypePrefixBox.setEnabled(true);
				} else {
					resTypeIDButton.setSelected(true);
					resTypePrefixBox.setEnabled(false);
				}
			} else {
				if (resTypeURIButton.isSelected()) {
					tmpResTypeURIType = URIType.URI;
					resTypePrefixBox.setEnabled(true);
				} else {
					tmpResTypeURIType = URIType.ID;
					resTypePrefixBox.setEnabled(false);
				}
			}
			setResourceTypeField(resInfo.getType().getURI());
		} else {
			setResourceTypeField("");
			resTypePrefixBox.setEnabled(false);
		}
		resTypeURIButton.setEnabled(t);
		resTypeIDButton.setEnabled(t);
		resTypeField.setEditable(t);
		selectTypeButton.setEnabled(t);
		jumpRDFSClassButton.setEnabled(t);
	}

	class IsTypeCellAction extends AbstractAction {
		public void actionPerformed(ActionEvent e) {
			selectTypeMode(isTypeCellCheck.isSelected());
		}
	}

	//	class SelectNode implements TreeSelectionListener {
	//		public void valueChanged(TreeSelectionEvent e) {
	//			JTree tree = (JTree) e.getSource();
	//			DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
	//
	//			if (node != null && tree.getSelectionCount() == 1 && resInfo.getURIType() != URIType.ANONYMOUS) {
	//				classType.setText(node.toString());
	//				classType.setToolTipText(node.toString());
	//			}
	//		}
	//	}

	class ChangeRDFURITypeAction implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			String type = (String) e.getActionCommand();
			tmpURIType = URIType.getURIType(type);

			if (tmpURIType == URIType.ANONYMOUS) {
				setURIField("", false);
			} else if (tmpURIType == URIType.ID) {
				resPrefixBox.setEnabled(false);
				uriField.setEditable(true);
				if (uriField.getText().length() == 0 || uriField.getText().charAt(0) != '#') {
					uriField.setText('#' + uriField.getText());
				}
			} else if (tmpURIType == URIType.URI) {
				resPrefixBox.setEnabled(true);
				uriField.setEditable(true);
			}
		}
	}

	class ChangeResTypeURITypeAction implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			String type = (String) e.getActionCommand();
			tmpResTypeURIType = URIType.getURIType(type);

			if (tmpResTypeURIType == URIType.ID) {
				resTypePrefixBox.setEnabled(false);
				uriField.setEditable(true);
				if (resTypeField.getText().length() == 0 || resTypeField.getText().charAt(0) != '#') {
					resTypeField.setText('#' + resTypeField.getText());
				}
			} else {
				resTypePrefixBox.setEnabled(true);
			}
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
		resPrefixBox.setSelectedIndex(0);
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
		resTypePrefixBox.setSelectedIndex(0);
		RDFSInfo rdfsInfo = rdfsInfoMap.getCellInfo(resInfo.getTypeCell());
		if (rdfsInfo != null && rdfsInfo.getURIType() == URIType.URI) {
			for (Iterator i = prefixNSInfoSet.iterator(); i.hasNext();) {
				PrefixNSInfo prefNSInfo = (PrefixNSInfo) i.next();
				if (prefNSInfo.getNameSpace().equals(rdfsInfo.getURI().getNameSpace())) {
					resTypePrefixBox.setSelectedItem(prefNSInfo.getPrefix());
					break;
				}
			}
		}
	}

	public void showRDFResInfo(GraphCell c) {
		cell = c;
		resInfo = resInfoMap.getCellInfo(cell);
		prefixNSInfoSet = gmanager.getPrefixNSInfoSet();
		PrefixNSUtil.setPrefixNSInfoSet(prefixNSInfoSet);
		resPrefixBox.setModel(new DefaultComboBoxModel(PrefixNSUtil.getPrefixes().toArray()));
		resPrefixBox.insertItemAt("", 0);
		resTypePrefixBox.setModel(new DefaultComboBoxModel(PrefixNSUtil.getPrefixes().toArray()));
		resTypePrefixBox.insertItemAt("", 0);
		setResPrefix();
		setResTypePrefix();
		setURI(resInfo.getURI());
		selectTypeMode(isTypeURI());
	}

	public void setURI(Resource resURI) {
		tmpURIType = resInfo.getURIType();
		if (tmpURIType == URIType.URI) {
			resPrefixBox.setEnabled(true);
			rdfURIButton.setSelected(true);
			setURIField(resURI.getURI(), true);
		} else if (tmpURIType == URIType.ID) {
			resPrefixBox.setEnabled(false);
			rdfIDButton.setSelected(true);
			setURIField(resURI.getURI(), true);
		} else if (tmpURIType == URIType.ANONYMOUS) {
			rdfAnonymousButton.setSelected(true);
			setURIField("", false);
		}
	}

	private void setURIField(String uri, boolean t) {
		uriField.setText(uri);
		uriField.setToolTipText(uri);
		uriField.setEditable(t);
	}

	private void setCellValue() {
		String uri = uriField.getText();
		// setURI()をする前にURIタイプを変更する必要あり．URIタイプによって処理を分けているため
		resInfo.setURIType(tmpURIType);
		resInfo.setURI(uri);
		gmanager.changeCellView();
	}

	// URIの重複と，空のチェックをする．URITypeがIDの場合は，baseURIを含めた形でチェックする．
	private boolean isErrorResource() {
		String uri = uriField.getText();
		if (tmpURIType == URIType.ANONYMOUS) {
			return false;
		}
		String tmpURI = getAddedBaseURI(uri, tmpURIType);
		if (isLocalDuplicated(tmpURI) || gmanager.isEmptyURI(tmpURI) || gmanager.isDuplicatedWithDialog(tmpURI, cell, GraphType.RDF)) {
			return true;
		}
		return false;
	}

	private boolean isLocalDuplicated(String tmpURI) {
		String tmpResTypeURI = "";
		if (isTypeCellCheck.isSelected()) {
			if (tmpResTypeURIType == URIType.ID) {
				tmpResTypeURI = gmanager.getBaseURI();
			}
			tmpResTypeURI += resTypeField.getText();
		}
		if (tmpURI.equals(tmpResTypeURI)) {
			JOptionPane.showInternalMessageDialog(this, "URI is duplicated", "Warning", JOptionPane.ERROR_MESSAGE);
			return true;
		} else {
			return false;
		}
	}

	private String getAddedBaseURI(String uri, URIType uriType) {
		String tmpURI = "";
		if (uriType == URIType.ID) {
			tmpURI = gmanager.getBaseURI(); // チェックする時は，フルパスで．
		}
		tmpURI += uri;
		return tmpURI;
	}

	private static final String selectSupClassesTitle = "Select Super Classes";

	private GraphCell getResourceType() {
		GraphCell typeCell = null;
		Resource uri = new ResourceImpl(resTypeField.getText());

		if (gmanager.isEmptyURI(uri.getURI())) {
			return null;
		}
		if (rdfsInfoMap.isClassCell(uri)) {
			typeCell = (GraphCell) gmanager.getClassCell(uri, tmpResTypeURIType, false);
		} else {
			if (gmanager.isDuplicatedWithDialog(uri.getURI(), null, GraphType.CLASS))
				return null;
			if (resInfo.getTypeCell() == null) {
				int ans = JOptionPane.showConfirmDialog(null, "Not Defined.Create Class ?", "Warning", JOptionPane.YES_NO_OPTION);
				if (ans == JOptionPane.YES_OPTION) {
					Set supClasses = gmanager.getSupRDFS(gmanager.getClassGraph(), selectSupClassesTitle);
					typeCell = (GraphCell) gmanager.insertSubRDFS(uri, tmpResTypeURIType, supClasses, gmanager.getClassGraph());
				}
			} else {
				SelectRDFSCheckDialog dialog = new SelectRDFSCheckDialog("Choose One Select");
				CreateRDFSType createType = (CreateRDFSType) dialog.getValue();
				if (createType == CreateRDFSType.CREATE) {
					Set supClasses = gmanager.getSupRDFS(gmanager.getClassGraph(), selectSupClassesTitle);
					typeCell = (GraphCell) gmanager.insertSubRDFS(uri, tmpResTypeURIType, supClasses, gmanager.getClassGraph());
				} else if (createType == CreateRDFSType.RENAME) {
					typeCell = (GraphCell) resInfo.getTypeCell();
					RDFSInfo rdfsInfo = rdfsInfoMap.getCellInfo(typeCell);
					rdfsInfoMap.removeURICellMap(rdfsInfo);
					rdfsInfo.setURI(uri.getURI());
					rdfsInfo.setURIType(tmpResTypeURIType);
					rdfsInfoMap.putURICellMap(rdfsInfo, typeCell);
				} else {
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
		Resource uri = new ResourceImpl(resTypeField.getText());
		if (gmanager.isEmptyURI(uri.getURI())) {
			return;
		}
		if (rdfsInfoMap.isClassCell(uri)) {
			Object classCell = (GraphCell) rdfsInfoMap.getClassCell(uri);
			gmanager.jumpClassArea(classCell);
		} else {
			JOptionPane.showMessageDialog(null, "Not Defined", "Warning", JOptionPane.ERROR_MESSAGE);
		}
	}

	private void apply() {
		if (isErrorResource()) {
			return;
		}
		if (isTypeCellCheck.isSelected()) {
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
		SelectResourceTypeDialog classDialog = new SelectResourceTypeDialog(gmanager);
		classDialog.replaceGraph(gmanager.getClassGraph());
		classDialog.setInitCell(resInfo.getTypeCell());
		classDialog.setVisible(true);
		Resource uri = (Resource) classDialog.getValue();
		if (uri != null) {
			if (classDialog.getURIType() == URIType.URI) {
				resTypeURIButton.setSelected(true);
			} else {
				resTypeIDButton.setSelected(true);
			}
			setResourceTypeField(uri.getURI());
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
