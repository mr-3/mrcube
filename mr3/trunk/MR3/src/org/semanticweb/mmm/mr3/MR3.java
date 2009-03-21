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
 * DODDLE-OWL is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with MR^3.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */

package org.semanticweb.mmm.mr3;

import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.event.*;
import java.lang.ref.*;
import java.util.*;
import java.util.prefs.*;

import javax.swing.*;
import javax.swing.event.*;

import org.semanticweb.mmm.mr3.actions.*;
import org.semanticweb.mmm.mr3.data.*;
import org.semanticweb.mmm.mr3.data.MR3Constants.*;
import org.semanticweb.mmm.mr3.editor.*;
import org.semanticweb.mmm.mr3.io.*;
import org.semanticweb.mmm.mr3.jgraph.*;
import org.semanticweb.mmm.mr3.layout.*;
import org.semanticweb.mmm.mr3.ui.*;
import org.semanticweb.mmm.mr3.ui.FindResourceDialog.*;
import org.semanticweb.mmm.mr3.util.*;

/**
 * @author Takeshi Morita
 */
public class MR3 extends JFrame implements ChangeListener {

    public static boolean OFF_META_MODEL_MANAGEMENT;

    private static Preferences userPrefs;
    private static JTabbedPane desktopTabbedPane;

    private MR3Reader mr3Reader;
    private MR3Writer mr3Writer;
    private GraphManager gmanager;

    private WeakReference<OverviewDialog> rdfEditorOverviewRef;
    private WeakReference<OverviewDialog> classEditorOverviewRef;
    private WeakReference<OverviewDialog> propertyEditorOverviewRef;
    // private WeakReference<OntTreeEditor> ontTreeEditorRef;
    private WeakReference<OptionDialog> optionDialogRef;
    private WeakReference<ImportDialog> importDialogRef;
    private WeakReference<ExportDialog> exportDialogRef;
    private WeakReference<HistoryManager> historyManagerRef;
    private WeakReference<ValidatorDialog> validatorRef;
    private WeakReference<ProjectInfoDialog> projectInfoDialogRef;
    private MR3LogConsole mr3LogConsole;

    private JCheckBoxMenuItem uriView;
    private JCheckBoxMenuItem idView;
    private JCheckBoxMenuItem labelView;

    private JCheckBoxMenuItem showTypeCellBox;
    private JCheckBoxMenuItem showToolTips;
    private JCheckBoxMenuItem isGroup;
    private JCheckBoxMenuItem showRDFPropertyLabelBox;

    public static StatusBarPanel STATUS_BAR;
    private static final int MAIN_FRAME_HEIGHT = 600;
    private static final int MAIN_FRAME_WIDTH = 800;
    private static final Color DESKTOP_BACK_COLOR = Color.WHITE;

    public MR3() {
        MR3Constants.loadResourceBundle();
        initWeakReferences();
        mr3LogConsole = new MR3LogConsole(this, Translator.getString("LogConsole.Title"), null);

        desktopTabbedPane = new JTabbedPane();
        desktopTabbedPane.addChangeListener(this);
        gmanager = new GraphManager(desktopTabbedPane, userPrefs, this);
        mr3Reader = new MR3Reader(gmanager);
        mr3Writer = new MR3Writer(gmanager);
        initActions();
        getContentPane().add(createToolBar(), BorderLayout.NORTH);

        STATUS_BAR = new StatusBarPanel();
        getContentPane().add(desktopTabbedPane, BorderLayout.CENTER);
        getContentPane().add(STATUS_BAR, BorderLayout.SOUTH);

        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        addWindowListener(new CloseWindow(this));
        setIconImage(MR3Constants.LOGO.getImage());
        setJMenuBar(createMenuBar());
        initOptions();

        getProjectInfoDialog(); // 生成しておかないと，機能しないため
        // getHistoryManager();
        HistoryManager.setGraphManager(gmanager);
    }

