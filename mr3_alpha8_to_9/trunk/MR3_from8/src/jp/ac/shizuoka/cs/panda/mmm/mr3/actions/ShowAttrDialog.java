/*
 * Created on 2003/09/17
 *
 */
package jp.ac.shizuoka.cs.panda.mmm.mr3.actions;

import java.awt.event.*;

import javax.swing.*;

import jp.ac.shizuoka.cs.panda.mmm.mr3.*;
import jp.ac.shizuoka.cs.panda.mmm.mr3.jgraph.*;
import jp.ac.shizuoka.cs.panda.mmm.mr3.util.*;

/**
 * @author takeshi morita
 */
public class ShowAttrDialog extends MR3AbstractAction {
	
	private static final String TITLE = "Show Attribute Dialog";
	private static final ImageIcon ICON = Utilities.getImageIcon("attrDialogIcon.gif"); 

	public ShowAttrDialog(MR3 mr3) {
		super(mr3, TITLE, ICON);
		setValues();
	}

	private void setValues() {
		putValue(SHORT_DESCRIPTION, getName());
		putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_A, KeyEvent.ALT_MASK));
	}

	public void actionPerformed(ActionEvent e) {
		RDFGraph graph = null;
		Object selectionCell = null;
		JInternalFrame[] iFrames = mr3.getInternalFrames();
		
		if (iFrames[0].isSelected()) {
			graph = mr3.getRDFGraph();
			selectionCell = graph.getSelectionCell();
		} else if (iFrames[1].isSelected()) {
			graph = mr3.getClassGraph();
			selectionCell = graph.getSelectionCell();
		} else if (iFrames[2].isSelected()) {
			graph = mr3.getPropertyGraph();
			selectionCell = graph.getSelectionCell();
		}

		mr3.getAttrDialog().setVisible(true);

		if (graph != null && selectionCell != null) {
			graph.setSelectionCell(selectionCell);
		}
	}
}
