package jp.ac.shizuoka.cs.panda.mmm.mr3;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.util.prefs.*;

import javax.swing.*;
import javax.swing.text.*;

import jp.ac.shizuoka.cs.panda.mmm.mr3.actions.*;
import jp.ac.shizuoka.cs.panda.mmm.mr3.data.*;
import jp.ac.shizuoka.cs.panda.mmm.mr3.editor.*;
import jp.ac.shizuoka.cs.panda.mmm.mr3.io.*;
import jp.ac.shizuoka.cs.panda.mmm.mr3.jgraph.*;
import jp.ac.shizuoka.cs.panda.mmm.mr3.ui.*;
import jp.ac.shizuoka.cs.panda.mmm.mr3.util.*;
import jp.ac.shizuoka.cs.panda.mmm.mr3.util.Utilities;

import com.hp.hpl.jena.rdf.model.*;

/**
  *   {MR} ^3 Meta-Model Management founded on RDF-baed Revision Reflection  
  */
public class MR3 extends JFrame {

	private File currentProject;
	private JDesktopPane desktop;
	private Preferences userPrefs; // ユーザの設定を保存(Windowサイズなど）
	private static ResourceBundle resources;
	private static final int MAIN_FRAME_HEIGHT = 600;
	private static final int MAIN_FRAME_WIDTH = 800;
	private static final int FRAME_HEIGHT = 400;
	private static final int FRAME_WIDTH = 600;
	private static final Integer DEMO_FRAME_LAYER = new Integer(0);

	private GraphManager gmanager;
	private RDFEditor rdfEditor;
	private OverviewDialog rdfEditorOverview;
	private ClassEditor classEditor;
	private OverviewDialog classEditorOverview;
	private PropertyEditor propertyEditor;
	private OverviewDialog propertyEditorOverview;

	//	private RDFSTreePanel classTreePanel;
	//	private RDFSTreePanel propTreePanel;

	private MR3Reader mr3Reader;
	private MR3Writer mr3Writer;

	private NameSpaceTableDialog nsTableDialog;
	private FindResourceDialog findResDialog;
	private AttributeDialog attrDialog;
	private MR3OverviewPanel overviewPanel;
	private PrefDialog prefDialog;
	private MR3LogConsole logger;

	private JRadioButton uriView;
	private JRadioButton idView;
	private JRadioButton labelView;

	private JCheckBoxMenuItem showTypeCellBox;
	private JCheckBoxMenuItem showToolTips;
	private JCheckBoxMenuItem isGroup;
	private JCheckBoxMenuItem selectAbstractLevelMode;

	private SourceDialog srcDialog;
	private JCheckBoxMenuItem rdfEditorView;
	private JCheckBoxMenuItem classEditorView;
	private JCheckBoxMenuItem propertyEditorView;
	private JInternalFrame[] iFrames = new JInternalFrame[3];

	private RDFResourceInfoMap resInfoMap = RDFResourceInfoMap.getInstance();
	private RDFLiteralInfoMap litInfoMap = RDFLiteralInfoMap.getInstance();
	private RDFSInfoMap rdfsInfoMap = RDFSInfoMap.getInstance();

	private static final Color DESKTOP_BACK_COLOR = new Color(245, 245, 245);

