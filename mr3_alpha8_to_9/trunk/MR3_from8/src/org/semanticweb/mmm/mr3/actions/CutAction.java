/*
 * Created on 2003/07/24
 *
 */
package org.semanticweb.mmm.mr3.actions;

import java.awt.event.*;

import javax.swing.*;

import org.semanticweb.mmm.mr3.jgraph.*;
import org.semanticweb.mmm.mr3.util.*;

/**
 * @author takeshi morita
 */
public class CutAction extends AbstractAction {
	private RDFGraph graph;
	private static final String TITLE = Translator.getString("Action.Cut.Text");
	private static final ImageIcon ICON = Utilities.getImageIcon(Translator.getString("Action.Cut.Icon"));

	public CutAction(RDFGraph g) {
		super(TITLE, ICON);
		graph = g;
		putValue(SHORT_DESCRIPTION, TITLE);
		putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_X, KeyEvent.CTRL_MASK));
	}

	public void actionPerformed(ActionEvent e) {
		graph.cut();
	}
}