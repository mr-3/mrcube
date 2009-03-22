/*
 * @(#)  2008/03/07
 */

package org.semanticweb.mmm.mr3;

import java.awt.*;
import java.io.*;

import javax.swing.*;

import net.infonode.docking.*;
import net.infonode.docking.properties.*;
import net.infonode.docking.theme.*;
import net.infonode.docking.util.*;
import net.infonode.util.*;

import org.semanticweb.mmm.mr3.data.*;
import org.semanticweb.mmm.mr3.data.MR3Constants.*;
import org.semanticweb.mmm.mr3.editor.*;
import org.semanticweb.mmm.mr3.jgraph.*;
import org.semanticweb.mmm.mr3.ui.*;
import org.semanticweb.mmm.mr3.util.*;

/**
 * @author Takeshi Morita
 */
public class MR3Project extends JPanel {

    private View[] mainViews;
    private RootWindow rootWindow;

    private File currentProjectFile;
    private RDFSInfoMap rdfsInfoMap;

    private RDFEditor rdfEditor;
    private ClassEditor classEditor;
    private PropertyEditor propertyEditor;

    private TabComponent tabComponent;

    public MR3Project(GraphManager gmanager, String basePath, Color color, TabComponent tabComp) {
        rdfsInfoMap = new RDFSInfoMap();
        tabComponent = tabComp;
        currentProjectFile = new File(basePath, Translator.getString("Component.File.NewProject.Text"));
        mainViews = new View[3];
        ViewMap viewMap = new ViewMap();

        classEditor = new ClassEditor(gmanager);
        classEditor.setBackground(color);
        propertyEditor = new PropertyEditor(gmanager);
        propertyEditor.setBackground(color);
        rdfEditor = new RDFEditor(gmanager);
        rdfEditor.setBackground(color);
        registerComponent();

        mainViews[0] = new View(Translator.getString("ClassEditor.Title"), Utilities.getImageIcon(Translator
                .getString("ClassEditor.Icon")), classEditor);
        mainViews[0].getWindowProperties().setUndockEnabled(false);
        mainViews[0].getWindowProperties().setCloseEnabled(false);
        mainViews[1] = new View(Translator.getString("PropertyEditor.Title"), Utilities.getImageIcon(Translator
                .getString("PropertyEditor.Icon")), propertyEditor);
        mainViews[1].getWindowProperties().setUndockEnabled(false);
        mainViews[1].getWindowProperties().setCloseEnabled(false);
        mainViews[2] = new View(Translator.getString("RDFEditor.Title"), Utilities.getImageIcon(Translator
                .getString("RDFEditor.Icon")), rdfEditor);
        mainViews[2].getWindowProperties().setUndockEnabled(false);
        mainViews[2].getWindowProperties().setCloseEnabled(false);
        for (int i = 0; i < mainViews.length; i++) {
            viewMap.addView(i, mainViews[i]);
        }
        rootWindow = createRootWindow(viewMap);
        deployCPR();

        setLayout(new BorderLayout());
        add(rootWindow, BorderLayout.CENTER);
    }

    public TabComponent getTabComponent() {
        return tabComponent;
    }

    public void frontEditor(GraphType graphType) {
        if (graphType == GraphType.RDF) {
            mainViews[2].restoreFocus();
        } else if (graphType == GraphType.CLASS) {
            mainViews[0].restoreFocus();
        } else if (graphType == GraphType.PROPERTY) {
            mainViews[1].restoreFocus();
        }
    }

    public void deployCPR() {
        SplitWindow sw1 = new SplitWindow(true, 0.5f, mainViews[0], mainViews[1]);
        SplitWindow sw2 = new SplitWindow(false, 0.5f, sw1, mainViews[2]);
        rootWindow.setWindow(sw2);
    }

    public void deployCR() {
        deployCPR();
        mainViews[1].minimize();
    }

    public void deployPR() {
        deployCPR();
        mainViews[0].minimize();
    }

    public void registerComponent() {
        ToolTipManager.sharedInstance().registerComponent(rdfEditor.getGraph());
        ToolTipManager.sharedInstance().registerComponent(classEditor.getGraph());
        ToolTipManager.sharedInstance().registerComponent(propertyEditor.getGraph());
    }

    public GraphType getFocusedEditorType() {
        if (rootWindow.getFocusedView() == mainViews[0]) {
            return GraphType.CLASS;
        } else if (rootWindow.getFocusedView() == mainViews[1]) {
            return GraphType.PROPERTY;
        } else if (rootWindow.getFocusedView() == mainViews[2]) { return GraphType.RDF; }
        return GraphType.RDF;
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

    public RDFSInfoMap getRDFSInfoMap() {
        return rdfsInfoMap;
    }

    public String getTitle() {
        if (currentProjectFile == null) { return Translator.getString("Component.File.NewProject.Text"); }
        return currentProjectFile.getAbsolutePath();
    }

    private static RootWindow createRootWindow(ViewMap viewMap) {
        RootWindow rootWindow = DockingUtil.createRootWindow(viewMap, true);
        rootWindow.getWindowBar(Direction.DOWN).setEnabled(true);
        RootWindowProperties properties = new RootWindowProperties();
        DockingWindowsTheme currentTheme = new ShapedGradientDockingTheme();
        properties.addSuperObject(currentTheme.getRootWindowProperties());
        RootWindowProperties titleBarStyleProperties = PropertiesUtil.createTitleBarStyleRootWindowProperties();
        properties.addSuperObject(titleBarStyleProperties);
        rootWindow.getRootWindowProperties().addSuperObject(properties);
        return rootWindow;
    }

}
