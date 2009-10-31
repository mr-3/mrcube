package mr3.ui;

import java.awt.*;
import java.awt.event.*;
import java.util.prefs.*;

import javax.swing.*;

import mr3.jgraph.*;

/**
 * @author takeshi morita
 *
 */
public class PrefDialog extends JInternalFrame {

	private JCheckBox isAntialiasBox;
	private JLabel baseURILabel;
	private JTextField baseURIField;

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

		isAntialiasBox = new JCheckBox("Antialias");

		baseURILabel = new JLabel("Base URI:   ");
		baseURIField = new JTextField(20);
		baseURIField.setPreferredSize(new Dimension(250, 20));
		baseURIField.setMinimumSize(new Dimension(250, 20));

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

		applyButton = new JButton("Apply");
		applyButton.addActionListener(new DesideAction());
		cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(new DesideAction());
		JPanel buttonGroup = new JPanel();
		buttonGroup.add(applyButton);
		buttonGroup.add(cancelButton);

		Container contentPane = getContentPane();
		GridBagLayout gridbag = new GridBagLayout();
		GridBagConstraints c = new GridBagConstraints();
		contentPane.setLayout(gridbag);
		c.weighty = 5;
		c.anchor = GridBagConstraints.WEST;
		c.gridwidth = GridBagConstraints.REMAINDER;
		gridbag.setConstraints(isAntialiasBox, c);
		contentPane.add(isAntialiasBox);
		c.gridwidth = GridBagConstraints.RELATIVE;
		gridbag.setConstraints(baseURILabel, c);
		contentPane.add(baseURILabel);
		c.gridwidth = GridBagConstraints.REMAINDER;
		gridbag.setConstraints(baseURIField, c);
		contentPane.add(baseURIField);
		c.gridwidth = GridBagConstraints.RELATIVE;
		gridbag.setConstraints(isProxy, c);
		contentPane.add(isProxy);
		gridbag.setConstraints(proxyHost, c);
		contentPane.add(proxyHost);
		c.gridwidth = GridBagConstraints.REMAINDER;
		gridbag.setConstraints(proxyPort, c);
		contentPane.add(proxyPort);
		c.anchor = GridBagConstraints.CENTER;
		gridbag.setConstraints(buttonGroup, c);
		contentPane.add(buttonGroup);

		initParameter();

		setSize(new Dimension(450, 200));
		setLocation(100, 100);
		setVisible(true);
	}

	class CheckProxy extends AbstractAction {
		public void actionPerformed(ActionEvent e) {
			proxyHost.setEditable(isProxy.isSelected());
			proxyPort.setEditable(isProxy.isSelected());
		}
	}

	private static final String Antialias = "Antialias";
	private static final String BaseURI = "Base URI";
	private static final String Proxy = "Proxy";
	private static final String ProxyHost = "Proxy Host";
	private static final String ProxyPort = "Proxy Port";

	private void initParameter() {
		isAntialiasBox.setSelected(userPrefs.getBoolean(Antialias, true));
		baseURIField.setText(userPrefs.get(BaseURI, "http://mr3"));
		isProxy.setSelected(userPrefs.getBoolean(Proxy, false));
		proxyHost.setText(userPrefs.get(ProxyHost, "http://localhost"));
		proxyHost.setEditable(isProxy.isSelected());
		proxyPort.setText(Integer.toString(userPrefs.getInt(ProxyPort, 3128)));
		proxyPort.setEditable(isProxy.isSelected());
	}

	class DesideAction extends AbstractAction {
		public void actionPerformed(ActionEvent e) {
			if (e.getSource() == applyButton) {
				try {
					userPrefs.putBoolean(Antialias, isAntialiasBox.isSelected());
					gmanager.setAntialias();
					userPrefs.put(BaseURI, baseURIField.getText());
					gmanager.setBaseURI(baseURIField.getText());
					userPrefs.putBoolean(Proxy, isProxy.isSelected());
					userPrefs.put(ProxyHost, proxyHost.getText());
					userPrefs.putInt(ProxyPort, Integer.parseInt(proxyPort.getText()));
				} catch (NumberFormatException nfe) {
					JOptionPane.showMessageDialog(null, "Number Format Exception", "Warning", JOptionPane.ERROR_MESSAGE);
					return;
				}
			}
			setVisible(false);
		}
	}
}
