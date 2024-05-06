package jp.ac.aoyama.it.ke.mrcube.models;

import org.apache.jena.rdf.model.RDFNode;
import jp.ac.aoyama.it.ke.mrcube.utils.GraphUtilities;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

public class SPARQLQueryResultTableCellRenderer extends JLabel implements TableCellRenderer {

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                   boolean hasFocus, int row, int column) {
        RDFNode node = (RDFNode) value;
        if (node.isResource()) {
            setText(GraphUtilities.getQName(node.asResource()));
        } else if (node.isLiteral()) {
            setText(node.asLiteral().getString());
        }
        return this;
    }
}
