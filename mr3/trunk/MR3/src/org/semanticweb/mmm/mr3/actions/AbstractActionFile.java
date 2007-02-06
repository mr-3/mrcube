/*
 * @(#) AbstractAction.java
 * 
 * Copyright (C) 2003 The MMM Project
 * 
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation; either version 2.1 of the License, or (at your
 * option) any later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *  
 */

package org.semanticweb.mmm.mr3.actions;

import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.prefs.*;

import javax.swing.*;

import org.semanticweb.mmm.mr3.*;
import org.semanticweb.mmm.mr3.data.*;
import org.semanticweb.mmm.mr3.data.MR3Constants.*;
import org.semanticweb.mmm.mr3.ui.*;
import org.semanticweb.mmm.mr3.util.*;

import com.hp.hpl.jena.rdf.model.*;

/**
 * @author takeshi morita
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
        if (is == null) { return null; }
        Model model = ModelFactory.createDefaultModel();
        try {
            model.read(is, xmlbase, type);
        } catch (RDFException e) {
            e.printStackTrace();
        }

        return model;
    }

    protected InputStream getInputStream(String ext) {
        File file = getFile(true, ext);
        if (file == null) { return null; }
        if (ext.equals("mr3")) {
            MR3.setCurrentProject(file);
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
    private static OWLFileFilter owlFileFilter = new OWLFileFilter();
    private static RDFsFileFilter rdfsFileFilter = new RDFsFileFilter();
    private static NTripleFileFilter n3FileFilter = new NTripleFileFilter();
    private static PNGFileFilter pngFileFilter = new PNGFileFilter();

    protected File getFile(boolean isOpenFile, String extension) {
        Component desktop = mr3.getDesktopPane();
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
            if (jfc.showOpenDialog(desktop) == JFileChooser.APPROVE_OPTION) { return jfc.getSelectedFile(); }
            return null;
        }
        if (jfc.showSaveDialog(desktop) == JFileChooser.APPROVE_OPTION) {
            String defaultPath = jfc.getSelectedFile().getAbsolutePath();
            if (extension.equals("mr3")) { return new File(complementMR3Extension(defaultPath, extension)); }
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
            // RDF/XML-ABBREVÇ…Ç∑ÇÈÇ∆RDFAnonÇ™èoÇƒÅCimportéûÇ…AnonymousÇ™Ç§Ç‹Ç≠àµÇ¶Ç»Ç¢ÅD
            setNsPrefix(exportModel);
            exportModel.write(writer, "RDF/XML", getBaseURI());
            mr3.setTitle("MR3 - " + file.getAbsolutePath());
            MR3.setCurrentProject(file);
        } catch (RDFException e1) {
            e1.printStackTrace();
        } catch (FileNotFoundException e2) {
            JOptionPane.showMessageDialog(null, "FileNotFound", "Warning", JOptionPane.ERROR_MESSAGE);
        } catch (UnsupportedEncodingException e3) {
            e3.printStackTrace();
        }
    }

    protected void saveProjectAs() {
        File file = getFile(false, "mr3");
        if (file == null) { return; }
        saveProject(file);
        HistoryManager.saveHistory(HistoryType.SAVE_PROJECT_AS, file.getAbsolutePath());
        MR3.setCurrentProject(file);
    }

    protected void exitProgram(Frame frame) {
        int messageType = confirmExitProject(frame, Translator.getString("ExitProgram"));
        if (messageType == JOptionPane.CANCEL_OPTION) { return; }
        saveWindows();
        System.exit(0);
    }
    private void saveWindowBounds(Preferences userPrefs) {
        Rectangle windowRect = mr3.getBounds();
        userPrefs.putInt(PrefConstants.WindowHeight, (int) windowRect.getHeight());
        userPrefs.putInt(PrefConstants.WindowWidth, (int) windowRect.getWidth());
        userPrefs.putInt(PrefConstants.WindowPositionX, (int) windowRect.getX());
        userPrefs.putInt(PrefConstants.WindowPositionY, (int) windowRect.getY());
    }

    private void saveRDFEditorBounds(Preferences userPrefs, Rectangle rect) {
        userPrefs.putInt(PrefConstants.RDFEditorHeight, (int) rect.getHeight());
        userPrefs.putInt(PrefConstants.RDFEditorWidth, (int) rect.getWidth());
        userPrefs.putInt(PrefConstants.RDFEditorPositionX, (int) rect.getX());
        userPrefs.putInt(PrefConstants.RDFEditorPositionY, (int) rect.getY());
    }

    private void saveClassEditorBounds(Preferences userPrefs, Rectangle rect) {
        userPrefs.putInt(PrefConstants.ClassEditorHeight, (int) rect.getHeight());
        userPrefs.putInt(PrefConstants.ClassEditorWidth, (int) rect.getWidth());
        userPrefs.putInt(PrefConstants.ClassEditorPositionX, (int) rect.getX());
        userPrefs.putInt(PrefConstants.ClassEditorPositionY, (int) rect.getY());
    }

    private void savePropertyEditorBounds(Preferences userPrefs, Rectangle rect) {
        userPrefs.putInt(PrefConstants.PropertyEditorHeight, (int) rect.getHeight());
        userPrefs.putInt(PrefConstants.PropertyEditorWidth, (int) rect.getWidth());
        userPrefs.putInt(PrefConstants.PropertyEditorPositionX, (int) rect.getX());
        userPrefs.putInt(PrefConstants.PropertyEditorPositionY, (int) rect.getY());
    }

    private void saveWindows() {
        Preferences userPrefs = mr3.getUserPrefs();
        saveWindowBounds(userPrefs);
        saveRDFEditorBounds(userPrefs, mr3.getRDFEditor().getBounds());
        saveClassEditorBounds(userPrefs, mr3.getClassEditor().getBounds());
        savePropertyEditorBounds(userPrefs, mr3.getPropertyEditor().getBounds());
    }

    protected int confirmExitProject(Frame root, String title) {
        int messageType = JOptionPane.showConfirmDialog(root, Translator.getString("SaveChanges"), "MR3 - " + title,
                JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE);
        if (messageType == JOptionPane.YES_OPTION) {
            saveProjectAs();
        }
        return messageType;
    }
}
