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

package jp.ac.aoyama.it.ke.mrcube;

import com.formdev.flatlaf.FlatLightLaf;
import jp.ac.aoyama.it.ke.mrcube.actions.*;
import jp.ac.aoyama.it.ke.mrcube.editors.ClassEditor;
import jp.ac.aoyama.it.ke.mrcube.editors.Editor;
import jp.ac.aoyama.it.ke.mrcube.editors.InstanceEditor;
import jp.ac.aoyama.it.ke.mrcube.editors.PropertyEditor;
import jp.ac.aoyama.it.ke.mrcube.io.MR3Reader;
import jp.ac.aoyama.it.ke.mrcube.io.MR3Writer;
import jp.ac.aoyama.it.ke.mrcube.jgraph.*;
import jp.ac.aoyama.it.ke.mrcube.layout.GraphLayoutUtilities;
import jp.ac.aoyama.it.ke.mrcube.models.MR3Constants;
import jp.ac.aoyama.it.ke.mrcube.models.MR3Constants.ArrangeWindowsType;
import jp.ac.aoyama.it.ke.mrcube.models.MR3Constants.CellViewType;
import jp.ac.aoyama.it.ke.mrcube.models.MR3Constants.GraphType;
import jp.ac.aoyama.it.ke.mrcube.models.MR3Constants.HistoryType;
import jp.ac.aoyama.it.ke.mrcube.models.PrefConstants;
import jp.ac.aoyama.it.ke.mrcube.utils.GraphUtilities;
import jp.ac.aoyama.it.ke.mrcube.utils.MR3CellMaker;
import jp.ac.aoyama.it.ke.mrcube.utils.Translator;
import jp.ac.aoyama.it.ke.mrcube.utils.Utilities;
import jp.ac.aoyama.it.ke.mrcube.views.*;
import org.apache.jena.sys.JenaSystem;

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

    private WeakReference<OverviewDialog> instanceEditorOverviewRef;
    private WeakReference<OverviewDialog> classEditorOverviewRef;
    private WeakReference<OverviewDialog> propertyEditorOverviewRef;
    // private WeakReference<OntTreeEditor> ontTreeEditorRef;
    private WeakReference<OptionDialog> optionDialogRef;
    private WeakReference<RDFSourceCodeViewer> RDFSourceCodeViewerRef;
    private final HistoryManager historyManager;
    private final SPARQLQueryDialog sparqlQueryDialog;
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
                Utilities.getSVGIcon("log.svg").getImage());
        gmanager = new GraphManager(userPrefs, this);
        mr3Reader = new MR3Reader(gmanager);
        mr3Writer = new MR3Writer(gmanager);
        historyManager = new HistoryManager(this, this);
        sparqlQueryDialog = new SPARQLQueryDialog(mr3Writer, gmanager);
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
        setIconImage(MR3Constants.SPLASH_LOGO.getImage());
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                quitAction.quitMR3();
            }
        });
        setJMenuBar(createMenuBar());
        initOptions();
        HistoryManager.setGraphManager(gmanager);
        newProject();
    }


    private void initWeakReferences() {
        instanceEditorOverviewRef = new WeakReference<>(null);
        classEditorOverviewRef = new WeakReference<>(null);
        propertyEditorOverviewRef = new WeakReference<>(null);
        // ontTreeEditorRef = new WeakReference<OntTreeEditor>(null);
        RDFSourceCodeViewerRef = new WeakReference<>(null);
        validatorRef = new WeakReference<>(null);
        projectInfoDialogRef = new WeakReference<>(null);
        optionDialogRef = new WeakReference<>(null);
    }

    private AbstractAction newProjectAction;
    private OpenFileAction openFileAction;
    private OpenResourceAction openResourceAction;
    private AbstractAction saveFileAction;
    private AbstractAction saveFileAsAction;
    private AbstractAction saveInstanceGraphAsImageFileAction;
    private AbstractAction saveClassGraphAsImageFileAction;
    private AbstractAction savePropertyGraphAsImageFileAction;
    private AbstractAction showValidatorAction;
    private AbstractAction arrangeWindowsCPIAction;
    private AbstractAction arrangeWindowsCIAction;
    private AbstractAction arrangeWindowsPIAction;
    private AbstractAction showAttrDialogAction;
    private AbstractAction showNSTableDialogAction;
    private AbstractAction showRDFSourceCodeViewer;
    private AbstractAction findResAction;
    private AbstractAction showSPARQLQueryDialogAction;
    private AbstractAction showProjectInfoAction;
    private AbstractAction showLogConsoleAciton;
    private AbstractAction showHistoryManagerAciton;
    private AbstractAction showOptionDialogAction;
    private AbstractAction showVersionInfoAction;
    private AbstractAction showManualAction;

    private void initActions() {
        newProjectAction = new NewProject(this);
        openResourceAction = new OpenResourceAction(this);
        openFileAction = new OpenFileAction(this);
        saveFileAction = new SaveFileAction(this, SaveFileAction.SAVE_PROJECT, SaveFileAction.SAVE_PROJECT_ICON);
        saveFileAsAction = new SaveFileAction(this, SaveFileAction.SAVE_AS_PROJECT, SaveFileAction.SAVE_AS_PROJECT_ICON);
        saveInstanceGraphAsImageFileAction = new SaveGraphImageAction(gmanager, GraphType.Instance,
                Translator.getString("Menu.File.SaveInstanceGraphAsImageFile.Text"),
                Utilities.getSVGIcon(Translator.getString("InstanceEditor.Icon")));
        saveClassGraphAsImageFileAction = new SaveGraphImageAction(gmanager, GraphType.Class,
                Translator.getString("Menu.File.SaveClassGraphAsImageFile.Text"),
                Utilities.getSVGIcon(Translator.getString("ClassEditor.Icon")));
        savePropertyGraphAsImageFileAction = new SaveGraphImageAction(gmanager, GraphType.Property,
                Translator.getString("Menu.File.SavePropertyGraphAsImageFile.Text"),
                Utilities.getSVGIcon(Translator.getString("PropertyEditor.Icon")));
        showValidatorAction = new ShowValidator(this);
        arrangeWindowsCPIAction = new ArrangeWindows(this, Translator.getString("Menu.Window.ArrangeCPIWindows.Text"),
                ArrangeWindowsType.CPI,
                KeyStroke.getKeyStroke(KeyEvent.VK_1, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()),
                Utilities.getSVGIcon(Translator.getString("Menu.Window.ArrangeCPIWindows.Icon")));
        arrangeWindowsCIAction = new ArrangeWindows(this, Translator.getString("Menu.Window.ArrangeCIWindows.Text"),
                ArrangeWindowsType.CI,
                KeyStroke.getKeyStroke(KeyEvent.VK_2, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()),
                Utilities.getSVGIcon(Translator.getString("Menu.Window.ArrangeCIWindows.Icon")));
        arrangeWindowsPIAction = new ArrangeWindows(this, Translator.getString("Menu.Window.ArrangePIWindows.Text"),
                ArrangeWindowsType.PI,
                KeyStroke.getKeyStroke(KeyEvent.VK_3, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()),
                Utilities.getSVGIcon(Translator.getString("Menu.Window.ArrangePIWindows.Icon")));
        showAttrDialogAction = new ShowAttrDialog(this);
        showNSTableDialogAction = new ShowNSTableDialog(this);
        showRDFSourceCodeViewer = new ShowRDFSourceCodeViewer(this,
                Translator.getString("RDFSourceCodeViewer.Title"));
        findResAction = new FindResAction(null, gmanager);
        showSPARQLQueryDialogAction = new ShowSPARQLQueryDialog(this,
                Translator.getString("SPARQLQueryDialog.Title"));
        showProjectInfoAction = new ShowProjectInfoDialog(this);
        showLogConsoleAciton = new ShowLogConsole(this);
        showHistoryManagerAciton = new ShowHistoryManager(this);
        showOptionDialogAction = new ShowOptionDialog(this);
        showVersionInfoAction = new ShowVersionInfoAction(this);
        showManualAction = new ShowManualAction(this);
    }

    private JToolBar createToolBar() {
        JToolBar toolbar = new JToolBar();
        toolbar.setFloatable(false);
        toolbar.add(newProjectAction);
        toolbar.add(openFileAction);
        toolbar.add(saveFileAction);
        toolbar.add(saveFileAsAction);
        toolbar.addSeparator();
        toolbar.add(showAttrDialogAction);
        toolbar.add(showNSTableDialogAction);
        toolbar.addSeparator();
        toolbar.add(findResAction);
        toolbar.addSeparator();
        toolbar.add(showRDFSourceCodeViewer);
        toolbar.add(showHistoryManagerAciton);
        toolbar.add(showValidatorAction);
        toolbar.add(showProjectInfoAction);
        toolbar.add(showOptionDialogAction);

        return toolbar;
    }

    public void showInstanceEditorOverview() {
        OverviewDialog result = instanceEditorOverviewRef.get();
        if (result == null) {
            InstanceEditor editor = getProjectPanel().getInstanceEditor();
            result = new OverviewDialog(this, OverviewDialog.INSTANCE_EDITOR_OVERVIEW, editor);
            result.setIconImage(OverviewDialog.INSTANCE_EDITOR_ICON.getImage());
            instanceEditorOverviewRef = new WeakReference<>(result);
        } else {
            result.setEditor(getProjectPanel().getInstanceEditor());
        }
        result.setVisible(true);
    }

    public void showClassEditorOverview() {
        OverviewDialog result = classEditorOverviewRef.get();
        if (result == null) {
            ClassEditor editor = getProjectPanel().getClassEditor();
            result = new OverviewDialog(this, OverviewDialog.CLASS_EDITOR_OVERVIEW, editor);
            result.setIconImage(OverviewDialog.CLASS_EDITOR_ICON.getImage());
            classEditorOverviewRef = new WeakReference<>(result);
        } else {
            result.setEditor(getProjectPanel().getClassEditor());
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
            PropertyEditor editor = getProjectPanel().getPropertyEditor();
            result = new OverviewDialog(this, OverviewDialog.PROPERTY_EDITOR_OVERVIEW, editor);
            result.setIconImage(OverviewDialog.PROPERTY_EDITOR_ICON.getImage());
            propertyEditorOverviewRef = new WeakReference<>(result);
        } else {
            result.setEditor(getProjectPanel().getPropertyEditor());
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
        return historyManager;
    }

    public SPARQLQueryDialog getSparqlQueryDialog() {
        return sparqlQueryDialog;
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
        JMenu saveGraphAsImageMenu = new JMenu(Translator.getString("Menu.File.SaveGraphAsImageFile.Text"));
        saveGraphAsImageMenu.add(saveInstanceGraphAsImageFileAction);
        saveGraphAsImageMenu.add(saveClassGraphAsImageFileAction);
        saveGraphAsImageMenu.add(savePropertyGraphAsImageFileAction);
        menu.add(saveGraphAsImageMenu);
        menu.addSeparator();
        menu.add(quitAction);

        return menu;
    }

    private void initOptions() {
        GraphLayoutUtilities.LAYOUT_TYPE = userPrefs.get(PrefConstants.LAYOUT_TYPE, GraphLayoutUtilities.LAYOUT_TYPE);
        GraphLayoutUtilities.RDF_LAYOUT_DIRECTION = userPrefs.get(PrefConstants.INSTANCE_GRAPH_LAYOUT_DIRECTION,
                GraphLayoutUtilities.RDF_LAYOUT_DIRECTION);
        GraphLayoutUtilities.RDF_LAYOUT_DIRECTION = changeDirectionWithLanguage(
                GraphLayoutUtilities.RDF_LAYOUT_DIRECTION, GraphLayoutUtilities.RDF_LAYOUT_DIRECTION);
        GraphLayoutUtilities.CLASS_LAYOUT_DIRECTION = userPrefs.get(PrefConstants.CLASS_GRAPH_LAYOUT_DIRECTION,
                GraphLayoutUtilities.CLASS_LAYOUT_DIRECTION);
        GraphLayoutUtilities.CLASS_LAYOUT_DIRECTION = changeDirectionWithLanguage(
                GraphLayoutUtilities.CLASS_LAYOUT_DIRECTION, GraphLayoutUtilities.CLASS_LAYOUT_DIRECTION);
        GraphLayoutUtilities.PROPERTY_LAYOUT_DIRECTION = userPrefs.get(PrefConstants.PROPERTY_GRAPH_LAYOUT_DIRECTION,
                GraphLayoutUtilities.PROPERTY_LAYOUT_DIRECTION);
        GraphLayoutUtilities.PROPERTY_LAYOUT_DIRECTION = changeDirectionWithLanguage(
                GraphLayoutUtilities.PROPERTY_LAYOUT_DIRECTION, GraphLayoutUtilities.PROPERTY_LAYOUT_DIRECTION);
        GraphLayoutUtilities.RDF_VERTICAL_SPACE = Integer.parseInt(userPrefs.get(PrefConstants.INSTANCE_NODE_VERTICAL_SPACE,
                Integer.toString(GraphLayoutUtilities.VERTICAL_SPACE)));
        GraphLayoutUtilities.RDF_HORIZONTAL_SPACE = Integer.parseInt(userPrefs.get(PrefConstants.INSTANCE_NODE_HORIZONTAL_SPACE,
                Integer.toString(GraphLayoutUtilities.HORIZONTAL_SPACE)));
        GraphLayoutUtilities.CLASS_VERTICAL_SPACE = Integer.parseInt(userPrefs.get(PrefConstants.CLASS_NODE_VERTICAL_SPACE,
                Integer.toString(GraphLayoutUtilities.VERTICAL_SPACE)));
        GraphLayoutUtilities.CLASS_HORIZONTAL_SPACE = Integer.parseInt(userPrefs.get(
                PrefConstants.CLASS_NODE_HORIZONTAL_SPACE, Integer.toString(GraphLayoutUtilities.HORIZONTAL_SPACE)));
        GraphLayoutUtilities.PROPERTY_VERTICAL_SPACE = Integer.parseInt(userPrefs.get(
                PrefConstants.PROPERTY_NODE_VERTICAL_SPACE, Integer.toString(GraphLayoutUtilities.VERTICAL_SPACE)));
        GraphLayoutUtilities.PROPERTY_HORIZONTAL_SPACE = Integer.parseInt(userPrefs.get(
                PrefConstants.PROPERTY_NODE_HORIZONTAL_SPACE, Integer.toString(GraphLayoutUtilities.HORIZONTAL_SPACE)));


        InstanceCell.foregroundColor = new Color(userPrefs.getInt(PrefConstants.InstanceNodeForegroundColor, InstanceCell.DEFAULT_FG_COLOR.getRGB()));
        InstancePropertyCell.foregroundColor = new Color(userPrefs.getInt(PrefConstants.InstancePropertyForegroundColor, InstancePropertyCell.DEFAULT_FG_COLOR.getRGB()));
        LiteralCell.foregroundColor = new Color(userPrefs.getInt(PrefConstants.LiteralNodeForegroundColor, LiteralCell.DEFAULT_FG_COLOR.getRGB()));
        OntClassCell.foregroundColor = new Color(userPrefs.getInt(PrefConstants.ClassNodeForegroundColor, OntClassCell.DEFAULT_FG_COLOR.getRGB()));
        OntPropertyCell.foregroundColor = new Color(userPrefs.getInt(PrefConstants.PropertyNodeForegroundColor, OntPropertyCell.DEFAULT_FG_COLOR.getRGB()));

        InstanceCell.backgroundColor = new Color(userPrefs.getInt(PrefConstants.InstanceNodeBackgroundColor, InstanceCell.DEFAULT_BG_COLOR.getRGB()));
        LiteralCell.backgroundColor = new Color(userPrefs.getInt(PrefConstants.LiteralNodeBackgroundColor, LiteralCell.DEFAULT_BG_COLOR.getRGB()));
        OntClassCell.backgroundColor = new Color(userPrefs.getInt(PrefConstants.ClassNodeBackgroundColor, OntClassCell.DEFAULT_BG_COLOR.getRGB()));
        OntPropertyCell.backgroundColor = new Color(userPrefs.getInt(PrefConstants.PropertyNodeBackgroundColor, OntPropertyCell.DEFAULT_BG_COLOR.getRGB()));

        InstanceCell.borderColor = new Color(userPrefs.getInt(PrefConstants.InstanceNodeBorderColor, InstanceCell.DEFAULT_BORDER_COLOR.getRGB()));
        InstancePropertyCell.borderColor = new Color(userPrefs.getInt(PrefConstants.InstancePropertyBorderColor, InstancePropertyCell.DEFAULT_BORDER_COLOR.getRGB()));
        LiteralCell.borderColor = new Color(userPrefs.getInt(PrefConstants.LiteralNodeBorderColor, LiteralCell.DEFAULT_BORDER_COLOR.getRGB()));
        OntClassCell.borderColor = new Color(userPrefs.getInt(PrefConstants.ClassNodeBorderColor, OntClassCell.DEFAULT_BORDER_COLOR.getRGB()));
        OntPropertyCell.borderColor = new Color(userPrefs.getInt(PrefConstants.PropertyNodeBorderColor, OntPropertyCell.DEFAULT_BORDER_COLOR.getRGB()));

        InstanceCell.selectedBackgroundColor = new Color(userPrefs.getInt(PrefConstants.InstanceNodeSelectedBackgroundColor, InstanceCell.DEFAULT_SELECTED_BACKGROUND_COLOR.getRGB()));
        LiteralCell.selectedBackgroundColor = new Color(userPrefs.getInt(PrefConstants.LiteralNodeSelectedBackgroundColor, LiteralCell.DEFAULT_SELECTED_BACKGROUND_COLOR.getRGB()));
        OntClassCell.selectedBackgroundColor = new Color(userPrefs.getInt(PrefConstants.ClassNodeSelectedBackgroundColor, OntClassCell.DEFAULT_SELECTED_BACKGROUND_COLOR.getRGB()));
        OntPropertyCell.selectedBackgroundColor = new Color(userPrefs.getInt(PrefConstants.PropertyNodeSelectedBackgroundColor, OntPropertyCell.DEFAULT_SELECTED_BACKGROUND_COLOR.getRGB()));

        InstanceCell.selectedBorderColor = new Color(userPrefs.getInt(PrefConstants.InstanceNodeSelectedBorderColor, InstanceCell.DEFAULT_SELECTED_BORDER_COLOR.getRGB()));
        InstancePropertyCell.selectedBorderColor = new Color(userPrefs.getInt(PrefConstants.InstancePropertySelectedBorderColor, InstancePropertyCell.DEFAULT_SELECTED_BORDER_COLOR.getRGB()));
        LiteralCell.selectedBorderColor = new Color(userPrefs.getInt(PrefConstants.LiteralNodeSelectedBorderColor, LiteralCell.DEFAULT_SELECTED_BORDER_COLOR.getRGB()));
        OntClassCell.selectedBorderColor = new Color(userPrefs.getInt(PrefConstants.ClassNodeSelectedBorderColor, OntClassCell.DEFAULT_SELECTED_BORDER_COLOR.getRGB()));
        OntPropertyCell.selectedBorderColor = new Color(userPrefs.getInt(PrefConstants.PropertyNodeSelectedBorderColor, OntPropertyCell.DEFAULT_SELECTED_BORDER_COLOR.getRGB()));

        Editor.backgroundColor = new Color(userPrefs.getInt(PrefConstants.EditorBackgroundColor, Editor.DEFAUlT_BACKGROUND_COLOR.getRGB()));
        GraphUtilities.isBlackAndWhite = userPrefs.getBoolean(PrefConstants.BlackAndWhite, false);

        GraphUtilities.resetEditorBackgroudColor(gmanager);

        MR3CellMaker.CELL_WIDTH = Integer.parseInt(userPrefs.get(PrefConstants.NODE_WIDTH, Integer.toString(MR3CellMaker.CELL_WIDTH)));
        MR3CellMaker.CELL_HEIGHT = Integer.parseInt(userPrefs.get(PrefConstants.NODE_HEIGHT, Integer.toString(MR3CellMaker.CELL_HEIGHT)));

        setSize(userPrefs.getInt(PrefConstants.WindowWidth, MAIN_FRAME_WIDTH),
                userPrefs.getInt(PrefConstants.WindowHeight, MAIN_FRAME_HEIGHT));
        setLocation(userPrefs.getInt(PrefConstants.WindowPositionX, 50), userPrefs.getInt(PrefConstants.WindowPositionY, 50));

        String workDirPath = userPrefs.get(PrefConstants.WorkDirectory, System.getProperty("user.dir"));
        HistoryManager.initLogger(workDirPath + File.separator + HistoryManager.DEFAULT_LOG_FILE_NAME);

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

    public static MR3ProjectPanel getProjectPanel() {
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
        showTypeCellBox = new JCheckBoxMenuItem(Translator.getString("Menu.View.InstanceType.Text"), true);
        gmanager.setIsShowTypeCell(true);
        showTypeCellBox.addActionListener(new ShowTypeCellAction());
        menu.add(showTypeCellBox);

        showRDFPropertyLabelBox = new JCheckBoxMenuItem(Translator.getString("Menu.View.InstancePropertyLabel.Text"),
                true);
        showRDFPropertyLabelBox.addActionListener(new ShowRDFPropertyLabelAction());
        menu.add(showRDFPropertyLabelBox);

        showToolTips = new JCheckBoxMenuItem(Translator.getString("Menu.View.ToolTips.Text"), true);
        showToolTips.addActionListener(new ShowToolTipsAction());
        ToolTipManager.sharedInstance().setEnabled(true);
        menu.add(showToolTips);
        menu.addSeparator();

        menu.add(new GraphLayoutAction(gmanager, GraphType.Instance, GraphLayoutUtilities.LEFT_TO_RIGHT));
        menu.add(new GraphLayoutAction(gmanager, GraphType.Class, GraphLayoutUtilities.LEFT_TO_RIGHT));
        menu.add(new GraphLayoutAction(gmanager, GraphType.Class, GraphLayoutUtilities.UP_TO_DOWN));
        menu.add(new GraphLayoutAction(gmanager, GraphType.Property, GraphLayoutUtilities.LEFT_TO_RIGHT));
        menu.add(new GraphLayoutAction(gmanager, GraphType.Property, GraphLayoutUtilities.UP_TO_DOWN));

        return menu;
    }

    private JMenu getWindowMenu() {
        JMenu menu = new JMenu(Translator.getString("Menu.Window.Text") + "(W)");
        menu.setMnemonic('w');
        menu.add(new ShowOverview(this, ShowOverview.INSTANCE_EDITOR_OVERVIEW, ShowOverview.INSTANCE_EDITOR_OVERVIEW_ICON));
        menu.add(new ShowOverview(this, ShowOverview.CLASS_EDITOR_OVERVIEW, ShowOverview.CLASS_EDITOR_OVERVIEW_ICON));
        menu.add(new ShowOverview(this, ShowOverview.PROPERTY_EDITOR_OVERVIEW, ShowOverview.PROPERTY_EDITOR_OVERVIEW_ICON));
        menu.addSeparator();
        menu.add(showAttrDialogAction);
        menu.add(showNSTableDialogAction);
        menu.addSeparator();
        menu.add(arrangeWindowsCPIAction);
        menu.add(arrangeWindowsCIAction);
        menu.add(arrangeWindowsPIAction);

        return menu;
    }

    private JMenu getToolsMenu() {
        JMenu menu = new JMenu(Translator.getString("Menu.Tools.Text") + "(T)");
        menu.setMnemonic('t');
        menu.add(showRDFSourceCodeViewer);
        menu.add(findResAction);
        menu.add(showSPARQLQueryDialogAction);
        menu.add(showValidatorAction);
        menu.add(showProjectInfoAction);
        menu.add(showHistoryManagerAciton);
        menu.add(showLogConsoleAciton);
        menu.addSeparator();
        menu.add(showOptionDialogAction);

        return menu;
    }

    private JMenu getHelpMenu() {
        JMenu menu = new JMenu(Translator.getString("Menu.Help.Text") + "(H)");
        menu.setMnemonic('h');
        menu.add(showVersionInfoAction);
        menu.add(showManualAction);
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
        MR3.getProjectPanel().setProjectFile(newFile);
        HistoryManager.saveHistory(HistoryType.NEW_PROJECT);
        mr3ProjectPanel.resetEditors();
        mr3ProjectPanel.arrangeWindowsCPI();
    }

    public MR3Reader getMR3Reader() {
        return mr3Reader;
    }

    public MR3Writer getMR3Writer() {
        return mr3Writer;
    }

    public RDFGraph getRDFGraph() {
        return gmanager.getInstanceGraph();
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
        FlatLightLaf.setup();
        JenaSystem.init();
        userPrefs = Preferences.userNodeForPackage(cls);
        Translator.loadResourceBundle(userPrefs);
        UIManager.put("TitledBorder.border", new LineBorder(new Color(200, 200, 200), 1));
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
        System.setProperty("javax.accessibility.assistive_technologies", "");
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