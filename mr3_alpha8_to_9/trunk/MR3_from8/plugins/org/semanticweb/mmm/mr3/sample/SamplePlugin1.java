package org.semanticweb.mmm.mr3.sample;
import org.semanticweb.mmm.mr3.plugin.*;

import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.vocabulary.*;

/**
 * 
 * @author Takeshi Morita
 * replace RDF Model Sample
 */
public class SamplePlugin1 extends MR3Plugin {

	public void exec() {
		Model sampleModel = ModelFactory.createDefaultModel();
		try {
			String sampleURI = "http://mmm.semanticweb.org/mr3#";
			Resource sampleSubject = ResourceFactory.createResource(sampleURI+"sample_subject");		
			Property sampleProperty = ResourceFactory.createProperty(sampleURI+"sample_property");
            Literal sampleLiteral = sampleModel.createLiteral("sample_literal");
			Statement stmt = sampleModel.createStatement(sampleSubject, sampleProperty, sampleLiteral);
			sampleModel.add(stmt);
			Resource sampleSubjectType = ResourceFactory.createResource(sampleURI+"sample_subjectType");
			stmt = sampleModel.createStatement(sampleSubject, RDF.type, sampleSubjectType);
			sampleModel.add(stmt);
		} catch (RDFException e) {
			e.printStackTrace();
		}
		replaceRDFModel(sampleModel);
	}
	
}
