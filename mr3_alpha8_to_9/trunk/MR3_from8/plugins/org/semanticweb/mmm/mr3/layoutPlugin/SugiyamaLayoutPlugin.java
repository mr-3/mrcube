/*
 * Created on 2003/09/23
 *
 */
package org.semanticweb.mmm.mr3.layoutPlugin;

import java.awt.*;

import org.semanticweb.mmm.mr3.plugin.*;

import org.jgraph.*;

/**
 * @author takeshi morita
 */
public class SugiyamaLayoutPlugin extends MR3Plugin {

	public void applySugiyamaLayout(JGraph graph, Point space) {
		SugiyamaLayoutAlgorithm sugiyamaLayout = new SugiyamaLayoutAlgorithm();		
		sugiyamaLayout.perform(graph, true, space);
	}

	public void exec() {
		applySugiyamaLayout(getRDFGraph(), new Point(200, 200));
		
		reverseClassArc();
		applySugiyamaLayout(getClassGraph(), new Point(200, 200));
		reverseClassArc();
		
		reversePropertyArc();
		applySugiyamaLayout(getPropertyGraph(), new Point(200, 200));
		reversePropertyArc();
	}
}
