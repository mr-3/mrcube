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
public class ReplaceRDF extends AbstractActionFile {

	public static final String REPLACE_RDF_FILE = Translator.getString("Component.File.Import.Replace.RDF/XML(File).Text");
	public static final String REPLACE_N_TRIPLE_FILE = Translator.getString("Component.File.Import.Replace.RDF/N-Triple(File).Text");
	public static final String REPLACE_RDF_URI = Translator.getString("Component.File.Import.Replace.RDF/XML(URI).Text");
	public static final String REPLACE_N_TRIPLE_URI = Translator.getString("Component.File.Import.Replace.RDF/N-Triple(URI).Text");

	public ReplaceRDF(MR3 mr3, String title) {
		super(mr3, title);
	}

	public void actionPerformed(ActionEvent e) {
		Model model = null;
		Component desktop = mr3.getDesktopPane();
		GraphManager gmanager = mr3.getGraphManager();
		gmanager.setIsImporting(true);
		if (e.getActionCommand().equals(REPLACE_RDF_FILE)) {
			model = readModel(getReader("rdf", null), gmanager.getBaseURI(), "RDF/XML");
		} else if (e.getActionCommand().equals(REPLACE_N_TRIPLE_FILE)) {
			model = readModel(getReader("n3", null), gmanager.getBaseURI(), "N-TRIPLE");
		} else if (e.getActionCommand().equals(REPLACE_RDF_URI)) {
			String uri = JOptionPane.showInternalInputDialog(desktop, "Open URI ( exp. http://slashdot.jp/slashdot.rdf )");
			model = readModel(getReader(uri), gmanager.getBaseURI(), "RDF/XML");
		} else if (e.getActionCommand().equals(REPLACE_N_TRIPLE_URI)) {
			String uri = JOptionPane.showInternalInputDialog(desktop, "Open URI ( exp. http://slashdot.jp/slashdot.rdf )");
			model = readModel(getReader(uri), gmanager.getBaseURI(), "N-TRIPLE");
		}
		if (model == null) {
			gmanager.setIsImporting(false);
			return;
		}
		mr3.replaceRDFModel(model);
		gmanager.applyTreeLayout();
		gmanager.setIsImporting(false);
	}

}
