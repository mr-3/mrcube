package mr3.ui;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.util.prefs.*;

import javax.swing.*;

import mr3.data.*;
import mr3.jgraph.*;
import mr3.util.*;

/**
 * @author takeshi morita
 *
 */
public class PrefDialog extends JInternalFrame {

	private JCheckBox isAntialiasBox;
	private JComboBox uriPrefixBox;
	private JLabel baseURILabel;

	private JLabel workDirectoryLabel;
	private JTextField workDirectoryField;
	private JButton browseWorkDirectoryButton;

	private JCheckBox isProxy;
	private JTextField proxyHost;
	private JTextField proxyPort;

	private JLabel inputEncodingLabel;
	private ComboBoxModel inputEncodingBoxModel;
	private JComboBox inputEncodingBox;

	private JLabel outputEncodingLabel;
	private ComboBoxModel outputEncodingBoxModel;
	private JComboBox outputEncodingBox;

	private GraphManager gmanager;
	private Preferences userPrefs;

	private JButton applyButton;
	private JButton cancelButton;

	public PrefDialog(GraphManager manager, Preferences prefs) {
		super("Preference", false, false, false);
		gmanager = manager;
		userPrefs = prefs;

		initEncodingBox();
		initBaseURIField();
		initWorkDirectoryField();
		initProxyField();

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

		layoutEncodingBox(innerPanel, gridbag, c);
		layoutBaseURIField(innerPanel, gridbag, c);
		layoutWorkDirectory(innerPanel, gridbag, c);
		layoutProxyField(innerPanel, gridbag, c);

		gridbag.setConstraints(isAntialiasBox, c);
		innerPanel.add(isAntialiasBox);

		c.anchor = GridBagConstraints.CENTER;
		gridbag.setConstraints(buttonGroup, c);
		innerPanel.add(buttonGroup);
		contentPane.add(innerPanel);

		initParameter();

		setSize(new Dimension(500, 350));
		setLocation(100, 100);
		setVisible(true);
	}

	private void initComponent(JComponent component, String title, int width, int height) {
		component.setPreferredSize(new Dimension(width, height));
		component.setMinimumSize(new Dimension(width, height));
		component.setBorder(BorderFactory.createTitledBorder(title));
	}

	private void layoutProxyField(JPanel innerPanel, GridBagLayout gridbag, GridBagConstraints c) {
		JPanel proxyPanel = new JPanel();
		proxyPanel.add(isProxy);
		proxyPanel.add(proxyHost);
		proxyPanel.add(proxyPort);
		gridbag.setConstraints(proxyPanel, c);
		innerPanel.add(proxyPanel);
	}

	private void layoutWorkDirectory(JPanel innerPanel, GridBagLayout gridbag, GridBagConstraints c) {
		JPanel workDirectoryPanel = new JPanel();
		workDirectoryPanel.add(workDirectoryLabel);
		workDirectoryPanel.add(workDirectoryField);
		workDirectoryPanel.add(browseWorkDirectoryButton);
		gridbag.setConstraints(workDirectoryPanel, c);
		innerPanel.add(workDirectoryPanel);
	}

	private void layoutBaseURIField(JPanel innerPanel, GridBagLayout gridbag, GridBagConstraints c) {
		c.gridwidth = GridBagConstraints.REMAINDER;
		JPanel baseURIPanel = new JPanel();
		baseURIPanel.add(uriPrefixBox);
		baseURIPanel.add(baseURILabel);
		gridbag.setConstraints(baseURIPanel, c);
		innerPanel.add(baseURIPanel);
	}

	private void layoutEncodingBox(JPanel innerPanel, GridBagLayout gridbag, GridBagConstraints c) {
		c.gridwidth = GridBagConstraints.RELATIVE;
		gridbag.setConstraints(inputEncodingLabel, c);
		innerPanel.add(inputEncodingLabel);
		c.gridwidth = GridBagConstraints.REMAINDER;
		gridbag.setConstraints(inputEncodingBox, c);
		innerPanel.add(inputEncodingBox);

		c.gridwidth = GridBagConstraints.RELATIVE;
		gridbag.setConstraints(outputEncodingLabel, c);
		innerPanel.add(outputEncodingLabel);
		c.gridwidth = GridBagConstraints.REMAINDER;
		gridbag.setConstraints(outputEncodingBox, c);
		innerPanel.add(outputEncodingBox);
	}

