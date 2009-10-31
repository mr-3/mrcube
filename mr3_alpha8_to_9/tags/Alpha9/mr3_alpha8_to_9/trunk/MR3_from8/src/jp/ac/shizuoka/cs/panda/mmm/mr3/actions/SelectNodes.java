/*
 * Created on 2003/07/19
 *
 */
package jp.ac.shizuoka.cs.panda.mmm.mr3.actions;

import java.awt.event.*;

import jp.ac.shizuoka.cs.panda.mmm.mr3.jgraph.*;

/**
 * @author takeshi morita
 *
 */
public class SelectNodes extends MR3AbstractAction {

	private RDFGraph graph;

	public SelectNodes(RDFGraph g, String name) {
		super(name);
		graph = g;
	}

	public void actionPerformed(ActionEvent e) {
		String menuName = e.getActionCommand();
		graph.selectAllNodes();
	}

}
