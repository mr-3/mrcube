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

import org.jgraph.graph.*;

import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.rdf.model.impl.*;

/**
 *
 * @author takeshi morita
 */
public abstract class RDFSPanel extends JPanel {

	protected JButton apply;
	protected JButton close;

	protected JComboBox metaClassBox;

	protected JComboBox uriPrefixBox;
	protected JTextField idField;
	protected JLabel nsLabel;
	protected Set prefixNSInfoSet;

	protected JTextField labelField;
	protected JTextField isDefinedBy;
	protected JTextArea commentArea;
	protected JScrollPane commentScroll;
	protected JTabbedPane metaTab;
	protected RDFGraph graph;
	protected GraphManager gmanager;
	protected GraphCell cell;
	protected RDFSInfo rdfsInfo;
	protected RDFSInfoMap rdfsInfoMap = RDFSInfoMap.getInstance();

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

	private static final int LANG_FIELD_LENGTH = 4;
	protected static final int LIST_WIDTH = 350;
	protected static final int FIELD_HEIGHT = 40;
	protected static final int LIST_HEIGHT = 80;
	private static final int INSTANCE_LIST_HEIGHT = 120;
	protected static Object[] ZERO = new Object[0];

	private static final int BOX_WIDTH = 70;
	private static final int BOX_HEIGHT = 30;
	private static final int ID_BOX_WIDTH = 200;
	private static final int PREFIX_BOX_WIDTH = 150;
	private static final int PREFIX_BOX_HEIGHT = 50;

	private static final String EDIT = Translator.getString("Edit");
	private static final String ADD = Translator.getString("Add");
	private static final String REMOVE = Translator.getString("Remove");

