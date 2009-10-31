/*
 * Created on 2003/03/24
 *
 */
package jp.ac.shizuoka.cs.panda.mmm.mr3.io;

import jp.ac.shizuoka.cs.panda.mmm.mr3.*;
import jp.ac.shizuoka.cs.panda.mmm.mr3.data.*;
import jp.ac.shizuoka.cs.panda.mmm.mr3.jgraph.*;
import jp.ac.shizuoka.cs.panda.mmm.mr3.ui.*;
import jp.ac.shizuoka.cs.panda.mmm.mr3.util.*;

import com.hp.hpl.jena.mem.*;
import com.hp.hpl.jena.rdf.model.*;

/**
 * @author takeshi morita
 */
public class MR3Reader {

	private GraphManager gmanager;
	private MR3Parser mr3Parser;
	private MR3Generator mr3Generator;
	private NameSpaceTableDialog nsTableDialog;
	private RDFSInfoMap rdfsInfoMap = RDFSInfoMap.getInstance();

	/*
	 *  RDFReader -> JenaReader or N3JenaReader
	 */
	public MR3Reader(GraphManager gm, NameSpaceTableDialog nsTableD) {
		gmanager = gm;
		nsTableDialog = nsTableD;
		mr3Parser = new MR3Parser(gmanager);
		mr3Generator = new MR3Generator(gmanager);
	}

	private void replaceGraph(RDFGraph newGraph) {
		gmanager.getRDFGraph().setRDFState(newGraph.getRDFState());
	}

	public void replaceRDFModel(Model model) {
		gmanager.getRDFGraph().removeAllCells();
		RDFGraph newGraph = mr3Parser.convertRDFToJGraph(model);
		replaceGraph(newGraph);
		nsTableDialog.setCurrentNSPrefix();
	}

	public void mergeRDFModel(Model newModel) {
		try {
			Model model = mr3Generator.getRDFModel();
			model.add(newModel);
			gmanager.getRDFGraph().removeAllCells();
			RDFGraph newGraph = mr3Parser.convertRDFToJGraph(model);
			replaceGraph(newGraph);
			nsTableDialog.setCurrentNSPrefix();
		} catch (RDFException e) {
			e.printStackTrace();
		}
	}

	public void mergeRDFSModel(Model model) {
		mergePropertyModel(model);
		mergeClassModel(model);
		nsTableDialog.setCurrentNSPrefix();
	}

	private void mergeClassModel(Model model) {
		try {
			model.add(mr3Generator.getClassModel());
			mr3Parser.createClassGraph(model);
			rdfsInfoMap.setClassTreeModel();
			rdfsInfoMap.clearTemporaryMap();
		} catch (RDFException e) {
			e.printStackTrace();
		}
	}

	private void mergePropertyModel(Model model) {
		try {
			model.add(mr3Generator.getPropertyModel());
			mr3Parser.createPropertyGraph(model);
			rdfsInfoMap.setPropTreeModel();
			rdfsInfoMap.clearTemporaryMap();
		} catch (RDFException e) {
			e.printStackTrace();
		}
	}

	public void replaceRDF(Model model) {
		if (model != null) {
			replaceRDFModel(model);
			mergeRDFSModel(new ModelMem()); // RDFからRDFSへ反映されたクラス，プロパティの処理
		}
	}

	public void mergeRDF(Model model) {
		if (model != null) {
			mergeRDFModel(model);
			mergeRDFSModel(new ModelMem()); // RDFからRDFSへ反映されたクラス，プロパティの処理
		}
	}

	public void mergeRDFS(Model model) {
		if (model != null) {
			mergeRDFSModel(model);
			mergeRDF(model); // RDFSにRDFが含まれていた場合の処理(mergeRDFModel()ではない)
		}
	}

	public void replaceProjectModel(Model model, MR3 mr3) {
		if (model != null) {
			if (model != null) {
				mr3.newProject();
				// 順番が重要
				ProjectManager projectManager = new ProjectManager(mr3.getGraphManager(), mr3.getNSTableDialog());
				Model projectModel = projectManager.extractProjectModel(model);
				mergeRDFS(model); // mergeRDFModelではない．まぎらわしい．
				projectManager.loadProject(projectModel);
				projectManager.removeEmptyClass();
				gmanager.removeTypeCells();
				gmanager.addTypeCells();
				nsTableDialog.changeCellView();
				gmanager.clearSelection();
			}
		}
	}
}