	private void initEncodingBox() {
		inputEncodingLabel = new JLabel("Input Encoding:   ");
		Object[] encodingList = new Object[] { "JISAutoDetect", "SJIS", "EUC_JP", "ISO2022JP", "UTF-8", "UTF-16" };
		inputEncodingBoxModel = new DefaultComboBoxModel(encodingList);
		inputEncodingBox = new JComboBox(inputEncodingBoxModel);
		inputEncodingBox.setPreferredSize(new Dimension(prefixBoxWidth, 30));
		inputEncodingBox.setMinimumSize(new Dimension(prefixBoxWidth, 30));

		outputEncodingLabel = new JLabel("Output Encoding:   ");
		encodingList = new Object[] { "SJIS", "EUC_JP", "ISO2022JP", "UTF-8", "UTF-16" };
		outputEncodingBoxModel = new DefaultComboBoxModel(encodingList);
		outputEncodingBox = new JComboBox(outputEncodingBoxModel);
		outputEncodingBox.setPreferredSize(new Dimension(prefixBoxWidth, 30));
		outputEncodingBox.setMinimumSize(new Dimension(prefixBoxWidth, 30));
	}

	private static final int prefixBoxWidth = 120;
	private static final int prefixBoxHeight = 50;

	private void initBaseURIField() {
		uriPrefixBox = new JComboBox();
		uriPrefixBox.addActionListener(new ChangePrefixAction());
		initComponent(uriPrefixBox, "Prefix", prefixBoxWidth, prefixBoxHeight);
		baseURILabel = new JLabel("");
		initComponent(baseURILabel, "BaseURI", 300, 40);
		initPrefixBox();
	}

	public void initPrefixBox() {
		PrefixNSUtil.setPrefixNSInfoSet(gmanager.getPrefixNSInfoSet());
		uriPrefixBox.setModel(new DefaultComboBoxModel(PrefixNSUtil.getPrefixes().toArray()));
		setPrefix();
	}

	private void setPrefix() {
		for (Iterator i = gmanager.getPrefixNSInfoSet().iterator(); i.hasNext();) {
			PrefixNSInfo prefNSInfo = (PrefixNSInfo) i.next();
			if (prefNSInfo.getNameSpace().equals(gmanager.getBaseURI())) {
				uriPrefixBox.setSelectedItem(prefNSInfo.getPrefix());
				baseURILabel.setText(prefNSInfo.getNameSpace());
				break;
			}
		}
	}

	class ChangePrefixAction extends AbstractAction {
		public void actionPerformed(ActionEvent e) {
			PrefixNSUtil.replacePrefix((String) uriPrefixBox.getSelectedItem(), baseURILabel);
		}
	}

	private void initWorkDirectoryField() {
		workDirectoryLabel = new JLabel("Work Directory:   ");
		workDirectoryField = new JTextField(20);
		workDirectoryField.setEditable(false);
		workDirectoryField.setPreferredSize(new Dimension(300, 20));
		workDirectoryField.setMinimumSize(new Dimension(300, 20));
		browseWorkDirectoryButton = new JButton("Browse");
		browseWorkDirectoryButton.addActionListener(new BrowseDirectory());
	}

	private void initProxyField() {
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
		inputEncodingBox.setSelectedItem(userPrefs.get(PrefConstants.InputEncoding, "SJIS"));
		outputEncodingBox.setSelectedItem(userPrefs.get(PrefConstants.OutputEncoding, "SJIS"));
		isAntialiasBox.setSelected(userPrefs.getBoolean(PrefConstants.Antialias, true));
		baseURILabel.setText(userPrefs.get(PrefConstants.BaseURI, MR3Resource.Default_URI.getURI()));
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
					userPrefs.put(PrefConstants.InputEncoding, (String) inputEncodingBox.getSelectedItem());
					userPrefs.put(PrefConstants.OutputEncoding, (String) outputEncodingBox.getSelectedItem());
					userPrefs.putBoolean(PrefConstants.Antialias, isAntialiasBox.isSelected());
					gmanager.setAntialias();
					userPrefs.put(PrefConstants.BaseURI, baseURILabel.getText());
					gmanager.setBaseURI(baseURILabel.getText());
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
