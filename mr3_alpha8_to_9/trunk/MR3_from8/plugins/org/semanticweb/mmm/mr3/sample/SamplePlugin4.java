/*
 * Created on 2003/11/26
 *
 */
package org.semanticweb.mmm.mr3.sample;

import java.util.*;

import org.semanticweb.mmm.mr3.plugin.*;

/**
 * @author takeshi morita
 */
public class SamplePlugin4 extends MR3Plugin {

	public void exec() {
		String ns = "http://mmm.semanticweb.org/mr3#";
		Set selectionCells = new HashSet();
		selectionCells.add(ns+"a");
		selectionCells.add(ns+"b");
		selectionCells.add(ns+"c");
		selectRDFNodes(selectionCells);
		selectClassNodes(selectionCells);
		selectPropertyNodes(selectionCells);
	}

}
