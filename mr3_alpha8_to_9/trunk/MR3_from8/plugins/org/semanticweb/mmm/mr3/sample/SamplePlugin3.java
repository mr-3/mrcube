package org.semanticweb.mmm.mr3.sample;
import java.io.*;

import javax.swing.*;

import org.semanticweb.mmm.mr3.plugin.*;

import com.hp.hpl.jena.rdf.arp.*;
import com.hp.hpl.jena.rdf.model.*;

/**
 * @author takeshi morita
 * 
 * ProjectFileÇì«Ç›çûÇﬁÉvÉâÉOÉCÉì
 */
public class SamplePlugin3 extends MR3Plugin {

	public void exec() {
		JFileChooser jfc = new JFileChooser();
		if (jfc.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
			File file = jfc.getSelectedFile();
			try {
				Model model = ModelFactory.createDefaultModel();
				RDFReader jenaReader = new JenaReader();
				Reader r = new InputStreamReader(new FileInputStream(file), "UTF8");
				jenaReader.read(model, r, getBaseURI());
				replaceProjectModel(model);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
}
