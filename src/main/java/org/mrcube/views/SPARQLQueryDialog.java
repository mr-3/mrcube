package org.mrcube.views;

import org.apache.jena.query.*;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.mrcube.io.MR3Writer;
import org.mrcube.jgraph.GraphManager;
import org.mrcube.models.MR3Constants;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class SPARQLQueryDialog extends JDialog {
    private MR3Writer mr3Writer;
    private GraphManager gmanager;
    private JTextArea queryTextArea;
    private JTable queryResultsTable;
    private JButton runQueryButton;
    private JButton cancelButton;

    private static final int WIDTH = 500;
    private static final int HEIGHT = 500;


    public SPARQLQueryDialog(MR3Writer mr3Writer, GraphManager gmanager) {
        this.mr3Writer = mr3Writer;
        this.gmanager = gmanager;
        queryTextArea = new JTextArea();
        var scrollAbleQueryTextArea = new JScrollPane(queryTextArea);
        scrollAbleQueryTextArea.setBorder(BorderFactory.createTitledBorder("SPARQL Query Text"));
        queryResultsTable = new JTable();
        var scrollAbleQueryResultsTable = new JScrollPane(queryResultsTable);
        scrollAbleQueryResultsTable.setBorder(BorderFactory.createTitledBorder("Query Results"));

        var centerPanel = new JPanel();
        centerPanel.setLayout(new GridLayout(2, 1));
        centerPanel.add(scrollAbleQueryTextArea);
        centerPanel.add(scrollAbleQueryResultsTable);

        runQueryButton = new JButton("Run Query");
        runQueryButton.addActionListener(l -> {
            Query query = QueryFactory.create(queryTextArea.getText());
            QueryExecution qexec = QueryExecutionFactory.create(query, getModel());
            try {
                ResultSet results = qexec.execSelect();
                while (results.hasNext()) {
                    QuerySolution solution = results.nextSolution();
                    java.util.List<String> varNameList = new ArrayList<>();
                    solution.varNames().forEachRemaining(varNameList::add);
                    for (String varName : varNameList) {
                        System.out.print(varName + ": " + solution.get(varName));
                        System.out.print(", ");
                    }
                    System.out.println();
                }
            } finally {
                qexec.close();
            }
        });
        cancelButton = new JButton(MR3Constants.CANCEL);
        cancelButton.addActionListener(l -> setVisible(false));
        var buttonPanel = new JPanel();
        buttonPanel.add(runQueryButton);
        buttonPanel.add(cancelButton);

        getContentPane().add(centerPanel, BorderLayout.CENTER);
        getContentPane().add(buttonPanel, BorderLayout.SOUTH);
        setLocationRelativeTo(this.gmanager.getRootFrame());
        setSize(new Dimension(WIDTH, HEIGHT));
    }

    private Model getModel() {
        Model model = ModelFactory.createDefaultModel();
        model.add(mr3Writer.getRDFModel());
        model.add(mr3Writer.getClassModel());
        model.add(mr3Writer.getPropertyModel());
        return model;
    }

}
