package mr3.ui;
import java.awt.*;
import java.util.*;

import javax.swing.*;

import com.jgraph.event.*;

/**
 *
 * @auther takeshi morita
 */
public class SelectRegionDialog extends SelectClassDialog {

    private Set orgRegionSet;
    private Set newRegionSet;
    private JList regionList;
    private JScrollPane regionListScroll;    

    public SelectRegionDialog() {
        super("Select Region");
        newRegionSet = new HashSet();
    }

    public void setRegionSet(Set set) {
        orgRegionSet = set;
        regionList.setListData(orgRegionSet.toArray());
    }

    protected void initEachDialogAttr() {
        regionList = new JList();
        regionListScroll = new JScrollPane(regionList);
        regionListScroll.setBorder(
            BorderFactory.createTitledBorder("Domain List")); 
        regionListScroll.setPreferredSize(new Dimension(450, 80));
        regionListScroll.setMinimumSize(new Dimension(450, 80));
    }

    protected void setEachDialogAttrLayout() {
        gridbag.setConstraints(regionListScroll, c);
        inlinePanel.add(regionListScroll);
    }
    
    public void valueChanged(GraphSelectionEvent e) {        
        newRegionSet.removeAll(newRegionSet);
        Object[] cells = graph.getSelectionCells();
        for (int i = 0; i < cells.length; i++) {
//			if (graph.isRDFResourceCell(cells[i])) {
			if (graph.isRDFSClassCell(cells[i])) {
                //RDFSInfo info = rdfsMap.getCellInfo(cells[i]);
//				newRegionSet.add(info.getURI());
				newRegionSet.add(cells[i]);
            }
        }
        if (orgRegionSet != null) {
            newRegionSet.addAll(orgRegionSet);
        }
        regionList.setListData(newRegionSet.toArray());
    }

    public Object getValue() {
        if (isOk) {
            isOk = false;
            return newRegionSet;
        } else {
            return null;
        }
    }
}
