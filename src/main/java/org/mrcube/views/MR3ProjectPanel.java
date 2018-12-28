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

package org.mrcube.views;

import org.mrcube.editors.ClassEditor;
import org.mrcube.editors.Editor;
import org.mrcube.editors.PropertyEditor;
import org.mrcube.editors.RDFEditor;
import org.mrcube.jgraph.GraphManager;
import org.mrcube.jgraph.RDFGraph;
import org.mrcube.models.MR3Constants.GraphType;
import org.mrcube.models.RDFSModelMap;
import org.mrcube.utils.Translator;
import org.mrcube.utils.Utilities;
import org.mrcube.views.TabComponent;

import javax.swing.*;
import java.awt.*;
import java.beans.PropertyVetoException;
import java.io.File;

/**
 * @author Takeshi Morita
 */
public class MR3ProjectPanel extends JPanel {

    private File currentProjectFile;
    private RDFSModelMap rdfsModelMap;

    private RDFEditor rdfEditor;
    private ClassEditor classEditor;
    private PropertyEditor propertyEditor;

    private JDesktopPane desktopPane;
    private JInternalFrame rdfEditorFrame;
    private JInternalFrame classEditorFrame;
    private JInternalFrame propertyEditorFrame;

    private static final int HEADER_HEIGHT = 70;

    public MR3ProjectPanel(GraphManager gmanager) {
        rdfsModelMap = new RDFSModelMap();
        currentProjectFile = new File(System.getProperty("user.dir"), Translator.getString("Component.File.NewProject.Text"));

        classEditor = new ClassEditor(gmanager);
        classEditor.setBackground(Color.WHITE);
        propertyEditor = new PropertyEditor(gmanager);
        propertyEditor.setBackground(Color.WHITE);
        rdfEditor = new RDFEditor(gmanager);
        rdfEditor.setBackground(Color.WHITE);
        registerComponent();

        rdfEditorFrame = createEditorFrame(rdfEditor, Translator.getString("RDFEditor.Title"),
                Utilities.getImageIcon(Translator.getString("RDFEditor.Icon")));

        classEditorFrame = createEditorFrame(classEditor, Translator.getString("ClassEditor.Title"),
                Utilities.getImageIcon(Translator.getString("ClassEditor.Icon")));

        propertyEditorFrame = createEditorFrame(propertyEditor, Translator.getString("PropertyEditor.Title"),
                Utilities.getImageIcon(Translator.getString("PropertyEditor.Icon")));

        desktopPane = new JDesktopPane();
        desktopPane.add(rdfEditorFrame);
        desktopPane.add(classEditorFrame);
        desktopPane.add(propertyEditorFrame);

        setLayout(new BorderLayout());
        add(desktopPane, BorderLayout.CENTER);
    }

    private JInternalFrame createEditorFrame(Editor editor, String title, ImageIcon imageIcon) {
        JInternalFrame editorFrame = new JInternalFrame();
        editorFrame.getContentPane().add(editor, BorderLayout.CENTER);
        editorFrame.setClosable(false);
        editorFrame.setFrameIcon(imageIcon);
        editorFrame.setTitle(title);
        editorFrame.setResizable(true);
        editorFrame.setMaximizable(true);
        editorFrame.setIconifiable(true);
        editorFrame.setVisible(true);
        return editorFrame;
    }

    public void frontEditor(GraphType graphType) {
        if (graphType == GraphType.RDF) {
            rdfEditorFrame.toFront();
        } else if (graphType == GraphType.CLASS) {
            classEditorFrame.toFront();
        } else if (graphType == GraphType.PROPERTY) {
            propertyEditorFrame.toFront();
        }
    }

