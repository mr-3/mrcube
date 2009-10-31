/*
 * Created on 2003/07/19
 *
 */
package mr3.actions;

import java.awt.event.*;
import java.io.*;

import mr3.*;

import com.hp.hpl.mesa.rdf.jena.common.*;
import com.hp.hpl.mesa.rdf.jena.model.*;

/**
 * @author takeshi morita
 *
 */
public class ConvertNTriple extends AbstractActionFile {

	public ConvertNTriple(MR3 mr3, String name) {
		super(mr3, name);
	}

	public void convertNTripleSRC(boolean isSelected) {
		Model model = null;
		if (isSelected) {
			model = mr3.getSelectedRDFModel();
		} else {
			model = mr3.getRDFModel();
		}
		Writer output = new StringWriter();
		RDFWriter writer = new NTripleWriter();
		writeModel(model, output, writer);
		mr3.getSourceArea().setText(output.toString());
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals("RDF/N-Triple")) {
			convertNTripleSRC(false);
		} else {
			convertNTripleSRC(true);
		}

		showSrcView();
	}
}