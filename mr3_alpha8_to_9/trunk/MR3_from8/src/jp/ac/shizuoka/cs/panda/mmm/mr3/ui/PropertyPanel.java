package jp.ac.shizuoka.cs.panda.mmm.mr3.ui;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;
import javax.swing.event.*;

import jp.ac.shizuoka.cs.panda.mmm.mr3.data.*;
import jp.ac.shizuoka.cs.panda.mmm.mr3.jgraph.*;

/**
 *
 * @author takeshi morita
 */
public class PropertyPanel extends RDFSPanel {

	private JList domainList;
	private JList rangeList;
	private JList supProperties;

	private JButton addDomainButton;
	private JButton addRangeButton;
	private JButton removeDomainButton;
	private JButton removeRangeButton;

	private JScrollPane supPropertiesScroll;
	private JScrollPane domainScroll;
	private JScrollPane rangeScroll;

	public PropertyPanel(GraphManager manager) {
		super(manager.getPropertyGraph(), manager);
		setBorder(BorderFactory.createTitledBorder("RDFS Property Attributes"));
		setBaseTab();
		setCommentTab();
		setRegionTab();
		setReferenceTab();
		setLayout(new BorderLayout());
		add(metaTab, BorderLayout.CENTER);
		JPanel inline = new JPanel();
		inline.add(apply);
		inline.add(close);
		add(inline, BorderLayout.SOUTH);
	}

	public void setInstanceList() {
		instanceList.setListData(gmanager.getPropertyInstanceSet(cell).toArray());
	}

	private void setReferenceTab() {
		supProperties = new JList();
		supPropertiesScroll = new JScrollPane(supProperties);
		initComponent(supPropertiesScroll, "Super Property", LIST_WIDTH, LIST_HEIGHT);

		JPanel inline = new JPanel();
		GridBagLayout gridbag = new GridBagLayout();
		GridBagConstraints c = new GridBagConstraints();
		inline.setLayout(gridbag);
		c.anchor = GridBagConstraints.PAGE_START;
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.weighty = 10;

		gridbag.setConstraints(supPropertiesScroll, c);
		inline.add(supPropertiesScroll);

		gridbag.setConstraints(instanceListScroll, c);
		inline.add(instanceListScroll);

		metaTab.addTab("Reference", inline);
	}

	class InstanceAction implements ListSelectionListener {
		public void valueChanged(ListSelectionEvent e) {

		}
	}

	private void setRegionTab() {
		domainList = new JList();
		domainScroll = new JScrollPane(domainList);
		initComponent(domainScroll, "Domain", LIST_WIDTH, LIST_HEIGHT);

		rangeList = new JList();
		rangeScroll = new JScrollPane(rangeList);
		initComponent(rangeScroll, "Range", LIST_WIDTH, LIST_HEIGHT);

		AddDomainRange adr = new AddDomainRange(); //Ç‡Ç¡Ç∆ìKêÿÇ»ñºëOÇÇ¬ÇØÇÈ
		addDomainButton = new JButton("Add");
		addDomainButton.addActionListener(adr);

		addRangeButton = new JButton("Add");
		addRangeButton.addActionListener(adr);

		RemoveList rlist = new RemoveList();
		removeDomainButton = new JButton("Remove");
		removeDomainButton.addActionListener(rlist);

		removeRangeButton = new JButton("Remove");
		removeRangeButton.addActionListener(rlist);

		JPanel inline = new JPanel();
		GridBagLayout gridbag = new GridBagLayout();
		GridBagConstraints c = new GridBagConstraints();
		inline.setLayout(gridbag);
		c.anchor = GridBagConstraints.PAGE_START;
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.weighty = 10;

		gridbag.setConstraints(domainScroll, c);
		inline.add(domainScroll);
		c.gridwidth = GridBagConstraints.RELATIVE;
		c.weightx = 1.0;
		gridbag.setConstraints(addDomainButton, c);
		inline.add(addDomainButton);
		c.gridwidth = GridBagConstraints.REMAINDER;
		gridbag.setConstraints(removeDomainButton, c);
		inline.add(removeDomainButton);

		gridbag.setConstraints(rangeScroll, c);
		inline.add(rangeScroll);
		c.gridwidth = GridBagConstraints.RELATIVE;
		c.weightx = 1.0;
		gridbag.setConstraints(addRangeButton, c);
		inline.add(addRangeButton);
		c.gridwidth = GridBagConstraints.REMAINDER;
		gridbag.setConstraints(removeRangeButton, c);
		inline.add(removeRangeButton);

		metaTab.addTab("Region", inline);
	}

	class RemoveList implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			PropertyInfo info = (PropertyInfo) rdfsInfoMap.getCellInfo(cell);
			if (e.getSource() == removeDomainButton) {
				if (!domainList.isSelectionEmpty()) {
					Object[] rlist = domainList.getSelectedValues();
					for (int i = 0; i < rlist.length; i++) {
						info.removeDomain(rlist[i]);
					}
					rdfsInfoMap.putCellInfo(cell, info);
					domainList.setListData(info.getDomain().toArray());
				}
			} else if (e.getSource() == removeRangeButton) {
				if (!rangeList.isSelectionEmpty()) {
					Object[] rlist = rangeList.getSelectedValues();
					for (int i = 0; i < rlist.length; i++) {
						info.removeRange(rlist[i]);
					}
					rdfsInfoMap.putCellInfo(cell, info);
					rangeList.setListData(info.getRange().toArray());
				}
			}
		}
	}

	class AddDomainRange implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			PropertyInfo info = (PropertyInfo) rdfsInfoMap.getCellInfo(cell);
			if (info == null) {
				return;
			}

			SelectRDFSDialog regionDialog = new SelectRDFSDialog("Select Region", gmanager);
			if (e.getSource() == addDomainButton) {
				initRegionDialog(regionDialog);
				regionDialog.setRegionSet(info.getDomain());
				setDomainList((Set) regionDialog.getValue());
			} else if (e.getSource() == addRangeButton) {
				initRegionDialog(regionDialog);
				regionDialog.setRegionSet(info.getRange());
				setRangeList((Set) regionDialog.getValue());
			}
		}
		private void initRegionDialog(SelectRDFSDialog regionDialog) {
			regionDialog.replaceGraph(gmanager.getClassGraph());
			regionDialog.setVisible(true);
		}
	}

	private void setDomainList(Set set) {
		if (set != null) {
			PropertyInfo info = (PropertyInfo) rdfsInfoMap.getCellInfo(cell);
			info.addAllDomain(set);
			domainList.setListData(info.getDomain().toArray());
		}
	}

	private void setRangeList(Set set) {
		if (set != null) {
			PropertyInfo info = (PropertyInfo) rdfsInfoMap.getCellInfo(cell);
			info.addAllRange(set);
			rangeList.setListData(info.getRange().toArray());
		}
	}

	public void setValue(Set supCellSet) {
		PropertyInfo propInfo = (PropertyInfo) rdfsInfo;
		super.setValue();
		setMetaClassBox(gmanager.getPropertyClassList());
		if (rdfsInfoMap.isPropertyCell(MR3Resource.Property)) {
			supCellSet.remove(gmanager.getPropertyCell(MR3Resource.Property, false));
		}
		supProperties.setListData(getTargetInfo(supCellSet));
		domainList.setListData(propInfo.getDomain().toArray());
		rangeList.setListData(propInfo.getRange().toArray());
	}
}
