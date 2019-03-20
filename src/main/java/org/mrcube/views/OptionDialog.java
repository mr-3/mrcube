/*
 * Project Name: MR^3 (Meta-Model Management based on RDFs Revision Reflection)
 * Project Website: http://mrcube.org/
 *
 * Copyright (C) 2003-2019 Yamaguchi Laboratory, Keio University. All rights reserved.
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

package org.mrcube.views;

import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.mrcube.jgraph.GraphManager;
import org.mrcube.layout.GraphLayoutUtilities;
import org.mrcube.models.MR3Constants;
import org.mrcube.models.MR3Resource;
import org.mrcube.models.NamespaceModel;
import org.mrcube.models.PrefConstants;
import org.mrcube.utils.*;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.List;
import java.util.*;
import java.util.prefs.Preferences;

/**
 * @author Takeshi Morita
 */
public class OptionDialog extends JDialog implements ListSelectionListener {

    private final JList menuList;
    private final JPanel mainPanel;

    private final CardLayout cardLayout;

    private final BasePanel basePanel;
    private final DirectoryPanel directoryPanel;
    private final ProxyPanel proxyPanel;
    private final MetaClassPanel metaClassPanel;
    private final LayoutPanel layoutPanel;

    private final GraphManager gmanager;
    private final Preferences userPrefs;

    private JButton applyButton;
    private JButton confirmButton;

    private static final int WINDOW_WIDTH = 600;
    private static final int WINDOW_HEIGHT = 450;

    public static JComponent topLevelComponent;

    public OptionDialog(GraphManager gm, Preferences prefs) {
        super(gm.getRootFrame(), Translator.getString("OptionDialog.Title"), true);
        setIconImage(Utilities.getImageIcon("baseline_settings_black_18dp.png").getImage());
        gmanager = gm;
        userPrefs = prefs;
        loadResourceBundle();

        directoryPanel = new DirectoryPanel();
        proxyPanel = new ProxyPanel();
        basePanel = new BasePanel();
        metaClassPanel = new MetaClassPanel();
        layoutPanel = new LayoutPanel();

        menuList = new JList(new Object[]{basePanel, metaClassPanel, layoutPanel});
        menuList.addListSelectionListener(this);
        JComponent menuListPanel = Utilities.createTitledPanel(menuList, "", 100, 100);

        mainPanel = new JPanel();
        cardLayout = new CardLayout(3, 5);
        mainPanel.setLayout(cardLayout);

        mainPanel.add(basePanel.toString(), basePanel);
        mainPanel.add(metaClassPanel.toString(), metaClassPanel);
        mainPanel.add(layoutPanel.toString(), layoutPanel);

        JPanel topLevelPanel = new JPanel();
        topLevelPanel.setLayout(new BorderLayout());
        topLevelPanel.add(menuListPanel, BorderLayout.WEST);
        topLevelPanel.add(mainPanel, BorderLayout.CENTER);
        topLevelPanel.add(getButtonGroupPanel(), BorderLayout.SOUTH);
        topLevelComponent = topLevelPanel;

        getContentPane().add(topLevelPanel);

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
        JButton cancelButton = new JButton(MR3Constants.CANCEL);
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
        titleField.setBackground(MR3Constants.TITLE_BACKGROUND_COLOR);
        titleField.setForeground(Color.white);
        titleField.setFont(new Font("SansSerif", Font.BOLD, MR3Constants.TITLE_FONT_SIZE));
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
        private JComboBox uriPrefixBox;
        private JLabel baseURILabel;

        BasePanel() {
            JPanel panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
            panel.add(getLangPanel());
            panel.add(getUILangPanel());
            panel.add(getBaseURIPanel());
            panel.add(directoryPanel);
            panel.add(proxyPanel);
            setLayout(new BorderLayout());
            add(getTitledPanel(panel, toString()), BorderLayout.NORTH);
            setBorder(BorderFactory.createEtchedBorder());
        }

        public String toString() {
            return Translator.getString("OptionDialog.Base");
        }

        void setConfig() {
            if (!userPrefs.get(PrefConstants.UILang, "en").equals(uiLangBox.getSelectedItem())) {
                userPrefs.put(PrefConstants.UILang, (String) uiLangBox.getSelectedItem());
            }
            userPrefs.put(PrefConstants.DefaultLang, defaultLangField.getText());
            userPrefs.put(PrefConstants.BaseURI, baseURILabel.getText());
            gmanager.setBaseURI(baseURILabel.getText());
        }

        void resetConfig() {
            initPrefixBox();
            uiLangBox.setModel(new DefaultComboBoxModel(getUILanguages()));
            uiLangBox.setSelectedItem(userPrefs.get(PrefConstants.UILang, "en"));
            defaultLangField.setText(userPrefs.get(PrefConstants.DefaultLang, "ja"));
            baseURILabel.setText(userPrefs.get(PrefConstants.BaseURI, MR3Resource.DefaultURI.getURI()));
            directoryPanel.resetConfig();
            proxyPanel.resetConfig();
            HistoryManager.resetFileAppender(directoryPanel.workDirectoryField.getText() + File.separator + HistoryManager.DEFAULT_LOG_FILE_NAME);
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
            Set<String> langSet = new TreeSet<>();
            try {
                for (File resFile : resourceDir.listFiles()) {
                    if (resFile.getName().matches("MR3_.*\\.properties")) {
                        String lang = resFile.getName().split("_")[1].split("\\.")[0];
                        langSet.add(lang);
                    }
                }
            } catch (Exception e) {// Java Web Startのための処理
                return new Object[]{"en", "ja", "zh"};
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
            if (0 < languages.length) {
                return languages;
            }
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
                    .getString("OptionDialog.Base.BaseURI")));
            baseURIPanel.add(uriPrefixBoxP);
            baseURIPanel.add(baseURILabelP);

            return baseURIPanel;
        }

