/*
 * Created on 2003/07/19
 *
 */
package org.semanticweb.mmm.mr3.actions;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import org.semanticweb.mmm.mr3.*;
import org.semanticweb.mmm.mr3.jgraph.*;
import org.semanticweb.mmm.mr3.util.*;

import com.hp.hpl.jena.rdf.model.*;

/**
 * @author takeshi morita
 *
 */
public class MergeRDFs extends AbstractActionFile {

	public static final String MERGE_RDFS_FILE = Translator.getString("Component.File.Import.Merge.RDF(S)/XML(File).Text");
	public static final String MERGE_RDFS_URI = Translator.getString("Component.File.Import.Merge.RDF(S)/XML(URI).Text");
	public static final String MERGE_N_TRIPLE_FILE = Translator.getString("Component.File.Import.Merge.RDF(S)/N-Triple(File).Text");
	public static final String MERGE_N_TRIPLE_URI = Translator.getString("Component.File.Import.Merge.RDF(S)/N-Triple(URI).Text");

	public MergeRDFs(MR3 mr3, String title) {
		super(mr3, title);
	}

	public void actionPerformed(ActionEvent e) {
		Component desktop = mr3.getDesktopPane();
		GraphManager gmanager = mr3.getGraphManager();
		gmanager.setIsImporting(true);
		Model model = null;
		if (e.getActionCommand().equals(MERGE_RDFS_FILE)) {
			model = readModel(getReader("rdfs", null), gmanager.getBaseURI(), "RDF/XML");
		} else if (e.getActionCommand().equals(MERGE_N_TRIPLE_FILE)) {
			model = readModel(getReader("n3", null), gmanager.getBaseURI(), "N-TRIPLE");
		} else if (e.getActionCommand().equals(MERGE_RDFS_URI)) {
			String uri = JOptionPane.showInternalInputDialog(desktop, "Open URI ( exp. http://www.w3.org/TR/rdf-schema/rdfs-namespace.xml )");
			model = readModel(getReader(uri), gmanager.getBaseURI(), "RDF/XML");
		} else if (e.getActionCommand().equals(MERGE_N_TRIPLE_URI)) {
			String uri = JOptionPane.showInternalInputDialog(desktop, "Open URI ( exp. http://www.w3.org/TR/rdf-schema/rdfs-namespace.xml )");
			model = readModel(getReader(uri), gmanager.getBaseURI(), "N-TRIPLE");
		}
		if (model == null) {
			gmanager.setIsImporting(false);
			return;
		}
		mr3.mergeRDFSModel(model);
		mr3.performTreeLayout();
		gmanager.setIsImporting(false);
	}

}