	public RDFSPanel(RDFGraph g, GraphManager manager) {
		graph = g;
		gmanager = manager;
		initInstancesList();
		apply = new JButton(Translator.getString("Apply"));
		apply.addActionListener(new ChangeInfoAction());
		close = new JButton(Translator.getString("Close"));
		close.addActionListener(new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				gmanager.setVisibleAttrDialog(false);
			}
		});
		metaTab = new JTabbedPane();
	}

	protected void setBaseTab() {
		metaClassBox = new JComboBox();
		Utilities.initComponent(metaClassBox, Translator.getString("ResourceType"), LIST_WIDTH, PREFIX_BOX_HEIGHT);

		uriPrefixBox = new JComboBox();
		uriPrefixBox.addActionListener(new ChangePrefixAction());
		Utilities.initComponent(uriPrefixBox, MR3Constants.PREFIX, PREFIX_BOX_WIDTH, PREFIX_BOX_HEIGHT);

		idField = new JTextField();
		Utilities.initComponent(idField, "ID", ID_BOX_WIDTH, FIELD_HEIGHT);

		JPanel uriPanel = new JPanel();
		uriPanel.add(uriPrefixBox);
		uriPanel.add(idField);

		nsLabel = new JLabel("");
		Utilities.initComponent(nsLabel, MR3Constants.NAME_SPACE, LIST_WIDTH, FIELD_HEIGHT);

		isDefinedBy = new JTextField();
		Utilities.initComponent(isDefinedBy, "isDefinedBy", LIST_WIDTH, FIELD_HEIGHT);

		labelLangField = new JTextField(LANG_FIELD_LENGTH);
		Utilities.initComponent(labelLangField, Translator.getString("Lang"), BOX_WIDTH, FIELD_HEIGHT);
		labelField = new JTextField();
		Utilities.initComponent(labelField, Translator.getString("Label"), 300, FIELD_HEIGHT);
		JPanel labelGroup = new JPanel();
		labelGroup.add(labelLangField);
		labelGroup.add(labelField);

		labelLangBox = new JComboBox();
		labelLangBox.addActionListener(new SelectLangAction());
		labelLangBox.setPreferredSize(new Dimension(BOX_WIDTH, BOX_HEIGHT));
		labelLangBox.setMinimumSize(new Dimension(BOX_WIDTH, BOX_HEIGHT));
		editLabelButton = new JButton(EDIT);
		editLabelButton.addActionListener(new EditLiteralAction());
		addLabelButton = new JButton(ADD);
		addLabelButton.addActionListener(new AddLiteralAction());
		removeLabelButton = new JButton(REMOVE);
		removeLabelButton.addActionListener(new RemoveLiteralAction());
		JPanel labelButtonGroup = new JPanel();
		labelButtonGroup.add(labelLangBox);
		labelButtonGroup.add(editLabelButton);
		labelButtonGroup.add(addLabelButton);
		labelButtonGroup.add(removeLabelButton);

		JPanel basePanel = new JPanel();
		GridBagLayout gridbag = new GridBagLayout();
		GridBagConstraints c = new GridBagConstraints();
		basePanel.setLayout(gridbag);
		c.weightx = c.weighty = 3;
		c.anchor = GridBagConstraints.CENTER;
		c.gridwidth = GridBagConstraints.REMAINDER;

		gridbag.setConstraints(metaClassBox, c);
		basePanel.add(metaClassBox);
		gridbag.setConstraints(uriPanel, c);
		basePanel.add(uriPanel);
		gridbag.setConstraints(nsLabel, c);
		basePanel.add(nsLabel);
		//		gridbag.setConstraints(isDefinedBy, c);
		//		inline.add(isDefinedBy);

		basePanel.setLayout(gridbag);
		gridbag.setConstraints(labelGroup, c);
		basePanel.add(labelGroup);
		gridbag.setConstraints(labelButtonGroup, c);
		basePanel.add(labelButtonGroup);

		metaTab.addTab(Translator.getString("Base"), basePanel);
	}

	protected void setCommentTab() {
		commentLangField = new JTextField(LANG_FIELD_LENGTH);
		Utilities.initComponent(commentLangField, Translator.getString("Lang"), BOX_WIDTH, FIELD_HEIGHT);
		commentArea = new JTextArea(5, 20); // 縦,横        
		commentScroll = new JScrollPane(commentArea);
		Utilities.initComponent(commentScroll, Translator.getString("Comment"), 300, 150);
		JPanel commentPanel = new JPanel();
		commentPanel.add(commentLangField);
		commentPanel.add(commentScroll);

		commentLangBox = new JComboBox();
		commentLangBox.addActionListener(new SelectLangAction());
		commentLangBox.setPreferredSize(new Dimension(BOX_WIDTH, BOX_HEIGHT));
		editCommentButton = new JButton(EDIT);
		editCommentButton.addActionListener(new EditLiteralAction());
		addCommentButton = new JButton(ADD);
		addCommentButton.addActionListener(new AddLiteralAction());
		removeCommentButton = new JButton(REMOVE);
		removeCommentButton.addActionListener(new RemoveLiteralAction());
		JPanel commentButtonGroup = new JPanel();
		commentButtonGroup.add(commentLangBox);
		commentButtonGroup.add(editCommentButton);
		commentButtonGroup.add(addCommentButton);
		commentButtonGroup.add(removeCommentButton);

		JPanel inlinePanel = new JPanel();
		GridBagLayout gridbag = new GridBagLayout();
		GridBagConstraints c = new GridBagConstraints();
		c.weightx = c.weighty = 3;
		c.anchor = GridBagConstraints.CENTER;
		c.gridwidth = GridBagConstraints.REMAINDER;

		c.anchor = GridBagConstraints.CENTER;
		gridbag.setConstraints(commentPanel, c);
		inlinePanel.add(commentPanel);
		c.anchor = GridBagConstraints.WEST;
		gridbag.setConstraints(commentButtonGroup, c);
		inlinePanel.add(commentButtonGroup);
		metaTab.addTab(Translator.getString("Comment"), inlinePanel);
	}

	class ChangePrefixAction extends AbstractAction {
		public void actionPerformed(ActionEvent e) {
			PrefixNSUtil.replacePrefix((String) uriPrefixBox.getSelectedItem(), nsLabel);
		}
	}

	private void initInstancesList() {
		instanceList = new JList();
		instanceList.addListSelectionListener(new InstanceAction());
		instanceListScroll = new JScrollPane(instanceList);
		instanceListScroll.setPreferredSize(new Dimension(LIST_WIDTH, INSTANCE_LIST_HEIGHT));
		instanceListScroll.setMinimumSize(new Dimension(LIST_WIDTH, INSTANCE_LIST_HEIGHT));
		instanceListScroll.setBorder(BorderFactory.createTitledBorder(Translator.getString("Instances")));
	}

	public void setCell(GraphCell cell) {
		this.cell = cell;
	}

	private void clearField() {
		labelField.setText("");
		labelLangField.setText("");
		commentArea.setText("");
		commentLangField.setText("");
	}

	public void setValue() {
		idField.setText(rdfsInfo.getLocalName());
		//		isDefinedBy.setText(rdfsInfo.getIsDefinedBy().getURI());
		clearField();

		ComboBoxModel model = getLabelComboBoxModel(rdfsInfo.getLabelList());
		labelLangBox.setModel(model);

		model = getCommentComboBoxModel(rdfsInfo.getCommentList());
		commentLangBox.setModel(model);
	}

	private boolean isLastLiteral(MR3Literal literal, MR3Literal lastLiteral) throws RDFException {
		return lastLiteral.equals(literal);
	}

	protected void setMetaClassBox(Set metaClassList) {
		ComboBoxModel model = new DefaultComboBoxModel(metaClassList.toArray());
		metaClassBox.setModel(model);
		metaClassBox.setSelectedItem(new ResourceImpl(rdfsInfo.getMetaClass()));
		metaClassBox.setEnabled(!metaClassList.contains(rdfsInfo.getURI()));
	}

	protected ComboBoxModel getLabelComboBoxModel(List list) {
		MR3Literal defaultLiteral = rdfsInfo.getLastLabel();
		DefaultComboBoxModel comboModel = new DefaultComboBoxModel();
		for (Iterator i = list.iterator(); i.hasNext();) {
			MR3Literal literal = (MR3Literal) i.next();
			LiteralLang lang = new LiteralLang(literal);
			comboModel.addElement(lang);
			if (gmanager.getDefaultLang().equals(literal.getLanguage())) {
				defaultLiteral = literal;
			}
		}
		if (defaultLiteral != null) {
			labelField.setText(defaultLiteral.getString());
			labelLangField.setText(defaultLiteral.getLanguage());
			comboModel.setSelectedItem(defaultLiteral.getLanguage());
		}

		return comboModel;
	}

	protected ComboBoxModel getCommentComboBoxModel(List list) {
		MR3Literal defaultLiteral = rdfsInfo.getLastComment();
		DefaultComboBoxModel comboModel = new DefaultComboBoxModel();
		for (Iterator i = list.iterator(); i.hasNext();) {
			MR3Literal literal = (MR3Literal) i.next();
			LiteralLang lang = new LiteralLang(literal);
			comboModel.addElement(lang);
			if (gmanager.getDefaultLang().equals(literal.getLanguage())) {
				defaultLiteral = literal;
			}
		}
		if (defaultLiteral != null) {
			commentArea.setText(defaultLiteral.getString());
			commentLangField.setText(defaultLiteral.getLanguage());
			comboModel.setSelectedItem(defaultLiteral.getLanguage());
		}

		return comboModel;
	}

	class EditLiteralAction extends AbstractAction {
		public void actionPerformed(ActionEvent e) {
			if (e.getSource() == editLabelButton) {
				if (labelLangBox.getItemCount() == 0 || labelLangBox.getSelectedItem() == null)
					return;
				DefaultComboBoxModel comboModel = (DefaultComboBoxModel) labelLangBox.getModel();
				LiteralLang addElement = new LiteralLang(new MR3Literal(labelField.getText(), labelLangField.getText(), null));
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
				LiteralLang addElement = new LiteralLang(new MR3Literal(commentArea.getText(), commentLangField.getText(), null));
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
				MR3Literal literal = new MR3Literal(labelField.getText(), labelLangField.getText(), null);
				LiteralLang addElement = new LiteralLang(literal);
				comboModel.addElement(addElement);
				comboModel.setSelectedItem(addElement);
				rdfsInfo.addLabel(literal);
			} else if (e.getSource() == addCommentButton) {
				DefaultComboBoxModel comboModel = (DefaultComboBoxModel) commentLangBox.getModel();
				MR3Literal literal = new MR3Literal(commentArea.getText(), commentLangField.getText(), null);
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
				rdfsInfo.setLastLabel(literal);
				// 最後にセットしたラベル
			} else if (e.getSource() == commentLangBox) {
				if (commentLangBox.getItemCount() == 0 || commentLangBox.getSelectedItem() == null)
					return;
				LiteralLang lang = (LiteralLang) commentLangBox.getSelectedItem();
				MR3Literal literal = lang.getLiteral();
				commentLangField.setText(literal.getLanguage());
				commentArea.setText(literal.getString());
				rdfsInfo.setLastComment(literal);
				// 最後にセットしたコメント
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
		String uri = nsLabel.getText() + idField.getText();
		if (gmanager.isEmptyURI(uri) || gmanager.isDuplicatedWithDialog(uri, cell, graph.getType())) {
			return;
		} else {
			rdfsInfoMap.removeURICellMap(rdfsInfo);
			// ここで，URIとセルのマッピングを削除する
			rdfsInfo.setURI(uri);
			//			rdfsInfo.setIsDefinedby(isDefinedBy.getText());
			rdfsInfoMap.putURICellMap(rdfsInfo, cell);
			gmanager.setCellValue(cell, rdfsInfo.getURIStr());
			rdfsInfo.setMetaClass(metaClassBox.getSelectedItem().toString());
		}
	}

	class ChangeInfoAction implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			if (cell != null) {
				changeInfo();
				gmanager.changeCellView();
				graph.clearSelection();
				graph.setSelectionCell(cell);
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

	private void setPrefix() {
		for (Iterator i = prefixNSInfoSet.iterator(); i.hasNext();) {
			PrefixNSInfo prefNSInfo = (PrefixNSInfo) i.next();
			if (prefNSInfo.getNameSpace().equals(rdfsInfo.getURI().getNameSpace())) {
				uriPrefixBox.setSelectedItem(prefNSInfo.getPrefix());
				nsLabel.setText(prefNSInfo.getNameSpace());
				break;
			}
		}
	}

	public void showRDFSInfo(DefaultGraphCell cell) {
		if (graph.isRDFSCell(cell)) {
			rdfsInfo = rdfsInfoMap.getCellInfo(cell);
			if (rdfsInfo != null) {
				setCell(cell);
				prefixNSInfoSet = gmanager.getPrefixNSInfoSet();
				PrefixNSUtil.setPrefixNSInfoSet(prefixNSInfoSet);
				uriPrefixBox.setModel(new DefaultComboBoxModel(PrefixNSUtil.getPrefixes().toArray()));
				setPrefix();
				setInstanceList();
				Set sourceCells = graph.getSourceCells(cell);
				setValue(sourceCells);
				rdfsInfo.setSupRDFS(sourceCells);
			}
		}
	}
}
