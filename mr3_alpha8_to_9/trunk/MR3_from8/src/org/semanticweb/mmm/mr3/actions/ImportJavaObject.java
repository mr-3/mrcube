/*
 * @(#) ImportJavaObject.java
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
import java.util.*;
import java.util.zip.*;

import org.semanticweb.mmm.mr3.*;
import org.semanticweb.mmm.mr3.util.*;

/**
 * @author takeshi morita
 *
 */
public class ImportJavaObject extends AbstractActionFile {

	private static final String PROJECT = Translator.getString("Component.File.Import.Project.Text");

	public ImportJavaObject(MR3 mr3) {
		super(mr3, PROJECT);
	}

	private ObjectInputStream createInputStream(File file) throws FileNotFoundException, IOException {
		InputStream fi = new FileInputStream(file);
		fi = new GZIPInputStream(fi);
		return new ObjectInputStream(fi);
	}

	public void openProject(File file) {
		try {
			mr3.newProject();
			ObjectInputStream oi = createInputStream(file);
			Object obj = oi.readObject();
			if (obj instanceof ArrayList) {
				ArrayList list = (ArrayList) obj;
				int index = mr3.getGraphManager().loadState(list);
				mr3.getNSTableDialog().loadState((List) list.get(index));
			}
			oi.close();
			mr3.setTitle("MR^3 -(Java Object)- " + file.getAbsolutePath());
			mr3.setCurrentProject(file);
		} catch (IOException ex) {
			ex.printStackTrace();
		} catch (ClassNotFoundException cnfe) {
			cnfe.printStackTrace();
		}
	}

	public void actionPerformed(ActionEvent e) {
		File file = getFile(true, "mr3");
		if (file == null) {
			return;
		}
		openProject(file);
	}

}
