/*
 * Created on 2003/07/19
 *
 */
package jp.ac.shizuoka.cs.panda.mmm.mr3.actions;

import java.awt.event.*;
import java.io.*;

import javax.swing.*;

import jp.ac.shizuoka.cs.panda.mmm.mr3.*;
import jp.ac.shizuoka.cs.panda.mmm.mr3.jgraph.*;
import jp.ac.shizuoka.cs.panda.mmm.mr3.util.*;

import com.hp.hpl.jena.rdf.model.*;

/**
 * @author takeshi morita
 *
 */
public class OpenProject extends AbstractActionFile {

	private static final String OPEN_PROJECT = "Open Project";

	public OpenProject(MR3 mr3) {
		super(mr3, OPEN_PROJECT, Utilities.getImageIcon("open.gif"));
		setValues();
	}

	private void setValues() {
		putValue(SHORT_DESCRIPTION, "Open Project");
		putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_O, KeyEvent.CTRL_MASK));
	}

	public void actionPerformed(ActionEvent e) {
		GraphManager gmanager = mr3.getGraphManager();
		gmanager.setIsImporting(true);
		Model model = readModel(getReader("mr3", "UTF8"), gmanager.getBaseURI());

		if (model != null) {
			File tmp = mr3.getCurrentProject(); // New Projectよりも前のを保存
			mr3.replaceProjectModel(model);
			mr3.setCurrentProject(tmp);
			mr3.setTitle("MR^3 - " + mr3.getCurrentProject().getAbsolutePath());
		}
		gmanager.clearSelection();
		gmanager.setIsImporting(false);
	}

}
