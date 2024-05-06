/*
 * Project Name: MR^3 (Meta-Model Management based on RDFs Revision Reflection)
 * Project Website: https://mr-3.github.io/
 *
 * Copyright (C) 2003-2024 Takeshi Morita. All rights reserved.
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

package jp.ac.aoyama.it.ke.mrcube.actions;

import jp.ac.aoyama.it.ke.mrcube.MR3;
import jp.ac.aoyama.it.ke.mrcube.io.MR3Writer;
import jp.ac.aoyama.it.ke.mrcube.models.MR3Constants.HistoryType;
import jp.ac.aoyama.it.ke.mrcube.models.NamespaceModel;
import jp.ac.aoyama.it.ke.mrcube.models.PrefConstants;
import jp.ac.aoyama.it.ke.mrcube.utils.GraphUtilities;
import jp.ac.aoyama.it.ke.mrcube.utils.Translator;
import jp.ac.aoyama.it.ke.mrcube.utils.Utilities;
import jp.ac.aoyama.it.ke.mrcube.utils.file_filter.*;
import jp.ac.aoyama.it.ke.mrcube.views.HistoryManager;
import jp.ac.aoyama.it.ke.mrcube.views.MR3ProjectPanel;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;

import javax.swing.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.Set;
import java.util.prefs.Preferences;

/**
 * @author Takeshi Morita
 */
abstract class AbstractActionFile extends MR3AbstractAction {

    private final MR3Writer mr3Writer;
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
        if (fileChooser.showOpenDialog(MR3.getProjectPanel()) == JFileChooser.APPROVE_OPTION) {
            return fileChooser.getSelectedFile();
        } else {
            return null;
        }
    }


    protected File selectSaveFile() {
        Preferences userPrefs = mr3.getUserPrefs();
        fileChooser.setCurrentDirectory(new File(userPrefs.get(PrefConstants.WorkDirectory, "")));
        if (fileChooser.showSaveDialog(MR3.getProjectPanel()) == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            if (fileChooser.getFileFilter() instanceof MR3FileFilter filter) {
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
                MR3.getProjectPanel().setProjectFile(file);
            } else {
                exportModel = getModel();
                switch (fileExtension) {
                    case "ttl" -> rdfFormat = RDFFormat.TURTLE;
                    case "jsonld" -> rdfFormat = RDFFormat.JSONLD;
                    case "rdf" -> rdfFormat = RDFFormat.RDFXML_ABBREV;
                    case "n3" -> rdfFormat = RDFFormat.NTRIPLES_UTF8;
                }
            }
            setNsPrefix(exportModel);
            RDFDataMgr.write(new FileOutputStream(file), exportModel, rdfFormat);
            MR3.STATUS_BAR.setText(SaveFileAction.SAVE_PROJECT + ": " + file.getAbsolutePath());
            MR3.getProjectPanel().setProjectFile(file);
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
        File projectFile = MR3.getProjectPanel().getProjectFile();
        if (isNewProjectFile(MR3.getProjectPanel())) {
            saveFileAs();
            HistoryManager.saveHistory(HistoryType.SAVE_PROJECT_AS, projectFile.getAbsolutePath());
        } else {
            saveFile(projectFile);
            HistoryManager.saveHistory(HistoryType.SAVE_PROJECT, projectFile.getAbsolutePath());
        }
    }

    protected boolean isNewProjectFile(MR3ProjectPanel projectPanel) {
        String basePath = null;
        File newFile = new File(basePath, Translator.getString("Menu.File.New.Text"));
        File projectFile = projectPanel.getProjectFile();
        return newFile.getAbsolutePath().equals(projectFile.getAbsolutePath());
    }

    protected int confirmExitProject() {
        String title = MR3.getProjectPanel().getTitle();
        int messageType = JOptionPane.showConfirmDialog(MR3.getProjectPanel(), title + "\n" + Translator.getString("SaveChanges"),
                "MR^3 - " + title, JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE);
        if (messageType == JOptionPane.YES_OPTION) {
            quitProject();
        }
        return messageType;
    }
}
