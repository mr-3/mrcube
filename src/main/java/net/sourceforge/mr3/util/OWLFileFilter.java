/*
 * Project Name: MR^3 (Meta-Model Management based on RDFs Revision Reflection)
 * Project Website: http://mr3.sourceforge.net/
 * 
 * Copyright (C) 2003-2015 Yamaguchi Laboratory, Keio University. All rights reserved. 
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

package net.sourceforge.mr3.util;

import java.io.*;

/**
 * @author Takeshi Morita
 */
public class OWLFileFilter extends MR3FileFilter implements java.io.FileFilter {

    public String getExtension() {
        return "owl";
    }

    public boolean accept(File f) {
        if (f.isDirectory()) { return true; }
        String extension = getExtension(f);
        if (extension != null && extension.equals("owl")) { return true; }
        return false;
    }

    public String getDescription() {
        return "OWL File (*.owl)";
    }
}
