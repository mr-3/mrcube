/*
 * Created on 2003/09/17
 *
 */
package org.semanticweb.mmm.mr3.actions;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;

import org.semanticweb.mmm.mr3.data.*;
import org.semanticweb.mmm.mr3.jgraph.*;
import org.semanticweb.mmm.mr3.util.*;

/**
 * @author takeshi morita
 */
public class TransformElementAction extends AbstractAction {

	private HashSet uriSet;
	private RDFGraph graph;
	private GraphType fromGraphType;
	private GraphType toGraphType;
	private GraphManager gmanager;
	private RDFSInfoMap rdfsInfoMap = RDFSInfoMap.getInstance();
	private RDFResourceInfoMap resInfoMap = RDFResourceInfoMap.getInstance();

	public TransformElementAction(RDFGraph g, GraphManager gm, GraphType fromType, GraphType toType) {
		super(Translator.getString("Action.TransformElement."+fromType+"To"+toType+".Text"));
		graph = g;
		gmanager = gm;
		fromGraphType = fromType;
		toGraphType = toType;
	}

	private Set getURISet() {
		uriSet = new HashSet();
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
		return uriSet;
	}

	private void insertElements(Set uriSet) {
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

	class TransformThread extends Thread {
		public void run() {
			while (gmanager.getRmDialog().isVisible()) {
				try {
					Thread.sleep(500); 
				} catch (InterruptedException ie) {
					ie.printStackTrace();
				}
			}
			if (isRmCellsRemoved()) {
				insertElements(uriSet);
			}
		}
	}

	private boolean isRmCellsRemoved() {
		Object[] cells = gmanager.getRemoveCells();
		for (int i = 0; i < cells.length; i++) {
			if (gmanager.getRDFGraph().getModel().contains(cells[i])
				|| gmanager.getClassGraph().getModel().contains(cells[i])
				|| gmanager.getPropertyGraph().getModel().contains(cells[i])) {
				return false;
			}
		}
		return true;
	}

	public void actionPerformed(ActionEvent e) {
		Set uriSet = getURISet();
		//		System.out.println(uriSet);
		gmanager.initRemoveAction(graph);
		gmanager.removeAction();
		// 削除した時に，メタモデル管理が行われるが，その間にinsertされないようにするための仕掛け
		// モーダルにできれば，いいが．．．
		new TransformThread().start();
	}

}
