/*
 * Project Name: MR^3 (Meta-Model Management based on RDFs Revision Reflection)
 * Project Website: https://mr-3.github.io/
 * 
 * Copyright (C) 2003-2025 Takeshi Morita. All rights reserved.
 * 
 * This file is part of MR^3.
 * 
 * MR^3 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * MR^3 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with MR^3.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */

package jp.ac.aoyama.it.ke.mrcube.plugin;

import jp.ac.aoyama.it.ke.mrcube.MR3;
import jp.ac.aoyama.it.ke.mrcube.actions.GroupAction;
import jp.ac.aoyama.it.ke.mrcube.actions.UnGroupAction;
import jp.ac.aoyama.it.ke.mrcube.jgraph.RDFGraph;
import jp.ac.aoyama.it.ke.mrcube.layout.GraphLayoutUtilities;
import jp.ac.aoyama.it.ke.mrcube.models.*;
import jp.ac.aoyama.it.ke.mrcube.utils.GraphUtilities;
import jp.ac.aoyama.it.ke.mrcube.utils.MR3CellMaker;
import jp.ac.aoyama.it.ke.mrcube.views.MR3TreePanel;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.vocabulary.RDFS;
import org.jgraph.JGraph;
import org.jgraph.graph.GraphCell;
import org.jgraph.graph.GraphConstants;

import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import java.util.HashSet;
import java.util.Set;

/**
 * MR3のプラグインを作成するためのクラス
 * 
 * @author Takeshi Morita
 */
public abstract class MR3Plugin {

    private MR3 mr3;
    private final String menuName;

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
     * MR3Pluginクラスのサブクラスで実装する． File -- pluginsに追加されるメニューを実行すると，execメソッドが実行される．
     */
    public abstract void exec();

    public String toString() {
        return menuName;
    }


    /**
     * 
     * MR３で設定した出力エンコーディングの文字列を返す
     * 
     * @return MR3で設定した出力エンコーディングの文字列（exp. SJIS, EUC_JP, etc)
     */
    protected String getOutputEncoding() {
        return mr3.getUserPrefs().get(PrefConstants.OutputEncoding, "SJIS");
    }

    /**
     * Jenaが提供するModelを，MR3のRDFグラフへ変換する． 変換したRDFグラフを編集中のRDFグラフと置換する．
     */
    protected void replaceRDFModel(Model model) {
        mr3.getMR3Reader().replaceRDFModel(model);
    }

    /**
     * Jenaが提供するModelを，MR3のRDFグラフへ変換する． 変換したRDFグラフを編集中のRDFグラフにマージする．
     */
    protected void mergeRDFModel(Model model) {
        mr3.getMR3Reader().mergeRDFModelThread(model);
    }

    /**
     * Jenaが提供するModelを，MR3のRDFSグラフへ変換する． 変換したRDFSグラフを編集中のRDFSグラフにマージする．
     */
    protected void mergeRDFSModel(Model model) {
        mr3.getMR3Reader().mergeRDFandRDFSModel(model);
    }

    /**
     * Jenaが提供するOntModelを，MR3のグラフへ変換する． 変換したグラフを編集中のグラフにマージする．
     */
    protected void mergeOntologyModel(OntModel model) {
        mr3.getMR3Reader().mergeOntologyModel(model);
        mr3.getMR3Reader().performTreeLayout();
    }

    /**
     * Jenaが提供するModelを，MR3のプロジェクトへ変換する．
     * 
     * @param model
     *            MR3のプロジェクトファイル．
     * 
     */
    protected void replaceProjectModel(Model model) {
        mr3.getMR3Reader().replaceProjectModel(model);
    }

    /**
     * MR3のRDFグラフをJenaのModelに変換する．
     */
    protected Model getRDFModel() {
        return mr3.getMR3Writer().getRDFModel();
    }

    /**
     * 選択されているMR3のRDFグラフをJenaのModelに変換する．
     */
    protected Model getSelectedRDFModel() {
        return mr3.getMR3Writer().getSelectedRDFModel();
    }

