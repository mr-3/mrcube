/*
 * @(#) MR3WebStart 2004/02/05
 * 
 * 
 * Copyright (C) 2003-2005 The MMM Project
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

package org.semanticweb.mmm.mr3.webstart;

import javax.swing.*;

import org.semanticweb.mmm.mr3.*;
import org.semanticweb.mmm.mr3.data.*;
import org.semanticweb.mmm.mr3.ui.*;

/**
 * @author takeshi morita
 */
public class MR3WebStart {
    public static void main(String[] arg) {
        MR3.initialize(MR3WebStart.class);
        JWindow splashWindow = new HelpWindow(null, MR3Constants.LOGO);
        try {
            new MR3();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            splashWindow.dispose();
        }
    }
}
