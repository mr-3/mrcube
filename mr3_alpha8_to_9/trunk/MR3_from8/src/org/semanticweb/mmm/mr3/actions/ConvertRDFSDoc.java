/*
 * @(#) ConvertRDFSDoc.java
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
import java.io.*;

import org.semanticweb.mmm.mr3.*;
import org.semanticweb.mmm.mr3.util.*;

import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.rdf.model.impl.*;

/**
 * @author takeshi morita
 *
 */
public class ConvertRDFSDoc extends AbstractActionFile {
	
	public static final String RDFS = Translator.getString("Component.Convert.RDFS/XML.RDFS(Class/Property).Text");
	public static final String SELECTED_RDFS = Translator.getString("Component.Convert.RDFS/XML.SelectedRDFS(Class/Property).Text");
	
	public ConvertRDFSDoc(MR3 mr3, String title) {
		super(mr3, title);
	}

	public void actionPerformed(ActionEvent e) {
		String command = e.getActionCommand();
		try {
			Model model = null;
			if (command.equals(RDFS)) {
				model = mr3.getRDFSModel();
			} else {
				model = mr3.getSelectedRDFSModel();
			}

			Writer output = new StringWriter();
			RDFWriter writer = new RDFWriterFImpl().getWriter("RDF/XML-ABBREV");
			writeModel(model, output, writer);
			mr3.getSourceArea().setText(output.toString());
			showSrcView();
		} catch (RDFException rex) {
			rex.printStackTrace();
		}
	}
}
