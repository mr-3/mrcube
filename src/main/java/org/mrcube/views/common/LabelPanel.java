/*
 * Project Name: MR^3 (Meta-Model Management based on RDFs Revision Reflection)
 * Project Website: http://mrcube.org/
 * 
 * Copyright (C) 2003-2022 Takeshi Morita. All rights reserved.
 * 
 * This file is part of MR^3.
 * 
 * MR^3 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * MR^3 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with MR^3.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */

package org.mrcube.views.common;

import org.mrcube.models.ClassModel;
import org.mrcube.models.MR3Constants;
import org.mrcube.models.MR3Constants.GraphType;
import org.mrcube.models.MR3Constants.HistoryType;
import org.mrcube.models.MR3Literal;
import org.mrcube.models.ResourceModel;
import org.mrcube.utils.Utilities;
import org.mrcube.views.HistoryManager;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Takeshi Morita
 */
public class LabelPanel extends JPanel implements ActionListener {

	private ResourceModel resInfo;

	private final JTable labelTable;
	private final LabelTableModel labelTableModel;

	private final JTextField langField;
	private final JTextField labelField;

	private final JButton addLabelButton;
	private final JButton removeLabelButton;
	private final JButton clearFieldButton;

	private GraphType graphType;

	public LabelPanel() {
		graphType = GraphType.Instance;
		labelTableModel = new LabelTableModel(
				new Object[] { MR3Constants.LANG, MR3Constants.LABEL }, 0);
		labelTable = new JTable(labelTableModel);
		labelTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		setTableColumn(labelTable.getColumnModel());

		langField = new JTextField(5);
		JComponent langFieldP = Utilities.createTitledPanel(langField, MR3Constants.LANG);
		labelField = new JTextField(20);
		JComponent labelFieldP = Utilities.createTitledPanel(labelField, MR3Constants.LABEL);
		JPanel textFieldPanel = new JPanel();
		textFieldPanel.setLayout(new BorderLayout());
		textFieldPanel.add(langFieldP, BorderLayout.WEST);
		textFieldPanel.add(labelFieldP, BorderLayout.CENTER);

		addLabelButton = new JButton(MR3Constants.ADD);
		addLabelButton.addActionListener(this);
		removeLabelButton = new JButton(MR3Constants.REMOVE);
		removeLabelButton.addActionListener(this);
		clearFieldButton = new JButton(MR3Constants.CLEAR);
		clearFieldButton.addActionListener(this);

		JPanel southPanel = new JPanel();
		southPanel.setLayout(new BoxLayout(southPanel, BoxLayout.Y_AXIS));
		southPanel.add(textFieldPanel);
		southPanel.add(getButtonPanel());
		JPanel labelPanel = new JPanel();
		labelPanel.setLayout(new BorderLayout());
		labelPanel.add(new JScrollPane(labelTable), BorderLayout.CENTER);
		labelPanel.add(southPanel, BorderLayout.SOUTH);

		setLayout(new BorderLayout());
		add(labelPanel, BorderLayout.CENTER);
	}

	public void setGraphType(GraphType type) {
		graphType = type;
	}

