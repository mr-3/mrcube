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

	private JDesktopPane desktop;
	private static final int MAIN_FRAME_HEIGHT = 600;
	private static final int MAIN_FRAME_WIDTH = 800;
	private static final int FRAME_HEIGHT = 400;
	private static final int FRAME_WIDTH = 600;
	private static final Integer DEMO_FRAME_LAYER = new Integer(0);

	private File currentProject;

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

	private GraphManager gmanager;
	private RDFResourceInfoMap resInfoMap = RDFResourceInfoMap.getInstance();
	private RDFLiteralInfoMap litInfoMap = RDFLiteralInfoMap.getInstance();
	private RDFSInfoMap rdfsInfoMap = RDFSInfoMap.getInstance();

	private JCheckBoxMenuItem showTypeCellBox;
	private JCheckBoxMenuItem selectAbstractLevelMode;
	private JCheckBoxMenuItem showToolTips;
	private JCheckBoxMenuItem isGroup;

	private JInternalFrame[] iFrames = new JInternalFrame[3];
	private SourceDialog srcDialog;
	private JCheckBoxMenuItem rdfEditorView;
	private JCheckBoxMenuItem classEditorView;
	private JCheckBoxMenuItem propertyEditorView;

	private JRadioButton uriView;
	private JRadioButton idView;
	private JRadioButton labelView;

	private static final Color DESKTOP_BACK_COLOR = new Color(245, 245, 245);

	private Preferences userPrefs; // ユーザの設定を保存(Windowサイズなど）
	private static ResourceBundle resources;

	MR3() {
		userPrefs = Preferences.userNodeForPackage(this.getClass());

		setSize(userPrefs.getInt(PrefConstants.WindowWidth, MAIN_FRAME_WIDTH), userPrefs.getInt(PrefConstants.WindowHeight, MAIN_FRAME_HEIGHT));
		setLocation(userPrefs.getInt(PrefConstants.WindowPositionX, 50), userPrefs.getInt(PrefConstants.WindowPositionY, 50));
		//		setLookAndFeel();

		logger = new MR3LogConsole("Log Console", null);
		attrDialog = new AttributeDialog();
		gmanager = new GraphManager(attrDialog, userPrefs);
		initAction();
		getContentPane().add(createToolBar(), BorderLayout.NORTH);
		createDesktop();
		gmanager.setDesktop(desktop);
		gmanager.setRoot(this);

		rdfEditor = new RDFEditor(nsTableDialog, findResDialog, gmanager);
		rdfEditorOverview = new OverviewDialog("RDF Editor Overview", rdfEditor.getGraph(), rdfEditor.getJViewport());
		rdfEditorOverview.setFrameIcon(Utilities.getImageIcon("rdfEditorIcon.gif"));
		desktop.add(rdfEditorOverview, JLayeredPane.MODAL_LAYER);
		classEditor = new ClassEditor(nsTableDialog, findResDialog, gmanager);
		classEditorOverview = new OverviewDialog("Class Editor Overview", classEditor.getGraph(), classEditor.getJViewport());
		classEditorOverview.setFrameIcon(Utilities.getImageIcon("classEditorIcon.gif"));
		desktop.add(classEditorOverview, JLayeredPane.MODAL_LAYER);
		propertyEditor = new PropertyEditor(nsTableDialog, findResDialog, gmanager);
		propertyEditorOverview = new OverviewDialog("Property Editor Overview", propertyEditor.getGraph(), propertyEditor.getJViewport());
		propertyEditorOverview.setFrameIcon(Utilities.getImageIcon("propertyEditorIcon.gif"));
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
		setTitle("MR^3 - New Project");
		setJMenuBar(createMenuBar());
		initPreferences();
		setVisible(true);
	}

	private void setTreeLayout() {
		//		classTreePanel = new RDFSTreePanel(gmanager, rdfsInfoMap.getClassTreeModel(), new ClassTreeCellRenderer());
		//		propTreePanel = new RDFSTreePanel(gmanager, rdfsInfoMap.getPropTreeModel(), new PropertyTreeCellRenderer());
		//		JTabbedPane treeTab = new JTabbedPane();
		//		treeTab.add("Class", classTreePanel);
		//		treeTab.add("Property", propTreePanel);
		//		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, treeTab, desktop);
		//		splitPane.setOneTouchExpandable(true);
		//		getContentPane().add(splitPane);		
	}

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
		saveProjectAction = new SaveProject(this, "Save Project", Utilities.getImageIcon("save.gif"));
		saveProjectAsAction = new SaveProject(this, "Save Project As", Utilities.getImageIcon("saveas.gif"));
		toFrontRDFEditorAction = new EditorSelect(this, TO_FRONT_RDF_EDITOR, Utilities.getImageIcon("rdfEditorIcon.gif"));
		toFrontClassEditorAction = new EditorSelect(this, TO_FRONT_CLASS_EDITOR, Utilities.getImageIcon("classEditorIcon.gif"));
		toFrontPropertyEditorAction = new EditorSelect(this, TO_FRONT_PROPERTY_EDITOR, Utilities.getImageIcon("propertyEditorIcon.gif"));
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

	private void setLookAndFeel() {
		try {
			//			UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			SwingUtilities.updateComponentTreeUI(this);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private static final String FILE_MENU = "File";
	private static final String EDIT_MENU = "Edit";
	private static final String SELECT_MENU = "Select";
	private static final String VIEW_MENU = "View";
	private static final String WINDOW_MENU = "Window";
	private static final String CONVERT_MENU = "Convert";
	private static final String HELP_MENU = "Help";

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

	private static final String SELECT_ALL_RDF_NODES = "Select All RDF Nodes";
	private static final String SELECT_ALL_CLASS_NODES = "Select All Class Nodes";
	private static final String SELECT_ALL_PROPERTY_NODES = "Select All Property Nodes";

	private JMenu getEditMenu() {
		JMenu menu = new JMenu("Edit");
		menu.add(new FindResAction(getRDFGraph(), findResDialog));
		menu.addSeparator();
		//		selectAbstractLevelMode = new JCheckBoxMenuItem("Change Abstract Level", false);
		//		selectAbstractLevelMode.addActionListener(new SelectAbstractLevelAction());
		//		menu.add(selectAbstractLevelMode);
		menu.add(new PreferenceAction());

		return menu;
	}

	private JMenu getSelectMenu() {
		JMenu selectMenu = new JMenu(SELECT_MENU);
		selectMenu.add(new SelectNodes(getRDFGraph(), SELECT_ALL_RDF_NODES));
		selectMenu.add(new SelectNodes(getClassGraph(), SELECT_ALL_CLASS_NODES));
		selectMenu.add(new SelectNodes(getPropertyGraph(), SELECT_ALL_PROPERTY_NODES));

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
			super("Preference");
		}

		public void actionPerformed(ActionEvent e) {
			prefDialog.initPrefixBox();
			prefDialog.setVisible(true);
		}
	}

	private static final String PROJECT = "Project (Java Object)";
	private static final String RDFS_XML = "RDFS/XML";
	private static final String RDFS_NTriple = "RDFS/N-Triple";
	private static final String RDF_XML = "RDF/XML";
	private static final String RDF_NTriple = "RDF/N-Triple";
	private static final String SelectedRDFS_XML = "Selected RDFS/XML";
	private static final String SelectedRDFS_NTriple = "Selected RDFS/N-Triple";
	private static final String SelectedRDF_XML = "Selected RDF/XML";
	private static final String SelectedRDF_NTriple = "Selected RDF/N-Triple";
	private static final String REPLACE_RDF_FILE = "RDF/XML (File)";
	private static final String REPLACE_RDF_URI = "RDF/XML (URI)";
	private static final String REPLACE_RDFS_FILE = "RDFS/XML (File)";
	private static final String REPLACE_RDFS_URI = "RDFS/XML (URI)";
	private static final String MERGE_RDFS_FILE = "RDF(S)/XML (File)";
	private static final String MERGE_RDFS_URI = "RDF(S)/XML (URI)";

	private JMenu getFileMenu() {
		JMenu menu = new JMenu(FILE_MENU);
		menu.add(newProjectAction);
		menu.add(openProjectAction);
		menu.add(saveProjectAction);
		menu.add(saveProjectAsAction);
		menu.addSeparator();

		JMenu importMenu = new JMenu("Import");
		importMenu.setIcon(Utilities.getImageIcon("import.gif"));
		menu.add(importMenu);

		JMenu replaceMenu = new JMenu("Replace");
		replaceMenu.add(new ReplaceRDF(this, REPLACE_RDF_FILE));
		replaceMenu.add(new ReplaceRDF(this, REPLACE_RDF_URI));
		replaceMenu.add(new ReplaceRDFS(this, REPLACE_RDFS_FILE));
		replaceMenu.add(new ReplaceRDFS(this, REPLACE_RDFS_URI));
		importMenu.add(replaceMenu);

		JMenu mergeMenu = new JMenu("Merge");
		mergeMenu.add(new MergeRDFs(this, MERGE_RDFS_FILE));
		mergeMenu.add(new MergeRDFs(this, MERGE_RDFS_URI));
		importMenu.add(mergeMenu);

		importMenu.add(new ImportJavaObject(this));

		JMenu exportMenu = new JMenu("Export");
		exportMenu.setIcon(Utilities.getImageIcon("export.gif"));
		menu.add(exportMenu);

		JMenu rdfMenu = new JMenu("RDF/XML");
		exportMenu.add(rdfMenu);
		rdfMenu.add(new ExportRDF(this, RDF_XML));
		rdfMenu.add(new ExportRDFS(this, RDFS_XML));
		rdfMenu.add(new ExportRDF(this, SelectedRDF_XML));
		rdfMenu.add(new ExportRDFS(this, SelectedRDFS_XML));

		JMenu nTripleMenu = new JMenu("N-Triple");
		exportMenu.add(nTripleMenu);
		nTripleMenu.add(new ExportRDF(this, RDF_NTriple));
		nTripleMenu.add(new ExportRDFS(this, RDFS_NTriple));
		nTripleMenu.add(new ExportRDF(this, SelectedRDF_NTriple));
		nTripleMenu.add(new ExportRDFS(this, SelectedRDFS_NTriple));

		JMenu imgMenu = new JMenu("Image");
		exportMenu.add(imgMenu);
		imgMenu.add(new FileExportImg(this, GraphType.RDF, "png", "RDF Graph -> PNG"));
		imgMenu.add(new FileExportImg(this, GraphType.CLASS, "png", "Class Graph -> PNG"));
		imgMenu.add(new FileExportImg(this, GraphType.PROPERTY, "png", "Property Graph -> PNG"));

		exportMenu.add(new ExportJavaObject(this));

		menu.addSeparator();
		menu.add(getPluginMenus()); // JavaWebStartでは，pluginは使用できないと思われる．
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
		JMenu menu = new JMenu("Plugins");
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

	// encodingの指定ができない．
	//	private Model loadModel(String ext, String lang) {
	//		File file = getFile(true, ext);
	//		if (file == null) {
	//			return null;
	//		}
	//		Model model = ModelLoader.loadModel(file.getAbsolutePath(), lang);	
	//		return model;
	//	}

	private JMenu getViewMenu() {
		JMenu menu = new JMenu(VIEW_MENU);
		ChangeCellViewAction changeCellViewAction = new ChangeCellViewAction();
		uriView = new JRadioButton("URI View");
		uriView.setSelected(true);
		gmanager.setCellViewType(CellViewType.URI);
		uriView.addItemListener(changeCellViewAction);
		idView = new JRadioButton("ID View");
		idView.addItemListener(changeCellViewAction);
		labelView = new JRadioButton("Label View");
		labelView.addItemListener(changeCellViewAction);
		ButtonGroup group = new ButtonGroup();
		group.add(uriView);
		group.add(idView);
		group.add(labelView);
		menu.add(uriView);
		menu.add(idView);
		menu.add(labelView);
		menu.addSeparator();
		showTypeCellBox = new JCheckBoxMenuItem("Show Type", true);
		gmanager.setIsShowTypeCell(true);
		showTypeCellBox.addActionListener(new ShowTypeCellAction());
		menu.add(showTypeCellBox);
		showToolTips = new JCheckBoxMenuItem("Show ToolTips", true);
		showToolTips.addActionListener(new ShowToolTipsAction());
		ToolTipManager.sharedInstance().setEnabled(true);
		menu.add(showToolTips);
		isGroup = new JCheckBoxMenuItem("is Group", true);
		isGroup.addActionListener(new IsGroupAction());
		menu.add(isGroup);

		return menu;
	}

	class IsGroupAction extends AbstractAction {
		public void actionPerformed(ActionEvent e) {
			getRDFGraph().getSelectionModel().setChildrenSelectable(!isGroup.isSelected());
		}
	}

	private static final String TO_FRONT_RDF_EDITOR = "To Front RDF Editor";
	private static final String TO_FRONT_CLASS_EDITOR = "To Front Class Editor";
	private static final String TO_FRONT_PROPERTY_EDITOR = "To Front Property Editor";

	private JMenu getWindowMenu() {
		JMenu menu = new JMenu(WINDOW_MENU);
		menu.add(new ShowOverview(this, rdfEditorOverview, "Show RDF Graph Overview"));
		menu.add(new ShowOverview(this, classEditorOverview, "Show Class Graph Overview"));
		menu.add(new ShowOverview(this, propertyEditorOverview, "Show Property Graph Overview"));
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
		JMenu menu = new JMenu(CONVERT_MENU);

		JMenu rdfView = new JMenu("RDF/XML");
		menu.add(rdfView);
		rdfView.add(new ConvertRDFDoc(this, "RDF/XML"));
		rdfView.add(new ConvertRDFDoc(this, "Selected RDF/XML"));

		JMenu rdfsView = new JMenu("RDFS/XML");
		menu.add(rdfsView);
		rdfsView.add(new ConvertRDFSDoc(this, "RDFS(Class/Property)/XML"));
		rdfsView.add(new ConvertClassDoc(this, "RDFS(Class)/XML"));
		rdfsView.add(new ConvertPropertyDoc(this, "RDFS(Property)/XML"));
		rdfsView.add(new ConvertRDFSDoc(this, "Selected RDFS(Class/Property)/XML"));
		rdfsView.add(new ConvertClassDoc(this, "Selected RDFS(Class)/XML"));
		rdfsView.add(new ConvertPropertyDoc(this, "Selected RDFS(Property)/XML"));

		JMenu nTripleView = new JMenu("RDF/N-Triple");
		menu.add(nTripleView);
		nTripleView.add(new ConvertNTriple(this, "RDF/N-Triple"));
		nTripleView.add(new ConvertNTriple(this, "Selected RDF/N-Triple"));

		return menu;
	}

	private JMenu getHelpMenu() {
		JMenu menu = new JMenu(HELP_MENU);
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
		setTitle("MR^3 - New Project");
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
		ImageIcon icon = Utilities.getImageIcon("mr3_logo.png");
		JWindow splashWindow = new HelpWindow(null, icon);
		try {
			new MR3();
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println(e.getMessage());
		} finally {
			splashWindow.dispose();
		}
	}
}
