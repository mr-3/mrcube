package mr3.ui;
import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import mr3.data.*;
import mr3.jgraph.*;
import mr3.util.*;

import com.jgraph.event.*;
import com.jgraph.graph.*;

/**
 *
 * @auther takeshi morita
 */
public abstract class SelectClassDialog extends JDialog
    implements ActionListener, GraphSelectionListener {
     
    protected boolean isOk;
    protected JButton confirm;
    protected JButton cancel;

    protected RDFGraph graph;
    protected JPanel inlinePanel = new JPanel();
    protected GridBagLayout gridbag;
    protected GridBagConstraints c;
    
    protected RDFSInfoMap rdfsMap = RDFSInfoMap.getInstance();

    public SelectClassDialog(String title) {
        super((Frame)null, title, true);

        initGraph();     
        initEachDialogAttr();
        initButton();

        initGridLayout();

        setGraphLayout();
        setEachDialogAttrLayout();
        setCommonLayout();
    }

    protected abstract void initEachDialogAttr();
    
    protected void initButton() {
        confirm = new JButton("OK");
        confirm.addActionListener(this);
        cancel = new JButton("Cancel");
        cancel.addActionListener(this);
    }

    protected void initGridLayout() {
        gridbag = new GridBagLayout();
        c = new GridBagConstraints();
        inlinePanel.setLayout(gridbag);
        c.anchor = GridBagConstraints.PAGE_START;
        c.gridwidth = GridBagConstraints.REMAINDER;
    }
    
    protected void setGraphLayout() {
        JScrollPane graphScroll = new JScrollPane(graph);
        graphScroll.setPreferredSize(new Dimension(450, 300));
        graphScroll.setMinimumSize(new Dimension(450, 300));        
        gridbag.setConstraints(graphScroll, c);
        inlinePanel.add(graphScroll);
    }
    
    protected abstract void setEachDialogAttrLayout();

    protected void setCommonLayout() {
        c.gridwidth = GridBagConstraints.RELATIVE;
        c.weightx = 1.0;
        gridbag.setConstraints(confirm, c);
        inlinePanel.add(confirm);
        c.gridwidth = GridBagConstraints.REMAINDER;
        gridbag.setConstraints(cancel, c);
        inlinePanel.add(cancel);

        Container contentPane = getContentPane();
        contentPane.add(inlinePanel);
        setLocation(100, 100);
        setSize(new Dimension(550, 450));
        setVisible(false);
    }
    
    private void initGraph() {
        graph = new RDFGraph();
        graph.setMarqueeHandler(new BasicMarqueeHandler());
        graph.getSelectionModel().addGraphSelectionListener(this);
        graph.setCloneable(false);
        graph.setBendable(false);
        graph.setDisconnectable(false);
        graph.setPortsVisible(false);
        graph.setDragEnabled(false);
        graph.setDropEnabled(false);
        graph.setEditable(false);
    }

    public void replaceGraph(RDFGraph newGraph) {
       	graph.setRDFState(newGraph.getRDFState());
       	changeAllCellColor(Color.green);
    }

    public abstract void valueChanged(GraphSelectionEvent e);
    
    public void actionPerformed(ActionEvent e) {
        String type = (String)e.getActionCommand();
        if (type.equals("OK")) {
            isOk = true;
        } else {
            isOk = false;    
        }
        setVisible(false);
    }

	protected void changeAllCellColor(Color color) {
		Object[] cells = graph.getAllCells();
		for (int i = 0; i < cells.length; i++) {
			GraphCell cell = (GraphCell) cells[i];
			if (graph.isRDFSClassCell(cell)) {
				ChangeCellAttributes.changeCellColor(graph, cell, color);
			}
		}
	}
}
