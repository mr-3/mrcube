package mr3.ui;
import java.awt.*;

import javax.swing.*;

/**
 * @author take
 *
 */
public class ProgressWindow extends JDialog {
	private JProgressBar jprogressBar;

	ProgressWindow() {
		super((Frame) null, "loading...", false);
		jprogressBar = new JProgressBar(0, 100);
		jprogressBar.setPreferredSize(new Dimension(100, 30));
		jprogressBar.setMinimumSize(new Dimension(100, 30));
		getContentPane().add(jprogressBar);
		setSize(200, 100);
		setLocation(200, 200);
		setVisible(true);
	}

	public void setValue(int value) {
		jprogressBar.setValue(value);
	}

	public void destroy() {
		setVisible(false);
		dispose();
	}

	public static void main(String[] arg) {
		ProgressWindow progWindow = new ProgressWindow();
		progWindow.setValue(0);
		for (int i = 0; i < 100; i += 10) {
			try {
				Thread.sleep(1000);
			} catch (Exception e) {
			}
			progWindow.setValue(i);
		}
		progWindow.setValue(100);
		progWindow.destroy();
	}
}
