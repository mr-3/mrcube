package jp.ac.shizuoka.cs.panda.mmm.mr3.ui;
import java.awt.*;

import javax.swing.*;
import javax.swing.event.*;

import jp.ac.shizuoka.cs.panda.mmm.mr3.util.*;

/**
 * @author takehshi morita
 *
 */
public class AttributeDialog extends JInternalFrame {

	private static final JPanel NULL_PANEL = new JPanel();

	private static int DIALOG_WIDTH = 430;
	private static int DIALOG_HEIGHT = 360; // 変更すると，コメントが削除できなくなる可能性がある 
	
	public AttributeDialog() {
		super(Translator.getString("AttributeDialog.Title"), false, true);
		setFrameIcon(Utilities.getImageIcon("attrDialogIcon.gif"));
		addInternalFrameListener(new InternalFrameAdapter() {
			public void internalFrameClosing(InternalFrameEvent e) {	
				setVisible(false);
			}
		});
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		setSize(new Dimension(DIALOG_WIDTH, DIALOG_HEIGHT));
		setLocation(50, 50);
		setVisible(false);
	}

	public void setNullPanel() {
		setContentPane(NULL_PANEL);
	}

	public void setVisible(boolean b) {
		super.setVisible(b);
		if (!b) {
			setNullPanel();
		}
	}
}
