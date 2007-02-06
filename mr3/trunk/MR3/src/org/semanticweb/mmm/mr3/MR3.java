/*
 * @(#) MR3.java
 * 
 * Copyright (C) 2003-2005 The MMM Project
 * 
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *  
 */

package org.semanticweb.mmm.mr3;

import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.event.*;
import java.io.*;
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
import org.semanticweb.mmm.mr3.util.*;

/**
 * MR3 Meta-Model Management founded on RDF-baed Revision Reflection
 */
public class MR3 extends JFrame {

    public static boolean OFF_META_MODEL_MANAGEMENT;

    private static File currentProject;
    private static Preferences userPrefs;
    private JDesktopPane desktop;

    private MR3Reader mr3Reader;
    private MR3Writer mr3Writer;
    private GraphManager gmanager;

    private WeakReference<RDFEditor> rdfEditorRef;
    private WeakReference<OverviewDialog> rdfEditorOverviewRef;
    private WeakReference<ClassEditor> classEditorRef;
    private WeakReference<OverviewDialog> classEditorOverviewRef;
    private WeakReference<PropertyEditor> propertyEditorRef;
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

    // private JInternalFrame[] iFrames = new JInternalFrame[4];
    private JInternalFrame[] iFrames = new JInternalFrame[3];

    private RDFSInfoMap rdfsInfoMap = RDFSInfoMap.getInstance();

    public static StatusBarPanel STATUS_BAR;
    private static final int MAIN_FRAME_HEIGHT = 600;
    private static final int MAIN_FRAME_WIDTH = 800;
    private static final Color DESKTOP_BACK_COLOR = Color.WHITE;

