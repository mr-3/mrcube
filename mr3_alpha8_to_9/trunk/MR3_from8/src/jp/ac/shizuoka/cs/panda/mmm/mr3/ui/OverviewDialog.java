/*
 * Created on 2003/08/02
 *
 */
package jp.ac.shizuoka.cs.panda.mmm.mr3.ui;

import java.awt.*;

import javax.swing.*;
import javax.swing.event.*;

import jp.ac.shizuoka.cs.panda.mmm.mr3.jgraph.*;
import jp.ac.shizuoka.cs.panda.mmm.mr3.util.*;

import org.jgraph.*;

/**
 * @author takeshi morita
 */
public class OverviewDialog extends JInternalFrame {

	private static final int LENGH = 200;

	public static final String RDF_EDITOR_OVERVIEW = Translator.getString("RDFEditorOverview.Title");
	public static final String CLASS_EDITOR_OVERVIEW = Translator.getString("ClassEditorOverview.Title");
	public static final String PROPERTY_EDITOR_OVERVIEW = Translator.getString("PropertyEditorOverview.Title");

	public static final ImageIcon RDF_EDITOR_ICON = Utilities.getImageIcon(Translator.getString("RDFEditor.Icon"));
	public static final ImageIcon CLASS_EDITOR_ICON = Utilities.getImageIcon(Translator.getString("ClassEditor.Icon"));
	public static final ImageIcon PROPERTY_EDITOR_ICON = Utilities.getImageIcon(Translator.getString("PropertyEditor.Icon"));

	public OverviewDialog(String title, JGraph graph, JViewport viewport) {
		super(title, true, true);
		JPanel panel = new MR3OverviewPanel(graph, viewport);
		getContentPane().add(panel);
		addInternalFrameListener(new InternalFrameAdapter() {
			public void internalFrameClosing(InternalFrameEvent e) {
				setVisible(false);
			}
		});
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		setBounds(new Rectangle(100, 100, LENGH, LENGH));
		setVisible(false);
	}

}
