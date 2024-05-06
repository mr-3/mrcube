package jp.ac.aoyama.it.ke.mrcube.views.common;

import jp.ac.aoyama.it.ke.mrcube.actions.SelectEditorAction;
import jp.ac.aoyama.it.ke.mrcube.jgraph.OntClassCell;
import jp.ac.aoyama.it.ke.mrcube.jgraph.OntPropertyCell;
import jp.ac.aoyama.it.ke.mrcube.jgraph.InstancePropertyCell;
import jp.ac.aoyama.it.ke.mrcube.jgraph.InstanceCell;

import javax.swing.*;
import java.awt.*;

public class ResourceListCellRenderer extends DefaultListCellRenderer {
    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        if (value instanceof InstanceCell || value instanceof InstancePropertyCell) {
            setIcon(SelectEditorAction.INSTANCE_EDITOR_ICON);
        } else if (value instanceof OntClassCell) {
            setIcon(SelectEditorAction.CLASS_EDITOR_ICON);
        } else if (value instanceof OntPropertyCell) {
            setIcon(SelectEditorAction.PROPERTY_EDITOR_ICON);
        }
        return this;
    }
}
