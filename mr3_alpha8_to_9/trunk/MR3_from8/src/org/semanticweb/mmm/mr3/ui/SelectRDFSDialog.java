/*
 * Created on 2003/09/25
 *
 */
package org.semanticweb.mmm.mr3.ui;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;

import org.semanticweb.mmm.mr3.data.*;
import org.semanticweb.mmm.mr3.jgraph.*;

/**
 * @author takeshi morita
 */
public class SelectRDFSDialog extends JDialog implements ActionListener {

	private boolean isOk;
	private JButton confirmButton;
	private JButton cancelButton;
	private SelectRDFSPanel panel;

	public SelectRDFSDialog(String title, GraphManager gm) {
		super(gm.getRoot(), title, true);
		panel = new SelectRDFSPanel(gm);
		getContentPane().add(panel, BorderLayout.CENTER);
		JPanel inlinePanel = new JPanel();
		initButton();
		inlinePanel.add(confirmButton);
		inlinePanel.add(cancelButton);
		getContentPane().add(inlinePanel, BorderLayout.SOUTH);
		
		setLocation(100, 100);
		setSize(new Dimension(500, 500));
		setVisible(false);
	}

	private void initButton() {
		confirmButton = new JButton(MR3Constants.OK);
		confirmButton.addActionListener(this);
		cancelButton = new JButton(MR3Constants.CANCEL);
		cancelButton.addActionListener(this);
	}

	public void replaceGraph(RDFGraph graph) {
		panel.replaceGraph(graph);
	}

	public void setRegionSet(Set regionSet) {
		panel.setRegionSet(regionSet);
	}

	public Object getValue() {
		if (isOk) {
			isOk = false;
			return panel.getRegionSet();
		} else {
			return null;
		}
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
