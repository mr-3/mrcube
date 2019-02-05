/*
 * Project Name: MR^3 (Meta-Model Management based on RDFs Revision Reflection)
 * Project Website: http://mrcube.org/
 *
 * Copyright (C) 2003-2018 Yamaguchi Laboratory, Keio University. All rights reserved.
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

package org.mrcube;

import org.apache.jena.sys.JenaSystem;
import org.mrcube.actions.*;
import org.mrcube.editors.ClassEditor;
import org.mrcube.editors.PropertyEditor;
import org.mrcube.editors.RDFEditor;
import org.mrcube.io.MR3Reader;
import org.mrcube.io.MR3Writer;
import org.mrcube.jgraph.GraphManager;
import org.mrcube.jgraph.RDFGraph;
import org.mrcube.layout.GraphLayoutUtilities;
import org.mrcube.models.MR3Constants;
import org.mrcube.models.MR3Constants.CellViewType;
import org.mrcube.models.MR3Constants.DeployType;
import org.mrcube.models.MR3Constants.GraphType;
import org.mrcube.models.MR3Constants.HistoryType;
import org.mrcube.models.PrefConstants;
import org.mrcube.utils.GraphUtilities;
import org.mrcube.utils.MR3CellMaker;
import org.mrcube.utils.Translator;
import org.mrcube.utils.Utilities;
import org.mrcube.views.*;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.*;
import java.io.File;
import java.lang.ref.WeakReference;
import java.util.prefs.Preferences;

/**
 * @author Takeshi Morita
 */
public class MR3 extends JFrame implements ChangeListener {

    public static boolean OFF_META_MODEL_MANAGEMENT;

    private static Preferences userPrefs;
    private static MR3ProjectPanel mr3ProjectPanel;

    private final MR3Reader mr3Reader;
    private final MR3Writer mr3Writer;
    private final GraphManager gmanager;

    private final QuitAction quitAction;

    private WeakReference<OverviewDialog> rdfEditorOverviewRef;
    private WeakReference<OverviewDialog> classEditorOverviewRef;
    private WeakReference<OverviewDialog> propertyEditorOverviewRef;
    // private WeakReference<OntTreeEditor> ontTreeEditorRef;
    private WeakReference<OptionDialog> optionDialogRef;
    private WeakReference<RDFSourceCodeViewer> RDFSourceCodeViewerRef;
    private WeakReference<HistoryManager> historyManagerRef;
    private WeakReference<ValidatorDialog> validatorRef;
    private WeakReference<ProjectInfoDialog> projectInfoDialogRef;
    private final MR3LogConsole mr3LogConsole;

    private JCheckBoxMenuItem uriView;
    private JCheckBoxMenuItem idView;
    private JCheckBoxMenuItem labelView;

    private JCheckBoxMenuItem showTypeCellBox;
    private JCheckBoxMenuItem showToolTips;
    private JCheckBoxMenuItem showRDFPropertyLabelBox;

    public static JTextField ResourcePathTextField;
    public static StatusBarPanel STATUS_BAR;
    private static final int MAIN_FRAME_WIDTH = 1024;
    private static final int MAIN_FRAME_HEIGHT = 768;

