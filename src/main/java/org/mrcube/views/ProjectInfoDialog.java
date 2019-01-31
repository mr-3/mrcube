/*
 * Project Name: MR^3 (Meta-Model Management based on RDFs Revision Reflection)
 * Project Website: http://mrcube.org/
 *
 * Copyright (C) 2003-2018 Yamaguchi Laboratory, Keio University. All rights reserved.
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

package org.mrcube.views;

import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.util.IteratorCollection;
import org.mrcube.MR3;
import org.mrcube.io.MR3Writer;
import org.mrcube.jgraph.GraphManager;
import org.mrcube.jgraph.RDFGraph;
import org.mrcube.models.MR3Constants;
import org.mrcube.utils.Translator;
import org.mrcube.utils.Utilities;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Takeshi Morita
 */
public class ProjectInfoDialog extends JDialog {

    private final JLabel currentProjectValue;
    private final JLabel lastImportTimeValue;

    private final JLabel modelResourceCntValue;
    private final JLabel modelLiteralCntValue;
    private final JLabel modelStatementCntValue;

    private final JLabel ontClassCntValue;
    private final JLabel ontPropertyCntValue;

    private final JLabel allResourceCntValue;
    private final JLabel allLiteralCntValue;
    private final JLabel allStatementCntValue;

    private final MR3Writer mr3Writer;
    private final GraphManager gmanager;

    private static final int WINDOW_HEIGHT = 450;
    private static final int WINDOW_WIDTH = 550;

    public ProjectInfoDialog(GraphManager gm, Frame frame) {
        super(frame, Translator.getString("Menu.Tools.ProjectInfo.Text"));
        setIconImage(Utilities.getImageIcon("information.png").getImage());
        mr3Writer = new MR3Writer(gm);
        gmanager = gm;

        JLabel currentProjectLabel = new JLabel(Translator.getString("Menu.Tools.ProjectInfo.CurrentProjectName"));
        currentProjectValue = new JLabel();
        JLabel lastImportTimeLabel = new JLabel(Translator.getString("Menu.Tools.ProjectInfo.LastImportTime"));
        lastImportTimeValue = new JLabel();

        JPanel importPanel = new JPanel();
        importPanel.setBorder(BorderFactory.createTitledBorder(""));
        importPanel.setLayout(new GridLayout(2, 2, 60, 10));
        importPanel.add(currentProjectLabel);
        importPanel.add(currentProjectValue);
        importPanel.add(lastImportTimeLabel);
        importPanel.add(lastImportTimeValue);

        JLabel modelResourceCntLabel = new JLabel(Translator.getString("Menu.Tools.ProjectInfo.NumberOfRDFResources"));
        modelResourceCntValue = new JLabel();
        JLabel modelLiteralCntLabel = new JLabel(Translator.getString("Menu.Tools.ProjectInfo.NumberOfRDFLiterals"));
        modelLiteralCntValue = new JLabel();
        JLabel modelStatementCntLabel = new JLabel(Translator.getString("Menu.Tools.ProjectInfo.NumberOfRDFStatements"));
        modelStatementCntValue = new JLabel();

        JLabel ontClassCntLabel = new JLabel(Translator.getString("Menu.Tools.ProjectInfo.NumberOfClasses"));
        ontClassCntValue = new JLabel();
        JLabel ontPropertyCntLabel = new JLabel(Translator.getString("Menu.Tools.ProjectInfo.NumberOfProperties"));
        ontPropertyCntValue = new JLabel();

        JLabel allResourceCntLabel = new JLabel(Translator.getString("Menu.Tools.ProjectInfo.NumberOfAllResources"));
        allResourceCntValue = new JLabel();
        JLabel allLiteralCntLabel = new JLabel(Translator.getString("Menu.Tools.ProjectInfo.NumberOfAllLiterals"));
        allLiteralCntValue = new JLabel();
        JLabel allStatementCntLabel = new JLabel(Translator.getString("Menu.Tools.ProjectInfo.NumberOfAllStatements"));
        allStatementCntValue = new JLabel();

        JPanel cntPanel = new JPanel();
        cntPanel.setBorder(BorderFactory.createTitledBorder(Translator.getString("Menu.Tools.ProjectInfo.OntologyMetrics")));
        cntPanel.setLayout(new GridLayout(8, 2, 60, 10));
        cntPanel.add(modelResourceCntLabel);
        cntPanel.add(modelResourceCntValue);
        cntPanel.add(modelLiteralCntLabel);
        cntPanel.add(modelLiteralCntValue);
        cntPanel.add(modelStatementCntLabel);
        cntPanel.add(modelStatementCntValue);
        cntPanel.add(ontClassCntLabel);
        cntPanel.add(ontClassCntValue);
        cntPanel.add(ontPropertyCntLabel);
        cntPanel.add(ontPropertyCntValue);
        cntPanel.add(allResourceCntLabel);
        cntPanel.add(allResourceCntValue);
        cntPanel.add(allLiteralCntLabel);
        cntPanel.add(allLiteralCntValue);
        cntPanel.add(allStatementCntLabel);
        cntPanel.add(allStatementCntValue);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());
        mainPanel.add(importPanel, BorderLayout.NORTH);
        mainPanel.add(cntPanel, BorderLayout.CENTER);

