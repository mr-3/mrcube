package mr3.ui;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

import javax.swing.*;
import javax.swing.event.*;

import mr3.data.*;
import mr3.jgraph.*;

import com.hp.hpl.mesa.rdf.jena.model.*;
import com.jgraph.graph.*;

/**
 *
 * @auther takeshi morita
 */
public abstract class RDFSPanel extends JPanel {

	protected JButton apply;
	protected JButton close;

	protected JRadioButton uriButton;
	protected JRadioButton idButton;

	protected URIType uriType;

	protected JTextField uriField;
	protected JTextField labelField;
	protected JTextField isDefinedBy;
	protected JTextArea comment;
	protected JScrollPane commentScroll;
	protected JTabbedPane metaTab;
	protected RDFGraph graph;
	protected GraphManager gmanager;
	protected GraphCell cell;
	protected RDFSInfo rdfsInfo;
	protected RDFSInfoMap rdfsInfoMap = RDFSInfoMap.getInstance();
	protected static Object[] ZERO = new Object[0];
	protected static final int listWidth = 350;
	protected static final int fieldHeight = 40;
	protected static final int listHeight = 80;

	protected JComboBox labelLangBox;
	protected JTextField labelLangField;
	protected JButton editLabelButton;
	protected JButton addLabelButton;
	protected JButton removeLabelButton;

	protected JComboBox commentLangBox;
	protected JTextField commentLangField;
	protected JButton editCommentButton;
	protected JButton addCommentButton;
	protected JButton removeCommentButton;

	protected JList instanceList;
	protected JScrollPane instanceListScroll;
	private static final int instanceListHeight = 200;
	private static final int LangFieldLength = 4;

