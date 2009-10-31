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
public class CopyAction extends AbstractAction {
	RDFGraph graph;

	public CopyAction(RDFGraph g, String title) {
		super(title);
		graph = g;
	}

	public void actionPerformed(ActionEvent e) {
		graph.copy(new Point(100, 100));
	}
}
