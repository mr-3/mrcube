/*
 * Created on 2003/09/25
 *
 */
package jp.ac.shizuoka.cs.panda.mmm.mr3.ui;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import jp.ac.shizuoka.cs.panda.mmm.mr3.jgraph.*;
import jp.ac.shizuoka.cs.panda.mmm.mr3.util.*;

/**
 * @author takeshi morita
 */
public class SelectResourceTypeDialog extends JDialog implements ActionListener {

	private boolean isOk;
	private JButton confirmButton;
	private JButton cancelButton;
	private SelectResourceTypePanel panel;

	SelectResourceTypeDialog(String title, GraphManager gm) {
		super(gm.getRoot(), title, true);
		panel = new SelectResourceTypePanel(gm);
		getContentPane().add(panel, BorderLayout.CENTER);
		JPanel inlinePanel = new JPanel();
		initButton();
		inlinePanel.add(confirmButton);
		inlinePanel.add(cancelButton);
		getContentPane().add(inlinePanel, BorderLayout.SOUTH);
		setResizable(false);
		setLocation(100, 100);
		setSize(new Dimension(500, 450));
		setVisible(false);
	}

	private void initButton() {
		confirmButton = new JButton(Translator.getString("OK"));
		confirmButton.addActionListener(this);
		cancelButton = new JButton(Translator.getString("Cancel"));
		cancelButton.addActionListener(this);
	}

	public Object getValue() {
		if (panel.getPrevCell() != null) {
			if (isOk) {
				isOk = false;
				ChangeCellAttributes.changeCellColor(panel.getGraph(), panel.getPrevCell(), Color.green);
				return panel.getURI();
			} else {
				ChangeCellAttributes.changeCellColor(panel.getGraph(), panel.getPrevCell(), Color.green);
				return null;
			}
		} else {
			return null;
		}
	}

	public void replaceGraph(RDFGraph graph) {
		panel.replaceGraph(graph);
	}
	
	public void setInitCell(Object typeCell) {
		panel.setInitCell(typeCell);
	}

	public void actionPerformed(ActionEvent e) {
		String type = (String) e.getActionCommand();
		if (type.equals(Translator.getString("OK"))) {
			isOk = true;
		} else {
			isOk = false;
		}
		setVisible(false);
	}
}
