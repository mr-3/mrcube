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

	private JTabbedPane tab;

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

	private JCheckBox isColorBox;

	private JButton rdfResourceColorButton;
	private JButton literalColorButton;
	private JButton classColorButton;
	private JButton propertyColorButton;
	private JButton selectedColorButton;
	private JButton backgroundColorButton;

	private Color rdfResourceColor;
	private Color literalColor;
	private Color classColor;
	private Color propertyColor;
	private Color selectedColor;
	private Color backgroundColor;

	private JButton applyButton;
	private JButton cancelButton;

	public PrefDialog(GraphManager manager, Preferences prefs) {
		super("Preference", false, false, false);
		gmanager = manager;
		userPrefs = prefs;

		tab = new JTabbedPane();
		tab.add("Base", getBasePanel());
		tab.add("Rendering", getRenderingPanel());
		getContentPane().add(tab);
		getContentPane().add(getButtonGroupPanel(), BorderLayout.SOUTH);

		initParameter();

		setSize(new Dimension(500, 350));
		setLocation(100, 100);
		setVisible(true);
	}

	private JPanel getButtonGroupPanel() {
		applyButton = new JButton("Apply");
		applyButton.addActionListener(new DesideAction());
		cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(new DesideAction());
		JPanel buttonGroup = new JPanel();
		buttonGroup.add(applyButton);
		buttonGroup.add(cancelButton);

		return buttonGroup;
	}

	private JPanel getBasePanel() {
		initEncodingBox();
		initBaseURIField();
		initWorkDirectoryField();
		initProxyField();

		JPanel basePanel = new JPanel();
		GridBagLayout gridbag = new GridBagLayout();
		GridBagConstraints c = new GridBagConstraints();
		basePanel.setLayout(gridbag);
		c.weighty = 5;
		c.anchor = GridBagConstraints.WEST;

		layoutEncodingBox(basePanel, gridbag, c);
		layoutBaseURIField(basePanel, gridbag, c);
		layoutWorkDirectory(basePanel, gridbag, c);
		layoutProxyField(basePanel, gridbag, c);

		return basePanel;
	}

	private void initColorButton(JButton button, String name, int width, int height, Action action) {
		button.setIcon(new ColorSwatch(name));
		button.setPreferredSize(new Dimension(width, height));
		button.addActionListener(action);
	}

	private static final int BUTTON_WIDTH = 200;
	private static final int BUTTON_HEIGHT = 30;

	private JPanel getRenderingPanel() {
		ChangeColorAction action = new ChangeColorAction();
		rdfResourceColorButton = new JButton("RDF Resource Color");
		initColorButton(rdfResourceColorButton, "Resource", BUTTON_WIDTH, BUTTON_HEIGHT, action);
		literalColorButton = new JButton("Literal Color");
		initColorButton(literalColorButton, "Literal", BUTTON_WIDTH, BUTTON_HEIGHT, action);
		classColorButton = new JButton("Class Color");
		initColorButton(classColorButton, "Class", BUTTON_WIDTH, BUTTON_HEIGHT, action);
		propertyColorButton = new JButton("Property Color");
		initColorButton(propertyColorButton, "Property", BUTTON_WIDTH, BUTTON_HEIGHT, action);
		selectedColorButton = new JButton("Selected Color");
		initColorButton(selectedColorButton, "Selected", BUTTON_WIDTH, BUTTON_HEIGHT, action);
		backgroundColorButton = new JButton("Background Color");
		initColorButton(backgroundColorButton, "Background", BUTTON_WIDTH, BUTTON_HEIGHT, action);

		isColorBox = new JCheckBox("Color");
		isAntialiasBox = new JCheckBox("Antialias");

		JPanel renderingPanel = new JPanel();
		GridBagLayout gridbag = new GridBagLayout();
		GridBagConstraints c = new GridBagConstraints();
		renderingPanel.setLayout(gridbag);
		c.weighty = 5;
		c.anchor = GridBagConstraints.WEST;
		c.gridwidth = GridBagConstraints.REMAINDER;

		gridbag.setConstraints(rdfResourceColorButton, c);
		renderingPanel.add(rdfResourceColorButton);
		gridbag.setConstraints(literalColorButton, c);
		renderingPanel.add(literalColorButton);
		gridbag.setConstraints(classColorButton, c);
		renderingPanel.add(classColorButton);
		gridbag.setConstraints(propertyColorButton, c);
		renderingPanel.add(propertyColorButton);
		gridbag.setConstraints(selectedColorButton, c);
		renderingPanel.add(selectedColorButton);
		gridbag.setConstraints(backgroundColorButton, c);
		renderingPanel.add(backgroundColorButton);

		gridbag.setConstraints(isColorBox, c);
		renderingPanel.add(isColorBox);
		gridbag.setConstraints(isAntialiasBox, c);
		renderingPanel.add(isAntialiasBox);

		return renderingPanel;
	}

	class ChangeColorAction extends AbstractAction {
		public void actionPerformed(ActionEvent e) {
			Color current = Color.black;

			if (e.getSource() == rdfResourceColorButton) {
				current = rdfResourceColor;
			} else if (e.getSource() == literalColorButton) {
				current = literalColor;
			} else if (e.getSource() == classColorButton) {
				current = classColor;
			} else if (e.getSource() == propertyColorButton) {
				current = propertyColor;
			} else if (e.getSource() == selectedColorButton) {
				current = selectedColor;
			} else if (e.getSource() == backgroundColorButton) {
				current = backgroundColor;
			}

			Color c = JColorChooser.showDialog(getContentPane(), "Choose Color", current);
			if (c == null) {
				c = current;
			}

			if (e.getSource() == rdfResourceColorButton) {
				rdfResourceColor = c;
			} else if (e.getSource() == literalColorButton) {
				literalColor = c;
			} else if (e.getSource() == classColorButton) {
				classColor = c;
			} else if (e.getSource() == propertyColorButton) {
				propertyColor = c;
			} else if (e.getSource() == selectedColorButton) {
				selectedColor = c;
			} else if (e.getSource() == backgroundColorButton) {
				backgroundColor = c;
			}
		}
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

	private static final int URI_FIELD_WIDTH = 300;

	private void initWorkDirectoryField() {
		workDirectoryLabel = new JLabel("Work Directory:   ");
		workDirectoryField = new JTextField(20);
		workDirectoryField.setEditable(false);
		workDirectoryField.setPreferredSize(new Dimension(URI_FIELD_WIDTH, 20));
		workDirectoryField.setMinimumSize(new Dimension(URI_FIELD_WIDTH, 20));
		browseWorkDirectoryButton = new JButton("Browse");
		browseWorkDirectoryButton.addActionListener(new BrowseDirectory());
	}

	private void initProxyField() {
		isProxy = new JCheckBox("Proxy");
		isProxy.addActionListener(new CheckProxy());
		proxyHost = new JTextField(25);
		proxyHost.setPreferredSize(new Dimension(URI_FIELD_WIDTH, 40));
		proxyHost.setMinimumSize(new Dimension(URI_FIELD_WIDTH, 40));
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
		baseURILabel.setText(userPrefs.get(PrefConstants.BaseURI, MR3Resource.Default_URI.getURI()));
		workDirectoryField.setText(userPrefs.get(PrefConstants.DefaultWorkDirectory, ""));
		isProxy.setSelected(userPrefs.getBoolean(PrefConstants.Proxy, false));
		proxyHost.setText(userPrefs.get(PrefConstants.ProxyHost, "http://localhost"));
		proxyHost.setEditable(isProxy.isSelected());
		proxyPort.setText(Integer.toString(userPrefs.getInt(PrefConstants.ProxyPort, 3128)));
		proxyPort.setEditable(isProxy.isSelected());

		rdfResourceColor = new Color(userPrefs.getInt(PrefConstants.RDFResourceColor, Color.pink.getRGB()));
		literalColor = new Color(userPrefs.getInt(PrefConstants.LiteralColor, Color.orange.getRGB()));
		classColor = new Color(userPrefs.getInt(PrefConstants.ClassColor, Color.green.getRGB()));
		propertyColor = new Color(userPrefs.getInt(PrefConstants.PropertyColor, new Color(255, 158, 62).getRGB()));
		selectedColor = new Color(userPrefs.getInt(PrefConstants.SelectedColor, new Color(255, 255, 50).getRGB()));
		backgroundColor = new Color(userPrefs.getInt(PrefConstants.BackgroundColor, Color.white.getRGB()));

		isColorBox.setSelected(userPrefs.getBoolean(PrefConstants.Color, true));
		isAntialiasBox.setSelected(userPrefs.getBoolean(PrefConstants.Antialias, true));
	}

	class DesideAction extends AbstractAction {
		public void actionPerformed(ActionEvent e) {
			if (e.getSource() == applyButton) {
				try {
					userPrefs.put(PrefConstants.InputEncoding, (String) inputEncodingBox.getSelectedItem());
					userPrefs.put(PrefConstants.OutputEncoding, (String) outputEncodingBox.getSelectedItem());
					userPrefs.put(PrefConstants.BaseURI, baseURILabel.getText());
					gmanager.setBaseURI(baseURILabel.getText());
					userPrefs.put(PrefConstants.DefaultWorkDirectory, workDirectoryField.getText());
					userPrefs.putBoolean(PrefConstants.Proxy, isProxy.isSelected());
					userPrefs.put(PrefConstants.ProxyHost, proxyHost.getText());
					userPrefs.putInt(PrefConstants.ProxyPort, Integer.parseInt(proxyPort.getText()));

					userPrefs.putInt(PrefConstants.RDFResourceColor, rdfResourceColor.getRGB());
					ChangeCellAttributes.rdfResourceColor = rdfResourceColor;
					userPrefs.putInt(PrefConstants.LiteralColor, literalColor.getRGB());
					ChangeCellAttributes.literalColor = literalColor;
					userPrefs.putInt(PrefConstants.ClassColor, classColor.getRGB());
					ChangeCellAttributes.classColor = classColor;
					userPrefs.putInt(PrefConstants.PropertyColor, propertyColor.getRGB());
					ChangeCellAttributes.propertyColor = propertyColor;
					userPrefs.putInt(PrefConstants.SelectedColor, selectedColor.getRGB());
					ChangeCellAttributes.selectedColor = selectedColor;
					userPrefs.putInt(PrefConstants.BackgroundColor, backgroundColor.getRGB());
					gmanager.setGraphBackground(backgroundColor);
					userPrefs.putBoolean(PrefConstants.Color, isColorBox.isSelected());
					ChangeCellAttributes.isColor = isColorBox.isSelected();
					// Colorがあるかないかをチェックした後に，セルの色を変更する．
					ChangeCellAttributes.changeAllCellColor(gmanager);
					
					userPrefs.putBoolean(PrefConstants.Antialias, isAntialiasBox.isSelected());
					gmanager.setAntialias();
				} catch (NumberFormatException nfe) {
					JOptionPane.showInternalMessageDialog(gmanager.getDesktop(), "Number Format Exception", "Warning", JOptionPane.ERROR_MESSAGE);
					return;
				}
			}
			setVisible(false);
		}
	}

	class ColorSwatch implements Icon {
		private String name;

		ColorSwatch(String str) {
			name = str;
		}

		public int getIconWidth() {
			return 11;
		}

		public int getIconHeight() {
			return 11;
		}

		public void paintIcon(Component c, Graphics g, int x, int y) {
			g.setColor(Color.black);
			g.fillRect(x, y, getIconWidth(), getIconHeight());

			if (name.equals("Resource")) {
				g.setColor(rdfResourceColor);
			} else if (name.equals("Literal")) {
				g.setColor(literalColor);
			} else if (name.equals("Class")) {
				g.setColor(classColor);
			} else if (name.equals("Property")) {
				g.setColor(propertyColor);
			} else if (name.equals("Selected")) {
				g.setColor(selectedColor);
			} else if (name.equals("Background")) {
				g.setColor(backgroundColor);
			}

			g.fillRect(x + 2, y + 2, getIconWidth() - 4, getIconHeight() - 4);
		}
	}
}
