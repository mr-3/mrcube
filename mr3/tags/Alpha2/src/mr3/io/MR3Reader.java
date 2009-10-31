/*
 * Created on 2003/03/24
 *
 */
package mr3.io;

import mr3.data.*;
import mr3.jgraph.*;
import mr3.util.*;

import com.hp.hpl.mesa.rdf.jena.mem.*;
import com.hp.hpl.mesa.rdf.jena.model.*;

/**
 * @author cs9088
 */
public class MR3Reader {

	private GraphManager gmanager;
	private RDFToJGraph rdfToGraph;
	private JGraphToRDF graphToRDF;
	private RDFSInfoMap rdfsInfoMap = RDFSInfoMap.getInstance();

	/*
	 *  RDFReader -> JenaReader or N3JenaReader
	 */
	public MR3Reader(GraphManager manager) {
		gmanager = manager;
		rdfToGraph = new RDFToJGraph(manager);
		graphToRDF = new JGraphToRDF(manager);
	}
	
	private void replaceGraph(RDFGraph newGraph) {
		gmanager.getRDFGraph().setRDFState(newGraph.getRDFState());
	}

	public void replaceRDFModel(Model model) {
		gmanager.getRDFGraph().removeAllCells();
		RDFGraph newGraph = rdfToGraph.convertRDFToJGraph(model);
		replaceGraph(newGraph);
	}

	public void mergeRDFModel(Model newModel) {
		try {
			Model model = graphToRDF.getRDFModel();
			model.add(newModel);
			gmanager.getRDFGraph().removeAllCells();
			RDFGraph newGraph = rdfToGraph.convertRDFToJGraph(model);
			replaceGraph(newGraph);
		} catch (RDFException e) {
			e.printStackTrace();
		}
	}

	public void mergeRDFSModel(Model model) {
		mergePropertyModel(model);
		mergeClassModel(model);
	}

	private void mergeClassModel(Model model) {
		try {
			model.add(graphToRDF.getClassModel());
			rdfToGraph.createClassGraph(model);
			rdfsInfoMap.setClassTreeModel();
			rdfsInfoMap.clearTemporaryMap();
		} catch (RDFException e) {
			e.printStackTrace();
		}
	}

	private void mergePropertyModel(Model model) {
		try {
			model.add(graphToRDF.getPropertyModel());
			rdfToGraph.createPropertyGraph(model);
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
			gmanager.applyTreeLayout();
		}
	}

	public void mergeRDF(Model model) {
		if (model != null) {
			mergeRDFModel(model);
			mergeRDFSModel(new ModelMem()); // RDFからRDFSへ反映されたクラス，プロパティの処理
			gmanager.applyTreeLayout();
		}
	}

	public void mergeRDFS(Model model) {
		if (model != null) {
			mergeRDFSModel(model);
			mergeRDF(model); // RDFSにRDFが含まれていた場合の処理(mergeRDFModel()ではない)
			gmanager.applyTreeLayout();
		}
	}
}
