/*
 * @(#) SamplePlugin2.java
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

package org.semanticweb.mmm.mr3.sample;

import java.awt.*;
import java.io.*;

import javax.swing.*;
import javax.swing.event.*;

import org.semanticweb.mmm.mr3.plugin.*;

import com.hp.hpl.jena.rdf.model.*;
/**
 *
 * @author takeshi morita
 *  print RDF Model
 */
public class SamplePlugin2 extends MR3Plugin {

	private JTextArea textArea;
	private JInternalFrame srcFrame;

	public SamplePlugin2() {
		textArea = new JTextArea();
		initSRCFrame();
		srcFrame.getContentPane().add(textArea);				
	}

	private void initSRCFrame() {
		srcFrame = new JInternalFrame("Sample Plugin 2", true, true);
		srcFrame.addInternalFrameListener(new InternalFrameAdapter() {
			public void internalFrameClosing(InternalFrameEvent e) {
				srcFrame.setVisible(false);
			}
		});
		srcFrame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		srcFrame.setBounds(new Rectangle(100, 100, 450, 300));
	}

	public void exec() {
		getDesktopPane().add(srcFrame);
		srcFrame.setVisible(true);
		try {
			Model rdfModel = getRDFModel();
			Writer out = new StringWriter();
			rdfModel.write(new PrintWriter(out));
			textArea.setText(out.toString());
		} catch (RDFException e) {
			e.printStackTrace();
		}
	}
	
}
