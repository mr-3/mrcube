/*
 * @(#) ConvertNTriple.java
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
public class ConvertNTriple extends AbstractActionFile {

	public static final String RDF = Translator.getString("Component.Convert.RDF/N-Triple.RDF.Text");
	public static final String SELECTED_RDF = Translator.getString("Component.Convert.RDF/N-Triple.SelectedRDF.Text");

	public ConvertNTriple(MR3 mr3, String name) {
		super(mr3, name);
	}

	public void convertNTripleSRC(boolean isSelected) {
		Model model = null;
		if (isSelected) {
			model = mr3.getSelectedRDFModel();
		} else {
			model = mr3.getRDFModel();
		}
		Writer output = new StringWriter();
		RDFWriter writer = new NTripleWriter();
		writeModel(model, output, writer);
		mr3.getSourceArea().setText(output.toString());
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals(RDF)) {
			convertNTripleSRC(false);
		} else {
			convertNTripleSRC(true);
		}

		showSrcView();
	}
}
