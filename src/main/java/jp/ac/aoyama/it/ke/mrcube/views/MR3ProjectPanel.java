/*
 * Project Name: MR^3 (Meta-Model Management based on RDFs Revision Reflection)
 * Project Website: https://mr-3.github.io/
 *
 * Copyright (C) 2003-2025 Takeshi Morita. All rights reserved.
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

package jp.ac.aoyama.it.ke.mrcube.views;

import jp.ac.aoyama.it.ke.mrcube.editors.ClassEditor;
import jp.ac.aoyama.it.ke.mrcube.editors.Editor;
import jp.ac.aoyama.it.ke.mrcube.editors.PropertyEditor;
import jp.ac.aoyama.it.ke.mrcube.editors.InstanceEditor;
import jp.ac.aoyama.it.ke.mrcube.jgraph.GraphManager;
import jp.ac.aoyama.it.ke.mrcube.jgraph.RDFGraph;
import jp.ac.aoyama.it.ke.mrcube.models.MR3Constants.GraphType;
import jp.ac.aoyama.it.ke.mrcube.models.RDFSModelMap;
import jp.ac.aoyama.it.ke.mrcube.utils.Translator;
import jp.ac.aoyama.it.ke.mrcube.utils.Utilities;

import javax.swing.*;
import java.awt.*;
import java.beans.PropertyVetoException;
import java.io.File;

/**
 * @author Takeshi Morita
 */
public class MR3ProjectPanel extends JPanel {

    private File projectFile;
    private final RDFSModelMap rdfsModelMap;

    private final GraphManager gmanager;
    private final InstanceEditor instanceEditor;
    private final ClassEditor classEditor;
    private final PropertyEditor propertyEditor;

    private final JDesktopPane desktopPane;
    private final JInternalFrame instanceEditorFrame;
    private final JInternalFrame classEditorFrame;
    private final JInternalFrame propertyEditorFrame;

    private static final int MARGIN = 5;

    public MR3ProjectPanel(GraphManager gmanager) {
        this.gmanager = gmanager;
        rdfsModelMap = new RDFSModelMap();
        projectFile = new File(System.getProperty("user.dir"), Translator.getString("Menu.File.New.Text"));

        classEditor = new ClassEditor(gmanager);
        classEditor.setBackground(Color.WHITE);
        propertyEditor = new PropertyEditor(gmanager);
        propertyEditor.setBackground(Color.WHITE);
        instanceEditor = new InstanceEditor(gmanager);
        instanceEditor.setBackground(Color.WHITE);
        registerComponent();

        instanceEditorFrame = createEditorFrame(instanceEditor, Translator.getString("InstanceEditor"),
                Utilities.getSVGIcon(Translator.getString("InstanceEditor.Icon")));

        classEditorFrame = createEditorFrame(classEditor, Translator.getString("ClassEditor.Title"),
                Utilities.getSVGIcon(Translator.getString("ClassEditor.Icon")));

        propertyEditorFrame = createEditorFrame(propertyEditor, Translator.getString("PropertyEditor.Title"),
                Utilities.getSVGIcon(Translator.getString("PropertyEditor.Icon")));

        desktopPane = new JDesktopPane();
        desktopPane.add(instanceEditorFrame);
        desktopPane.add(classEditorFrame);
        desktopPane.add(propertyEditorFrame);

        setLayout(new BorderLayout());
        add(desktopPane, BorderLayout.CENTER);
    }

