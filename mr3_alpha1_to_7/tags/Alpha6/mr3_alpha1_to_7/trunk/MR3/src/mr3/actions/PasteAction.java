/*
 * Created on 2003/07/24
 *
 */
package mr3.actions;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import mr3.jgraph.*;

/**
 * @author takeshi morita
 */

public class PasteAction extends AbstractAction {
	RDFGraph graph;

	public PasteAction(RDFGraph g, String title) {
		super(title);
		graph = g;
	}

	public void actionPerformed(ActionEvent e) {
		graph.paste(new Point(100, 100));
	}
}