	MR3() {
		userPrefs = Preferences.userNodeForPackage(this.getClass());
		Translator.loadResourceBundle(userPrefs);
		MR3Constants.initConstants();

		setSize(userPrefs.getInt(PrefConstants.WindowWidth, MAIN_FRAME_WIDTH), userPrefs.getInt(PrefConstants.WindowHeight, MAIN_FRAME_HEIGHT));
		setLocation(userPrefs.getInt(PrefConstants.WindowPositionX, 50), userPrefs.getInt(PrefConstants.WindowPositionY, 50));

		logger = new MR3LogConsole(Translator.getString("LogConsole.Title"), null);
		attrDialog = new AttributeDialog();
		gmanager = new GraphManager(attrDialog, userPrefs);
		initAction();
		getContentPane().add(createToolBar(), BorderLayout.NORTH);
		createDesktop();
		gmanager.setDesktop(desktop);
		gmanager.setRoot(this);

		rdfEditor = new RDFEditor(nsTableDialog, findResDialog, gmanager);
		rdfEditorOverview = new OverviewDialog(OverviewDialog.RDF_EDITOR_OVERVIEW, rdfEditor.getGraph(), rdfEditor.getJViewport());
		rdfEditorOverview.setFrameIcon(OverviewDialog.RDF_EDITOR_ICON);
		desktop.add(rdfEditorOverview, JLayeredPane.MODAL_LAYER);
		classEditor = new ClassEditor(nsTableDialog, findResDialog, gmanager);
		classEditorOverview = new OverviewDialog(OverviewDialog.CLASS_EDITOR_OVERVIEW, classEditor.getGraph(), classEditor.getJViewport());
		classEditorOverview.setFrameIcon(OverviewDialog.CLASS_EDITOR_ICON);
		desktop.add(classEditorOverview, JLayeredPane.MODAL_LAYER);
		propertyEditor = new PropertyEditor(nsTableDialog, findResDialog, gmanager);
		propertyEditorOverview =
			new OverviewDialog(OverviewDialog.PROPERTY_EDITOR_OVERVIEW, propertyEditor.getGraph(), propertyEditor.getJViewport());
		propertyEditorOverview.setFrameIcon(OverviewDialog.PROPERTY_EDITOR_ICON);
		desktop.add(propertyEditorOverview, JLayeredPane.MODAL_LAYER);

		mr3Reader = new MR3Reader(gmanager, nsTableDialog);
		mr3Writer = new MR3Writer(gmanager);

		createInternalFrames();

		//		setTreeLayout();
		desktop.setBackground(DESKTOP_BACK_COLOR);
		getContentPane().add(desktop);

		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		addWindowListener(new CloseWindow(this));
		setIconImage(Utilities.getImageIcon("mr3_logo.png").getImage());
		setTitle("MR^3 - " + Translator.getString("Component.File.NewProject.Text"));
		setJMenuBar(createMenuBar());
		initPreferences();
		setVisible(true);
	}

	//	private void setTreeLayout() {
	//		classTreePanel = new RDFSTreePanel(gmanager, rdfsInfoMap.getClassTreeModel(), new ClassTreeCellRenderer());
	//		propTreePanel = new RDFSTreePanel(gmanager, rdfsInfoMap.getPropTreeModel(), new PropertyTreeCellRenderer());
	//		JTabbedPane treeTab = new JTabbedPane();
	//		treeTab.add("Class", classTreePanel);
	//		treeTab.add("Property", propTreePanel);
	//		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, treeTab, desktop);
	//		splitPane.setOneTouchExpandable(true);
	//		getContentPane().add(splitPane);		
	//	}

	private AbstractAction newProjectAction;
	private AbstractAction openProjectAction;
	private AbstractAction saveProjectAction;
	private AbstractAction saveProjectAsAction;
	private AbstractAction toFrontRDFEditorAction;
	private AbstractAction toFrontClassEditorAction;
	private AbstractAction toFrontPropertyEditorAction;
	private AbstractAction showAttrDialogAction;
	private AbstractAction showNSTableDialogAction;
	private AbstractAction showSrcDialogAction;

	private void initAction() {
		newProjectAction = new NewProject(this);
		openProjectAction = new OpenProject(this);
		saveProjectAction = new SaveProject(this, SaveProject.SAVE_PROJECT, SaveProject.SAVE_PROJECT_ICON);
		saveProjectAsAction = new SaveProject(this, SaveProject.SAVE_AS_PROJECT, SaveProject.SAVE_AS_PROJECT_ICON);
		toFrontRDFEditorAction = new EditorSelect(this, EditorSelect.RDF_EDITOR, EditorSelect.RDF_EDITOR_ICON);
		toFrontClassEditorAction = new EditorSelect(this, EditorSelect.CLASS_EDITOR, EditorSelect.CLASS_EDITOR_ICON);
		toFrontPropertyEditorAction = new EditorSelect(this, EditorSelect.PROPERTY_EDITOR, EditorSelect.PROPERTY_EDITOR_ICON);
		showAttrDialogAction = new ShowAttrDialog(this);
		showNSTableDialogAction = new ShowNSTableDialog(this);
		showSrcDialogAction = new ShowSrcDialog(this);
	}

