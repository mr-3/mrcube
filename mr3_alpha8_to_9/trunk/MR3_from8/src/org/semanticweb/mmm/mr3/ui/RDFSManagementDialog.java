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
import org.semanticweb.mmm.mr3.util.*;

/**
 * @author takeshi morita
 */
public class RDFSManagementDialog extends JDialog implements ActionListener {

	private CreateRDFSType type;
	private JButton confirmButton;
	private JButton cancelButton;
	private JRadioButton renameButton;
	private JRadioButton createButton;
	private SelectRDFSPanel panel;
	private RDFSInfoMap rdfsInfoMap = RDFSInfoMap.getInstance();
	private static final String TITLE = Translator.getString("RDFSManagementDialog.Title"); 

	public RDFSManagementDialog(GraphManager gm) {
		super(gm.getRoot(), TITLE, true);
		getContentPane().add(getChooseOnePanel(), BorderLayout.NORTH);
		panel = new SelectRDFSPanel(gm);
		getContentPane().add(panel, BorderLayout.CENTER);
		JPanel inlinePanel = new JPanel();
		initButton();
		inlinePanel.add(confirmButton);
		inlinePanel.add(cancelButton);
		getContentPane().add(inlinePanel, BorderLayout.SOUTH);

		setLocation(100, 100);
		setSize(new Dimension(500, 550));
		setVisible(false);
	}

	private static final int WIDTH = 200;
	private static final int HEIGHT = 50;
	
	private JComponent getChooseOnePanel() {
		JPanel chooseOnePanel = new JPanel();
		ButtonGroup group = new ButtonGroup();
		renameButton = new JRadioButton(Translator.getString("Rename"));
		renameButton.addActionListener(this);
		createButton = new JRadioButton(Translator.getString("Create"));
		createButton.addActionListener(this);
		createButton.setSelected(true);
		group.add(renameButton);
		group.add(createButton);
		chooseOnePanel.setLayout(new BoxLayout(chooseOnePanel, BoxLayout.X_AXIS));
		chooseOnePanel.add(renameButton);
		chooseOnePanel.add(createButton);
		chooseOnePanel.setBorder(BorderFactory.createTitledBorder(Translator.getString("ChooseOneSelect")));
		return chooseOnePanel;
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

	public Set getSupRDFSSet() {
		return panel.getRegionSet();
	}

	public CreateRDFSType getType() {
		return type;
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == confirmButton) {
			if (renameButton.isSelected()) {
				type = CreateRDFSType.RENAME;
			} else {
				type = CreateRDFSType.CREATE;
			}
			setVisible(false);
		} else if (e.getSource() == cancelButton) {
			type = null;
			setVisible(false);
		} else {
			if (renameButton.isSelected()) {
				panel.setEnabled(false);
			} else if (createButton.isSelected()) {
				panel.setEnabled(true);
			}
		}
	}

}