    /**
     * MR3のRDFSグラフをJenaのModelに変換する．
     */
    protected Model getRDFSModel() {
        return mr3.getMR3Writer().getRDFSModel();
    }

    /**
     * 選択されているMR3のRDFSグラフをJenaのModelに変換する．
     */
    protected Model getSelectedRDFSModel() {
        return mr3.getMR3Writer().getSelectedRDFSModel();
    }

    /**
     * MR3のクラスグラフをJenaのModelに変換する．
     * 
     * @return Model
     */
    protected Model getClassModel() {
        return mr3.getMR3Writer().getClassModel();
    }

    /**
     * MR3のクラスグラフをJTreeのTreeModelに変換する．
     * 
     * @return TreeModel
     */
    protected TreeModel getClassTreeModel() {
        TreeNode rootNode = MR3TreePanel.getRDFSTreeRoot(mr3.getMR3Writer().getClassModel(), RDFS.Resource,
                RDFS.subClassOf);
        return new DefaultTreeModel(rootNode);
    }

    /**
     * 選択されているMR3のクラスグラフをJenaのModelに変換する．
     * 
     * @return Model
     */
    protected Model getSelectedClassModel() {
        return mr3.getMR3Writer().getSelectedClassModel();
    }

    /**
     * MR3のプロパティグラフをJenaのModelに変換する．
     * 
     * @return Model
     */
    protected Model getPropertyModel() {
        return mr3.getMR3Writer().getPropertyModel();
    }

    /**
     * MR3のプロパティグラフをJTreeのTreeModelに変換する．
     * 
     * @return TreeModel
     */
    protected TreeModel getPropertyTreeModel() {
        TreeNode rootNode = MR3TreePanel.getRDFSTreeRoot(mr3.getMR3Writer().getPropertyModel(), MR3Resource.Property,
                RDFS.subPropertyOf);
        return new DefaultTreeModel(rootNode);
    }

    /**
     * 選択されているMR3のプロパティグラフをJenaのModelに変換する．
     * 
     * @return Model
     */
    protected Model getSelectedPropertyModel() {
        return mr3.getMR3Writer().getSelectedPropertyModel();
    }

