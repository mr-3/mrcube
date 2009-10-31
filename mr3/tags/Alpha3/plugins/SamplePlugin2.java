import java.io.*;

import mr3.plugin.*;

import com.hp.hpl.mesa.rdf.jena.model.*;
/**
 *
 * @auther takeshi morita
 *  print RDF Model
 */
public class SamplePlugin2 extends MR3Plugin {

	public SamplePlugin2() {
		super("Sample Plugin 2");
	}

	public void exec() {
		try {
			Model rdfModel = getRDFModel();
			rdfModel.write(new PrintWriter(System.out));
		} catch (RDFException e) {
			e.printStackTrace();	
		}
	}
}
