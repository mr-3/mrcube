package mr3.util;
import mr3.data.*;
import mr3.jgraph.*;

import com.hp.hpl.mesa.rdf.jena.common.*;
import com.hp.hpl.mesa.rdf.jena.mem.*;
import com.hp.hpl.mesa.rdf.jena.model.*;
import com.hp.hpl.mesa.rdf.jena.vocabulary.*;

public class RDFSModelExtraction {

	private GraphManager gmanager;
	private RDFSInfoMap rdfsInfoMap = RDFSInfoMap.getInstance();

	RDFSModelExtraction(GraphManager manager) {
		gmanager = manager;
	}

	public Model extractClassModel(Model orgModel) throws RDFException {
		Model classModel = new ModelMem();

		addInnerClassModel(orgModel);
		for (ResIterator i = orgModel.listSubjectsWithProperty(RDF.type, RDFS.Class); i.hasNext();) {
			Resource classResource = i.next();
			ClassInfo info = getClassResInfo(classResource);
			setDefaultSubClassOf(classResource, info);
			extractRDFSModel(orgModel, classModel, classResource, info);
		}		
		return classModel;
	}

	// subPropertyOfが省略されたプロパティは，Propertyクラスのサブクラスとみなす、
	private void setDefaultSubPropertyOf(Resource property) throws RDFException {
		if (!property.hasProperty(RDFS.subPropertyOf)) {
			rdfsInfoMap.addRootProperties(property);
		}
	}

	// subClassOfで指定されているクラスをオリジナルに追加．（定義されていないかもしれないので）
	private void addInnerClassModel(Model orgModel) throws RDFException {
		Model tmpModel = new ModelMem();
		for (ResIterator i = orgModel.listSubjectsWithProperty(RDF.type, RDFS.Class); i.hasNext();) {
			Resource property = i.next();
			for (StmtIterator j = property.listProperties(); j.hasNext();) {
				Statement stmt = j.next();
				if (stmt.getPredicate().equals(RDFS.subClassOf)) {
					tmpModel.add(new StatementImpl((Resource) stmt.getObject(), RDF.type, RDFS.Class));
				}
			}
		}
		orgModel.add(tmpModel);
	}

	// subPropertyOfで指定されているプロパティとdomain, rangeで指定されているクラスをオリジナルに追加
	// 先にプロパティを読み込むことで，domain, rangeに指定されたクラスを処理できる
	private void addInnerPropertyModel(Model orgModel) throws RDFException {
		Model tmpModel = new ModelMem();
		for (ResIterator i = orgModel.listSubjectsWithProperty(RDF.type, RDF.Property); i.hasNext();) {
			Resource property = i.next();
			for (StmtIterator j = property.listProperties(); j.hasNext();) {
				Statement stmt = j.next();
				if (stmt.getPredicate().equals(RDFS.domain) || stmt.getPredicate().equals(RDFS.range)) {
					tmpModel.add(new StatementImpl((Resource) stmt.getObject(), RDF.type, RDFS.Class));
				} else if (stmt.getPredicate().equals(RDFS.subPropertyOf)) {
					tmpModel.add(new StatementImpl((Resource) stmt.getObject(), RDF.type, RDF.Property));
				}
			}
		}
		orgModel.add(tmpModel);
	}

	public Model extractPropertyModel(Model orgModel) throws RDFException {
		Model propertyModel = new ModelMem();

		addInnerPropertyModel(orgModel);
		for (ResIterator i = orgModel.listSubjectsWithProperty(RDF.type, RDF.Property); i.hasNext();) {
			Resource property = i.next();
			PropertyInfo info = getPropertyResInfo(property);
			setDefaultSubPropertyOf(property);
			extractRDFSModel(orgModel, propertyModel, property, info);
		}
		return propertyModel;
	}

	public void extractRDFSModel(Model orgModel, Model rdfsModel, Resource metaResource, RDFSInfo info) throws RDFException {
		for (StmtIterator i = metaResource.listProperties(); i.hasNext();) {
			Statement stmt = i.next();
			if (setRDFSInfo(stmt, info)) {
				rdfsModel.add(stmt);
			} else {
				info.addStatement(stmt); // RDFS以外のプロパティを持つ文を保存				
			}
			orgModel.remove(stmt);
		}

		info.setURI(metaResource.toString());
		rdfsInfoMap.putResourceInfo(metaResource, info);
	}

