package mr3.ui;
import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import mr3.data.*;
import mr3.jgraph.*;

import com.hp.hpl.mesa.rdf.jena.common.*;
import com.hp.hpl.mesa.rdf.jena.model.*;
import com.jgraph.graph.*;

public class RDFResourcePanel extends JPanel implements ActionListener {

	private JButton selectTypeButton;
	private JButton jumpRDFSClassButton;
	private JButton applyButton;
	private JButton closeButton;

	private JTextField uriField;
	//	private RDFSClassTree classTree;
	private JTextField resTypeField;

	private boolean isAnonymous;

	private JCheckBox isTypeCellCheck;

	private JRadioButton uriButton;
	private JRadioButton anonymousButton;
	private JRadioButton idButton;

	private AttributeDialog propDialog;

	private GraphCell cell;
	private RDFResourceInfo resInfo;
	private GraphManager gmanager;
	private RDFResourceInfoMap resInfoMap = RDFResourceInfoMap.getInstance();
	private RDFLiteralInfoMap litInfoMap = RDFLiteralInfoMap.getInstance();
	private RDFSInfoMap rdfsInfoMap = RDFSInfoMap.getInstance();

	private static final int listWidth = 350;
	private static final int listHeight = 40;

	public RDFResourcePanel(GraphManager manager, AttributeDialog pw) {
		gmanager = manager;
		propDialog = pw;

		setBorder(BorderFactory.createTitledBorder("Resource"));

		resTypeField = new JTextField();
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

		uriButton = new JRadioButton("URI");
		idButton = new JRadioButton("ID");
		anonymousButton = new JRadioButton("ANONYMOUS");
		RadioAction ra = new RadioAction();
		uriButton.addActionListener(ra);
		idButton.addActionListener(ra);
		anonymousButton.addActionListener(ra);
		ButtonGroup group = new ButtonGroup();
		group.add(uriButton);
		group.add(idButton);
		group.add(anonymousButton);
		JPanel uriTypeGroupPanel = new JPanel();
		uriTypeGroupPanel.setBorder(BorderFactory.createTitledBorder("URI Type"));
		uriTypeGroupPanel.add(uriButton);
		uriTypeGroupPanel.add(idButton);
		uriTypeGroupPanel.add(anonymousButton);

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
		//c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.CENTER;
		gridbag.setConstraints(resTypeField, c);
		add(resTypeField);
		c.anchor = GridBagConstraints.WEST;
		gridbag.setConstraints(typePanel, c);
		add(typePanel);
		gridbag.setConstraints(uriTypeGroupPanel, c);
		add(uriTypeGroupPanel);
		c.anchor = GridBagConstraints.CENTER;
		gridbag.setConstraints(uriField, c);
		add(uriField);
		gridbag.setConstraints(buttonGroup, c);
		add(buttonGroup);
	}