    public void deployCPR() {
        int rootWindowWidth = getRootPane().getWidth();
        int rootWindowHeight = getRootPane().getHeight() - HEADER_HEIGHT;
        try {
            classEditorFrame.setIcon(false);
            propertyEditorFrame.setIcon(false);
            rdfEditorFrame.setIcon(false);
        } catch (PropertyVetoException e) {
            e.printStackTrace();
        }
        classEditorFrame.setSize(rootWindowWidth / 2, rootWindowHeight / 2);
        classEditorFrame.setLocation(new Point(0, 0));
        propertyEditorFrame.setSize(rootWindowWidth / 2, rootWindowHeight / 2);
        propertyEditorFrame.setLocation(new Point(rootWindowWidth / 2, 0));
        rdfEditorFrame.setSize(rootWindowWidth, rootWindowHeight / 2);
        rdfEditorFrame.setLocation(new Point(0, rootWindowHeight / 2));
    }

    public void deployCR() {
        int rootWindowWidth = getRootPane().getWidth();
        int rootWindowHeight = getRootPane().getHeight() - HEADER_HEIGHT;
        try {
            classEditorFrame.setIcon(false);
            rdfEditorFrame.setIcon(false);
        } catch (PropertyVetoException e) {
            e.printStackTrace();
        }
        classEditorFrame.setSize(rootWindowWidth, rootWindowHeight / 2);
        classEditorFrame.setLocation(new Point(0, 0));
        rdfEditorFrame.setSize(rootWindowWidth, rootWindowHeight / 2);
        rdfEditorFrame.setLocation(new Point(0, rootWindowHeight / 2));
        propertyEditorFrame.toBack();
    }

    public void deployPR() {
        int rootWindowWidth = getRootPane().getWidth();
        int rootWindowHeight = getRootPane().getHeight() - HEADER_HEIGHT;
        try {
            propertyEditorFrame.setIcon(false);
            rdfEditorFrame.setIcon(false);
        } catch (PropertyVetoException e) {
            e.printStackTrace();
        }
        propertyEditorFrame.setSize(rootWindowWidth, rootWindowHeight / 2);
        propertyEditorFrame.setLocation(new Point(0, 0));
        rdfEditorFrame.setSize(rootWindowWidth, rootWindowHeight / 2);
        rdfEditorFrame.setLocation(new Point(0, rootWindowHeight / 2));
        classEditorFrame.toBack();
    }

    public void registerComponent() {
        ToolTipManager.sharedInstance().registerComponent(rdfEditor.getGraph());
        ToolTipManager.sharedInstance().registerComponent(classEditor.getGraph());
        ToolTipManager.sharedInstance().registerComponent(propertyEditor.getGraph());
    }

    public GraphType getFocusedEditorType() {
        if (desktopPane.getSelectedFrame().equals(rdfEditorFrame)) {
            return GraphType.RDF;
        } else if (desktopPane.getSelectedFrame().equals(classEditorFrame)) {
            return GraphType.CLASS;
        } else if (desktopPane.getSelectedFrame().equals(propertyEditorFrame)) {
            return GraphType.PROPERTY;
        }
        return GraphType.RDF;
    }

    public void resetEditors() {
        ((RDFGraph) rdfEditor.getGraph()).removeAllCells();
        ((RDFGraph) classEditor.getGraph()).removeAllCells();
        ((RDFGraph) propertyEditor.getGraph()).removeAllCells();
    }

    public RDFEditor getRDFEditor() {
        return rdfEditor;
    }

    public ClassEditor getClassEditor() {
        return classEditor;
    }

    public PropertyEditor getPropertyEditor() {
        return propertyEditor;
    }

    public File getCurrentProjectFile() {
        return currentProjectFile;
    }

    public void setCurrentProjectFile(File file) {
        currentProjectFile = file;
    }

    public RDFSModelMap getRDFSInfoMap() {
        return rdfsModelMap;
    }

    public String getTitle() {
        if (currentProjectFile == null) {
            return Translator.getString("Component.File.NewProject.Text");
        }
        return currentProjectFile.getName();
    }

}
