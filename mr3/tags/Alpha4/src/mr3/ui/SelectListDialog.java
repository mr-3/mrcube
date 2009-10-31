package mr3.ui;
import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.event.*;

/**
 *
 * <subClass>
 * SelectNameSpaceDialog
 * SelectPropertyDialog
 *
 * @auther takeshi morita
 */
public abstract class SelectListDialog extends JDialog implements ListSelectionListener, ActionListener {

	protected boolean isOk;
	protected JLabel uri;
	protected JButton confirm;
	protected JButton cancel;
	protected JList upperList;
	protected JList lowerList;
	protected static Object[] NULL = new Object[0];

	public SelectListDialog(String title) {
		super((Frame) null, title, true);
		Container contentPane = getContentPane();

		upperList = new JList();
		upperList.addListSelectionListener(this);
		JScrollPane upperListScroll = new JScrollPane(upperList);
		upperListScroll.setBorder(BorderFactory.createTitledBorder("NameSpace"));
		upperListScroll.setPreferredSize(new Dimension(350, 100));

		lowerList = new JList();
		lowerList.addListSelectionListener(this);
		JScrollPane lowerListScroll = new JScrollPane(lowerList);
		lowerListScroll.setBorder(BorderFactory.createTitledBorder("LocalName"));
		lowerListScroll.setPreferredSize(new Dimension(350, 100));

		uri = new JLabel();
		uri.setBorder(BorderFactory.createTitledBorder("URI"));
		uri.setPreferredSize(new Dimension(350, 50));
		confirm = new JButton("OK");
		confirm.addActionListener(this);
		cancel = new JButton("Cancel");
		cancel.addActionListener(this);

		JPanel panel = new JPanel();
		GridBagLayout gridbag = new GridBagLayout();
		GridBagConstraints c = new GridBagConstraints();
		panel.setLayout(gridbag);
		c.anchor = GridBagConstraints.PAGE_START;
		c.gridwidth = GridBagConstraints.REMAINDER;
		gridbag.setConstraints(upperListScroll, c);
		panel.add(upperListScroll);
		gridbag.setConstraints(lowerListScroll, c);
		panel.add(lowerListScroll);
		gridbag.setConstraints(uri, c);
		panel.add(uri);
		c.gridwidth = GridBagConstraints.RELATIVE;
		c.weightx = 1.0;
		gridbag.setConstraints(confirm, c);
		panel.add(confirm);
		c.gridwidth = GridBagConstraints.REMAINDER;
		gridbag.setConstraints(cancel, c);
		panel.add(cancel);
		contentPane.add(panel);

		setLocation(100, 100);
		setSize(new Dimension(400, 350));
		setVisible(false);
	}

	public Object getValue() {
		String uriStr = uri.getText();

		uri.setText("");
		upperList.clearSelection();
		lowerList.clearSelection();
		lowerList.setListData(NULL);
		if (isOk) {
			isOk = false;
			return uriStr;
		} else {
			return null;
		}
	}

	public abstract void valueChanged(ListSelectionEvent e);

	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == confirm) {
			if (upperList.isSelectionEmpty() || lowerList.isSelectionEmpty()) {
				System.out.println("list is not Selected");
				return;
			} else {
				isOk = true;
			}
		} else if (e.getSource() == cancel) {
			isOk = false;
		}

		setVisible(false);
	}
}
