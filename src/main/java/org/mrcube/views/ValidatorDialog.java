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

import org.apache.jena.rdf.model.*;
import org.apache.jena.reasoner.ValidityReport;
import org.mrcube.io.MR3Writer;
import org.mrcube.jgraph.GraphManager;
import org.mrcube.models.MR3Constants;
import org.mrcube.utils.Translator;
import org.mrcube.utils.Utilities;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Iterator;

/**
 * @author Takeshi Morita
 */
public class ValidatorDialog extends JDialog {

	private MR3Writer mr3Writer;
	private GraphManager gmanager;

	private JEditorPane indicationPane;

    private static final int WINDOW_HEIGHT = 400;
	private static final int WINDOW_WIDTH = 550;

	private static final String TITLE = Translator.getString("Component.Tools.Validator.Text");

	public ValidatorDialog(Frame frame, GraphManager gm) {
		super(frame, TITLE);
		setIconImage(Utilities.getImageIcon("accept.png").getImage());
		mr3Writer = new MR3Writer(gm);
		gmanager = gm;

		indicationPane = new JEditorPane();
		indicationPane.addHyperlinkListener(new HyperlinkAction());
		indicationPane.setEditable(false);
		indicationPane.setContentType("text/html");
		JScrollPane showWarningAreaScroll = new JScrollPane(indicationPane);

		getContentPane().add(showWarningAreaScroll, BorderLayout.CENTER);
		getContentPane().add(getButtonPanel(), BorderLayout.SOUTH);

		setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
		setLocationRelativeTo(this);
		setVisible(false);
	}

	class HyperlinkAction implements HyperlinkListener {
		public void hyperlinkUpdate(HyperlinkEvent e) {
			if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
				Resource res = ResourceFactory.createResource(e.getURL().toString());
				Object cell = gmanager.getRDFResourceCell(res);
				if (cell != null) {
					gmanager.selectRDFCell(cell);
					return;
				}
				cell = gmanager.getRDFPropertyCell(res);
				if (cell != null) {
					gmanager.selectRDFCell(cell);
					return;
				}
				cell = gmanager.getClassCell(res);
				if (cell != null) {
					gmanager.selectClassCell(cell);
					return;
				}
				cell = gmanager.getPropertyCell(res);
				if (cell != null) {
					gmanager.selectPropertyCell(cell);
				}
			}
		}
	}

	private JComponent getButtonPanel() {
        JButton validateButton = new JButton(new ValidateAction());
		validateButton.setMnemonic('v');
        JButton cancelButton = new JButton(new CancelAction());
		cancelButton.setMnemonic('c');
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new GridLayout(1, 2, 5, 5));
		buttonPanel.add(validateButton);
		buttonPanel.add(cancelButton);
		return Utilities.createEastPanel(buttonPanel);
	}

	private void validateModel() {
		Model model = mr3Writer.getRDFModel();
		model.add(mr3Writer.getRDFSModel());
		StringBuilder messages = new StringBuilder();
		InfModel infmodel = ModelFactory.createRDFSModel(model);
		ValidityReport validity = infmodel.validate();
		if (validity.isValid()) {
			messages.append("OK");
		} else {
			messages.append("Conflicts");
			messages.append(System.lineSeparator());
			for (Iterator i = validity.getReports(); i.hasNext();) {
				messages.append(" - ").append(i.next());
				messages.append(System.lineSeparator());
			}
		}
		indicationPane.setText(messages.toString());
	}

	class ValidateAction extends AbstractAction {
		ValidateAction() {
			super(Translator.getString("Component.Tools.Validator.Validate"));
		}

		public void actionPerformed(ActionEvent e) {
			validateModel();
		}
	}

	class CancelAction extends AbstractAction {
		CancelAction() {
			super(MR3Constants.CANCEL);
		}

		public void actionPerformed(ActionEvent e) {
			setVisible(false);
		}
	}

	public void setVisible(boolean t) {
		validateModel();
		super.setVisible(t);
	}
}
