package mr3.data;
import java.io.*;
import java.util.*;

import com.hp.hpl.mesa.rdf.jena.common.*;
import com.hp.hpl.mesa.rdf.jena.mem.*;
import com.hp.hpl.mesa.rdf.jena.model.*;
import com.hp.hpl.mesa.rdf.jena.vocabulary.*;

public abstract class RDFSInfo implements Serializable {

	transient protected Resource uri;
	transient private Literal label;        // 現在，選択されているラベル
	transient private List labelList;
	transient private Literal comment; // 現在，選択されているコメント
	transient private List commentList;
	transient private Resource isDefinedBy;
	transient protected Model model;
	transient protected Set supRDFS;
	transient protected RDFSInfoMap rdfsInfoMap = RDFSInfoMap.getInstance();

	RDFSInfo(String uri) {
		try {
			this.uri = new ResourceImpl(uri);
		} catch (Exception e) {
		}
		labelList = new ArrayList();
		commentList = new ArrayList();
		isDefinedBy = new ResourceImpl("");
		model = new ModelMem();
		supRDFS = new HashSet();
	}

	public abstract Set getRDFSSubList();

	/** uriが等しければ，等しいとする */
	public boolean equals(Object o) {
		String uriStr = (String) o;
		return uriStr.equals(uri.getURI());
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
		if (isDefinedBy.getURI().length() != 0) {
			tmpModel.add(tmpModel.createStatement(uri, RDFS.isDefinedBy, isDefinedBy));
		}

		for (Iterator i = labelList.iterator(); i.hasNext();) {
			Literal literal = (Literal) i.next();
			tmpModel.add(tmpModel.createStatement(uri, RDFS.label, literal));
		}

		for (Iterator i = commentList.iterator(); i.hasNext();) {
			Literal literal = (Literal) i.next();
			tmpModel.add(tmpModel.createStatement(uri, RDFS.comment, literal));
		}

		return tmpModel;
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
		uri = new ResourceImpl(str);
	}

	public Resource getURI() {
		return uri;
	}

	public String getURIStr() {
		return uri.getURI();
	}

	public void setLabel(Literal label) {
		this.label = label;
	}

	public Literal getLabel() {
		return label;
	}

	public void addLabel(Literal literal) {
		labelList.add(literal);
	}

	public void removeLabel(Literal literal) {
		labelList.remove(literal);
	}

	public List getLabelList() {
		return Collections.unmodifiableList(labelList);
	}

	public void addComment(Literal literal) {
		commentList.add(literal);
	}

	public void removeComment(Literal literal) {
		commentList.remove(literal);
	}

	public List getCommentList() {
		return Collections.unmodifiableList(commentList);
	}

	public void setComment(Literal comment) {
		this.comment = comment;
	}

	public Literal getComment() {
		return comment;
	}

	public void setIsDefinedby(String str) {
		isDefinedBy = new ResourceImpl(str);
	}

	public Resource getIsDefinedBy() {
		return isDefinedBy;
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

	public String toString() {
		String msg = "";

		if (uri != null)
			msg += "URI: " + uri + "\n";

		msg += "Label: " + label + "\n" + "Comment: " + comment + "\n";

		if (isDefinedBy != null) {
			msg += "isDefinedBy: " + isDefinedBy.toString() + "\n";
		}
		return msg;
	}
}
