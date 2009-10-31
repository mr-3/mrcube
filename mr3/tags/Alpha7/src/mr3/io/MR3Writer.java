/*
 * Created on 2003/03/27
 *
  */
package mr3.io;

import mr3.*;
import mr3.jgraph.*;
import mr3.util.*;

import com.hp.hpl.mesa.rdf.jena.mem.*;
import com.hp.hpl.mesa.rdf.jena.model.*;

/**
 * @author takeshi morita
 */
public class MR3Writer {

	private JGraphToRDF graphToRDF;

	public MR3Writer(GraphManager manager) {
		graphToRDF = new JGraphToRDF(manager);
	}

	public Model getRDFModel() {
		return graphToRDF.getRDFModel();
	}

	public Model getSelectedRDFModel() {
		return graphToRDF.getSelectedRDFModel();
	}

	public Model getRDFSModel() {
		Model model = null;
		try {
			model = new ModelMem();
			model.add(getClassModel());
			model.add(getPropertyModel());
		} catch (RDFException e) {
			e.printStackTrace();
		}

		return model;
	}

	public Model getSelectedRDFSModel() {
		Model model = null;
		try {
			model = new ModelMem();
			model.add(getSelectedClassModel());
			model.add(getSelectedPropertyModel());
		} catch (RDFException e) {
			e.printStackTrace();
		}

		return model;
	}

	public Model getClassModel() {
		return graphToRDF.getClassModel();
	}

	public Model getSelectedClassModel() {
		return graphToRDF.getSelectedClassModel();
	}

	public Model getPropertyModel() {
		return graphToRDF.getPropertyModel();
	}

	public Model getSelectedPropertyModel() {
		return graphToRDF.getSelectedPropertyModel();
	}

	public Model getProjectModel(MR3 mr3) {
		Model exportModel = getRDFModel();
		try {
			// 順番に注意．リテラルのモデルを抽出して，プロジェクトモデルを抽出してから
			// リテラルモデルを削除する
			// クラスとプロパティのリテラルモデルを抽出してはいけないので，
			// RDFモデルのリテラルモデルを抽出してから，ＲＤＦＳモデルを抽出する			
			ProjectManager projectManager = new ProjectManager(mr3.getGraphManager(), mr3.getNSTableDialog());
			Model literalModel = projectManager.getLiteralModel(exportModel);
			exportModel.add(getRDFSModel());
			exportModel.add(projectManager.getProjectModel());
			exportModel.remove(literalModel);
		} catch (RDFException e) {
			e.printStackTrace();
		}
		return exportModel;
	}
}
