package mr3;

import java.awt.*;
import java.awt.Container;
import java.awt.event.*;
import java.beans.*;
import java.io.*;
import java.lang.reflect.*;
import java.net.*;
import java.util.*;
import java.util.prefs.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;

import mr3.data.*;
import mr3.editor.*;
import mr3.editor.PropertyEditor;
import mr3.io.*;
import mr3.jgraph.*;
import mr3.ui.*;
import mr3.util.*;
import actions.*;

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
	private RealRDFEditor realRDFEditor;
	private ClassEditor classEditor;
	private PropertyEditor propertyEditor;
	private JTextArea srcArea;

	private RDFSTreePanel classTreePanel;
	private RDFSTreePanel propTreePanel;

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
	private JInternalFrame[] internalFrames = new JInternalFrame[3];
	private JInternalFrame srcFrame;
	private JCheckBoxMenuItem rdfEditorView;
	private JCheckBoxMenuItem classEditorView;
	private JCheckBoxMenuItem propertyEditorView;
	private JCheckBoxMenuItem showSrcWindowBox;

	private JRadioButton uriView;
	private JRadioButton idView;
	private JRadioButton labelView;

	private static final Color DESKTOP_BACK_COLOR = new Color(225, 225, 225);

	private Preferences userPrefs; // ユーザの設定を保存(Windowサイズなど）
	private static ResourceBundle resources;

	private MR3 obj_for_plugin;

	MR3(String title) {
		super(title);

		userPrefs = Preferences.userNodeForPackage(this.getClass());

		setSize(userPrefs.getInt(PrefConstants.WindowWidth, MAIN_FRAME_WIDTH), userPrefs.getInt(PrefConstants.WindowHeight, MAIN_FRAME_HEIGHT));
		setLocation(userPrefs.getInt(PrefConstants.WindowPositionX, 50), userPrefs.getInt(PrefConstants.WindowPositionY, 50));
		//		setLookAndFeel();

		attrDialog = new AttributeDialog();
		gmanager = new GraphManager(attrDialog, userPrefs);
		createDesktop();
		gmanager.setDesktop(desktop);

		rdfEditor = new RDFEditor(nsTableDialog, findResDialog, gmanager);
		//		realRDFEditor = new RealRDFEditor(nsTableDialog, findResDialog, gmanager);
		classEditor = new ClassEditor(nsTableDialog, findResDialog, gmanager);
		propertyEditor = new PropertyEditor(nsTableDialog, findResDialog, gmanager);
		srcArea = new JTextArea();
		srcArea.setEditable(false);

		mr3Reader = new MR3Reader(gmanager);
		mr3Writer = new MR3Writer(gmanager);

		createInternalFrames();

		desktop.setBackground(DESKTOP_BACK_COLOR);
		//		classTreePanel = new RDFSTreePanel(gmanager, rdfsInfoMap.getClassTreeModel(), new ClassTreeCellRenderer());
		//		propTreePanel = new RDFSTreePanel(gmanager, rdfsInfoMap.getPropTreeModel(), new PropertyTreeCellRenderer());
		//		JTabbedPane treeTab = new JTabbedPane();
		//		treeTab.add("Class", classTreePanel);
		//		treeTab.add("Property", propTreePanel);
		//		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, treeTab, desktop);
		//		splitPane.setOneTouchExpandable(true);
		//		getContentPane().add(splitPane);
		getContentPane().add(desktop);

		obj_for_plugin = this; //一時しのぎ

		setJMenuBar(createMenuBar());
		setIcon();

		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		addWindowListener(new CloseWindow(this));
		setVisible(true);
		loadWindows();
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
		internalFrames[0] = rdfEditor;
		internalFrames[1] = classEditor;
		internalFrames[2] = propertyEditor;

		desktop.add(internalFrames[0], DEFAULT_CURSOR);
		desktop.add(internalFrames[1], DEFAULT_CURSOR);
		desktop.add(internalFrames[2], DEFAULT_CURSOR);

		srcFrame = createInternalFrame(new JScrollPane(srcArea), "Source Window", DEMO_FRAME_LAYER);
		URL srcAreaUrl = this.getClass().getClassLoader().getResource("mr3/resources/source_window.gif");
		srcFrame.setFrameIcon(new ImageIcon(srcAreaUrl));
		srcFrame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		srcFrame.addInternalFrameListener(new CloseInternalFrameAction());
		srcFrame.setBounds(new Rectangle(0, 0, FRAME_WIDTH, FRAME_HEIGHT));
		srcFrame.setVisible(false);

		rdfEditor.setInternalFrames(internalFrames);
		classEditor.setInternalFrames(internalFrames);
		propertyEditor.setInternalFrames(internalFrames);
	}

	class CloseInternalFrameAction extends InternalFrameAdapter {
		public void internalFrameClosing(InternalFrameEvent e) {
			JInternalFrame tmp = e.getInternalFrame();

			if (tmp == internalFrames[0]) {
				rdfEditorView.setSelected(false);
			} else if (tmp == internalFrames[1]) {
				classEditorView.setSelected(false);
			} else if (tmp == internalFrames[2]) {
				propertyEditorView.setSelected(false);
			} else if (tmp == srcFrame) {
				showSrcWindowBox.setSelected(false);
			}
			tmp.setVisible(false);
		}
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

	public JInternalFrame createInternalFrame(Container container, String title, Integer layer) {
		JInternalFrame jif = new JInternalFrame(title, true, true, true);
		jif.setIconifiable(true);
		jif.setContentPane(container);
		desktop.add(jif, layer);
		try {
			jif.setSelected(true);
		} catch (java.beans.PropertyVetoException e) {
			e.printStackTrace();
		}
		jif.setVisible(true);

		return jif;
	}

	private void setIcon() {
		URL jgraphUrl = this.getClass().getClassLoader().getResource("mr3/resources/mr3_logo.png");
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

	private static final String SELECT_ALL_NODES = "Select All Nodes";
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
		//JMenu layout = new JMenu("Layout");
		//mi = new JMenuItem("TreeAlgorithm");
		//mi.addActionListener(new GraphLayoutAction());
		//		layout.add(mi);
		//		menu.add(layout);
		JMenu selectMenu = new JMenu("Select");
		selectMenu.add(new SelectNodes(this, SELECT_ALL_NODES));
		selectMenu.add(new SelectNodes(this, SELECT_ALL_RDF_NODES));
		selectMenu.add(new SelectNodes(this, SELECT_ALL_CLASS_NODES));
		selectMenu.add(new SelectNodes(this, SELECT_ALL_PROPERTY_NODES));
		menu.add(selectMenu);
		menu.addSeparator();
		menu.add(new PreferenceAction());

		return menu;
	}

	public JInternalFrame[] getInternalFrames() {
		return internalFrames;
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
		replace.add(new ReplaceRDF(REPLACE_RDF_FILE));
		replace.add(new ReplaceRDF(REPLACE_RDF_URI));
		importRDF.add(replace);

		JMenu mergeMenu = new JMenu("Merge");
		mergeMenu.add(new MergeRDFs(this, MERGE_RDFS_FILE));
		mergeMenu.add(new MergeRDFs(this, MERGE_RDFS_URI));
		importRDF.add(mergeMenu);

		//		importRDF.add(new ImportRealRDFAction());
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
		internalFrames[0].setBounds(new Rectangle(editorPositionX, editorPositionY, editorWidth, editorHeight)); // RDF

		editorPositionX = userPrefs.getInt(PrefConstants.ClassEditorPositionX, 0);
		editorPositionY = userPrefs.getInt(PrefConstants.ClassEditorPositionY, 0);
		editorWidth = userPrefs.getInt(PrefConstants.ClassEditorWidth, width / 2);
		editorHeight = userPrefs.getInt(PrefConstants.ClassEditorHeight, height / 2);
		internalFrames[1].setBounds(new Rectangle(editorPositionX, editorPositionY, editorWidth, editorHeight)); // RDF

		editorPositionX = userPrefs.getInt(PrefConstants.PropertyEditorPositionX, width / 2);
		editorPositionY = userPrefs.getInt(PrefConstants.PropertyEditorPositionY, 0);
		editorWidth = userPrefs.getInt(PrefConstants.PropertyEditorWidth, width / 2);
		editorHeight = userPrefs.getInt(PrefConstants.PropertyEditorHeight, height / 2);
		internalFrames[2].setBounds(new Rectangle(editorPositionX, editorPositionY, editorWidth, editorHeight)); // RDF
	}

	private JMenu getPluginMenus() {
		JMenu menu = new JMenu("Plugins");
		Map pluginMenuMap = PluginLoader.getPluginMenuMap();
		Set keys = pluginMenuMap.keySet();
		for (Iterator i = keys.iterator(); i.hasNext();) {
			String menuName = (String) i.next();
			menu.add(new PluginAction(menuName));
		}
		return menu;
	}

	private static final String PLUGIN_METHOD_NAME = "exec";

	class PluginAction extends AbstractAction {

		PluginAction(String title) {
			super(title);
		}

		public void actionPerformed(ActionEvent e) {
			String menuName = e.getActionCommand();
			Map pluginMenuMap = PluginLoader.getPluginMenuMap();
			try {
				Class classObj = (Class) pluginMenuMap.get(menuName);
				Object instance = classObj.newInstance();
				Method initMethod = classObj.getMethod("setMRCube", new Class[] { MR3.class });
				initMethod.invoke(instance, new Object[] { obj_for_plugin });
				Method m = classObj.getMethod(PLUGIN_METHOD_NAME, null);
				m.invoke(instance, null);
			} catch (NoSuchMethodException nsme) {
				nsme.printStackTrace();
			} catch (InstantiationException ine) {
				ine.printStackTrace();
			} catch (IllegalAccessException ille) {
				ille.printStackTrace();
			} catch (InvocationTargetException inve) {
				inve.printStackTrace();
			}
		}
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
		//		menu.add(getEditorViewMenu());
		showSrcWindowBox = new JCheckBoxMenuItem("Show Source Window", false);
		showSrcWindowBox.addActionListener(new ShowViewAction());
		menu.add(attrDialog.getShowPropWindow());
		menu.add(nsTableDialog.getShowNSTable());
		menu.add(showSrcWindowBox);
		showTypeCellBox = new JCheckBoxMenuItem("Show Type", true);
		gmanager.setIsShowTypeCell(true);
		showTypeCellBox.addActionListener(new ShowTypeCellAction());
		menu.add(showTypeCellBox);
		showToolTips = new JCheckBoxMenuItem("Show ToolTips", true);
		showToolTips.addActionListener(new ShowToolTipsAction());
		ToolTipManager.sharedInstance().setEnabled(true);
		menu.add(showToolTips);

		return menu;
	}

	// エディタは常に表示しておくべきという指摘があったので，消すことはできないようにする．
	private JMenu getEditorViewMenu() {
		JMenu editorViewMenu = new JMenu("Editor");
		rdfEditorView = new JCheckBoxMenuItem("Show RDF Editor", true);
		rdfEditorView.addActionListener(new ShowViewAction());
		editorViewMenu.add(rdfEditorView);
		classEditorView = new JCheckBoxMenuItem("Show Class Editor", true);
		classEditorView.addActionListener(new ShowViewAction());
		editorViewMenu.add(classEditorView);
		propertyEditorView = new JCheckBoxMenuItem("Show Property Editor", true);
		propertyEditorView.addActionListener(new ShowViewAction());
		editorViewMenu.add(propertyEditorView);
		return editorViewMenu;
	}

	private static final String TO_FRONT_RDF_EDITOR = "To Front RDF Editor";
	private static final String TO_FRONT_CLASS_EDITOR = "To Front Class Editor";
	private static final String TO_FRONT_PROPERTY_EDITOR = "To Front Property Editor";

	private JMenu getWindowMenu() {
		JMenu menu = new JMenu("Window");
		menu.add(new EditorSelectAction(TO_FRONT_RDF_EDITOR));
		menu.add(new EditorSelectAction(TO_FRONT_CLASS_EDITOR));
		menu.add(new EditorSelectAction(TO_FRONT_PROPERTY_EDITOR));
		menu.addSeparator();
		menu.add(new DeployWindows(this));

		return menu;
	}

	class EditorSelectAction extends AbstractAction {

		EditorSelectAction(String title) {
			super(title);
		}

		private void toFrontInternalFrame(int i) {
			try {
				internalFrames[i].toFront();
				internalFrames[i].setIcon(false);
				internalFrames[i].setSelected(true);
			} catch (PropertyVetoException pve) {
				pve.printStackTrace();
			}
		}

		public void actionPerformed(ActionEvent e) {
			String en = e.getActionCommand();
			if (en.equals(TO_FRONT_RDF_EDITOR)) {
				toFrontInternalFrame(0);
			} else if (en.equals(TO_FRONT_CLASS_EDITOR)) {
				toFrontInternalFrame(1);
			} else if (en.equals(TO_FRONT_PROPERTY_EDITOR)) {
				toFrontInternalFrame(2);
			}
		}

	}

	class ShowViewAction extends AbstractAction {
		public void actionPerformed(ActionEvent e) {
			JCheckBoxMenuItem tmp = (JCheckBoxMenuItem) e.getSource();
			if (tmp == rdfEditorView) {
				internalFrames[0].setVisible(rdfEditorView.getState());
				internalFrames[0].toFront();
			} else if (tmp == classEditorView) {
				internalFrames[1].setVisible(classEditorView.getState());
				internalFrames[1].toFront();
			} else if (tmp == propertyEditorView) {
				internalFrames[2].setVisible(propertyEditorView.getState());
				internalFrames[2].toFront();
			} else if (tmp == showSrcWindowBox) {
				srcFrame.setVisible(showSrcWindowBox.getState());
			}
		}
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

	class GraphLayoutAction extends AbstractAction {
		public void actionPerformed(ActionEvent e) {
			gmanager.applyTreeLayout();
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

	public void showSrcView() {
		try {
			srcFrame.toFront();
			srcFrame.setVisible(true);
			srcFrame.setIcon(false);
			showSrcWindowBox.setState(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
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
		return srcArea;
	}

	public static void main(String[] arg) {
		new MR3("MR^3 - New Project");
	}
}
