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
public class MergeRDFs extends AbstractActionFile {

	public MergeRDFs(MR3 mr3, String title) {
		super(mr3, title);
	}

	private static final String MERGE_RDFS_FILE = Translator.getString("Component.File.Import.Merge.RDF(S)/XML(File).Text");
	private static final String MERGE_RDFS_URI = Translator.getString("Component.File.Import.Merge.RDF(S)/XML(URI).Text");

	public void actionPerformed(ActionEvent e) {
		Component desktop = mr3.getDesktopPane();
		GraphManager gmanager = mr3.getGraphManager();
		gmanager.setIsImporting(true);
		Model model = null;
		if (e.getActionCommand().equals(MERGE_RDFS_FILE)) {
			model = readModel(getReader("rdfs", null), gmanager.getBaseURI());
		} else if (e.getActionCommand().equals(MERGE_RDFS_URI)) {
			String uri = JOptionPane.showInternalInputDialog(desktop, "Open URI ( exp. http://www.w3.org/TR/rdf-schema/rdfs-namespace.xml )");
			model = readModel(getReader(uri), gmanager.getBaseURI());
		}
		if (model == null) {
			gmanager.setIsImporting(false);
			return;
		}
		mr3.mergeRDFSModel(model);	
		gmanager.applyTreeLayout();	
		gmanager.setIsImporting(false);
	}

}
