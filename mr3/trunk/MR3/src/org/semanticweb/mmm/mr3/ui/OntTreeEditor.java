/*
 * @(#) 2004/05/07
 * 
 * 
 * Copyright (C) 2003 The MMM Project
 * 
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
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
public class OntTreeEditor extends JInternalFrame {

    private MR3TreePanel classTreePanel;
    private MR3TreePanel propertyTreePanel;

    public OntTreeEditor(GraphManager gm) {
        super("OntTreeEditor", true, false, true, true);
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