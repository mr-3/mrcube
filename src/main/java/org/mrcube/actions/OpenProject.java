/*
 * Project Name: MR^3 (Meta-Model Management based on RDFs Revision Reflection)
 * Project Website: http://mr3.sourceforge.net/
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
import org.mrcube.MR3;
import org.mrcube.utils.Translator;
import org.mrcube.utils.Utilities;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

/**
 * @author Takeshi Morita
 * 
 */
public class OpenProject extends AbstractActionFile {

    private static final String TITLE = Translator.getString("Component.File.OpenProject.Text");
    private static final ImageIcon ICON = Utilities.getImageIcon(Translator
            .getString("Component.File.OpenProject.Icon"));

    public OpenProject(MR3 mr3) {
        super(mr3, TITLE, ICON);
        setValues();
    }

    private void setValues() {
        putValue(SHORT_DESCRIPTION, TITLE);
        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_O, KeyEvent.CTRL_MASK));
    }

    public void actionPerformed(ActionEvent e) {
        if (MR3.getCurrentProject() == null) {
            mr3.newProject(null);
        } else if (!isNewProjectFile(MR3.getCurrentProject())) {
            confirmExitProject(mr3.getGraphManager().getRootFrame(), MR3.getCurrentProject());
        }
        Model model = readModel(getInputStream("mr3"), mr3.getGraphManager().getBaseURI(), "RDF/XML");
        mr3.getMR3Reader().replaceProjectModel(model);
        mr3.setTitle("MR^3: " + MR3.getCurrentProject().getTitle());
    }
}