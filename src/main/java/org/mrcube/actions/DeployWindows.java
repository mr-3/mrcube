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

package org.mrcube.actions;

import org.mrcube.MR3;
import org.mrcube.views.MR3ProjectPanel;
import org.mrcube.models.MR3Constants.DeployType;

import javax.swing.*;
import java.awt.event.ActionEvent;

/**
 * @author Takeshi Morita
 */
public class DeployWindows extends MR3AbstractAction {

    private final DeployType deployType;

    public DeployWindows(MR3 mr3, String title, ImageIcon icon, DeployType type, KeyStroke keyStroke) {
        super(mr3, title, icon);
        deployType = type;
        putValue(SHORT_DESCRIPTION, title);
        putValue(ACCELERATOR_KEY, keyStroke);
    }

    public void actionPerformed(ActionEvent e) {
        MR3ProjectPanel project = MR3.getCurrentProject();
        if (project == null) {
            return;
        }
        switch (deployType) {
            case CPR:
                project.deployCPR();
                break;
            case CR:
                project.deployCR();
                break;
            case PR:
                project.deployPR();
                break;
        }
    }

}