	private JComponent getButtonPanel() {
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new GridLayout(1, 3, 5, 5));
		buttonPanel.add(addLabelButton);
		buttonPanel.add(removeLabelButton);
		buttonPanel.add(clearFieldButton);
		return Utilities.createEastPanel(buttonPanel);
	}

	public void clearField() {
		langField.setText("");
		labelField.setText("");
	}

	private void setTableColumn(TableColumnModel tcModel) {
		tcModel.getColumn(0).setPreferredWidth(15);
		tcModel.getColumn(1).setPreferredWidth(200);
	}

	private void addLabel(String lang, String label) {
		if (isValidLabel(lang, label)) {
			labelTableModel.insertRow(labelTableModel.getRowCount(), new Object[] { lang, label });
			List<MR3Literal> beforeMR3LabelList = new ArrayList<>(resInfo.getLabelList());
			setLabelList();
			List<MR3Literal> afterMR3LabelList = resInfo.getLabelList();
			if (graphType == GraphType.Instance) {
				HistoryManager.saveHistory(HistoryType.ADD_RESOURCE_LABEL, beforeMR3LabelList,
						afterMR3LabelList);
			} else if (graphType == GraphType.Class) {
				HistoryManager.saveHistory(HistoryType.ADD_CLASS_LABEL, beforeMR3LabelList,
						afterMR3LabelList);
			} else if (graphType == GraphType.Property) {
				HistoryManager.saveHistory(HistoryType.ADD_ONT_PROPERTY_LABEL, beforeMR3LabelList,
						afterMR3LabelList);
			}
		}
	}

	private void removeLabel() {
		if (labelTable.getSelectedRowCount() == 1) {
			labelTableModel.removeRow(labelTable.getSelectedRow());
			List<MR3Literal> beforeMR3LabelList = new ArrayList<>(resInfo.getLabelList());
			setLabelList();
			List<MR3Literal> afterMR3LabelList = resInfo.getLabelList();
			if (graphType == GraphType.Instance) {
				HistoryManager.saveHistory(HistoryType.DELETE_RESOURCE_LABEL, beforeMR3LabelList,
						afterMR3LabelList);
			} else if (graphType == GraphType.Class) {
				HistoryManager.saveHistory(HistoryType.DELETE_CLASS_LABEL, beforeMR3LabelList,
						afterMR3LabelList);
			} else if (graphType == GraphType.Property) {
				HistoryManager.saveHistory(HistoryType.DELETE_ONT_PROPERTY_LABEL,
						beforeMR3LabelList, afterMR3LabelList);
			}
		}
	}

	private boolean isValidLabel(String lang, String label) {
		return !label.equals("");
	}

	public void setResourceInfo(ResourceModel info) {
		resInfo = info;
		while (labelTableModel.getRowCount() != 0) {
			labelTableModel.removeRow(0);
		}
		List<MR3Literal> labelList = info.getLabelList();
		for (int i = 0; i < labelList.size(); i++) {
			MR3Literal literal = labelList.get(i);
			labelTableModel.insertRow(i,
					new Object[] { literal.getLanguage(), literal.getString() });
		}
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == clearFieldButton) {
			clearField();
		} else if (e.getSource() == addLabelButton) {
			addLabel(langField.getText(), labelField.getText());
		} else if (e.getSource() == removeLabelButton) {
			removeLabel();
		}
	}

	private void setLabelList() {
		List<MR3Literal> labelList = new ArrayList<>();
		for (int i = 0; i < labelTable.getRowCount(); i++) {
			String lang = labelTable.getValueAt(i, 0).toString();
			String label = labelTable.getValueAt(i, 1).toString();
			labelList.add(new MR3Literal(label, lang, null));
		}
		if (resInfo != null) {
			resInfo.setLabelList(labelList);
		}
	}

	public String toString() {
		return MR3Constants.LABEL;
	}

	class LabelTableModel extends DefaultTableModel implements Serializable {

		LabelTableModel(Object[] columnNames, int rowCount) {
			super(columnNames, rowCount);
		}

		public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
			if (aValue instanceof String) {
				if (columnIndex == 0 || (columnIndex == 1 && !aValue.equals(""))) {
					super.setValueAt(aValue, rowIndex, columnIndex);
					List<MR3Literal> beforeMR3LabelList = new ArrayList<>(
							resInfo.getLabelList());
					setLabelList();
					List<MR3Literal> afterMR3LabelList = resInfo.getLabelList();
					if (graphType == GraphType.Instance) {
						HistoryManager.saveHistory(HistoryType.EDIT_RESOURCE_LABEL,
								beforeMR3LabelList, afterMR3LabelList);
					} else if (graphType == GraphType.Class) {
						HistoryManager.saveHistory(HistoryType.EDIT_CLASS_LABEL,
								beforeMR3LabelList, afterMR3LabelList);
					} else if (graphType == GraphType.Property) {
						HistoryManager.saveHistory(HistoryType.EDIT_ONT_PROPERTY_LABEL,
								beforeMR3LabelList, afterMR3LabelList);
					}
				}
			}
		}
	}

	public static void main(String[] args) {
		JFrame frame = new JFrame();
		LabelPanel labelPanel = new LabelPanel();
		ResourceModel info = new ClassModel("http://mrcube.org#test");
		info.addLabel(new MR3Literal("てすと", "ja", null));
		info.addLabel(new MR3Literal("test", "en", null));
		labelPanel.setResourceInfo(info);
		frame.getContentPane().add(labelPanel);
		frame.setSize(new Dimension(350, 200));
		frame.setVisible(true);
	}
}
