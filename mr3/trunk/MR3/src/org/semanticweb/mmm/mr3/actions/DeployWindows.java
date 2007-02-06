/*
 * @(#) DeployAction.java
 * 
 * 
 * Copyright (C) 2003 The MMM Project
 * 
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation; either version 2.1 of the License, or (at your
 * option) any later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *  
 */

package org.semanticweb.mmm.mr3.actions;

import java.awt.*;
import java.awt.event.*;
import java.beans.*;

import javax.swing.*;

import org.semanticweb.mmm.mr3.*;
import org.semanticweb.mmm.mr3.data.MR3Constants.*;
import static org.semanticweb.mmm.mr3.data.MR3Constants.DeployType.CPR;
import static org.semanticweb.mmm.mr3.data.MR3Constants.DeployType.CR;
import static org.semanticweb.mmm.mr3.data.MR3Constants.DeployType.PR;

/**
 * @author takeshi morita
 * 
 */
public class DeployWindows extends MR3AbstractAction {

    private DeployType deployType;
    private Component desktop;
    private JInternalFrame[] internalFrames;

    public DeployWindows(MR3 mr3, String title, ImageIcon icon, DeployType type, String shortCut) {
        super(mr3, title, icon);
        deployType = type;
        internalFrames = mr3.getInternalFrames();
        putValue(SHORT_DESCRIPTION, title);
        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(shortCut));
    }

    private void toFrontFrames(int num) {
        try {
            internalFrames[num].setIcon(false);
            internalFrames[num].toFront();
        } catch (PropertyVetoException pve) {
            pve.printStackTrace();
        }
    }

    private void deployCPR() {
        int width = desktop.getWidth();
        int height = desktop.getHeight();
        internalFrames[0].setBounds(new Rectangle(0, height / 2, width, height / 2)); // RDF
        internalFrames[1].setBounds(new Rectangle(0, 0, width / 2, height / 2)); // Class
        internalFrames[2].setBounds(new Rectangle(width / 2, 0, width / 2, height / 2)); // Property
        for (int i = 0; i < 3; i++) {
            toFrontFrames(i);
        }
    }

    private void deployCR() {
        int width = desktop.getWidth();
        int height = desktop.getHeight();
        internalFrames[0].setBounds(new Rectangle(0, height / 2, width, height / 2)); // RDF
        toFrontFrames(0);
        internalFrames[1].setBounds(new Rectangle(0, 0, width, height / 2)); // Class
        toFrontFrames(1);
    }

    private void deployPR() {
        int width = desktop.getWidth();
        int height = desktop.getHeight();
        internalFrames[0].setBounds(new Rectangle(0, height / 2, width, height / 2)); // RDF
        toFrontFrames(0);
        internalFrames[2].setBounds(new Rectangle(0, 0, width, height / 2)); // Property
        toFrontFrames(2);
    }

    public void actionPerformed(ActionEvent e) {
        desktop = mr3.getDesktopPane();
        if (!mr3.isActiveFrames()) { return; }

        switch (deployType) {
        case CPR:
            deployCPR();
            break;
        case CR:
            deployCR();
            break;
        case PR:
            deployPR();
            break;
        }
    }

}
