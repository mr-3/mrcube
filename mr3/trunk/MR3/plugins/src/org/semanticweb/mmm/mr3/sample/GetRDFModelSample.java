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
 * @author takeshi morita print RDF Model
 */
public class GetRDFModelSample extends MR3Plugin {

    private JTree classTree;
    private JTree propertyTree;
    private JTextArea textArea;
    private JInternalFrame srcFrame;

    public GetRDFModelSample() {
        classTree = new JTree();
        JScrollPane classTreeScroll = new JScrollPane(classTree);
        classTreeScroll.setMinimumSize(new Dimension(150, 150));
        classTreeScroll.setBorder(BorderFactory.createTitledBorder("Class Tree"));
        propertyTree = new JTree();
        JScrollPane propertyTreeScroll = new JScrollPane(propertyTree);
        //propertyTreeScroll.setPreferredSize(new Dimension(150, 200));
        propertyTreeScroll.setBorder(BorderFactory.createTitledBorder("Property Tree"));
        textArea = new JTextArea(5, 10);
        JScrollPane textAreaScroll = new JScrollPane(textArea);
        textAreaScroll.setBorder(BorderFactory.createTitledBorder("RDF/XML"));
        srcFrame = new JInternalFrame("Sample Plugin 2", true, true);
        srcFrame.addInternalFrameListener(new InternalFrameAdapter() {
            public void internalFrameClosing(InternalFrameEvent e) {
                srcFrame.setVisible(false);
            }
        });
        srcFrame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        JSplitPane treeSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, classTreeScroll, propertyTreeScroll);
        treeSplitPane.setDividerLocation(0.5);
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, treeSplitPane, textAreaScroll);
        splitPane.setDividerLocation(0.3);
        srcFrame.getContentPane().add(splitPane);
        srcFrame.setBounds(new Rectangle(50, 50, 500, 400));
    }

    public void exec() {
        getDesktopPane().add(srcFrame);
        try {
            Model rdfModel = getRDFModel();
            Writer out = new StringWriter();
            rdfModel.write(new PrintWriter(out));
            textArea.setText(out.toString());
        } catch (RDFException e) {
            e.printStackTrace();
        }
        classTree.setModel(getClassTreeModel());
        propertyTree.setModel(getPropertyTreeModel());
        srcFrame.setVisible(true);
    }
}