    private void initWeakReferences() {
        rdfEditorOverviewRef = new WeakReference<OverviewDialog>(null);
        classEditorOverviewRef = new WeakReference<OverviewDialog>(null);
        propertyEditorOverviewRef = new WeakReference<OverviewDialog>(null);
        // ontTreeEditorRef = new WeakReference<OntTreeEditor>(null);
        importDialogRef = new WeakReference<ImportDialog>(null);
        exportDialogRef = new WeakReference<ExportDialog>(null);
        historyManagerRef = new WeakReference<HistoryManager>(null);
        validatorRef = new WeakReference<ValidatorDialog>(null);
        projectInfoDialogRef = new WeakReference<ProjectInfoDialog>(null);
        optionDialogRef = new WeakReference<OptionDialog>(null);
    }

    private AbstractAction newProjectAction;
    private AbstractAction openProjectAction;
    private AbstractAction saveProjectAction;
    private AbstractAction saveProjectAsAction;
    private AbstractAction openPluginManagerAction;
    private AbstractAction deployWindowCPRAction;
    private AbstractAction deployWindowCRAction;
    private AbstractAction deployWindowPRAction;
    private AbstractAction showAttrDialogAction;
    private AbstractAction showNSTableDialogAction;
    private AbstractAction showImportDialogAction;
    private AbstractAction showExportDialogAction;
    private AbstractAction findResAction;

    private ImageIcon CPR_ICON = Utilities.getImageIcon(Translator.getString("Component.Window.DeployCPRWindows.Icon"));
    private ImageIcon CR_ICON = Utilities.getImageIcon(Translator.getString("Component.Window.DeployCRWindows.Icon"));
    private ImageIcon PR_ICON = Utilities.getImageIcon(Translator.getString("Component.Window.DeployPRWindows.Icon"));

    private void initActions() {
        newProjectAction = new NewProject(this);
        openProjectAction = new OpenProject(this);
        saveProjectAction = new SaveProject(this, SaveProject.SAVE_PROJECT, SaveProject.SAVE_PROJECT_ICON);
        saveProjectAsAction = new SaveProject(this, SaveProject.SAVE_AS_PROJECT, SaveProject.SAVE_AS_PROJECT_ICON);
        openPluginManagerAction = new OpenPluginManagerAction(this, Translator
                .getString("Component.Tools.Plugins.Text"));
        deployWindowCPRAction = new DeployWindows(this, Translator.getString("Component.Window.DeployCPRWindows.Text"),
                CPR_ICON, DeployType.CPR, "control alt R");
        deployWindowCRAction = new DeployWindows(this, Translator.getString("Component.Window.DeployCRWindows.Text"),
                CR_ICON, DeployType.CR, "control alt C");
        deployWindowPRAction = new DeployWindows(this, Translator.getString("Component.Window.DeployPRWindows.Text"),
                PR_ICON, DeployType.PR, "control alt P");
        showAttrDialogAction = new ShowAttrDialog(this);
        showNSTableDialogAction = new ShowNSTableDialog(this);
        showImportDialogAction = new ShowImportDialog(this, Translator.getString("Component.Window.ImportDialog.Text"));
        showExportDialogAction = new ShowExportDialog(this, Translator.getString("Component.Window.ExportDialog.Text"));
        findResAction = new FindResAction(null, gmanager);
    }

    private JLabel findLabel;
    private JTextField findField;
    private JLabel findResNum;
    private JButton findPrevButton;
    private JButton findNextButton;
    private int currentFindResourceNum;
    private Object[] findList;

    private JToolBar createToolBar() {
        JToolBar toolbar = new JToolBar();
        toolbar.setFloatable(true);
        toolbar.add(newProjectAction);
        toolbar.add(openProjectAction);
        toolbar.add(saveProjectAction);
        toolbar.add(saveProjectAsAction);
        toolbar.addSeparator();
        toolbar.add(showImportDialogAction);
        toolbar.add(showExportDialogAction);
        toolbar.addSeparator();
        toolbar.add(openPluginManagerAction);
        toolbar.addSeparator();
        toolbar.add(findResAction);
        toolbar.addSeparator();
        toolbar.add(showAttrDialogAction);
        toolbar.add(showNSTableDialogAction);
        toolbar.addSeparator();
        toolbar.add(deployWindowCPRAction);
        toolbar.add(deployWindowCRAction);
        toolbar.add(deployWindowPRAction);
        toolbar.addSeparator();
        findLabel = new JLabel(Translator.getString("Component.Edit.FindResource.Text") + ": ");
        toolbar.add(findLabel);
        findField = new JTextField(20);
        findField.setFocusAccelerator('/');
        findField.addActionListener(new NextResourceAction());
        findField.getDocument().addDocumentListener(new IncrementalFindAction());
        toolbar.add(findField);
        findResNum = new JLabel("(0/0)");
        toolbar.add(findResNum);
        ImageIcon PREV_ICON = Utilities.getImageIcon(Translator.getString("ToolBar.FindField.Icon.prev"));
        ImageIcon NEXT_ICON = Utilities.getImageIcon(Translator.getString("ToolBar.FindField.Icon.next"));
        findPrevButton = new JButton(PREV_ICON);
        findPrevButton.addActionListener(new PrevResourceAction());
        findNextButton = new JButton(NEXT_ICON);
        findNextButton.addActionListener(new NextResourceAction());
        toolbar.add(findPrevButton);
        toolbar.add(findNextButton);

        return toolbar;
    }

