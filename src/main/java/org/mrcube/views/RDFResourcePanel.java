/*
 * Project Name: MR^3 (Meta-Model Management based on RDFs Revision Reflection)
 * Project Website: http://mrcube.org/
 * 
 * Copyright (C) 2003-2018 Yamaguchi Laboratory, Keio University. All rights reserved.
 * 
 * This file is part of MR^3.
 * 
 * MR^3 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * MR^3 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with MR^3.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */

package org.mrcube.views;

import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.jgraph.graph.GraphCell;
import org.jgraph.graph.GraphConstants;
import org.mrcube.MR3;
import org.mrcube.jgraph.GraphManager;
import org.mrcube.models.*;
import org.mrcube.models.MR3Constants.CreateRDFSType;
import org.mrcube.models.MR3Constants.GraphType;
import org.mrcube.models.MR3Constants.HistoryType;
import org.mrcube.models.MR3Constants.URIType;
import org.mrcube.utils.GraphUtilities;
import org.mrcube.utils.PrefixNSUtil;
import org.mrcube.utils.Translator;
import org.mrcube.utils.Utilities;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.ref.WeakReference;
import java.util.Set;

/**
 * 
 * @author Takeshi Morita
 * 
 */
public class RDFResourcePanel extends JPanel implements ListSelectionListener {

	private WeakReference<SelectResourceTypeDialog> selectResTypeDialogRef;

	private Action decideAction;

	private JList menuList;
	private JPanel menuPanel;
	private CardLayout cardLayout;

	private URIPanel uriPanel;
	private TypePanel typePanel;
	private LabelPanel labelPanel;
	private CommentPanel commentPanel;

	private JButton resetButton;
	private JButton applyButton;
	private JButton cancelButton;

	private Set<NamespaceModel> namespaceModelSet;

	private GraphCell cell;
	private RDFResourceModel resInfo;
	private GraphManager gmanager;

	private static final String WARNING = Translator.getString("Warning");
	private static final int MENU_WIDTH = 80;

	public RDFResourcePanel(GraphManager gm) {
		gmanager = gm;
		loadResourceBundle();
		decideAction = new DecideActon();
		selectResTypeDialogRef = new WeakReference<SelectResourceTypeDialog>(null);
		// setBorder(BorderFactory.createTitledBorder(Translator.getString("AttributeDialog.RDFResourceAttribute.Text")));

		uriPanel = new URIPanel();
		typePanel = new TypePanel();
		labelPanel = new LabelPanel();
		labelPanel.setGraphType(GraphType.RDF);
		commentPanel = new CommentPanel(gmanager.getRootFrame());
		commentPanel.setGraphType(GraphType.RDF);

		menuList = new JList(new Object[] { uriPanel.toString(), typePanel.toString(), MR3Constants.LABEL,
				MR3Constants.COMMENT });
		menuList.addListSelectionListener(this);
		menuPanel = new JPanel();
		cardLayout = new CardLayout();
		menuPanel.setLayout(cardLayout);
		menuPanel.add(uriPanel.toString(), uriPanel);
		menuPanel.add(typePanel.toString(), typePanel);
		menuPanel.add(MR3Constants.LABEL, labelPanel);
		menuPanel.add(MR3Constants.COMMENT, commentPanel);

		menuList.setSelectedIndex(0);

		applyButton = new JButton(MR3Constants.APPLY);
		applyButton.setMnemonic('a');
		applyButton.addActionListener(decideAction);
		resetButton = new JButton(MR3Constants.RESET);
		resetButton.setMnemonic('s');
		resetButton.addActionListener(decideAction);
		cancelButton = new JButton(MR3Constants.CANCEL);
		cancelButton.setMnemonic('c');
		cancelButton.addActionListener(decideAction);
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new GridLayout(1, 3, 5, 5));
		buttonPanel.add(applyButton);
		buttonPanel.add(resetButton);
		buttonPanel.add(cancelButton);

		setLayout(new BorderLayout());
		JPanel titlePanel = new JPanel();
		titlePanel.setBackground(new Color(49, 105, 198));
		ImageIcon icon = Utilities.getImageIcon(Translator.getString("RDFEditor.Icon"));
		JLabel titleLabel = new JLabel(Translator.getString("AttributeDialog.RDFResourceAttribute.Text"), icon,
				SwingConstants.LEFT);
		titleLabel.setForeground(Color.white);
		titleLabel.setFont(new Font("SansSerif", Font.BOLD, 13));
		titlePanel.setLayout(new BorderLayout());
		titlePanel.add(titleLabel, BorderLayout.WEST);

