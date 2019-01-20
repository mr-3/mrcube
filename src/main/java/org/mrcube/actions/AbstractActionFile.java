/*
 * Project Name: MR^3 (Meta-Model Management based on RDFs Revision Reflection)
 * Project Website: http://mrcube.org/
 *
 * Copyright (C) 2003-2018 Yamaguchi Laboratory, Keio University. All rights reserved.
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

package org.mrcube.actions;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.mrcube.MR3;
import org.mrcube.views.MR3ProjectPanel;
import org.mrcube.models.MR3Constants.HistoryType;
import org.mrcube.models.PrefConstants;
import org.mrcube.models.NamespaceModel;
import org.mrcube.utils.*;
import org.mrcube.views.HistoryManager;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.prefs.Preferences;

/**
 * @author Takeshi Morita
 */
abstract class AbstractActionFile extends MR3AbstractAction {

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
        Set<NamespaceModel> namespaceModelSet = GraphUtilities.getNamespaceModelSet();
        for (NamespaceModel info : namespaceModelSet) {
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
            return new BufferedInputStream(new FileInputStream(file));
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    private static final ProjectFileFilter mr3FileFilter = new ProjectFileFilter();
    private static final OWLFileFilter owlFileFilter = new OWLFileFilter(true);
    private static final RDFsFileFilter rdfsFileFilter = new RDFsFileFilter(true);
    private static final NTripleFileFilter n3FileFilter = new NTripleFileFilter(true);
    private static final PNGFileFilter pngFileFilter = new PNGFileFilter();

    protected File getFile(boolean isOpenFile, String extension) {
        Preferences userPrefs = mr3.getUserPrefs();
        JFileChooser jfc = new JFileChooser(userPrefs.get(PrefConstants.WorkDirectory, ""));
        switch (extension) {
            case "mr3":
                jfc.setFileFilter(mr3FileFilter);
                break;
            case "n3":
                jfc.setFileFilter(n3FileFilter);
                break;
            case "png":
                jfc.setFileFilter(pngFileFilter);
                break;
            case "owl":
                jfc.setFileFilter(owlFileFilter);
                break;
            default:
                jfc.setFileFilter(rdfsFileFilter);
                break;
        }

        if (isOpenFile) {
            if (jfc.showOpenDialog(MR3.getCurrentProject()) == JFileChooser.APPROVE_OPTION) {
                return jfc.getSelectedFile();
            }
            return null;
        }
        if (jfc.showSaveDialog(MR3.getCurrentProject()) == JFileChooser.APPROVE_OPTION) {
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
            Writer writer = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8);
            // RDF/XML-ABBREVにするとRDFAnonが出て，import時にAnonymousがうまく扱えない．
            setNsPrefix(exportModel);
            exportModel.write(writer, "RDF/XML", getBaseURI());
            MR3.getCurrentProject().setCurrentProjectFile(file);
        } catch (FileNotFoundException e2) {
            Utilities.showErrorMessageDialog("File Not Found");
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
    }


    protected void quitProject() {
        File currentProjectFile = MR3.getCurrentProject().getCurrentProjectFile();
        if (isNewProjectFile(MR3.getCurrentProject())) {
            saveProjectAs();
            HistoryManager.saveHistory(HistoryType.SAVE_PROJECT_AS, currentProjectFile.getAbsolutePath());
        } else {
            saveProject(currentProjectFile);
            HistoryManager.saveHistory(HistoryType.SAVE_PROJECT, currentProjectFile.getAbsolutePath());
        }
    }


    protected boolean isNewProjectFile(MR3ProjectPanel currentProject) {
        String basePath = null;
        File newFile = new File(basePath, Translator.getString("Component.File.NewProject.Text"));
        File currentProjectFile = currentProject.getCurrentProjectFile();
        return newFile.getAbsolutePath().equals(currentProjectFile.getAbsolutePath());
    }

    protected int confirmExitProject() {
        String title = MR3.getCurrentProject().getTitle();
        int messageType = JOptionPane.showConfirmDialog(MR3.getCurrentProject(), title + "\n" + Translator.getString("SaveChanges"),
                "MR^3 - " + title, JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE);
        if (messageType == JOptionPane.YES_OPTION) {
            quitProject();
        }
        return messageType;
    }
}
