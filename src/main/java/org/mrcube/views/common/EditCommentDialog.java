/*
 * Project Name: MR^3 (Meta-Model Management based on RDFs Revision Reflection)
 * Project Website: http://mrcube.org/
 * 
 * Copyright (C) 2003-2019 Yamaguchi Laboratory, Keio University. All rights reserved.
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

import org.mrcube.models.MR3Constants;
import org.mrcube.models.MR3Literal;
import org.mrcube.utils.Translator;
import org.mrcube.utils.Utilities;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * @author Takeshi Morita
 */
class EditCommentDialog extends JDialog implements ActionListener {

    private final MR3Literal literal;

    private final JTextField langField;
    private final JTextArea commentArea;

    private final JButton applyButton;
    private final JButton cancelButton;

    private static final int DIALOG_WIDTH = 300;
    private static final int DIALOG_HEIGHT = 250;

    public EditCommentDialog(Frame rootFrame) {
        super(rootFrame, Translator.getString("EditCommentDialog.Title"), true);

        literal = new MR3Literal();

        langField = new JTextField(5);
        JComponent langFieldP = Utilities.createTitledPanel(langField, MR3Constants.LANG, 50, 30);
        commentArea = new JTextArea(5, 20);
        commentArea.setLineWrap(true);
        JScrollPane commentAreaScroll = new JScrollPane(commentArea);
        commentAreaScroll.setBorder(BorderFactory.createTitledBorder(MR3Constants.COMMENT));

        applyButton = new JButton(MR3Constants.OK);
        applyButton.setMnemonic('o');
        applyButton.addActionListener(this);
        cancelButton = new JButton(MR3Constants.CANCEL);
        cancelButton.setMnemonic('c');
        cancelButton.addActionListener(this);
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(1, 2, 5, 5));
        buttonPanel.add(applyButton);
        buttonPanel.add(cancelButton);

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(Utilities.createWestPanel(langFieldP), BorderLayout.NORTH);
        getContentPane().add(commentAreaScroll, BorderLayout.CENTER);
        getContentPane().add(Utilities.createEastPanel(buttonPanel), BorderLayout.SOUTH);

        setSize(new Dimension(DIALOG_WIDTH, DIALOG_HEIGHT));
        setLocationRelativeTo(rootFrame);
        setVisible(false);
    }

    public void setComment(String lang, String comment) {
        langField.setText(lang);
        commentArea.setText(comment);
    }

    public MR3Literal getComment() {
        return literal;
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == applyButton) {
            literal.setLanguage(langField.getText());
            literal.setString(commentArea.getText());
            commentArea.requestFocus();
            setVisible(false);
        } else if (e.getSource() == cancelButton) {
            literal.setLanguage("");
            literal.setString("");
            commentArea.requestFocus();
            setVisible(false);
        }
    }

    public static void main(String[] args) {
        EditCommentDialog dialog = new EditCommentDialog(null);
        dialog.setVisible(true);
    }
}
