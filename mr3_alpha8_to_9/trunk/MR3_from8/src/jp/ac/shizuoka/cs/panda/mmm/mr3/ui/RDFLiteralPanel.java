package jp.ac.shizuoka.cs.panda.mmm.mr3.ui;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;

import jp.ac.shizuoka.cs.panda.mmm.mr3.data.*;
import jp.ac.shizuoka.cs.panda.mmm.mr3.jgraph.*;
import jp.ac.shizuoka.cs.panda.mmm.mr3.util.*;

import org.jgraph.graph.*;

import com.hp.hpl.jena.datatypes.*;
import com.hp.hpl.jena.rdf.model.*;

public class RDFLiteralPanel extends JPanel implements ActionListener {

	private JTextField langField;
	private JCheckBox isTypedLiteralBox;
	private JComboBox typeBox;
	private JTextArea labelValueArea;
	private JTextField lineLength;
	private JButton fillTextButton;
	private JButton applyButton;
	private JButton closeButton;
	private GraphCell cell;
	private GraphManager gmanager;
	private TypeMapper typeMapper;
	
	private static final int LABEL_WIDTH = 350;
	private static final int LABEL_HEIGHT = 120;
	private RDFLiteralInfoMap litInfoMap = RDFLiteralInfoMap.getInstance();

	public RDFLiteralPanel(GraphManager manager) {
		gmanager = manager;
		typeMapper = TypeMapper.getInstance();
		setBorder(BorderFactory.createTitledBorder("RDF Literal Attributes"));

		langField = new JTextField(10);
		langField.setBorder(BorderFactory.createTitledBorder("Lang"));
		lineLength = new JTextField(6);
		lineLength.setBorder(BorderFactory.createTitledBorder("Length"));
		lineLength.addActionListener(this);
		fillTextButton = new JButton("Fill Text");
		fillTextButton.addActionListener(this);
		JPanel inlinePanel = new JPanel();
		inlinePanel.add(langField);
		;
		inlinePanel.add(lineLength);
		inlinePanel.add(fillTextButton);

		isTypedLiteralBox = new JCheckBox("is Type");
		isTypedLiteralBox.addActionListener(this);
		isTypedLiteralBox.setSelected(false);
		typeBox = new JComboBox();
		typeBox.setEnabled(false);
		initComponent(typeBox, "Type", 300, 50);
		JPanel typedLitPanel = new JPanel();
		typedLitPanel.add(isTypedLiteralBox);
		typedLitPanel.add(typeBox);

		labelValueArea = new JTextArea();
		JScrollPane valueScroll = new JScrollPane(labelValueArea);
		initComponent(valueScroll, "Literal", LABEL_WIDTH, LABEL_HEIGHT);
//		valueScroll.setPreferredSize(new Dimension(LABEL_WIDTH, LABEL_HEIGHT));
//		valueScroll.setMinimumSize(new Dimension(LABEL_WIDTH, LABEL_HEIGHT));

		applyButton = new JButton("Apply");
		applyButton.addActionListener(this);
		closeButton = new JButton("Close");
		closeButton.addActionListener(this);
		JPanel buttonGroup = new JPanel();
		buttonGroup.add(applyButton);
		buttonGroup.add(closeButton);

		GridBagLayout gridbag = new GridBagLayout();
		GridBagConstraints c = new GridBagConstraints();
		setLayout(gridbag);

		c.gridwidth = GridBagConstraints.REMAINDER;
		c.weighty = 1.0;
		c.anchor = GridBagConstraints.WEST;
		gridbag.setConstraints(inlinePanel, c);
		add(inlinePanel);
		c.anchor = GridBagConstraints.CENTER;
		gridbag.setConstraints(typedLitPanel, c);
		add(typedLitPanel);
		gridbag.setConstraints(valueScroll, c);
		add(valueScroll);
		gridbag.setConstraints(buttonGroup, c);
		add(buttonGroup);
	}

	private void initComponent(JComponent component, String title, int width, int height) {
		component.setPreferredSize(new Dimension(width, height));
		component.setMinimumSize(new Dimension(width, height));
		component.setBorder(BorderFactory.createTitledBorder(title));
	}

	private void clearTextField() {
		langField.setText("");
		labelValueArea.setText("");
	}

	public void showLiteralInfo(GraphCell c) {
		cell = c;
		clearTextField();
		setValue(cell);
	}

	public void setValue(Object cell) {
		try {
			Literal literal = litInfoMap.getCellInfo(cell);
			if (literal != null) {				
				DefaultComboBoxModel model = new DefaultComboBoxModel();
				for (Iterator i = typeMapper.listTypes(); i.hasNext();) {
					model.addElement(((RDFDatatype) i.next()).getURI());
				}
				typeBox.setModel(model);
				if (literal.getDatatype() != null) {
					setTypeLiteralEnable(true);
					typeBox.setSelectedItem(literal.getDatatypeURI());
				} else {
					setTypeLiteralEnable(false);
				}
				langField.setText(literal.getLanguage());
				labelValueArea.setText(RDFLiteralUtil.fixString(literal.getString()));
			}
		} catch (RDFException e) {
			e.printStackTrace();
		}
	}

	public void apply() {
		if (cell != null) {
			String str = labelValueArea.getText();
			String dataType = null;
			if (isTypedLiteralBox.isSelected()) {
				dataType = (String)typeBox.getSelectedItem();
			}
			litInfoMap.putCellInfo(cell, RDFLiteralUtil.createLiteral(str, langField.getText(), typeMapper.getTypeByName(dataType)));
			str = "<html>" + str + "</html>";
			str = str.replaceAll("(\n|\r)+", "<br>");
			gmanager.setCellValue(cell, str);
			gmanager.getRDFGraph().clearSelection();
			gmanager.getRDFGraph().setSelectionCell(cell);
			gmanager.changeCellView();
			cell = null;
		}
	}

	public void fillText() {
		String oldStr = labelValueArea.getText();
		String length = lineLength.getText();
		int len = 20;
		try {
			len = Integer.parseInt(length);
		} catch (NumberFormatException numEx) {
			len = 20;
		}
		String newStr = RDFLiteralUtil.insertLineFeed(oldStr, len);
		labelValueArea.setText(newStr);
	}

	private void setTypeLiteralEnable(boolean t) {
		isTypedLiteralBox.setSelected(t);
		typeBox.setEnabled(t);
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == fillTextButton || e.getSource() == lineLength) {
			fillText();
		} else if (e.getSource() == isTypedLiteralBox) {
			typeBox.setEnabled(isTypedLiteralBox.isSelected());
		} else if (e.getSource() == applyButton) {
			apply();
		} else if (e.getSource() == closeButton) {
			gmanager.setVisibleAttrDialog(false);
		}
	}
}
