/*
 * @(#) FileExportImg.java
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
import java.awt.image.*;
import java.io.*;

import javax.imageio.*;

import org.semanticweb.mmm.mr3.*;
import org.semanticweb.mmm.mr3.data.*;
import org.semanticweb.mmm.mr3.jgraph.*;
import org.semanticweb.mmm.mr3.util.*;

/**
 *
 * @author takeshi morita
 *
 */
public class FileExportImg extends AbstractActionFile {

	private String fileType;
	private GraphType graphType;

	public FileExportImg(MR3 mr3, GraphType gt, String ft, String title) {
		super(mr3, title);
		graphType = gt;
		fileType = ft;
	}

	public void actionPerformed(ActionEvent e) {
		GraphManager gmanager =mr3.getGraphManager(); 
		File file = getFile(false, "png");
		if (file == null) {
			return;
		}
		try {
			BufferedImage img = null;
			if (graphType == GraphType.RDF && gmanager.getRDFGraph().getModel().getRootCount() > 0) {
				img = GPConverter.toImage(gmanager.getRDFGraph());
			} else if (graphType == GraphType.CLASS && gmanager.getClassGraph().getModel().getRootCount() > 0) {
				img = GPConverter.toImage(gmanager.getClassGraph());
			} else if (graphType == GraphType.PROPERTY && gmanager.getPropertyGraph().getModel().getRootCount() > 0) {
				img = GPConverter.toImage(gmanager.getPropertyGraph());
			}
			if (img != null) {
				ImageIO.write(img, fileType.toLowerCase(), file);
			}
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}
}