    public GraphManager getGraphManager() {
        return gmanager;
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

    public void displayEditorInFront(GraphType graphType) {
        try {
            if (graphType == GraphType.Instance) {
                if (!instanceEditorFrame.isSelected()) {
                    instanceEditorFrame.setSelected(true);
                    instanceEditor.getGraph().requestFocusInWindow();
                }
            } else if (graphType == GraphType.Class) {
                if (!classEditorFrame.isSelected()) {
                    classEditorFrame.setSelected(true);
                    classEditor.getGraph().requestFocusInWindow();
                }
            } else if (graphType == GraphType.Property) {
                if (!propertyEditorFrame.isSelected()) {
                    propertyEditorFrame.setSelected(true);
                    propertyEditor.getGraph().requestFocusInWindow();
                }
            }
        } catch (PropertyVetoException e) {
            e.printStackTrace();
        }
    }

    public void arrangeWindowsCPI() {
        int desktopPaneWidth = desktopPane.getWidth();
        int desktopPaneHeight = desktopPane.getHeight();
        try {
            classEditorFrame.setIcon(false);
            propertyEditorFrame.setIcon(false);
            instanceEditorFrame.setIcon(false);
        } catch (PropertyVetoException e) {
            e.printStackTrace();
        }
        classEditorFrame.setSize(MARGIN + desktopPaneWidth / 2, MARGIN + desktopPaneHeight / 2);
        classEditorFrame.setLocation(new Point(0, 0));
        propertyEditorFrame.setSize(MARGIN + desktopPaneWidth / 2, MARGIN + desktopPaneHeight / 2);
        propertyEditorFrame.setLocation(new Point(desktopPaneWidth / 2, 0));
        instanceEditorFrame.setSize(MARGIN + desktopPaneWidth, MARGIN + desktopPaneHeight / 2);
        instanceEditorFrame.setLocation(new Point(0, desktopPaneHeight / 2));
    }

    public void arrangeWindowsCI() {
        int desktopPaneWidth = desktopPane.getWidth();
        int desktopPaneHeight = desktopPane.getHeight();
        try {
            classEditorFrame.setIcon(false);
            instanceEditorFrame.setIcon(false);
        } catch (PropertyVetoException e) {
            e.printStackTrace();
        }
        classEditorFrame.setSize(desktopPaneWidth, MARGIN + desktopPaneHeight / 2);
        classEditorFrame.setLocation(new Point(0, 0));
        instanceEditorFrame.setSize(desktopPaneWidth, MARGIN + desktopPaneHeight / 2);
        instanceEditorFrame.setLocation(new Point(0, desktopPaneHeight / 2));
        propertyEditorFrame.toBack();
    }

    public void arrangeWindowsPI() {
        int desktopPaneWidth = desktopPane.getWidth();
        int desktopPaneHeight = desktopPane.getHeight();
        try {
            propertyEditorFrame.setIcon(false);
            instanceEditorFrame.setIcon(false);
        } catch (PropertyVetoException e) {
            e.printStackTrace();
        }
        propertyEditorFrame.setSize(desktopPaneWidth, MARGIN + desktopPaneHeight / 2);
        propertyEditorFrame.setLocation(new Point(0, 0));
        instanceEditorFrame.setSize(desktopPaneWidth, MARGIN + desktopPaneHeight / 2);
        instanceEditorFrame.setLocation(new Point(0, desktopPaneHeight / 2));
        classEditorFrame.toBack();
    }

    private void registerComponent() {
        ToolTipManager.sharedInstance().registerComponent(instanceEditor.getGraph());
        ToolTipManager.sharedInstance().registerComponent(classEditor.getGraph());
        ToolTipManager.sharedInstance().registerComponent(propertyEditor.getGraph());
    }

    public GraphType getFocusedEditorType() {
        if (desktopPane.getSelectedFrame() == null) {
            return GraphType.Instance;
        }
        if (desktopPane.getSelectedFrame().equals(instanceEditorFrame)) {
            return GraphType.Instance;
        } else if (desktopPane.getSelectedFrame().equals(classEditorFrame)) {
            return GraphType.Class;
        } else if (desktopPane.getSelectedFrame().equals(propertyEditorFrame)) {
            return GraphType.Property;
        }
        return GraphType.Instance;
    }

    public void resetEditors() {
        ((RDFGraph) instanceEditor.getGraph()).removeAllCells();
        ((RDFGraph) classEditor.getGraph()).removeAllCells();
        ((RDFGraph) propertyEditor.getGraph()).removeAllCells();
    }

    public InstanceEditor getInstanceEditor() {
        return instanceEditor;
    }

    public ClassEditor getClassEditor() {
        return classEditor;
    }

    public PropertyEditor getPropertyEditor() {
        return propertyEditor;
    }

    public File getProjectFile() {
        return projectFile;
    }

    public void setProjectFile(File file) {
        projectFile = file;
    }

    public RDFSModelMap getRDFSInfoMap() {
        return rdfsModelMap;
    }

    public String getTitle() {
        if (projectFile == null) {
            return Translator.getString("Menu.File.New.Text");
        }
        return projectFile.getName();
    }

}
