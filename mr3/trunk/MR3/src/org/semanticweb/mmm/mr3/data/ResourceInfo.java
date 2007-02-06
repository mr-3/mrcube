/*
 * @(#)  2004/02/18
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

import java.util.*;

import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.vocabulary.*;

/**
 * @author takeshi morita
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
        if (0 < labelList.size()) { return labelList.get(0); }
        return null;
    }

    public List<MR3Literal> getLabelList() {
        return Collections.unmodifiableList(labelList);
    }

    public MR3Literal getDefaultLabel(String defaultLang) {
        for (Iterator i = labelList.iterator(); i.hasNext();) {
            MR3Literal literal = (MR3Literal) i.next();
            if (literal.getLanguage().equals(defaultLang)) { return literal; }
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
        if (0 < commentList.size()) { return commentList.get(0); }
        return null;
    }

    public MR3Literal getDefaultComment(String defaultLang) {
        for (Iterator i = commentList.iterator(); i.hasNext();) {
            MR3Literal literal = (MR3Literal) i.next();
            if (literal.getLanguage().equals(defaultLang)) { return literal; }
        }
        return null;
    }

    public void setCommentList(List<MR3Literal> commentList) {
        this.commentList = commentList;
    }

    public Model getModel(Resource res) throws RDFException {
        Model tmpModel = ModelFactory.createDefaultModel();
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

}
