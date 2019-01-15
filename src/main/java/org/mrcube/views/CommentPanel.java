/*
 * Project Name: MR^3 (Meta-Model Management based on RDFs Revision Reflection)
 * Project Website: http://mrcube.org/
 * 
 * Copyright (C) 2003-2018 Yamaguchi Laboratory, Keio University. All rights reserved.
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

package org.mrcube.views;

import org.mrcube.models.ClassModel;
import org.mrcube.models.MR3Constants;
import org.mrcube.models.MR3Constants.GraphType;
import org.mrcube.models.MR3Constants.HistoryType;
import org.mrcube.models.MR3Literal;
import org.mrcube.models.ResourceModel;
import org.mrcube.utils.Utilities;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Takeshi Morita
 */
public class CommentPanel extends JPanel implements ActionListener {

	private final Frame rootFrame;
	private ResourceModel resInfo;

	private WeakReference<EditCommentDialog> editCommentDialogRef;

	private final JTable commentTable;
	private final CommentTableModel commentTableModel;

	private final JButton editCommentButton;
	private final JButton addCommentButton;
	private final JButton removeCommentButton;

	private GraphType graphType;

	public CommentPanel(Frame frame) {
		rootFrame = frame;
		graphType = GraphType.RDF;
		editCommentDialogRef = new WeakReference<>(null);

		commentTableModel = new CommentTableModel(new Object[] { MR3Constants.LANG,
				MR3Constants.COMMENT }, 0);
		commentTable = new JTable(commentTableModel);
		commentTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		setTableColumn(commentTable.getColumnModel());

		editCommentButton = new JButton(MR3Constants.EDIT);
		editCommentButton.addActionListener(this);
		addCommentButton = new JButton(MR3Constants.ADD);
		addCommentButton.addActionListener(this);
		removeCommentButton = new JButton(MR3Constants.REMOVE);
		removeCommentButton.addActionListener(this);

		JPanel commentPanel = new JPanel();
		commentPanel.setLayout(new BorderLayout());
		commentPanel.add(new JScrollPane(commentTable), BorderLayout.CENTER);
		commentPanel.add(getButtonPanel(), BorderLayout.SOUTH);

		setLayout(new BorderLayout());
		add(commentPanel, BorderLayout.CENTER);
	}

	public void setGraphType(GraphType type) {
		graphType = type;
	}

	private EditCommentDialog getEditCommentDialog() {
		EditCommentDialog result = editCommentDialogRef.get();
		if (result == null) {
			result = new EditCommentDialog(rootFrame);
			editCommentDialogRef = new WeakReference<>(result);
		}
		return result;
	}

