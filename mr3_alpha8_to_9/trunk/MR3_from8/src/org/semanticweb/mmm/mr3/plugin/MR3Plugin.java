/*
 * MR^3のプラグインを作成するためのクラス
 *  
 */
package org.semanticweb.mmm.mr3.plugin;

import java.util.*;

import javax.swing.*;

import org.jgraph.*;
import org.semanticweb.mmm.mr3.*;
import org.semanticweb.mmm.mr3.data.*;
import org.semanticweb.mmm.mr3.jgraph.*;

import com.hp.hpl.jena.rdf.model.*;

/**
 * @author takeshi morita
 */
public abstract class MR3Plugin {

	private MR3 mr3;
	private String menuName;
	private RDFSInfoMap rdfsInfoMap = RDFSInfoMap.getInstance();
	private RDFResourceInfoMap resInfoMap = RDFResourceInfoMap.getInstance();

	protected MR3Plugin(String mn) {
		menuName = mn;
	}

	protected MR3Plugin() {
		menuName = "none";
	}

	public String getMenuName() {
		return menuName;
	}

	public void setMR3(MR3 mr3) {
		this.mr3 = mr3;
	}

	/**
	 * MR3Pluginクラスのサブクラスで実装する． File->pluginsに追加されるメニューを実行すると，execメソッドが実行される．
	 */
	public abstract void exec();

	public String toString() {
		return menuName;
	}

	/**
	 * Jenaが提供するModelを，MR3のRDFグラフへ変換する． 変換したRDFグラフを編集中のRDFグラフと置換する．
	 */
	protected void replaceRDFModel(Model model) {
		mr3.replaceRDFModel(model);
		mr3.performTreeLayout();
	}

	/**
	 * Jenaが提供するModelを，MR3のRDFグラフへ変換する． 変換したRDFグラフを編集中のRDFグラフにマージする．
	 */
	protected void mergeRDFModel(Model model) {
		mr3.mergeRDFModel(model);
	}

	/**
	 * Jenaが提供するModelを，MR3のRDFSグラフへ変換する． 変換したRDFSグラフを編集中のRDFSグラフにマージする．
	 */
	protected void mergeRDFSModel(Model model) {
		mr3.mergeRDFSModel(model);
		mr3.performTreeLayout();
	}

	/**
	 * Jenaが提供するModelを，MR^3のプロジェクトへ変換する．
	 * 
	 * @param model
	 *                MR^3のプロジェクトファイル．
	 */
	protected void replaceProjectModel(Model model) {
		mr3.replaceProjectModel(model);
	}

	/**
	 * MR3のRDFグラフをJenaのModelに変換する．
	 */
	protected Model getRDFModel() {
		return mr3.getRDFModel();
	}

	/**
	 * 選択されているMR3のRDFグラフをJenaのModelに変換する．
	 */
	protected Model getSelectedRDFModel() {
		return mr3.getSelectedRDFModel();
	}

	/**
	 * MR3のRDFSグラフをJenaのModelに変換する．
	 */
	protected Model getRDFSModel() {
		return mr3.getRDFSModel();
	}

	/**
	 * 選択されているMR3のRDFSグラフをJenaのModelに変換する．
	 */
	protected Model getSelectedRDFSModel() {
		return mr3.getSelectedRDFSModel();
	}

	/**
	 * MR3のクラスグラフをJenaのModelに変換する．
	 * 
	 * @return Model
	 */
	protected Model getClassModel() {
		return mr3.getClassModel();
	}

	/**
	 * 選択されているMR3のクラスグラフをJenaのModelに変換する．
	 * 
	 * @return Model
	 */
	protected Model getSelectedClassModel() {
		return mr3.getSelectedClassModel();
	}

	/**
	 * MR3のプロパティグラフをJenaのModelに変換する．
	 * 
	 * @return Model
	 */
	protected Model getPropertyModel() {
		return mr3.getPropertyModel();
	}

