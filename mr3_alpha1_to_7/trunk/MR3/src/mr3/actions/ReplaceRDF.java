/*
 * Created on 2003/07/19
 *
 */
package mr3.actions;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import mr3.*;
import mr3.jgraph.*;

import com.hp.hpl.mesa.rdf.jena.model.*;

/**
 * @author takeshi morita
 *
 */
public class ReplaceRDF extends AbstractActionFile {

	public ReplaceRDF(MR3 mr3, String title) {
		super(mr3, title);
	}
	private static final String REPLACE_RDF_FILE = "RDF/XML (File)";
	private static final String REPLACE_RDF_URI = "RDF/XML (URI)";

	public void actionPerformed(ActionEvent e) {
		Model model = null;
		Component desktop = mr3.getDesktopPane();
		GraphManager gmanager = mr3.getGraphManager();
		gmanager.setIsImporting(true);
		if (e.getActionCommand().equals(REPLACE_RDF_FILE)) {
			model = readModel(getReader("rdf", null), gmanager.getBaseURI());
		} else if (e.getActionCommand().equals(REPLACE_RDF_URI)) {
			String uri = JOptionPane.showInternalInputDialog(desktop, "Open URI ( exp. http://slashdot.jp/slashdot.rdf )");
			model = readModel(getReader(uri), gmanager.getBaseURI());
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
