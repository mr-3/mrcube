/*
 * Created on 2003/07/19
 *
 */
package mr3.actions;

import java.awt.event.*;

import mr3.jgraph.*;

/**
 * @author takeshi morita
 *
 */
public class SelectNodes extends MR3AbstractAction {

	private static final String SELECT_ALL_RDF_NODES = "Select All RDF Nodes";
	private static final String SELECT_ALL_CLASS_NODES = "Select All Class Nodes";
	private static final String SELECT_ALL_PROPERTY_NODES = "Select All Property Nodes";

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
