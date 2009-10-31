/*
 * Created on 2003/09/23
 *
 */
package jp.ac.shizuoka.cs.panda.mmm.mr3.layoutPlugin;

import java.awt.*;

import jp.ac.shizuoka.cs.panda.mmm.mr3.plugin.*;

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
		applySugiyamaLayout(getClassGraph(), new Point(200, 200));
		applySugiyamaLayout(getPropertyGraph(), new Point(200, 200));
	}
}