	public RDFSPanel(RDFGraph g, GraphManager manager) {
		graph = g;
		gmanager = manager;
		initInstancesList();
		apply = new JButton("Apply");
		close = new JButton("Close");
		close.addActionListener(new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				gmanager.setVisibleAttrDialog(false);
			}
		});
		metaTab = new JTabbedPane();
	}

	protected void initComponent(JComponent component, String title, int width, int height) {
		component.setPreferredSize(new Dimension(width, height));
		component.setMinimumSize(new Dimension(width, height));
		component.setBorder(BorderFactory.createTitledBorder(title));
	}

	class RadioAction implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			String type = (String) e.getActionCommand();
			uriType = URIType.getURIType(type);

			if (uriType == URIType.ID) {
				uriField.setEditable(true);
				if (uriField.getText().length() == 0 || uriField.getText().charAt(0) != '#') {
					uriField.setText('#' + uriField.getText());
				}
			} else if (uriType == URIType.URI) {
				uriField.setEditable(true);
			}
		}
	}

	protected void setBaseTab() {
		uriButton = new JRadioButton("URI");
		idButton = new JRadioButton("ID");
		RadioAction ra = new RadioAction();
		uriButton.addActionListener(ra);
		idButton.addActionListener(ra);
		ButtonGroup group = new ButtonGroup();
		group.add(uriButton);
		group.add(idButton);
		JPanel uriTypeGroupPanel = new JPanel();
		uriTypeGroupPanel.setBorder(BorderFactory.createTitledBorder("URI Type"));
		uriTypeGroupPanel.setPreferredSize(new Dimension(130, 55));
		uriTypeGroupPanel.setMinimumSize(new Dimension(130, 55));
		uriTypeGroupPanel.add(uriButton);
		uriTypeGroupPanel.add(idButton);

		uriField = new JTextField();
		initComponent(uriField, "URI", listWidth, fieldHeight);

		isDefinedBy = new JTextField();
		initComponent(isDefinedBy, "isDefinedBy", listWidth, fieldHeight);

		labelLangField = new JTextField(LangFieldLength);
		initComponent(labelLangField, "Lang", 50, fieldHeight);
		labelField = new JTextField();
		initComponent(labelField, "Label", 300, fieldHeight);
		JPanel labelGroup = new JPanel();
		labelGroup.add(labelLangField);
		labelGroup.add(labelField);

		labelLangBox = new JComboBox();
		labelLangBox.addActionListener(new SelectLangAction());
		labelLangBox.setPreferredSize(new Dimension(70, 25));
		labelLangBox.setMinimumSize(new Dimension(70, 25));
		editLabelButton = new JButton("edit");
		editLabelButton.addActionListener(new EditLiteralAction());
		addLabelButton = new JButton("add");
		addLabelButton.addActionListener(new AddLiteralAction());
		removeLabelButton = new JButton("remove");
		removeLabelButton.addActionListener(new RemoveLiteralAction());
		JPanel labelButtonGroup = new JPanel();
		labelButtonGroup.add(labelLangBox);
		labelButtonGroup.add(editLabelButton);
		labelButtonGroup.add(addLabelButton);
		labelButtonGroup.add(removeLabelButton);

		commentLangField = new JTextField(LangFieldLength);
		initComponent(commentLangField, "Lang", 50, fieldHeight);
		comment = new JTextArea(5, 15); // 縦,横        
		commentScroll = new JScrollPane(comment);
		initComponent(commentScroll, "Comment", 300, listHeight);
		JPanel commentGroup = new JPanel();
		commentGroup.add(commentLangField);
		commentGroup.add(commentScroll);

		commentLangBox = new JComboBox();
		commentLangBox.addActionListener(new SelectLangAction());
		commentLangBox.setPreferredSize(new Dimension(70, 25));
		editCommentButton = new JButton("edit");
		editCommentButton.addActionListener(new EditLiteralAction());
		addCommentButton = new JButton("add");
		addCommentButton.addActionListener(new AddLiteralAction());
		removeCommentButton = new JButton("remove");
		removeCommentButton.addActionListener(new RemoveLiteralAction());
		JPanel commentButtonGroup = new JPanel();
		commentButtonGroup.add(commentLangBox);
		commentButtonGroup.add(editCommentButton);
		commentButtonGroup.add(addCommentButton);
		commentButtonGroup.add(removeCommentButton);

		JPanel inline = new JPanel();
		GridBagLayout gridbag = new GridBagLayout();
		GridBagConstraints c = new GridBagConstraints();
		inline.setLayout(gridbag);
		c.anchor = GridBagConstraints.WEST;
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.weighty = 10;

		gridbag.setConstraints(uriTypeGroupPanel, c);
		inline.add(uriTypeGroupPanel);

		c.anchor = GridBagConstraints.PAGE_START;

		gridbag.setConstraints(uriField, c);
		inline.add(uriField);
		//		gridbag.setConstraints(isDefinedBy, c);
		//		inline.add(isDefinedBy);

		gridbag.setConstraints(labelGroup, c);
		inline.add(labelGroup);
		c.anchor = GridBagConstraints.WEST;
		gridbag.setConstraints(labelButtonGroup, c);
		inline.add(labelButtonGroup);

		c.anchor = GridBagConstraints.CENTER;
		gridbag.setConstraints(commentGroup, c);
		inline.add(commentGroup);
		c.anchor = GridBagConstraints.WEST;
		gridbag.setConstraints(commentButtonGroup, c);
		inline.add(commentButtonGroup);

		metaTab.addTab("Base", inline);
	}

	private void initInstancesList() {
		instanceList = new JList();
		instanceList.addListSelectionListener(new InstanceAction());
		instanceListScroll = new JScrollPane(instanceList);
		instanceListScroll.setPreferredSize(new Dimension(listWidth, instanceListHeight));
		instanceListScroll.setMinimumSize(new Dimension(listWidth, instanceListHeight));
		instanceListScroll.setBorder(BorderFactory.createTitledBorder("Instances"));
	}

	public void setCell(GraphCell cell) {
		this.cell = cell;
	}

	private void clearField() {
		labelField.setText("");
		labelLangField.setText("");
		comment.setText("");
		commentLangField.setText("");
	}

	public void setValue() {
		uriField.setText(rdfsInfo.getURIStr());
		uriType = rdfsInfo.getURIType();
		if (uriType == URIType.URI) {
			uriButton.setSelected(true);
		} else {
			idButton.setSelected(true);
		}

		isDefinedBy.setText(rdfsInfo.getIsDefinedBy().getURI());
		clearField();

		ComboBoxModel model = getLabelComboBoxModel(rdfsInfo.getLabelList());
		labelLangBox.setModel(model);

		model = getCommentComboBoxModel(rdfsInfo.getCommentList());
		commentLangBox.setModel(model);
	}

	private boolean isLastLiteral(MR3Literal literal, MR3Literal lastLiteral) throws RDFException {
		return lastLiteral.equals(literal);
	}

	protected ComboBoxModel getLabelComboBoxModel(List list) {
		DefaultComboBoxModel comboModel = new DefaultComboBoxModel();
		for (Iterator i = list.iterator(); i.hasNext();) {
			MR3Literal literal = (MR3Literal) i.next();
			LiteralLang lang = new LiteralLang(literal);
			comboModel.addElement(lang);
			try {
				if (isLastLiteral(literal, rdfsInfo.getLabel())) {
					labelField.setText(literal.getString());
					labelLangField.setText(literal.getLanguage());
					comboModel.setSelectedItem(lang);
				}
			} catch (RDFException rdfex) {
				rdfex.printStackTrace();
			}
		}
		return comboModel;
	}

	protected ComboBoxModel getCommentComboBoxModel(List list) {
		DefaultComboBoxModel comboModel = new DefaultComboBoxModel();
		for (Iterator i = list.iterator(); i.hasNext();) {
			MR3Literal literal = (MR3Literal) i.next();
			LiteralLang lang = new LiteralLang(literal);
			comboModel.addElement(lang);
			try {
				if (isLastLiteral(literal, rdfsInfo.getComment())) {
					comment.setText(literal.getString());
					commentLangField.setText(literal.getLanguage());
					comboModel.setSelectedItem(lang);
				}
			} catch (RDFException rdfex) {
				rdfex.printStackTrace();
			}
		}
		return comboModel;
	}

	class EditLiteralAction extends AbstractAction {
		public void actionPerformed(ActionEvent e) {
			if (e.getSource() == editLabelButton) {
				if (labelLangBox.getItemCount() == 0 || labelLangBox.getSelectedItem() == null)
					return;
				DefaultComboBoxModel comboModel = (DefaultComboBoxModel) labelLangBox.getModel();
				LiteralLang addElement = new LiteralLang(new MR3Literal(labelField.getText(), labelLangField.getText()));
				LiteralLang rmElement = (LiteralLang) comboModel.getSelectedItem();
				comboModel.removeElement(rmElement);
				rdfsInfo.removeLabel(rmElement.getLiteral());
				comboModel.addElement(addElement);
				comboModel.setSelectedItem(addElement);
				rdfsInfo.addLabel(addElement.getLiteral());
			} else if (e.getSource() == editCommentButton) {
				if (commentLangBox.getItemCount() == 0 || commentLangBox.getSelectedItem() == null)
					return;
				DefaultComboBoxModel comboModel = (DefaultComboBoxModel) commentLangBox.getModel();
				LiteralLang addElement = new LiteralLang(new MR3Literal(comment.getText(), commentLangField.getText()));
				LiteralLang rmElement = (LiteralLang) comboModel.getSelectedItem();
				comboModel.removeElement(rmElement);
				rdfsInfo.removeComment(rmElement.getLiteral());
				comboModel.addElement(addElement);
				comboModel.setSelectedItem(addElement);
				rdfsInfo.addLabel(addElement.getLiteral());
			}
		}
	}

	class AddLiteralAction extends AbstractAction {
		public void actionPerformed(ActionEvent e) {
			if (e.getSource() == addLabelButton) {
				DefaultComboBoxModel comboModel = (DefaultComboBoxModel) labelLangBox.getModel();
				MR3Literal literal = new MR3Literal(labelField.getText(), labelLangField.getText());
				LiteralLang addElement = new LiteralLang(literal);
				comboModel.addElement(addElement);
				comboModel.setSelectedItem(addElement);
				rdfsInfo.addLabel(literal);
			} else if (e.getSource() == addCommentButton) {
				DefaultComboBoxModel comboModel = (DefaultComboBoxModel) commentLangBox.getModel();
				MR3Literal literal = new MR3Literal(comment.getText(), commentLangField.getText());
				LiteralLang addElement = new LiteralLang(literal);
				comboModel.addElement(addElement);
				comboModel.setSelectedItem(addElement);
				rdfsInfo.addComment(literal);
			}
		}
	}

	class RemoveLiteralAction extends AbstractAction {
		public void actionPerformed(ActionEvent e) {
			if (e.getSource() == removeLabelButton) {
				if (labelLangBox.getItemCount() == 0 || labelLangBox.getSelectedItem() == null)
					return;
				DefaultComboBoxModel comboModel = (DefaultComboBoxModel) labelLangBox.getModel();
				LiteralLang lang = (LiteralLang) comboModel.getSelectedItem();
				comboModel.removeElement(lang);
				rdfsInfo.removeLabel(lang.getLiteral());
			} else if (e.getSource() == removeCommentButton) {
				if (commentLangBox.getItemCount() == 0 || commentLangBox.getSelectedItem() == null)
					return;
				DefaultComboBoxModel comboModel = (DefaultComboBoxModel) commentLangBox.getModel();
				LiteralLang lang = (LiteralLang) comboModel.getSelectedItem();
				comboModel.removeElement(lang);
				rdfsInfo.removeComment(lang.getLiteral());
			}
		}
	}

	class SelectLangAction extends AbstractAction {
		public void actionPerformed(ActionEvent e) {
			if (e.getSource() == labelLangBox) {
				if (labelLangBox.getItemCount() == 0 || labelLangBox.getSelectedItem() == null)
					return;
				LiteralLang lang = (LiteralLang) labelLangBox.getSelectedItem();
				MR3Literal literal = lang.getLiteral();
				labelLangField.setText(literal.getLanguage());
				labelField.setText(literal.getString());
				rdfsInfo.setLabel(literal); // 最後にセットしたラベル
			} else if (e.getSource() == commentLangBox) {
				if (commentLangBox.getItemCount() == 0 || commentLangBox.getSelectedItem() == null)
					return;
				LiteralLang lang = (LiteralLang) commentLangBox.getSelectedItem();
				MR3Literal literal = lang.getLiteral();
				commentLangField.setText(literal.getLanguage());
				comment.setText(literal.getString());
				rdfsInfo.setComment(literal); // 最後にセットしたコメント
			}
		}
	}

	class LiteralLang {
		private String lang;
		private MR3Literal literal;

		LiteralLang(MR3Literal literal) {
			this.literal = literal;
			if (literal.getLanguage().length() != 0) {
				lang = literal.getLanguage();
			} else {
				lang = "---";
			}
		}

		public MR3Literal getLiteral() {
			return literal;
		}

		public String toString() {
			return lang;
		}
	}

	abstract public void setValue(Set supCellSet);

	public void changeInfo() {
		String uri = uriField.getText();
		String tmpURI = "";
		if (uriType == URIType.ID) {
			tmpURI = gmanager.getBaseURI(); // チェックする時は，フルパスで．
		}
		tmpURI = tmpURI + uri;
		if (gmanager.isEmptyURI(tmpURI) || gmanager.isDuplicatedWithDialog(tmpURI, cell, graph.getType())) {
			return;
		} else {
			rdfsInfoMap.removeURICellMap(rdfsInfo); // ここで，URIとセルのマッピングを削除する
			rdfsInfo.setURI(uriField.getText());
			rdfsInfo.setURIType(uriType);
			rdfsInfo.setIsDefinedby(isDefinedBy.getText());
			rdfsInfoMap.putURICellMap(rdfsInfo, cell);
			gmanager.setCellValue(cell, rdfsInfo.getURIStr());
		}
	}

	class ChangeInfoAction implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			if (cell != null) {
				changeInfo();
				gmanager.changeCellView();
			} else {
				System.out.println("cell is null");
			}
		}
	}

	/** スーパークラスまたは、スーパープロパティの名前のセットを返す */
	protected Object[] getTargetInfo(Set supCellSet) {
		Set result = new HashSet();
		for (Iterator i = supCellSet.iterator(); i.hasNext();) {
			RDFSInfo supInfo = rdfsInfoMap.getCellInfo(i.next());
			result.add(supInfo.getURIStr());
		}
		return result.toArray();
	}

	abstract public void setInstanceList();

	protected class InstanceAction implements ListSelectionListener {
		public void valueChanged(ListSelectionEvent e) {
			Object cell = instanceList.getSelectedValue();
			gmanager.jumpRDFArea(cell);
		}
	}

	public void displayRDFSInfo(DefaultGraphCell cell) {
		if (graph.isRDFSCell(cell)) {
			rdfsInfo = rdfsInfoMap.getCellInfo(cell);
			if (rdfsInfo != null) {
				setCell(cell);
				setInstanceList();
				Set targetCells = graph.getTargetCells(cell);
				setValue(targetCells);
				rdfsInfo.setSupRDFS(targetCells);
			}
		}
	}
}
