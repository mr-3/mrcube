package org.mrcube.models;

import org.mrcube.jgraph.GraphManager;

import javax.swing.table.DefaultTableModel;

public class SPARQLQueryResultTableModel extends DefaultTableModel {
    private GraphManager gmanager;

    public SPARQLQueryResultTableModel(GraphManager gm) {
        this.gmanager = gm;
    }

}
