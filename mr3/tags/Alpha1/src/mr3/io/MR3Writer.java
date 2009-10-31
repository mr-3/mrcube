/*
 * Created on 2003/03/27
 *
  */
package mr3.io;

import mr3.jgraph.*;
import mr3.util.*;

import com.hp.hpl.mesa.rdf.jena.mem.*;
import com.hp.hpl.mesa.rdf.jena.model.*;

/**
 * @author cs9088
 */
public class MR3Writer {

	JGraphToRDF graphToRDF;

	public MR3Writer(GraphManager manager) {
		graphToRDF = new JGraphToRDF(manager);
	}

	public Model getRDFModel() {
		return graphToRDF.getRDFModel();
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

	public Model getClassModel() {
		try {
			Model model = graphToRDF.getClassModel();
			return model;
		} catch (RDFException e) {
			e.printStackTrace();
		}
		return null;
	}

	public Model getPropertyModel() {
		try {
			Model model = graphToRDF.getPropertyModel();
			return model;
		} catch (RDFException e) {
			e.printStackTrace();
		}
		return null;
	}

}
