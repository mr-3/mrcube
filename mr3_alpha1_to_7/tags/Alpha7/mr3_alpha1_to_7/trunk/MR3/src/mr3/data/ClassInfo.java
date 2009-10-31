package mr3.data;
import java.util.*;

import com.hp.hpl.mesa.rdf.jena.model.*;
import com.hp.hpl.mesa.rdf.jena.vocabulary.*;

/**
 * @author takeshi morita
 *
 */
public class ClassInfo extends RDFSInfo {

	private static final long serialVersionUID = -3455137904632666118L;

	private transient Set subClasses;
	private transient Set supClasses;

	public ClassInfo(String uri) {
		super(uri);
		subClasses = new HashSet();
		supClasses = new HashSet();
	}

	public ClassInfo(ClassInfo info) {
		super(info);
		subClasses = new HashSet();
		supClasses = new HashSet();
	}

	public Set getRDFSSubList() {
		return Collections.unmodifiableSet(subClasses);
	}

	public Model getModel() {
		try {
			Model tmpModel = super.getModel();
			Resource res = getURI();

			tmpModel.add(tmpModel.createStatement(res, RDF.type, RDFS.Class));
			for (Iterator i = supRDFS.iterator(); i.hasNext();) {
				RDFSInfo classInfo = rdfsInfoMap.getCellInfo(i.next());
				tmpModel.add(tmpModel.createStatement(res, RDFS.subClassOf, classInfo.getURI()));
			}
			return tmpModel;
		} catch (RDFException rdfex) {
			rdfex.printStackTrace();
		}
		return model;
	}

	public void addSubClass(Resource subClass) {
		subClasses.add(subClass);
	}

	public void addSupClass(Resource supClass) {
		supClasses.add(supClass);
	}

	public Set getSupClasses() {
		return Collections.unmodifiableSet(supClasses);
	}

	public String toString() {
		String msg = super.toString();

		if (subClasses.size() > 0) {
			msg += "SubClasses: " + subClasses.toString() + "\n";
		}
		if (supClasses.size() > 0) {
			msg += "SuperClasses: " + supClasses.toString() + "\n";
		}

		msg += getModelString();

		return msg;
	}
}
