/*
 * @(#) ExportRDF.java
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
import java.util.prefs.*;

import org.semanticweb.mmm.mr3.*;
import org.semanticweb.mmm.mr3.data.*;
import org.semanticweb.mmm.mr3.util.*;

import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.rdf.model.impl.*;

/**
 * @author takeshi morita
 *
 */
public class ExportRDF extends AbstractActionFile {

	public static final String RDF_XML = Translator.getString("Component.File.Export.RDF/XML.RDF.Text");
	public static final String RDF_NTriple = Translator.getString("Component.File.Export.N-Triple.RDF.Text");
	public static final String SelectedRDF_XML = Translator.getString("Component.File.Export.RDF/XML.SelectedRDF.Text");
	public static final String SelectedRDF_NTriple = Translator.getString("Component.File.Export.N-Triple.SelectedRDF.Text");

	public ExportRDF(MR3 mr3, String title) {
		super(mr3, title);
	}

	public void actionPerformed(ActionEvent e) {
		Preferences userPrefs = mr3.getUserPrefs();
		String type = getName();
		String ext = "rdf";
		if (type.equals(RDF_NTriple) || type.equals(SelectedRDF_NTriple)) {
			ext = "n3";
		}
		File file = getFile(false, ext);
		if (file == null) {
			return;
		}
		try {
			String encoding = userPrefs.get(PrefConstants.OutputEncoding, "EUC_JP");
			Writer output = new OutputStreamWriter(new FileOutputStream(file), encoding);
			RDFWriter writer = new RDFWriterFImpl().getWriter("RDF/XML-ABBREV");
			if (type.equals(RDF_NTriple) || type.equals(SelectedRDF_NTriple)) {
				writer = new RDFWriterFImpl().getWriter("N-TRIPLE");
			}
			if (type.equals(SelectedRDF_XML) || type.equals(SelectedRDF_NTriple)) {
				writeModel(mr3.getSelectedRDFModel(), output, writer);
			} else {
				writeModel(mr3.getRDFModel(), output, writer);
			}
			output.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}
