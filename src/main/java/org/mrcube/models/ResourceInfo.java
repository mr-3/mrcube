/*
 * Project Name: MR^3 (Meta-Model Management based on RDFs Revision Reflection)
 * Project Website: http://mrcube.org/
 * 
 * Copyright (C) 2003-2018 Yamaguchi Laboratory, Keio University. All rights reserved.
 * 
 * This file is part of MR^3.
 * 
 * MR^3 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * MR^3 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with MR^3.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */

package org.mrcube.models;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDFS;

import java.util.Collections;
import java.util.List;

/**
 * @author Takeshi Morita
 */
public abstract class ResourceInfo {

	protected transient List<MR3Literal> labelList;
	protected transient List<MR3Literal> commentList;

	public void setLabelList(List<MR3Literal> labelList) {
		this.labelList = labelList;
	}

	public void addLabel(MR3Literal literal) {
		labelList.add(literal);
	}

	public MR3Literal getFirstLabel() {
		if (0 < labelList.size()) {
			return labelList.get(0);
		}
		return null;
	}

	public List<MR3Literal> getLabelList() {
		return Collections.unmodifiableList(labelList);
	}

	public MR3Literal getDefaultLabel(String defaultLang) {
		for (MR3Literal literal : labelList) {
			if (literal.getLanguage().equals(defaultLang)) {
				return literal;
			}
		}
		return null;
	}

	public void addComment(MR3Literal literal) {
		commentList.add(literal);
	}

	public List<MR3Literal> getCommentList() {
		return Collections.unmodifiableList(commentList);
	}

	public MR3Literal getFirstComment() {
		if (0 < commentList.size()) {
			return commentList.get(0);
		}
		return null;
	}

	public MR3Literal getDefaultComment(String defaultLang) {
		for (MR3Literal literal : commentList) {
			if (literal.getLanguage().equals(defaultLang)) {
				return literal;
			}
		}
		return null;
	}

	public void setCommentList(List<MR3Literal> commentList) {
		this.commentList = commentList;
	}

	public Model getModel(Resource res) {
		Model tmpModel = ModelFactory.createDefaultModel();
		for (MR3Literal literal : labelList) {
			tmpModel.add(tmpModel.createStatement(res, RDFS.label, literal.getLiteral()));
		}
		for (MR3Literal literal : commentList) {
			tmpModel.add(tmpModel.createStatement(res, RDFS.comment, literal.getLiteral()));
		}
		return tmpModel;
	}

}
