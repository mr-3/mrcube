package mr3;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.prefs.*;

import javax.swing.*;
import javax.swing.text.*;

import mr3.actions.*;
import mr3.data.*;
import mr3.editor.*;
import mr3.io.*;
import mr3.jgraph.*;
import mr3.ui.*;
import mr3.util.*;

import com.hp.hpl.mesa.rdf.jena.model.*;

/**
  *   Meta-  Model Management founded on RDF-baed Revision Reflection  
  *   {MR} ^3
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
	private ClassEditor classEditor;
	private PropertyEditor propertyEditor;

	//	private RDFSTreePanel classTreePanel;
	//	private RDFSTreePanel propTreePanel;

	private MR3Reader mr3Reader;
	private MR3Writer mr3Writer;

	private NameSpaceTableDialog nsTableDialog;
	private FindResourceDialog findResDialog;
	private AttributeDialog attrDialog;
	private PrefDialog prefDialog;

	private GraphManager gmanager;
	private RDFResourceInfoMap resInfoMap = RDFResourceInfoMap.getInstance();
	private RDFLiteralInfoMap litInfoMap = RDFLiteralInfoMap.getInstance();
	private RDFSInfoMap rdfsInfoMap = RDFSInfoMap.getInstance();

	private JCheckBoxMenuItem showTypeCellBox;
	private JCheckBoxMenuItem selectAbstractLevelMode;
	private JCheckBoxMenuItem showToolTips;
	private JCheckBoxMenuItem isGroup;

	private JInternalFrame[] iFrames = new JInternalFrame[3];
	private SourceFrame srcFrame;
	private JCheckBoxMenuItem rdfEditorView;
	private JCheckBoxMenuItem classEditorView;
	private JCheckBoxMenuItem propertyEditorView;

	private JRadioButton uriView;
	private JRadioButton idView;
	private JRadioButton labelView;

	private static final Color DESKTOP_BACK_COLOR = new Color(225, 225, 225);

	private Preferences userPrefs; // ユーザの設定を保存(Windowサイズなど）
	private static ResourceBundle resources;

	MR3(String title) {
		super(title);

		userPrefs = Preferences.userNodeForPackage(this.getClass());

		setSize(userPrefs.getInt(PrefConstants.WindowWidth, MAIN_FRAME_WIDTH), userPrefs.getInt(PrefConstants.WindowHeight, MAIN_FRAME_HEIGHT));
		setLocation(userPrefs.getInt(PrefConstants.WindowPositionX, 50), userPrefs.getInt(PrefConstants.WindowPositionY, 50));
		//		setLookAndFeel();

		attrDialog = new AttributeDialog();
		gmanager = new GraphManager(attrDialog, userPrefs);
		getContentPane().add(createToolBar(), BorderLayout.NORTH);
		createDesktop();
		gmanager.setDesktop(desktop);

		rdfEditor = new RDFEditor(nsTableDialog, findResDialog, gmanager);
		classEditor = new ClassEditor(nsTableDialog, findResDialog, gmanager);
		propertyEditor = new PropertyEditor(nsTableDialog, findResDialog, gmanager);

		mr3Reader = new MR3Reader(gmanager, nsTableDialog);
		mr3Writer = new MR3Writer(gmanager);

		createInternalFrames();

		//		setTreeLayout();
		desktop.setBackground(DESKTOP_BACK_COLOR);

		getContentPane().add(desktop);

		setJMenuBar(createMenuBar());
		setIcon();

		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		addWindowListener(new CloseWindow(this));
		setVisible(true);
		loadWindows();
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

	private URL getImageIcon(String image) {
		return this.getClass().getClassLoader().getResource("mr3/resources/" + image);
	}

	private JToolBar createToolBar() {
		JToolBar toolbar = new JToolBar();
		toolbar.setFloatable(false);

		ImageIcon newProjectIcon = new ImageIcon(getImageIcon("new.gif"));
		toolbar.add(new NewProject(this, newProjectIcon));
		ImageIcon openProjectIcon = new ImageIcon(getImageIcon("open.gif"));
		toolbar.add(new OpenProject(this, openProjectIcon));
		ImageIcon saveProjectIcon = new ImageIcon(getImageIcon("save.gif"));
		toolbar.add(new SaveProject(this, "Save Project", saveProjectIcon));
		ImageIcon saveAsProjectIcon = new ImageIcon(getImageIcon("saveas.gif"));
		toolbar.add(new SaveProject(this, "Save Project As", saveAsProjectIcon));

		toolbar.addSeparator();

		ImageIcon nsTableDialogIcon = new ImageIcon(getImageIcon("nameSpaceTableIcon.gif"));
		toolbar.add(new AbstractAction("", nsTableDialogIcon) {
			public void actionPerformed(ActionEvent e) {
				nsTableDialog.setVisible(true);
			}
		});

		ImageIcon attrDialogIcon = new ImageIcon(getImageIcon("attrDialogIcon.gif"));
		toolbar.add(new AbstractAction("", attrDialogIcon) {
			public void actionPerformed(ActionEvent e) {
				RDFGraph graph = null;
				Object selectionCell = null;
				if (iFrames[0].isSelected()) {
					graph = gmanager.getRDFGraph();
					selectionCell = graph.getSelectionCell();
				} else if (iFrames[1].isSelected()) {
					graph = gmanager.getClassGraph();
					selectionCell = graph.getSelectionCell();
				} else if (iFrames[2].isSelected()) {
					graph = gmanager.getPropertyGraph();
					selectionCell = graph.getSelectionCell();
				}

				attrDialog.setVisible(true);

				if (graph != null && selectionCell != null) {
					graph.setSelectionCell(selectionCell);
				}
			}
		});

		toolbar.addSeparator();

		ImageIcon rdfIcon = new ImageIcon(getImageIcon("rdfEditorIcon.gif"));
		toolbar.add(new EditorSelect(this, TO_FRONT_RDF_EDITOR, rdfIcon));
		ImageIcon classIcon = new ImageIcon(getImageIcon("classEditorIcon.gif"));
		toolbar.add(new EditorSelect(this, TO_FRONT_CLASS_EDITOR, classIcon));
		ImageIcon propertyIcon = new ImageIcon(getImageIcon("propertyEditorIcon.gif"));
		toolbar.add(new EditorSelect(this, TO_FRONT_PROPERTY_EDITOR, propertyIcon));

		return toolbar;
	}

	private void createDesktop() {
		desktop = new JDesktopPane();
		desktop.add(attrDialog, JLayeredPane.MODAL_LAYER);
		findResDialog = new FindResourceDialog("Find Resource", gmanager);
		desktop.add(findResDialog, JLayeredPane.MODAL_LAYER);
		nsTableDialog = new NameSpaceTableDialog(gmanager);
		desktop.add(nsTableDialog, JLayeredPane.MODAL_LAYER);
		desktop.add(gmanager.getRefDialog(), JLayeredPane.MODAL_LAYER);
		prefDialog = new PrefDialog(gmanager, userPrefs);
		prefDialog.setVisible(false);
		desktop.add(prefDialog, JLayeredPane.MODAL_LAYER);
	}

	private void createInternalFrames() {
		iFrames[0] = rdfEditor;
		iFrames[1] = classEditor;
		iFrames[2] = propertyEditor;
		srcFrame = new SourceFrame("Source Window");

		desktop.add(iFrames[0], Cursor.DEFAULT_CURSOR);
		desktop.add(iFrames[1], Cursor.DEFAULT_CURSOR);
		desktop.add(iFrames[2], Cursor.DEFAULT_CURSOR);
		desktop.add(srcFrame, Cursor.DEFAULT_CURSOR);

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

	private void setIcon() {
		URL jgraphUrl = getImageIcon("mr3_logo.png");
		if (jgraphUrl != null) {
			ImageIcon jgraphIcon = new ImageIcon(jgraphUrl);
			setIconImage(jgraphIcon.getImage());
		}
	}

	private JMenuBar createMenuBar() {
		JMenuBar mb = new JMenuBar();
		mb.add(getFileMenu());
		mb.add(getEditMenu());
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
		menu.add(new FindAction());
		menu.addSeparator();
		//		selectAbstractLevelMode = new JCheckBoxMenuItem("Change Abstract Level", false);
		//		selectAbstractLevelMode.addActionListener(new SelectAbstractLevelAction());
		//		menu.add(selectAbstractLevelMode);
		JMenu selectMenu = new JMenu("Select");
		selectMenu.add(new SelectNodes(gmanager.getRDFGraph(), SELECT_ALL_RDF_NODES));
		selectMenu.add(new SelectNodes(gmanager.getClassGraph(), SELECT_ALL_CLASS_NODES));
		selectMenu.add(new SelectNodes(gmanager.getPropertyGraph(), SELECT_ALL_PROPERTY_NODES));
		menu.add(selectMenu);
		menu.addSeparator();
		menu.add(new PreferenceAction());

		return menu;
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
	private static final String MERGE_RDFS_FILE = "RDF(S)/XML (File)";
	private static final String MERGE_RDFS_URI = "RDF(S)/XML (URI)";

	private JMenu getFileMenu() {
		JMenu menu = new JMenu("File");
		menu.add(new NewProject(this));
		menu.add(new OpenProject(this));
		menu.add(new SaveProject(this, "Save Project"));
		menu.add(new SaveProject(this, "Save Project As"));
		menu.addSeparator();
		JMenu importRDF = new JMenu("Import");
		importRDF.add(new ImportJavaObject(this));
		JMenu replace = new JMenu("Replace");
		replace.add(new ReplaceRDF(this, REPLACE_RDF_FILE));
		replace.add(new ReplaceRDF(this, REPLACE_RDF_URI));
		importRDF.add(replace);

		JMenu mergeMenu = new JMenu("Merge");
		mergeMenu.add(new MergeRDFs(this, MERGE_RDFS_FILE));
		mergeMenu.add(new MergeRDFs(this, MERGE_RDFS_URI));
		importRDF.add(mergeMenu);

		menu.add(importRDF);

		JMenu exportMenu = new JMenu("Export");
		menu.add(exportMenu);
		exportMenu.add(new ExportJavaObject(this));
		exportMenu.add(new ExportRDFS(this, RDFS_XML));
		exportMenu.add(new ExportRDFS(this, RDFS_NTriple));
		exportMenu.add(new ExportRDF(this, RDF_XML));
		exportMenu.add(new ExportRDF(this, RDF_NTriple));

		JMenu selectedMenu = new JMenu("Selected");
		exportMenu.add(selectedMenu);
		selectedMenu.add(new ExportRDFS(this, SelectedRDFS_XML));
		selectedMenu.add(new ExportRDFS(this, SelectedRDFS_NTriple));
		selectedMenu.add(new ExportRDF(this, SelectedRDF_XML));
		selectedMenu.add(new ExportRDF(this, SelectedRDF_NTriple));

		menu.addSeparator();
		menu.add(getPluginMenus()); // JavaWebStartでは，pluginは使用できないと思われる．
		menu.addSeparator();
		menu.add(new ExitAction(this));

		return menu;
	}

	private void loadWindows() {
		int width = desktop.getWidth();
		int height = desktop.getHeight();

		int editorPositionX = userPrefs.getInt(PrefConstants.RDFEditorPositionX, 0);
		int editorPositionY = userPrefs.getInt(PrefConstants.RDFEditorPositionY, height / 2);
		int editorWidth = userPrefs.getInt(PrefConstants.RDFEditorWidth, width);
		int editorHeight = userPrefs.getInt(PrefConstants.RDFEditorHeight, height / 2);
		iFrames[0].setBounds(new Rectangle(editorPositionX, editorPositionY, editorWidth, editorHeight)); // RDF

		editorPositionX = userPrefs.getInt(PrefConstants.ClassEditorPositionX, 0);
		editorPositionY = userPrefs.getInt(PrefConstants.ClassEditorPositionY, 0);
		editorWidth = userPrefs.getInt(PrefConstants.ClassEditorWidth, width / 2);
		editorHeight = userPrefs.getInt(PrefConstants.ClassEditorHeight, height / 2);
		iFrames[1].setBounds(new Rectangle(editorPositionX, editorPositionY, editorWidth, editorHeight)); // RDF

		editorPositionX = userPrefs.getInt(PrefConstants.PropertyEditorPositionX, width / 2);
		editorPositionY = userPrefs.getInt(PrefConstants.PropertyEditorPositionY, 0);
		editorWidth = userPrefs.getInt(PrefConstants.PropertyEditorWidth, width / 2);
		editorHeight = userPrefs.getInt(PrefConstants.PropertyEditorHeight, height / 2);
		iFrames[2].setBounds(new Rectangle(editorPositionX, editorPositionY, editorWidth, editorHeight)); // RDF
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

	// encodingの指定ができないので，却下．
	//	private Model loadModel(String ext, String lang) {
	//		File file = getFile(true, ext);
	//		if (file == null) {
	//			return null;
	//		}
	//		Model model = ModelLoader.loadModel(file.getAbsolutePath(), lang);	
	//		return model;
	//	}

	private JMenu getViewMenu() {
		JMenu menu = new JMenu("View");
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
		menu.add(attrDialog.getShowPropWindow());
		menu.add(nsTableDialog.getShowNSTable());
		menu.add(srcFrame.getShowSrcWindowBox());
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
			gmanager.getRDFGraph().getSelectionModel().setChildrenSelectable(!isGroup.isSelected());
		}
	}

	private static final String TO_FRONT_RDF_EDITOR = "To Front RDF Editor";
	private static final String TO_FRONT_CLASS_EDITOR = "To Front Class Editor";
	private static final String TO_FRONT_PROPERTY_EDITOR = "To Front Property Editor";

	private JMenu getWindowMenu() {
		JMenu menu = new JMenu("Window");
		menu.add(new EditorSelect(this, TO_FRONT_RDF_EDITOR));
		menu.add(new EditorSelect(this, TO_FRONT_CLASS_EDITOR));
		menu.add(new EditorSelect(this, TO_FRONT_PROPERTY_EDITOR));
		menu.addSeparator();
		menu.add(new DeployWindows(this));

		return menu;
	}

	private JMenu getConvertMenu() {
		JMenu menu = new JMenu("Convert");

		JMenu rdfView = new JMenu("RDF");
		menu.add(rdfView);
		rdfView.add(new ConvertRDF(this, "RDF/XML"));
		rdfView.add(new ConvertNTriple(this, "RDF/N-Triple"));

		JMenu rdfsView = new JMenu("RDFS");
		menu.add(rdfsView);
		rdfsView.add(new ConvertRDFS(this, "RDFS(Class/Property)/XML"));
		rdfsView.add(new ConvertClass(this, "RDFS(Class)/XML"));
		rdfsView.add(new ConvertProperty(this, "RDFS(Property)/XML"));

		JMenu selectedRDFView = new JMenu("Selected RDF");
		menu.add(selectedRDFView);
		selectedRDFView.add(new ConvertRDF(this, "Selected RDF/XML"));
		selectedRDFView.add(new ConvertNTriple(this, "Selected RDF/N-Triple"));

		JMenu selectedRDFSView = new JMenu("Selected RDFS");
		menu.add(selectedRDFSView);
		selectedRDFSView.add(new ConvertRDFS(this, "Selected RDFS(Class/Property)/XML"));
		selectedRDFSView.add(new ConvertClass(this, "Selected RDFS(Class)/XML"));
		selectedRDFSView.add(new ConvertProperty(this, "RDFS(Property)/XML"));

		return menu;
	}

	private JMenu getHelpMenu() {
		JMenu menu = new JMenu("Help");
		menu.add(new HelpAbout());
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

	class FindAction extends AbstractAction {

		FindAction() {
			super("Find Resource");
		}

		public void actionPerformed(ActionEvent e) {
			findResDialog.setVisible(true);
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

	public JCheckBoxMenuItem getShowSrcWindowBox() {
		return srcFrame.getShowSrcWindowBox();
	}

	public JInternalFrame getSourceFrame() {
		return srcFrame;
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

	public void replaceRDFModel(Model model) {
		mr3Reader.replaceRDF(model);
	}

	public void mergeRDFModel(Model model) {
		mr3Reader.mergeRDF(model);
	}

	public void mergeRDFSModel(Model model) {
		mr3Reader.mergeRDFS(model);
	}

	public JTextComponent getSourceArea() {
		return srcFrame.getSourceArea();
	}

	public static void main(String[] arg) {
		new MR3("MR^3 - New Project");
	}
}