		add(titlePanel, BorderLayout.NORTH);

		add(menuPanel, BorderLayout.CENTER);
		add(Utilities.createTitledPanel(menuList, "", MENU_WIDTH, MENU_WIDTH), BorderLayout.WEST);
		add(Utilities.createEastPanel(buttonPanel), BorderLayout.SOUTH);
	}

	private static String ISBLANK;

	public static void loadResourceBundle() {
		ISBLANK = Translator.getString("IsBlank");
	}

	public void valueChanged(ListSelectionEvent e) {
		cardLayout.show(menuPanel, menuList.getSelectedValue().toString());
	}

	class TypePanel extends JPanel implements ActionListener {

		private JComboBox resTypePrefixBox;
		private JTextField resTypeField;
		private JCheckBox isTypeCellCheckBox;
		private JLabel resTypeNSLabel;
		private JButton selectTypeButton;
		private JButton jumpRDFSClassButton;

		TypePanel() {
			resTypePrefixBox = new JComboBox();
			resTypePrefixBox.addActionListener(this);
			JComponent resTypePrefixBoxP = Utilities.createTitledPanel(resTypePrefixBox, MR3Constants.PREFIX);

			resTypeField = new JTextField();
			JComponent resTypeFieldP = Utilities.createTitledPanel(resTypeField, Translator.getString("ResourceType")
					+ " ID");

			isTypeCellCheckBox = new JCheckBox(Translator.getString("IsType"));
			isTypeCellCheckBox.addActionListener(new IsTypeCellAction());

			JPanel resTypePanel = new JPanel();
			resTypePanel.setLayout(new GridLayout(1, 3, 5, 10));
			resTypePanel.add(resTypePrefixBoxP);
			resTypePanel.add(resTypeFieldP);
			resTypePanel.add(isTypeCellCheckBox);

			resTypeNSLabel = new JLabel();
			JComponent resTypeNSLabelP = Utilities.createTitledPanel(resTypeNSLabel, MR3Constants.NAME_SPACE);

			JPanel panel = new JPanel();
			panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
			panel.add(resTypePanel);
			panel.add(resTypeNSLabelP);
			panel.add(getTypeButtonPanel());

			setLayout(new BorderLayout());
			add(panel, BorderLayout.NORTH);
		}

		public JComboBox getResTypePrefixBox() {
			return resTypePrefixBox;
		}

		public JLabel getResTypeNSLabel() {
			return resTypeNSLabel;
		}

		public boolean isType() {
			return isTypeCellCheckBox.isSelected();
		}

		private JComponent getTypeButtonPanel() {
			selectTypeButton = new JButton(Translator.getString("SelectType"));
			selectTypeButton.addActionListener(this);
			jumpRDFSClassButton = new JButton(Translator.getString("Jump") + Translator.getString("Class"));
			jumpRDFSClassButton.addActionListener(this);

			selectTypeMode(false);

			JPanel buttonPanel = new JPanel();
			buttonPanel.setLayout(new GridLayout(1, 2, 5, 5));
			buttonPanel.add(selectTypeButton);
			buttonPanel.add(jumpRDFSClassButton);
			return Utilities.createEastPanel(buttonPanel);
		}

		public String toString() {
			return Translator.getString("Type");
		}

		private String getResourceTypeURI() {
			return resTypeNSLabel.getText() + resTypeField.getText();
		}

		public void actionPerformed(ActionEvent e) {
			if (e.getSource() == selectTypeButton) {
				selectResourceType();
			} else if (e.getSource() == jumpRDFSClassButton) {
				jumpRDFSClass();
			} else if (e.getSource() == resTypePrefixBox) {
				PrefixNSUtil.replacePrefix((String) resTypePrefixBox.getSelectedItem(), resTypeNSLabel);
			}
		}

		public void selectResourceType() {
			SelectResourceTypeDialog selectResDialog = getSelectResourceTypeDialog();
			Resource uri = (Resource) selectResDialog.getValue();
			if (uri != null) {
				String ns = Utilities.getNameSpace(uri);
				setResTypePrefix(ns);
				PrefixNSUtil.setNSLabel(resTypeNSLabel, ns);
				// setResourceTypeField(uri.getLocalName());
				setResourceTypeField(Utilities.getLocalName(uri));
			}
		}

		private void jumpRDFSClass() {
			Resource uri = ResourceFactory.createResource(getResourceTypeURI());
			if (gmanager.isEmptyURI(uri.getURI())) {
				return;
			}
			RDFSModelMap rdfsModelMap = gmanager.getCurrentRDFSInfoMap();
			if (rdfsModelMap.isClassCell(uri)) {
				Object classCell = (GraphCell) rdfsModelMap.getClassCell(uri);
				gmanager.selectClassCell(classCell);
			} else {
				JOptionPane.showMessageDialog(gmanager.getDesktopTabbedPane(),
						Translator.getString("Warning.Message3"), WARNING, JOptionPane.ERROR_MESSAGE);
			}
		}

		private void setResourceTypeField(String uri) {
			resTypeField.setText(uri);
			resTypeField.setToolTipText(uri);
		}

		// RDF���\�[�X�̃^�C�v�����݂���΃`�F�b�N�{�^���Ƀ`�F�b�N����D
		public void selectTypeMode(boolean t) {
			isTypeCellCheckBox.setSelected(t);
			if (t) {
				// setResourceTypeField(resInfo.getType().getLocalName());
				setResourceTypeField(Utilities.getLocalName(resInfo.getType()));
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
	}

	class URIPanel extends JPanel {

		private JCheckBox isAnonBox;
		private JTextField uriField;
		private JComboBox resPrefixBox;

		URIPanel() {
			resPrefixBox = new JComboBox();
			resPrefixBox.addActionListener(new ChangePrefixAction());
			JComponent resPrefixBoxP = Utilities.createTitledPanel(resPrefixBox, MR3Constants.PREFIX);

			isAnonBox = new JCheckBox(ISBLANK);
			isAnonBox.addActionListener(new IsAnonAction());
			JPanel resPanel = new JPanel();
			resPanel.setLayout(new GridLayout(1, 2));
			resPanel.add(resPrefixBoxP);
			resPanel.add(isAnonBox);

			uriField = new JTextField();
			uriField.addActionListener(decideAction);
			JComponent uriFieldP = Utilities.createTitledPanel(uriField, Translator.getString("RDFResource"));

			JPanel panel = new JPanel();
			panel.setLayout(new GridLayout(2, 1, 5, 10));
			panel.add(resPanel);
			panel.add(uriFieldP);

			setLayout(new BorderLayout());
			add(panel, BorderLayout.NORTH);
		}

		public JTextField getURIField() {
			return uriField;
		}

		public void setResPrefixBox() {
			resPrefixBox.setModel(new DefaultComboBoxModel(PrefixNSUtil.getPrefixes().toArray()));
		}

		public JComboBox getResPrefixBox() {
			return resPrefixBox;
		}

		public boolean isAnon() {
			return isAnonBox.isSelected();
		}

		class IsAnonAction extends AbstractAction {
			public void actionPerformed(ActionEvent e) {
				setURIField("", !isAnonBox.isSelected());
			}
		}

		public String toString() {
			return "URI";
		}

		// URI�̏d���ƁC��̃`�F�b�N������D
		public boolean isErrorResource() {
			String uri = getResourceURI();
			if (isAnonBox.isSelected()) {
				return false;
			}
			if (isLocalDuplicated(uri) || gmanager.isEmptyURI(uri)
					|| gmanager.isDuplicatedWithDialog(uri, cell, GraphType.RDF)) {
				return true;
			}
			return false;
		}

		public String getResourceURI() {
			return uriField.getText();
		}

		public void setURI(String resURI) {
			if (resInfo.getURIType() == URIType.ANONYMOUS) {
				setURIField("", false);
			} else {
				setURIField(resURI, true);
			}
		}

		private void setURIField(String uri) {
			uriField.setText(uri);
			uriField.setToolTipText(uri);
		}

		private void setURIField(String uri, boolean t) {
			setURIField(uri);
			uriField.setEditable(t);
			resPrefixBox.setEnabled(t);
			isAnonBox.setSelected(!t);
		}
	}

	class ChangePrefixAction implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			if (e.getSource() == uriPanel.getResPrefixBox()) {
				String ns = PrefixNSUtil.getNameSpace((String) uriPanel.getResPrefixBox().getSelectedItem());
				// String id =
				// ResourceFactory.createResource(uriPanel.getResourceURI()).getLocalName();
				String id = Utilities.getLocalName(ResourceFactory.createResource(uriPanel.getResourceURI()));
				uriPanel.setURIField(ns + id);
			}
		}
	}

	private void setResPrefix() {
		if (resInfo.getURIType() == URIType.URI) {
			for (NamespaceModel prefNSInfo : namespaceModelSet) {
				if (prefNSInfo.getNameSpace().equals(Utilities.getNameSpace(resInfo.getURI()))) {
					uriPanel.getResPrefixBox().setSelectedItem(prefNSInfo.getPrefix());
					break;
				}
			}
		}
	}

	private void setResTypePrefix() {
		GraphCell cell = resInfo.getTypeCell();
		if (cell == null || cell.getAttributes() == null) {
			setResTypePrefix(gmanager.getBaseURI());
			PrefixNSUtil.setNSLabel(typePanel.getResTypeNSLabel(), gmanager.getBaseURI());
			return;
		}
		RDFSModel rdfsModel = (RDFSModel) GraphConstants.getValue(cell.getAttributes());
		setResTypePrefix(rdfsModel.getURI().getNameSpace());
	}

	private void setResTypePrefix(String ns) {
		for (NamespaceModel prefNSInfo : namespaceModelSet) {
			if (prefNSInfo.getNameSpace().equals(ns)) {
				typePanel.getResTypePrefixBox().setSelectedItem(prefNSInfo.getPrefix());
				break;
			}
		}
	}

	public void setValue(GraphCell c) {
		cell = c;
		resInfo = (RDFResourceModel) GraphConstants.getValue(cell.getAttributes());
		labelPanel.clearField();
		labelPanel.setResourceInfo(resInfo);
		commentPanel.setResourceInfo(resInfo);
		namespaceModelSet = GraphUtilities.getNamespaceModelSet();
		PrefixNSUtil.setNamespaceModelSet(namespaceModelSet);
		uriPanel.setResPrefixBox();
		typePanel.getResTypePrefixBox().setModel(new DefaultComboBoxModel(PrefixNSUtil.getPrefixes().toArray()));
		setResPrefix();
		setResTypePrefix();
		uriPanel.setURI(resInfo.getURIStr());
		typePanel.selectTypeMode(resInfo.hasType());
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				uriPanel.getURIField().requestFocus();
			}
		});
	}

	private void setCellValue() {
		// setURI()������O��URI�^�C�v��ύX����K�v����DURI�^�C�v�ɂ���ď����ᕪ���Ă��邽��
		if (uriPanel.isAnon()) {
			resInfo.setURIType(URIType.ANONYMOUS);
			resInfo.setURI(ResourceFactory.createResource().toString());
		} else {
			resInfo.setURIType(URIType.URI);
			resInfo.setURI(uriPanel.getResourceURI());
		}
		GraphConstants.setValue(cell.getAttributes(), resInfo);
		GraphUtilities.resizeRDFResourceCell(gmanager, resInfo, cell);
	}

	public SelectResourceTypeDialog getSelectResourceTypeDialog() {
		SelectResourceTypeDialog result = selectResTypeDialogRef.get();
		if (result == null) {
			result = new SelectResourceTypeDialog(gmanager);
			selectResTypeDialogRef = new WeakReference<SelectResourceTypeDialog>(result);
		}
		result.replaceGraph(gmanager.getCurrentClassGraph());
		result.setInitCell(resInfo.getTypeCell());
		result.setVisible(true);
		return result;
	}

	class DecideActon extends AbstractAction {

		private GraphCell getResourceType() {
			GraphCell typeCell = null;
			Resource uri = ResourceFactory.createResource(typePanel.getResourceTypeURI());
			if (gmanager.isEmptyURI(uri.getURI())) {
				return null;
			}
			RDFSModelMap rdfsModelMap = gmanager.getCurrentRDFSInfoMap();
			if (rdfsModelMap.isClassCell(uri)) {
				typeCell = gmanager.getClassCell(uri, false);
			} else {
				if (gmanager.isDuplicatedWithDialog(uri.getURI(), null, GraphType.CLASS)) {
					return null;
				}
				if (MR3.OFF_META_MODEL_MANAGEMENT) {
					return null;
				}
				if (resInfo.getTypeCell() == null) {
					int ans = JOptionPane.showConfirmDialog(gmanager.getDesktopTabbedPane(),
							Translator.getString("Warning.Message2"), WARNING, JOptionPane.YES_NO_OPTION);
					if (ans == JOptionPane.YES_OPTION) {
						Set supClasses = gmanager.getSupRDFS(gmanager.getCurrentClassGraph(),
								Translator.getString("SelectSupClassesDialog.Title"));
						if (supClasses == null) {
							return null;
						}
						typeCell = (GraphCell) gmanager.insertSubRDFS(uri, supClasses, gmanager.getCurrentClassGraph());
						HistoryManager
								.saveHistory(HistoryType.META_MODEL_MANAGEMNET_REPLACE_RESOURCE_TYPE_WITH_CREATE_CLASS);
					}
				} else {
					// OntManagementDialog dialog = new
					// OntManagementDialog(gmanager);
					MetaModelManagementDialog dialog = new MetaModelManagementDialog(gmanager);
					// dialog.replaceGraph(gmanager.getClassGraph());
					// dialog.setRegionSet(new HashSet());
					dialog.setVisible(true);
					CreateRDFSType createType = dialog.getCreateRDFSType();
					if (createType == CreateRDFSType.CREATE) {
						// Set supClasses = dialog.getSupRDFSSet();
						typeCell = (GraphCell) gmanager.insertSubRDFS(uri, null, gmanager.getCurrentClassGraph());
						HistoryManager
								.saveHistory(HistoryType.META_MODEL_MANAGEMNET_REPLACE_RESOURCE_TYPE_WITH_CREATE_CLASS);
					} else if (createType == CreateRDFSType.RENAME) {
						RDFSModel rdfsModel = resInfo.getTypeInfo();
						typeCell = resInfo.getTypeCell();
						rdfsModelMap.removeURICellMap(rdfsModel);
						rdfsModel.setURI(uri.getURI());
						GraphUtilities.resizeRDFSResourceCell(gmanager, rdfsModel, typeCell);
						rdfsModelMap.putURICellMap(rdfsModel, typeCell);
						gmanager.selectChangedRDFCells(rdfsModel); // RDF�O���t�̕\�����e���X�V
						HistoryManager
								.saveHistory(HistoryType.META_MODEL_MANAGEMNET_REPLACE_RESOURCE_TYPE_WITH_REPLACE_CLASS);
					} else if (createType == null) {
						return null;
					}
				}
			}
			return typeCell;
		}

		private void setResourceType(GraphCell typeCell) {
			resInfo.setTypeCell(typeCell, gmanager.getCurrentRDFGraph());
			typePanel.setResourceTypeField(resInfo.getType().getURI());
		}

		public void actionPerformed(ActionEvent e) {
			if (e.getSource() == applyButton) {
				apply();
			} else if (e.getSource() == resetButton) {
				gmanager.getCurrentRDFGraph().setSelectionCell(cell);
			} else if (e.getSource() == cancelButton) {
				gmanager.setVisibleAttrDialog(false);
			}
		}

		private void apply() {
			if (uriPanel.isErrorResource()) {
				return;
			}
			RDFResourceModel beforeInfo = new RDFResourceModel(resInfo);
			if (typePanel.isType()) {
				GraphCell resTypeCell = getResourceType();
				if (resTypeCell != null) {
					setResourceType(resTypeCell);
					if (!gmanager.getCurrentRDFEditor().isEditMode()) {
						gmanager.selectClassCell(resTypeCell); // �Ή�����N���X��I������
					}
				}
			} else {
				setResourceType(null);
			}
			setCellValue();
			gmanager.selectRDFCell(cell); // AttributeDialog�̕\����RDF���\�[�X�ɖ߂�
			HistoryManager.saveHistory(HistoryType.EDIT_RESOURCE_WITH_DIALOG, beforeInfo, resInfo);
		}
	}

	private boolean isLocalDuplicated(String tmpURI) {
		String tmpResTypeURI = "";
		if (typePanel.isType()) {
			tmpResTypeURI = typePanel.getResourceTypeURI();
		}
		if (tmpURI.equals(tmpResTypeURI)) {
			JOptionPane.showMessageDialog(this, Translator.getString("Warning.Message1"), WARNING,
					JOptionPane.ERROR_MESSAGE);
			return true;
		}
		return false;
	}
}
