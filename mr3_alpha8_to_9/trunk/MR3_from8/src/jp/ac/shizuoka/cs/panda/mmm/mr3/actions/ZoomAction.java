/*
 * Created on 2003/10/02
 *
 */
package jp.ac.shizuoka.cs.panda.mmm.mr3.actions;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import jp.ac.shizuoka.cs.panda.mmm.mr3.editor.*;
import jp.ac.shizuoka.cs.panda.mmm.mr3.jgraph.*;
import jp.ac.shizuoka.cs.panda.mmm.mr3.util.*;

/**
 * @author takeshi morita
 */
public class ZoomAction extends AbstractAction {

	private String title;
	private RDFGraph graph;
	private Editor editor;

	public static final String ZOOM_STD = Translator.getString("Action.ZoomStd.Text");
	public static final String ZOOM_IN = Translator.getString("Action.ZoomIn.Text");
	public static final String ZOOM_OUT = Translator.getString("Action.ZoomOut.Text");
	public static final String ZOOM_SUITABLE = Translator.getString("Action.ZoomSuitable.Text");

	public static final ImageIcon ZOOM_STD_ICON = Utilities.getImageIcon(Translator.getString("Action.ZoomStd.Icon"));
	public static final ImageIcon ZOOM_IN_ICON = Utilities.getImageIcon(Translator.getString("Action.ZoomIn.Icon"));
	public static final ImageIcon ZOOM_OUT_ICON = Utilities.getImageIcon(Translator.getString("Action.ZoomOut.Icon"));
	public static final ImageIcon ZOOM_SUITABLE_ICON = Utilities.getImageIcon(Translator.getString("Action.ZoomSuitable.Icon"));

	public ZoomAction(RDFGraph g, Editor editor, String title, Icon icon) {
		super(title, icon);
		this.title = title;
		graph = g;
		this.editor = editor;
		putValue(SHORT_DESCRIPTION, title);
	}

	public void actionPerformed(ActionEvent e) {
		if (title.equals(ZOOM_STD)) {
			graph.setScale(1.0);
		} else if (title.equals(ZOOM_IN)) {
			graph.setScale(1.5 * graph.getScale());
		} else if (title.equals(ZOOM_OUT)) {
			graph.setScale(graph.getScale() / 1.5);
		} else if (title.equals(ZOOM_SUITABLE)) {
			fitWindow();
		}
	}

	private void setValues() {
		putValue(SHORT_DESCRIPTION, title);
		//		putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_N, KeyEvent.CTRL_MASK));
		//		putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_N));
	}

	public void fitWindow() {
		Rectangle p = graph.getCellBounds(graph.getRoots());
		if (p != null) {
			Dimension s = editor.getJScrollPane().getViewport().getExtentSize();
			double scale = 1;
			if (s.width == 0 && s.height == 0) {
				graph.setScale(scale);
				return;
			}
			if (Math.abs(s.getWidth() - (p.x + p.getWidth())) > Math.abs(s.getHeight() - (p.x + p.getHeight())))
				scale = s.getWidth() / (p.x + p.getWidth());
			else
				scale = s.getHeight() / (p.y + p.getHeight());
			scale = Math.max(Math.min(scale, 16), .01);
			graph.setScale(scale);
		}
	}

}
