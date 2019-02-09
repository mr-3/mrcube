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
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.mrcube.MR3;
import org.mrcube.io.MR3Writer;
import org.mrcube.utils.file_filter.*;
import org.mrcube.views.MR3ProjectPanel;
import org.mrcube.models.MR3Constants.HistoryType;
import org.mrcube.models.PrefConstants;
import org.mrcube.models.NamespaceModel;
import org.mrcube.utils.*;
import org.mrcube.views.HistoryManager;

import javax.swing.*;
import java.io.*;
import java.util.Set;
import java.util.prefs.Preferences;

/**
 * @author Takeshi Morita
 */
abstract class AbstractActionFile extends MR3AbstractAction {

    private MR3Writer mr3Writer;
    protected JFileChooser fileChooser;

    protected void initializeJFileChooser() {
        fileChooser = new JFileChooser();
        fileChooser.setAcceptAllFileFilterUsed(true);
        fileChooser.addChoosableFileFilter(turtleFileFilter);
        fileChooser.addChoosableFileFilter(jsonldFileFilter);
        fileChooser.addChoosableFileFilter(n3FileFilter);
        fileChooser.addChoosableFileFilter(RDF_FILE_FILTER);
        fileChooser.addChoosableFileFilter(mr3FileFilter);
    }

    public AbstractActionFile(MR3 mr3, String name, ImageIcon icon) {
        super(mr3, name, icon);
        mr3Writer = new MR3Writer(mr3.getGraphManager());
    }

    protected void setNsPrefix(Model model) {
        Set<NamespaceModel> namespaceModelSet = GraphUtilities.getNamespaceModelSet();
        for (NamespaceModel info : namespaceModelSet) {
            if (info.isAvailable()) {
                model.setNsPrefix(info.getPrefix(), info.getNameSpace());
            }
        }
    }

    private static final MR3ProjectFileFilter mr3FileFilter = new MR3ProjectFileFilter();
    private static final RDFFileFilter RDF_FILE_FILTER = new RDFFileFilter(true);
    private static final NTripleFileFilter n3FileFilter = new NTripleFileFilter(true);
    private static final TurtleFileFilter turtleFileFilter = new TurtleFileFilter(false);
    private static final JSONLDFileFilter jsonldFileFilter = new JSONLDFileFilter(false);

    protected File selectOpenFile() {
        Preferences userPrefs = mr3.getUserPrefs();
        fileChooser.setCurrentDirectory(new File(userPrefs.get(PrefConstants.WorkDirectory, "")));
        if (fileChooser.showOpenDialog(MR3.getCurrentProject()) == JFileChooser.APPROVE_OPTION) {
            return fileChooser.getSelectedFile();
        } else {
            return null;
        }
    }


    protected File selectSaveFile() {
        Preferences userPrefs = mr3.getUserPrefs();
        fileChooser.setCurrentDirectory(new File(userPrefs.get(PrefConstants.WorkDirectory, "")));
        if (fileChooser.showSaveDialog(MR3.getCurrentProject()) == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            if (fileChooser.getFileFilter() instanceof MR3FileFilter) {
                MR3FileFilter filter = (MR3FileFilter) fileChooser.getFileFilter();
                return new File(addFileExtension(selectedFile.getAbsolutePath(), filter.getExtension()));
            } else {
                return selectedFile;
            }
        } else {
            return null;
        }
    }

    private Model getModel() {
        Model model = ModelFactory.createDefaultModel();
        model.add(mr3Writer.getRDFModel());
        model.add(mr3Writer.getClassModel());
        model.add(mr3Writer.getPropertyModel());
        return model;
    }

    String getExtension(File f) {
        String ext = null;
        String s = f.getName();
        int i = s.lastIndexOf('.');

        if (i > 0 && i < s.length() - 1) {
            ext = s.substring(i + 1).toLowerCase();
        }
        return ext;
    }

    protected void saveFile(File file) {
        try {
            var fileExtension = getExtension(file);
            var exportModel = ModelFactory.createDefaultModel();
            var rdfFormat = RDFFormat.TURTLE;

            if (fileExtension == null) {
                file = new File(file.getAbsolutePath() + ".mr3");
                fileExtension = "mr3";
            }
            if (fileExtension.equals("mr3")) {
                exportModel = mr3.getMR3Writer().getProjectModel();
                MR3.getCurrentProject().setCurrentProjectFile(file);
            } else {
                exportModel = getModel();
                switch (fileExtension) {
                    case "ttl":
                        rdfFormat = RDFFormat.TURTLE;
                        break;
                    case "jsonld":
                        rdfFormat = RDFFormat.JSONLD;
                        break;
                    case "rdf":
                        rdfFormat = RDFFormat.RDFXML_ABBREV;
                        break;
                    case "n3":
                        rdfFormat = RDFFormat.NTRIPLES_UTF8;
                        break;
                }
            }
            setNsPrefix(exportModel);
            RDFDataMgr.write(new FileOutputStream(file), exportModel, rdfFormat);
            MR3.STATUS_BAR.setText(SaveFileAction.SAVE_PROJECT + ": " + file.getAbsolutePath());
            MR3.getCurrentProject().setCurrentProjectFile(file);
            mr3.ResourcePathTextField.setText(file.getAbsolutePath());
        } catch (FileNotFoundException e2) {
            Utilities.showErrorMessageDialog("File Not Found");
        }
    }

    private String addFileExtension(String defaultPath, String extension) {
        String ext = (extension != null) ? "." + extension.toLowerCase() : "";
        if (extension != null
                && !defaultPath.toLowerCase().endsWith(".ttl")
                && !defaultPath.toLowerCase().endsWith(".jsonld")
                && !defaultPath.toLowerCase().endsWith(".rdf")
                && !defaultPath.toLowerCase().endsWith(".n3")
                && !defaultPath.toLowerCase().endsWith(".mr3")
                && !defaultPath.toLowerCase().endsWith(".png")
        ) {
            defaultPath += ext;
        }
        return defaultPath;
    }

    protected void saveFileAs() {
        File file = selectSaveFile();
        if (file == null) {
            return;
        }
        saveFile(file);
        HistoryManager.saveHistory(HistoryType.SAVE_PROJECT_AS, file.getAbsolutePath());
    }

    protected void quitProject() {
        File currentProjectFile = MR3.getCurrentProject().getCurrentProjectFile();
        if (isNewProjectFile(MR3.getCurrentProject())) {
            saveFileAs();
            HistoryManager.saveHistory(HistoryType.SAVE_PROJECT_AS, currentProjectFile.getAbsolutePath());
        } else {
            saveFile(currentProjectFile);
            HistoryManager.saveHistory(HistoryType.SAVE_PROJECT, currentProjectFile.getAbsolutePath());
        }
    }

    protected boolean isNewProjectFile(MR3ProjectPanel currentProject) {
        String basePath = null;
        File newFile = new File(basePath, Translator.getString("Menu.File.New.Text"));
        File currentProjectFile = currentProject.getCurrentProjectFile();
        return newFile.getAbsolutePath().equals(currentProjectFile.getAbsolutePath());
    }

    protected int confirmExitProject() {
        String title = MR3.getCurrentProject().getTitle();
        int messageType = JOptionPane.showConfirmDialog(MR3.getCurrentProject(), title + "\n" + Translator.getString("SaveChanges"),
                "MR^3 - " + title, JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE);
        if (messageType == JOptionPane.YES_OPTION) {
            quitProject();
        }
        return messageType;
    }
}
