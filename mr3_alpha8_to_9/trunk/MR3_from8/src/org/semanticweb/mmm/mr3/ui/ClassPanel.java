/*
 * @(#) ClassPanel.java
 *
 * Copyright (C) 2003 The MMM Project
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 */

package org.semanticweb.mmm.mr3.ui;

import java.awt.*;
import java.util.*;

import javax.swing.*;

import org.semanticweb.mmm.mr3.jgraph.*;
import org.semanticweb.mmm.mr3.util.*;

/*
 * 
 * @author takeshi morita
 *
 */
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
		Utilities.initComponent(supClassesScroll, Translator.getString("SuperClasses"), LIST_WIDTH, LIST_HEIGHT);

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
