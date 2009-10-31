package mr3.ui;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

import javax.swing.*;

import mr3.data.*;
import mr3.jgraph.*;

import com.hp.hpl.mesa.rdf.jena.common.*;
import com.hp.hpl.mesa.rdf.jena.model.*;
import com.jgraph.graph.*;

public class RDFPropertyPanel extends JPanel implements ActionListener {

	private JTextField uriField;
	private JButton apply;
	private JButton close;
	private JButton selectProp;
	private JButton jumpRDFSProp;
	private GraphCell edge;
	private SelectPropertyDialog propDialog;
	private List propList;
	private List validPropList;
	private AttributeDialog propWindow;
	private GraphManager gmanager;
	private RDFSInfoMap rdfsInfoMap = RDFSInfoMap.getInstance();

	private static final int listWidth = 350;
	private static final int listHeight = 40;

	public RDFPropertyPanel(GraphManager manager, AttributeDialog pw) {
		gmanager = manager;
		propWindow = pw;
		propDialog = new SelectPropertyDialog();
		setBorder(BorderFactory.createTitledBorder("Property"));

		uriField = new JTextField();
		uriField.setPreferredSize(new Dimension(listWidth, listHeight));
		uriField.setMinimumSize(new Dimension(listWidth, listHeight));
		uriField.setBorder(BorderFactory.createTitledBorder("URI"));
		uriField.addActionListener(this);

		selectProp = new JButton("Select Property");
		selectProp.addActionListener(this);
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
		c.anchor = GridBagConstraints.CENTER;
		c.gridwidth = GridBagConstraints.RELATIVE;
		gridbag.setConstraints(selectProp, c);
		add(selectProp);
		c.gridwidth = GridBagConstraints.REMAINDER;
		gridbag.setConstraints(jumpRDFSProp, c);
		add(jumpRDFSProp);
		gridbag.setConstraints(buttonGroup, c);
		add(buttonGroup);
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

	public void setPropertyList(List list) {
		propList = list;
	}

	public void setValidPropertyList(List list) {
		validPropList = list;
	}

	public void selectProperty() {
		Map propMap = new HashMap();
		Set propNameSpaces = new HashSet();

		for (Iterator i = propList.iterator(); i.hasNext();) {
			RDFSInfo info = rdfsInfoMap.getCellInfo(i.next());
			Resource uri = info.getURI();
			propNameSpaces.add(uri.getNameSpace());

			Set localNames = (Set) propMap.get(uri.getNameSpace());
			if (localNames == null) {
				localNames = new HashSet();
				propMap.put(uri.getNameSpace(), localNames);
			}
			localNames.add(uri.getLocalName());
		}

		for (Iterator i = propNameSpaces.iterator(); i.hasNext();) {
			String nameSpace = (String) i.next();
			Set localNames = (Set) propMap.get(nameSpace);
		}

		propDialog.setProperties(propNameSpaces, propMap, validPropList);
		propDialog.setVisible(true);
		setValue((String) propDialog.getValue());
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
			//			propertyCell = (GraphCell) rdfsInfoMap.getPropertyCell(uri);
			propertyCell = (GraphCell) gmanager.getPropertyCell(uri, false);
		} else {
			if (gmanager.isDuplicatedWithDialog(uri.getURI(), null, GraphType.PROPERTY)) {
				return;
			}
			if (uri.equals(MR3Resource.Nil)) {
				propertyCell = (GraphCell) gmanager.getPropertyCell(uri, false);
				JOptionPane.showMessageDialog(null, "Create Default Property.", "Warning", JOptionPane.ERROR_MESSAGE);
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
			}
		} else if (e.getSource() == jumpRDFSProp) {
			jumpRDFSProperty();
		} else if (e.getSource() == selectProp) {
			selectProperty();
		} else if (e.getSource() == close) {
			propWindow.setVisible(false);
		}
	}
}