    public MR3() {
        MR3Constants.loadResourceBundle();
        initWeakReferences();
        mr3LogConsole = new MR3LogConsole(this, Translator.getString("LogConsole.Title"),
                Utilities.getImageIcon("ic_message_black_18dp.png").getImage());

        gmanager = new GraphManager(userPrefs, this);

        mr3Reader = new MR3Reader(gmanager);
        mr3Writer = new MR3Writer(gmanager);
        initActions();
        getContentPane().add(createToolBar(), BorderLayout.NORTH);

        var resourcePathPanel = new JPanel();
        ResourcePathTextField = new JTextField();
        var openResourceButton = new JButton(openResourceAction);
        resourcePathPanel.setLayout(new BorderLayout());
        resourcePathPanel.add(ResourcePathTextField, BorderLayout.CENTER);
        resourcePathPanel.add(openResourceButton, BorderLayout.EAST);
        STATUS_BAR = new StatusBarPanel();
        mr3ProjectPanel = new MR3ProjectPanel(gmanager);
        mr3ProjectPanel.add(resourcePathPanel, BorderLayout.NORTH);

        gmanager.setMR3ProjectPanel(mr3ProjectPanel);
        getContentPane().add(mr3ProjectPanel, BorderLayout.CENTER);
        getContentPane().add(STATUS_BAR, BorderLayout.SOUTH);

        quitAction = new QuitAction(this);
        if (Desktop.isDesktopSupported()) {
            var desktop = Desktop.getDesktop();
            if (desktop.isSupported(Desktop.Action.APP_QUIT_HANDLER)) {
                desktop.setQuitHandler((e, response) -> {
                    // do nothing but invoke quit action
                });
            }
        }
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                quitAction.quitMR3();
            }
        });
        setJMenuBar(createMenuBar());
        initOptions();

        HistoryManager.setGraphManager(gmanager);
        String logFilePath = userPrefs.get(PrefConstants.logFile, System.getProperty("user.dir") + "/"
                + HistoryManager.DEFAULT_LOG_FILE_NAME);
        HistoryManager.initLogger(logFilePath);
        newProject();
    }


    private void initWeakReferences() {
        rdfEditorOverviewRef = new WeakReference<>(null);
        classEditorOverviewRef = new WeakReference<>(null);
        propertyEditorOverviewRef = new WeakReference<>(null);
        // ontTreeEditorRef = new WeakReference<OntTreeEditor>(null);
        RDFSourceCodeViewerRef = new WeakReference<>(null);
        historyManagerRef = new WeakReference<>(null);
        validatorRef = new WeakReference<>(null);
        projectInfoDialogRef = new WeakReference<>(null);
        optionDialogRef = new WeakReference<>(null);
    }

    private AbstractAction newProjectAction;
    private OpenFileAction openFileAction;
    private OpenResourceAction openResourceAction;
    private AbstractAction saveFileAction;
    private AbstractAction saveFileAsAction;
    private AbstractAction showValidatorAction;
    private AbstractAction deployWindowCPRAction;
    private AbstractAction deployWindowCRAction;
    private AbstractAction deployWindowPRAction;
    private AbstractAction showAttrDialogAction;
    private AbstractAction showNSTableDialogAction;
    private AbstractAction showRDFSourceCodeViewer;
    private AbstractAction findResAction;
    private AbstractAction showProjectInfoAction;
    private AbstractAction showLogConsoleAciton;
    private AbstractAction showOptionDialogAction;
    private AbstractAction showVersionInfoAction;

    private final ImageIcon CPR_ICON = Utilities.getImageIcon(Translator.getString("Menu.Window.DeployCPRWindows.Icon"));
    private final ImageIcon CR_ICON = Utilities.getImageIcon(Translator.getString("Menu.Window.DeployCRWindows.Icon"));
    private final ImageIcon PR_ICON = Utilities.getImageIcon(Translator.getString("Menu.Window.DeployPRWindows.Icon"));

    private void initActions() {
        newProjectAction = new NewProject(this);
        openResourceAction = new OpenResourceAction(this);
        openFileAction = new OpenFileAction(this);
        saveFileAction = new SaveFileAction(this, SaveFileAction.SAVE_PROJECT, SaveFileAction.SAVE_PROJECT_ICON);
        saveFileAsAction = new SaveFileAction(this, SaveFileAction.SAVE_AS_PROJECT, SaveFileAction.SAVE_AS_PROJECT_ICON);
        showValidatorAction = new ShowValidator(this);

        deployWindowCPRAction = new DeployWindows(this, Translator.getString("Menu.Window.DeployCPRWindows.Text"),
                CPR_ICON, DeployType.CPR,
                KeyStroke.getKeyStroke(KeyEvent.VK_1, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
        deployWindowCRAction = new DeployWindows(this, Translator.getString("Menu.Window.DeployCRWindows.Text"),
                CR_ICON, DeployType.CR,
                KeyStroke.getKeyStroke(KeyEvent.VK_2, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
        deployWindowPRAction = new DeployWindows(this, Translator.getString("Menu.Window.DeployPRWindows.Text"),
                PR_ICON, DeployType.PR,
                KeyStroke.getKeyStroke(KeyEvent.VK_3, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
        showAttrDialogAction = new ShowAttrDialog(this);
        showNSTableDialogAction = new ShowNSTableDialog(this);
        showRDFSourceCodeViewer = new ShowRDFSourceCodeViewer(this, Translator.getString("RDFSourceCodeViewer.Title"));
        findResAction = new FindResAction(null, gmanager);
        showProjectInfoAction = new ShowProjectInfoDialog(this);
        showLogConsoleAciton = new ShowLogConsole(this);
        showOptionDialogAction = new ShowOptionDialog(this);
        showVersionInfoAction = new ShowVersionInfoAction(this);
    }

    private JToolBar createToolBar() {
        JToolBar toolbar = new JToolBar();
        toolbar.setFloatable(false);
        toolbar.add(newProjectAction);
        toolbar.add(openFileAction);
        toolbar.add(saveFileAction);
        toolbar.add(saveFileAsAction);
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
        toolbar.add(showRDFSourceCodeViewer);
        toolbar.add(showValidatorAction);
        toolbar.add(showProjectInfoAction);
        toolbar.add(showOptionDialogAction);

        return toolbar;
    }

    public void showRDFEditorOverview() {
        OverviewDialog result = rdfEditorOverviewRef.get();
        if (result == null) {
            RDFEditor editor = getCurrentProject().getRDFEditor();
            result = new OverviewDialog(this, OverviewDialog.RDF_EDITOR_OVERVIEW, editor);
            result.setIconImage(OverviewDialog.RDF_EDITOR_ICON.getImage());
            rdfEditorOverviewRef = new WeakReference<>(result);
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
            classEditorOverviewRef = new WeakReference<>(result);
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
            propertyEditorOverviewRef = new WeakReference<>(result);
        } else {
            result.setEditor(getCurrentProject().getPropertyEditor());
        }
        result.setVisible(true);
    }

    public void showOptionDialog() {
        OptionDialog result = optionDialogRef.get();
        if (result == null) {
            result = new OptionDialog(gmanager, userPrefs);
            optionDialogRef = new WeakReference<>(result);
        }
        result.resetConfig();
        result.setVisible(true);
    }

    public RDFSourceCodeViewer getRDFSourceCodeViewer() {
        RDFSourceCodeViewer result = RDFSourceCodeViewerRef.get();
        if (result == null) {
            result = new RDFSourceCodeViewer(gmanager);
            RDFSourceCodeViewerRef = new WeakReference<>(result);
        }
        return result;
    }

    public HistoryManager getHistoryManager() {
        HistoryManager result = historyManagerRef.get();
        if (result == null) {
            result = new HistoryManager(gmanager.getRootFrame(), this);
            historyManagerRef = new WeakReference<>(result);
        }
        return result;
    }

    public ValidatorDialog getValidator() {
        ValidatorDialog result = validatorRef.get();
        if (result == null) {
            result = new ValidatorDialog(gmanager.getRootFrame(), gmanager);
            validatorRef = new WeakReference<>(result);
        }
        return result;
    }

    public ProjectInfoDialog getProjectInfoDialog() {
        ProjectInfoDialog result = projectInfoDialogRef.get();
        if (result == null) {
            result = new ProjectInfoDialog(gmanager, this);
            projectInfoDialogRef = new WeakReference<>(result);
        }
        return result;
    }

    public MR3LogConsole getLogConsole() {
        return mr3LogConsole;
    }

    private JMenuBar createMenuBar() {
        JMenuBar mb = new JMenuBar();
        mb.add(getFileMenu());
        mb.add(getViewMenu());
        mb.add(getWindowMenu());
        mb.add(getToolsMenu());
        mb.add(getHelpMenu());
        return mb;
    }

    public GraphManager getGraphManager() {
        return gmanager;
    }

    private JMenu getFileMenu() {
        JMenu menu = new JMenu(Translator.getString("Menu.File.Text") + "(F)");
        menu.setMnemonic('f');
        menu.add(newProjectAction);
        menu.add(openFileAction);
        menu.addSeparator();
        menu.add(saveFileAction);
        menu.add(saveFileAsAction);
        menu.addSeparator();
        menu.add(quitAction);

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

        MR3CellMaker.CELL_WIDTH = Integer.parseInt(userPrefs.get(PrefConstants.NODE_WIDTH, Integer.toString(MR3CellMaker.CELL_WIDTH)));
        MR3CellMaker.CELL_HEIGHT = Integer.parseInt(userPrefs.get(PrefConstants.NODE_HEIGHT, Integer.toString(MR3CellMaker.CELL_HEIGHT)));

        setSize(userPrefs.getInt(PrefConstants.WindowWidth, MAIN_FRAME_WIDTH),
                userPrefs.getInt(PrefConstants.WindowHeight, MAIN_FRAME_HEIGHT));
        setLocation(userPrefs.getInt(PrefConstants.WindowPositionX, 50), userPrefs.getInt(PrefConstants.WindowPositionY, 50));

        HistoryManager.resetFileAppender(userPrefs.get(PrefConstants.logFile, System.getProperty("user.dir") + "\\"
                + HistoryManager.DEFAULT_LOG_FILE_NAME));

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

    public static MR3ProjectPanel getCurrentProject() {
        return mr3ProjectPanel;
    }

    public Preferences getUserPrefs() {
        return userPrefs;
    }

    private JMenu getViewMenu() {
        JMenu menu = new JMenu(Translator.getString("Menu.View.Text") + "(V)");
        menu.setMnemonic('v');
        ChangeCellViewAction changeCellViewAction = new ChangeCellViewAction();
        uriView = new JCheckBoxMenuItem(Translator.getString("Menu.View.URI.Text"));
        uriView.setSelected(true);
        GraphManager.cellViewType = CellViewType.URI;
        uriView.addActionListener(changeCellViewAction);
        idView = new JCheckBoxMenuItem(Translator.getString("Menu.View.ID.Text"));
        idView.addActionListener(changeCellViewAction);
        labelView = new JCheckBoxMenuItem(Translator.getString("Menu.View.Label.Text"));
        labelView.addActionListener(changeCellViewAction);
        ButtonGroup group = new ButtonGroup();
        group.add(uriView);
        group.add(idView);
        group.add(labelView);
        menu.add(uriView);
        menu.add(idView);
        menu.add(labelView);
        menu.addSeparator();
        showTypeCellBox = new JCheckBoxMenuItem(Translator.getString("Menu.View.Type.Text"), true);
        gmanager.setIsShowTypeCell(true);
        showTypeCellBox.addActionListener(new ShowTypeCellAction());
        menu.add(showTypeCellBox);

        showRDFPropertyLabelBox = new JCheckBoxMenuItem(Translator.getString("Menu.View.RDFPropertyLabel.Text"),
                true);
        showRDFPropertyLabelBox.addActionListener(new ShowRDFPropertyLabelAction());
        menu.add(showRDFPropertyLabelBox);

        showToolTips = new JCheckBoxMenuItem(Translator.getString("Menu.View.ToolTips.Text"), true);
        showToolTips.addActionListener(new ShowToolTipsAction());
        ToolTipManager.sharedInstance().setEnabled(true);
        menu.add(showToolTips);
        menu.addSeparator();

        menu.add(new GraphLayoutAction(gmanager, GraphType.RDF, GraphLayoutAction.layoutRDFGraphIcon));
        menu.add(new GraphLayoutAction(gmanager, GraphType.CLASS, GraphLayoutAction.layoutClassGraphIcon));
        menu.add(new GraphLayoutAction(gmanager, GraphType.PROPERTY, GraphLayoutAction.layoutPropertyGraphIcon));

        return menu;
    }

    private JMenu getWindowMenu() {
        JMenu menu = new JMenu(Translator.getString("Menu.Window.Text") + "(W)");
        menu.setMnemonic('w');
        menu.add(new ShowOverview(this, ShowOverview.RDF_EDITOR_OVERVIEW, ShowOverview.RDF_EDITOR_OVERVIEW_ICON));
        menu.add(new ShowOverview(this, ShowOverview.CLASS_EDITOR_OVERVIEW, ShowOverview.CLASS_EDITOR_OVERVIEW_ICON));
        menu.add(new ShowOverview(this, ShowOverview.PROPERTY_EDITOR_OVERVIEW, ShowOverview.PROPERTY_EDITOR_OVERVIEW_ICON));
        menu.addSeparator();
        menu.add(showAttrDialogAction);
        menu.add(showNSTableDialogAction);
        menu.addSeparator();
        menu.add(deployWindowCPRAction);
        menu.add(deployWindowCRAction);
        menu.add(deployWindowPRAction);

        return menu;
    }

    private JMenu getToolsMenu() {
        JMenu menu = new JMenu(Translator.getString("Menu.Tools.Text") + "(T)");
        menu.setMnemonic('t');
        menu.add(showRDFSourceCodeViewer);
        menu.add(findResAction);
        menu.add(showValidatorAction);
        menu.add(showProjectInfoAction);
        // menu.add(new ShowHistoryManager(this));
        menu.add(showLogConsoleAciton);
        menu.addSeparator();
        menu.add(showOptionDialogAction);

        return menu;
    }

    private JMenu getHelpMenu() {
        JMenu menu = new JMenu(Translator.getString("Menu.Help.Text") + "(H)");
        menu.setMnemonic('h');
        menu.add(showVersionInfoAction);
        return menu;
    }

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
            gmanager.refreshGraphs();
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

    class ShowRDFPropertyLabelAction implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            gmanager.showRDFPropertyLabel(showRDFPropertyLabelBox.isSelected());
        }
    }

    public void newProject() {
        gmanager.getAttrDialog().setNullPanel();
        gmanager.getNSTableDialog().setDefaultNSPrefix();
        var newFile = new File(Translator.getString("Menu.File.New.Text"));
        MR3.getCurrentProject().setCurrentProjectFile(newFile);
        HistoryManager.saveHistory(HistoryType.NEW_PROJECT);
        mr3ProjectPanel.resetEditors();
        mr3ProjectPanel.deployCPR();
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
        JenaSystem.init();
        userPrefs = Preferences.userNodeForPackage(cls);
        Translator.loadResourceBundle(userPrefs);
        UIManager.put("TitledBorder.border", new LineBorder(new Color(200, 200, 200), 1));
        try {
            try {
                for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                    if ("Nimbus".equals(info.getName())) {
                        UIManager.setLookAndFeel(info.getClassName());
                        break;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            System.setProperty("swing.plaf.metal.controlFont", Translator.getString("ControlFont"));
            System.setProperty("swing.plaf.windows.controlFont", Translator.getString("ControlFont"));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        StringSelection ss = new StringSelection("");
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(ss, null);
        if (Taskbar.isTaskbarSupported()) {
            var taskbar = Taskbar.getTaskbar();
            if (taskbar.isSupported(Taskbar.Feature.ICON_IMAGE)) {
                taskbar.setIconImage(MR3Constants.SPLASH_LOGO.getImage());
            }
        }
    }

    public static void main(String[] arg) {
        initialize(MR3.class);
        JWindow splashWindow = new SplashWindow(null, MR3Constants.SPLASH_LOGO);
        try {
            if (arg.length == 1 && arg[0].equals("--off")) {
                MR3.OFF_META_MODEL_MANAGEMENT = true;
            }
            new MR3();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            splashWindow.dispose();
        }
    }

    public void stateChanged(ChangeEvent e) {
        if (mr3ProjectPanel != null) {
            setTitle("MR^3: " + mr3ProjectPanel.getTitle());
        } else {
            setTitle("MR^3");
        }
    }
}