    class PrevResourceAction implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            setFindList();
            currentFindResourceNum--;
            if (currentFindResourceNum == -1) {
                currentFindResourceNum = findList.length - 1;
            }
            jumpFindResource(currentFindResourceNum);
        }
    }

    class NextResourceAction implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            setFindList();
            currentFindResourceNum++;
            if (currentFindResourceNum == findList.length) {
                currentFindResourceNum = 0;
            }
            jumpFindResource(currentFindResourceNum);
        }
    }

    private void jumpFindResource(int num) {
        if (0 <= num && num < findList.length) {
            gmanager.selectRDFCell(findList[num]);
            gmanager.selectClassCell(findList[num]);
            gmanager.selectPropertyCell(findList[num]);
            findResNum.setText("(" + (num + 1) + "/" + findList.length + ")");
        }
    }

    private static Object[] NULL = new Object[0];

    private void setFindList() {
        gmanager.getFindResourceDialog().setAllCheckBoxSelected(true);
        gmanager.getFindResourceDialog().setURIPrefixBox();
        if (findField.getText().length() == 0) {
            findList = NULL;
        } else {
            findList = gmanager.getFindResourceDialog().getFindResources(findField.getText(), FindActionType.URI);
        }
    }

    class IncrementalFindAction implements DocumentListener {

        private void findResources() {
            setFindList();
            if (findList.length == 0) {
                findResNum.setText("(0/0)");
                return;
            }
            currentFindResourceNum = 0;
            jumpFindResource(currentFindResourceNum);
        }

        public void changedUpdate(DocumentEvent e) {
            findResources();
        }

        public void insertUpdate(DocumentEvent e) {
            findResources();
        }

        public void removeUpdate(DocumentEvent e) {
            findResources();
        }
    }

    public void showRDFEditorOverview() {
        OverviewDialog result = rdfEditorOverviewRef.get();
        if (result == null) {
            RDFEditor editor = getCurrentProject().getRDFEditor();
            result = new OverviewDialog(this, OverviewDialog.RDF_EDITOR_OVERVIEW, editor);
            result.setIconImage(OverviewDialog.RDF_EDITOR_ICON.getImage());
            rdfEditorOverviewRef = new WeakReference<OverviewDialog>(result);
        } else {
            result.setEditor(getCurrentProject().getRDFEditor());
        }
        result.setVisible(true);
    }

    public void showClassEditorOverview() {
        OverviewDialog result = classEditorOverviewRef.get();
        if (result == null) {
            ClassEditor editor = getCurrentProject().getClassEditor();
            result = new OverviewDialog(this, OverviewDialog.CLASS_EDITOR_OVERVIEW, editor);
            result.setIconImage(OverviewDialog.CLASS_EDITOR_ICON.getImage());
            classEditorOverviewRef = new WeakReference<OverviewDialog>(result);
        } else {
            result.setEditor(getCurrentProject().getClassEditor());
        }
        result.setVisible(true);
    }

    // public OntTreeEditor getRDFSTreeEditor() {
    // OntTreeEditor result = (OntTreeEditor) rdfsTreeEditorRef.get();
    // if (result == null) {
    // result = new OntTreeEditor(gmanager);
    // gmanager.setRDFSTreeEditor(result);
    // iFrames[3] = result;
    // setRDFSTreeEditorBounds();
    // desktop.add(iFrames[3], Cursor.DEFAULT_CURSOR);
    // rdfsTreeEditorRef = new WeakReference(result);
    // }
    // return result;
    // }

    public void showPropertyEditorOverview() {
        OverviewDialog result = propertyEditorOverviewRef.get();
        if (result == null) {
            PropertyEditor editor = getCurrentProject().getPropertyEditor();
            result = new OverviewDialog(this, OverviewDialog.PROPERTY_EDITOR_OVERVIEW, editor);
            result.setIconImage(OverviewDialog.PROPERTY_EDITOR_ICON.getImage());
            propertyEditorOverviewRef = new WeakReference<OverviewDialog>(result);
        } else {
            result.setEditor(getCurrentProject().getPropertyEditor());
        }
        result.setVisible(true);
    }

    public void showOptionDialog() {
        OptionDialog result = optionDialogRef.get();
        if (result == null) {
            result = new OptionDialog(gmanager, userPrefs);
            optionDialogRef = new WeakReference<OptionDialog>(result);
        }
        result.resetConfig();
        result.setVisible(true);
    }

    public ImportDialog getImportDialog() {
        ImportDialog result = importDialogRef.get();
        if (result == null) {
            result = new ImportDialog(gmanager);
            importDialogRef = new WeakReference<ImportDialog>(result);
        }
        return result;
    }

    public ExportDialog getExportDialog() {
        ExportDialog result = exportDialogRef.get();
        if (result == null) {
            result = new ExportDialog(gmanager);
            exportDialogRef = new WeakReference<ExportDialog>(result);
        }
        return result;
    }

    public HistoryManager getHistoryManager() {
        HistoryManager result = historyManagerRef.get();
        if (result == null) {
            result = new HistoryManager(gmanager.getRootFrame(), this);
            historyManagerRef = new WeakReference<HistoryManager>(result);
        }
        return result;
    }

    public ValidatorDialog getValidator() {
        ValidatorDialog result = validatorRef.get();
        if (result == null) {
            result = new ValidatorDialog(gmanager.getRootFrame(), gmanager);
            validatorRef = new WeakReference<ValidatorDialog>(result);
        }
        return result;
    }

    public ProjectInfoDialog getProjectInfoDialog() {
        ProjectInfoDialog result = projectInfoDialogRef.get();
        if (result == null) {
            result = new ProjectInfoDialog(gmanager, this);
            projectInfoDialogRef = new WeakReference<ProjectInfoDialog>(result);
        }
        return result;
    }

    public MR3LogConsole getLogConsole() {
        return mr3LogConsole;
    }

    private JMenuBar createMenuBar() {
        JMenuBar mb = new JMenuBar();
        mb.add(getFileMenu());
        mb.add(getEditMenu());
        mb.add(getViewMenu());
        mb.add(getWindowMenu());
        mb.add(getToolsMenu());
        mb.add(getHelpMenu());
        return mb;
    }

    private JMenu getEditMenu() {
        JMenu menu = new JMenu(Translator.getString("Component.Edit.Text") + "(E)");
        menu.setMnemonic('e');
        menu.add(findResAction);
        menu.addSeparator();
        menu.add(getSelectMenu());
        // selectAbstractLevelMode = new JCheckBoxMenuItem("Change Abstract
        // Level", false);
        // selectAbstractLevelMode.addActionListener(new
        // SelectAbstractLevelAction());
        // menu.add(selectAbstractLevelMode);

        return menu;
    }

    private JMenu getSelectMenu() {
        JMenu selectMenu = new JMenu(Translator.getString("Component.Select.Text"));
        selectMenu.add(new SelectNodes(gmanager, GraphType.RDF, Translator.getString("Component.Select.RDF.Text")));
        selectMenu.add(new SelectNodes(gmanager, GraphType.CLASS, Translator.getString("Component.Select.Class.Text")));
        selectMenu.add(new SelectNodes(gmanager, GraphType.PROPERTY, Translator
                .getString("Component.Select.Property.Text")));

        return selectMenu;
    }

    public GraphManager getGraphManager() {
        return gmanager;
    }

    private JMenu getFileMenu() {
        JMenu menu = new JMenu(Translator.getString("Component.File.Text") + "(F)");
        menu.setMnemonic('f');
        menu.add(newProjectAction);
        menu.add(openProjectAction);
        menu.add(saveProjectAction);
        menu.add(saveProjectAsAction);
        menu.addSeparator();

        menu.add(new ShowImportDialog(this, Translator.getString("Component.File.Import.Text")));
        menu.add(new ShowExportDialog(this, Translator.getString("Component.File.Export.Text")));
        // exportMenu.add(new ExportJavaObject(this));

        menu.addSeparator();
        menu.add(new ExitAction(this));

        return menu;
    }

    private void initOptions() {
        GraphLayoutUtilities.LAYOUT_TYPE = userPrefs.get(PrefConstants.LAYOUT_TYPE, GraphLayoutUtilities.LAYOUT_TYPE);
        GraphLayoutUtilities.RDF_LAYOUT_DIRECTION = userPrefs.get(PrefConstants.RDF_LAYOUT_DIRECTION,
                GraphLayoutUtilities.RDF_LAYOUT_DIRECTION);
        GraphLayoutUtilities.RDF_LAYOUT_DIRECTION = changeDirectionWithLanguage(
                GraphLayoutUtilities.RDF_LAYOUT_DIRECTION, GraphLayoutUtilities.RDF_LAYOUT_DIRECTION);
        GraphLayoutUtilities.CLASS_LAYOUT_DIRECTION = userPrefs.get(PrefConstants.CLASS_LAYOUT_DIRECTION,
                GraphLayoutUtilities.CLASS_LAYOUT_DIRECTION);
        GraphLayoutUtilities.CLASS_LAYOUT_DIRECTION = changeDirectionWithLanguage(
                GraphLayoutUtilities.CLASS_LAYOUT_DIRECTION, GraphLayoutUtilities.CLASS_LAYOUT_DIRECTION);
        GraphLayoutUtilities.PROPERTY_LAYOUT_DIRECTION = userPrefs.get(PrefConstants.PROPERTY_LAYOUT_DIRECTION,
                GraphLayoutUtilities.PROPERTY_LAYOUT_DIRECTION);
        GraphLayoutUtilities.PROPERTY_LAYOUT_DIRECTION = changeDirectionWithLanguage(
                GraphLayoutUtilities.PROPERTY_LAYOUT_DIRECTION, GraphLayoutUtilities.PROPERTY_LAYOUT_DIRECTION);
        GraphLayoutUtilities.RDF_VERTICAL_SPACE = Integer.parseInt(userPrefs.get(PrefConstants.RDF_VERTICAL_SPACE,
                Integer.toString(GraphLayoutUtilities.VERTICAL_SPACE)));
        GraphLayoutUtilities.RDF_HORIZONTAL_SPACE = Integer.parseInt(userPrefs.get(PrefConstants.RDF_HORIZONTAL_SPACE,
                Integer.toString(GraphLayoutUtilities.HORIZONTAL_SPACE)));
        GraphLayoutUtilities.CLASS_VERTICAL_SPACE = Integer.parseInt(userPrefs.get(PrefConstants.CLASS_VERTICAL_SPACE,
                Integer.toString(GraphLayoutUtilities.VERTICAL_SPACE)));
        GraphLayoutUtilities.CLASS_HORIZONTAL_SPACE = Integer.parseInt(userPrefs.get(
                PrefConstants.CLASS_HORIZONTAL_SPACE, Integer.toString(GraphLayoutUtilities.HORIZONTAL_SPACE)));
        GraphLayoutUtilities.PROPERTY_VERTICAL_SPACE = Integer.parseInt(userPrefs.get(
                PrefConstants.PROPERTY_VERTICAL_SPACE, Integer.toString(GraphLayoutUtilities.VERTICAL_SPACE)));
        GraphLayoutUtilities.PROPERTY_HORIZONTAL_SPACE = Integer.parseInt(userPrefs.get(
                PrefConstants.PROPERTY_HORIZONTAL_SPACE, Integer.toString(GraphLayoutUtilities.HORIZONTAL_SPACE)));

        MR3CellMaker.CELL_WIDTH = Integer.parseInt(userPrefs.get(PrefConstants.NODE_WIDTH, Integer
                .toString(MR3CellMaker.CELL_WIDTH)));
        MR3CellMaker.CELL_HEIGHT = Integer.parseInt(userPrefs.get(PrefConstants.NODE_HEIGHT, Integer
                .toString(MR3CellMaker.CELL_HEIGHT)));

        RDFResourceCell.rdfResourceColor = new Color(userPrefs.getInt(PrefConstants.RDFResourceColor,
                RDFResourceCell.rdfResourceColor.getRGB()));
        RDFLiteralCell.literalColor = new Color(userPrefs.getInt(PrefConstants.LiteralColor,
                RDFLiteralCell.literalColor.getRGB()));
        OntClassCell.classColor = new Color(userPrefs
                .getInt(PrefConstants.ClassColor, OntClassCell.classColor.getRGB()));
        OntPropertyCell.propertyColor = new Color(userPrefs.getInt(PrefConstants.PropertyColor,
                OntPropertyCell.propertyColor.getRGB()));
        GraphUtilities.selectedColor = new Color(userPrefs.getInt(PrefConstants.SelectedColor,
                GraphUtilities.selectedColor.getRGB()));

        GraphUtilities.isColor = userPrefs.getBoolean(PrefConstants.Color, true);

        setSize(userPrefs.getInt(PrefConstants.WindowWidth, MAIN_FRAME_WIDTH), userPrefs.getInt(
                PrefConstants.WindowHeight, MAIN_FRAME_HEIGHT));
        setLocation(userPrefs.getInt(PrefConstants.WindowPositionX, 50), userPrefs.getInt(
                PrefConstants.WindowPositionY, 50));

        HistoryManager.resetFileAppender(userPrefs.get(PrefConstants.logFile, System.getProperty("user.dir")
                + "\\mr3.log"));

        setTitle("MR^3");
        setVisible(true);
    }

    private String changeDirectionWithLanguage(String direction, String type) {
        if (direction.equals("上から下") || direction.equals("UP_TO_DOWN")) {
            userPrefs.put(type, GraphLayoutUtilities.UP_TO_DOWN);
        } else if (direction.equals("左から右") || direction.equals("LEFT_TO_RIGHT")) {
            userPrefs.put(type, GraphLayoutUtilities.LEFT_TO_RIGHT);
        }
        return userPrefs.get(type, GraphLayoutUtilities.UP_TO_DOWN);
    }

    public static MR3Project getCurrentProject() {
        return (MR3Project) desktopTabbedPane.getSelectedComponent();
    }

    public static void setCurrentProjectName() {
        String tabName = getCurrentProject().getCurrentProjectFile().getName().replaceAll(".mr3", "");
        getCurrentProject().getTabComponent().setTabName(tabName);
    }

    public Preferences getUserPrefs() {
        return userPrefs;
    }

    private JMenu getViewMenu() {
        JMenu menu = new JMenu(Translator.getString("Component.View.Text") + "(V)");
        menu.setMnemonic('v');
        ChangeCellViewAction changeCellViewAction = new ChangeCellViewAction();
        uriView = new JCheckBoxMenuItem(Translator.getString("Component.View.URI.Text"));
        uriView.setSelected(true);
        GraphManager.cellViewType = CellViewType.URI;
        uriView.addActionListener(changeCellViewAction);
        idView = new JCheckBoxMenuItem(Translator.getString("Component.View.ID.Text"));
        idView.addActionListener(changeCellViewAction);
        labelView = new JCheckBoxMenuItem(Translator.getString("Component.View.Label.Text"));
        labelView.addActionListener(changeCellViewAction);
        ButtonGroup group = new ButtonGroup();
        group.add(uriView);
        group.add(idView);
        group.add(labelView);
        menu.add(uriView);
        menu.add(idView);
        menu.add(labelView);
        menu.addSeparator();
        showTypeCellBox = new JCheckBoxMenuItem(Translator.getString("Component.View.Type.Text"), true);
        gmanager.setIsShowTypeCell(true);
        showTypeCellBox.addActionListener(new ShowTypeCellAction());
        menu.add(showTypeCellBox);

        // menu.add(new ShowGraphNodeAction("Show Graph Node"));

        showRDFPropertyLabelBox = new JCheckBoxMenuItem(Translator.getString("Component.View.RDFPropertyLabel.Text"),
                true);
        showRDFPropertyLabelBox.addActionListener(new ShowRDFPropertyLabelAction());
        menu.add(showRDFPropertyLabelBox);

        showToolTips = new JCheckBoxMenuItem(Translator.getString("Component.View.ToolTips.Text"), true);
        showToolTips.addActionListener(new ShowToolTipsAction());
        ToolTipManager.sharedInstance().setEnabled(true);
        menu.add(showToolTips);
        menu.addSeparator();
        isGroup = new JCheckBoxMenuItem(Translator.getString("Component.View.Group.Text"), true);
        isGroup.addActionListener(new IsGroupAction());
        menu.add(isGroup);
        menu.addSeparator();
        JMenu applyLayout = new JMenu(Translator.getString("Component.View.ApplyLayout.Text"));
        menu.add(applyLayout);
        applyLayout.add(new GraphLayoutAction(gmanager, GraphType.RDF));
        applyLayout.add(new GraphLayoutAction(gmanager, GraphType.CLASS));
        applyLayout.add(new GraphLayoutAction(gmanager, GraphType.PROPERTY));

        return menu;
    }

    class IsGroupAction extends AbstractAction {
        public void actionPerformed(ActionEvent e) {
            getRDFGraph().getSelectionModel().setChildrenSelectable(!isGroup.isSelected());
        }
    }

    private JMenu getWindowMenu() {
        JMenu menu = new JMenu(Translator.getString("Component.Window.Text") + "(W)");
        menu.setMnemonic('w');
        menu.add(new ShowOverview(this, ShowOverview.RDF_EDITOR_OVERVIEW));
        menu.add(new ShowOverview(this, ShowOverview.CLASS_EDITOR_OVERVIEW));
        menu.add(new ShowOverview(this, ShowOverview.PROPERTY_EDITOR_OVERVIEW));
        menu.addSeparator();
        menu.add(showAttrDialogAction);
        menu.add(showNSTableDialogAction);
        menu.addSeparator();
        JMenu windowMenu = new JMenu(Translator.getString("Component.Window.DeployWindows.Text"));
        menu.add(windowMenu);
        windowMenu.add(deployWindowCPRAction);
        windowMenu.add(deployWindowCRAction);
        windowMenu.add(deployWindowPRAction);

        return menu;
    }

    private JMenu getToolsMenu() {
        JMenu menu = new JMenu(Translator.getString("Component.Tools.Text") + "(T)");
        menu.setMnemonic('t');
        menu.add(openPluginManagerAction);
        menu.add(new ShowValidator(this));
        menu.add(new ShowProjectInfoDialog(this));
        // menu.add(new ShowHistoryManager(this));
        menu.add(new ShowLogConsole(this));
        menu.addSeparator();
        menu.add(new ShowOptionDialog(this));

        return menu;
    }

    private JMenu getHelpMenu() {
        JMenu menu = new JMenu(Translator.getString("Component.Help.Text") + "(H)");
        menu.setMnemonic('h');
        // menu.add(getShowHelpItem());
        // Action gcAction = new AbstractAction() {
        // public void actionPerformed(ActionEvent e) {
        // System.gc();
        // }
        // };
        // gcAction.putValue(Action.NAME, "GC");
        // menu.add(gcAction);
        menu.add(new HelpAbout(this));
        return menu;
    }

    // private JMenuItem getShowHelpItem() {
    // HelpSet hs = null;
    // try {
    // hs = new HelpSet(null, Utilities.getURL("MR3Help/MR3Help.hs"));
    // } catch (Exception ex) {
    // ex.printStackTrace();
    // }
    // HelpBroker hb = hs.createHelpBroker("MR3Help");
    // // hb.enableHelpKey(getRootPane(), "overview", hs); // for F1 おちる
    // ActionListener helper = new CSH.DisplayHelpFromSource(hb);
    // JMenuItem item = new
    // JMenuItem(Translator.getString("Component.Help.ShowHelp.Text"));
    // item.addActionListener(helper);
    //
    // return item;
    // }

    class ShowToolTipsAction extends AbstractAction {
        public void actionPerformed(ActionEvent e) {
            ToolTipManager.sharedInstance().setEnabled(showToolTips.getState());
        }
    }

    class ChangeCellViewAction implements ActionListener {

        private void selectCells(RDFGraph graph) {
            Object[] selectedCells = graph.getSelectionCells();
            graph.setSelectionCells(graph.getAllCells());
            graph.setSelectionCells(selectedCells);
        }

        public void actionPerformed(ActionEvent e) {
            if (e.getSource() == uriView) {
                GraphManager.cellViewType = CellViewType.URI;
            } else if (e.getSource() == idView) {
                GraphManager.cellViewType = CellViewType.ID;
            } else if (e.getSource() == labelView) {
                GraphManager.cellViewType = CellViewType.LABEL;
            }
            GraphUtilities.resizeAllRDFResourceCell(gmanager);
            GraphUtilities.resizeAllRDFSResourceCell(gmanager);
            gmanager.refleshGraphs();
        }
    }

    class ShowGraphNodeAction extends AbstractAction {

        ShowGraphNodeAction(String title) {
            super(title);
        }

        public void actionPerformed(ActionEvent e) {
            RDFGraph classGraph = getClassGraph();
            Object[] cells = classGraph.getSelectionCells();
            if (cells != null) {
                LinkedList<Object> children = new LinkedList<Object>();
                for (int i = 0; i < cells.length; i++) {
                    if (RDFGraph.isRDFSClassCell(cells[i])) {
                        children.add(cells[i]);
                    }
                }
                // メソッド名が変更されている
                // JGraphUtilities.collapseGroup(classGraph,
                // children.toArray());
            }
        }
    }

    class ShowTypeCellAction implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            gmanager.setIsShowTypeCell(showTypeCellBox.isSelected());
            if (showTypeCellBox.isSelected()) {
                gmanager.addTypeCells();
            } else {
                gmanager.removeTypeCells();
            }
        }
    }

    public class ShowRDFPropertyLabelAction implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            gmanager.showRDFPropertyLabel(showRDFPropertyLabelBox.isSelected());
        }
    }

    public void newProject(String basePath) {
        gmanager.getAttrDialog().setNullPanel();
        gmanager.getNSTableDialog().setDefaultNSPrefix();
        Color backgroundColor = new Color(userPrefs.getInt(PrefConstants.BackgroundColor, DESKTOP_BACK_COLOR.getRGB()));
        TabComponent tabComponent = new TabComponent(this, Translator.getString("Component.File.NewProject.Text"));
        MR3Project project = new MR3Project(gmanager, basePath, backgroundColor, tabComponent);
        desktopTabbedPane.addTab(null, project);
        desktopTabbedPane.setTabComponentAt(desktopTabbedPane.getTabCount() - 1, tabComponent);
        HistoryManager.saveHistory(HistoryType.NEW_PROJECT);
    }

    public void removeTab(MR3Project project) {
        desktopTabbedPane.remove(project);
    }

    public MR3Reader getMR3Reader() {
        return mr3Reader;
    }

    public MR3Writer getMR3Writer() {
        return mr3Writer;
    }

    public RDFGraph getRDFGraph() {
        return gmanager.getCurrentRDFGraph();
    }

    public RDFGraph getClassGraph() {
        return gmanager.getCurrentClassGraph();
    }

    public RDFGraph getPropertyGraph() {
        return gmanager.getCurrentPropertyGraph();
    }

    public String getBaseURI() {
        return gmanager.getBaseURI();
    }

    public static void initialize(Class cls) {
        userPrefs = Preferences.userNodeForPackage(cls);
        Translator.loadResourceBundle(userPrefs);
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            System.setProperty("swing.plaf.metal.controlFont", Translator.getString("ControlFont"));
            System.setProperty("swing.plaf.windows.controlFont", Translator.getString("ControlFont"));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void main(String[] arg) {
        initialize(MR3.class);
        JWindow splashWindow = new HelpWindow(null, MR3Constants.SPLASH_LOGO);
        try {
            if (arg.length == 1 && arg[0].equals("--off")) {
                MR3.OFF_META_MODEL_MANAGEMENT = true;
            }
            // クリップボードの内容をクリアする
            StringSelection ss = new StringSelection("");
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(ss, ss);
            new MR3();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            splashWindow.dispose();
        }
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        MR3Project project = (MR3Project) desktopTabbedPane.getSelectedComponent();
        setTitle("MR^3: " + project.getTitle());
    }
}