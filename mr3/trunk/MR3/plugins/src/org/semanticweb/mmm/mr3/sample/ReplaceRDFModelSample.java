/*
 * @(#) SamplePlugin1.java
 * 
 * Copyright (C) 2003 The MMM Project
 * 
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation; either version 2.1 of the License, or (at your
 * option) any later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *  
 */

package org.semanticweb.mmm.mr3.sample;

import org.semanticweb.mmm.mr3.plugin.*;

import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.vocabulary.*;

/**
 * @author Takeshi Morita replace RDF Model Sample
 */
public class ReplaceRDFModelSample extends MR3Plugin {

	public void exec() {
		Model sampleModel = ModelFactory.createDefaultModel();
		try {
			String sampleURI = "http://mmm.semanticweb.org/mr3#";
			Resource sampleSubject = ResourceFactory.createResource(sampleURI + "sample_subject");
			Property sampleProperty = ResourceFactory.createProperty(sampleURI + "sample_property");
			Literal sampleLiteral = sampleModel.createLiteral("sample_literal");
			Statement stmt = sampleModel.createStatement(sampleSubject, sampleProperty, sampleLiteral);
			sampleModel.add(stmt);
			Resource sampleSubjectType = ResourceFactory.createResource(sampleURI + "sample_subjectType");
			stmt = sampleModel.createStatement(sampleSubject, RDF.type, sampleSubjectType);
			sampleModel.add(stmt);
		} catch (RDFException e) {
			e.printStackTrace();
		}
		replaceRDFModel(sampleModel);
	}

}
