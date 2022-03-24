package org.mrcube.views;

import org.apache.jena.query.*;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.mrcube.io.MR3Writer;
import org.mrcube.jgraph.GraphManager;
import org.mrcube.models.MR3Constants;
import org.mrcube.models.SPARQLQueryResultTableCellRenderer;
import org.mrcube.utils.GraphUtilities;
import org.mrcube.utils.Translator;
import org.mrcube.utils.Utilities;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class SPARQLQueryDialog extends JDialog {
    private final MR3Writer mr3Writer;
    private final GraphManager gmanager;
    private final JTextArea queryTextArea;
    private final JTable queryResultsTable;
    private final DefaultTableModel queryResultsTableModel;
    private final SPARQLQueryResultTableCellRenderer sparqlTableCellRenderer;
    private final JButton runQueryButton;
    private final JButton cancelButton;

    private static final int WIDTH = 500;
    private static final int HEIGHT = 500;

    public SPARQLQueryDialog(MR3Writer mr3Writer, GraphManager gmanager) {
        this.mr3Writer = mr3Writer;
        this.gmanager = gmanager;
        queryTextArea = new JTextArea();
        var scrollAbleQueryTextArea = new JScrollPane(queryTextArea);
        scrollAbleQueryTextArea.setBorder(
                BorderFactory.createTitledBorder(Translator.getString("SPARQLQueryDialog.QueryText")));
        queryResultsTableModel = new DefaultTableModel();
        sparqlTableCellRenderer = new SPARQLQueryResultTableCellRenderer();
        queryResultsTable = new JTable(queryResultsTableModel);
        queryResultsTable.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                int row = queryResultsTable.getSelectedRow();
                int col = queryResultsTable.getSelectedColumn();
                RDFNode selectedNode = (RDFNode) queryResultsTable.getValueAt(row, col);
                GraphUtilities.selectCellSet(gmanager, new HashSet<>(Arrays.asList(selectedNode)));
            }
        });

        var scrollAbleQueryResultsTable = new JScrollPane(queryResultsTable);
        scrollAbleQueryResultsTable.setBorder(BorderFactory.createTitledBorder(
                Translator.getString("SPARQLQueryDialog.QueryResults")));

        var centerPanel = new JPanel();
        centerPanel.setLayout(new GridLayout(2, 1));
        centerPanel.add(scrollAbleQueryTextArea);
        centerPanel.add(scrollAbleQueryResultsTable);

        runQueryButton = new JButton(Translator.getString("SPARQLQueryDialog.RunQuery"));
        runQueryButton.addActionListener(l -> {
            queryResultsTableModel.setRowCount(0);
            Query query = QueryFactory.create(queryTextArea.getText());
            try (QueryExecution qexec = QueryExecutionFactory.create(query, getModel())) {
                if (query.queryType() != QueryType.SELECT) {
                    Utilities.showErrorMessageDialog(Translator.getString("SPARQLQueryDialog.Warning"));
                    return;
                }
                ResultSet results = qexec.execSelect();
                java.util.List<String> resultVarList = results.getResultVars();
                Set<RDFNode> nodeSet = new HashSet<>();
                queryResultsTableModel.setColumnIdentifiers(resultVarList.toArray());
                while (results.hasNext()) {
                    QuerySolution solution = results.nextSolution();
                    java.util.List<RDFNode> nodeList = resultVarList.stream()
                            .map(solution::get)
                            .collect(Collectors.toList());
                    queryResultsTableModel.addRow(nodeList.toArray());
                    nodeSet.addAll(nodeList);
                }
                resultVarList.forEach(id ->
                        queryResultsTable.getColumn(id).setCellRenderer(sparqlTableCellRenderer)
                );
            }
        });
        cancelButton = new JButton(MR3Constants.CANCEL);
        cancelButton.addActionListener(l -> setVisible(false));
        var buttonPanel = new JPanel();
        buttonPanel.add(runQueryButton);
        buttonPanel.add(cancelButton);

        getContentPane().add(centerPanel, BorderLayout.CENTER);
        getContentPane().add(buttonPanel, BorderLayout.SOUTH);
        setSize(new Dimension(WIDTH, HEIGHT));
        setLocationRelativeTo(gmanager.getRootFrame());
    }

    private Model getModel() {
        Model model = ModelFactory.createDefaultModel();
        model.add(mr3Writer.getRDFModel());
        model.add(mr3Writer.getClassModel());
        model.add(mr3Writer.getPropertyModel());
        return model;
    }

}