	private JComponent getButtonPanel() {
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new GridLayout(1, 3, 5, 5));
		buttonPanel.add(editCommentButton);
		buttonPanel.add(addCommentButton);
		buttonPanel.add(removeCommentButton);
		return Utilities.createEastPanel(buttonPanel);
	}

	private void setTableColumn(TableColumnModel tcModel) {
		tcModel.getColumn(0).setPreferredWidth(15);
		tcModel.getColumn(1).setPreferredWidth(200);
	}

	private String getSelectedLang() {
		return commentTable.getValueAt(commentTable.getSelectedRow(), 0).toString();
	}

	private String getSelectedComment() {
		return commentTable.getValueAt(commentTable.getSelectedRow(), 1).toString();
	}

	private void setComment(MR3Literal literal) {
		if (!literal.getString().equals("")) {
			commentTableModel.setValueAt(literal.getLanguage(), commentTable.getSelectedRow(), 0);
			commentTableModel.setValueAt(literal.getString(), commentTable.getSelectedRow(), 1);
		}
	}

	private void editComment() {
		if (commentTable.getSelectedRowCount() == 1) {
			EditCommentDialog editCommentDialog = getEditCommentDialog();
			editCommentDialog.setComment(getSelectedLang(), getSelectedComment());
			editCommentDialog.setVisible(true);
			setComment(editCommentDialog.getComment());
			setCommentList();
		}
	}

	private void addComment() {
		EditCommentDialog editCommentDialog = getEditCommentDialog();
		editCommentDialog.setComment("", "");
		editCommentDialog.setVisible(true);
		MR3Literal literal = editCommentDialog.getComment();
		if (!literal.getString().equals("")) {
			commentTableModel.insertRow(commentTableModel.getRowCount(),
					new Object[] { literal.getLanguage(), literal.getString() });
			List<MR3Literal> beforeMR3CommentList = new ArrayList<>(
					resInfo.getCommentList());
			setCommentList();
			List<MR3Literal> afterMR3CommentList = resInfo.getCommentList();
			if (graphType == GraphType.RDF) {
				HistoryManager.saveHistory(HistoryType.ADD_RESOURCE_COMMENT, beforeMR3CommentList,
						afterMR3CommentList);
			} else if (graphType == GraphType.CLASS) {
				HistoryManager.saveHistory(HistoryType.ADD_CLASS_COMMENT, beforeMR3CommentList,
						afterMR3CommentList);
			} else if (graphType == GraphType.PROPERTY) {
				HistoryManager.saveHistory(HistoryType.ADD_ONT_PROPERTY_COMMENT,
						beforeMR3CommentList, afterMR3CommentList);
			}
		}
	}

	private void deleteComment() {
		if (commentTable.getSelectedRowCount() == 1) {
			commentTableModel.removeRow(commentTable.getSelectedRow());
			List<MR3Literal> beforeMR3CommentList = new ArrayList<>(
					resInfo.getCommentList());
			setCommentList();
			List<MR3Literal> afterMR3CommentList = resInfo.getCommentList();
			if (graphType == GraphType.RDF) {
				HistoryManager.saveHistory(HistoryType.DELETE_RESOURCE_COMMENT,
						beforeMR3CommentList, afterMR3CommentList);
			} else if (graphType == GraphType.CLASS) {
				HistoryManager.saveHistory(HistoryType.DELETE_CLASS_COMMENT, beforeMR3CommentList,
						afterMR3CommentList);
			} else if (graphType == GraphType.PROPERTY) {
				HistoryManager.saveHistory(HistoryType.DELETE_ONT_PROPERTY_COMMENT,
						beforeMR3CommentList, afterMR3CommentList);
			}
		}
	}

	public void setResourceInfo(ResourceModel info) {
		resInfo = info;
		while (commentTableModel.getRowCount() != 0) {
			commentTableModel.removeRow(0);
		}
		List<MR3Literal> commentList = info.getCommentList();
		for (int i = 0; i < commentList.size(); i++) {
			MR3Literal literal = commentList.get(i);
			commentTableModel.insertRow(i,
					new Object[] { literal.getLanguage(), literal.getString() });
		}
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == editCommentButton) {
			editComment();
		} else if (e.getSource() == addCommentButton) {
			addComment();
		} else if (e.getSource() == removeCommentButton) {
			deleteComment();
		}
	}

	private void setCommentList() {
		List<MR3Literal> commentList = new ArrayList<>();
		for (int i = 0; i < commentTable.getRowCount(); i++) {
			String lang = commentTable.getValueAt(i, 0).toString();
			String label = commentTable.getValueAt(i, 1).toString();
			commentList.add(new MR3Literal(label, lang, null));
		}
		if (resInfo != null) {
			resInfo.setCommentList(commentList);
		}
	}

	public String toString() {
		return MR3Constants.COMMENT;
	}

	class CommentTableModel extends DefaultTableModel implements Serializable {

		CommentTableModel(Object[] columnNames, int rowCount) {
			super(columnNames, rowCount);
		}

		public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
			if (aValue instanceof String) {
				if (columnIndex == 0 || (columnIndex == 1 && !aValue.equals(""))) {
					super.setValueAt(aValue, rowIndex, columnIndex);
					List<MR3Literal> beforeMR3CommentList = new ArrayList<>(
							resInfo.getCommentList());
					setCommentList();
					List<MR3Literal> afterMR3CommentList = resInfo.getCommentList();
					if (graphType == GraphType.RDF) {
						HistoryManager.saveHistory(HistoryType.EDIT_RESOURCE_COMMENT,
								beforeMR3CommentList, afterMR3CommentList);
					} else if (graphType == GraphType.CLASS) {
						HistoryManager.saveHistory(HistoryType.EDIT_CLASS_COMMENT,
								beforeMR3CommentList, afterMR3CommentList);
					} else if (graphType == GraphType.PROPERTY) {
						HistoryManager.saveHistory(HistoryType.EDIT_ONT_PROPERTY_COMMENT,
								beforeMR3CommentList, afterMR3CommentList);
					}
				}
			}
		}
	}

	public static void main(String[] args) {
		JFrame frame = new JFrame();
		CommentPanel commentPanel = new CommentPanel(new JFrame());
		ResourceModel info = new ClassModel("http://mrcube.org#test");
		info.addLabel(new MR3Literal("日本語コメントのテスト", "ja", null));
		info.addLabel(new MR3Literal("english comment test", "en", null));
		commentPanel.setResourceInfo(info);
		frame.getContentPane().add(commentPanel);
		frame.setSize(new Dimension(350, 200));
		frame.setVisible(true);
	}
}