    public MR3() {
        MR3Constants.loadResourceBundle();
        initWeakReferences();
        mr3LogConsole = new MR3LogConsole(this, Translator.getString("LogConsole.Title"), null);

        desktop = new JDesktopPane();
        gmanager = new GraphManager(desktop, userPrefs, this);
        mr3Reader = new MR3Reader(gmanager);
        mr3Writer = new MR3Writer(gmanager);
        initActions();
        getContentPane().add(createToolBar(), BorderLayout.NORTH);

        STATUS_BAR = new StatusBarPanel();
        desktop.setBackground(DESKTOP_BACK_COLOR);
        getContentPane().add(desktop, BorderLayout.CENTER);
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
        rdfEditorRef = new WeakReference<RDFEditor>(null);
        rdfEditorOverviewRef = new WeakReference<OverviewDialog>(null);
        classEditorRef = new WeakReference<ClassEditor>(null);
        classEditorOverviewRef = new WeakReference<OverviewDialog>(null);
        propertyEditorRef = new WeakReference<PropertyEditor>(null);
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
    private AbstractAction toFrontRDFEditorAction;
    private AbstractAction toFrontClassEditorAction;
    private AbstractAction toFrontPropertyEditorAction;
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
        toFrontRDFEditorAction = new EditorSelect(this, EditorSelect.RDF_EDITOR, EditorSelect.RDF_EDITOR_ICON);
        toFrontClassEditorAction = new EditorSelect(this, EditorSelect.CLASS_EDITOR, EditorSelect.CLASS_EDITOR_ICON);
        toFrontPropertyEditorAction = new EditorSelect(this, EditorSelect.PROPERTY_EDITOR,
                EditorSelect.PROPERTY_EDITOR_ICON);
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
        toolbar.add(toFrontRDFEditorAction);
        toolbar.add(toFrontClassEditorAction);
        toolbar.add(toFrontPropertyEditorAction);
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
            findList = gmanager.getFindResourceDialog().getFindResources(findField.getText() + ".*");
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

    public RDFEditor getRDFEditor() {
        RDFEditor result = rdfEditorRef.get();
        if (result == null) {
            result = new RDFEditor(gmanager);
            gmanager.setRDFEditor(result);
            iFrames[0] = result;
            setRDFEditorBounds();
            desktop.add(iFrames[0], Cursor.DEFAULT_CURSOR);
            rdfEditorRef = new WeakReference<RDFEditor>(result);
        }
        return result;
    }

    public void showRDFEditorOverview() {
        if (!isActiveFrames()) { return; }
        OverviewDialog result = rdfEditorOverviewRef.get();
        if (result == null) {
            RDFEditor editor = getRDFEditor();
            result = new OverviewDialog(OverviewDialog.RDF_EDITOR_OVERVIEW, editor.getGraph(), editor.getJViewport());
            result.setFrameIcon(OverviewDialog.RDF_EDITOR_ICON);
            desktop.add(result, JLayeredPane.MODAL_LAYER);
            rdfEditorOverviewRef = new WeakReference<OverviewDialog>(result);
        }
        result.setVisible(true);
    }

    public ClassEditor getClassEditor() {
        ClassEditor result = classEditorRef.get();
        if (result == null) {
            result = new ClassEditor(gmanager);
            gmanager.setClassEditor(result);
            iFrames[1] = result;
            setClassEditorBounds();
            desktop.add(iFrames[1], Cursor.DEFAULT_CURSOR);
            classEditorRef = new WeakReference<ClassEditor>(result);
        }
        return result;
    }

    public void showClassEditorOverview() {
        if (!isActiveFrames()) { return; }
        OverviewDialog result = classEditorOverviewRef.get();
        if (result == null) {
            ClassEditor editor = getClassEditor();
            result = new OverviewDialog(OverviewDialog.CLASS_EDITOR_OVERVIEW, editor.getGraph(), editor.getJViewport());
            result.setFrameIcon(OverviewDialog.CLASS_EDITOR_ICON);
            desktop.add(result, JLayeredPane.MODAL_LAYER);
            classEditorOverviewRef = new WeakReference<OverviewDialog>(result);
        }
        result.setVisible(true);
    }

    public PropertyEditor getPropertyEditor() {
        PropertyEditor result = propertyEditorRef.get();
        if (result == null) {
            result = new PropertyEditor(gmanager);
            gmanager.setPropertyEditor(result);
            iFrames[2] = result;
            setPropertyEditorBounds();
            desktop.add(iFrames[2], Cursor.DEFAULT_CURSOR);
            propertyEditorRef = new WeakReference<PropertyEditor>(result);
        }
        return result;
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
        if (!isActiveFrames()) { return; }
        OverviewDialog result = propertyEditorOverviewRef.get();
        if (result == null) {
            PropertyEditor editor = getPropertyEditor();
            result = new OverviewDialog(OverviewDialog.PROPERTY_EDITOR_OVERVIEW, editor.getGraph(), editor
                    .getJViewport());
            result.setFrameIcon(OverviewDialog.PROPERTY_EDITOR_ICON);
            desktop.add(result, JLayeredPane.MODAL_LAYER);
            propertyEditorOverviewRef = new WeakReference<OverviewDialog>(result);
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
        if (!isActiveFrames()) { return null; }
        ImportDialog result = importDialogRef.get();
        if (result == null) {
            result = new ImportDialog(gmanager);
            importDialogRef = new WeakReference<ImportDialog>(result);
        }
        return result;
    }

    public ExportDialog getExportDialog() {
        if (!isActiveFrames()) { return null; }
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
        if (!isActiveFrames()) { return null; }
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
        selectMenu.add(new SelectNodes(getRDFGraph(), Translator.getString("Component.Select.RDF.Text")));
        selectMenu.add(new SelectNodes(getClassGraph(), Translator.getString("Component.Select.Class.Text")));
        selectMenu.add(new SelectNodes(getPropertyGraph(), Translator.getString("Component.Select.Property.Text")));

        return selectMenu;
    }

    public boolean isActiveFrames() {
        for (int i = 0; i < iFrames.length; i++) {
            if (iFrames[i] == null) { return false; }
        }
        return true;
        // return iFrames[0] != null && iFrames[1] != null && iFrames[2] !=
        // null;
    }

    public JInternalFrame[] getInternalFrames() {
        return iFrames;
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

    private static final int EDITOR_WIDTH = 792;
    private static final int EDITOR_HEIGHT = 518;

    private void setRDFEditorBounds() {
        int editorPositionX = userPrefs.getInt(PrefConstants.RDFEditorPositionX, 0);
        int editorPositionY = userPrefs.getInt(PrefConstants.RDFEditorPositionY, EDITOR_HEIGHT / 2);
        int editorWidth = userPrefs.getInt(PrefConstants.RDFEditorWidth, EDITOR_WIDTH);
        int editorHeight = userPrefs.getInt(PrefConstants.RDFEditorHeight, EDITOR_HEIGHT / 2);
        iFrames[0].setBounds(new Rectangle(editorPositionX, editorPositionY, editorWidth, editorHeight)); // RDF
    }

    private void setClassEditorBounds() {
        int editorPositionX = userPrefs.getInt(PrefConstants.ClassEditorPositionX, 0);
        int editorPositionY = userPrefs.getInt(PrefConstants.ClassEditorPositionY, 0);
        int editorWidth = userPrefs.getInt(PrefConstants.ClassEditorWidth, EDITOR_WIDTH / 2);
        int editorHeight = userPrefs.getInt(PrefConstants.ClassEditorHeight, EDITOR_HEIGHT / 2);
        iFrames[1].setBounds(new Rectangle(editorPositionX, editorPositionY, editorWidth, editorHeight)); // Class
    }

    private void setPropertyEditorBounds() {
        int editorPositionX = userPrefs.getInt(PrefConstants.PropertyEditorPositionX, EDITOR_WIDTH / 2);
        int editorPositionY = userPrefs.getInt(PrefConstants.PropertyEditorPositionY, 0);
        int editorWidth = userPrefs.getInt(PrefConstants.PropertyEditorWidth, EDITOR_WIDTH / 2);
        int editorHeight = userPrefs.getInt(PrefConstants.PropertyEditorHeight, EDITOR_HEIGHT / 2);
        iFrames[2].setBounds(new Rectangle(editorPositionX, editorPositionY, editorWidth, editorHeight)); // Property
    }

    private void setRDFSTreeEditorBounds() {
        int editorPositionX = userPrefs.getInt(PrefConstants.RDFSTreeEditorPositionX, 0);
        int editorPositionY = userPrefs.getInt(PrefConstants.RDFSTreeEditorPositionY, 0);
        int editorWidth = userPrefs.getInt(PrefConstants.RDFSTreeEditorWidth, EDITOR_WIDTH / 3);
        int editorHeight = userPrefs.getInt(PrefConstants.RDFSTreeEditorHeight, EDITOR_HEIGHT);
        iFrames[3].setBounds(new Rectangle(editorPositionX, editorPositionY, editorWidth, editorHeight)); // Property
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
        gmanager.setGraphBackground(new Color(userPrefs.getInt(PrefConstants.BackgroundColor, DESKTOP_BACK_COLOR
                .getRGB())));

        setSize(userPrefs.getInt(PrefConstants.WindowWidth, MAIN_FRAME_WIDTH), userPrefs.getInt(
                PrefConstants.WindowHeight, MAIN_FRAME_HEIGHT));
        setLocation(userPrefs.getInt(PrefConstants.WindowPositionX, 50), userPrefs.getInt(
                PrefConstants.WindowPositionY, 50));

        HistoryManager.resetFileAppender(userPrefs.get(PrefConstants.logFile, System.getProperty("user.dir")
                + "\\mr3.log"));

        setTitle("MR3 - ");
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

    public void clearMap() {
        rdfsInfoMap.clear();
    }

    public static File getCurrentProject() {
        return currentProject;
    }

    public static void setCurrentProject(File file) {
        currentProject = file;
        // HistoryManager.resetFileAppender(file.getAbsolutePath() +
        // ".log.txt");
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
        showTypeCellBox = new JCheckBoxMenuItem(Translator.getString("Component.View.Type.Text"), false);
        gmanager.setIsShowTypeCell(false);
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
        menu.add(toFrontRDFEditorAction);
        menu.add(toFrontClassEditorAction);
        menu.add(toFrontPropertyEditorAction);
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
        gmanager.getNSTableDialog().resetNSTable();
        gmanager.getAttrDialog().setNullPanel();
        clearMap();
        gmanager.removeAllCells();
        gmanager.getNSTableDialog().setDefaultNSPrefix();
        setTitle("MR3 - " + Translator.getString("Component.File.NewProject.Text"));
        setCurrentProject(new File(basePath, Translator.getString("Component.File.NewProject.Text")));
        getRDFEditor().setVisible(true);
        getClassEditor().setVisible(true);
        getPropertyEditor().setVisible(true);
        // getRDFSTreeEditor().setVisible(false);
        HistoryManager.saveHistory(HistoryType.NEW_PROJECT);
    }

    public JDesktopPane getDesktopPane() {
        return gmanager.getDesktop();
    }

    public MR3Reader getMR3Reader() {
        return mr3Reader;
    }

    public MR3Writer getMR3Writer() {
        return mr3Writer;
    }

    public RDFGraph getRDFGraph() {
        return gmanager.getRDFGraph();
    }

    public RDFGraph getClassGraph() {
        return gmanager.getClassGraph();
    }

    public RDFGraph getPropertyGraph() {
        return gmanager.getPropertyGraph();
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
        JWindow splashWindow = new HelpWindow(null, MR3Constants.LOGO);
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
}