        getContentPane().setLayout(new BorderLayout(20, 20));
        getContentPane().add(mainPanel, BorderLayout.CENTER);
        getContentPane().add(getButtonPanel(), BorderLayout.SOUTH);
        setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        setLocationRelativeTo(this);
        setVisible(false);
    }

    public void resetStatus() {
        String newProjectText = Translator.getString("Menu.File.New.Text");
        if (MR3.getCurrentProject() == null || MR3.getCurrentProject().getTitle().equals(newProjectText)) {
            currentProjectValue.setText(newProjectText);
        } else {
            currentProjectValue.setText(MR3.getCurrentProject().getTitle());
        }
        lastImportTimeValue.setText(Double.toString(MR3.STATUS_BAR.getProgressTime()));

        modelResourceCntValue.setText(Integer.toString(calcResourceCnt(mr3Writer.getRDFModel())));
        modelLiteralCntValue.setText(Integer.toString(calcLiteralCnt(mr3Writer.getRDFModel())));
        modelStatementCntValue.setText(Integer.toString(calcStatementCnt(mr3Writer.getRDFModel())));
        ontClassCntValue.setText(Integer.toString(calcOntCnt(gmanager.getCurrentClassGraph())));
        ontPropertyCntValue.setText(Integer.toString(calcOntCnt(gmanager.getCurrentPropertyGraph())));
        allResourceCntValue.setText(Integer.toString(calcAllResourceCnt()));
        allLiteralCntValue.setText(Integer.toString(calcAllLiteralCnt()));
        allStatementCntValue.setText(Integer.toString(calcAllStatementCnt()));
    }

    private JComponent getButtonPanel() {
        JButton reloadButton = new JButton(new ReloadAction());
        reloadButton.setMnemonic('o');
        JButton cancelButton = new JButton(new CancelAction());
        cancelButton.setMnemonic('c');
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(1, 2, 5, 5));
        buttonPanel.add(reloadButton);
        buttonPanel.add(cancelButton);
        return Utilities.createEastPanel(buttonPanel);
    }

    class ReloadAction extends AbstractAction {
        ReloadAction() {
            super(MR3Constants.RELOAD);
        }

        public void actionPerformed(ActionEvent e) {
            resetStatus();
        }
    }

    class CancelAction extends AbstractAction {
        CancelAction() {
            super(MR3Constants.CANCEL);
        }

        public void actionPerformed(ActionEvent e) {
            setVisible(false);
        }
    }

    private int calcResourceCnt(Model model) {
        Set subjectSet = IteratorCollection.iteratorToSet(model.listSubjects());
        Set objectSet = IteratorCollection.iteratorToSet(model.listObjects());
        Set resourceSet = new HashSet();
        resourceSet.addAll(subjectSet);
        resourceSet.addAll(objectSet);

        int rdfResourceCnt = 0;
        for (Object o : resourceSet) {
            if (o instanceof Resource) {
                rdfResourceCnt++;
            }
        }
        return rdfResourceCnt;
    }

    private int calcLiteralCnt(Model model) {
        Set objectSet = IteratorCollection.iteratorToSet(model.listObjects());

        int literalCnt = 0;
        for (Object o : objectSet) {
            if (o instanceof Literal) {
                literalCnt++;
            }
        }

        return literalCnt;
    }

    private int calcOntCnt(RDFGraph graph) {
        int ontCnt = 0;
        for (Object cell : graph.getAllCells()) {
            if (RDFGraph.isRDFSCell(cell)) {
                ontCnt++;
            }
        }
        return ontCnt;
    }

    private int calcAllResourceCnt() {
        return calcResourceCnt(mr3Writer.getRDFModel()) + calcResourceCnt(mr3Writer.getRDFSModel());
    }

    private int calcAllLiteralCnt() {
        return calcLiteralCnt(mr3Writer.getRDFModel()) + calcLiteralCnt(mr3Writer.getRDFSModel());
    }

    private int calcStatementCnt(Model model) {
        return IteratorCollection.iteratorToSet(model.listStatements()).size();
    }

    private int calcAllStatementCnt() {
        return calcStatementCnt(mr3Writer.getRDFModel()) + calcStatementCnt(mr3Writer.getRDFSModel());
    }

    public void setVisible(boolean t) {
        super.setVisible(t);
    }
}