	private void selectTypeMode(boolean t) {
		isTypeCellCheck.setSelected(t);
		if (t) {
			setTypeField(resInfo.getType().getURI());
		} else {
			setTypeField("");
		}
		resTypeField.setEditable(t);
		selectTypeButton.setEnabled(t);
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

	class RadioAction implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			String type = (String) e.getActionCommand();
			resInfo.setURIType(URIType.getURIType(type));
//			System.out.println(type);
//			System.out.println(resInfo.getURIType());
			if (resInfo.getURIType() == URIType.ANONYMOUS) {
				setAnonymousCell();
			} else if (resInfo.getURIType() == URIType.ID) {
				if (uriField.getText().length() == 0 || uriField.getText().charAt(0) != '#') {
					uriField.setText('#' + uriField.getText());
				}
				uriField.setEditable(true);
			} else {
				uriField.setEditable(true);
			}
		}
	}

	public void setAnonymousCell() {
		anonymousButton.setSelected(true);
		setURIField("", false);
		resInfo.setURIType(URIType.ANONYMOUS);
		setCellValue();
	}

	private boolean isTypeURI() {
		return resInfo.getType().getURI().length() != 0;
	}

	private void setTypeField(String uri) {
		resTypeField.setText(uri);
		resTypeField.setToolTipText(uri);
	}

	public void displayResInfo(GraphCell c) {
		cell = c;
		resInfo = resInfoMap.getCellInfo(cell);
		setURI(resInfo.getURI());
		selectTypeMode(isTypeURI());
	}

	public void setURI(Resource resURI) {
		if (resInfo.getURIType() == URIType.URI) {
			uriButton.setSelected(true);
			resInfo.setURIType(URIType.URI);
			setURIField(resURI.getURI(), true);
		} else if (resInfo.getURIType() == URIType.ID) {
			idButton.setSelected(true);
			resInfo.setURIType(URIType.ID);
			setURIField(resURI.getURI(), true);
		} else if (resInfo.getURIType() == URIType.ANONYMOUS) {
			setAnonymousCell();
		}
	}

	private void setURIField(String uri, boolean t) {
		uriField.setText(uri);
		uriField.setToolTipText(uri);
		uriField.setEditable(t);
	}

	private void setCellValue() {
		String uri = uriField.getText();
		if (resInfo.getURIType() != URIType.ANONYMOUS) {
			if (resInfo.getURIType() == URIType.ID) {
				uri = gmanager.getBaseURI() + uri; // チェックする時は，フルパスで．
			}
			if (gmanager.isEmptyURI(uri) || gmanager.isDuplicatedWithDialog(uri, cell, GraphType.RDF)) {
				return;
			}
		}
		uri = uriField.getText();
		gmanager.setCellValue(cell, uri);
		resInfo.setURI(uri);
		gmanager.changeCellView();
	}

	private GraphCell getType() {
		GraphCell typeCell = null;
		Resource uri = new ResourceImpl(resTypeField.getText());

		if (gmanager.isEmptyURI(uri.getURI())) {
			return null;
		}
		if (rdfsInfoMap.isClassCell(uri)) {
			//			typeCell = (GraphCell) rdfsInfoMap.getClassCell(uri);
			typeCell = (GraphCell) gmanager.getClassCell(uri, false);
		} else {
			if (gmanager.isDuplicatedWithDialog(uri.getURI(), null, GraphType.CLASS))
				return null;
			if (resInfo.getTypeCell() == null) {
				int ans = JOptionPane.showConfirmDialog(null, "Not Defined.Create Class ?", "Warning", JOptionPane.YES_NO_OPTION);
				if (ans == JOptionPane.YES_OPTION) {
					typeCell = (GraphCell) gmanager.getClassCell(uri, false);
				}
			} else {
				SelectRDFSCheckDialog dialog = new SelectRDFSCheckDialog("Choose One Select");
				CreateRDFSType createType = (CreateRDFSType) dialog.getValue();
				if (createType == CreateRDFSType.CREATE) {
					typeCell = (GraphCell) gmanager.getClassCell(uri, false);
				} else if (createType == CreateRDFSType.RENAME) {
					typeCell = (GraphCell) resInfo.getTypeCell();
					RDFSInfo rdfsInfo = rdfsInfoMap.getCellInfo(typeCell);
					rdfsInfoMap.removeURICellMap(rdfsInfo);
					rdfsInfo.setURI(uri.getURI());
					rdfsInfoMap.putURICellMap(rdfsInfo, typeCell);
				} else {
					return null;
				}
			}
		}
		return typeCell;
	}

	private void setType(GraphCell typeCell) {
		resInfo.setType(typeCell);
		String typeValue = resInfo.getType().getURI();
		resTypeField.setText(typeValue);
		resTypeField.setToolTipText(typeValue);
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
		if (isTypeCellCheck.isSelected()) {
			GraphCell t = getType();
			if (t != null) {
				setType(t);
			}
		} else {
			setType(null);
		}
		setCellValue();
	}

	private void selectType() {
		SelectTypeDialog classDialog = new SelectTypeDialog();
		classDialog.replaceGraph(gmanager.getClassGraph());
		classDialog.setInitCell(resInfo.getTypeCell());
		classDialog.setVisible(true);
		Resource uri = (Resource) classDialog.getValue();
		if (uri != null) {
			resTypeField.setText(uri.getURI());
		}
	}

	private void close() {
		if (resInfo.getURI().isAnon() || uriField.getText().length() == 0) {
			setAnonymousCell();
		}
		propDialog.setVisible(false);
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == applyButton) {
			apply();
		} else if (e.getSource() == jumpRDFSClassButton) {
			jumpRDFSClass();
		} else if (e.getSource() == selectTypeButton) {
			selectType();
		} else if (e.getSource() == closeButton) {
			close();
		}
	}
}
