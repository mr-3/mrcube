/*
 * Project Name: MR^3 (Meta-Model Management based on RDFs Revision Reflection)
 * Project Website: http://mr3.sourceforge.net/
 * 
 * Copyright (C) 2003-2008 Yamaguchi Laboratory, Keio University. All rights reserved. 
 * 
 * This file is part of MR^3.
 * 
 * MR^3 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * MR^3 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with MR^3.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */

package org.semanticweb.mmm.mr3.ui;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.lang.ref.*;
import java.util.*;
import java.util.List;
import java.util.prefs.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;

import org.jgraph.graph.*;
import org.semanticweb.mmm.mr3.data.*;
import org.semanticweb.mmm.mr3.jgraph.*;
import org.semanticweb.mmm.mr3.layout.*;
import org.semanticweb.mmm.mr3.util.*;
import org.semanticweb.mmm.mr3.util.Utilities;

import say.swing.*;

import com.hp.hpl.jena.vocabulary.*;

/**
 * @author takeshi morita
 * 
 */
public class OptionDialog extends JDialog implements ListSelectionListener {

    private JList menuList;
    private JPanel mainPanel;

    private CardLayout cardLayout;

    private BasePanel basePanel;
    private DirectoryPanel directoryPanel;
    private ProxyPanel proxyPanel;
    private MetaClassPanel metaClassPanel;
    private LayoutPanel layoutPanel;
    private RenderingPanel renderingPanel;

    private GraphManager gmanager;
    private Preferences userPrefs;

    private JButton applyButton;
    private JButton confirmButton;
    private JButton cancelButton;

    private static final int WINDOW_WIDTH = 550;
    private static final int WINDOW_HEIGHT = 400;

    public static JComponent topLevelComponent;

    public OptionDialog(GraphManager gm, Preferences prefs) {
        super(gm.getRootFrame(), Translator.getString("PreferenceDialog.Title"), true);
        gmanager = gm;
        userPrefs = prefs;
        loadResourceBundle();

        basePanel = new BasePanel();
        directoryPanel = new DirectoryPanel();
        proxyPanel = new ProxyPanel();
        metaClassPanel = new MetaClassPanel();
        layoutPanel = new LayoutPanel();
        renderingPanel = new RenderingPanel();

        menuList = new JList(new Object[] { basePanel, directoryPanel, proxyPanel, metaClassPanel, layoutPanel,
                renderingPanel});
        menuList.addListSelectionListener(this);
        JComponent menuListPanel = Utilities.createTitledPanel(menuList, "", 100, 100);

        mainPanel = new JPanel();
        cardLayout = new CardLayout(5, 5);
        mainPanel.setLayout(cardLayout);

        mainPanel.add(basePanel.toString(), basePanel);
        mainPanel.add(directoryPanel.toString(), directoryPanel);
        mainPanel.add(proxyPanel.toString(), proxyPanel);
        mainPanel.add(metaClassPanel.toString(), metaClassPanel);
        mainPanel.add(layoutPanel.toString(), layoutPanel);
        mainPanel.add(renderingPanel.toString(), renderingPanel);

        JPanel topLevelPanel = new JPanel();
        topLevelPanel.setLayout(new BorderLayout());
        topLevelPanel.add(menuListPanel, BorderLayout.WEST);
        topLevelPanel.add(mainPanel, BorderLayout.CENTER);
        topLevelPanel.add(getButtonGroupPanel(), BorderLayout.SOUTH);
        topLevelComponent = topLevelPanel;

        getContentPane().add(topLevelPanel);
        // getContentPane().add(menuListPanel, BorderLayout.WEST);
        // getContentPane().add(mainPanel, BorderLayout.CENTER);
        // getContentPane().add(getButtonGroupPanel(), BorderLayout.SOUTH);

        menuList.setSelectedIndex(0);

        Dimension size = new Dimension(WINDOW_WIDTH, WINDOW_HEIGHT);
        setSize(size);
        setLocationRelativeTo(gmanager.getRootFrame());
        setVisible(false);
    }

    private static String EDIT;
    private static String ADD;
    private static String REMOVE;

    public static void loadResourceBundle() {
        EDIT = Translator.getString("Edit");
        ADD = Translator.getString("Add");
        REMOVE = Translator.getString("Remove");
    }

    public void valueChanged(ListSelectionEvent e) {
        cardLayout.show(mainPanel, menuList.getSelectedValue().toString());
    }

