package mr3.ui;
import java.util.*;

import javax.swing.event.*;

import mr3.data.*;
import mr3.jgraph.*;

/**
 * upperList ... classification
 * lowerList ... nameSpaces
 *
 * @auther takeshi morita
 */
public class SelectNameSpaceDialog extends SelectListDialog {

	private Map prefixNSMap;
	private GraphManager gmanager;

	public SelectNameSpaceDialog(GraphManager m, Map map) {
		super(m.getRoot(), "Select NameSpace");
		gmanager = m;
		prefixNSMap = map;
		setClassification();
	}

	private void setClassification() {
		Object[] list = new Object[] { GraphType.RDF, GraphType.CLASS, GraphType.PROPERTY };
		upperList.setListData(list);
	}

	public void valueChanged(ListSelectionEvent e) {
		try {
			if (e.getSource() == upperList) {
				if (!lowerList.isSelectionEmpty()) {
					lowerList.clearSelection();
				}
				GraphType type = (GraphType) upperList.getSelectedValue();
				Set nsSet = gmanager.getNameSpaceSet(type);
				Collection orgNSSet = prefixNSMap.values();
				nsSet.removeAll(orgNSSet);
				lowerList.setListData(nsSet.toArray());
			} else if (e.getSource() == lowerList) {
				if (upperList.getSelectedValue() != null && lowerList.getSelectedValue() != null) {
					String ns = lowerList.getSelectedValue().toString();
					uri.setText(ns);
					uri.setToolTipText(ns);
				}
			}
		} catch (NullPointerException np) {
			System.out.println("null");
		}
	}
}
