package mr3.ui;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.prefs.*;

import javax.swing.*;

import mr3.data.*;
import mr3.jgraph.*;

/**
 * @author takeshi morita
 *
 */
public class PrefDialog extends JInternalFrame {

	private JCheckBox isAntialiasBox;
	private JLabel baseURILabel;
	private JTextField baseURIField;

	private JLabel workDirectoryLabel;
	private JTextField workDirectoryField;
	private JButton browseWorkDirectoryButton;

	private JCheckBox isProxy;
	private JTextField proxyHost;
	private JTextField proxyPort;

	private GraphManager gmanager;
	private Preferences userPrefs;

	private JButton applyButton;
	private JButton cancelButton;

	public PrefDialog(GraphManager manager, Preferences prefs) {
		super("Preference", false, false, false);
		gmanager = manager;
		userPrefs = prefs;

		baseURILabel = new JLabel("Base URI:   ");
		baseURIField = new JTextField(20);
		baseURIField.setPreferredSize(new Dimension(300, 20));
		baseURIField.setMinimumSize(new Dimension(300, 20));

		workDirectoryLabel = new JLabel("Work Directory:   ");
		workDirectoryField = new JTextField(20);
		workDirectoryField.setEditable(false);
		workDirectoryField.setPreferredSize(new Dimension(300, 20));
		workDirectoryField.setMinimumSize(new Dimension(300, 20));
		browseWorkDirectoryButton = new JButton("Browse");
		browseWorkDirectoryButton.addActionListener(new BrowseDirectory());

		isProxy = new JCheckBox("Proxy");
		isProxy.addActionListener(new CheckProxy());
		proxyHost = new JTextField(20);
		proxyHost.setPreferredSize(new Dimension(250, 40));
		proxyHost.setMinimumSize(new Dimension(250, 40));
		proxyHost.setBorder(BorderFactory.createTitledBorder("Host"));
		proxyPort = new JTextField(5);
		proxyPort.setPreferredSize(new Dimension(50, 40));
		proxyPort.setMinimumSize(new Dimension(50, 40));
		proxyPort.setBorder(BorderFactory.createTitledBorder("Port"));

		isAntialiasBox = new JCheckBox("Antialias");

		applyButton = new JButton("Apply");
		applyButton.addActionListener(new DesideAction());
		cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(new DesideAction());
		JPanel buttonGroup = new JPanel();
		buttonGroup.add(applyButton);
		buttonGroup.add(cancelButton);

		Container contentPane = getContentPane();
		JPanel innerPanel = new JPanel();

		GridBagLayout gridbag = new GridBagLayout();
		GridBagConstraints c = new GridBagConstraints();
		innerPanel.setLayout(gridbag);

		c.weighty = 5;
		c.anchor = GridBagConstraints.WEST;
		c.gridwidth = GridBagConstraints.RELATIVE;
		gridbag.setConstraints(baseURILabel, c);
		innerPanel.add(baseURILabel);
		c.gridwidth = GridBagConstraints.REMAINDER;
		gridbag.setConstraints(baseURIField, c);
		innerPanel.add(baseURIField);

		c.gridwidth = GridBagConstraints.RELATIVE;
		gridbag.setConstraints(workDirectoryLabel, c);
		innerPanel.add(workDirectoryLabel);
		gridbag.setConstraints(workDirectoryField, c);
		innerPanel.add(workDirectoryField);
		c.gridwidth = GridBagConstraints.REMAINDER;
		gridbag.setConstraints(browseWorkDirectoryButton, c);
		innerPanel.add(browseWorkDirectoryButton);

		c.gridwidth = GridBagConstraints.RELATIVE;
		gridbag.setConstraints(isProxy, c);
		innerPanel.add(isProxy);
		gridbag.setConstraints(proxyHost, c);
		innerPanel.add(proxyHost);
		c.gridwidth = GridBagConstraints.REMAINDER;
		gridbag.setConstraints(proxyPort, c);
		innerPanel.add(proxyPort);

		gridbag.setConstraints(isAntialiasBox, c);
		innerPanel.add(isAntialiasBox);

		c.anchor = GridBagConstraints.CENTER;
		gridbag.setConstraints(buttonGroup, c);
		innerPanel.add(buttonGroup);
		contentPane.add(innerPanel);

		initParameter();

		setSize(new Dimension(550, 250));
		setLocation(100, 100);
		setVisible(true);
	}

	private String getDirectoryName() {
		File currentDirectory = new File(userPrefs.get(PrefConstants.DefaultWorkDirectory, ""));
		JFileChooser jfc = new JFileChooser(currentDirectory);
		jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		jfc.setDialogTitle("Select Directory");
		int fd = jfc.showOpenDialog(this);
		if (fd == JFileChooser.APPROVE_OPTION) {
			return jfc.getSelectedFile().toString();
		}
		return null;
	}

	class BrowseDirectory extends AbstractAction {
		public void actionPerformed(ActionEvent e) {
			String directoryName = getDirectoryName();
			if (directoryName != null) {
				workDirectoryField.setText(directoryName);
			}
		}
	}

	class CheckProxy extends AbstractAction {
		public void actionPerformed(ActionEvent e) {
			proxyHost.setEditable(isProxy.isSelected());
			proxyPort.setEditable(isProxy.isSelected());
		}
	}

	private void initParameter() {
		isAntialiasBox.setSelected(userPrefs.getBoolean(PrefConstants.Antialias, true));
		baseURIField.setText(userPrefs.get(PrefConstants.BaseURI, "http://mr3"));
		workDirectoryField.setText(userPrefs.get(PrefConstants.DefaultWorkDirectory, ""));
		isProxy.setSelected(userPrefs.getBoolean(PrefConstants.Proxy, false));
		proxyHost.setText(userPrefs.get(PrefConstants.ProxyHost, "http://localhost"));
		proxyHost.setEditable(isProxy.isSelected());
		proxyPort.setText(Integer.toString(userPrefs.getInt(PrefConstants.ProxyPort, 3128)));
		proxyPort.setEditable(isProxy.isSelected());
	}

	class DesideAction extends AbstractAction {
		public void actionPerformed(ActionEvent e) {
			if (e.getSource() == applyButton) {
				try {
					userPrefs.putBoolean(PrefConstants.Antialias, isAntialiasBox.isSelected());
					gmanager.setAntialias();
					userPrefs.put(PrefConstants.BaseURI, baseURIField.getText());
					gmanager.setBaseURI(baseURIField.getText());
					userPrefs.put(PrefConstants.DefaultWorkDirectory, workDirectoryField.getText());
					userPrefs.putBoolean(PrefConstants.Proxy, isProxy.isSelected());
					userPrefs.put(PrefConstants.ProxyHost, proxyHost.getText());
					userPrefs.putInt(PrefConstants.ProxyPort, Integer.parseInt(proxyPort.getText()));
				} catch (NumberFormatException nfe) {
					JOptionPane.showMessageDialog(null, "Number Format Exception", "Warning", JOptionPane.ERROR_MESSAGE);
					return;
				}
			}
			setVisible(false);
		}
	}
}
