/*
 * Project Name: MR^3 (Meta-Model Management based on RDFs Revision Reflection)
 * Project Website: http://mr3.sourceforge.net/
 * 
 * Copyright (C) 2003-2015 Yamaguchi Laboratory, Keio University. All rights reserved. 
 * 
 * This file is part of MR^3.
 * 
 * MR^3 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * MR^3 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with MR^3.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */

package net.sourceforge.mr3.actions;

import java.awt.Component;
import java.awt.Frame;
import java.awt.Rectangle;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.Set;
import java.util.prefs.Preferences;

import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import net.sourceforge.mr3.MR3;
import net.sourceforge.mr3.MR3Project;
import net.sourceforge.mr3.data.MR3Constants.HistoryType;
import net.sourceforge.mr3.data.PrefConstants;
import net.sourceforge.mr3.data.PrefixNSInfo;
import net.sourceforge.mr3.ui.HistoryManager;
import net.sourceforge.mr3.util.GraphUtilities;
import net.sourceforge.mr3.util.NTripleFileFilter;
import net.sourceforge.mr3.util.OWLFileFilter;
import net.sourceforge.mr3.util.PNGFileFilter;
import net.sourceforge.mr3.util.ProjectFileFilter;
import net.sourceforge.mr3.util.RDFsFileFilter;
import net.sourceforge.mr3.util.Translator;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

/**
 * @author Takeshi Morita
 * 
 */
public abstract class AbstractActionFile extends MR3AbstractAction {

	public AbstractActionFile() {
	}

	public AbstractActionFile(MR3 mr3) {
		this.mr3 = mr3;
	}

	public AbstractActionFile(String name) {
		super(name);
	}

	public AbstractActionFile(MR3 mr3, String name) {
		super(name);
		this.mr3 = mr3;
	}

	public AbstractActionFile(MR3 mr3, String name, ImageIcon icon) {
		super(mr3, name, icon);
	}

	protected void setNsPrefix(Model model) {
		Set<PrefixNSInfo> prefixNsInfoSet = GraphUtilities.getPrefixNSInfoSet();
		for (PrefixNSInfo info : prefixNsInfoSet) {
			if (info.isAvailable()) {
				model.setNsPrefix(info.getPrefix(), info.getNameSpace());
			}
		}
	}

	private String getBaseURI() {
		return mr3.getGraphManager().getBaseURI().replaceAll("#", "");
	}

	protected Model readModel(InputStream is, String xmlbase, String type) {
		if (is == null) {
			return null;
		}
		Model model = ModelFactory.createDefaultModel();
		model.read(is, xmlbase, type);
		return model;
	}

