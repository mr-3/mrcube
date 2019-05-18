package org.mrcube.views;

import org.apache.jena.query.*;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.jgraph.graph.GraphCell;
import org.mrcube.io.MR3Writer;
import org.mrcube.jgraph.GraphManager;
import org.mrcube.models.MR3Constants;
import org.mrcube.utils.Utilities;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class SPARQLQueryDialog extends JDialog {
    private MR3Writer mr3Writer;
    private GraphManager gmanager;
    private JTextArea queryTextArea;
    private JTable queryResultsTable;
    private DefaultTableModel queryResultsTableModel;
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
        queryResultsTableModel = new DefaultTableModel();
        queryResultsTable = new JTable(queryResultsTableModel);
        queryResultsTable.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                int row = queryResultsTable.getSelectedRow();
                int col = queryResultsTable.getSelectedColumn();
                RDFNode selectedNode = (RDFNode) queryResultsTable.getValueAt(row, col);
                Set<GraphCell> cellSet = new HashSet<>();
                if (selectedNode.isResource()) {
                    cellSet.addAll(gmanager.findRDFResourceSet(selectedNode.asResource().getURI()));
                    cellSet.addAll(gmanager.findRDFSResourceSet(selectedNode.asResource().getURI(),
                            gmanager.getClassGraph()));
                    cellSet.addAll(gmanager.findRDFSResourceSet(selectedNode.asResource().getURI(),
                            gmanager.getPropertyGraph()));
                } else if (selectedNode.isLiteral()) {
                    cellSet.addAll(gmanager.findRDFResourceSet(selectedNode.asLiteral().getString()));
                    cellSet.addAll(gmanager.findRDFSResourceSet(selectedNode.asLiteral().getString(),
                            gmanager.getClassGraph()));
                    cellSet.addAll(gmanager.findRDFSResourceSet(selectedNode.asLiteral().getString(),
                            gmanager.getPropertyGraph()));
                }
                cellSet.stream().forEach(c -> {
                    gmanager.selectCell(c, gmanager.getRDFGraph());
                    gmanager.selectCell(c, gmanager.getClassGraph());
                    gmanager.selectCell(c, gmanager.getPropertyGraph());
                });
            }
        });

        var scrollAbleQueryResultsTable = new JScrollPane(queryResultsTable);
        scrollAbleQueryResultsTable.setBorder(BorderFactory.createTitledBorder("Query Results"));

        var centerPanel = new JPanel();
        centerPanel.setLayout(new GridLayout(2, 1));
        centerPanel.add(scrollAbleQueryTextArea);
        centerPanel.add(scrollAbleQueryResultsTable);

        runQueryButton = new JButton("Run Query");
        runQueryButton.addActionListener(l -> {
            queryResultsTableModel.setRowCount(0);
            Query query = QueryFactory.create(queryTextArea.getText());
            QueryExecution qexec = QueryExecutionFactory.create(query, getModel());
            try {
                if (query.getQueryType() != Query.QueryTypeSelect) {
                    Utilities.showErrorMessageDialog("It supports only select query.");
                    return;
                }
                ResultSet results = qexec.execSelect();
                java.util.List<String> resultVarList = results.getResultVars();
                queryResultsTableModel.setColumnIdentifiers(resultVarList.toArray());
                while (results.hasNext()) {
                    QuerySolution solution = results.nextSolution();
                    java.util.List<RDFNode> nodeList = resultVarList.stream()
                            .map(n -> solution.get(n))
                            .collect(Collectors.toList());
                    queryResultsTableModel.addRow(nodeList.toArray());
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
