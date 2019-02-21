package org.mrcube.views.common;

import org.mrcube.actions.SelectEditorAction;
import org.mrcube.jgraph.OntClassCell;
import org.mrcube.jgraph.OntPropertyCell;
import org.mrcube.jgraph.RDFPropertyCell;
import org.mrcube.jgraph.RDFResourceCell;

import javax.swing.*;
import java.awt.*;

public class ResourceListCellRenderer extends DefaultListCellRenderer {
    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        if (value instanceof RDFResourceCell || value instanceof RDFPropertyCell) {
            setIcon(SelectEditorAction.RDF_EDITOR_ICON);
        } else if (value instanceof OntClassCell) {
            setIcon(SelectEditorAction.CLASS_EDITOR_ICON);
        } else if (value instanceof OntPropertyCell) {
            setIcon(SelectEditorAction.PROPERTY_EDITOR_ICON);
        }
        return this;
    }
}
