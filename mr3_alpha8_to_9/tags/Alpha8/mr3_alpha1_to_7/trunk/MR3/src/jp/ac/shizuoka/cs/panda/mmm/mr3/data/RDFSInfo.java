package jp.ac.shizuoka.cs.panda.mmm.mr3.data;
import java.io.*;
import java.util.*;

import com.hp.hpl.mesa.rdf.jena.common.*;
import com.hp.hpl.mesa.rdf.jena.mem.*;
import com.hp.hpl.mesa.rdf.jena.model.*;
import com.hp.hpl.mesa.rdf.jena.vocabulary.*;

public abstract class RDFSInfo implements Serializable {

	private static final long serialVersionUID = -2970145279588775430L;

	protected transient String uri;
	protected transient String metaClass;
	private transient MR3Literal label; // 現在，選択されているラベル
	private transient List labelList;
	private transient MR3Literal comment; // 現在，選択されているコメント
	private transient List commentList;
	private transient String isDefinedBy;

	transient protected Model model;
	transient protected Set supRDFS;
	transient protected RDFSInfoMap rdfsInfoMap = RDFSInfoMap.getInstance();

	RDFSInfo(String uri) {
		this.uri = uri;
		labelList = new ArrayList();
		commentList = new ArrayList();
		isDefinedBy = "";
		model = new ModelMem();
		supRDFS = new HashSet();
	}

	RDFSInfo(RDFSInfo info) {
		uri = info.getURIStr();
		metaClass = info.getMetaClass();
		label = info.getLabel();
		labelList = new ArrayList(info.getLabelList());
		comment = info.getComment();
		commentList = new ArrayList(info.getCommentList());
		isDefinedBy = info.getIsDefinedBy().getURI();
		model = new ModelMem();
		supRDFS = new HashSet();
	}

	RDFSInfo() {
	}

	public abstract Set getRDFSSubList();

	/** uriが等しければ，等しいとする */
	public boolean equals(Object o) {
		String uriStr = (String) o;
		return uriStr.equals(uri);
	}

	public void addStatement(Statement stmt) {
		try {
			model.add(stmt);
		} catch (RDFException e) {
			e.printStackTrace();
		}
	}

	public Model getInnerModel() {
		return model;
	}

	public void setInnerModel(Model m) {
		model = m;
	}

	public Model getModel() throws RDFException {
		Model tmpModel = new ModelMem();

		tmpModel.add(model);
		Resource res = new ResourceImpl(uri);
		if (isDefinedBy.length() != 0) {
			tmpModel.add(tmpModel.createStatement(res, RDFS.isDefinedBy, new ResourceImpl(isDefinedBy)));
		}

		for (Iterator i = labelList.iterator(); i.hasNext();) {
			MR3Literal literal = (MR3Literal) i.next();
			tmpModel.add(tmpModel.createStatement(res, RDFS.label, literal.getLiteral()));
		}

		for (Iterator i = commentList.iterator(); i.hasNext();) {
			MR3Literal literal = (MR3Literal) i.next();
			tmpModel.add(tmpModel.createStatement(res, RDFS.comment, literal.getLiteral()));
		}

		return tmpModel;
	}

	public void setMetaClass(String metaClass) {
		this.metaClass = metaClass;
	}

	public String getMetaClass() {
		return metaClass;
	}

	public void setSupRDFS(Set set) {
		supRDFS.clear();
		supRDFS.addAll(set);
	}

	public void addSupRDFS(Object rdfs) {
		supRDFS.add(rdfs);
	}

	public Set getSupRDFS() {
		return Collections.unmodifiableSet(supRDFS);
	}

	public void setURI(String str) {
		uri = str;
	}

	public Resource getURI() {
		return new ResourceImpl(uri);
	}

	public String getURIStr() {
		return uri;
	}

	public String getNameSpace() {
		Resource tmp = new ResourceImpl(uri);
		return tmp.getNameSpace();
	}

	public String getLocalName() {
		Resource tmp = new ResourceImpl(uri);
		return tmp.getLocalName();
	}

	public void setLabel(MR3Literal label) {
		this.label = label;
	}

	public MR3Literal getLabel() {
		return label;
	}

	public void addLabel(MR3Literal literal) {
		labelList.add(literal);
	}

	public void removeLabel(MR3Literal literal) {
		labelList.remove(literal);
	}

	public List getLabelList() {
		return Collections.unmodifiableList(labelList);
	}

	public void addComment(MR3Literal literal) {
		commentList.add(literal);
	}

	public void removeComment(MR3Literal literal) {
		commentList.remove(literal);
	}

	public List getCommentList() {
		return Collections.unmodifiableList(commentList);
	}

	public void setComment(MR3Literal comment) {
		this.comment = comment;
	}

	public MR3Literal getComment() {
		return comment;
	}

	public void setIsDefinedby(String str) {
		isDefinedBy = str;
	}

	public Resource getIsDefinedBy() {
		return new ResourceImpl(isDefinedBy);
	}

	public String getModelString() {
		StringWriter writer = new StringWriter();
		try {
			getModel().write(writer);
		} catch (RDFException e) {
			e.printStackTrace();
		}

		return writer.toString();
	}

	private void writeObject(ObjectOutputStream s) throws IOException {
		s.defaultWriteObject();
		s.writeObject(uri);
		s.writeObject(metaClass);
		s.writeObject(label);
		s.writeObject(labelList);
		s.writeObject(comment);
		s.writeObject(commentList);
		s.writeObject(isDefinedBy);
	}

	private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {
		s.defaultReadObject();
		uri = (String) s.readObject();
		metaClass = (String) s.readObject();
		label = (MR3Literal) s.readObject();
		labelList = (List) s.readObject();
		comment = (MR3Literal) s.readObject();
		commentList = (List) s.readObject();
		isDefinedBy = (String) s.readObject();
	}

	public String toString() {
		String msg = "";
		if (uri != null) {
			msg += "URI: " + uri + "\n";
		}
		msg += "MetaClass: "+metaClass+"\n";
		msg += "Label: " + label + "\n" + "Comment: " + comment + "\n";
		if (isDefinedBy != null) {
			msg += "isDefinedBy: " + isDefinedBy.toString() + "\n";
		}
		return msg;
	}
}