        private void initPrefixBox() {
            PrefixNSUtil.setNamespaceModelSet(GraphUtilities.getNamespaceModelSet());
            uriPrefixBox.setModel(new DefaultComboBoxModel(PrefixNSUtil.getPrefixes().toArray()));
            setPrefix();
        }

        private void setPrefix() {
            for (NamespaceModel prefNSInfo : GraphUtilities.getNamespaceModelSet()) {
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

        DirectoryPanel() {
            initWorkDirectoryField();
            setLayout(new BorderLayout());
            add(getWorkDirectoryPanel(), BorderLayout.CENTER);
            setBorder(BorderFactory.createEtchedBorder());
        }

        void setConfig() {
            userPrefs.put(PrefConstants.WorkDirectory, workDirectoryField.getText());
        }

        void resetConfig() {
            setText(workDirectoryField, userPrefs.get(PrefConstants.WorkDirectory, System.getProperty("user.dir")));
        }

        class BrowseDirectory extends AbstractAction {
            private final JTextField directoryField;

            BrowseDirectory(JTextField field) {
                directoryField = field;
            }

            private String getDirectoryName() {
                File currentDirectory = null;
                if (directoryField == workDirectoryField) {
                    currentDirectory = new File(userPrefs.get(PrefConstants.WorkDirectory, System.getProperty("user.dir")));
                }

                JFileChooser jfc = new JFileChooser(currentDirectory);
                jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                jfc.setDialogTitle("Select Directory");
                int fd = jfc.showOpenDialog(gmanager.getRootFrame());
                if (fd == JFileChooser.APPROVE_OPTION) {
                    return jfc.getSelectedFile().toString();
                }
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
            browseWorkDirectoryButton = new JButton(Translator.getString("OptionDialog.Directory.Browse")
                    + "(W)");
            browseWorkDirectoryButton.setMnemonic('w');
            browseWorkDirectoryButton.addActionListener(new BrowseDirectory(workDirectoryField));
        }

        private JPanel getWorkDirectoryPanel() {
            JPanel workDirectoryPanel = new JPanel();
            workDirectoryPanel.setLayout(new BoxLayout(workDirectoryPanel, BoxLayout.X_AXIS));
            workDirectoryPanel.setBorder(BorderFactory.createTitledBorder(Translator
                    .getString("OptionDialog.Directory.WorkDirectory")));
            workDirectoryPanel.add(workDirectoryField);
            workDirectoryPanel.add(browseWorkDirectoryButton);

            return workDirectoryPanel;
        }

    }

    class ProxyPanel extends JPanel {

        private final JCheckBox isProxy;
        private final JTextField proxyHost;
        private final JTextField proxyPort;

        ProxyPanel() {
            isProxy = new JCheckBox(Translator.getString("OptionDialog.Proxy"));
            isProxy.addActionListener(new CheckProxy());
            proxyHost = new JTextField(25);
            JComponent proxyHostP = Utilities.createTitledPanel(proxyHost,
                    Translator.getString("OptionDialog.Proxy.Host"));
            proxyPort = new JTextField(5);
            JComponent proxyPortP = Utilities.createTitledPanel(proxyPort,
                    Translator.getString("OptionDialog.Proxy.Port"));

            JPanel panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
            panel.add(isProxy);
            panel.add(proxyHostP);
            panel.add(proxyPortP);
            setLayout(new BorderLayout());
            add(panel, BorderLayout.NORTH);
            setBorder(BorderFactory.createEtchedBorder());
        }

        class CheckProxy extends AbstractAction {
            public void actionPerformed(ActionEvent e) {
                proxyHost.setEditable(isProxy.isSelected());
                proxyPort.setEditable(isProxy.isSelected());
            }
        }

        void setConfig() {
            userPrefs.putBoolean(PrefConstants.Proxy, isProxy.isSelected());
            userPrefs.put(PrefConstants.ProxyHost, proxyHost.getText());
            userPrefs.putInt(PrefConstants.ProxyPort, Integer.parseInt(proxyPort.getText()));
        }

        void resetConfig() {
            isProxy.setSelected(userPrefs.getBoolean(PrefConstants.Proxy, false));
            setText(proxyHost, userPrefs.get(PrefConstants.ProxyHost, "http://localhost"));
            proxyHost.setEditable(isProxy.isSelected());
            proxyPort.setText(Integer.toString(userPrefs.getInt(PrefConstants.ProxyPort, 3128)));
            proxyPort.setEditable(isProxy.isSelected());
        }
    }

    class MetaClassPanel extends JPanel implements ListSelectionListener {

        private final JTextField metaClassField;

        private final JTextField defaultClassClassField;
        private final JList classClassList;
        private final DefaultListModel classClassListModel;
        private JButton setDefaultClassClassButton;
        private JButton classClassEditButton;
        private JButton classClassAddButton;
        private JButton classClassRemoveButton;

        private final JTextField defaultPropertyClassField;
        private final JList propClassList;
        private final DefaultListModel propClassListModel;
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
                    Translator.getString("OptionDialog.MetaClass.DefaultClassClass"));
            classClassListModel = new DefaultListModel();
            classClassList = new JList(classClassListModel);
            classClassList.addListSelectionListener(this);
            JScrollPane classClassListScroll = new JScrollPane(classClassList);
            Utilities.initComponent(classClassListScroll,
                    Translator.getString("OptionDialog.MetaClass.ClassClassList"),
                    LONG_URI_FIELD_WIDTH, LIST_HEIGHT);

            defaultPropertyClassField = new JTextField();
            defaultPropertyClassField.setEditable(false);
            JComponent defaultPropertyClassFieldP = Utilities.createTitledPanel(defaultPropertyClassField,
                    Translator.getString("OptionDialog.MetaClass.DefaultPropertyClass"));
            propClassListModel = new DefaultListModel();
            propClassList = new JList(propClassListModel);
            propClassList.addListSelectionListener(this);
            JScrollPane propClassListScroll = new JScrollPane(propClassList);
            Utilities.initComponent(propClassListScroll,
                    Translator.getString("OptionDialog.MetaClass.PropertyClassList"), LONG_URI_FIELD_WIDTH,
                    LIST_HEIGHT);

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
            tab.add(Utilities.createNorthPanel(classClassPanel),
                    Translator.getString("OptionDialog.MetaClass.ClassClass"));
            tab.add(Utilities.createNorthPanel(propertyClassPanel),
                    Translator.getString("OptionDialog.MetaClass.PropertyClass"));

            setLayout(new BorderLayout());
            add(getTitledPanel(metaClassFieldP, toString()), BorderLayout.NORTH);
            add(tab, BorderLayout.CENTER);
            setBorder(BorderFactory.createEtchedBorder());
        }

        public String toString() {
            return Translator.getString("OptionDialog.MetaClass");
        }

        void setConfig() {
            userPrefs.put(PrefConstants.DefaultClassClass, defaultClassClassField.getText());
            userPrefs.put(PrefConstants.ClassClassList, getMetaClassStr(classClassListModel.toArray()));
            userPrefs.put(PrefConstants.DefaultPropertyClass, defaultPropertyClassField.getText());
            userPrefs.put(PrefConstants.PropClassList, getMetaClassStr(propClassListModel.toArray()));
        }

        void resetConfig() {
            String defaultClassClass = userPrefs.get(PrefConstants.DefaultClassClass, RDFS.Class.getURI());
            defaultClassClassField.setText(defaultClassClass);
            defaultClassClassField.setToolTipText(defaultClassClass);
            String classClassListStr = userPrefs.get(PrefConstants.ClassClassList, GraphManager.CLASS_CLASS_LIST);
            String[] list = classClassListStr.split(" ");
            Arrays.sort(list);
            classClassListModel.clear();
            for (String s1 : list) {
                classClassListModel.addElement(s1);
            }

            String defaultPropertyClass = userPrefs.get(PrefConstants.DefaultPropertyClass, RDF.Property.getURI());
            defaultPropertyClassField.setText(defaultPropertyClass);
            defaultPropertyClassField.setToolTipText(defaultPropertyClass);
            String propClassListStr = userPrefs.get(PrefConstants.PropClassList, GraphManager.PROPERTY_CLASS_LIST);
            list = propClassListStr.split(" ");
            Arrays.sort(list);
            propClassListModel.clear();
            for (String s : list) {
                propClassListModel.addElement(s);
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
            setDefaultClassClassButton = new JButton(Translator.getString("OptionDialog.MetaClass.SetDefault"));
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
            setDefaultPropertyClassButton = new JButton(Translator.getString("OptionDialog.MetaClass.SetDefault"));
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
                if (classClassList.isSelectionEmpty()) {
                    return;
                }
                List removeList = Collections.singletonList(classClassList.getSelectedValuesList());
                for (Object item : removeList) {
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
                if (propClassList.isSelectionEmpty()) {
                    return;
                }
                List removeList = Collections.singletonList(propClassList.getSelectedValuesList());
                for (Object item : removeList) {
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

    class LayoutPanel extends JPanel {

        private final JRadioButton autoSizeButton;
        private final JRadioButton fixSizeButton;
        private final JLabel nodeHeightLabel;
        private final JLabel nodeWidthLabel;
        private final JSpinner nodeHeightSpinner;
        private final JSpinner nodeWidthSpinner;

        private final JLabel verticalSpaceLabel;
        private final JLabel horizontalSpaceLabel;

        private final JSpinner rdfVerticalSpaceSpinner;
        private final JSpinner rdfHorizontalSpaceSpinner;
        private final JSpinner classVerticalSpaceSpinner;
        private final JSpinner classHorizontalSpaceSpinner;
        private final JSpinner propertyVerticalSpaceSpinner;
        private final JSpinner propertyHorizontalSpaceSpinner;

        LayoutPanel() {
            ChangeNodeSizeAction nodeSizeAction = new ChangeNodeSizeAction();
            autoSizeButton = new JRadioButton(Translator.getString("OptionDialog.Layout.Auto"));
            autoSizeButton.addActionListener(nodeSizeAction);
            autoSizeButton.setSelected(true);
            fixSizeButton = new JRadioButton(Translator.getString("OptionDialog.Layout.Fix"));
            fixSizeButton.addActionListener(nodeSizeAction);
            ButtonGroup group = new ButtonGroup();
            group.add(autoSizeButton);
            group.add(fixSizeButton);
            JPanel nodeSizeButtonPanel = new JPanel();
            nodeSizeButtonPanel.add(autoSizeButton);
            nodeSizeButtonPanel.add(fixSizeButton);
            nodeSizeButtonPanel.setBorder(BorderFactory.createTitledBorder(Translator.getString("OptionDialog.Layout.NodeSize")));

            nodeWidthLabel = new JLabel(Translator.getString("OptionDialog.Layout.NodeWidth"));
            nodeWidthSpinner = new JSpinner(new SpinnerNumberModel(50, 0, 999, 1));
            nodeWidthSpinner.setEnabled(false);
            nodeHeightLabel = new JLabel(Translator.getString("OptionDialog.Layout.NodeHeight"));
            nodeHeightSpinner = new JSpinner(new SpinnerNumberModel(50, 0, 999, 1));
            nodeHeightSpinner.setEnabled(false);

            JPanel nodeSizePanel = new JPanel();
            nodeSizePanel.add(nodeSizeButtonPanel);
            nodeSizePanel.add(nodeWidthLabel);
            nodeSizePanel.add(nodeWidthSpinner);
            nodeSizePanel.add(nodeHeightLabel);
            nodeSizePanel.add(nodeHeightSpinner);
            nodeSizePanel.setBorder(BorderFactory.createEtchedBorder());

            verticalSpaceLabel = new JLabel(Translator.getString("OptionDialog.Layout.VerticalSpace"));
            verticalSpaceLabel.setHorizontalAlignment(JLabel.CENTER);
            horizontalSpaceLabel = new JLabel(Translator.getString("OptionDialog.Layout.HorizontalSpace"));
            horizontalSpaceLabel.setHorizontalAlignment(JLabel.CENTER);

            rdfVerticalSpaceSpinner = new JSpinner(new SpinnerNumberModel(50, 0, 999, 1));
            rdfHorizontalSpaceSpinner = new JSpinner(new SpinnerNumberModel(200, 0, 999, 1));
            classVerticalSpaceSpinner = new JSpinner(new SpinnerNumberModel(50, 0, 999, 1));
            classHorizontalSpaceSpinner = new JSpinner(new SpinnerNumberModel(200, 0, 999, 1));
            propertyVerticalSpaceSpinner = new JSpinner(new SpinnerNumberModel(50, 0, 999, 1));
            propertyHorizontalSpaceSpinner = new JSpinner(new SpinnerNumberModel(200, 0, 999, 1));

            JPanel mainPanel = new JPanel();
            mainPanel.setLayout(new BorderLayout());
            mainPanel.add(getLayoutSpacePanel(), BorderLayout.CENTER);
            mainPanel.add(nodeSizePanel, BorderLayout.SOUTH);

            setLayout(new BorderLayout());
            add(getTitledPanel(mainPanel, toString()), BorderLayout.NORTH);
            setBorder(BorderFactory.createEtchedBorder());
        }

        public String toString() {
            return Translator.getString("OptionDialog.Layout");
        }

        void setConfig() {
            userPrefs.put(PrefConstants.RDF_VERTICAL_SPACE, rdfVerticalSpaceSpinner.getValue().toString());
            GraphLayoutUtilities.RDF_VERTICAL_SPACE = Integer.parseInt(rdfVerticalSpaceSpinner.getValue().toString());
            userPrefs.put(PrefConstants.RDF_HORIZONTAL_SPACE, rdfHorizontalSpaceSpinner.getValue().toString());
            GraphLayoutUtilities.RDF_HORIZONTAL_SPACE = Integer.parseInt(rdfHorizontalSpaceSpinner.getValue().toString());

            userPrefs.put(PrefConstants.CLASS_VERTICAL_SPACE, classVerticalSpaceSpinner.getValue().toString());
            GraphLayoutUtilities.CLASS_VERTICAL_SPACE = Integer.parseInt(classVerticalSpaceSpinner.getValue().toString());
            userPrefs.put(PrefConstants.CLASS_HORIZONTAL_SPACE, classHorizontalSpaceSpinner.getValue().toString());
            GraphLayoutUtilities.CLASS_HORIZONTAL_SPACE = Integer.parseInt(classHorizontalSpaceSpinner.getValue().toString());

            userPrefs.put(PrefConstants.PROPERTY_VERTICAL_SPACE, propertyVerticalSpaceSpinner.getValue().toString());
            GraphLayoutUtilities.PROPERTY_VERTICAL_SPACE = Integer.parseInt(propertyVerticalSpaceSpinner.getValue().toString());
            userPrefs.put(PrefConstants.PROPERTY_HORIZONTAL_SPACE, propertyHorizontalSpaceSpinner.getValue().toString());
            GraphLayoutUtilities.PROPERTY_HORIZONTAL_SPACE = Integer.parseInt(propertyHorizontalSpaceSpinner.getValue().toString());

            if (autoSizeButton.isSelected()) {
                userPrefs.put(PrefConstants.NODE_SIZE, PrefConstants.NODE_SIZE_AUTO);
            } else {
                userPrefs.put(PrefConstants.NODE_SIZE, PrefConstants.NODE_SIZE_FIX);
            }
            userPrefs.put(PrefConstants.NODE_WIDTH, nodeWidthSpinner.getValue().toString());
            MR3CellMaker.CELL_WIDTH = Integer.parseInt(nodeWidthSpinner.getValue().toString());
            userPrefs.put(PrefConstants.NODE_HEIGHT, nodeHeightSpinner.getValue().toString());
            MR3CellMaker.CELL_HEIGHT = Integer.parseInt(nodeHeightSpinner.getValue().toString());
        }

        void resetConfig() {
            rdfVerticalSpaceSpinner.setValue(Integer.valueOf(userPrefs.get(PrefConstants.RDF_VERTICAL_SPACE,
                    Integer.toString(GraphLayoutUtilities.VERTICAL_SPACE))));
            rdfHorizontalSpaceSpinner.setValue(Integer.valueOf(userPrefs.get(PrefConstants.RDF_HORIZONTAL_SPACE,
                    Integer.toString(GraphLayoutUtilities.HORIZONTAL_SPACE))));
            classVerticalSpaceSpinner.setValue(Integer.valueOf(userPrefs.get(PrefConstants.CLASS_VERTICAL_SPACE,
                    Integer.toString(GraphLayoutUtilities.VERTICAL_SPACE))));
            classHorizontalSpaceSpinner.setValue(Integer.valueOf(userPrefs.get(PrefConstants.CLASS_HORIZONTAL_SPACE,
                    Integer.toString(GraphLayoutUtilities.HORIZONTAL_SPACE))));
            propertyVerticalSpaceSpinner.setValue(Integer.valueOf(userPrefs.get(PrefConstants.PROPERTY_VERTICAL_SPACE,
                    Integer.toString(GraphLayoutUtilities.VERTICAL_SPACE))));
            propertyHorizontalSpaceSpinner.setValue(Integer.valueOf(userPrefs.get(PrefConstants.PROPERTY_HORIZONTAL_SPACE,
                    Integer.toString(GraphLayoutUtilities.HORIZONTAL_SPACE))));

            String nodeSize = userPrefs.get(PrefConstants.NODE_SIZE, PrefConstants.NODE_SIZE_AUTO);
            if (nodeSize.equals(PrefConstants.NODE_SIZE_AUTO)) {
                autoSizeButton.setSelected(true);
            } else {
                fixSizeButton.setSelected(true);
            }
            nodeWidthSpinner.setValue(Integer.valueOf(userPrefs.get(PrefConstants.NODE_WIDTH,
                    Integer.toString(MR3CellMaker.CELL_WIDTH))));
            nodeHeightSpinner.setValue(Integer.valueOf(userPrefs.get(PrefConstants.NODE_HEIGHT,
                    Integer.toString(MR3CellMaker.CELL_HEIGHT))));
            nodeWidthSpinner.setEnabled(fixSizeButton.isSelected());
            nodeHeightSpinner.setEnabled(fixSizeButton.isSelected());
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

    private static final int PREFIX_BOX_WIDTH = 120;
    private static final int PREFIX_BOX_HEIGHT = 30;

    private void setText(JTextComponent jtc, String text) {
        jtc.setText(text);
        jtc.setToolTipText(text);
    }

    public void resetConfig() {
        basePanel.resetConfig();
        metaClassPanel.resetConfig();
        layoutPanel.resetConfig();
    }

    private String getMetaClassStr(Object[] list) {
        String metaClassListStr = "";
        for (Object o : list) {
            metaClassListStr += o + " ";
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
            } catch (NumberFormatException nfe) {
                Utilities.showErrorMessageDialog("Number Format Exception");
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
