/*
 * Created on 2003/07/19
 *
 */
package jp.ac.shizuoka.cs.panda.mmm.mr3.actions;

import java.awt.event.*;
import java.io.*;

import jp.ac.shizuoka.cs.panda.mmm.mr3.*;
import jp.ac.shizuoka.cs.panda.mmm.mr3.util.*;

import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.rdf.model.impl.*;

/**
 * @author takeshi morita
 *
 */
public class ConvertNTriple extends AbstractActionFile {

	public static final String RDF = Translator.getString("Component.Convert.RDF/N-Triple.RDF.Text");
	public static final String SELECTED_RDF = Translator.getString("Component.Convert.RDF/N-Triple.SelectedRDF.Text");

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
		if (e.getActionCommand().equals(RDF)) {
			convertNTripleSRC(false);
		} else {
			convertNTripleSRC(true);
		}

		showSrcView();
	}
}
