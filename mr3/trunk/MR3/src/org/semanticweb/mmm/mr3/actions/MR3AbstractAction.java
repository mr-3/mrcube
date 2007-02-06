/*
 * @(#) MR3AbstractAction.java
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
