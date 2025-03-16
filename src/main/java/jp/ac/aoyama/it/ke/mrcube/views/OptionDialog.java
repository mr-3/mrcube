/*
 * Project Name: MR^3 (Meta-Model Management based on RDFs Revision Reflection)
 * Project Website: https://mr-3.github.io/
 *
 * Copyright (C) 2003-2025 Takeshi Morita. All rights reserved.
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

package jp.ac.aoyama.it.ke.mrcube.views;

import jp.ac.aoyama.it.ke.mrcube.editors.Editor;
import jp.ac.aoyama.it.ke.mrcube.jgraph.*;
import jp.ac.aoyama.it.ke.mrcube.layout.GraphLayoutUtilities;
import jp.ac.aoyama.it.ke.mrcube.models.MR3Constants;
import jp.ac.aoyama.it.ke.mrcube.models.MR3Resource;
import jp.ac.aoyama.it.ke.mrcube.models.NamespaceModel;
import jp.ac.aoyama.it.ke.mrcube.models.PrefConstants;
import jp.ac.aoyama.it.ke.mrcube.utils.*;
import jp.ac.aoyama.it.ke.mrcube.views.option_dialog.ResourceColorPanel;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;

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
    private final RenderingPanel renderingPanel;

    private final GraphManager gmanager;
    private final Preferences userPrefs;

    private JButton applyButton;
    private JButton confirmButton;

    private static final int WINDOW_WIDTH = 600;
    private static final int WINDOW_HEIGHT = 600;

    public static JComponent topLevelComponent;

    public OptionDialog(GraphManager gm, Preferences prefs) {
        super(gm.getRootFrame(), Translator.getString("OptionDialog.Title"), true);
        setIconImage(Utilities.getSVGIcon("settings.svg").getImage());
        gmanager = gm;
        userPrefs = prefs;
        loadResourceBundle();

        directoryPanel = new DirectoryPanel();
        proxyPanel = new ProxyPanel();
        basePanel = new BasePanel();
        metaClassPanel = new MetaClassPanel();
        layoutPanel = new LayoutPanel();
        renderingPanel = new RenderingPanel();

        menuList = new JList(new Object[]{basePanel, metaClassPanel, layoutPanel, renderingPanel});
        menuList.addListSelectionListener(this);
        JComponent menuListPanel = Utilities.createTitledPanel(menuList, "", 100, 100);

        mainPanel = new JPanel();
        cardLayout = new CardLayout(3, 5);
        mainPanel.setLayout(cardLayout);

        mainPanel.add(basePanel.toString(), basePanel);
        mainPanel.add(metaClassPanel.toString(), metaClassPanel);
        mainPanel.add(layoutPanel.toString(), layoutPanel);
        mainPanel.add(renderingPanel.toString(), new JScrollPane(renderingPanel));

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
            panel.add(getDefaultLanguageTagPanel());
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

        private JComponent getDefaultLanguageTagPanel() {
            defaultLangField = new JTextField();
            defaultLangField.setPreferredSize(new Dimension(PREFIX_BOX_WIDTH, PREFIX_BOX_HEIGHT));
            JPanel defaultLangTagPanel = new JPanel();
            defaultLangTagPanel.setLayout(new GridLayout(1, 2, 5, 5));
            defaultLangTagPanel.add(new JLabel(Translator.getString("OptionDialog.Base.DefaultLanguageTag") + ": "));
            defaultLangTagPanel.add(defaultLangField);

            return Utilities.createWestPanel(defaultLangTagPanel);
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
            uiLangPanel.add(new JLabel("UI " + Translator.getString("Lang") + ": "));
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
            userPrefs.put(PrefConstants.INSTANCE_NODE_VERTICAL_SPACE, rdfVerticalSpaceSpinner.getValue().toString());
            GraphLayoutUtilities.RDF_VERTICAL_SPACE = Integer.parseInt(rdfVerticalSpaceSpinner.getValue().toString());
            userPrefs.put(PrefConstants.INSTANCE_NODE_HORIZONTAL_SPACE, rdfHorizontalSpaceSpinner.getValue().toString());
            GraphLayoutUtilities.RDF_HORIZONTAL_SPACE = Integer.parseInt(rdfHorizontalSpaceSpinner.getValue().toString());

            userPrefs.put(PrefConstants.CLASS_NODE_VERTICAL_SPACE, classVerticalSpaceSpinner.getValue().toString());
            GraphLayoutUtilities.CLASS_VERTICAL_SPACE = Integer.parseInt(classVerticalSpaceSpinner.getValue().toString());
            userPrefs.put(PrefConstants.CLASS_NODE_HORIZONTAL_SPACE, classHorizontalSpaceSpinner.getValue().toString());
            GraphLayoutUtilities.CLASS_HORIZONTAL_SPACE = Integer.parseInt(classHorizontalSpaceSpinner.getValue().toString());

            userPrefs.put(PrefConstants.PROPERTY_NODE_VERTICAL_SPACE, propertyVerticalSpaceSpinner.getValue().toString());
            GraphLayoutUtilities.PROPERTY_VERTICAL_SPACE = Integer.parseInt(propertyVerticalSpaceSpinner.getValue().toString());
            userPrefs.put(PrefConstants.PROPERTY_NODE_HORIZONTAL_SPACE, propertyHorizontalSpaceSpinner.getValue().toString());
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
            rdfVerticalSpaceSpinner.setValue(Integer.valueOf(userPrefs.get(PrefConstants.INSTANCE_NODE_VERTICAL_SPACE,
                    Integer.toString(GraphLayoutUtilities.VERTICAL_SPACE))));
            rdfHorizontalSpaceSpinner.setValue(Integer.valueOf(userPrefs.get(PrefConstants.INSTANCE_NODE_HORIZONTAL_SPACE,
                    Integer.toString(GraphLayoutUtilities.HORIZONTAL_SPACE))));
            classVerticalSpaceSpinner.setValue(Integer.valueOf(userPrefs.get(PrefConstants.CLASS_NODE_VERTICAL_SPACE,
                    Integer.toString(GraphLayoutUtilities.VERTICAL_SPACE))));
            classHorizontalSpaceSpinner.setValue(Integer.valueOf(userPrefs.get(PrefConstants.CLASS_NODE_HORIZONTAL_SPACE,
                    Integer.toString(GraphLayoutUtilities.HORIZONTAL_SPACE))));
            propertyVerticalSpaceSpinner.setValue(Integer.valueOf(userPrefs.get(PrefConstants.PROPERTY_NODE_VERTICAL_SPACE,
                    Integer.toString(GraphLayoutUtilities.VERTICAL_SPACE))));
            propertyHorizontalSpaceSpinner.setValue(Integer.valueOf(userPrefs.get(PrefConstants.PROPERTY_NODE_HORIZONTAL_SPACE,
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
            JLabel rdfSpaceLabel = new JLabel(Translator.getString("Instance"));
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


    public enum RenderingResourceType {RDFResource, RDFProperty, RDFLiteral, Class, Property, Editor}

    public enum RenderingType {Foreground, Background, Border, SelectedBackground, SelectedBorder}

    class RenderingPanel extends JPanel implements ActionListener {

        private final JButton setDefaultColorsButton;

        private final JCheckBox isAntialiasBox;
        private final JCheckBox isBlackAndWhiteBox;

        private final ResourceColorPanel instanceNodeColorPanel;
        private final ResourceColorPanel instancePropertyNodeColorPanel;
        private final ResourceColorPanel literalNodeColorPanel;
        private final ResourceColorPanel classNodeColorPanel;
        private final ResourceColorPanel propertyNodeColorPanel;
        private final ResourceColorPanel editorColorPanel;

        RenderingPanel() {
            setDefaultColorsButton = new JButton(Translator.getString("PreferenceDialog.RenderingTab.SetDefaultColors"));
            setDefaultColorsButton.addActionListener(this);

            instanceNodeColorPanel = new ResourceColorPanel(Translator.getString("PreferenceDialog.RenderingTab.InstanceColor"),
                    RenderingResourceType.RDFResource, InstanceCell.foregroundColor,
                    InstanceCell.backgroundColor, InstanceCell.borderColor,
                    InstanceCell.selectedBackgroundColor, InstanceCell.selectedBorderColor);
            instancePropertyNodeColorPanel = new ResourceColorPanel(Translator.getString("PreferenceDialog.RenderingTab.InstancePropertyColor"),
                    RenderingResourceType.RDFProperty, InstancePropertyCell.foregroundColor,
                    null, InstancePropertyCell.borderColor,
                    null, InstancePropertyCell.selectedBorderColor);
            literalNodeColorPanel = new ResourceColorPanel(Translator.getString("PreferenceDialog.RenderingTab.LiteralColor"),
                    RenderingResourceType.RDFLiteral, LiteralCell.foregroundColor,
                    LiteralCell.backgroundColor, LiteralCell.borderColor,
                    LiteralCell.selectedBackgroundColor, LiteralCell.selectedBorderColor);
            classNodeColorPanel = new ResourceColorPanel(Translator.getString("PreferenceDialog.RenderingTab.ClassColor"),
                    RenderingResourceType.Class, OntClassCell.foregroundColor,
                    OntClassCell.backgroundColor, OntClassCell.borderColor,
                    OntClassCell.selectedBackgroundColor, OntClassCell.selectedBorderColor);
            propertyNodeColorPanel = new ResourceColorPanel(Translator.getString("PreferenceDialog.RenderingTab.PropertyColor"),
                    RenderingResourceType.Property, OntPropertyCell.foregroundColor,
                    OntPropertyCell.backgroundColor, OntPropertyCell.borderColor,
                    OntPropertyCell.selectedBackgroundColor, OntPropertyCell.selectedBorderColor);
            editorColorPanel = new ResourceColorPanel(Translator.getString("PreferenceDialog.RenderingTab.EditorColor"),
                    RenderingResourceType.Editor, null, Editor.backgroundColor,
                    null, null, null);

            isBlackAndWhiteBox = new JCheckBox(Translator.getString("PreferenceDialog.RenderingTab.Option.BlackAndWhite"), true);
            isAntialiasBox = new JCheckBox(Translator.getString("PreferenceDialog.RenderingTab.Option.Antialias"), false);

            JPanel colorPanel = new JPanel();
            colorPanel.setLayout(new GridLayout(3, 2, 0, 0));
            colorPanel.add(instanceNodeColorPanel);
            colorPanel.add(instancePropertyNodeColorPanel);
            colorPanel.add(literalNodeColorPanel);
            colorPanel.add(classNodeColorPanel);
            colorPanel.add(propertyNodeColorPanel);
            colorPanel.add(editorColorPanel);
            JPanel optionPanel = new JPanel();
            optionPanel.setBorder(BorderFactory.createTitledBorder(Translator.getString("PreferenceDialog.RenderingTab.Option")));
            optionPanel.add(isBlackAndWhiteBox);
            optionPanel.add(isAntialiasBox);
            JPanel panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
            panel.add(colorPanel);
            panel.add(optionPanel);

            setLayout(new BorderLayout());
            add(getTitleField(toString()), BorderLayout.NORTH);
            add(Utilities.createNorthPanel(panel), BorderLayout.WEST);
            JPanel southPanel = new JPanel();
            southPanel.setLayout(new BorderLayout());
            southPanel.add(setDefaultColorsButton, BorderLayout.WEST);
            add(southPanel, BorderLayout.SOUTH);
            setBorder(BorderFactory.createEtchedBorder());
        }

        public String toString() {
            return Translator.getString("PreferenceDialog.RenderingTab");
        }

        void setConfig() {
            userPrefs.putInt(PrefConstants.InstanceNodeForegroundColor, instanceNodeColorPanel.getFgColor().getRGB());
            InstanceCell.foregroundColor = instanceNodeColorPanel.getFgColor();
            userPrefs.putInt(PrefConstants.InstancePropertyForegroundColor, instancePropertyNodeColorPanel.getFgColor().getRGB());
            InstancePropertyCell.foregroundColor = instancePropertyNodeColorPanel.getFgColor();
            userPrefs.putInt(PrefConstants.LiteralNodeForegroundColor, literalNodeColorPanel.getFgColor().getRGB());
            LiteralCell.foregroundColor = literalNodeColorPanel.getFgColor();
            userPrefs.putInt(PrefConstants.ClassNodeForegroundColor, classNodeColorPanel.getFgColor().getRGB());
            OntClassCell.foregroundColor = classNodeColorPanel.getFgColor();
            userPrefs.putInt(PrefConstants.PropertyNodeForegroundColor, propertyNodeColorPanel.getFgColor().getRGB());
            OntPropertyCell.foregroundColor = propertyNodeColorPanel.getFgColor();

            userPrefs.putInt(PrefConstants.InstanceNodeBackgroundColor, instanceNodeColorPanel.getBgColor().getRGB());
            InstanceCell.backgroundColor = instanceNodeColorPanel.getBgColor();
            userPrefs.putInt(PrefConstants.LiteralNodeBackgroundColor, literalNodeColorPanel.getBgColor().getRGB());
            LiteralCell.backgroundColor = literalNodeColorPanel.getBgColor();
            userPrefs.putInt(PrefConstants.ClassNodeBackgroundColor, classNodeColorPanel.getBgColor().getRGB());
            OntClassCell.backgroundColor = classNodeColorPanel.getBgColor();
            userPrefs.putInt(PrefConstants.PropertyNodeBackgroundColor, propertyNodeColorPanel.getBgColor().getRGB());
            OntPropertyCell.backgroundColor = propertyNodeColorPanel.getBgColor();

            userPrefs.putInt(PrefConstants.InstanceNodeBorderColor, instanceNodeColorPanel.getBorderColor().getRGB());
            InstanceCell.borderColor = instanceNodeColorPanel.getBorderColor();
            userPrefs.putInt(PrefConstants.InstancePropertyBorderColor, instancePropertyNodeColorPanel.getBorderColor().getRGB());
            InstancePropertyCell.borderColor = instancePropertyNodeColorPanel.getBorderColor();
            userPrefs.putInt(PrefConstants.LiteralNodeBorderColor, literalNodeColorPanel.getBorderColor().getRGB());
            LiteralCell.borderColor = literalNodeColorPanel.getBorderColor();
            userPrefs.putInt(PrefConstants.ClassNodeBorderColor, classNodeColorPanel.getBorderColor().getRGB());
            OntClassCell.borderColor = classNodeColorPanel.getBorderColor();
            userPrefs.putInt(PrefConstants.PropertyNodeBorderColor, propertyNodeColorPanel.getBorderColor().getRGB());
            OntPropertyCell.borderColor = propertyNodeColorPanel.getBorderColor();

            userPrefs.putInt(PrefConstants.InstanceNodeSelectedBackgroundColor, instanceNodeColorPanel.getSelectedBgColor().getRGB());
            InstanceCell.selectedBackgroundColor = instanceNodeColorPanel.getSelectedBgColor();
            userPrefs.putInt(PrefConstants.LiteralNodeSelectedBackgroundColor, literalNodeColorPanel.getSelectedBgColor().getRGB());
            LiteralCell.selectedBackgroundColor = literalNodeColorPanel.getSelectedBgColor();
            userPrefs.putInt(PrefConstants.ClassNodeSelectedBackgroundColor, classNodeColorPanel.getSelectedBgColor().getRGB());
            OntClassCell.selectedBackgroundColor = classNodeColorPanel.getSelectedBgColor();
            userPrefs.putInt(PrefConstants.PropertyNodeSelectedBackgroundColor, propertyNodeColorPanel.getSelectedBgColor().getRGB());
            OntPropertyCell.selectedBackgroundColor = propertyNodeColorPanel.getSelectedBgColor();

            userPrefs.putInt(PrefConstants.InstanceNodeSelectedBorderColor, instanceNodeColorPanel.getSelectedBorderColor().getRGB());
            InstanceCell.selectedBorderColor = instanceNodeColorPanel.getSelectedBorderColor();
            userPrefs.putInt(PrefConstants.InstancePropertySelectedBorderColor, instancePropertyNodeColorPanel.getSelectedBorderColor().getRGB());
            InstancePropertyCell.selectedBorderColor = instancePropertyNodeColorPanel.getSelectedBorderColor();
            userPrefs.putInt(PrefConstants.LiteralNodeSelectedBorderColor, literalNodeColorPanel.getSelectedBorderColor().getRGB());
            LiteralCell.selectedBorderColor = literalNodeColorPanel.getSelectedBorderColor();
            userPrefs.putInt(PrefConstants.ClassNodeSelectedBorderColor, classNodeColorPanel.getSelectedBorderColor().getRGB());
            OntClassCell.selectedBorderColor = classNodeColorPanel.getSelectedBorderColor();
            userPrefs.putInt(PrefConstants.PropertyNodeSelectedBorderColor, propertyNodeColorPanel.getSelectedBorderColor().getRGB());
            OntPropertyCell.selectedBorderColor = propertyNodeColorPanel.getSelectedBorderColor();

            userPrefs.putInt(PrefConstants.EditorBackgroundColor, editorColorPanel.getBgColor().getRGB());
            Editor.backgroundColor = editorColorPanel.getBgColor();

            userPrefs.putBoolean(PrefConstants.BlackAndWhite, isBlackAndWhiteBox.isSelected());
            GraphUtilities.isBlackAndWhite = isBlackAndWhiteBox.isSelected();
            GraphUtilities.changeAllCellColor(gmanager);
            GraphUtilities.resetEditorBackgroudColor(gmanager);

            userPrefs.putBoolean(PrefConstants.Antialias, isAntialiasBox.isSelected());
            gmanager.setAntialias();
        }

        void resetConfig() {
            instanceNodeColorPanel.setFgColor(
                    new Color(userPrefs.getInt(PrefConstants.InstanceNodeForegroundColor,
                            InstanceCell.DEFAULT_FG_COLOR.getRGB()))
            );
            instancePropertyNodeColorPanel.setFgColor(
                    new Color(userPrefs.getInt(PrefConstants.InstancePropertyForegroundColor,
                            InstancePropertyCell.DEFAULT_FG_COLOR.getRGB()))
            );
            literalNodeColorPanel.setFgColor(
                    new Color(userPrefs.getInt(PrefConstants.LiteralNodeForegroundColor,
                            LiteralCell.DEFAULT_FG_COLOR.getRGB()))
            );
            classNodeColorPanel.setFgColor(
                    new Color(userPrefs.getInt(PrefConstants.ClassNodeForegroundColor,
                            OntClassCell.DEFAULT_FG_COLOR.getRGB()))
            );
            propertyNodeColorPanel.setFgColor(
                    new Color(userPrefs.getInt(PrefConstants.PropertyNodeForegroundColor,
                            OntPropertyCell.DEFAULT_FG_COLOR.getRGB()))
            );

            instanceNodeColorPanel.setBgColor(
                    new Color(userPrefs.getInt(PrefConstants.InstanceNodeBackgroundColor,
                            InstanceCell.DEFAULT_BG_COLOR.getRGB()))
            );
            literalNodeColorPanel.setBgColor(
                    new Color(userPrefs.getInt(PrefConstants.LiteralNodeBackgroundColor,
                            LiteralCell.DEFAULT_BG_COLOR.getRGB()))
            );
            classNodeColorPanel.setBgColor(
                    new Color(userPrefs.getInt(PrefConstants.ClassNodeBackgroundColor,
                            OntClassCell.DEFAULT_BG_COLOR.getRGB()))
            );
            propertyNodeColorPanel.setBgColor(
                    new Color(userPrefs.getInt(PrefConstants.PropertyNodeBackgroundColor,
                            OntPropertyCell.DEFAULT_BG_COLOR.getRGB()))
            );

            instanceNodeColorPanel.setBorderColor(
                    new Color(userPrefs.getInt(PrefConstants.InstanceNodeBorderColor,
                            InstanceCell.DEFAULT_BORDER_COLOR.getRGB()))
            );
            instancePropertyNodeColorPanel.setBorderColor(
                    new Color(userPrefs.getInt(PrefConstants.InstancePropertyBorderColor,
                            InstancePropertyCell.DEFAULT_BORDER_COLOR.getRGB()))
            );
            literalNodeColorPanel.setBorderColor(
                    new Color(userPrefs.getInt(PrefConstants.LiteralNodeBorderColor,
                            LiteralCell.DEFAULT_BORDER_COLOR.getRGB()))
            );
            classNodeColorPanel.setBorderColor(
                    new Color(userPrefs.getInt(PrefConstants.ClassNodeBorderColor,
                            OntClassCell.DEFAULT_BORDER_COLOR.getRGB()))
            );
            propertyNodeColorPanel.setBorderColor(
                    new Color(userPrefs.getInt(PrefConstants.PropertyNodeBorderColor,
                            OntPropertyCell.DEFAULT_BORDER_COLOR.getRGB()))
            );

            instanceNodeColorPanel.setSelectedBgColor(
                    new Color(userPrefs.getInt(PrefConstants.InstanceNodeSelectedBackgroundColor,
                            InstanceCell.DEFAULT_SELECTED_BACKGROUND_COLOR.getRGB()))
            );
            literalNodeColorPanel.setSelectedBgColor(
                    new Color(userPrefs.getInt(PrefConstants.LiteralNodeSelectedBackgroundColor,
                            LiteralCell.DEFAULT_SELECTED_BACKGROUND_COLOR.getRGB()))
            );
            classNodeColorPanel.setSelectedBgColor(
                    new Color(userPrefs.getInt(PrefConstants.ClassNodeSelectedBackgroundColor,
                            OntClassCell.DEFAULT_SELECTED_BACKGROUND_COLOR.getRGB()))
            );
            propertyNodeColorPanel.setSelectedBgColor(
                    new Color(userPrefs.getInt(PrefConstants.PropertyNodeSelectedBackgroundColor,
                            OntPropertyCell.DEFAULT_SELECTED_BACKGROUND_COLOR.getRGB()))
            );

            instanceNodeColorPanel.setSelectedBorderColor(
                    new Color(userPrefs.getInt(PrefConstants.InstanceNodeSelectedBorderColor,
                            InstanceCell.DEFAULT_SELECTED_BORDER_COLOR.getRGB()))
            );
            instancePropertyNodeColorPanel.setSelectedBorderColor(
                    new Color(userPrefs.getInt(PrefConstants.InstancePropertySelectedBorderColor,
                            InstancePropertyCell.DEFAULT_SELECTED_BORDER_COLOR.getRGB()))
            );
            literalNodeColorPanel.setSelectedBorderColor(
                    new Color(userPrefs.getInt(PrefConstants.LiteralNodeSelectedBorderColor,
                            LiteralCell.DEFAULT_SELECTED_BORDER_COLOR.getRGB()))
            );
            classNodeColorPanel.setSelectedBorderColor(
                    new Color(userPrefs.getInt(PrefConstants.ClassNodeSelectedBorderColor,
                            OntClassCell.DEFAULT_SELECTED_BORDER_COLOR.getRGB()))
            );
            propertyNodeColorPanel.setSelectedBorderColor(
                    new Color(userPrefs.getInt(PrefConstants.PropertyNodeSelectedBorderColor,
                            OntPropertyCell.DEFAULT_SELECTED_BORDER_COLOR.getRGB()))
            );

            editorColorPanel.setBgColor(
                    new Color(userPrefs.getInt(PrefConstants.EditorBackgroundColor,
                            Editor.DEFAUlT_BACKGROUND_COLOR.getRGB()))
            );

            isBlackAndWhiteBox.setSelected(userPrefs.getBoolean(PrefConstants.BlackAndWhite, false));
            isAntialiasBox.setSelected(userPrefs.getBoolean(PrefConstants.Antialias, true));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            instanceNodeColorPanel.setFgColor(InstanceCell.DEFAULT_FG_COLOR);
            instancePropertyNodeColorPanel.setFgColor(InstancePropertyCell.DEFAULT_FG_COLOR);
            literalNodeColorPanel.setFgColor(LiteralCell.DEFAULT_FG_COLOR);
            classNodeColorPanel.setFgColor(OntClassCell.DEFAULT_FG_COLOR);
            propertyNodeColorPanel.setFgColor(OntPropertyCell.DEFAULT_FG_COLOR);

            instanceNodeColorPanel.setBgColor(InstanceCell.DEFAULT_BG_COLOR);
            literalNodeColorPanel.setBgColor(LiteralCell.DEFAULT_BG_COLOR);
            classNodeColorPanel.setBgColor(OntClassCell.DEFAULT_BG_COLOR);
            propertyNodeColorPanel.setBgColor(OntPropertyCell.DEFAULT_BG_COLOR);

            instanceNodeColorPanel.setBorderColor(InstanceCell.DEFAULT_BORDER_COLOR);
            instancePropertyNodeColorPanel.setBorderColor(InstancePropertyCell.DEFAULT_BORDER_COLOR);
            literalNodeColorPanel.setBorderColor(LiteralCell.DEFAULT_BORDER_COLOR);
            classNodeColorPanel.setBorderColor(OntClassCell.DEFAULT_BORDER_COLOR);
            propertyNodeColorPanel.setBorderColor(OntPropertyCell.DEFAULT_BORDER_COLOR);

            instanceNodeColorPanel.setSelectedBgColor(InstanceCell.DEFAULT_SELECTED_BACKGROUND_COLOR);
            literalNodeColorPanel.setSelectedBgColor(LiteralCell.DEFAULT_SELECTED_BACKGROUND_COLOR);
            classNodeColorPanel.setSelectedBgColor(OntClassCell.DEFAULT_SELECTED_BACKGROUND_COLOR);
            propertyNodeColorPanel.setSelectedBgColor(OntPropertyCell.DEFAULT_SELECTED_BACKGROUND_COLOR);

            instanceNodeColorPanel.setSelectedBorderColor(InstanceCell.DEFAULT_SELECTED_BORDER_COLOR);
            instancePropertyNodeColorPanel.setSelectedBorderColor(InstancePropertyCell.DEFAULT_SELECTED_BORDER_COLOR);
            literalNodeColorPanel.setSelectedBorderColor(LiteralCell.DEFAULT_SELECTED_BORDER_COLOR);
            classNodeColorPanel.setSelectedBorderColor(OntClassCell.DEFAULT_SELECTED_BORDER_COLOR);
            propertyNodeColorPanel.setSelectedBorderColor(OntPropertyCell.DEFAULT_SELECTED_BORDER_COLOR);

            editorColorPanel.setBgColor(Editor.DEFAUlT_BACKGROUND_COLOR);

            instanceNodeColorPanel.repaint();
            instancePropertyNodeColorPanel.repaint();
            literalNodeColorPanel.repaint();
            classNodeColorPanel.repaint();
            propertyNodeColorPanel.repaint();
            editorColorPanel.repaint();
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
        renderingPanel.resetConfig();
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
                renderingPanel.setConfig();
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
