/*
 * Project Name: MR^3 (Meta-Model Management based on RDFs Revision Reflection)
 * Project Website: http://mr3.sourceforge.net/
 * 
 * Copyright (C) 2003-2008 Yamaguchi Laboratory, Keio University. All rights reserved. 
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

package org.semanticweb.mmm.mr3.actions;

import javax.swing.*;

import org.semanticweb.mmm.mr3.*;

/**
 * @author takeshi morita
 * 
 */
public abstract class MR3AbstractAction extends AbstractAction {

    protected MR3 mr3;

    public MR3AbstractAction() {

    }

    public MR3AbstractAction(MR3 mr3) {
        this.mr3 = mr3;
    }

    public MR3AbstractAction(String name) {
        super(name);
    }

    public MR3AbstractAction(String name, ImageIcon icon) {
        super(name, icon);
    }

    public MR3AbstractAction(MR3 mr3, String name) {
        super(name);
        this.mr3 = mr3;
    }

    public MR3AbstractAction(MR3 mr3, String name, ImageIcon icon) {
        super(name, icon);
        this.mr3 = mr3;
    }

    public String getName() {
        return (String) getValue(NAME);
    }

}
