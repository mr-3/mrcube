/*
 * Created on 2003/07/19
 *
 */
package jp.ac.shizuoka.cs.panda.mmm.mr3.actions;

import java.awt.event.*;
import java.io.*;
import java.util.prefs.*;

import jp.ac.shizuoka.cs.panda.mmm.mr3.*;
import jp.ac.shizuoka.cs.panda.mmm.mr3.data.*;
import jp.ac.shizuoka.cs.panda.mmm.mr3.util.*;

import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.rdf.model.impl.*;

/**
 * @author takeshi morita
 *
 */
public class ExportRDF extends AbstractActionFile {

	public ExportRDF(MR3 mr3, String title) {
		super(mr3, title);
	}

	private static final String RDF_XML = Translator.getString("Component.File.Export.RDF/XML.RDF.Text");
	private static final String RDF_NTriple = Translator.getString("Component.File.Export.N-Triple.RDF.Text");
	private static final String SelectedRDF_XML = Translator.getString("Component.File.Export.RDF/XML.SelectedRDF.Text");
	private static final String SelectedRDF_NTriple = Translator.getString("Component.File.Export.N-Triple.SelectedRDF.Text");

	public void actionPerformed(ActionEvent e) {
		Preferences userPrefs = mr3.getUserPrefs();
		String type = getName();
		String ext = "rdf";
		if (type.equals(RDF_NTriple) || type.equals(SelectedRDF_NTriple)) {
			ext = "n3";
		}
		File file = getFile(false, ext);
		if (file == null) {
			return;
		}
		try {
			String encoding = userPrefs.get(PrefConstants.OutputEncoding, "EUC_JP");
			Writer output = new OutputStreamWriter(new FileOutputStream(file), encoding);
			RDFWriter writer = new RDFWriterFImpl().getWriter("RDF/XML-ABBREV");
			if (type.equals(RDF_NTriple) || type.equals(SelectedRDF_NTriple)) {
				writer = new RDFWriterFImpl().getWriter("N-TRIPLE");
			}
			if (type.equals(SelectedRDF_XML) || type.equals(SelectedRDF_NTriple)) {
				writeModel(mr3.getSelectedRDFModel(), output, writer);
			} else {
				writeModel(mr3.getRDFModel(), output, writer);
			}
			output.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}
