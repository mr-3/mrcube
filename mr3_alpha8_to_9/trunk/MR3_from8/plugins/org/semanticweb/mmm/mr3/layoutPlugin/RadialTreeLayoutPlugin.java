/*
 * Created on 2003/11/25
 *  
 */
package org.semanticweb.mmm.mr3.layoutPlugin;

import java.awt.*;

import org.jgraph.*;
import org.jgraph.layout.*;
import org.semanticweb.mmm.mr3.plugin.*;

/**
 * @author takeshi morita
 */
public class RadialTreeLayoutPlugin extends MR3Plugin {
	
	public void applyRadialTreeLayout(JGraph graph, Point space) {
		RadialTreeLayoutAlgorithm radialTreeLayout = new RadialTreeLayoutAlgorithm();
		radialTreeLayout.perform(graph, true, null);
	}

	public void exec() {
		applyRadialTreeLayout(getRDFGraph(), new Point(200, 200));
		applyRadialTreeLayout(getClassGraph(), new Point(200, 200));
		applyRadialTreeLayout(getPropertyGraph(), new Point(200, 200));
	}
}
