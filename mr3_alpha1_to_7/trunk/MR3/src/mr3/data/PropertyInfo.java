package mr3.data;
import java.util.*;

import com.hp.hpl.mesa.rdf.jena.model.*;
import com.hp.hpl.mesa.rdf.jena.vocabulary.*;

/**
 * @author user
 *
 */
public class PropertyInfo extends RDFSInfo {

	private Set domain;
	private Set range;
	private Set subProperties;
	private Set supProperties;

	public PropertyInfo(String uri) {
		super(uri);
		domain = new HashSet();
		range = new HashSet();
		subProperties = new HashSet();
		supProperties = new HashSet();
	}

	public Set getRDFSSubList() {
		return Collections.unmodifiableSet(subProperties);
	}

	public Model getModel() {
		try {
			Model tmpModel = super.getModel();

			tmpModel.add(tmpModel.createStatement(uri, RDF.type, RDF.Property));
			for (Iterator i = supRDFS.iterator(); i.hasNext();) {
				RDFSInfo propInfo = rdfsInfoMap.getCellInfo(i.next());
				if (!propInfo.getURI().equals(MR3Resource.Property)) // MRCUBE.Propertyは，ルートになっているだけなので．
					tmpModel.add(tmpModel.createStatement(uri, RDFS.subPropertyOf, propInfo.getURI()));
			}
			for (Iterator i = domain.iterator(); i.hasNext();) {
				RDFSInfo classInfo = rdfsInfoMap.getCellInfo(i.next());
				tmpModel.add(tmpModel.createStatement(uri, RDFS.domain, classInfo.getURI()));
			}
			for (Iterator i = range.iterator(); i.hasNext();) {
				RDFSInfo classInfo = rdfsInfoMap.getCellInfo(i.next());
				tmpModel.add(tmpModel.createStatement(uri, RDFS.range, classInfo.getURI()));
			}
			return tmpModel;
		} catch (RDFException e) {
			e.printStackTrace();
		}
		return model;
	}

	public void addDomain(Object resource) {
		domain.add(resource);
	}

	public void addAllDomain(Set set) {
		domain.addAll(set);
	}

	public void removeDomain(Object obj) {
		domain.remove(obj);
	}

	public Set getDomain() {
		return Collections.unmodifiableSet(domain);
	}

	public void addRange(Object resource) {
		range.add(resource);
	}

	public void addAllRange(Set set) {
		range.addAll(set);
	}

	public void removeRange(Object obj) {
		range.remove(obj);
	}

	public Set getRange() {
		return Collections.unmodifiableSet(range);
	}

	public void addSubProperty(Object resource) {
		subProperties.add(resource);
	}

	public void addSupProperty(Object resource) {
		supProperties.add(resource);
	}

	public Set getSupProperties() {
		return Collections.unmodifiableSet(supProperties);
	}

	public String toString() {
		String msg = super.toString();

		if (domain.size() > 0) {
			msg += "domain: " + domain.toString() + "\n";
		}
		if (range.size() > 0) {
			msg += "range: " + range.toString() + "\n";
		}
		if (subProperties.size() > 0) {
			msg += "SubProperty: " + subProperties.toString() + "\n";
		}
		if (supProperties.size() > 0) {
			msg += "SuperProperty: " + supProperties.toString() + "\n";
		}
		msg += getModelString();
		return msg;
	}
}
