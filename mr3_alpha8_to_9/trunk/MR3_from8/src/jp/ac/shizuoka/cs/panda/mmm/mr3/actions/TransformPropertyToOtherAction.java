/*
 * Created on 2003/09/17
 *
 */
package jp.ac.shizuoka.cs.panda.mmm.mr3.actions;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;

import jp.ac.shizuoka.cs.panda.mmm.mr3.data.*;
import jp.ac.shizuoka.cs.panda.mmm.mr3.jgraph.*;
import jp.ac.shizuoka.cs.panda.mmm.mr3.util.*;

/**
 * @author takeshi morita
 */
public class TransformPropertyToOtherAction extends AbstractAction {

	private RDFGraph graph;
	private GraphType graphType;
	private GraphManager gmanager;
	private RDFSInfoMap rdfsInfoMap = RDFSInfoMap.getInstance();
	private RDFResourceInfoMap resInfoMap = RDFResourceInfoMap.getInstance();

	public TransformPropertyToOtherAction(RDFGraph g, GraphManager gm, GraphType type) {
		super("Transform from Property to " + type);
		graph = g;
		gmanager = gm;
		graphType = type;
	}

	public void actionPerformed(ActionEvent e) {
		Set uriSet = new HashSet();
		Object[] cells = graph.getAllCells();
		for (int i = 0; i < cells.length; i++) {
			Object cell = cells[i];
			if (graph.isRDFSPropertyCell(cell)) {
				RDFSInfo info = rdfsInfoMap.getCellInfo(cell);
				uriSet.add(info.getURIStr());
			}
			//	   RDFSプロパティとクラスが重複してしまうため
			//				 else if (graph.isRDFPropertyCell(cell)) {
			//					Object propCell = rdfsInfoMap.getEdgeInfo(cell);
			//					RDFSInfo info = rdfsInfoMap.getCellInfo(propCell);
			//					resSet.add(info.getURI());
			//				}
		}
		gmanager.removeAction(graph);
		//			System.out.println(uriSet);
		Point pt = new Point(100, 100);
		RDFCellMaker cellMaker = new RDFCellMaker(gmanager);
		for (Iterator i = uriSet.iterator(); i.hasNext();) {
			String uri = (String) i.next();
			if (graphType == GraphType.RDF) {
				cellMaker.insertRDFResource(pt, uri, null, URIType.URI);
			} else if (graphType == GraphType.CLASS) {
				cellMaker.insertClass(pt, uri);
			}
			pt.x += 20;
			pt.y += 20;
		}
	}
}
