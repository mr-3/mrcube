/*
 * MR^3のプラグインを作成するためのクラス
 *
 */
package jp.ac.shizuoka.cs.panda.mmm.mr3.plugin;

import javax.swing.*;

import jp.ac.shizuoka.cs.panda.mmm.mr3.*;

import org.jgraph.*;

import com.hp.hpl.jena.rdf.model.*;

/**
 * @author takeshi morita
 */
public abstract class MR3Plugin {

	private MR3 mr3;
	private String menuName;

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
	 * MR3Pluginクラスのサブクラスで実装する．
	 * File->pluginsに追加されるメニューを実行すると，execメソッドが実行される．
	 */
	public abstract void exec();

	public String toString() {
		return menuName;
	}

	/** 
	 *  Jenaが提供するModelを，MR3のRDFグラフへ変換する．
	 *  変換したRDFグラフを編集中のRDFグラフと置換する．
	 */
	protected void replaceRDFModel(Model model) {
		mr3.replaceRDFModel(model);
		mr3.getGraphManager().applyTreeLayout();
	}

	/** 
	 *  Jenaが提供するModelを，MR3のRDFグラフへ変換する．
	 *  変換したRDFグラフを編集中のRDFグラフにマージする．
	 */
	protected void mergeRDFModel(Model model) {
		mr3.mergeRDFModel(model);
		mr3.getGraphManager().applyTreeLayout();
	}

	/** 
	 *  Jenaが提供するModelを，MR3のRDFSグラフへ変換する．
	 *  変換したRDFSグラフを編集中のRDFSグラフにマージする．
	 */
	protected void mergeRDFSModel(Model model) {
		mr3.mergeRDFSModel(model);
		mr3.getGraphManager().applyTreeLayout();
	}

	/**
	 * Jenaが提供するModelを，MR^3のプロジェクトへ変換する．
	 * @param model MR^3のプロジェクトファイル． 
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
	 * @return Model
	 */
	protected Model getClassModel() {
		return mr3.getClassModel();
	}

	/**
	 * 選択されているMR3のクラスグラフをJenaのModelに変換する．
	 * @return Model
	 */
	protected Model getSelectedClassModel() {
		return mr3.getSelectedClassModel();
	}

	/**
	 * MR3のプロパティグラフをJenaのModelに変換する．
	 * @return Model
	 */
	protected Model getPropertyModel() {
		return mr3.getPropertyModel();
	}

	/**
	 * 選択されているMR3のプロパティグラフをJenaのModelに変換する．
	 * @return Model
	 */
	protected Model getSelectedPropertyModel() {
		return mr3.getSelectedPropertyModel();
	}

	/**
	 * プロジェクトをJenaのModelに変換する．
	 * @return Model
	 */
	protected Model getProjectModel() {
		return mr3.getProjectModel();
	}

	/**
	 * RDFグラフ(org.jgraph.JGraph)を得る．
	 * @return org.jgraph.JGraph
	 */
	protected JGraph getRDFGraph() {
		return mr3.getRDFGraph();
	}

	/**
	 * クラスグラフ(org.jgraph.JGraph)を得る．
	 * @return org.jgraph.JGraph
	 */
	protected JGraph getClassGraph() {
		return mr3.getClassGraph();
	}

	/**
	 * プロパティグラフ(org.jgraph.JGraph)を得る．
	 * @return org.jgraph.JGraph
	 */
	protected JGraph getPropertyGraph() {
		return mr3.getPropertyGraph();
	}

	/**
	 * BaseURIを得る．
	 * @return String
	 */
	protected String getBaseURI() {
		return mr3.getBaseURI();
	}

	/**
	 * JDesktopPaneを得る．内部ウィンドウを作成する際に用いる．
	 * @return JDesktopPane
	 */
	protected JDesktopPane getDesktopPane() {
		return mr3.getDesktopPane();
	}
}
