/*
 * Created on 2003/03/27
 *  
 */
package org.semanticweb.mmm.mr3.io;

import org.semanticweb.mmm.mr3.*;
import org.semanticweb.mmm.mr3.jgraph.*;
import org.semanticweb.mmm.mr3.util.*;

import com.hp.hpl.jena.rdf.model.*;

/**
 * @author takeshi morita
 */
public class MR3Writer {

	private MR3Generator mr3Generator;

	public MR3Writer(GraphManager manager) {
		mr3Generator = new MR3Generator(manager);
	}

	public Model getRDFModel() {
		return mr3Generator.getRDFModel();
	}

	public Model getSelectedRDFModel() {
		return mr3Generator.getSelectedRDFModel();
	}

	public Model getRDFSModel() {
		Model model = ModelFactory.createDefaultModel();
		try {
			model.add(getClassModel());
			model.add(getPropertyModel());
		} catch (RDFException e) {
			e.printStackTrace();
		}

		return model;
	}

	public Model getSelectedRDFSModel() {
		Model model = ModelFactory.createDefaultModel();
		try {
			model.add(getSelectedClassModel());
			model.add(getSelectedPropertyModel());
		} catch (RDFException e) {
			e.printStackTrace();
		}

		return model;
	}

	public Model getClassModel() {
		return mr3Generator.getClassModel();
	}

	public Model getSelectedClassModel() {
		return mr3Generator.getSelectedClassModel();
	}

	public Model getPropertyModel() {
		return mr3Generator.getPropertyModel();
	}

	public Model getSelectedPropertyModel() {
		return mr3Generator.getSelectedPropertyModel();
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
