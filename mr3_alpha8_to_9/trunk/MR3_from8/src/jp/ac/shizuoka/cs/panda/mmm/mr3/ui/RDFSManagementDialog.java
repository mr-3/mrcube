/*
 * Created on 2003/09/25
 *
 */
package jp.ac.shizuoka.cs.panda.mmm.mr3.ui;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;

import jp.ac.shizuoka.cs.panda.mmm.mr3.data.*;
import jp.ac.shizuoka.cs.panda.mmm.mr3.jgraph.*;

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

	public RDFSManagementDialog(String title, GraphManager gm) {
		super(gm.getRoot(), title, true);
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
		setResizable(false);
		setVisible(false);
	}

	private static final int WIDTH = 200;
	private static final int HEIGHT = 50;
	
	private JComponent getChooseOnePanel() {
		JPanel chooseOnePanel = new JPanel();
		ButtonGroup group = new ButtonGroup();
		renameButton = new JRadioButton("Rename");
		renameButton.addActionListener(this);
		createButton = new JRadioButton("Create");
		createButton.addActionListener(this);
		createButton.setSelected(true);
		group.add(renameButton);
		group.add(createButton);
		chooseOnePanel.setLayout(new BoxLayout(chooseOnePanel, BoxLayout.X_AXIS));
		chooseOnePanel.add(renameButton);
		chooseOnePanel.add(createButton);
//		chooseOnePanel.setPreferredSize(new Dimension(WIDTH, HEIGHT));
//		chooseOnePanel.setMinimumSize(new Dimension(WIDTH, HEIGHT));
		chooseOnePanel.setBorder(BorderFactory.createTitledBorder("Choose One Select"));
		return chooseOnePanel;
	}

	private void initButton() {
		confirmButton = new JButton("OK");
		confirmButton.addActionListener(this);
		cancelButton = new JButton("Cancel");
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
