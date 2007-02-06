/*
 * @(#) PreferenceAction.java 2003/12/01
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

import java.awt.event.*;

import javax.swing.*;

import org.semanticweb.mmm.mr3.*;
import org.semanticweb.mmm.mr3.util.*;

/**
 * @author takeshi morita
 */
public class ShowOptionDialog extends MR3AbstractAction {

    public ShowOptionDialog(MR3 mr3) {
        super(mr3, Translator.getString("Component.Tools.Options.Text"));
        putValue(SHORT_DESCRIPTION, getName());
        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_O, KeyEvent.ALT_MASK));
    }

    public void actionPerformed(ActionEvent e) {
        mr3.showOptionDialog();        
    }
}
