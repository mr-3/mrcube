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

import java.awt.event.*;

import javax.swing.*;

import net.sourceforge.mr3.data.MR3Constants.*;
import net.sourceforge.mr3.jgraph.*;
import net.sourceforge.mr3.util.*;

/**
 * @author Takeshi Morita
 */
public class GraphLayoutAction extends AbstractAction {

	private GraphType graphType;
	private GraphManager gmanager;
	public static final ImageIcon layoutRDFGraphIcon = Utilities
			.getImageIcon("layout_rdf_graph.png");
	public static final ImageIcon layoutClassGraphIcon = Utilities
			.getImageIcon("layout_class_graph.png");
	public static final ImageIcon layoutPropertyGraphIcon = Utilities
			.getImageIcon("layout_property_graph.png");

	public GraphLayoutAction(GraphManager gm, GraphType type, ImageIcon icon) {
		super(getString(type), icon);
		gmanager = gm;
		graphType = type;
		putValue(SHORT_DESCRIPTION, type.toString() + "Graph Layout");
	}

	private static String getString(GraphType type) {
		if (type == GraphType.CLASS) {
			return Translator.getString("Class");
		} else if (type == GraphType.PROPERTY) {
			return Translator.getString("Property");
		} else {
			return type.toString();
		}
	}

	public void actionPerformed(ActionEvent arg0) {
		gmanager.applyLayout(graphType);
	}
}