	private JToolBar createToolBar() {
		JToolBar toolbar = new JToolBar();
		toolbar.setFloatable(false);
		toolbar.add(newProjectAction);
		toolbar.add(openProjectAction);
		toolbar.add(saveProjectAction);
		toolbar.add(saveProjectAsAction);
		toolbar.addSeparator();
		toolbar.add(toFrontRDFEditorAction);
		toolbar.add(toFrontClassEditorAction);
		toolbar.add(toFrontPropertyEditorAction);
		toolbar.addSeparator();
		toolbar.add(showAttrDialogAction);
		toolbar.add(showNSTableDialogAction);
		toolbar.add(showSrcDialogAction);

		return toolbar;
	}

	private void createDesktop() {
		desktop = new JDesktopPane();
		desktop.add(attrDialog, JLayeredPane.MODAL_LAYER);
		findResDialog = new FindResourceDialog(gmanager);
		desktop.add(findResDialog, JLayeredPane.MODAL_LAYER);
		nsTableDialog = new NameSpaceTableDialog(gmanager);
		desktop.add(nsTableDialog, JLayeredPane.MODAL_LAYER);
		desktop.add(gmanager.getRmDialog(), JLayeredPane.MODAL_LAYER);
		prefDialog = new PrefDialog(gmanager, userPrefs);
		prefDialog.setVisible(false);
		desktop.add(prefDialog, JLayeredPane.MODAL_LAYER);
	}

	private void createInternalFrames() {
		iFrames[0] = rdfEditor;
		iFrames[1] = classEditor;
		iFrames[2] = propertyEditor;
		srcDialog = new SourceDialog();

		desktop.add(iFrames[0], Cursor.DEFAULT_CURSOR);
		desktop.add(iFrames[1], Cursor.DEFAULT_CURSOR);
		desktop.add(iFrames[2], Cursor.DEFAULT_CURSOR);
		desktop.add(srcDialog, Cursor.DEFAULT_CURSOR);

		rdfEditor.setInternalFrames(iFrames);
		classEditor.setInternalFrames(iFrames);
		propertyEditor.setInternalFrames(iFrames);
	}

	private JMenuBar createMenuBar() {
		JMenuBar mb = new JMenuBar();
		mb.add(getFileMenu());
		mb.add(getEditMenu());
		mb.add(getSelectMenu());
		mb.add(getViewMenu());
		mb.add(getWindowMenu());
		mb.add(getConvertMenu());
		mb.add(getHelpMenu());
		return mb;
	}

	private JMenu getEditMenu() {
		JMenu menu = new JMenu(Translator.getString("Component.Edit.Text"));
		menu.add(new FindResAction(getRDFGraph(), findResDialog));
		menu.addSeparator();
		//		selectAbstractLevelMode = new JCheckBoxMenuItem("Change Abstract Level", false);
		//		selectAbstractLevelMode.addActionListener(new SelectAbstractLevelAction());
		//		menu.add(selectAbstractLevelMode);
		menu.add(new PreferenceAction());

		return menu;
	}

	private JMenu getSelectMenu() {
		JMenu selectMenu = new JMenu(Translator.getString("Component.Select.Text"));
		selectMenu.add(new SelectNodes(getRDFGraph(), Translator.getString("Component.Select.RDF.Text")));
		selectMenu.add(new SelectNodes(getClassGraph(), Translator.getString("Component.Select.Class.Text")));
		selectMenu.add(new SelectNodes(getPropertyGraph(), Translator.getString("Component.Select.Property.Text")));

		return selectMenu;
	}

