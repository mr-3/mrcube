package mr3.data;
import java.io.*;
import java.util.*;

import com.hp.hpl.mesa.rdf.jena.common.*;
import com.hp.hpl.mesa.rdf.jena.mem.*;
import com.hp.hpl.mesa.rdf.jena.model.*;
import com.hp.hpl.mesa.rdf.jena.vocabulary.*;

public abstract class RDFSInfo implements Serializable {

	//	transient protected Resource uri;
	protected String uri;
	protected URIType uriType;
//	transient private Literal label; // 現在，選択されているラベル
	private MR3Literal label; // 現在，選択されているラベル
	private List labelList;
//	transient private Literal comment; // 現在，選択されているコメント
	private MR3Literal comment; // 現在，選択されているコメント
	private List commentList;
	private String isDefinedBy;
	transient protected Model model;
	transient protected Set supRDFS;
	transient protected RDFSInfoMap rdfsInfoMap = RDFSInfoMap.getInstance();

	RDFSInfo(String uri, URIType type) {
		//			this.uri = new ResourceImpl(uri);
		this.uri = uri;
		uriType = type;
		labelList = new ArrayList();
		commentList = new ArrayList();
//		isDefinedBy = new ResourceImpl("");
		isDefinedBy = "";
		model = new ModelMem();
		supRDFS = new HashSet();
	}

	RDFSInfo(RDFSInfo info) {
		uri = info.getURIStr();
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

	public URIType getURIType() {
		return uriType;
	}
	
	public void setURIType(URIType type) {
		uriType = type;		
	}
	
	public Resource getURI() {
		return new ResourceImpl(uri);
	}

	public String getURIStr() {
		return uri;
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
