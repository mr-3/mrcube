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

	public SamplePlugin1() {
		super("Sample Plugin 1");
	}

	public void exec() {
		ModelMem sampleModel = new ModelMem();
		try {
			Resource sampleResource = new ResourceImpl("http://mrcube.sample.resource");
			Resource sampleResourceType = new ResourceImpl("http://mrcube.sample.resourceType");
			Property sampleProperty = new PropertyImpl("http://mrcube.sample.property");
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