    /**
     * プロジェクトをJenaのModelに変換する．
     * 
     * @return Model
     */
    protected Model getProjectModel() {
        return mr3.getMR3Writer().getProjectModel();
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
     * 
     * URI文字列のセットを受け取って，RDFエディタ内の指定されたノードを選択する
     * 
     */
    private void selectRDFNodes(Set nodes) {
        Set selectionCells = new HashSet();
        RDFGraph graph = mr3.getRDFGraph();

        for (Object node1 : nodes) {
            String uri = (String) node1;
            addRDFNode(graph, uri, selectionCells);
        }
        graph.setSelectionCells(selectionCells.toArray());
    }

    private void addRDFNode(RDFGraph graph, String uri, Set selectionCells) {
        for (Object cell : graph.getAllCells()) {
            if (RDFGraph.isRDFResourceCell(cell)) {
                InstanceModel info = (InstanceModel) GraphConstants.getValue(((GraphCell) cell).getAttributes());
                if (uri.equals(info.getURIStr())) {
                    selectionCells.add(cell);
                }
            } else if (RDFGraph.isRDFPropertyCell(cell)) {
                GraphCell propCell = (GraphCell) cell;
                RDFSModel info = (RDFSModel) GraphConstants.getValue(propCell.getAttributes());
                if (uri.equals(info.getURIStr())) {
                    selectionCells.add(cell);
                }
            }
        }
    }

    /**
     * 
     * URI文字列のセットを受け取って，クラスエディタ内の指定されたノードを選択する
     * 
     */
    private void selectClassNodes(Set nodes) {
        Set selectionCells = new HashSet();
        RDFGraph graph = mr3.getClassGraph();
        graph.clearSelection();
        RDFSModelMap rdfsModelMap = mr3.getGraphManager().getRDFSInfoMap();
        for (Object node : nodes) {
            Object cell = rdfsModelMap.getClassCell(ResourceFactory.createResource((String) node));
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
    private void selectPropertyNodes(Set nodes) {
        Set selectionCells = new HashSet();
        RDFGraph graph = mr3.getPropertyGraph();
        graph.clearSelection();
        RDFSModelMap rdfsModelMap = mr3.getGraphManager().getRDFSInfoMap();
        for (Object node : nodes) {
            Object cell = rdfsModelMap.getPropertyCell(ResourceFactory.createResource((String) node));
            if (cell != null) {
                selectionCells.add(cell);
            }
        }
        graph.setSelectionCells(selectionCells.toArray());
    }

    /**
     * 
     * URI文字列のセットを受け取って，RDFエディタ内の指定されたノードをグループ化する
     * 
     */
    protected void groupRDFNodes(Set nodes) {
        selectRDFNodes(nodes);
        GroupAction.group(mr3.getRDFGraph());
    }

    /**
     * 
     * URI文字列のセットを受け取って，RDFエディタ内の指定されたノードを非グループ化する
     * 
     */
    protected void unGroupRDFNodes(Set nodes) {
        selectRDFNodes(nodes);
        UnGroupAction.ungroup(mr3.getRDFGraph());
    }

    /**
     * 
     * URI文字列のセットを受け取って，クラスエディタ内の指定されたノードをグループ化する
     * 
     */
    protected void groupClassNodes(Set nodes) {
        selectClassNodes(nodes);
        GroupAction.group(mr3.getClassGraph());
    }

    /**
     * 
     * URI文字列のセットを受け取って，クラスエディタ内の指定されたノードを非グループ化する
     * 
     */
    protected void unGroupClassNodes(Set nodes) {
        selectClassNodes(nodes);
        UnGroupAction.ungroup(mr3.getClassGraph());
    }

    /**
     * 
     * URI文字列のセットを受け取って，プロパティエディタ内の指定されたノードをグループ化する
     * 
     */
    protected void groupPropertyNodes(Set nodes) {
        selectPropertyNodes(nodes);
        GroupAction.group(mr3.getPropertyGraph());
    }

    /**
     * 
     * URI文字列のセットを受け取って，プロパティエディタ内の指定されたノードを非グループ化する
     * 
     */
    protected void unGroupPropertyNodes(Set nodes) {
        selectPropertyNodes(nodes);
        UnGroupAction.ungroup(mr3.getPropertyGraph());
    }

    /**
     * 
     * URI文字列のセットを受け取り，RDFエディタ内のノードを強調する
     * 
     * @param nodes
     */
    protected void emphasisRDFNodes(Set nodes) {
        selectRDFNodes(nodes);
        GraphUtilities.emphasisNodes(mr3.getRDFGraph());
    }

    /**
     * 
     * URI文字列のセットを受け取り，クラスエディタ内のノードを強調する
     * 
     * @param nodes
     */
    protected void emphasisClassNodes(Set nodes) {
        selectClassNodes(nodes);
        GraphUtilities.emphasisNodes(mr3.getClassGraph());
    }

    /**
     * 
     * URI文字列のセットを受け取り，プロパティエディタ内のノードを強調する
     * 
     * @param nodes
     */
    protected void emphasisPropertyNodes(Set nodes) {
        selectPropertyNodes(nodes);
        GraphUtilities.emphasisNodes(mr3.getPropertyGraph());
    }

    protected void reverseClassArc() {
        GraphLayoutUtilities.reverseArc(new MR3CellMaker(mr3.getGraphManager()), mr3.getClassGraph());
    }

    protected void reversePropertyArc() {
        GraphLayoutUtilities.reverseArc(new MR3CellMaker(mr3.getGraphManager()), mr3.getPropertyGraph());
    }
}