	/** 　デバッグ用メソッド */
	public void printModel(Model model) {
		try {
			model.write(new PrintWriter(System.out));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public JFrame getLogConsole() {
		return logger;
	}

	public Editor getRDFEditor() {
		return rdfEditor;
	}

	public Editor getClassEditor() {
		return classEditor;
	}

	public Editor getPropertyEditor() {
		return propertyEditor;
	}

	public JInternalFrame[] getInternalFrames() {
		return iFrames;
	}

	public GraphManager getGraphManager() {
		return gmanager;
	}

	public NameSpaceTableDialog getNSTableDialog() {
		return nsTableDialog;
	}

	public SourceDialog getSrcDialog() {
		return srcDialog;
	}

	class PreferenceAction extends AbstractAction {
		PreferenceAction() {
			super(Translator.getString("Component.Edit.Preference.Text"));
		}

		public void actionPerformed(ActionEvent e) {
			prefDialog.initPrefixBox();
			prefDialog.setVisible(true);
		}
	}

	private JMenu getFileMenu() {
		JMenu menu = new JMenu(Translator.getString("Component.File.Text"));
		menu.add(newProjectAction);
		menu.add(openProjectAction);
		menu.add(saveProjectAction);
		menu.add(saveProjectAsAction);
		menu.addSeparator();

		JMenu importMenu = new JMenu(Translator.getString("Component.File.Import.Text"));
		importMenu.setIcon(Utilities.getImageIcon(Translator.getString("Component.File.Import.Icon")));
		menu.add(importMenu);

		JMenu replaceMenu = new JMenu(Translator.getString("Component.File.Import.Replace.Text"));
		importMenu.add(replaceMenu);
		JMenu replaceRDFMenu = new JMenu(Translator.getString("Component.File.Import.Replace.RDF.Text"));
		replaceMenu.add(replaceRDFMenu);
		replaceRDFMenu.add(new ReplaceRDF(this, ReplaceRDF.REPLACE_RDF_FILE));
		replaceRDFMenu.add(new ReplaceRDF(this, ReplaceRDF.REPLACE_RDF_URI));
		replaceRDFMenu.addSeparator();
		replaceRDFMenu.add(new ReplaceRDF(this, ReplaceRDF.REPLACE_N_TRIPLE_FILE));
		replaceRDFMenu.add(new ReplaceRDF(this, ReplaceRDF.REPLACE_N_TRIPLE_URI));
		JMenu replaceRDFSMenu = new JMenu(Translator.getString("Component.File.Import.Replace.RDFS.Text"));
		replaceMenu.add(replaceRDFSMenu);
		replaceRDFSMenu.add(new ReplaceRDFS(this, ReplaceRDFS.REPLACE_RDFS_FILE));
		replaceRDFSMenu.add(new ReplaceRDFS(this, ReplaceRDFS.REPLACE_RDFS_URI));
		replaceRDFSMenu.addSeparator();
		replaceRDFSMenu.add(new ReplaceRDFS(this, ReplaceRDFS.REPLACE_N_TRIPLE_FILE));
		replaceRDFSMenu.add(new ReplaceRDFS(this, ReplaceRDFS.REPLACE_N_TRIPLE_URI));

		JMenu mergeMenu = new JMenu(Translator.getString("Component.File.Import.Merge.Text"));
		mergeMenu.add(new MergeRDFs(this, MergeRDFs.MERGE_RDFS_FILE));
		mergeMenu.add(new MergeRDFs(this, MergeRDFs.MERGE_RDFS_URI));
		mergeMenu.addSeparator();
		mergeMenu.add(new MergeRDFs(this, MergeRDFs.MERGE_N_TRIPLE_FILE));
		mergeMenu.add(new MergeRDFs(this, MergeRDFs.MERGE_N_TRIPLE_URI));
		importMenu.add(mergeMenu);

		importMenu.add(new ImportJavaObject(this));

		JMenu exportMenu = new JMenu(Translator.getString("Component.File.Export.Text"));
		exportMenu.setIcon(Utilities.getImageIcon(Translator.getString("Component.File.Export.Icon")));
		menu.add(exportMenu);

		JMenu rdfMenu = new JMenu(Translator.getString("Component.File.Export.RDF/XML.Text"));
		exportMenu.add(rdfMenu);
		rdfMenu.add(new ExportRDF(this, ExportRDF.RDF_XML));
		rdfMenu.add(new ExportRDFS(this, ExportRDFS.RDFS_XML));
		rdfMenu.addSeparator();
		rdfMenu.add(new ExportRDF(this, ExportRDF.SelectedRDF_XML));
		rdfMenu.add(new ExportRDFS(this, ExportRDFS.SelectedRDFS_XML));

		JMenu nTripleMenu = new JMenu("N-Triple");
		exportMenu.add(nTripleMenu);
		nTripleMenu.add(new ExportRDF(this, ExportRDF.RDF_NTriple));
		nTripleMenu.add(new ExportRDFS(this, ExportRDFS.RDFS_NTriple));
		nTripleMenu.addSeparator();
		nTripleMenu.add(new ExportRDF(this, ExportRDF.SelectedRDF_NTriple));
		nTripleMenu.add(new ExportRDFS(this, ExportRDFS.SelectedRDFS_NTriple));

		JMenu imgMenu = new JMenu("Image");
		exportMenu.add(imgMenu);
		imgMenu.add(new FileExportImg(this, GraphType.RDF, "png", Translator.getString("Component.File.Export.Image.RDFGraph.Text")));
		imgMenu.add(new FileExportImg(this, GraphType.CLASS, "png", Translator.getString("Component.File.Export.Image.ClassGraph.Text")));
		imgMenu.add(new FileExportImg(this, GraphType.PROPERTY, "png", Translator.getString("Component.File.Export.Image.PropertyGraph.Text")));

		exportMenu.add(new ExportJavaObject(this));

		menu.addSeparator();
		menu.add(getPluginMenus());
		menu.addSeparator();
		menu.add(new ExitAction(this));

		return menu;
	}

	private void initPreferences() {
		int width = 792; // desktop.getWidth() -> 0
		int height = 518; // desktop.getHeight()->0

		int editorPositionX = userPrefs.getInt(PrefConstants.RDFEditorPositionX, 0);
		int editorPositionY = userPrefs.getInt(PrefConstants.RDFEditorPositionY, height / 2);
		int editorWidth = userPrefs.getInt(PrefConstants.RDFEditorWidth, width);
		int editorHeight = userPrefs.getInt(PrefConstants.RDFEditorHeight, height / 2);
		iFrames[0].setBounds(new Rectangle(editorPositionX, editorPositionY, editorWidth, editorHeight)); // RDF

		editorPositionX = userPrefs.getInt(PrefConstants.ClassEditorPositionX, 0);
		editorPositionY = userPrefs.getInt(PrefConstants.ClassEditorPositionY, 0);
		editorWidth = userPrefs.getInt(PrefConstants.ClassEditorWidth, width / 2);
		editorHeight = userPrefs.getInt(PrefConstants.ClassEditorHeight, height / 2);
		iFrames[1].setBounds(new Rectangle(editorPositionX, editorPositionY, editorWidth, editorHeight)); // Class

		editorPositionX = userPrefs.getInt(PrefConstants.PropertyEditorPositionX, width / 2);
		editorPositionY = userPrefs.getInt(PrefConstants.PropertyEditorPositionY, 0);
		editorWidth = userPrefs.getInt(PrefConstants.PropertyEditorWidth, width / 2);
		editorHeight = userPrefs.getInt(PrefConstants.PropertyEditorHeight, height / 2);
		iFrames[2].setBounds(new Rectangle(editorPositionX, editorPositionY, editorWidth, editorHeight)); // Property

		ChangeCellAttributes.rdfResourceColor = new Color(userPrefs.getInt(PrefConstants.RDFResourceColor, Color.pink.getRGB()));
		ChangeCellAttributes.literalColor = new Color(userPrefs.getInt(PrefConstants.LiteralColor, Color.orange.getRGB()));
		ChangeCellAttributes.classColor = new Color(userPrefs.getInt(PrefConstants.ClassColor, Color.green.getRGB()));
		ChangeCellAttributes.propertyColor = new Color(userPrefs.getInt(PrefConstants.PropertyColor, new Color(255, 158, 62).getRGB()));
		ChangeCellAttributes.selectedColor = new Color(userPrefs.getInt(PrefConstants.SelectedColor, new Color(255, 255, 50).getRGB()));

		ChangeCellAttributes.isColor = userPrefs.getBoolean(PrefConstants.Color, true);
		gmanager.setGraphBackground(new Color(userPrefs.getInt(PrefConstants.BackgroundColor, DESKTOP_BACK_COLOR.getRGB())));
	}

	private JMenu getPluginMenus() {
		JMenu menu = new JMenu(Translator.getString("Component.File.Plugins.Text"));
		Map pluginMenuMap = PluginLoader.getPluginMenuMap();
		Set keys = pluginMenuMap.keySet();
		for (Iterator i = keys.iterator(); i.hasNext();) {
			String menuName = (String) i.next();
			menu.add(new ImportPlugin(this, menuName));
		}
		return menu;
	}

	public AttributeDialog getAttrDialog() {
		return attrDialog;
	}

	public void clearMap() {
		resInfoMap.clear();
		litInfoMap.clear();
		rdfsInfoMap.clear();
	}

	public File getCurrentProject() {
		return currentProject;
	}

	public void setCurrentProject(File file) {
		currentProject = file;
	}

	public Preferences getUserPrefs() {
		return userPrefs;
	}

	private static MR3FileFilter mr3FileFilter = new MR3FileFilter();
	private static RDFsFileFilter rdfsFileFilter = new RDFsFileFilter();
	private static NTripleFileFilter n3FileFilter = new NTripleFileFilter();

	private JMenu getViewMenu() {
		JMenu menu = new JMenu(Translator.getString("Component.View.Text"));
		ChangeCellViewAction changeCellViewAction = new ChangeCellViewAction();
		uriView = new JRadioButton(Translator.getString("Component.View.URI.Text"));
		uriView.setSelected(true);
		gmanager.setCellViewType(CellViewType.URI);
		uriView.addItemListener(changeCellViewAction);
		idView = new JRadioButton(Translator.getString("Component.View.ID.Text"));
		idView.addItemListener(changeCellViewAction);
		labelView = new JRadioButton(Translator.getString("Component.View.Label.Text"));
		labelView.addItemListener(changeCellViewAction);
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
		showToolTips = new JCheckBoxMenuItem(Translator.getString("Component.View.ToolTips.Text"), true);
		showToolTips.addActionListener(new ShowToolTipsAction());
		ToolTipManager.sharedInstance().setEnabled(true);
		menu.add(showToolTips);
		isGroup = new JCheckBoxMenuItem(Translator.getString("Component.View.Group.Text"), true);
		isGroup.addActionListener(new IsGroupAction());
		menu.add(isGroup);
		JMenu lookAndFeel = new JMenu(Translator.getString("Component.View.LookAndFeel.Text"));
		menu.add(lookAndFeel);
		lookAndFeel.add(new ChangeLookAndFeelAction(this, ChangeLookAndFeelAction.METAL));
		lookAndFeel.add(new ChangeLookAndFeelAction(this, ChangeLookAndFeelAction.WINDOWS));
		lookAndFeel.add(new ChangeLookAndFeelAction(this, ChangeLookAndFeelAction.MOTIF));

		return menu;
	}

	class IsGroupAction extends AbstractAction {
		public void actionPerformed(ActionEvent e) {
			getRDFGraph().getSelectionModel().setChildrenSelectable(!isGroup.isSelected());
		}
	}

	private JMenu getWindowMenu() {
		JMenu menu = new JMenu(Translator.getString("Component.Window.Text"));
		menu.add(new ShowOverview(this, rdfEditorOverview, ShowOverview.RDF_EDITOR_OVERVIEW));
		menu.add(new ShowOverview(this, classEditorOverview, ShowOverview.CLASS_EDITOR_OVERVIEW));
		menu.add(new ShowOverview(this, propertyEditorOverview, ShowOverview.PROPERTY_EDITOR_OVERVIEW));
		menu.addSeparator();
		menu.add(toFrontRDFEditorAction);
		menu.add(toFrontClassEditorAction);
		menu.add(toFrontPropertyEditorAction);
		menu.add(showAttrDialogAction);
		menu.add(showNSTableDialogAction);
		menu.add(showSrcDialogAction);
		menu.addSeparator();
		menu.add(new ShowLogConsole(this));
		menu.addSeparator();
		menu.add(new DeployWindows(this));

		return menu;
	}

	private JMenu getConvertMenu() {
		JMenu menu = new JMenu(Translator.getString("Component.Convert.Text"));

		JMenu rdfView = new JMenu(Translator.getString("Component.Convert.RDF/XML.Text"));
		menu.add(rdfView);
		rdfView.add(new ConvertRDFDoc(this, ConvertRDFDoc.RDF));
		rdfView.add(new ConvertRDFDoc(this, ConvertRDFDoc.SELECTED_RDF));

		JMenu rdfsView = new JMenu(Translator.getString("Component.Convert.RDFS/XML.Text"));
		menu.add(rdfsView);
		rdfsView.add(new ConvertRDFSDoc(this, ConvertRDFSDoc.RDFS));
		rdfsView.add(new ConvertClassDoc(this, ConvertClassDoc.CLASS));
		rdfsView.add(new ConvertPropertyDoc(this, ConvertPropertyDoc.PROPERTY));
		rdfsView.addSeparator();
		rdfsView.add(new ConvertRDFSDoc(this, ConvertRDFSDoc.SELECTED_RDFS));
		rdfsView.add(new ConvertClassDoc(this, ConvertClassDoc.SELECTED_CLASS));
		rdfsView.add(new ConvertPropertyDoc(this, ConvertPropertyDoc.SELECTED_PROPERTY));

		JMenu nTripleView = new JMenu(Translator.getString("Component.Convert.RDF/N-Triple.Text"));
		menu.add(nTripleView);
		nTripleView.add(new ConvertNTriple(this, ConvertNTriple.RDF));
		nTripleView.add(new ConvertNTriple(this, ConvertNTriple.SELECTED_RDF));

		return menu;
	}

	private JMenu getHelpMenu() {
		JMenu menu = new JMenu(Translator.getString("Component.Help.Text"));
		menu.add(new HelpAbout(this));
		return menu;
	}

	class ShowToolTipsAction extends AbstractAction {
		public void actionPerformed(ActionEvent e) {
			ToolTipManager.sharedInstance().setEnabled(showToolTips.getState());
		}
	}

	class ChangeCellViewAction implements ItemListener {
		public void itemStateChanged(ItemEvent e) {
			if (e.getItemSelectable() == uriView) {
				gmanager.setCellViewType(CellViewType.URI);
			} else if (e.getItemSelectable() == idView) {
				gmanager.setCellViewType(CellViewType.ID);
			} else if (e.getItemSelectable() == labelView) {
				gmanager.setCellViewType(CellViewType.LABEL);
			}
			gmanager.changeCellView();
		}
	}

	class SelectAbstractLevelAction implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			boolean state = selectAbstractLevelMode.getState();
			gmanager.setSelectAbstractLevelMode(state);
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

	public void newProject() {
		nsTableDialog.resetNSTable();
		attrDialog.setNullPanel();
		clearMap();
		gmanager.removeAllCells();
		nsTableDialog.setDefaultNSPrefix();
		setTitle("MR^3 - " + Translator.getString("Component.File.NewProject.Text"));
		setCurrentProject(null);
	}

	public JInternalFrame getSourceFrame() {
		return srcDialog;
	}

	public JDesktopPane getDesktopPane() {
		return gmanager.getDesktop();
	}

	public Model getRDFModel() {
		return mr3Writer.getRDFModel();
	}

	public Model getSelectedRDFModel() {
		return mr3Writer.getSelectedRDFModel();
	}

	public Model getClassModel() {
		return mr3Writer.getClassModel();
	}

	public Model getSelectedClassModel() {
		return mr3Writer.getSelectedClassModel();
	}

	public Model getPropertyModel() {
		return mr3Writer.getPropertyModel();
	}

	public Model getSelectedPropertyModel() {
		return mr3Writer.getSelectedPropertyModel();
	}

	public Model getRDFSModel() {
		return mr3Writer.getRDFSModel();
	}

	public Model getSelectedRDFSModel() {
		return mr3Writer.getSelectedRDFSModel();
	}

	public Model getProjectModel() {
		return mr3Writer.getProjectModel(this);
	}

	public void replaceRDFModel(Model model) {
		mr3Reader.replaceRDF(model);
	}

	public void replaceRDFSModel(Model model) {
		ReplaceRDFSDialog replaceRDFSDialog = new ReplaceRDFSDialog(gmanager, model.union(model));
		if (replaceRDFSDialog.isApply()) {
			mr3Reader.replaceRDFS(model);
			gmanager.applyRDFSTreeLayout();
		}
	}

	public void mergeRDFModel(Model model) {
		mr3Reader.mergeRDF(model);
	}

	public void mergeRDFSModel(Model model) {
		mr3Reader.mergeRDFS(model);
	}

	public void replaceProjectModel(Model model) {
		mr3Reader.replaceProjectModel(model, this);
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

	public JTextComponent getSourceArea() {
		return srcDialog.getSourceArea();
	}

	public static void main(String[] arg) {
		Translator.loadResourceBundle("en");
		System.setProperty("swing.plaf.metal.controlFont", Translator.getString("ControlFont"));
		JWindow splashWindow = new HelpWindow(null, MR3Constants.LOGO);
		try {
			new MR3();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			splashWindow.dispose();
		}
	}
}
