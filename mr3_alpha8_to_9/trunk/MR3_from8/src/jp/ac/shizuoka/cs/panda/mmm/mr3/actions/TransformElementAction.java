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
public class TransformElementAction extends AbstractAction {

	private RDFGraph graph;
	private GraphType fromGraphType;
	private GraphType toGraphType;
	private GraphManager gmanager;
	private RDFSInfoMap rdfsInfoMap = RDFSInfoMap.getInstance();
	private RDFResourceInfoMap resInfoMap = RDFResourceInfoMap.getInstance();

	public TransformElementAction(RDFGraph g, GraphManager gm, GraphType fromType, GraphType toType) {
		super("Transform from " + fromType + " to " + toType);
		graph = g;
		gmanager = gm;
		fromGraphType = fromType;
		toGraphType = toType;
	}

	public void actionPerformed(ActionEvent e) {
		Set uriSet = new HashSet();
		Object[] cells = graph.getDescendants(graph.getSelectionCells());
		for (int i = 0; i < cells.length; i++) {
			Object cell = cells[i];
			if (fromGraphType == GraphType.RDF && graph.isRDFResourceCell(cell)) {
				RDFResourceInfo info = resInfoMap.getCellInfo(cell);
				uriSet.add(info.getURIStr());
			} else if (fromGraphType == GraphType.CLASS && graph.isRDFSClassCell(cell)) {
				RDFSInfo info = rdfsInfoMap.getCellInfo(cell);
				uriSet.add(info.getURIStr());
			} else if (fromGraphType == GraphType.PROPERTY && graph.isRDFSPropertyCell(cell)) {
				RDFSInfo info = rdfsInfoMap.getCellInfo(cell);
				uriSet.add(info.getURIStr());
			}
			//			RDFSプロパティとクラスが重複してしまうため，複雑な処理が必要．			
			//				 else if (graph.isRDFPropertyCell(cell)) {
			//					Object propCell = rdfsInfoMap.getEdgeInfo(cell);
			//					RDFSInfo info = rdfsInfoMap.getCellInfo(propCell);
			//					resSet.add(info.getURI());
			//				}
		}
		gmanager.removeAction(graph);
//		System.out.println(uriSet);
		Point pt = new Point(100, 100);
		RDFCellMaker cellMaker = new RDFCellMaker(gmanager);
		for (Iterator i = uriSet.iterator(); i.hasNext();) {
			String uri = (String) i.next();
			if (toGraphType == GraphType.RDF) {
				cellMaker.insertRDFResource(pt, uri, null, URIType.URI);
			} else if (toGraphType == GraphType.CLASS) {
				cellMaker.insertClass(pt, uri);
			} else if (toGraphType == GraphType.PROPERTY) {
				cellMaker.insertProperty(pt, uri);
			}
			pt.x += 20;
			pt.y += 20;
		}
	}
}
