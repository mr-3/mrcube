package jp.ac.shizuoka.cs.panda.mmm.mr3.sample;
import mr3.plugin.*;

import com.hp.hpl.mesa.rdf.jena.common.*;
import com.hp.hpl.mesa.rdf.jena.mem.*;
import com.hp.hpl.mesa.rdf.jena.model.*;
import com.hp.hpl.mesa.rdf.jena.vocabulary.*;

/**
 * 
 * @author Takeshi Morita
 * replace RDF Model Sample
 */
public class SamplePlugin1 extends MR3Plugin {

	public void exec() {
		ModelMem sampleModel = new ModelMem();
		try {
			String sampleURI = "http://panda.cs.inf.shizuoka.ac.jp/mmm/mr3#";
			Resource sampleSubject = new ResourceImpl(sampleURI+"sample_subject");		
			Property sampleProperty = new PropertyImpl(sampleURI+"sample_property");
            Literal sampleLiteral = new LiteralImpl("sample_literal");
			Statement stmt = sampleModel.createStatement(sampleSubject, sampleProperty, sampleLiteral);
			sampleModel.add(stmt);
			Resource sampleSubjectType = new ResourceImpl(sampleURI+"sample_subjectType");
			stmt = sampleModel.createStatement(sampleSubject, RDF.type, sampleSubjectType);
			sampleModel.add(stmt);
		} catch (RDFException e) {
			e.printStackTrace();
		}
		replaceRDFModel(sampleModel);
	}
	
}
