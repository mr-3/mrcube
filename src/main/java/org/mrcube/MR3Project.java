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

package org.mrcube;

import net.infonode.docking.RootWindow;
import net.infonode.docking.SplitWindow;
import net.infonode.docking.View;
import net.infonode.docking.properties.RootWindowProperties;
import net.infonode.docking.theme.DockingWindowsTheme;
import net.infonode.docking.theme.ShapedGradientDockingTheme;
import net.infonode.docking.util.DockingUtil;
import net.infonode.docking.util.PropertiesUtil;
import net.infonode.docking.util.ViewMap;
import net.infonode.util.Direction;
import org.mrcube.editors.ClassEditor;
import org.mrcube.editors.PropertyEditor;
import org.mrcube.editors.RDFEditor;
import org.mrcube.jgraph.GraphManager;
import org.mrcube.models.MR3Constants.GraphType;
import org.mrcube.models.RDFSModelMap;
import org.mrcube.utils.Translator;
import org.mrcube.utils.Utilities;
import org.mrcube.views.TabComponent;

import javax.swing.*;
import java.awt.*;
import java.io.File;

/**
 * @author Takeshi Morita
 */
public class MR3Project extends JPanel {

	private View[] mainViews;
	private RootWindow rootWindow;

	private File currentProjectFile;
	private RDFSModelMap rdfsModelMap;

	private RDFEditor rdfEditor;
	private ClassEditor classEditor;
	private PropertyEditor propertyEditor;

	private TabComponent tabComponent;

	public MR3Project(GraphManager gmanager, String basePath, Color color, TabComponent tabComp) {
		rdfsModelMap = new RDFSModelMap();
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

		mainViews[0] = new View(Translator.getString("ClassEditor.Title"),
				Utilities.getImageIcon(Translator.getString("ClassEditor.Icon")), classEditor);
		mainViews[0].getWindowProperties().setUndockEnabled(false);
		mainViews[0].getWindowProperties().setCloseEnabled(false);
		mainViews[1] = new View(Translator.getString("PropertyEditor.Title"),
				Utilities.getImageIcon(Translator.getString("PropertyEditor.Icon")), propertyEditor);
		mainViews[1].getWindowProperties().setUndockEnabled(false);
		mainViews[1].getWindowProperties().setCloseEnabled(false);
		mainViews[2] = new View(Translator.getString("RDFEditor.Title"),
				Utilities.getImageIcon(Translator.getString("RDFEditor.Icon")), rdfEditor);
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
		} else if (rootWindow.getFocusedView() == mainViews[2]) {
			return GraphType.RDF;
		}
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

	public RDFSModelMap getRDFSInfoMap() {
		return rdfsModelMap;
	}

	public String getTitle() {
		if (currentProjectFile == null) {
			return Translator.getString("Component.File.NewProject.Text");
		}
		return currentProjectFile.getName();
	}

	private static RootWindow createRootWindow(ViewMap viewMap) {
		RootWindow rootWindow = DockingUtil.createRootWindow(viewMap, true);
		rootWindow.getWindowBar(Direction.DOWN).setEnabled(true);
		RootWindowProperties properties = new RootWindowProperties();
		DockingWindowsTheme currentTheme = new ShapedGradientDockingTheme();
		properties.addSuperObject(currentTheme.getRootWindowProperties());
		RootWindowProperties titleBarStyleProperties = PropertiesUtil
				.createTitleBarStyleRootWindowProperties();
		properties.addSuperObject(titleBarStyleProperties);
		rootWindow.getRootWindowProperties().addSuperObject(properties);
		return rootWindow;
	}

}
