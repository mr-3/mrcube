package mr3.ui;
import java.awt.*;
import java.util.*;

import javax.swing.*;

import mr3.jgraph.*;

public class ClassPanel extends RDFSPanel {

	private JList supClasses;
	private JScrollPane supClassesScroll;
	private static final int supClassListHeight = 80;

	public ClassPanel(RDFGraph g, GraphManager manager) {
		super(g, manager);
		apply.addActionListener(new ChangeInfoAction());
		setBorder(BorderFactory.createTitledBorder("RDFS Class Attributes"));
		setBaseTab();
		setReferenceTab();
		setLayout(new BorderLayout());
		add(metaTab, BorderLayout.CENTER);
		JPanel inline = new JPanel();
		inline.add(apply);
		inline.add(close);
		add(inline, BorderLayout.SOUTH);
	}

	public void setInstanceList() {
		Set classInstanceList = gmanager.getClassInstanceSet(cell);
		instanceList.setListData(classInstanceList.toArray());
	}

	private void setReferenceTab() {
		supClasses = new JList();
		supClassesScroll = new JScrollPane(supClasses);
		supClassesScroll.setPreferredSize(new Dimension(listWidth, supClassListHeight));
		supClassesScroll.setMinimumSize(new Dimension(listWidth, supClassListHeight));
		supClassesScroll.setBorder(BorderFactory.createTitledBorder("Super Class"));

		JPanel inline = new JPanel();
		GridBagLayout gridbag = new GridBagLayout();
		GridBagConstraints c = new GridBagConstraints();
		inline.setLayout(gridbag);
		c.anchor = GridBagConstraints.PAGE_START;
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.weighty = 10;

		gridbag.setConstraints(supClassesScroll, c);
		inline.add(supClassesScroll);

		gridbag.setConstraints(instanceListScroll, c);
		inline.add(instanceListScroll);

		metaTab.addTab("Reference", inline);
	}

	public void setValue(Set supCellSet) {
		super.setValue();
		supClasses.setListData(getTargetInfo(supCellSet));
	}
}