    private JComponent getButtonGroupPanel() {
        DecideAction decideAction = new DecideAction();
        applyButton = new JButton(MR3Constants.APPLY);
        applyButton.setMnemonic('a');
        applyButton.addActionListener(decideAction);
        confirmButton = new JButton(MR3Constants.OK);
        confirmButton.setMnemonic('o');
        confirmButton.addActionListener(decideAction);
        cancelButton = new JButton(MR3Constants.CANCEL);
        cancelButton.setMnemonic('c');
        cancelButton.addActionListener(decideAction);
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(1, 3, 5, 5));
        buttonPanel.add(applyButton);
        buttonPanel.add(confirmButton);
        buttonPanel.add(cancelButton);
        return Utilities.createEastPanel(buttonPanel);
    }

    private JComponent getTitleField(String title) {
        JTextField titleField = new JTextField(title);
        titleField.setBackground(new Color(49, 105, 198));
        titleField.setForeground(Color.white);
        titleField.setFont(new Font("SansSerif", Font.BOLD, 14));
        titleField.setEditable(false);
        return titleField;
    }

    private JComponent getTitledPanel(JComponent panel, String title) {
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout(5, 10));
        mainPanel.add(getTitleField(title), BorderLayout.NORTH);
        mainPanel.add(panel, BorderLayout.CENTER);
        return mainPanel;
    }

    class BasePanel extends JPanel {

        private JTextField defaultLangField;
        private JComboBox uiLangBox;
        private ComboBoxModel outputEncodingBoxModel;
        private JComboBox outputEncodingBox;
        private JLabel fontSettingValueLabel;
        private JButton fontSettingButton;
        private JComboBox uriPrefixBox;
        private JLabel baseURILabel;
        private JCheckBox isLogAvailableCheckBox;
        private JTextField logFileField;
        private JButton browseLogFileButton;

        BasePanel() {
            JPanel panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
            panel.add(getLangPanel());
            panel.add(getUILangPanel());
            panel.add(getEncodingPanel());
            panel.add(getFontSettingPanel());
            panel.add(getBaseURIPanel());
            panel.add(getLogFilePanel());
            setLayout(new BorderLayout());
            add(getTitledPanel(panel, toString()), BorderLayout.NORTH);
            setBorder(BorderFactory.createEtchedBorder());
        }

        public String toString() {
            return Translator.getString("PreferenceDialog.BaseTab");
        }

        public void setConfig() {
            if (!userPrefs.get(PrefConstants.UILang, "en").equals(uiLangBox.getSelectedItem())) {
                userPrefs.put(PrefConstants.UILang, (String) uiLangBox.getSelectedItem());
                // Translator.loadResourceBundle(userPrefs);
            }

            userPrefs.put(PrefConstants.DefaultLang, defaultLangField.getText());
            // userPrefs.put(PrefConstants.InputEncoding, (String)
            // inputEncodingBox.getSelectedItem());
            userPrefs.put(PrefConstants.OutputEncoding, (String) outputEncodingBox.getSelectedItem());
            userPrefs.put(PrefConstants.BaseURI, baseURILabel.getText());
            gmanager.setBaseURI(baseURILabel.getText());
            if (isLogAvailableCheckBox.isSelected()) {
                userPrefs.put(PrefConstants.isLogAvailable, "true");
            } else {
                userPrefs.put(PrefConstants.isLogAvailable, "false");
            }
            userPrefs.put(PrefConstants.logFile, logFileField.getText());
            HistoryManager.resetFileAppender(logFileField.getText());
            PluginManager.reloadPlugins();
        }

        public void resetConfig() {
            initPrefixBox();
            uiLangBox.setModel(new DefaultComboBoxModel(getUILanguages()));
            uiLangBox.setSelectedItem(userPrefs.get(PrefConstants.UILang, "en"));
            defaultLangField.setText(userPrefs.get(PrefConstants.DefaultLang, "ja"));
            // inputEncodingBox.setSelectedItem(userPrefs.get(PrefConstants.
            // InputEncoding,
            // "SJIS"));
            outputEncodingBox.setSelectedItem(userPrefs.get(PrefConstants.OutputEncoding, "SJIS"));
            baseURILabel.setText(userPrefs.get(PrefConstants.BaseURI, MR3Resource.DefaultURI.getURI()));
            if (userPrefs.get(PrefConstants.isLogAvailable, "false").equals("true")) {
                isLogAvailableCheckBox.setSelected(true);
            } else {
                isLogAvailableCheckBox.setSelected(false);
            }
            setText(logFileField, userPrefs.get(PrefConstants.logFile, System.getProperty("user.dir") + "\\mr3.log"));
            HistoryManager.resetFileAppender(logFileField.getText());
        }

        class ChangePrefixAction extends AbstractAction {
            public void actionPerformed(ActionEvent e) {
                PrefixNSUtil.replacePrefix((String) uriPrefixBox.getSelectedItem(), baseURILabel);
            }
        }

        private JComponent getLangPanel() {
            defaultLangField = new JTextField();
            defaultLangField.setPreferredSize(new Dimension(PREFIX_BOX_WIDTH, PREFIX_BOX_HEIGHT));
            JPanel defaultLangPanel = new JPanel();
            defaultLangPanel.setLayout(new GridLayout(1, 2, 5, 5));
            defaultLangPanel.add(new JLabel(Translator.getString("Lang") + ": "));
            defaultLangPanel.add(defaultLangField);

            return Utilities.createWestPanel(defaultLangPanel);
        }

        private Object[] getLanguages(File resourceDir) {
            Set<String> langSet = new TreeSet<String>();
            try {
                for (File resFile : resourceDir.listFiles()) {
                    if (resFile.getName().matches("MR3_.*\\.properties")) {
                        String lang = resFile.getName().split("_")[1].split("\\.")[0];
                        langSet.add(lang);
                    }
                }
            } catch (Exception e) {// Java Web Startのための処理
                return new Object[] { "en", "ja", "zh"};
            }
            return langSet.toArray();
        }

        private Object[] getSystemUILanguages() {
            File resourceDir = new File(System.getProperty("user.dir") + "\\resources");
            return getLanguages(resourceDir);
        }

        private Object[] getUILanguages() {
            File resourceDir = new File(userPrefs.get(PrefConstants.ResourceDirectory, System.getProperty("user.dir")
                    + "\\resources"));
            Object[] languages = getLanguages(resourceDir);
            if (0 < languages.length) { return languages; }
            return getSystemUILanguages();
        }

        private JComponent getUILangPanel() {
            uiLangBox = new JComboBox(getUILanguages());
            uiLangBox.setSelectedItem(userPrefs.get(PrefConstants.UILang, "en"));
            uiLangBox.setPreferredSize(new Dimension(PREFIX_BOX_WIDTH, PREFIX_BOX_HEIGHT));
            JPanel uiLangPanel = new JPanel();
            uiLangPanel.setLayout(new GridLayout(1, 2, 5, 5));
            uiLangPanel.add(new JLabel("UI" + Translator.getString("Lang") + ": "));
            uiLangPanel.add(uiLangBox);

            return Utilities.createWestPanel(uiLangPanel);
        }

        private JComponent getEncodingPanel() {
            Object[] encodingList = new Object[] { "JISAutoDetect", "SJIS", "EUC_JP", "ISO2022JP", "UTF-8", "UTF-16"};
            encodingList = new Object[] { "SJIS", "EUC_JP", "ISO2022JP", "UTF-8", "UTF-16"};
            outputEncodingBoxModel = new DefaultComboBoxModel(encodingList);
            outputEncodingBox = new JComboBox(outputEncodingBoxModel);
            outputEncodingBox.setPreferredSize(new Dimension(PREFIX_BOX_WIDTH, PREFIX_BOX_HEIGHT));
            JPanel encodingPanel = new JPanel();
            encodingPanel.setLayout(new GridLayout(1, 2, 5, 5));
            encodingPanel.add(new JLabel(Translator.getString("PreferenceDialog.BaseTab.OutputEncoding") + ": "));
            encodingPanel.add(outputEncodingBox);

            return Utilities.createWestPanel(encodingPanel);
        }

        private JComponent getFontSettingPanel() {
            fontSettingValueLabel = new JLabel(getFont().getFontName() + "-" + getFont().getSize());
            fontSettingButton = new JButton(Translator.getString("PreferenceDialog.BaseTab.FontSetting.Button"));
            fontSettingButton
                    .addActionListener(new ChooseFontAction(Translator.getString("Component.View.ChooseFont")));
            JPanel fontSettingPanel = new JPanel();
            fontSettingPanel.setLayout(new GridLayout(1, 3, 5, 5));
            fontSettingPanel.add(new JLabel(Translator.getString("PreferenceDialog.BaseTab.FontSetting") + ": "));
            fontSettingPanel.add(fontSettingValueLabel);
            fontSettingPanel.add(fontSettingButton);
            return Utilities.createWestPanel(fontSettingPanel);
        }

        public class ChooseFontAction extends AbstractAction {

            private WeakReference<JFontChooser> jfontChooserRef;

            public ChooseFontAction(String name) {
                super(name);
                jfontChooserRef = new WeakReference<JFontChooser>(null);
            }

            private JFontChooser getJFontChooser() {
                JFontChooser result = jfontChooserRef.get();
                if (result == null) {
                    result = new JFontChooser();
                    jfontChooserRef = new WeakReference<JFontChooser>(result);
                }
                return result;
            }

            public void actionPerformed(ActionEvent arg0) {
                JFontChooser jfontChooser = getJFontChooser();
                if (GraphUtilities.defaultFont == null) {
                    GraphUtilities.defaultFont = getFont();
                }
                jfontChooser.setSelectedFont(GraphUtilities.defaultFont);
                int result = jfontChooser.showDialog(gmanager.getRootFrame());
                if (result == JFontChooser.OK_OPTION) {
                    // System.out.println(jfontChooser.getSelectedFont());
                    Font font = jfontChooser.getSelectedFont();
                    fontSettingValueLabel.setText(font.getFontName() + "-" + font.getSize());
                    GraphUtilities.defaultFont = font;
                    setGraphFont(gmanager.getCurrentRDFGraph(), font);
                    setGraphFont(gmanager.getCurrentClassGraph(), font);
                    setGraphFont(gmanager.getCurrentPropertyGraph(), font);
                }
            }

            private void setGraphFont(RDFGraph graph, Font font) {
                Object[] cells = graph.getAllCells();
                for (int i = 0; i < cells.length; i++) {
                    if (cells[i] instanceof GraphCell) {
                        GraphCell cell = (GraphCell) cells[i];
                        AttributeMap map = cell.getAttributes();
                        GraphConstants.setFont(map, font);
                        GraphUtilities.editCell(cell, map, graph);
                    }
                }
            }
        }

        private JComponent getBaseURIPanel() {
            uriPrefixBox = new JComboBox();
            uriPrefixBox.addActionListener(new ChangePrefixAction());
            JComponent uriPrefixBoxP = Utilities.createTitledPanel(uriPrefixBox, MR3Constants.PREFIX);
            baseURILabel = new JLabel("");
            JComponent baseURILabelP = Utilities.createTitledPanel(baseURILabel, "URI");
            initPrefixBox();

            JPanel baseURIPanel = new JPanel();
            baseURIPanel.setLayout(new BoxLayout(baseURIPanel, BoxLayout.X_AXIS));
            baseURIPanel.setBorder(BorderFactory.createTitledBorder(Translator
                    .getString("PreferenceDialog.BaseTab.BaseURI")));
            baseURIPanel.add(uriPrefixBoxP);
            baseURIPanel.add(baseURILabelP);

            return baseURIPanel;
        }

        class BrowseFile extends AbstractAction {
            private JTextField fileField;

            BrowseFile(JTextField field) {
                fileField = field;
            }

            private String getFileName() {
                File currentFile = null;
                if (fileField == logFileField) {
                    currentFile = new File(userPrefs.get(PrefConstants.logFile, ""));
                }

                JFileChooser jfc = new JFileChooser(currentFile);
                jfc.setFileSelectionMode(JFileChooser.FILES_ONLY);
                jfc.setDialogTitle("Select File");
                int fd = jfc.showOpenDialog(gmanager.getRootFrame());
                if (fd == JFileChooser.APPROVE_OPTION) { return jfc.getSelectedFile().toString(); }
                return null;
            }

            public void actionPerformed(ActionEvent e) {
                String fileName = getFileName();
                if (fileName != null) {
                    setText(fileField, fileName);
                }
            }
        }

        private JPanel getLogFilePanel() {
            isLogAvailableCheckBox = new JCheckBox(Translator.getString("PreferenceDialog.BaseTab.LogFile.check"),
                    false);
            logFileField = new JTextField(15);
            logFileField.setEditable(false);
            browseLogFileButton = new JButton(Translator.getString("PreferenceDialog.DirectoryTab.Browse") + "(L)");
            browseLogFileButton.setMnemonic('l');
            browseLogFileButton.addActionListener(new BrowseFile(logFileField));

            JPanel logFilePanel = new JPanel();
            logFilePanel.setLayout(new BorderLayout());
            logFilePanel.setBorder(BorderFactory.createTitledBorder(Translator
                    .getString("PreferenceDialog.BaseTab.LogFile")));
            logFilePanel.add(isLogAvailableCheckBox, BorderLayout.WEST);
            logFilePanel.add(logFileField, BorderLayout.CENTER);
            logFilePanel.add(browseLogFileButton, BorderLayout.EAST);
            return logFilePanel;
        }

        private void initPrefixBox() {
            PrefixNSUtil.setPrefixNSInfoSet(GraphUtilities.getPrefixNSInfoSet());
            uriPrefixBox.setModel(new DefaultComboBoxModel(PrefixNSUtil.getPrefixes().toArray()));
            setPrefix();
        }

        private void setPrefix() {
            for (PrefixNSInfo prefNSInfo : GraphUtilities.getPrefixNSInfoSet()) {
                if (prefNSInfo.getNameSpace().equals(gmanager.getBaseURI())) {
                    uriPrefixBox.setSelectedItem(prefNSInfo.getPrefix());
                    baseURILabel.setText(prefNSInfo.getNameSpace());
                    break;
                }
            }
        }
    }

    class DirectoryPanel extends JPanel {

        private JTextField workDirectoryField;
        private JButton browseWorkDirectoryButton;
        private JTextField pluginsDirectoryField;
        private JButton browsePluginsDirectoryButton;
        private JTextField resourceDirectoryField;
        private JButton browseResourceDirectoryButton;

        DirectoryPanel() {
            initWorkDirectoryField();
            initPluginsDirectoryField();
            initResourceDirectoryField();

            JPanel panel = new JPanel();
            panel.setLayout(new GridLayout(3, 1, 10, 5));
            panel.add(getWorkDirectoryPanel());
            panel.add(getPluginDirectoryPanel());
            panel.add(getResourceDirectoryPanel());
            setLayout(new BorderLayout());
            add(getTitledPanel(panel, toString()), BorderLayout.NORTH);
            setBorder(BorderFactory.createEtchedBorder());
        }

        public String toString() {
            return Translator.getString("PreferenceDialog.DirectoryTab");
        }

        public void setConfig() {
            userPrefs.put(PrefConstants.WorkDirectory, workDirectoryField.getText());
            userPrefs.put(PrefConstants.PluginsDirectory, pluginsDirectoryField.getText());
            userPrefs.put(PrefConstants.ResourceDirectory, resourceDirectoryField.getText());
        }

        public void resetConfig() {
            setText(workDirectoryField, userPrefs.get(PrefConstants.WorkDirectory, ""));
            setText(pluginsDirectoryField, userPrefs.get(PrefConstants.PluginsDirectory, System.getProperty("user.dir")
                    + "\\plugins"));
            setText(resourceDirectoryField, userPrefs.get(PrefConstants.ResourceDirectory, System
                    .getProperty("user.dir")
                    + "\\resources"));
        }

        class BrowseDirectory extends AbstractAction {
            private JTextField directoryField;

            BrowseDirectory(JTextField field) {
                directoryField = field;
            }

            private String getDirectoryName() {
                File currentDirectory = null;
                if (directoryField == workDirectoryField) {
                    currentDirectory = new File(userPrefs.get(PrefConstants.WorkDirectory, ""));
                } else if (directoryField == pluginsDirectoryField) {
                    currentDirectory = new File(userPrefs.get(PrefConstants.PluginsDirectory, ""));
                } else if (directoryField == resourceDirectoryField) {
                    currentDirectory = new File(userPrefs.get(PrefConstants.ResourceDirectory, ""));
                }

                JFileChooser jfc = new JFileChooser(currentDirectory);
                jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                jfc.setDialogTitle("Select Directory");
                int fd = jfc.showOpenDialog(gmanager.getRootFrame());
                if (fd == JFileChooser.APPROVE_OPTION) { return jfc.getSelectedFile().toString(); }
                return null;
            }

            public void actionPerformed(ActionEvent e) {
                String directoryName = getDirectoryName();
                if (directoryName != null) {
                    setText(directoryField, directoryName);
                }
            }
        }

        private void initWorkDirectoryField() {
            workDirectoryField = new JTextField(15);
            workDirectoryField.setEditable(false);
            browseWorkDirectoryButton = new JButton(Translator.getString("PreferenceDialog.DirectoryTab.Browse")
                    + "(W)");
            browseWorkDirectoryButton.setMnemonic('w');
            browseWorkDirectoryButton.addActionListener(new BrowseDirectory(workDirectoryField));
        }

        private void initPluginsDirectoryField() {
            pluginsDirectoryField = new JTextField(15);
            pluginsDirectoryField.setEditable(false);
            browsePluginsDirectoryButton = new JButton(Translator.getString("PreferenceDialog.DirectoryTab.Browse")
                    + "(P)");
            browsePluginsDirectoryButton.setMnemonic('p');
            browsePluginsDirectoryButton.addActionListener(new BrowseDirectory(pluginsDirectoryField));
        }

        private void initResourceDirectoryField() {
            resourceDirectoryField = new JTextField(15);
            resourceDirectoryField.setEditable(false);
            browseResourceDirectoryButton = new JButton(Translator.getString("PreferenceDialog.DirectoryTab.Browse")
                    + "(R)");
            browseResourceDirectoryButton.setMnemonic('r');
            browseResourceDirectoryButton.addActionListener(new BrowseDirectory(resourceDirectoryField));
        }

        private JPanel getWorkDirectoryPanel() {
            JPanel workDirectoryPanel = new JPanel();
            workDirectoryPanel.setLayout(new BoxLayout(workDirectoryPanel, BoxLayout.X_AXIS));
            workDirectoryPanel.setBorder(BorderFactory.createTitledBorder(Translator
                    .getString("PreferenceDialog.DirectoryTab.WorkDirectory")));
            workDirectoryPanel.add(workDirectoryField);
            workDirectoryPanel.add(browseWorkDirectoryButton);

            return workDirectoryPanel;
        }

        private JPanel getPluginDirectoryPanel() {
            JPanel pluginsDirectoryPanel = new JPanel();
            pluginsDirectoryPanel.setLayout(new BoxLayout(pluginsDirectoryPanel, BoxLayout.X_AXIS));
            pluginsDirectoryPanel.setBorder(BorderFactory.createTitledBorder(Translator
                    .getString("PreferenceDialog.DirectoryTab.PluginsDirectory")));
            pluginsDirectoryPanel.add(pluginsDirectoryField);
            pluginsDirectoryPanel.add(browsePluginsDirectoryButton);
            return pluginsDirectoryPanel;
        }

        private JPanel getResourceDirectoryPanel() {
            JPanel resourceDirectoryPanel = new JPanel();
            resourceDirectoryPanel.setLayout(new BoxLayout(resourceDirectoryPanel, BoxLayout.X_AXIS));
            resourceDirectoryPanel.setBorder(BorderFactory.createTitledBorder(Translator
                    .getString("PreferenceDialog.DirectoryTab.ResourcesDirectory")));
            resourceDirectoryPanel.add(resourceDirectoryField);
            resourceDirectoryPanel.add(browseResourceDirectoryButton);

            return resourceDirectoryPanel;
        }

    }

    class ProxyPanel extends JPanel {

        private JCheckBox isProxy;
        private JTextField proxyHost;
        private JTextField proxyPort;

        ProxyPanel() {
            isProxy = new JCheckBox(Translator.getString("PreferenceDialog.ProxyTab"));
            isProxy.addActionListener(new CheckProxy());
            proxyHost = new JTextField(25);
            JComponent proxyHostP = Utilities.createTitledPanel(proxyHost, Translator
                    .getString("PreferenceDialog.ProxyTab.Host"));
            proxyPort = new JTextField(5);
            JComponent proxyPortP = Utilities.createTitledPanel(proxyPort, Translator
                    .getString("PreferenceDialog.ProxyTab.Port"));

            JPanel panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
            panel.add(isProxy);
            panel.add(proxyHostP);
            panel.add(proxyPortP);
            setLayout(new BorderLayout());
            add(getTitledPanel(panel, toString()), BorderLayout.NORTH);
            setBorder(BorderFactory.createEtchedBorder());
        }

        public String toString() {
            return Translator.getString("PreferenceDialog.ProxyTab");
        }

        class CheckProxy extends AbstractAction {
            public void actionPerformed(ActionEvent e) {
                proxyHost.setEditable(isProxy.isSelected());
                proxyPort.setEditable(isProxy.isSelected());
            }
        }

        public void setConfig() {
            userPrefs.putBoolean(PrefConstants.Proxy, isProxy.isSelected());
            userPrefs.put(PrefConstants.ProxyHost, proxyHost.getText());
            userPrefs.putInt(PrefConstants.ProxyPort, Integer.parseInt(proxyPort.getText()));
        }

        public void resetConfig() {
            isProxy.setSelected(userPrefs.getBoolean(PrefConstants.Proxy, false));
            setText(proxyHost, userPrefs.get(PrefConstants.ProxyHost, "http://localhost"));
            proxyHost.setEditable(isProxy.isSelected());
            proxyPort.setText(Integer.toString(userPrefs.getInt(PrefConstants.ProxyPort, 3128)));
            proxyPort.setEditable(isProxy.isSelected());
        }
    }

    class MetaClassPanel extends JPanel implements ListSelectionListener {

        private JTextField metaClassField;

        private JTextField defaultClassClassField;
        private JList classClassList;
        private DefaultListModel classClassListModel;
        private JButton setDefaultClassClassButton;
        private JButton classClassEditButton;
        private JButton classClassAddButton;
        private JButton classClassRemoveButton;

        private JTextField defaultPropertyClassField;
        private JList propClassList;
        private DefaultListModel propClassListModel;
        private JButton setDefaultPropertyClassButton;
        private JButton propClassEditButton;
        private JButton propClassAddButton;
        private JButton propClassRemoveButton;

        private static final int LONG_URI_FIELD_WIDTH = 450;
        private static final int LIST_HEIGHT = 150;

        MetaClassPanel() {
            metaClassField = new JTextField();
            JComponent metaClassFieldP = Utilities.createTitledPanel(metaClassField, "URI");

            defaultClassClassField = new JTextField();
            defaultClassClassField.setEditable(false);
            JComponent defaultClassClassFieldP = Utilities.createTitledPanel(defaultClassClassField,
                    "Default Class Class");
            classClassListModel = new DefaultListModel();
            classClassList = new JList(classClassListModel);
            classClassList.addListSelectionListener(this);
            JScrollPane classClassListScroll = new JScrollPane(classClassList);
            Utilities.initComponent(classClassListScroll, Translator
                    .getString("PreferenceDialog.MetaClassTab.ClassClass"), LONG_URI_FIELD_WIDTH, LIST_HEIGHT);

            defaultPropertyClassField = new JTextField();
            defaultPropertyClassField.setEditable(false);
            JComponent defaultPropertyClassFieldP = Utilities.createTitledPanel(defaultPropertyClassField,
                    "Default Property Class");
            propClassListModel = new DefaultListModel();
            propClassList = new JList(propClassListModel);
            propClassList.addListSelectionListener(this);
            JScrollPane propClassListScroll = new JScrollPane(propClassList);
            Utilities.initComponent(propClassListScroll, Translator
                    .getString("PreferenceDialog.MetaClassTab.PropertyClass"), LONG_URI_FIELD_WIDTH, LIST_HEIGHT);

            JPanel classClassPanel = new JPanel();
            classClassPanel.setLayout(new BoxLayout(classClassPanel, BoxLayout.Y_AXIS));
            classClassPanel.add(defaultClassClassFieldP);
            classClassPanel.add(classClassListScroll);
            classClassPanel.add(getClassClassButtonPanel());

            JPanel propertyClassPanel = new JPanel();
            propertyClassPanel.setLayout(new BoxLayout(propertyClassPanel, BoxLayout.Y_AXIS));
            propertyClassPanel.add(defaultPropertyClassFieldP);
            propertyClassPanel.add(propClassListScroll);
            propertyClassPanel.add(getPropertyClassButtonPanel());

            JTabbedPane tab = new JTabbedPane();
            tab.add(Utilities.createNorthPanel(classClassPanel), "Class Class");
            tab.add(Utilities.createNorthPanel(propertyClassPanel), "Property Class");

            setLayout(new BorderLayout());
            add(getTitledPanel(metaClassFieldP, toString()), BorderLayout.NORTH);
            add(tab, BorderLayout.CENTER);
            setBorder(BorderFactory.createEtchedBorder());
        }

        public String toString() {
            return Translator.getString("PreferenceDialog.MetaClassTab");
        }

        public void setConfig() {
            userPrefs.put(PrefConstants.DefaultClassClass, defaultClassClassField.getText());
            userPrefs.put(PrefConstants.ClassClassList, getMetaClassStr(classClassListModel.toArray()));
            userPrefs.put(PrefConstants.DefaultPropertyClass, defaultPropertyClassField.getText());
            userPrefs.put(PrefConstants.PropClassList, getMetaClassStr(propClassListModel.toArray()));
        }

        public void resetConfig() {
            String defaultClassClass = userPrefs.get(PrefConstants.DefaultClassClass, RDFS.Class.getURI());
            defaultClassClassField.setText(defaultClassClass);
            defaultClassClassField.setToolTipText(defaultClassClass);
            String classClassListStr = userPrefs.get(PrefConstants.ClassClassList, GraphManager.CLASS_CLASS_LIST);
            String[] list = classClassListStr.split(" ");
            Arrays.sort(list);
            classClassListModel.clear();
            for (int i = 0; i < list.length; i++) {
                classClassListModel.addElement(list[i]);
            }

            String defaultPropertyClass = userPrefs.get(PrefConstants.DefaultPropertyClass, RDF.Property.getURI());
            defaultPropertyClassField.setText(defaultPropertyClass);
            defaultPropertyClassField.setToolTipText(defaultPropertyClass);
            String propClassListStr = userPrefs.get(PrefConstants.PropClassList, GraphManager.PROPERTY_CLASS_LIST);
            list = propClassListStr.split(" ");
            Arrays.sort(list);
            propClassListModel.clear();
            for (int i = 0; i < list.length; i++) {
                propClassListModel.addElement(list[i]);
            }
        }

        public void valueChanged(ListSelectionEvent e) {
            if (e.getSource() == classClassList) {
                if (!classClassList.isSelectionEmpty()) {
                    metaClassField.setText(classClassList.getSelectedValue().toString());
                    propClassList.clearSelection();
                }
            } else if (e.getSource() == propClassList) {
                if (!propClassList.isSelectionEmpty()) {
                    metaClassField.setText(propClassList.getSelectedValue().toString());
                    classClassList.clearSelection();
                }
            }
        }

        private JComponent getClassClassButtonPanel() {
            Action classClassButtonAction = new ClassClassButtonAction();
            setDefaultClassClassButton = new JButton("Set Default");
            setDefaultClassClassButton.addActionListener(classClassButtonAction);
            classClassEditButton = new JButton(EDIT);
            classClassEditButton.addActionListener(classClassButtonAction);
            classClassAddButton = new JButton(ADD);
            classClassAddButton.addActionListener(classClassButtonAction);
            classClassRemoveButton = new JButton(REMOVE);
            classClassRemoveButton.addActionListener(classClassButtonAction);
            JPanel classClassButtonPanel = new JPanel();
            classClassButtonPanel.setLayout(new GridLayout(1, 4, 5, 5));
            classClassButtonPanel.add(setDefaultClassClassButton);
            classClassButtonPanel.add(classClassEditButton);
            classClassButtonPanel.add(classClassAddButton);
            classClassButtonPanel.add(classClassRemoveButton);
            return Utilities.createEastPanel(classClassButtonPanel);
        }

        private JComponent getPropertyClassButtonPanel() {
            Action propClassButtonAction = new PropClassButtonAction();
            setDefaultPropertyClassButton = new JButton("Set Default");
            setDefaultPropertyClassButton.addActionListener(propClassButtonAction);
            propClassEditButton = new JButton(EDIT);
            propClassEditButton.addActionListener(propClassButtonAction);
            propClassAddButton = new JButton(ADD);
            propClassAddButton.addActionListener(propClassButtonAction);
            propClassRemoveButton = new JButton(REMOVE);
            propClassRemoveButton.addActionListener(propClassButtonAction);
            JPanel propClassButtonPanel = new JPanel();
            propClassButtonPanel.setLayout(new GridLayout(1, 4, 5, 5));
            propClassButtonPanel.add(setDefaultPropertyClassButton);
            propClassButtonPanel.add(propClassEditButton);
            propClassButtonPanel.add(propClassAddButton);
            propClassButtonPanel.add(propClassRemoveButton);
            return Utilities.createEastPanel(propClassButtonPanel);
        }

        class ClassClassButtonAction extends AbstractAction {
            private boolean isEditable() {
                return !classClassList.isSelectionEmpty() && classClassList.getSelectedIndices().length == 1
                        && !isDefaultClass(classClassList.getSelectedValue()) && isAddable();
            }
            private boolean isAddable() {
                return !classClassListModel.contains(metaClassField.getText())
                        && !propClassListModel.contains(metaClassField.getText());
            }

            private void edit() {
                if (isEditable()) {
                    classClassListModel.setElementAt(metaClassField.getText(), classClassList.getSelectedIndex());
                }
            }

            private void add() {
                if (isAddable()) {
                    classClassListModel.addElement(metaClassField.getText());
                }
            }

            private boolean isDefaultClass(Object item) {
                return item.equals(RDFS.Class.toString()) || item.equals(OWL.Class.toString());
            }

            private void remove() {
                if (classClassList.isSelectionEmpty()) { return; }
                List removeList = Arrays.asList(classClassList.getSelectedValues());
                for (Iterator i = removeList.iterator(); i.hasNext();) {
                    Object item = i.next();
                    if (!isDefaultClass(item)) {
                        if (item.equals(defaultClassClassField.getText())) {
                            setDefaultClassClass(RDFS.Class.getURI());
                        }
                        classClassListModel.removeElement(item);
                    }
                }
            }

            private void setDefaultClassClass(String defaultClassClass) {
                defaultClassClassField.setText(defaultClassClass);
                defaultClassClassField.setToolTipText(defaultClassClass);
            }

            public void actionPerformed(ActionEvent e) {
                Object command = e.getSource();
                if (command == setDefaultClassClassButton) {
                    if (classClassList.getSelectedValue() != null) {
                        setDefaultClassClass(classClassList.getSelectedValue().toString());
                    }
                } else if (command == classClassEditButton) {
                    edit();
                } else if (command == classClassAddButton) {
                    add();
                } else if (command == classClassRemoveButton) {
                    remove();
                }
            }
        }

        class PropClassButtonAction extends AbstractAction {

            private boolean isEditable() {
                return !propClassList.isSelectionEmpty() && propClassList.getSelectedIndices().length == 1
                        && !isDefaultProperty(propClassList.getSelectedValue()) && isAddable();
            }
            private boolean isAddable() {
                return !propClassListModel.contains(metaClassField.getText())
                        && !classClassListModel.contains(metaClassField.getText());
            }

            private void edit() {
                if (isEditable()) {
                    propClassListModel.setElementAt(metaClassField.getText(), propClassList.getSelectedIndex());
                }
            }

            private void add() {
                if (isAddable()) {
                    propClassListModel.addElement(metaClassField.getText());
                }
            }

            private boolean isDefaultProperty(Object item) {
                return item.equals(RDF.Property.toString()) || item.equals(OWL.ObjectProperty.toString())
                        || item.equals(OWL.DatatypeProperty.toString());
            }

            private void remove() {
                if (propClassList.isSelectionEmpty()) { return; }
                List removeList = Arrays.asList(propClassList.getSelectedValues());
                for (Iterator i = removeList.iterator(); i.hasNext();) {
                    Object item = i.next();
                    if (!isDefaultProperty(item)) {
                        if (item.equals(defaultPropertyClassField.getText())) {
                            setDefaultPropertyClass(RDF.Property.getURI());
                        }
                        propClassListModel.removeElement(item);
                    }
                }
            }

            private void setDefaultPropertyClass(String defaultPropertyClass) {
                defaultPropertyClassField.setText(defaultPropertyClass);
                defaultPropertyClassField.setToolTipText(defaultPropertyClass);
            }

            public void actionPerformed(ActionEvent e) {
                Object command = e.getSource();
                if (command == setDefaultPropertyClassButton) {
                    if (propClassList.getSelectedValue() != null) {
                        setDefaultPropertyClass(propClassList.getSelectedValue().toString());
                    }
                } else if (command == propClassEditButton) {
                    edit();
                } else if (command == propClassAddButton) {
                    add();
                } else if (command == propClassRemoveButton) {
                    remove();
                }
            }
        }
    }

    private static final int BUTTON_WIDTH = 200;
    private static final int BUTTON_HEIGHT = 25;

    class LayoutPanel extends JPanel {

        private JRadioButton autoSizeButton;
        private JRadioButton fixSizeButton;
        private JLabel nodeHeightLabel;
        private JLabel nodeWidthLabel;
        private JSpinner nodeHeightSpinner;
        private JSpinner nodeWidthSpinner;

        private JLabel layoutTypeLabel;
        private JLabel rdfLayoutLabel;
        private JLabel classLayoutLabel;
        private JLabel propertyLayoutLabel;

        private JLabel verticalSpaceLabel;
        private JLabel horizontalSpaceLabel;

        private JComboBox layoutTypeBox;
        private JComboBox rdfLayoutDirectionBox;
        private JComboBox classLayoutDirectionBox;
        private JComboBox propertyLayoutDirectionBox;

        private JSpinner rdfVerticalSpaceSpinner;
        private JSpinner rdfHorizontalSpaceSpinner;
        private JSpinner classVerticalSpaceSpinner;
        private JSpinner classHorizontalSpaceSpinner;
        private JSpinner propertyVerticalSpaceSpinner;
        private JSpinner propertyHorizontalSpaceSpinner;

        LayoutPanel() {
            ChangeNodeSizeAction nodeSizeAction = new ChangeNodeSizeAction();
            autoSizeButton = new JRadioButton(Translator.getString("PreferenceDialog.LayoutTab.Auto"));
            autoSizeButton.addActionListener(nodeSizeAction);
            autoSizeButton.setSelected(true);
            fixSizeButton = new JRadioButton(Translator.getString("PreferenceDialog.LayoutTab.Fix"));
            fixSizeButton.addActionListener(nodeSizeAction);
            ButtonGroup group = new ButtonGroup();
            group.add(autoSizeButton);
            group.add(fixSizeButton);
            JPanel nodeSizeButtonPanel = new JPanel();
            nodeSizeButtonPanel.add(autoSizeButton);
            nodeSizeButtonPanel.add(fixSizeButton);
            nodeSizeButtonPanel.setBorder(BorderFactory.createTitledBorder(Translator
                    .getString("PreferenceDialog.LayoutTab.NodeSize")));

            nodeWidthLabel = new JLabel(Translator.getString("PreferenceDialog.LayoutTab.NodeWidth"));
            nodeWidthSpinner = new JSpinner(new SpinnerNumberModel(50, 0, 999, 1));
            nodeWidthSpinner.setEnabled(false);
            nodeHeightLabel = new JLabel(Translator.getString("PreferenceDialog.LayoutTab.NodeHeight"));
            nodeHeightSpinner = new JSpinner(new SpinnerNumberModel(50, 0, 999, 1));
            nodeHeightSpinner.setEnabled(false);

            JPanel nodeSizePanel = new JPanel();
            nodeSizePanel.add(nodeSizeButtonPanel);
            nodeSizePanel.add(nodeWidthLabel);
            nodeSizePanel.add(nodeWidthSpinner);
            nodeSizePanel.add(nodeHeightLabel);
            nodeSizePanel.add(nodeHeightSpinner);
            nodeSizePanel.setBorder(BorderFactory.createEtchedBorder());

            layoutTypeLabel = new JLabel(Translator.getString("PreferenceDialog.LayoutTab.LayoutType"));
            verticalSpaceLabel = new JLabel(Translator.getString("PreferenceDialog.LayoutTab.VerticalSpace"));
            verticalSpaceLabel.setHorizontalAlignment(JLabel.CENTER);
            horizontalSpaceLabel = new JLabel(Translator.getString("PreferenceDialog.LayoutTab.HorizontalSpace"));
            horizontalSpaceLabel.setHorizontalAlignment(JLabel.CENTER);

            rdfLayoutLabel = new JLabel(Translator.getString("PreferenceDialog.LayoutTab.RDFLayoutDirection"));
            classLayoutLabel = new JLabel(Translator.getString("PreferenceDialog.LayoutTab.ClassLayoutDirection"));
            propertyLayoutLabel = new JLabel(Translator.getString("PreferenceDialog.LayoutTab.PropertyLayoutDirection"));

            layoutTypeBox = new JComboBox(new Object[] { GraphLayoutUtilities.VGJ_TREE_LAYOUT,
                    GraphLayoutUtilities.JGRAPH_TREE_LAYOUT});
            rdfVerticalSpaceSpinner = new JSpinner(new SpinnerNumberModel(50, 0, 999, 1));
            rdfHorizontalSpaceSpinner = new JSpinner(new SpinnerNumberModel(200, 0, 999, 1));
            classVerticalSpaceSpinner = new JSpinner(new SpinnerNumberModel(50, 0, 999, 1));
            classHorizontalSpaceSpinner = new JSpinner(new SpinnerNumberModel(200, 0, 999, 1));
            propertyVerticalSpaceSpinner = new JSpinner(new SpinnerNumberModel(50, 0, 999, 1));
            propertyHorizontalSpaceSpinner = new JSpinner(new SpinnerNumberModel(200, 0, 999, 1));
            Object[] directionList = new Object[] { GraphLayoutUtilities.UP_TO_DOWN, GraphLayoutUtilities.LEFT_TO_RIGHT};
            rdfLayoutDirectionBox = new JComboBox(directionList);
            classLayoutDirectionBox = new JComboBox(directionList);
            propertyLayoutDirectionBox = new JComboBox(directionList);

            JPanel mainPanel = new JPanel();
            mainPanel.setPreferredSize(new Dimension(400, 250));
            mainPanel.setLayout(new GridLayout(3, 1));
            mainPanel.add(getLayoutDirectionPanel());
            mainPanel.add(getLayoutSpacePanel());
            mainPanel.add(nodeSizePanel);

            setLayout(new BorderLayout());
            add(getTitledPanel(mainPanel, toString()), BorderLayout.NORTH);
            setBorder(BorderFactory.createEtchedBorder());
        }

        public String toString() {
            return Translator.getString("PreferenceDialog.LayoutTab");
        }

        public void setConfig() {
            userPrefs.put(PrefConstants.LAYOUT_TYPE, (String) layoutTypeBox.getSelectedItem());
            GraphLayoutUtilities.LAYOUT_TYPE = (String) layoutTypeBox.getSelectedItem();
            userPrefs.put(PrefConstants.RDF_LAYOUT_DIRECTION, (String) rdfLayoutDirectionBox.getSelectedItem());
            GraphLayoutUtilities.RDF_LAYOUT_DIRECTION = (String) rdfLayoutDirectionBox.getSelectedItem();
            userPrefs.put(PrefConstants.CLASS_LAYOUT_DIRECTION, (String) classLayoutDirectionBox.getSelectedItem());
            GraphLayoutUtilities.CLASS_LAYOUT_DIRECTION = (String) classLayoutDirectionBox.getSelectedItem();
            userPrefs.put(PrefConstants.PROPERTY_LAYOUT_DIRECTION, (String) propertyLayoutDirectionBox
                    .getSelectedItem());
            GraphLayoutUtilities.PROPERTY_LAYOUT_DIRECTION = (String) propertyLayoutDirectionBox.getSelectedItem();

            userPrefs.put(PrefConstants.RDF_VERTICAL_SPACE, rdfVerticalSpaceSpinner.getValue().toString());
            GraphLayoutUtilities.RDF_VERTICAL_SPACE = Integer.parseInt(rdfVerticalSpaceSpinner.getValue().toString());
            userPrefs.put(PrefConstants.RDF_HORIZONTAL_SPACE, rdfHorizontalSpaceSpinner.getValue().toString());
            GraphLayoutUtilities.RDF_HORIZONTAL_SPACE = Integer.parseInt(rdfHorizontalSpaceSpinner.getValue()
                    .toString());

            userPrefs.put(PrefConstants.CLASS_VERTICAL_SPACE, classVerticalSpaceSpinner.getValue().toString());
            GraphLayoutUtilities.CLASS_VERTICAL_SPACE = Integer.parseInt(classVerticalSpaceSpinner.getValue()
                    .toString());
            userPrefs.put(PrefConstants.CLASS_HORIZONTAL_SPACE, classHorizontalSpaceSpinner.getValue().toString());
            GraphLayoutUtilities.CLASS_HORIZONTAL_SPACE = Integer.parseInt(classHorizontalSpaceSpinner.getValue()
                    .toString());

            userPrefs.put(PrefConstants.PROPERTY_VERTICAL_SPACE, propertyVerticalSpaceSpinner.getValue().toString());
            GraphLayoutUtilities.PROPERTY_VERTICAL_SPACE = Integer.parseInt(propertyVerticalSpaceSpinner.getValue()
                    .toString());
            userPrefs
                    .put(PrefConstants.PROPERTY_HORIZONTAL_SPACE, propertyHorizontalSpaceSpinner.getValue().toString());
            GraphLayoutUtilities.PROPERTY_HORIZONTAL_SPACE = Integer.parseInt(propertyHorizontalSpaceSpinner.getValue()
                    .toString());

            if (autoSizeButton.isSelected()) {
                userPrefs.put(PrefConstants.NODE_SIZE, PrefConstants.NODE_SIZE_AUTO);
            } else {
                userPrefs.put(PrefConstants.NODE_SIZE, PrefConstants.NODE_SIZE_FIX);
            }
            // gmanager.setAutoSizeMode(autoSizeButton.isSelected());
            userPrefs.put(PrefConstants.NODE_WIDTH, nodeWidthSpinner.getValue().toString());
            MR3CellMaker.CELL_WIDTH = Integer.parseInt(nodeWidthSpinner.getValue().toString());
            userPrefs.put(PrefConstants.NODE_HEIGHT, nodeHeightSpinner.getValue().toString());
            MR3CellMaker.CELL_HEIGHT = Integer.parseInt(nodeHeightSpinner.getValue().toString());
        }

        public void resetConfig() {
            String layoutType = userPrefs.get(PrefConstants.LAYOUT_TYPE, GraphLayoutUtilities.VGJ_TREE_LAYOUT);
            layoutTypeBox.setSelectedItem(layoutType);
            String direction = userPrefs.get(PrefConstants.RDF_LAYOUT_DIRECTION, GraphLayoutUtilities.LEFT_TO_RIGHT);
            if (isValidLayoutDirection(rdfLayoutDirectionBox, direction)) {
                rdfLayoutDirectionBox.setSelectedItem(direction);
            } else {
                direction = changeDirectionWithLanguage(direction, PrefConstants.RDF_LAYOUT_DIRECTION);
                rdfLayoutDirectionBox.setSelectedItem(direction);
            }
            direction = userPrefs.get(PrefConstants.CLASS_LAYOUT_DIRECTION, GraphLayoutUtilities.LEFT_TO_RIGHT);
            if (isValidLayoutDirection(classLayoutDirectionBox, direction)) {
                classLayoutDirectionBox.setSelectedItem(direction);
            } else {
                direction = changeDirectionWithLanguage(direction, PrefConstants.CLASS_LAYOUT_DIRECTION);
                classLayoutDirectionBox.setSelectedItem(direction);
            }
            direction = userPrefs.get(PrefConstants.PROPERTY_LAYOUT_DIRECTION, GraphLayoutUtilities.LEFT_TO_RIGHT);
            if (isValidLayoutDirection(propertyLayoutDirectionBox, direction)) {
                propertyLayoutDirectionBox.setSelectedItem(direction);
            } else {
                direction = changeDirectionWithLanguage(direction, PrefConstants.PROPERTY_LAYOUT_DIRECTION);
                propertyLayoutDirectionBox.setSelectedItem(direction);
            }

            rdfVerticalSpaceSpinner.setValue(new Integer(userPrefs.get(PrefConstants.RDF_VERTICAL_SPACE, Integer
                    .toString(GraphLayoutUtilities.VERTICAL_SPACE))));
            rdfHorizontalSpaceSpinner.setValue(new Integer(userPrefs.get(PrefConstants.RDF_HORIZONTAL_SPACE, Integer
                    .toString(GraphLayoutUtilities.HORIZONTAL_SPACE))));
            classVerticalSpaceSpinner.setValue(new Integer(userPrefs.get(PrefConstants.CLASS_VERTICAL_SPACE, Integer
                    .toString(GraphLayoutUtilities.VERTICAL_SPACE))));
            classHorizontalSpaceSpinner.setValue(new Integer(userPrefs.get(PrefConstants.CLASS_HORIZONTAL_SPACE,
                    Integer.toString(GraphLayoutUtilities.HORIZONTAL_SPACE))));
            propertyVerticalSpaceSpinner.setValue(new Integer(userPrefs.get(PrefConstants.PROPERTY_VERTICAL_SPACE,
                    Integer.toString(GraphLayoutUtilities.VERTICAL_SPACE))));
            propertyHorizontalSpaceSpinner.setValue(new Integer(userPrefs.get(PrefConstants.PROPERTY_HORIZONTAL_SPACE,
                    Integer.toString(GraphLayoutUtilities.HORIZONTAL_SPACE))));

            String nodeSize = userPrefs.get(PrefConstants.NODE_SIZE, PrefConstants.NODE_SIZE_AUTO);
            if (nodeSize.equals(PrefConstants.NODE_SIZE_AUTO)) {
                autoSizeButton.setSelected(true);
            } else {
                fixSizeButton.setSelected(true);
            }
            nodeWidthSpinner.setValue(new Integer(userPrefs.get(PrefConstants.NODE_WIDTH, Integer
                    .toString(MR3CellMaker.CELL_WIDTH))));
            nodeHeightSpinner.setValue(new Integer(userPrefs.get(PrefConstants.NODE_HEIGHT, Integer
                    .toString(MR3CellMaker.CELL_HEIGHT))));
            nodeWidthSpinner.setEnabled(fixSizeButton.isSelected());
            nodeHeightSpinner.setEnabled(fixSizeButton.isSelected());
        }

        private String changeDirectionWithLanguage(String direction, String type) {
            if (direction.equals("上から下") || direction.equals("UP_TO_DOWN")) {
                userPrefs.put(type, GraphLayoutUtilities.UP_TO_DOWN);
            } else if (direction.equals("左から右") || direction.equals("LEFT_TO_RIGHT")) {
                userPrefs.put(type, GraphLayoutUtilities.LEFT_TO_RIGHT);
            }
            return userPrefs.get(type, GraphLayoutUtilities.UP_TO_DOWN);
        }

        private JPanel getLayoutDirectionPanel() {
            JPanel layoutDirectionPanel = new JPanel();
            layoutDirectionPanel.setLayout(new GridLayout(4, 2, 5, 2));
            layoutDirectionPanel.add(layoutTypeLabel);
            layoutDirectionPanel.add(layoutTypeBox);
            layoutDirectionPanel.add(rdfLayoutLabel);
            layoutDirectionPanel.add(rdfLayoutDirectionBox);
            layoutDirectionPanel.add(classLayoutLabel);
            layoutDirectionPanel.add(classLayoutDirectionBox);
            layoutDirectionPanel.add(propertyLayoutLabel);
            layoutDirectionPanel.add(propertyLayoutDirectionBox);
            layoutDirectionPanel.setBorder(BorderFactory.createEtchedBorder());

            return layoutDirectionPanel;
        }

        private JPanel getLayoutSpacePanel() {
            JPanel panel = new JPanel();
            JLabel graphLabel = new JLabel(Translator.getString("GraphType"));
            graphLabel.setHorizontalAlignment(JLabel.RIGHT);
            JLabel rdfSpaceLabel = new JLabel("RDF");
            rdfSpaceLabel.setHorizontalAlignment(JLabel.RIGHT);
            JLabel classSpaceLabel = new JLabel(Translator.getString("Class"));
            classSpaceLabel.setHorizontalAlignment(JLabel.RIGHT);
            JLabel propertySpaceLabel = new JLabel(Translator.getString("Property"));
            propertySpaceLabel.setHorizontalAlignment(JLabel.RIGHT);
            GridLayout gridLayout = new GridLayout(4, 3, 20, 0);
            panel.setLayout(gridLayout);

            panel.add(graphLabel);
            panel.add(verticalSpaceLabel);
            panel.add(horizontalSpaceLabel);

            panel.add(rdfSpaceLabel);
            panel.add(rdfVerticalSpaceSpinner);
            panel.add(rdfHorizontalSpaceSpinner);

            panel.add(classSpaceLabel);
            panel.add(classVerticalSpaceSpinner);
            panel.add(classHorizontalSpaceSpinner);

            panel.add(propertySpaceLabel);
            panel.add(propertyVerticalSpaceSpinner);
            panel.add(propertyHorizontalSpaceSpinner);
            panel.setBorder(BorderFactory.createEtchedBorder());

            return panel;
        }

        class ChangeNodeSizeAction implements ActionListener {
            public void actionPerformed(ActionEvent e) {
                boolean t = autoSizeButton.isSelected();
                nodeWidthSpinner.setEnabled(!t);
                nodeHeightSpinner.setEnabled(!t);
            }
        }

    }

    class RenderingPanel extends JPanel {

        private JCheckBox isAntialiasBox;

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

        RenderingPanel() {
            ChangeColorAction action = new ChangeColorAction();
            rdfResourceColorButton = new JButton(Translator.getString("PreferenceDialog.RenderingTab.RDFResourceColor")
                    + "(R)");
            rdfResourceColorButton.setMnemonic('r');
            initColorButton(rdfResourceColorButton, "Resource", BUTTON_WIDTH, BUTTON_HEIGHT, action);
            literalColorButton = new JButton(Translator.getString("PreferenceDialog.RenderingTab.RDFLiteralColor")
                    + "(L)");
            literalColorButton.setMnemonic('l');
            initColorButton(literalColorButton, "Literal", BUTTON_WIDTH, BUTTON_HEIGHT, action);
            classColorButton = new JButton(Translator.getString("PreferenceDialog.RenderingTab.ClassColor") + "(U)");
            classColorButton.setMnemonic('u');
            initColorButton(classColorButton, "Class", BUTTON_WIDTH, BUTTON_HEIGHT, action);
            propertyColorButton = new JButton(Translator.getString("PreferenceDialog.RenderingTab.PropertyColor")
                    + "(P)");
            propertyColorButton.setMnemonic('p');
            initColorButton(propertyColorButton, "Property", BUTTON_WIDTH, BUTTON_HEIGHT, action);
            selectedColorButton = new JButton(Translator.getString("PreferenceDialog.RenderingTab.SelectedColor")
                    + "(S)");
            selectedColorButton.setMnemonic('s');
            initColorButton(selectedColorButton, "Selected", BUTTON_WIDTH, BUTTON_HEIGHT, action);
            backgroundColorButton = new JButton(Translator.getString("PreferenceDialog.RenderingTab.BackgroundColor")
                    + "(B)");
            backgroundColorButton.setMnemonic('b');
            initColorButton(backgroundColorButton, "Background", BUTTON_WIDTH, BUTTON_HEIGHT, action);

            isColorBox = new JCheckBox(Translator.getString("PreferenceDialog.RenderingTab.Option.Color"));
            isAntialiasBox = new JCheckBox(Translator.getString("PreferenceDialog.RenderingTab.Option.Antialias"));

            JPanel colorPanel = new JPanel();
            colorPanel.setBorder(BorderFactory.createTitledBorder(Translator
                    .getString("PreferenceDialog.RenderingTab.SelectColor")));
            colorPanel.setLayout(new GridLayout(6, 1, 5, 5));
            colorPanel.add(rdfResourceColorButton);
            colorPanel.add(literalColorButton);
            colorPanel.add(classColorButton);
            colorPanel.add(propertyColorButton);
            colorPanel.add(selectedColorButton);
            colorPanel.add(backgroundColorButton);
            JPanel optionPanel = new JPanel();
            optionPanel.setBorder(BorderFactory.createTitledBorder(Translator
                    .getString("PreferenceDialog.RenderingTab.Option")));
            optionPanel.add(isColorBox);
            optionPanel.add(isAntialiasBox);
            JPanel panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
            panel.add(colorPanel);
            panel.add(optionPanel);

            setLayout(new BorderLayout());
            add(getTitleField(toString()), BorderLayout.NORTH);
            add(Utilities.createNorthPanel(panel), BorderLayout.WEST);
            setBorder(BorderFactory.createEtchedBorder());
        }

        public String toString() {
            return Translator.getString("PreferenceDialog.RenderingTab");
        }

        public void setConfig() {
            userPrefs.putInt(PrefConstants.RDFResourceColor, rdfResourceColor.getRGB());
            RDFResourceCell.rdfResourceColor = rdfResourceColor;
            userPrefs.putInt(PrefConstants.LiteralColor, literalColor.getRGB());
            RDFLiteralCell.literalColor = literalColor;
            userPrefs.putInt(PrefConstants.ClassColor, classColor.getRGB());
            OntClassCell.classColor = classColor;
            userPrefs.putInt(PrefConstants.PropertyColor, propertyColor.getRGB());
            OntPropertyCell.propertyColor = propertyColor;
            userPrefs.putInt(PrefConstants.SelectedColor, selectedColor.getRGB());
            GraphUtilities.selectedColor = selectedColor;
            userPrefs.putInt(PrefConstants.BackgroundColor, backgroundColor.getRGB());
            GraphUtilities.graphBackgroundColor = backgroundColor;
            gmanager.setGraphBackground(backgroundColor);
            userPrefs.putBoolean(PrefConstants.Color, isColorBox.isSelected());
            GraphUtilities.isColor = isColorBox.isSelected();
            // Colorがあるかないかをチェックした後に，セルの色を変更する．
            GraphUtilities.changeAllCellColor(gmanager);

            userPrefs.putBoolean(PrefConstants.Antialias, isAntialiasBox.isSelected());
            gmanager.setAntialias();
        }

        public void resetConfig() {
            rdfResourceColor = RDFResourceCell.rdfResourceColor;
            literalColor = RDFLiteralCell.literalColor;
            classColor = OntClassCell.classColor;
            propertyColor = OntPropertyCell.propertyColor;
            selectedColor = GraphUtilities.selectedColor;
            backgroundColor = Color.white;

            isColorBox.setSelected(userPrefs.getBoolean(PrefConstants.Color, true));
            isAntialiasBox.setSelected(userPrefs.getBoolean(PrefConstants.Antialias, true));
        }

        private void initColorButton(JButton button, String name, int width, int height, Action action) {
            button.setHorizontalAlignment(JButton.LEFT);
            button.setIcon(new ColorSwatch(name));
            button.setPreferredSize(new Dimension(width, height));
            button.addActionListener(action);
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
    }

    private static final int PREFIX_BOX_WIDTH = 120;
    private static final int PREFIX_BOX_HEIGHT = 20;

    private void setText(JTextComponent jtc, String text) {
        jtc.setText(text);
        jtc.setToolTipText(text);
    }

    private boolean isValidLayoutDirection(JComboBox box, String direction) {
        return (box.getModel().getElementAt(0).equals(direction) || box.getModel().getElementAt(1).equals(direction));
    }

    public void resetConfig() {
        basePanel.resetConfig();
        directoryPanel.resetConfig();
        proxyPanel.resetConfig();
        metaClassPanel.resetConfig();
        layoutPanel.resetConfig();
        renderingPanel.resetConfig();
    }

    private String getMetaClassStr(Object[] list) {
        String metaClassListStr = "";
        for (int i = 0; i < list.length; i++) {
            metaClassListStr += list[i] + " ";
        }
        return metaClassListStr;
    }

    class DecideAction extends AbstractAction {

        private void setConfig() {
            try {
                basePanel.setConfig();
                directoryPanel.setConfig();
                proxyPanel.setConfig();
                metaClassPanel.setConfig();
                layoutPanel.setConfig();
                renderingPanel.setConfig();
            } catch (NumberFormatException nfe) {
                JOptionPane.showMessageDialog(gmanager.getDesktopTabbedPane(), "Number Format Exception", "Warning",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

        public void actionPerformed(ActionEvent e) {
            if (e.getSource() == applyButton) {
                setConfig();
                resetConfig();
                return;
            } else if (e.getSource() == confirmButton) {
                setConfig();
            }
            setVisible(false);
        }
    }

}
