/*
 * @(#) MR3FileFilter.java
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

package org.semanticweb.mmm.mr3.util;

import java.io.*;

import javax.swing.filechooser.FileFilter;

/**
 * @author takeshi morita
 */
public class ProjectFileFilter extends MR3FileFilter {

    public String getExtension() {
        return "mr3";
    }

    public boolean accept(File f) {
        if (f.isDirectory()) { return true; }
        String extension = getExtension(f);
        if (extension != null && extension.equals("mr3")) { return true; }
        return false;
    }

    public String getDescription() {
        return "MR3 Project File (*.mr3)";
    }
}
