/*
 * Project Name: MR^3 (Meta-Model Management based on RDFs Revision Reflection)
 * Project Website: http://mr3.sourceforge.net/
 * 
 * Copyright (C) 2003-2008 Yamaguchi Laboratory, Keio University. All rights reserved. 
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

package org.semanticweb.mmm.mr3.ui;

import java.awt.*;
import java.awt.Container;

import javax.swing.*;

import org.semanticweb.mmm.mr3.data.*;
import org.semanticweb.mmm.mr3.jgraph.*;

import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.vocabulary.*;

/**
 * @author takeshi morita
 */
public class OntTreeEditor extends JDialog {

    private MR3TreePanel classTreePanel;
    private MR3TreePanel propertyTreePanel;

    public OntTreeEditor(GraphManager gm) {
        super(gm.getRootFrame(), "OntTreeEditor");
        classTreePanel = new MR3TreePanel(gm);
        classTreePanel.setClassTreeCellRenderer();
        propertyTreePanel = new MR3TreePanel(gm);
        propertyTreePanel.setPropertyTreeCellRenderer();

        Container contentPane = getContentPane();
        contentPane.setLayout(new GridLayout(1, 2));
        contentPane.add(classTreePanel);
        contentPane.add(propertyTreePanel);
    }

    public void setClassTreeRoot(Model model) {
        classTreePanel.setRDFSTreeRoot(model, RDFS.Resource, RDFS.subClassOf);
    }

    public void setPropertyTreeRoot(Model model) {
        propertyTreePanel.setRDFSTreeRoot(model, MR3Resource.Property, RDFS.subPropertyOf);
    }

}