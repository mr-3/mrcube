/*
 * @(#) RDFSInfo.java
 *
 *
 * Copyright (C) 2003 The MMM Project
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 */

package org.semanticweb.mmm.mr3.data;

import java.io.*;
import java.util.*;

import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.vocabulary.*;

/**
 *
 *  RDFSの情報を格納する 
 *  
 * @author takeshi morita
 *
 */
public abstract class RDFSInfo implements Serializable {

	private static final long serialVersionUID = -2970145279588775430L;

	protected transient String uri;
	protected transient String metaClass;
	private transient MR3Literal lastSelectedLabel; // 前回選択されていたラベル
	private transient List labelList;
	private transient MR3Literal lastSelectedComment; // 前回選択されていたコメント
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
		model = ModelFactory.createDefaultModel();
		supRDFS = new HashSet();
	}

	RDFSInfo(RDFSInfo info) {
		uri = info.getURIStr();
		metaClass = info.getMetaClass();
		lastSelectedLabel = info.getLastLabel();
		labelList = new ArrayList(info.getLabelList());
		lastSelectedComment = info.getLastComment();
		commentList = new ArrayList(info.getCommentList());
		isDefinedBy = info.getIsDefinedBy().getURI();
		model = ModelFactory.createDefaultModel();
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
		Model tmpModel = ModelFactory.createDefaultModel();

		tmpModel.add(model);
		Resource res = ResourceFactory.createResource(uri);
		if (isDefinedBy.length() != 0) {
			tmpModel.add(tmpModel.createStatement(res, RDFS.isDefinedBy, ResourceFactory.createResource(isDefinedBy)));
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
		return ResourceFactory.createResource(uri);
	}

	public String getURIStr() {
		return uri;
	}

	public String getNameSpace() {
		return ResourceFactory.createResource(uri).getNameSpace();
	}

	public String getLocalName() {
		return ResourceFactory.createResource(uri).getLocalName();
	}

	public void setLastLabel(MR3Literal label) {
		this.lastSelectedLabel = label;
	}

	public MR3Literal getLastLabel() {
		return lastSelectedLabel;
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

	public MR3Literal getDefaultLabel(String defaultLang) {
		for (Iterator i = labelList.iterator(); i.hasNext();) {
			MR3Literal literal = (MR3Literal) i.next();
			if (literal.getLanguage().equals(defaultLang)) {
				return literal;
			}
		}
		return null;
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

	public MR3Literal getDefaultComment(String defaultLang) {
		for (Iterator i = commentList.iterator(); i.hasNext();) {
			MR3Literal literal = (MR3Literal) i.next();
			if (literal.getLanguage().equals(defaultLang)) {
				return literal;
			}
		}
		return null;
	}

	public void setLastComment(MR3Literal comment) {
		this.lastSelectedComment = comment;
	}

	public MR3Literal getLastComment() {
		return lastSelectedComment;
	}

	public void setIsDefinedby(String str) {
		isDefinedBy = str;
	}

	public Resource getIsDefinedBy() {
		return ResourceFactory.createResource(isDefinedBy);
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
		s.writeObject(lastSelectedLabel);
		s.writeObject(labelList);
		s.writeObject(lastSelectedComment);
		s.writeObject(commentList);
		s.writeObject(isDefinedBy);
	}

	private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {
		s.defaultReadObject();
		uri = (String) s.readObject();
		metaClass = (String) s.readObject();
		lastSelectedLabel = (MR3Literal) s.readObject();
		labelList = (List) s.readObject();
		lastSelectedComment = (MR3Literal) s.readObject();
		commentList = (List) s.readObject();
		isDefinedBy = (String) s.readObject();
	}

	public String toString() {
		String msg = "";
		if (uri != null) {
			msg += "URI: " + uri + "\n";
		}
		msg += "MetaClass: " + metaClass + "\n";
		msg += "Label: " + lastSelectedLabel + "\n" + "Comment: " + lastSelectedComment + "\n";
		if (isDefinedBy != null) {
			msg += "isDefinedBy: " + isDefinedBy.toString() + "\n";
		}
		return msg;
	}
}
