/*
 * Created on 2003/09/17
 *
 */
package org.semanticweb.mmm.mr3.actions;

import java.awt.event.*;

import javax.swing.*;

import org.semanticweb.mmm.mr3.jgraph.*;
import org.semanticweb.mmm.mr3.ui.*;
import org.semanticweb.mmm.mr3.util.*;

/**
 * @author takeshi morita
 */
public class FindResAction extends AbstractAction {

	private RDFGraph graph;
	private FindResourceDialog findResDialog;
	private static final String TITLE = Translator.getString("Component.Edit.FindResource.Text");
	private static final ImageIcon ICON = Utilities.getImageIcon("find.gif"); 
	
	public FindResAction(RDFGraph g, FindResourceDialog frd) {
		super(TITLE, ICON);
		setValues();
		graph = g;
		findResDialog = frd;
	}

	private void setValues() {
		putValue(SHORT_DESCRIPTION, TITLE);
		putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_F, KeyEvent.ALT_MASK));
	}

	public void actionPerformed(ActionEvent e) {
		findResDialog.setSearchArea(graph.getType());
		findResDialog.setVisible(true);
	}
}
