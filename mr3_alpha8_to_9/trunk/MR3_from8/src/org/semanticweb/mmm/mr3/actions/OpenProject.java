/*
 * Created on 2003/07/19
 *
 */
package org.semanticweb.mmm.mr3.actions;

import java.awt.event.*;
import java.io.*;

import javax.swing.*;

import org.semanticweb.mmm.mr3.*;
import org.semanticweb.mmm.mr3.jgraph.*;
import org.semanticweb.mmm.mr3.util.*;

import com.hp.hpl.jena.rdf.model.*;

/**
 * @author takeshi morita
 *
 */
public class OpenProject extends AbstractActionFile {

	private static final String TITLE = Translator.getString("Component.File.OpenProject.Text");
	private static final ImageIcon ICON = Utilities.getImageIcon(Translator.getString("Component.File.OpenProject.Icon"));

	public OpenProject(MR3 mr3) {
		super(mr3, TITLE, ICON);
		setValues();
	}

	private void setValues() {
		putValue(SHORT_DESCRIPTION, TITLE);
		putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_O, KeyEvent.CTRL_MASK));
	}

	public void actionPerformed(ActionEvent e) {
		GraphManager gmanager = mr3.getGraphManager();
		gmanager.setIsImporting(true);
		Model model = readModel(getReader("mr3", "UTF8"), gmanager.getBaseURI(), "RDF/XML");

		if (model != null) {
			File tmp = mr3.getCurrentProject(); // New Project‚æ‚è‚à‘O‚Ì‚ð•Û‘¶
			mr3.replaceProjectModel(model);
			mr3.setCurrentProject(tmp);
			mr3.setTitle("MR^3 - " + mr3.getCurrentProject().getAbsolutePath());
		}
		gmanager.clearSelection();
		gmanager.setIsImporting(false);
	}

}
