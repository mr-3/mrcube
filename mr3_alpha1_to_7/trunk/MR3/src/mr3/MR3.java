package mr3;

import java.awt.*;
import java.awt.Container;
import java.awt.event.*;
import java.beans.*;
import java.io.*;
import java.lang.reflect.*;
import java.net.*;
import java.util.*;
import java.util.List;
import java.util.prefs.*;
import java.util.zip.*;

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

import com.hp.hpl.jena.rdf.arp.*;
import com.hp.hpl.mesa.rdf.jena.common.*;
import com.hp.hpl.mesa.rdf.jena.mem.*;
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
		addWindowListener(new WindowChangedAction());
		setVisible(true);
		loadWindows();
	}

	class WindowChangedAction extends WindowAdapter {
		public void windowClosing(WindowEvent e) {
			exitProgram();
		}
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
		internalFrames[0] = createInternalFrame(rdfEditor, "RDF Editor", DEMO_FRAME_LAYER);
		internalFrames[0].setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		internalFrames[0].addInternalFrameListener(new CloseInternalFrameAction());
		URL rdfEditorUrl = this.getClass().getClassLoader().getResource("mr3/resources/rdfEditorIcon.gif");
		internalFrames[0].setFrameIcon(new ImageIcon(rdfEditorUrl));

		//		createInternalFrame(realRDFEditor, "Real RDF Editor", DEMO_FRAME_LAYER);
		internalFrames[1] = createInternalFrame(classEditor, "Class Editor", DEMO_FRAME_LAYER);
		internalFrames[1].setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		internalFrames[1].addInternalFrameListener(new CloseInternalFrameAction());
		URL classEditorUrl = this.getClass().getClassLoader().getResource("mr3/resources/classEditorIcon.gif");
		internalFrames[1].setFrameIcon(new ImageIcon(classEditorUrl));

		internalFrames[2] = createInternalFrame(propertyEditor, "Property Editor", DEMO_FRAME_LAYER);
		internalFrames[2].setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		internalFrames[2].addInternalFrameListener(new CloseInternalFrameAction());
		URL propertyEditorUrl = this.getClass().getClassLoader().getResource("mr3/resources/propertyEditorIcon.gif");
		internalFrames[2].setFrameIcon(new ImageIcon(propertyEditorUrl));

		srcFrame = createInternalFrame(new JScrollPane(srcArea), "Source Window", DEMO_FRAME_LAYER);
		URL srcAreaUrl = this.getClass().getClassLoader().getResource("mr3/resources/source_window.gif");
		srcFrame.setFrameIcon(new ImageIcon(srcAreaUrl));
		srcFrame.setClosable(true);
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
		JInternalFrame jif = new JInternalFrame(title);
		jif.setClosable(false);
		jif.setMaximizable(true);
		jif.setIconifiable(true);
		jif.setResizable(true);
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
		selectMenu.add(new SelectNodesAction(SELECT_ALL_NODES));
		selectMenu.add(new SelectNodesAction(SELECT_ALL_RDF_NODES));
		selectMenu.add(new SelectNodesAction(SELECT_ALL_CLASS_NODES));
		selectMenu.add(new SelectNodesAction(SELECT_ALL_PROPERTY_NODES));
		menu.add(selectMenu);
		menu.addSeparator();
		menu.add(new PreferenceAction());

		return menu;
	}

	class SelectNodesAction extends AbstractAction {

		SelectNodesAction(String title) {
			super(title);
		}

		public void actionPerformed(ActionEvent e) {
			String menuName = e.getActionCommand();
			if (menuName.equals(SELECT_ALL_NODES)) {
				gmanager.selectAllNodes();
			} else if (menuName.equals(SELECT_ALL_RDF_NODES)) {
				gmanager.selectAllRDFNodes();
			} else if (menuName.equals(SELECT_ALL_CLASS_NODES)) {
				gmanager.selectAllClassNodes();
			} else if (menuName.equals(SELECT_ALL_PROPERTY_NODES)) {
				gmanager.selectAllPropertyNodes();
			}
		}
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
		importRDF.add(new ImportProjectAction());
		JMenu replace = new JMenu("Replace");
		replace.add(new ReplaceRDFAction(REPLACE_RDF_FILE));
		replace.add(new ReplaceRDFAction(REPLACE_RDF_URI));
		importRDF.add(replace);

		JMenu mergeMenu = new JMenu("Merge");
		mergeMenu.add(new MergeRDFsAction(MERGE_RDFS_FILE));
		mergeMenu.add(new MergeRDFsAction(MERGE_RDFS_URI));
		importRDF.add(mergeMenu);

		//		importRDF.add(new ImportRealRDFAction());
		menu.add(importRDF);

		JMenu exportMenu = new JMenu("Export");
		menu.add(exportMenu);
		exportMenu.add(new ExportProjectAction());
		exportMenu.add(new ExportRDFSAction(RDFS_XML));
		exportMenu.add(new ExportRDFSAction(RDFS_NTriple));
		exportMenu.add(new ExportRDFAction(RDFS_XML));
		exportMenu.add(new ExportRDFAction(RDFS_NTriple));

		JMenu selectedMenu = new JMenu("Selected");
		exportMenu.add(selectedMenu);
		selectedMenu.add(new ExportRDFSAction(SelectedRDFS_XML));
		selectedMenu.add(new ExportRDFSAction(SelectedRDFS_NTriple));
		selectedMenu.add(new ExportRDFAction(SelectedRDFS_XML));
		selectedMenu.add(new ExportRDFAction(SelectedRDFS_NTriple));

		menu.addSeparator();
		menu.add(getPluginMenus()); // JavaWebStartでは，pluginは使用できないと思われる．
		menu.addSeparator();
		menu.add(new ExitAction(this));

		return menu;
	}

	private void saveWindowBounds() {
		Rectangle windowRect = getBounds();
		userPrefs.putInt(PrefConstants.WindowHeight, (int) windowRect.getHeight());
		userPrefs.putInt(PrefConstants.WindowWidth, (int) windowRect.getWidth());
		userPrefs.putInt(PrefConstants.WindowPositionX, (int) windowRect.getX());
		userPrefs.putInt(PrefConstants.WindowPositionY, (int) windowRect.getY());
	}

	private void saveRDFEditorBounds() {
		Rectangle rdfEditorRect = internalFrames[0].getBounds();
		userPrefs.putInt(PrefConstants.RDFEditorHeight, (int) rdfEditorRect.getHeight());
		userPrefs.putInt(PrefConstants.RDFEditorWidth, (int) rdfEditorRect.getWidth());
		userPrefs.putInt(PrefConstants.RDFEditorPositionX, (int) rdfEditorRect.getX());
		userPrefs.putInt(PrefConstants.RDFEditorPositionY, (int) rdfEditorRect.getY());
	}

	private void saveClassEditorBounds() {
		Rectangle classEditorRect = internalFrames[1].getBounds();
		userPrefs.putInt(PrefConstants.ClassEditorHeight, (int) classEditorRect.getHeight());
		userPrefs.putInt(PrefConstants.ClassEditorWidth, (int) classEditorRect.getWidth());
		userPrefs.putInt(PrefConstants.ClassEditorPositionX, (int) classEditorRect.getX());
		userPrefs.putInt(PrefConstants.ClassEditorPositionY, (int) classEditorRect.getY());
	}

	private void savePropertyEditorBounds() {
		Rectangle propertyEditorRect = internalFrames[2].getBounds();
		userPrefs.putInt(PrefConstants.PropertyEditorHeight, (int) propertyEditorRect.getHeight());
		userPrefs.putInt(PrefConstants.PropertyEditorWidth, (int) propertyEditorRect.getWidth());
		userPrefs.putInt(PrefConstants.PropertyEditorPositionX, (int) propertyEditorRect.getX());
		userPrefs.putInt(PrefConstants.PropertyEditorPositionY, (int) propertyEditorRect.getY());
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

	private void saveWindows() {
		saveWindowBounds();
		saveRDFEditorBounds();
		saveClassEditorBounds();
		savePropertyEditorBounds();
	}

	public int confirmExitProject(String title) {
		int messageType =
			JOptionPane.showInternalConfirmDialog(
				desktop,
				"Save changes ?",
				"MR^3 - " + title,
				JOptionPane.YES_NO_CANCEL_OPTION,
				JOptionPane.INFORMATION_MESSAGE);
		if (messageType == JOptionPane.YES_OPTION) {
			saveProjectAs();
		}
		return messageType;
	}

	public void exitProgram() {
		int messageType = confirmExitProject("Exit Program"); // もっと適切なメソッド名にすべき
		if (messageType == JOptionPane.CANCEL_OPTION) {
			return;
		}
		saveWindows();
		System.exit(0);
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

	class ImportRealRDFAction extends AbstractAction {

		ImportRealRDFAction() {
			super("Real RDF");
		}

		public void actionPerformed(ActionEvent e) {
			File file = getFile(true, "rdf");
			if (file == null) {
				return;
			}
			realRDFEditor.importFile(file);
		}
	}

	public void newProject() {
		nsTableDialog.resetNSTable();
		attrDialog.setNullPanel();
		resInfoMap.clear();
		litInfoMap.clear();
		rdfsInfoMap.clear();
		gmanager.removeAllCells();
		nsTableDialog.setDefaultNSPrefix();
		setTitle("MR^3 - New Project");
		currentProject = null;
	}

	public void openProject() {
		try {
			ProjectManager pm = new ProjectManager(gmanager, nsTableDialog);
			gmanager.setIsImporting(true);
			Model model = readModel(getReader("mr3", "UTF8"), gmanager.getBaseURI());
			if (model == null) {
				return;
			}
			File tmp = currentProject;
			newProject();
			currentProject = tmp;
			// 順番が重要なので、よく考えること
			Model projectModel = pm.extractProjectModel(model);
			mr3Reader.mergeRDFS(model);
			nsTableDialog.setCurrentNSPrefix();
			pm.loadProject(projectModel);
			pm.removeEmptyClass();
			gmanager.removeTypeCells();
			gmanager.addTypeCells();
			gmanager.setIsImporting(false);
			setTitle("MR^3 - " + currentProject.getAbsolutePath());
		} catch (RDFException e1) {
			e1.printStackTrace();
		}
	}

	private ObjectOutputStream createOutputStream(File file) throws FileNotFoundException, IOException {
		OutputStream fo = new FileOutputStream(file);
		fo = new GZIPOutputStream(fo);
		return new ObjectOutputStream(fo);
	}

	public File getCurrentProject() {
		return currentProject;
	}

	public void saveProject(File file) {
		try {
			// 順番に注意．リテラルのモデルを抽出して，プロジェクトモデルを抽出してから
			// リテラルモデルを削除する
			ProjectManager pm = new ProjectManager(gmanager, nsTableDialog);
			Model exportModel = getRDFModel();
			exportModel.add(getRDFSModel());
			Model literalModel = pm.getLiteralModel(exportModel);
			exportModel.add(pm.getProjectModel());
			exportModel.remove(literalModel);
			Writer output = new OutputStreamWriter(new FileOutputStream(file), "UTF-8");
			RDFWriter writer = new RDFWriterFImpl().getWriter("RDF/XML-ABBREV");
			writeModel(exportModel, output, writer);
			setTitle("MR^3 - " + file.getAbsolutePath());
			currentProject = file;
		} catch (RDFException e1) {
			e1.printStackTrace();
		} catch (FileNotFoundException e2) {
			e2.printStackTrace();
		} catch (UnsupportedEncodingException e3) {
			e3.printStackTrace();
		}
	}

	public void saveProjectAs() {
		File file = getFile(false, "mr3");
		if (file == null) {
			return;
		}
		saveProject(file);
		currentProject = file;
	}

	private static MR3FileFilter mr3FileFilter = new MR3FileFilter();
	private static RDFsFileFilter rdfsFileFilter = new RDFsFileFilter();
	private static NTripleFileFilter n3FileFilter = new NTripleFileFilter();

	private File getFile(boolean isOpenFile, String extension) {
		JFileChooser jfc = new JFileChooser(userPrefs.get(PrefConstants.DefaultWorkDirectory, ""));
		if (extension.equals("mr3")) {
			jfc.setFileFilter(mr3FileFilter);
		} else if (extension.equals("n3")) {
			jfc.setFileFilter(n3FileFilter);
		} else {
			jfc.setFileFilter(rdfsFileFilter);
		}

		if (isOpenFile) {
			if (jfc.showOpenDialog(desktop) == JFileChooser.APPROVE_OPTION) {
				return jfc.getSelectedFile();
			} else {
				return null;
			}
		} else {
			if (jfc.showSaveDialog(desktop) == JFileChooser.APPROVE_OPTION) {
				String defaultPath = jfc.getSelectedFile().getAbsolutePath();
				if (extension.equals("mr3")) {
					return new File(complementMR3Extension(defaultPath, extension));
				} else {
					return new File(complementRDFsExtension(defaultPath, extension));
				}
			} else {
				return null;
			}
		}
	}

	private String complementMR3Extension(String tmp, String extension) {
		String ext = (extension != null) ? "." + extension.toLowerCase() : "";
		if (extension != null && !tmp.toLowerCase().endsWith(".mr3")) {
			tmp += ext;
		}
		return tmp;
	}

	private String complementRDFsExtension(String tmp, String extension) {
		String ext = (extension != null) ? "." + extension.toLowerCase() : "";
		if (extension != null && !tmp.toLowerCase().endsWith(".rdf") && !tmp.toLowerCase().endsWith(".rdfs") && !tmp.toLowerCase().endsWith(".n3")) {
			tmp += ext;
		}
		return tmp;
	}

	private URL getURI(String uri) throws MalformedURLException, UnknownHostException {
		URL rdfURI = null;
		boolean isProxy = userPrefs.getBoolean("Proxy", false);
		if (isProxy) {
			String proxyURL = userPrefs.get(PrefConstants.ProxyHost, "http://localhost");
			int proxyPort = userPrefs.getInt(PrefConstants.ProxyPort, 8080);
			rdfURI = new URL("http", proxyURL, proxyPort, uri);
		} else {
			rdfURI = new URL(uri);
		}
		return rdfURI;
	}

	// encodingの指定ができないので，却下．
	//	private Model loadModel(String ext, String lang) {
	//		File file = getFile(true, ext);
	//		if (file == null) {
	//			return null;
	//		}
	//		Model model = ModelLoader.loadModel(file.getAbsolutePath(), lang);	
	//		return model;
	//	}

	private Reader getReader(String uri) {
		if (uri == null) {
			return null;
		}
		URL rdfURI = null;
		try {
			rdfURI = getURI(uri);
			String encoding = userPrefs.get(PrefConstants.InputEncoding, "SJIS");
			Reader reader = new InputStreamReader(rdfURI.openStream(), encoding);
			return reader;
		} catch (UnknownHostException uhe) {
			JOptionPane.showInternalMessageDialog(desktop, "Unknown Host(Proxy)", "Warning", JOptionPane.ERROR_MESSAGE);
		} catch (MalformedURLException uriex) {
			uriex.printStackTrace();
		} catch (IOException ioe) {
			JOptionPane.showInternalMessageDialog(desktop, "File Not Found.", "Warning", JOptionPane.ERROR_MESSAGE);
		}

		return null;
	}

	private Reader getReader(String ext, String encoding) {
		File file = getFile(true, ext);
		if (file == null) {
			return null;
		}
		if (ext.equals("mr3")) {
			currentProject = file;
		}
		try {
			if (encoding == null) {
				encoding = userPrefs.get(PrefConstants.InputEncoding, "SJIS");
			}
			Reader reader = new InputStreamReader(new FileInputStream(file), encoding);
			return reader;
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		return null;
	}

	public Model readModel(Reader r, String xmlbase) {
		if (r == null) {
			return null;
		}
		Model model = new ModelMem();
		RDFReader reader = new JenaReader();
		try {
			reader.read(model, r, xmlbase);
		} catch (RDFException e) {
			e.printStackTrace();
		}
		return model;
	}

	public void replaceRDFModel(Model model) {
		mr3Reader.replaceRDF(model);
	}

	class ReplaceRDFAction extends AbstractAction {

		ReplaceRDFAction(String title) {
			super(title);
		}

		public void actionPerformed(ActionEvent e) {
			Model model = null;
			gmanager.setIsImporting(true);
			if (e.getActionCommand().equals(REPLACE_RDF_FILE)) {
				model = readModel(getReader("rdf", null), gmanager.getBaseURI());
			} else if (e.getActionCommand().equals(REPLACE_RDF_URI)) {
				String uri = JOptionPane.showInternalInputDialog(desktop, "Open URI");
				model = readModel(getReader(uri), gmanager.getBaseURI());
			}
			mr3Reader.replaceRDF(model);
			nsTableDialog.setCurrentNSPrefix();
			gmanager.setIsImporting(false);
		}
	}

	public void mergeRDFModel(Model model) {
		mr3Reader.mergeRDF(model);
	}

	public void mergeRDFSModel(Model model) {
		mr3Reader.mergeRDFS(model);
	}

	class MergeRDFsAction extends AbstractAction {

		MergeRDFsAction(String title) {
			super(title);
		}

		public void actionPerformed(ActionEvent e) {
			gmanager.setIsImporting(true);
			Model model = null;
			if (e.getActionCommand().equals(MERGE_RDFS_FILE)) {
				model = readModel(getReader("rdfs", null), gmanager.getBaseURI());
			} else if (e.getActionCommand().equals(MERGE_RDFS_URI)) {
				String uri = JOptionPane.showInternalInputDialog(desktop, "Open URI");
				model = readModel(getReader(uri), gmanager.getBaseURI());
			}
			mr3Reader.mergeRDFS(model);
			nsTableDialog.setCurrentNSPrefix();
			gmanager.setIsImporting(false);
		}
	}

	class ImportProjectAction extends AbstractAction {

		ImportProjectAction() {
			super(PROJECT);
		}

		private ObjectInputStream createInputStream(File file) throws FileNotFoundException, IOException {
			InputStream fi = new FileInputStream(file);
			fi = new GZIPInputStream(fi);
			return new ObjectInputStream(fi);
		}

		public void openProject(File file) {
			try {
				newProject();
				ObjectInputStream oi = createInputStream(file);
				Object obj = oi.readObject();
				if (obj instanceof ArrayList) {
					ArrayList list = (ArrayList) obj;
					int index = gmanager.loadState(list);
					nsTableDialog.loadState((List) list.get(index));
				}
				oi.close();
				setTitle("MR^3 - " + file.getAbsolutePath());
				currentProject = file;
			} catch (IOException ex) {
				ex.printStackTrace();
			} catch (ClassNotFoundException cnfe) {
				cnfe.printStackTrace();
			}
		}

		public void actionPerformed(ActionEvent e) {
			File file = getFile(true, "mr3");
			if (file == null) {
				return;
			}
			openProject(file);
		}
	}

	class ExportProjectAction extends AbstractAction {

		ExportProjectAction() {
			super(PROJECT);
		}

		public void actionPerformed(ActionEvent e) {
			File file = getFile(false, "mr3");
			if (file == null) {
				return;
			}
			try {
				ObjectOutputStream oo = createOutputStream(file);
				ArrayList list = gmanager.storeState();
				list.add(nsTableDialog.getState());
				oo.writeObject(list);
				oo.flush();
				oo.close();
				setTitle("MR^3 - " + file.getAbsolutePath());
				currentProject = file;
			} catch (FileNotFoundException fne) {
				fne.printStackTrace();
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}
		}
	}

	class ExportRDFSAction extends AbstractAction {

		ExportRDFSAction(String title) {
			super(title);
		}

		public void actionPerformed(ActionEvent e) {
			String type = e.getActionCommand();
			String ext = "rdfs";
			if (type.equals(RDFS_NTriple) || type.equals(SelectedRDFS_NTriple)) {
				ext = "n3";
			}
			File file = getFile(false, ext);
			if (file == null) {
				return;
			}
			try {
				String encoding = userPrefs.get(PrefConstants.OutputEncoding, "EUC_JP");
				Writer output = new OutputStreamWriter(new FileOutputStream(file), encoding);
				RDFWriter writer = new RDFWriterFImpl().getWriter("RDF/XML-ABBREV");
				if (type.equals(RDFS_NTriple) || type.equals(SelectedRDFS_NTriple)) {
					writer = new RDFWriterFImpl().getWriter("N-TRIPLE");
				}

				if (type.equals(SelectedRDFS_XML) || type.equals(SelectedRDFS_NTriple)) {
					writeModel(getSelectedRDFSModel(), output, writer);
				} else {
					writeModel(getRDFSModel(), output, writer);
				}
			} catch (RDFException re) {
				re.printStackTrace();
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}
		}
	}

	class ExportRDFAction extends AbstractAction {

		ExportRDFAction(String title) {
			super(title);
		}

		public void actionPerformed(ActionEvent e) {
			String type = e.getActionCommand();
			String ext = "rdf";
			if (type.equals(RDF_NTriple) || type.equals(SelectedRDF_NTriple)) {
				ext = "n3";
			}
			File file = getFile(false, ext);
			if (file == null) {
				return;
			}
			try {
				String encoding = userPrefs.get(PrefConstants.OutputEncoding, "EUC_JP");
				Writer output = new OutputStreamWriter(new FileOutputStream(file), encoding);
				RDFWriter writer = new RDFWriterFImpl().getWriter("RDF/XML-ABBREV");
				if (type.equals(RDF_NTriple) || type.equals(SelectedRDF_NTriple)) {
					writer = new RDFWriterFImpl().getWriter("N-TRIPLE");
				}
				if (type.equals(SelectedRDF_XML) || type.equals(SelectedRDF_NTriple)) {
					writeModel(getSelectedRDFModel(), output, writer);
				} else {
					writeModel(getRDFModel(), output, writer);
				}
				output.close();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}

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

	public void deployWindows() {
		try {
			int width = desktop.getWidth();
			int height = desktop.getHeight();
			internalFrames[0].setBounds(new Rectangle(0, height / 2, width, height / 2)); // RDF
			internalFrames[0].setIcon(false);
			internalFrames[1].setBounds(new Rectangle(0, 0, width / 2, height / 2)); // Class
			internalFrames[1].setIcon(false);
			internalFrames[2].setBounds(new Rectangle(width / 2, 0, width / 2, height / 2)); //Property
			internalFrames[2].setIcon(false);
		} catch (PropertyVetoException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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

	protected void setNsPrefix(RDFWriter writer) {
		Set prefixNsInfoSet = gmanager.getPrefixNSInfoSet();
		for (Iterator i = prefixNsInfoSet.iterator(); i.hasNext();) {
			PrefixNSInfo info = (PrefixNSInfo) i.next();
			if (info.isAvailable()) {
				writer.setNsPrefix(info.getPrefix(), info.getNameSpace());
			}
		}
	}

	public JTextComponent getSourceArea() {
		return srcArea;
	}
	
	public Writer writeModel(Model model, Writer output, RDFWriter writer) {
		try {
			setNsPrefix(writer);
			String baseURI = gmanager.getBaseURI().replaceAll("#", "");
			writer.write(model, output, baseURI);
		} catch (RDFException e) {
			e.printStackTrace();
		}
		return output;
	}

	public static void main(String[] arg) {
		new MR3("MR^3 - New Project");
	}
}