	protected InputStream getInputStream(String ext) {
		File file = getFile(true, ext);
		if (file == null) {
			return null;
		}
		if (ext.equals("mr3")) {
			MR3.getCurrentProject().setCurrentProjectFile(file);
		}
		try {
			BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
			return bis;
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		return null;
	}

	private static ProjectFileFilter mr3FileFilter = new ProjectFileFilter();
	private static OWLFileFilter owlFileFilter = new OWLFileFilter(true);
	private static RDFsFileFilter rdfsFileFilter = new RDFsFileFilter(true);
	private static NTripleFileFilter n3FileFilter = new NTripleFileFilter(true);
	private static PNGFileFilter pngFileFilter = new PNGFileFilter();

	protected File getFile(boolean isOpenFile, String extension) {
		Component desktop = mr3.getGraphManager().getDesktopTabbedPane();
		Preferences userPrefs = mr3.getUserPrefs();
		JFileChooser jfc = new JFileChooser(userPrefs.get(PrefConstants.WorkDirectory, ""));
		if (extension.equals("mr3")) {
			jfc.setFileFilter(mr3FileFilter);
		} else if (extension.equals("n3")) {
			jfc.setFileFilter(n3FileFilter);
		} else if (extension.equals("png")) {
			jfc.setFileFilter(pngFileFilter);
		} else if (extension.equals("owl")) {
			jfc.setFileFilter(owlFileFilter);
		} else {
			jfc.setFileFilter(rdfsFileFilter);
		}

		if (isOpenFile) {
			if (jfc.showOpenDialog(desktop) == JFileChooser.APPROVE_OPTION) {
				return jfc.getSelectedFile();
			}
			return null;
		}
		if (jfc.showSaveDialog(desktop) == JFileChooser.APPROVE_OPTION) {
			String defaultPath = jfc.getSelectedFile().getAbsolutePath();
			if (extension.equals("mr3")) {
				return new File(complementMR3Extension(defaultPath, extension));
			}
			return new File(complementRDFsExtension(defaultPath, extension));
		}
		return null;
	}

	private String complementMR3Extension(String tmp, String extension) {
		String ext = (extension != null) ? "." + extension.toLowerCase() : "";
		if (extension != null && !tmp.toLowerCase().endsWith(".mr3")) {
			tmp += ext;
		}
		return tmp;
	}

	private String complementRDFsExtension(String tmp, String extension) {
		String ext = (extension != null) ? "." + extension.toLowerCase() : "";
		if (extension != null && !tmp.toLowerCase().endsWith(".rdf") && !tmp.toLowerCase().endsWith(".rdfs")
				&& !tmp.toLowerCase().endsWith(".n3")) {
			tmp += ext;
		}
		return tmp;
	}

	protected void saveProject(File file) {
		try {
			Model exportModel = mr3.getMR3Writer().getProjectModel();
			Writer writer = new OutputStreamWriter(new FileOutputStream(file), "UTF-8");
			// RDF/XML-ABBREVにするとRDFAnonが出て，import時にAnonymousがうまく扱えない．
			setNsPrefix(exportModel);
			exportModel.write(writer, "RDF/XML", getBaseURI());
			MR3.getCurrentProject().setCurrentProjectFile(file);
			MR3.setCurrentProjectName();
		} catch (FileNotFoundException e2) {
			JOptionPane.showMessageDialog(null, "FileNotFound", "Warning", JOptionPane.ERROR_MESSAGE);
		} catch (UnsupportedEncodingException e3) {
			e3.printStackTrace();
		}
	}

	protected void saveProjectAs() {
		File file = getFile(false, "mr3");
		if (file == null) {
			return;
		}
		saveProject(file);
		HistoryManager.saveHistory(HistoryType.SAVE_PROJECT_AS, file.getAbsolutePath());
		MR3.getCurrentProject().setCurrentProjectFile(file);
		MR3.setCurrentProjectName();
	}

	protected void exitProgram(Frame frame) {
		int messageType = JOptionPane.showConfirmDialog(frame, Translator.getString("SaveChanges"), "MR^3 - "
				+ Translator.getString("ExitProgram"), JOptionPane.YES_NO_CANCEL_OPTION,
				JOptionPane.INFORMATION_MESSAGE);
		if (messageType == JOptionPane.YES_OPTION) {
			confirmAllExitProject(frame);
		} else if (messageType == JOptionPane.CANCEL_OPTION) {
			return;
		} else if (messageType == JOptionPane.NO_OPTION) {
			saveWindows();
			System.exit(0);
		}
	}

	protected void exitProject(MR3Project project) {
		if (project == null) {
			return;
		}
		File currentProjectFile = project.getCurrentProjectFile();
		if (isNewProjectFile(project)) {
			saveProjectAs();
			HistoryManager.saveHistory(HistoryType.SAVE_PROJECT_AS, currentProjectFile.getAbsolutePath());
		} else {
			saveProject(currentProjectFile);
			HistoryManager.saveHistory(HistoryType.SAVE_PROJECT, currentProjectFile.getAbsolutePath());
		}
	}

	private void saveWindowBounds(Preferences userPrefs) {
		Rectangle windowRect = mr3.getBounds();
		userPrefs.putInt(PrefConstants.WindowHeight, (int) windowRect.getHeight());
		userPrefs.putInt(PrefConstants.WindowWidth, (int) windowRect.getWidth());
		userPrefs.putInt(PrefConstants.WindowPositionX, (int) windowRect.getX());
		userPrefs.putInt(PrefConstants.WindowPositionY, (int) windowRect.getY());
	}

	private void saveWindows() {
		Preferences userPrefs = mr3.getUserPrefs();
		saveWindowBounds(userPrefs);
	}

	protected boolean isNewProjectFile(MR3Project currentProject) {
		String basePath = null;
		File newFile = new File(basePath, Translator.getString("Component.File.NewProject.Text"));
		File currentProjectFile = currentProject.getCurrentProjectFile();
		return newFile.getAbsolutePath().equals(currentProjectFile.getAbsolutePath());
	}

	protected int confirmExitProject(Frame root, MR3Project project) {
		String title = project.getTitle();
		int messageType = JOptionPane.showConfirmDialog(root, title + "\n" + Translator.getString("SaveChanges"),
				"MR^3 - " + title, JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE);
		if (messageType == JOptionPane.YES_OPTION) {
			exitProject(project);
		}
		return messageType;
	}

	protected void confirmAllExitProject(Frame root) {
		for (int i = 0; i < mr3.getDesktopTabbedPane().getTabCount(); i++) {
			mr3.getDesktopTabbedPane().setSelectedIndex(i);
			MR3Project project = (MR3Project) mr3.getDesktopTabbedPane().getSelectedComponent();
			confirmExitProject(root, project);
		}
		saveWindows();
		System.exit(0);
	}
}
