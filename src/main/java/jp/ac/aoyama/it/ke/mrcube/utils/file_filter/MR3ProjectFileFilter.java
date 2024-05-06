/*
 * Project Name: MR^3 (Meta-Model Management based on RDFs Revision Reflection)
 * Project Website: https://mr-3.github.io/
 * 
 * Copyright (C) 2003-2024 Takeshi Morita. All rights reserved.
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

package jp.ac.aoyama.it.ke.mrcube.utils.file_filter;

import java.io.File;

/**
 * @author Takeshi Morita
 */
public class MR3ProjectFileFilter extends MR3FileFilter {

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
        return "MR^3 Project (*.mr3)";
    }
}
