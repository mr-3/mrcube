/*
 * Created on 2003/07/24
 *
 */
package jp.ac.shizuoka.cs.panda.mmm.mr3.actions;

import java.awt.event.*;

import javax.swing.*;

import jp.ac.shizuoka.cs.panda.mmm.mr3.jgraph.*;
import jp.ac.shizuoka.cs.panda.mmm.mr3.util.*;

/**
 * @author takeshi morita
 */

public class PasteAction extends AbstractAction {

	private RDFGraph graph;
	private static final String TITLE = "Paste";
	private static final ImageIcon ICON = Utilities.getImageIcon("paste.gif");

	public PasteAction(RDFGraph g) {
		super(TITLE, ICON);
		graph = g;
		putValue(SHORT_DESCRIPTION, TITLE);
	}

	public void actionPerformed(ActionEvent e) {
		graph.paste();
	}
}
