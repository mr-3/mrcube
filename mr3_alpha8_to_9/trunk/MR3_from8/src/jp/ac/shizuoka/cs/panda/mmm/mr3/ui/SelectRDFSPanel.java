/*
 * Created on 2003/09/25
 *
 */
package jp.ac.shizuoka.cs.panda.mmm.mr3.ui;

import java.awt.*;
import java.util.*;

import javax.swing.*;

import jp.ac.shizuoka.cs.panda.mmm.mr3.jgraph.*;
import jp.ac.shizuoka.cs.panda.mmm.mr3.util.*;

import org.jgraph.event.*;

/**
 * @author takeshi morita
 */
public class SelectRDFSPanel extends SelectClassPanel {
	private Set orgRegionSet;
	private Set newRegionSet;
	private JList regionList;
	private JScrollPane regionListScroll;

	public SelectRDFSPanel(GraphManager gm) {
		super(gm);
		newRegionSet = new HashSet();
	}

	public void setRegionSet(Set set) {
		orgRegionSet = set;
		regionList.setListData(orgRegionSet.toArray());
	}

	protected void initEachDialogAttr() {
		regionList = new JList();
		regionListScroll = new JScrollPane(regionList);
		initComponent(regionListScroll, Translator.getString("SelectedList"), 450, 100);
	}

	protected void setEachDialogAttrLayout() {
		add(regionListScroll, BorderLayout.SOUTH);
	}

	public void valueChanged(GraphSelectionEvent e) {
		newRegionSet.removeAll(newRegionSet);
		Object[] cells = graph.getSelectionCells();
		for (int i = 0; i < cells.length; i++) {
			if (graph.isRDFSCell(cells[i])) {
				newRegionSet.add(cells[i]);
			}
		}
		if (orgRegionSet != null) {
			newRegionSet.addAll(orgRegionSet);
		}
		regionList.setListData(newRegionSet.toArray());
	}

	public Set getRegionSet() {
		return newRegionSet;
	}

	public void setEnabled(boolean flag) {
		super.setEnabled(flag);
		uriPrefixBox.setEnabled(flag);
		nsLabel.setEnabled(flag);
		findField.setEnabled(flag);
		findButton.setEnabled(flag);
		graph.setEnabled(flag);
		regionList.setEnabled(flag);
	}
}
