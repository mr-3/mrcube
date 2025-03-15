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

package jp.ac.aoyama.it.ke.mrcube.actions;

import jp.ac.aoyama.it.ke.mrcube.MR3;
import jp.ac.aoyama.it.ke.mrcube.views.MR3ProjectPanel;
import jp.ac.aoyama.it.ke.mrcube.models.MR3Constants.ArrangeWindowsType;

import javax.swing.*;
import java.awt.event.ActionEvent;

/**
 * @author Takeshi Morita
 */
public class ArrangeWindows extends MR3AbstractAction {

    private final ArrangeWindowsType arrangeWindowsType;

    public ArrangeWindows(MR3 mr3, String title, ArrangeWindowsType type, KeyStroke keyStroke) {
        super(mr3, title);
        arrangeWindowsType = type;
        putValue(SHORT_DESCRIPTION, title);
        putValue(ACCELERATOR_KEY, keyStroke);
    }

    public void actionPerformed(ActionEvent e) {
        MR3ProjectPanel project = MR3.getProjectPanel();
        if (project == null) {
            return;
        }
        switch (arrangeWindowsType) {
            case CPI -> project.arrangeWindowsCPI();
            case CI -> project.arrangeWindowsCI();
            case PI -> project.arrangeWindowsPI();
        }
    }

}
