package jp.ac.shizuoka.cs.panda.mmm.mr3.ui;
import java.awt.*;
import java.util.*;

import javax.swing.*;

import jp.ac.shizuoka.cs.panda.mmm.mr3.jgraph.*;
import jp.ac.shizuoka.cs.panda.mmm.mr3.util.*;

public class ClassPanel extends RDFSPanel {

	private JList supClasses;
	private JScrollPane supClassesScroll;

	public ClassPanel(RDFGraph g, GraphManager manager) {
		super(g, manager);
		setBorder(BorderFactory.createTitledBorder(Translator.getString("AttributeDialog.RDFSClassAttribute.Text")));
		setBaseTab();
		setCommentTab();
		setReferenceTab();
		setLayout(new BorderLayout());
		add(metaTab, BorderLayout.CENTER);
		JPanel inline = new JPanel();
		inline.add(apply);
		inline.add(close);
		add(inline, BorderLayout.SOUTH);
	}

	public void setInstanceList() {
		instanceList.setListData(gmanager.getClassInstanceSet(cell).toArray());
	}

	private void setReferenceTab() {
		supClasses = new JList();
		supClassesScroll = new JScrollPane(supClasses);
		initComponent(supClassesScroll, Translator.getString("SuperClasses"), LIST_WIDTH, LIST_HEIGHT);

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

		metaTab.addTab(Translator.getString("Reference"), inline);
	}

	public void setValue(Set supCellSet) {
		super.setValue();
		setMetaClassBox(gmanager.getClassClassList());
		supClasses.setListData(getTargetInfo(supCellSet));
	}
}