	/**
	 * 選択されているMR3のプロパティグラフをJenaのModelに変換する．
	 * 
	 * @return Model
	 */
	protected Model getSelectedPropertyModel() {
		return mr3.getSelectedPropertyModel();
	}

	/**
	 * プロジェクトをJenaのModelに変換する．
	 * 
	 * @return Model
	 */
	protected Model getProjectModel() {
		return mr3.getProjectModel();
	}

	/**
	 * RDFグラフ(org.jgraph.JGraph)を得る．
	 * 
	 * @return org.jgraph.JGraph
	 */
	protected JGraph getRDFGraph() {
		return mr3.getRDFGraph();
	}

	/**
	 * クラスグラフ(org.jgraph.JGraph)を得る．
	 * 
	 * @return org.jgraph.JGraph
	 */
	protected JGraph getClassGraph() {
		return mr3.getClassGraph();
	}

	/**
	 * プロパティグラフ(org.jgraph.JGraph)を得る．
	 * 
	 * @return org.jgraph.JGraph
	 */
	protected JGraph getPropertyGraph() {
		return mr3.getPropertyGraph();
	}

	/**
	 * BaseURIを得る．
	 * 
	 * @return String
	 */
	protected String getBaseURI() {
		return mr3.getBaseURI();
	}

	/**
	 * JDesktopPaneを得る．内部ウィンドウを作成する際に用いる．
	 * 
	 * @return JDesktopPane
	 */
	protected JDesktopPane getDesktopPane() {
		return mr3.getDesktopPane();
	}

	/**
	 * 
	 * URI文字列のセットを受け取って，RDFエディタ内の指定されたノードを選択する
	 *  
	 */
	protected void selectRDFNodes(Set nodes) {
		Set selectionCells = new HashSet();
		RDFGraph graph = mr3.getRDFGraph();

		for (Iterator node = nodes.iterator(); node.hasNext();) {
			String uri = (String) node.next();
			addRDFNode(graph, uri, selectionCells);
		}
		graph.setSelectionCells(selectionCells.toArray());
	}

	private void addRDFNode(RDFGraph graph, String uri, Set selectionCells) {	
		Object[] cells = graph.getAllCells();
		for (int i = 0; i < cells.length; i++) {
			if (graph.isRDFResourceCell(cells[i])) {
				RDFResourceInfo info = resInfoMap.getCellInfo(cells[i]);
				if (uri.equals(info.getURIStr())) {
					selectionCells.add(cells[i]);
				}
			} else if (graph.isRDFPropertyCell(cells[i])) {
				Object propCell = rdfsInfoMap.getEdgeInfo(cells[i]);
				RDFSInfo info = rdfsInfoMap.getCellInfo(propCell);
				if (uri.equals(info.getURIStr())) {
					selectionCells.add(cells[i]);
				}
			}
		}
	}

	/**
	 * 
	 * URI文字列のセットを受け取って，クラスエディタ内の指定されたノードを選択する
	 *  
	 */
	protected void selectClassNodes(Set nodes) {
		Set selectionCells = new HashSet();
		RDFGraph graph = mr3.getClassGraph();
		graph.clearSelection();
		for (Iterator i = nodes.iterator(); i.hasNext();) {
			Object cell = rdfsInfoMap.getClassCell(ResourceFactory.createResource((String) i.next()));
			if (cell != null) {
				selectionCells.add(cell);
			}
		}
		graph.setSelectionCells(selectionCells.toArray());
	}

	/**
	 * 
	 * URI文字列のセットを受け取って，プロパティエディタ内の指定されたノードを選択する
	 *  
	 */
	protected void selectPropertyNodes(Set nodes) {
		Set selectionCells = new HashSet();
		RDFGraph graph = mr3.getPropertyGraph();
		graph.clearSelection();
		for (Iterator i = nodes.iterator(); i.hasNext();) {
			Object cell = rdfsInfoMap.getPropertyCell(ResourceFactory.createResource((String) i.next()));
			if (cell != null) {
				selectionCells.add(cell);
			}
		}
		graph.setSelectionCells(selectionCells.toArray());
	}

}
