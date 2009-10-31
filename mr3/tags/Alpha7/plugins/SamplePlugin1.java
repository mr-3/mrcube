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
			Resource sampleResource = new ResourceImpl(sampleURI+"sample_resource");
			Resource sampleResourceType = new ResourceImpl(sampleURI+"sample_resourceType");
			Property sampleProperty = new PropertyImpl(sampleURI+"sample_property");
            Literal sampleLiteral = new LiteralImpl("Sample");
			Statement stmt = sampleModel.createStatement(sampleResource, sampleProperty, sampleLiteral);
			sampleModel.add(stmt);
			stmt = sampleModel.createStatement(sampleResource, RDF.type, sampleResourceType);
			sampleModel.add(stmt);
		} catch (RDFException e) {
			e.printStackTrace();
		}
		replaceRDFModel(sampleModel);
	}
	
}
