/*
 * Created on 2003/06/07
 *
 */
package org.semanticweb.mmm.mr3.util;

import java.io.*;

import javax.swing.filechooser.FileFilter;

/**
 * @author takeshi morita
 */
public class RDFsFileFilter extends FileFilter {

	public String getExtension(File f) {
		String ext = null;
		String s = f.getName();
		int i = s.lastIndexOf('.');

		if (i > 0 && i < s.length() - 1) {
			ext = s.substring(i + 1).toLowerCase();
		}
		return ext;
	}

	public boolean accept(File f) {
		if (f.isDirectory()) {
			return true;
		}

		String extension = getExtension(f);
		if (extension != null) {
			if (extension.equals("rdf") || extension.equals("rdfs")) {
				return true;
			} else {
				return false;
			}
		}

		return false;
	}

	public String getDescription() {
		return "RDF(S) (*.rdf, *.rdfs)";
	}
}
