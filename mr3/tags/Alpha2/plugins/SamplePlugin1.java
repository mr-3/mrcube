import java.io.*;

import mr3.plugin.*;

import com.hp.hpl.mesa.rdf.jena.common.*;
import com.hp.hpl.mesa.rdf.jena.mem.*;
import com.hp.hpl.mesa.rdf.jena.model.*;

/**
 * Created on 2003/03/24
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
			Property sampleProperty = new PropertyImpl("http://mrcube.sample.property");
			Literal sampleLiteral = new LiteralImpl("Sample");
			Statement stmt = sampleModel.createStatement(sampleResource, sampleProperty, sampleLiteral);
			sampleModel.add(stmt);
		} catch (RDFException e) {
			e.printStackTrace();
		}
		replaceRDFModel(sampleModel);
	}
}
