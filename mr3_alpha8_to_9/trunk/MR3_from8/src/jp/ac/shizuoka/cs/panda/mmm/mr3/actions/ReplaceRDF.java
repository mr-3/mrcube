/*
 * Created on 2003/07/19
 *
 */
package jp.ac.shizuoka.cs.panda.mmm.mr3.actions;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import jp.ac.shizuoka.cs.panda.mmm.mr3.*;
import jp.ac.shizuoka.cs.panda.mmm.mr3.jgraph.*;
import jp.ac.shizuoka.cs.panda.mmm.mr3.util.*;

import com.hp.hpl.jena.rdf.model.*;

/**
 * @author takeshi morita
 *
 */
public class ReplaceRDF extends AbstractActionFile {

	public ReplaceRDF(MR3 mr3, String title) {
		super(mr3, title);
	}
	private static final String REPLACE_RDF_FILE = Translator.getString("Component.File.Import.Replace.RDF/XML(File).Text");
	private static final String REPLACE_RDF_URI =  Translator.getString("Component.File.Import.Replace.RDF/XML(URI).Text");

	public void actionPerformed(ActionEvent e) {
		Model model = null;
		Component desktop = mr3.getDesktopPane();
		GraphManager gmanager = mr3.getGraphManager();
		gmanager.setIsImporting(true);
		if (e.getActionCommand().equals(REPLACE_RDF_FILE)) {
			model = readModel(getReader("rdf", null), gmanager.getBaseURI(), "RDF/XML");
		} else if (e.getActionCommand().equals(REPLACE_RDF_URI)) {
			String uri = JOptionPane.showInternalInputDialog(desktop, "Open URI ( exp. http://slashdot.jp/slashdot.rdf )");
			model = readModel(getReader(uri), gmanager.getBaseURI(), "RDF/XML");
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
