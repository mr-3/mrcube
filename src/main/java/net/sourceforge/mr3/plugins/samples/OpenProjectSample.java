/*
 * @(#) SamplePlugin3.java
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

package net.sourceforge.mr3.plugins.samples;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.swing.JFileChooser;

import net.sourceforge.mr3.plugin.MR3Plugin;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

/**
 * @author takeshi morita
 * 
 *         ProjectFileを読み込むプラグイン
 */
public class OpenProjectSample extends MR3Plugin {

	public void exec() {
		try {
			Model model = ModelFactory.createDefaultModel();
			InputStream is = getInputStream();
			if (is != null) {
				model.read(is, getBaseURI(), "RDF/XML");
				replaceProjectModel(model);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private InputStream getInputStream() throws IOException {
		JFileChooser jfc = new JFileChooser();
		if (jfc.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
			return new BufferedInputStream(new FileInputStream(jfc.getSelectedFile()));
		}
		return null;
	}
}
