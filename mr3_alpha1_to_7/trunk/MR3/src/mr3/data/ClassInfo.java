package mr3.data;
import java.util.*;

import com.hp.hpl.mesa.rdf.jena.common.*;
import com.hp.hpl.mesa.rdf.jena.model.*;
import com.hp.hpl.mesa.rdf.jena.vocabulary.*;

/**
 * @author takeshi morita
 *
 */
public class ClassInfo extends RDFSInfo {
	transient private Set subClasses;
	transient private Set supClasses;

	public ClassInfo(String uri, URIType type) {
		super(uri, type);
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

	public Model getModel(String baseURI) {
		try {
			Model tmpModel = super.getModel();
			Resource res = null;
			if (uriType == URIType.URI) {
				res = new ResourceImpl(uri);
			} else {
				res = new ResourceImpl(baseURI + uri);
			}

			tmpModel.add(tmpModel.createStatement(res, RDF.type, RDFS.Class));
			for (Iterator i = supRDFS.iterator(); i.hasNext();) {
				RDFSInfo classInfo = rdfsInfoMap.getCellInfo(i.next());
				if (classInfo.getURIType() == URIType.URI) {
					tmpModel.add(tmpModel.createStatement(res, RDFS.subClassOf, classInfo.getURI()));
				} else {
					tmpModel.add(tmpModel.createStatement(res, RDFS.subClassOf, new ResourceImpl(baseURI + classInfo.getURIStr())));
				}
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
