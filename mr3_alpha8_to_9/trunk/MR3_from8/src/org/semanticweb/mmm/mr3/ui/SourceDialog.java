/*
 * Created on 2003/07/20
 *
 */
package org.semanticweb.mmm.mr3.ui;

import java.awt.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;

import org.semanticweb.mmm.mr3.util.*;
import org.semanticweb.mmm.mr3.util.Utilities;

/**
 * @author takeshi morita
 *
 */
public class SourceDialog extends JInternalFrame {

	private JTextArea srcArea;
	private static final int FRAME_HEIGHT = 400;
	private static final int FRAME_WIDTH = 600;

	public SourceDialog() {
		super(Translator.getString("SourceDialog.Title"), true, true, true);
		setFrameIcon(Utilities.getImageIcon(Translator.getString("SourceDialog.Icon")));
		setIconifiable(true);

		srcArea = new JTextArea();
		srcArea.setEditable(false);
		setContentPane(new JScrollPane(srcArea));

		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		addInternalFrameListener(new CloseInternalFrameAction());
		setBounds(new Rectangle(0, 0, FRAME_WIDTH, FRAME_HEIGHT));
		setVisible(false);
	}

	class CloseInternalFrameAction extends InternalFrameAdapter {
		public void internalFrameClosing(InternalFrameEvent e) {
			setVisible(false);
		}
	}

	public JTextComponent getSourceArea() {
		return srcArea;
	}

}