	// Resourceクラス以外のsubClassOfが省略されたクラスは，
	// Resourceクラスのサブクラスとみなす
	private void setDefaultSubClassOf(Resource rdfsResource, ClassInfo info) throws RDFException {
		if (!rdfsResource.equals(RDFS.Resource) && !rdfsResource.hasProperty(RDFS.subClassOf)) {
			ClassInfo supResInfo = getClassResInfo(RDFS.Resource);
			supResInfo.addSubClass(rdfsResource);
			info.addSupClass(RDFS.Resource);
			rdfsInfoMap.putResourceInfo(RDFS.Resource, supResInfo);
		}
	}

	private boolean setClassInfo(Statement stmt, ClassInfo info) {
		Resource subject = stmt.getSubject(); // get the subject
		Property predicate = stmt.getPredicate(); // get the predicate
		RDFNode object = stmt.getObject(); // get the object

		if (setBaseInfo(stmt, info)) {
			return true;
		}

		if (predicate.equals(RDFS.subClassOf)) { //rdfs:subClassOf
			// subject < object
			// info -> subject info supInfo -> object info
			ClassInfo supResInfo = getClassResInfo((Resource) object);
			supResInfo.addSubClass(subject);
			info.addSupClass((Resource) object);
			rdfsInfoMap.putResourceInfo((Resource) object, supResInfo);
		} else {
			return false;
		}

		return true;
	}

	private boolean setPropertyInfo(Statement stmt, PropertyInfo info) {
		Resource subject = stmt.getSubject(); // get the subject
		Property predicate = stmt.getPredicate(); // get the predicate
		RDFNode object = stmt.getObject(); // get the object

		if (setBaseInfo(stmt, info)) {
			return true;
		}

		if (predicate.equals(RDFS.domain)) { // rdfs:domain
			Object cell = gmanager.getClassCell((Resource) object, false);
			info.addDomain(cell);
		} else if (predicate.equals(RDFS.range)) { // rdfs:range
			Object cell = gmanager.getClassCell((Resource) object, false);
			info.addRange(cell);
		} else if (predicate.equals(RDFS.subPropertyOf)) { //rdfs:subPropertyOf
			// subject < object
			// info -> subject info supInfo -> object info
			PropertyInfo supResInfo = getPropertyResInfo((Resource) object);
			//			rdfsInfoMap.addRootProperties((Resource)object); //一時しのぎ
			supResInfo.addSubProperty(subject);
			rdfsInfoMap.putResourceInfo((Resource) object, supResInfo);
			info.addSupProperty((Resource) object);
		} else {
			return false;
		}

		return true;
	}

	private boolean setBaseInfo(Statement stmt, RDFSInfo info) {
		Property predicate = stmt.getPredicate(); // get the predicate
		RDFNode object = stmt.getObject(); // get the object

		if (predicate.equals(RDFS.label)) { //rdfs:label
//			Literal literal = (Literal) object;
			MR3Literal literal = new MR3Literal((Literal)object);
			info.setLabel(literal);
			info.addLabel(literal);
		} else if (predicate.equals(RDFS.comment)) { //rdfs:comment
//			Literal literal = (Literal) object;
			MR3Literal literal = new MR3Literal((Literal)object);
			info.setComment(literal);
			info.addComment(literal);
		} else if (predicate.equals(RDFS.isDefinedBy)) { //rdfs:isDefinedBy
			info.setIsDefinedby(object.toString());
		} else {
			return false;
		}
		return true;
	}

	private boolean setRDFSInfo(Statement stmt, RDFSInfo info) {
		if (info instanceof ClassInfo) {
			return setClassInfo(stmt, (ClassInfo) info);
		} else if (info instanceof PropertyInfo) {
			return setPropertyInfo(stmt, (PropertyInfo) info);
		} else {
			return false;
		}
	}

	private ClassInfo getClassResInfo(Resource resource) {
		ClassInfo info = (ClassInfo) rdfsInfoMap.getResourceInfo(resource);
		if (info == null)
			info = new ClassInfo("");
		return info;
	}

	private PropertyInfo getPropertyResInfo(Resource resource) {
		PropertyInfo info = (PropertyInfo) rdfsInfoMap.getResourceInfo(resource);
		if (info == null)
			info = new PropertyInfo("");
		return info;
	}
}
