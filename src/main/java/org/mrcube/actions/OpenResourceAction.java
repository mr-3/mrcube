/*
 * Project Name: MR^3 (Meta-Model Management based on RDFs Revision Reflection)
 * Project Website: http://mrcube.org/
 *
 * Copyright (C) 2003-2019 Yamaguchi Laboratory, Keio University. All rights reserved.
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
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.mrcube.MR3;
import org.mrcube.io.MR3Reader;
import org.mrcube.utils.Translator;
import org.mrcube.utils.Utilities;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class OpenResourceAction extends AbstractActionFile {

    private MR3Reader mr3Reader;
    public static final String TITLE = Translator.getString("Menu.File.Open.Text");
    public static final ImageIcon ICON = Utilities.getImageIcon("baseline_open_in_browser_black_18dp.png");

    public OpenResourceAction(MR3 mr3) {
        super(mr3, TITLE, ICON);
        setValues();
        mr3Reader = new MR3Reader(mr3.getGraphManager());
        initializeJFileChooser();
    }

    protected void setValues() {
        putValue(SHORT_DESCRIPTION, TITLE);
    }

    public void openResource(String resourcePath) {
        try {
            if (resourcePath.endsWith("mr3")) {
                Model model = RDFDataMgr.loadModel(resourcePath, Lang.TURTLE);
                mr3.getMR3Reader().replaceProjectModel(model);
                mr3.setTitle("MR^3: " + MR3.getProjectPanel().getTitle());
            } else {
                Model model = RDFDataMgr.loadModel(resourcePath);
                mr3Reader.mergeRDFandRDFSModel(model);
            }
        } catch (Exception e) {
            Utilities.showErrorMessageDialog(Translator.getString("Warning.Message11"));
        }
    }

    public void actionPerformed(ActionEvent e) {
        var message = confirmExitProject();
        if (message != JOptionPane.CANCEL_OPTION) {
            mr3.newProject();
            openResource(MR3.ResourcePathTextField.getText());
        }
    }
}
