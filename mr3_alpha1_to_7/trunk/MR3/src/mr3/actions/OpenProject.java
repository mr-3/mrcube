/*
 * Created on 2003/07/19
 *
 */
package mr3.actions;

import java.awt.event.*;
import java.io.*;

import javax.swing.*;

import mr3.*;
import mr3.jgraph.*;
import mr3.util.*;

import com.hp.hpl.mesa.rdf.jena.model.*;

/**
 * @author takeshi morita
 *
 */
public class OpenProject extends AbstractActionFile {

	private static final String OPEN_PROJECT = "Open Project";

	public OpenProject(MR3 mr3) {
		super(mr3, OPEN_PROJECT);
	}

	public OpenProject(MR3 mr3, ImageIcon icon) {
		super(mr3, OPEN_PROJECT, icon);
	}

	public void actionPerformed(ActionEvent e) {
		openProject();
	}

	private void openProject() {
		GraphManager gmanager = mr3.getGraphManager();
		try {
			ProjectManager pm = new ProjectManager(gmanager, mr3.getNSTableDialog());
			gmanager.setIsImporting(true);
			Model model = readModel(getReader("mr3", "UTF8"), gmanager.getBaseURI());
			if (model == null) {
				return;
			}
			File tmp = mr3.getCurrentProject();
			newProject();
			mr3.setCurrentProject(tmp);
			// 順番が重要なので、よく考えること
			Model projectModel = pm.extractProjectModel(model);
			mr3.mergeRDFSModel(model);
			mr3.getNSTableDialog().setCurrentNSPrefix();
			pm.loadProject(projectModel);
			pm.removeEmptyClass();
			gmanager.removeTypeCells();
			gmanager.addTypeCells();
			gmanager.setIsImporting(false);
			mr3.setTitle("MR^3 - " + mr3.getCurrentProject().getAbsolutePath());
		} catch (RDFException e1) {
			e1.printStackTrace();
		}
	}
}
