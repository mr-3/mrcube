/*
 * @(#) ExportJavaObject.java
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
public class ExportJavaObject extends AbstractActionFile {
	
	private static final String PROJECT = Translator.getString("Component.File.Export.Project.Text");

	public ExportJavaObject(MR3 mr3) {
		super(mr3, PROJECT);
	}

	private ObjectOutputStream createOutputStream(File file) throws FileNotFoundException, IOException {
		OutputStream fo = new FileOutputStream(file);
		fo = new GZIPOutputStream(fo);
		return new ObjectOutputStream(fo);
	}

	public void actionPerformed(ActionEvent e) {
		File file = getFile(false, "mr3");
		if (file == null) {
			return;
		}
		try {
			ObjectOutputStream oo = createOutputStream(file);
			ArrayList list = mr3.getGraphManager().storeState();
			list.add(mr3.getNSTableDialog().getState());
			oo.writeObject(list);
			oo.flush();
			oo.close();
			mr3.setTitle("MR^3 -(Java Object)- " + file.getAbsolutePath());
			mr3.setCurrentProject(file);
		} catch (FileNotFoundException fne) {
			fne.printStackTrace();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}
}
