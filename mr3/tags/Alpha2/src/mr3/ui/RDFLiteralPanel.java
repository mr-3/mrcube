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

public class RDFLiteralPanel extends JPanel implements ActionListener {

	private JTextField langField;
	private JTextArea value;
	private JTextField lineLength;
	private JButton fillText;
	private JButton apply;
	private JButton close;
	private GraphCell cell;
	private GraphManager gmanager;
	private AttributeDialog propWindow;

	private RDFLiteralInfoMap litInfoMap = RDFLiteralInfoMap.getInstance();

	public RDFLiteralPanel(GraphManager manager, AttributeDialog pw) {
		gmanager = manager;
		propWindow = pw;
		setBorder(BorderFactory.createTitledBorder("Literal"));

		langField = new JTextField(10);
		langField.setBorder(BorderFactory.createTitledBorder("Lang"));

		value = new JTextArea(8, 25);
		JScrollPane valueScroll = new JScrollPane(value);
		valueScroll.setPreferredSize(new Dimension(300, 200));
		valueScroll.setMinimumSize(new Dimension(300, 200));

		lineLength = new JTextField(6);
		lineLength.setBorder(BorderFactory.createTitledBorder("Length"));
		lineLength.addActionListener(this);
		fillText = new JButton("FillText");
		fillText.addActionListener(this);
		JPanel fillTextGroup = new JPanel();
		fillTextGroup.add(lineLength);
		fillTextGroup.add(fillText);

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

		c.gridwidth = GridBagConstraints.REMAINDER;
		c.weighty = 1.0;
		c.anchor = GridBagConstraints.WEST;
		gridbag.setConstraints(langField, c);
		add(langField);
		c.anchor = GridBagConstraints.CENTER;
		gridbag.setConstraints(valueScroll, c);
		add(valueScroll);
		c.anchor = GridBagConstraints.WEST;
		gridbag.setConstraints(fillTextGroup, c);
		add(fillTextGroup);
		c.anchor = GridBagConstraints.CENTER;
		gridbag.setConstraints(buttonGroup, c);
		add(buttonGroup);
	}

	public void dspLiteralInfo(GraphCell c) {
		cell = c;
		setValue(cell);
	}

	public void setValue(Object cell) {
		try {
			Literal literal = litInfoMap.getCellInfo(cell);
			if (literal != null) {
				langField.setText(literal.getLanguage());
				value.setText(RDFLiteralUtil.fixString(literal.getString()));
			}
		} catch (RDFException e) {
			e.printStackTrace();
		}
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == fillText || e.getSource() == lineLength) {
			String oldStr = value.getText();
			String length = lineLength.getText();
			int len = 20;
			try {
				len = Integer.parseInt(length);
			} catch (NumberFormatException numEx) {
				len = 20;
			}
			String newStr = RDFLiteralUtil.insertLineFeed(oldStr, len);
			value.setText(newStr);
		} else if (e.getSource() == apply) {
			if (cell != null) {
				Map map = cell.getAttributes();
				String str = value.getText();
				litInfoMap.putCellInfo(cell, new LiteralImpl(str, langField.getText()));
				str = "<html>" + str + "</html>";
				str = str.replaceAll("(\n|\r)+", "<br>");
				GraphConstants.setValue(map, str);
				cell.setAttributes(map);
				gmanager.changeCellView();
				// literalInfo‚ð•Û‘¶				
				cell = null;
			}
		} else if (e.getSource() == close) {
			propWindow.setVisible(false);
		}
	}
}
