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
public class ExportRDFS extends AbstractActionFile {

	public static final String RDFS_XML = Translator.getString("Component.File.Export.RDF/XML.RDFS.Text");
	public static final String RDFS_NTriple = Translator.getString("Component.File.Export.N-Triple.RDFS.Text");
	public static final String SelectedRDFS_XML = Translator.getString("Component.File.Export.RDF/XML.SelectedRDFS.Text");
	public static final String SelectedRDFS_NTriple = Translator.getString("Component.File.Export.N-Triple.SelectedRDFS.Text");

	public ExportRDFS(MR3 mr3, String title) {
		super(mr3, title);
	}

	public void actionPerformed(ActionEvent e) {
		Preferences userPrefs = mr3.getUserPrefs();
		String type = e.getActionCommand();
		String ext = "rdfs";
		if (type.equals(RDFS_NTriple) || type.equals(SelectedRDFS_NTriple)) {
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
			if (type.equals(RDFS_NTriple) || type.equals(SelectedRDFS_NTriple)) {
				writer = new RDFWriterFImpl().getWriter("N-TRIPLE");
			}

			if (type.equals(SelectedRDFS_XML) || type.equals(SelectedRDFS_NTriple)) {
				writeModel(mr3.getSelectedRDFSModel(), output, writer);
			} else {
				writeModel(mr3.getRDFSModel(), output, writer);
			}
		} catch (RDFException re) {
			re.printStackTrace();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}

}
