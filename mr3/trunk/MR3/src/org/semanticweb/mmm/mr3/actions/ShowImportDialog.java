/*
 * @(#) ShowImportDialog 2004/02/11
 *
 *
 * Copyright (C) 2003 The MMM Project
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 */

package org.semanticweb.mmm.mr3.actions;

import java.awt.event.*;

import javax.swing.*;

import org.semanticweb.mmm.mr3.*;
import org.semanticweb.mmm.mr3.ui.*;
import org.semanticweb.mmm.mr3.util.*;

/**
 * @author takeshi morita
 */
public class ShowImportDialog extends MR3AbstractAction {

    private static ImageIcon ICON = Utilities.getImageIcon(Translator.getString("ImportDialog.Icon"));

    public ShowImportDialog(MR3 mr3, String title) {
        super(mr3, title, ICON);
        setValues();
    }

    private void setValues() {
        putValue(SHORT_DESCRIPTION, getName());
        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_I, KeyEvent.CTRL_MASK));
    }

    public void actionPerformed(ActionEvent e) {
        if (MR3.getCurrentProject() == null) {
            mr3.newProject(mr3.getGraphManager().getBaseURI());
        }
        ImportDialog importDialog = mr3.getImportDialog();
        if (importDialog != null) {
            mr3.getImportDialog().setVisible(true);
        }
    }
}
