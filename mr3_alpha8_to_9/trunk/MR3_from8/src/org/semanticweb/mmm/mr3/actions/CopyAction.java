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
public class CopyAction extends AbstractAction {
	
	private RDFGraph graph;
	private static final String TITLE = Translator.getString("Action.Copy.Text");
	private static final ImageIcon ICON = Utilities.getImageIcon(Translator.getString("Action.Copy.Icon")); 

	public CopyAction(RDFGraph g) {
		super(TITLE, ICON);
		graph = g;
		putValue(SHORT_DESCRIPTION, TITLE);
		putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_C, KeyEvent.CTRL_MASK));
	}

	public void actionPerformed(ActionEvent e) {
		graph.copy();
	}
}
