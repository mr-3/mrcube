/*
 * Created on 2003/09/17
 *
 */
package jp.ac.shizuoka.cs.panda.mmm.mr3.actions;

import java.awt.event.*;

import javax.swing.*;

import jp.ac.shizuoka.cs.panda.mmm.mr3.jgraph.*;

/**
 * @author takeshi morita
 */
public class RemoveAction extends AbstractAction {

	private RDFGraph graph;
	private GraphManager gmanager;

	public RemoveAction(RDFGraph g, GraphManager gm, String title) {
		super(title);
		graph = g;
		gmanager = gm;
	}

	public void actionPerformed(ActionEvent e) {
		gmanager.removeAction(graph);
	}
}
