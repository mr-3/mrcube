/*
 * Created on 2003/07/24
 *
 */
package jp.ac.shizuoka.cs.panda.mmm.mr3.actions;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import jp.ac.shizuoka.cs.panda.mmm.mr3.jgraph.*;

/**
 * @author takeshi morita
 */
public class CutAction extends AbstractAction {
	RDFGraph graph;

	public CutAction(RDFGraph g, String title) {
		super(title);
		graph = g;
	}

	public void actionPerformed(ActionEvent e) {
		graph.cut(new Point(100, 100));
	}
}
