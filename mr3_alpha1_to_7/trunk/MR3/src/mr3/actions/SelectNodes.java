/*
 * Created on 2003/07/19
 *
 */
package mr3.actions;

import java.awt.event.*;

import mr3.*;
import mr3.jgraph.*;

/**
 * @author takeshi morita
 *
 */
public class SelectNodes extends MR3AbstractAction {

	private static final String SELECT_ALL_NODES = "Select All Nodes";
	private static final String SELECT_ALL_RDF_NODES = "Select All RDF Nodes";
	private static final String SELECT_ALL_CLASS_NODES = "Select All Class Nodes";
	private static final String SELECT_ALL_PROPERTY_NODES = "Select All Property Nodes";

	public SelectNodes(MR3 mr3, String name) {
		super(mr3, name);
	}

	public void actionPerformed(ActionEvent e) {
		String menuName = e.getActionCommand();
		GraphManager gmanager = mr3.getGraphManager();

		if (menuName.equals(SELECT_ALL_NODES)) {
			gmanager.selectAllNodes();
		} else if (menuName.equals(SELECT_ALL_RDF_NODES)) {
			gmanager.selectAllRDFNodes();
		} else if (menuName.equals(SELECT_ALL_CLASS_NODES)) {
			gmanager.selectAllClassNodes();
		} else if (menuName.equals(SELECT_ALL_PROPERTY_NODES)) {
			gmanager.selectAllPropertyNodes();
		}
	}

}
