/*
 * Created on 2003/09/25
 *
 */
package org.semanticweb.mmm.mr3.ui;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import org.semanticweb.mmm.mr3.data.*;
import org.semanticweb.mmm.mr3.jgraph.*;
import org.semanticweb.mmm.mr3.util.*;

/**
 * @author takeshi morita
 */
public class SelectResourceTypeDialog extends JDialog implements ActionListener {

	private boolean isOk;
	private JButton confirmButton;
	private JButton cancelButton;
	private SelectResourceTypePanel panel;

	SelectResourceTypeDialog(GraphManager gm) {
		super(gm.getRoot(), Translator.getString("SelectResourceTypeDialog.Title"), true);
		panel = new SelectResourceTypePanel(gm);
		getContentPane().add(panel, BorderLayout.CENTER);
		JPanel inlinePanel = new JPanel();
		initButton();
		inlinePanel.add(confirmButton);
		inlinePanel.add(cancelButton);
		getContentPane().add(inlinePanel, BorderLayout.SOUTH);
		setLocation(100, 100);
		setSize(new Dimension(500, 450));
		setVisible(false);
	}

	private void initButton() {
		confirmButton = new JButton(MR3Constants.OK);
		confirmButton.addActionListener(this);
		cancelButton = new JButton(MR3Constants.CANCEL);
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
		if (e.getSource() == confirmButton) {
			isOk = true;
		} else {
			isOk = false;
		}
		setVisible(false);
	}
}
