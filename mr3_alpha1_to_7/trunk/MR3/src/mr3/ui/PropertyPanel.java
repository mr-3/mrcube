package mr3.ui;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;
import javax.swing.event.*;

import mr3.data.*;
import mr3.jgraph.*;

/**
 *
 * @auther takeshi morita
 */
public class PropertyPanel extends RDFSPanel {

	private JList domain;
	private JList range;
	private JList supProperties;

	private JButton addDomainButton;
	private JButton addRangeButton;
	private JButton removeDomainButton;
	private JButton removeRangeButton;

	private JScrollPane supPropertiesScroll;
	private JScrollPane domainScroll;
	private JScrollPane rangeScroll;

	private static final int listHeight2 = 80;

	public PropertyPanel(GraphManager manager) {
		super(manager.getPropertyGraph(), manager);
		apply.addActionListener(new ChangeInfoAction());
		setBorder(BorderFactory.createTitledBorder("RDFS Property Attributes"));
		setBaseTab();
		setReferenceTab();
		setRegionTab();
		setLayout(new BorderLayout());
		add(metaTab, BorderLayout.CENTER);
		JPanel inline = new JPanel();
		inline.add(apply);
		inline.add(close);
		add(inline, BorderLayout.SOUTH);
	}

	public void setInstanceList() {
		Set propInstanceList = gmanager.getPropertyInstanceSet(cell);
		instanceList.setListData(propInstanceList.toArray());
	}

	private void setReferenceTab() {
		supProperties = new JList();
		supPropertiesScroll = new JScrollPane(supProperties);
		supPropertiesScroll.setPreferredSize(new Dimension(listWidth, listHeight2));
		supPropertiesScroll.setMinimumSize(new Dimension(listWidth, listHeight2));
		supPropertiesScroll.setBorder(BorderFactory.createTitledBorder("Super Property"));

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
		domain = new JList();
		domainScroll = new JScrollPane(domain);
		domainScroll.setPreferredSize(new Dimension(listWidth, listHeight2));
		domainScroll.setMinimumSize(new Dimension(listWidth, listHeight2));
		domainScroll.setBorder(BorderFactory.createTitledBorder("domain"));

		range = new JList();
		rangeScroll = new JScrollPane(range);
		rangeScroll.setPreferredSize(new Dimension(listWidth, listHeight2));
		rangeScroll.setMinimumSize(new Dimension(listWidth, listHeight2));
		rangeScroll.setBorder(BorderFactory.createTitledBorder("range"));

		AddDomainRange adr = new AddDomainRange(); //Ç‡Ç¡Ç∆ìKêÿÇ»ñºëOÇÇ¬ÇØÇÈ
		addDomainButton = new JButton("add");
		addDomainButton.addActionListener(adr);

		addRangeButton = new JButton("add");
		addRangeButton.addActionListener(adr);

		RemoveList rlist = new RemoveList();
		removeDomainButton = new JButton("remove");
		removeDomainButton.addActionListener(rlist);

		removeRangeButton = new JButton("remove");
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
				if (!domain.isSelectionEmpty()) {
					Object[] rlist = domain.getSelectedValues();
					for (int i = 0; i < rlist.length; i++) {
						info.removeDomain(rlist[i]);
					}
					rdfsInfoMap.putCellInfo(cell, info);
					domain.setListData(info.getDomain().toArray());
				}
			} else if (e.getSource() == removeRangeButton) {
				if (!range.isSelectionEmpty()) {
					Object[] rlist = range.getSelectedValues();
					for (int i = 0; i < rlist.length; i++) {
						info.removeRange(rlist[i]);
					}
					rdfsInfoMap.putCellInfo(cell, info);
					range.setListData(info.getRange().toArray());
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
			domain.setListData(info.getDomain().toArray());
		}
	}

	private void setRangeList(Set set) {
		if (set != null) {
			PropertyInfo info = (PropertyInfo) rdfsInfoMap.getCellInfo(cell);
			info.addAllRange(set);
			range.setListData(info.getRange().toArray());
		}
	}

	public void setValue(Set supCellSet) {
		PropertyInfo propInfo = (PropertyInfo) rdfsInfo;
		super.setValue();
		//		supCellSet.remove(gmanager.getPropertyCell(RDF.Property));
		supCellSet.remove(gmanager.getPropertyCell(MR3Resource.Property, true));
		supProperties.setListData(getTargetInfo(supCellSet));
		domain.setListData(propInfo.getDomain().toArray());
		range.setListData(propInfo.getRange().toArray());
	}
}
