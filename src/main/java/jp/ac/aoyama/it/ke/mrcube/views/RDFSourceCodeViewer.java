/*
 * Project Name: MR^3 (Meta-Model Management based on RDFs Revision Reflection)
 * Project Website: https://mr-3.github.io/
 *
 * Copyright (C) 2003-2026 Takeshi Morita. All rights reserved.
 *
 * This file is part of MR^3.
 *
 * MR^3 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * MR^3 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with MR^3.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package jp.ac.aoyama.it.ke.mrcube.views;

import jp.ac.aoyama.it.ke.mrcube.io.MR3Writer;
import jp.ac.aoyama.it.ke.mrcube.jgraph.GraphManager;
import jp.ac.aoyama.it.ke.mrcube.models.NamespaceModel;
import jp.ac.aoyama.it.ke.mrcube.utils.GraphUtilities;
import jp.ac.aoyama.it.ke.mrcube.utils.Translator;
import jp.ac.aoyama.it.ke.mrcube.utils.Utilities;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.StringWriter;
import java.util.Set;

/**
 * @author Takeshi Morita
 */
public class RDFSourceCodeViewer extends JDialog implements ActionListener {

    private final MR3Writer mr3Writer;
    private final GraphManager gmanager;

    private final JRadioButton turtleRadioButton;
    private final JRadioButton jsonldRadioButton;
    private final JRadioButton xmlRadioButton;
    private final JRadioButton nTripleRadioButton;

    private static JTextArea sourceCodeTextArea;
    private static final int FRAME_HEIGHT = 500;
    private static final int FRAME_WIDTH = 600;
    private static final ImageIcon SOURCE_CODE_ICON = Utilities.getSVGIcon(Translator.getString("RDFSourceCodeViewer.Icon"));

    public RDFSourceCodeViewer(GraphManager gm) {
        super(gm.getRootFrame(), Translator.getString("RDFSourceCodeViewer.Title"), true);
        setIconImage(SOURCE_CODE_ICON.getImage());

        gmanager = gm;
        mr3Writer = new MR3Writer(gmanager);

        turtleRadioButton = new JRadioButton("Turtle");
        turtleRadioButton.addActionListener(this);
        turtleRadioButton.setSelected(true);
        jsonldRadioButton = new JRadioButton("JSONLD");
        jsonldRadioButton.addActionListener(this);
        xmlRadioButton = new JRadioButton("XML");
        xmlRadioButton.addActionListener(this);
        nTripleRadioButton = new JRadioButton("N-Triples");
        nTripleRadioButton.addActionListener(this);
        ButtonGroup group = new ButtonGroup();
        group.add(turtleRadioButton);
        group.add(jsonldRadioButton);
        group.add(xmlRadioButton);
        group.add(nTripleRadioButton);
        JPanel outputCheckPanel = new JPanel();
        outputCheckPanel.setLayout(new GridLayout(1, 4));
        outputCheckPanel.setBorder(BorderFactory.createTitledBorder(Translator.getString("RDFSourceCodeViewer.Syntax")));
        outputCheckPanel.add(turtleRadioButton);
        outputCheckPanel.add(jsonldRadioButton);
        outputCheckPanel.add(xmlRadioButton);
        outputCheckPanel.add(nTripleRadioButton);

        setLayout(new BorderLayout());
        sourceCodeTextArea = new JTextArea();
        add(outputCheckPanel, BorderLayout.NORTH);
        add(new JScrollPane(sourceCodeTextArea), BorderLayout.CENTER);

        Dimension size = new Dimension(FRAME_WIDTH, FRAME_HEIGHT);
        setSize(size);
        setLocationRelativeTo(gmanager.getRootFrame());
        setVisible(false);
    }

    private void writeModel(Model model, StringWriter writer) {
        RDFFormat rdfFormat = getRDFFormat();
        setNsPrefix(model);
        try {
            RDFDataMgr.write(writer, model, rdfFormat);
        } catch (Exception e) {
            e.printStackTrace();
            Utilities.showErrorMessageDialog("Export Error");
        }
    }

    private String getModelString(Model model) {
        StringWriter writer = new StringWriter();
        writeModel(model, writer);
        return writer.toString();
    }

    private RDFFormat getRDFFormat() {
        if (xmlRadioButton.isSelected()) {
            return RDFFormat.RDFXML_ABBREV;
        } else if (nTripleRadioButton.isSelected()) {
            return RDFFormat.NTRIPLES;
        } else if (turtleRadioButton.isSelected()) {
            return RDFFormat.TURTLE;
        } else if (jsonldRadioButton.isSelected()) {
            return RDFFormat.JSONLD;
        }
        return RDFFormat.RDFXML;
    }

    private Model getModel() {
        Model model = ModelFactory.createDefaultModel();
        model.add(mr3Writer.getRDFModel());
        model.add(mr3Writer.getClassModel());
        model.add(mr3Writer.getPropertyModel());
        return model;
    }

    private void setNsPrefix(Model model) {
        Set<NamespaceModel> namespaceModelSet = GraphUtilities.getNamespaceModelSet();
        for (NamespaceModel info : namespaceModelSet) {
            if (info.isAvailable()) {
                model.setNsPrefix(info.getPrefix(), info.getNameSpace());
            }
        }
    }

    public void actionPerformed(ActionEvent e) {
        sourceCodeTextArea.setText(getModelString(getModel()));
    }

    public void setFont(Font font) {
        super.setFont(font);
        sourceCodeTextArea.setFont(font);
    }

    public static void setText(String text) {
        sourceCodeTextArea.setText(text);
    }

    public void setVisible(boolean t) {
        if (t) {
            sourceCodeTextArea.setText(getModelString(getModel()));
            if (GraphUtilities.defaultFont != null) {
                setFont(GraphUtilities.defaultFont);
            }
        }
        super.setVisible(t);
    }
}
