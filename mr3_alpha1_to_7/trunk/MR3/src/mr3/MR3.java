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

import mr3.data.*;
import mr3.editor.*;
import mr3.editor.PropertyEditor;
import mr3.io.*;
import mr3.jgraph.*;
import mr3.ui.*;
import mr3.util.*;

import com.hp.hpl.jena.rdf.arp.*;
import com.hp.hpl.mesa.rdf.jena.common.*;
import com.hp.hpl.mesa.rdf.jena.mem.*;
import com.hp.hpl.mesa.rdf.jena.model.*;
import com.jgraph.graph.*;

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

	private JCheckBoxMenuItem showTypeCell;
	private JCheckBoxMenuItem selectAbstractLevelMode;
	private JCheckBoxMenuItem showToolTips;
	private JInternalFrame[] internalFrames = new JInternalFrame[3];
	private JInternalFrame srcFrame;
	private JCheckBoxMenuItem rdfEditorView;
	private JCheckBoxMenuItem classEditorView;
	private JCheckBoxMenuItem propertyEditorView;
	private JCheckBoxMenuItem showSrcView;
	//	private JCheckBoxMenuItem lightView;
	
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
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				exitProgram();
			}
		});

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
				showSrcView.setSelected(false);
			}
			tmp.setVisible(false);
		}
	}

	private void setLookAndFeel() {
		try {
			UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
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

	private JMenu getEditMenu() {
		JMenu menu = new JMenu("Edit");
		JMenuItem mi = new JMenuItem("Find Resource");
		mi.addActionListener(new FindAction());
		menu.add(mi);
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
		mi = new JMenuItem("Select all nodes");
		mi.addActionListener(new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				gmanager.selectAllNodes();
			}
		});
		selectMenu.add(mi);
		mi = new JMenuItem("Select all RDF nodes");
		mi.addActionListener(new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				gmanager.selectAllRDFNodes();
			}
		});
		selectMenu.add(mi);
		mi = new JMenuItem("Select all RDFS class nodes");
		mi.addActionListener(new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				gmanager.selectAllClassNodes();
			}
		});
		selectMenu.add(mi);
		mi = new JMenuItem("Select all RDFS property nodes");
		mi.addActionListener(new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				gmanager.selectAllPropertyNodes();
			}
		});
		selectMenu.add(mi);
		menu.add(selectMenu);
		menu.addSeparator();
		mi = new JMenuItem("Preference");
		mi.addActionListener(new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				prefDialog.setVisible(true);
			}
		});
		menu.add(mi);

		return menu;
	}

	private static final String RDFS_XML = "RDFS/XML";
	private static final String RDFS_NTriple = "RDFS/N-Triple";
	private static final String RDF_XML = "RDF/XML";
	private static final String RDF_NTriple = "RDF/N-Triple";
	private static final String SelectedRDFS_XML = "Selected RDFS/XML";
	private static final String SelectedRDFS_NTriple = "Selected RDFS/N-Triple";
	private static final String SelectedRDF_XML = "Selected RDF/XML";
	private static final String SelectedRDF_NTriple = "Selected RDF/N-Triple";

	private JMenu getFileMenu() {
		JMenu menu = new JMenu("File");
		JMenuItem mi = new JMenuItem(" New Project");
		mi.addActionListener(new NewProjectAction());
		menu.add(mi);
		mi = new JMenuItem("Open Project");
		mi.addActionListener(new OpenProjectAction());
		menu.add(mi);
		mi = new JMenuItem("Save Project");
		mi.addActionListener(new SaveProjectAction());
		menu.add(mi);
		mi = new JMenuItem("Save Project As");
		mi.addActionListener(new SaveProjectAction());
		menu.add(mi);
		menu.addSeparator();
		JMenu importRDF = new JMenu("Import");
		JMenu replace = new JMenu("Replace");

		mi = new JMenuItem("RDF/XML (File)");
		mi.addActionListener(new ReplaceRDFFileAction());
		replace.add(mi);
		mi = new JMenuItem("RDF/XML (URI)");
		mi.addActionListener(new ReplaceRDFURIAction());
		replace.add(mi);
		importRDF.add(replace);

		JMenu mergeMenu = new JMenu("Merge");
		mi = new JMenuItem("RDF(S)/XML (File)");
		mi.addActionListener(new MergeRDFSFileAction());
		mergeMenu.add(mi);
		mi = new JMenuItem("RDF(S)/XML (URI)");
		mi.addActionListener(new MergeRDFSURIAction());
		mergeMenu.add(mi);
		importRDF.add(mergeMenu);

		//		mi = new JMenuItem("Real RDF");
		//		mi.addActionListener(new ImportRealRDFAction());
		//		importRDF.add(mi);
		menu.add(importRDF);

		JMenu exportMenu = new JMenu("Export");
		menu.add(exportMenu);
		mi = new JMenuItem(RDFS_XML);
		mi.addActionListener(new ExportRDFSAction());
		exportMenu.add(mi);
		mi = new JMenuItem(RDFS_NTriple);
		mi.addActionListener(new ExportRDFSAction());
		exportMenu.add(mi);
		mi = new JMenuItem(RDF_XML);
		mi.addActionListener(new ExportRDFAction());
		exportMenu.add(mi);
		mi = new JMenuItem(RDF_NTriple);
		mi.addActionListener(new ExportRDFAction());
		exportMenu.add(mi);

		JMenu selectedMenu = new JMenu("Selected");
		exportMenu.add(selectedMenu);
		mi = new JMenuItem(SelectedRDFS_XML);
		mi.addActionListener(new ExportRDFSAction());
		selectedMenu.add(mi);
		mi = new JMenuItem(SelectedRDFS_NTriple);
		mi.addActionListener(new ExportRDFSAction());
		selectedMenu.add(mi);
		mi = new JMenuItem(SelectedRDF_XML);
		mi.addActionListener(new ExportRDFAction());
		selectedMenu.add(mi);
		mi = new JMenuItem(SelectedRDF_NTriple);
		mi.addActionListener(new ExportRDFAction());
		selectedMenu.add(mi);

		menu.addSeparator();
		menu.add(getPluginMenus()); // JavaWebStartでは，pluginは使用できないと思われる．
		menu.addSeparator();

		mi = new JMenuItem("Exit");
		mi.addActionListener(new ExitAction());
		menu.add(mi);

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

	private int confirmExitProject(String title) {
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

	private void exitProgram() {
		int messageType = confirmExitProject("Exit Program"); // もっと適切なメソッド名にすべき
		if (messageType == JOptionPane.CANCEL_OPTION) {
			return;
		}
		saveWindows();
		System.exit(0);
	}

	class ExitAction extends AbstractAction {
		public void actionPerformed(ActionEvent e) {
			exitProgram();
		}
	}

	private JMenu getPluginMenus() {
		JMenu menu = new JMenu("Plugins");
		Map pluginMenuMap = PluginLoader.getPluginMenuMap();
		Set keys = pluginMenuMap.keySet();
		AbstractAction pluginAction = new PluginAction();
		for (Iterator i = keys.iterator(); i.hasNext();) {
			String menuName = (String) i.next();
			JMenuItem item = new JMenuItem(menuName);
			item.addActionListener(pluginAction);
			menu.add(item);
		}
		return menu;
	}

	private static final String PLUGIN_METHOD_NAME = "exec";

	class PluginAction extends AbstractAction {
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
		public void actionPerformed(ActionEvent e) {
			File file = getFile(true, "rdf");
			if (file == null) {
				return;
			}
			realRDFEditor.importFile(file);
		}
	}

	private void newProject() {
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

	class NewProjectAction extends AbstractAction {
		public void actionPerformed(ActionEvent e) {
			int messageType = confirmExitProject("New Project");
			if (messageType != JOptionPane.CANCEL_OPTION) {
				newProject();
			}
		}
	}

	class OpenProjectAction extends AbstractAction {

		private ObjectInputStream createInputStream(File file) throws FileNotFoundException, IOException {
			InputStream fi = new FileInputStream(file);
			fi = new GZIPInputStream(fi);
			return new ObjectInputStream(fi);
		}

		public void openProject(File file) {
			try {
				if (file != null) {
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
				}
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

	private ObjectOutputStream createOutputStream(File file) throws FileNotFoundException, IOException {
		OutputStream fo = new FileOutputStream(file);
		fo = new GZIPOutputStream(fo);
		return new ObjectOutputStream(fo);
	}

	private void saveProject(File file) {
		try {
			ObjectOutputStream oo = createOutputStream(file);
			ArrayList list = gmanager.storeState();
			list.add(nsTableDialog.getState());
			oo.writeObject(list);
			oo.flush();
			oo.close();
			setTitle("MR^3 - " + file.getAbsolutePath());
		} catch (FileNotFoundException fne) {
			fne.printStackTrace();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}

	private void saveProjectAs() {
		File file = getFile(false, "mr3");
		if (file == null) {
			return;
		}
		saveProject(file);
		currentProject = file;
	}

	class SaveProjectAction extends AbstractAction {
		public void actionPerformed(ActionEvent e) {
			if (e.getActionCommand().equals("Save Project")) {
				if (currentProject == null) {
					saveProjectAs();
				} else {
					saveProject(currentProject);
				}
			} else {
				saveProjectAs();
			}
		}
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
			jfc.showOpenDialog(desktop);
		} else {
			jfc.showSaveDialog(desktop);
			if (jfc.getSelectedFile() != null) {
				String defaultPath = jfc.getSelectedFile().getAbsolutePath();
				if (extension.equals("mr3")) {
					return new File(complementMR3Extension(defaultPath, extension));
				} else {
					return new File(complementRDFsExtension(defaultPath, extension));
				}
			}
		}
		return jfc.getSelectedFile();
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

	private Reader getReader(String uri, String ext) {
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
			ioe.printStackTrace();
		}

		return null;
	}

	private Reader getReader(String ext) {
		File file = getFile(true, ext);
		if (file == null) {
			return null;
		}
		try {
			String encoding = userPrefs.get(PrefConstants.InputEncoding, "SJIS");
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
		rdfEditor.fitWindow();
	}

	class ReplaceRDFFileAction extends AbstractAction {
		public void actionPerformed(ActionEvent e) {
			Model model = readModel(getReader("rdf"), gmanager.getBaseURI());
			gmanager.setIsImporting(true);
			mr3Reader.replaceRDF(model);
			rdfEditor.fitWindow();
			gmanager.setIsImporting(false);
		}
	}

	class ReplaceRDFURIAction extends AbstractAction {
		public void actionPerformed(ActionEvent e) {
			gmanager.setIsImporting(true);
			String uri = JOptionPane.showInternalInputDialog(desktop, "Open URI");
			Model model = readModel(getReader(uri, "rdf"), gmanager.getBaseURI());
			mr3Reader.replaceRDF(model);
			rdfEditor.fitWindow();
			gmanager.setIsImporting(false);
		}
	}

	public void mergeRDFModel(Model model) {
		mr3Reader.mergeRDF(model);
	}

	public void mergeRDFSModel(Model model) {
		mr3Reader.mergeRDFS(model);
	}

	class MergeRDFSFileAction extends AbstractAction {
		public void actionPerformed(ActionEvent e) {
			gmanager.setIsImporting(true);
			Model model = readModel(getReader("rdfs"), gmanager.getBaseURI());
			mr3Reader.mergeRDFS(model);
			gmanager.setIsImporting(false);
		}
	}

	class MergeRDFSURIAction extends AbstractAction {
		public void actionPerformed(ActionEvent e) {
			gmanager.setIsImporting(true);
			String uri = JOptionPane.showInternalInputDialog(desktop, "Open URI");
			Model model = readModel(getReader(uri, "rdfs"), gmanager.getBaseURI());
			mr3Reader.mergeRDFS(model);
			gmanager.setIsImporting(false);
		}
	}

	class ExportRDFSAction implements ActionListener {
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
					rdfEditor.writeModel(getSelectedRDFSModel(), output, writer);
				} else {
					rdfEditor.writeModel(getRDFSModel(), output, writer);
				}
			} catch (RDFException re) {
				re.printStackTrace();
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}
		}
	}

	class ExportRDFAction implements ActionListener {
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
					rdfEditor.writeModel(getSelectedRDFModel(), output, writer);
				} else {
					rdfEditor.writeModel(getRDFModel(), output, writer);
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
		uriView.addItemListener(changeCellViewAction);
		idView = new JRadioButton("ID View");
		idView.addItemListener(changeCellViewAction);
		idView.setSelected(true);
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
		showSrcView = new JCheckBoxMenuItem("Show Source Window", false);
		showSrcView.addActionListener(new ShowViewAction());
		menu.add(showSrcView);
		menu.add(attrDialog.getShowPropWindow());
		menu.add(nsTableDialog.getShowNSTable());
		//		showTypeCell = new JCheckBoxMenuItem("Show Type", true);
		//		showTypeCell.addActionListener(new ShowTypeCellAction());
		//		menu.add(showTypeCell);
		showToolTips = new JCheckBoxMenuItem("Show ToolTips", false);
		showToolTips.addActionListener(new ShowToolTipsAction());
		ToolTipManager.sharedInstance().setEnabled(false);
		menu.add(showToolTips);
		//		lightView = new JCheckBoxMenuItem("Color Mode", false);
		//		lightView.addActionListener(new LightViewAction());
		//		menu.add(lightView);

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

	private JMenu getWindowMenu() {
		JMenu menu = new JMenu("Window");
		AbstractAction editorSelectAction = new EditorSelectAction();
		JMenuItem item = new JMenuItem("To Front RDF Editor");
		item.addActionListener(editorSelectAction);
		menu.add(item);
		item = new JMenuItem("To Front Class Editor");
		item.addActionListener(editorSelectAction);
		menu.add(item);
		item = new JMenuItem("To Front Property Editor");
		item.addActionListener(editorSelectAction);
		menu.add(item);
		menu.addSeparator();
		item = new JMenuItem("Deploy Windows");
		item.addActionListener(new DeployWindows());
		menu.add(item);

		return menu;
	}

	class EditorSelectAction extends AbstractAction {

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
			if (en.equals("To Front RDF Editor")) {
				toFrontInternalFrame(0);
			} else if (en.equals("To Front Class Editor")) {
				toFrontInternalFrame(1);
			} else if (en.equals("To Front Property Editor")) {
				toFrontInternalFrame(2);
			}
		}

	}

	private void deployWindows() {
		int width = desktop.getWidth();
		int height = desktop.getHeight();
		internalFrames[0].setBounds(new Rectangle(0, height / 2, width, height / 2)); // RDF
		internalFrames[1].setBounds(new Rectangle(0, 0, width / 2, height / 2)); // Class
		internalFrames[2].setBounds(new Rectangle(width / 2, 0, width / 2, height / 2)); //Property
	}

	class DeployWindows extends AbstractAction {
		public void actionPerformed(ActionEvent e) {
			deployWindows();
		}
	}

	class ShowViewAction extends AbstractAction {
		public void actionPerformed(ActionEvent e) {
			JCheckBoxMenuItem tmp = (JCheckBoxMenuItem) e.getSource();
			if (tmp == rdfEditorView) {
				internalFrames[0].setVisible(rdfEditorView.getState());
			} else if (tmp == classEditorView) {
				internalFrames[1].setVisible(classEditorView.getState());
			} else if (tmp == propertyEditorView) {
				internalFrames[2].setVisible(propertyEditorView.getState());
			} else if (tmp == showSrcView) {
				srcFrame.setVisible(showSrcView.getState());
			}
		}
	}

	//	cass LightViewAction extends AbstractAction {
	//	public void actionPerformed(ActionEvent e) {	
	//		
	//		}
	//	}

	private JMenu getConvertMenu() {
		JMenu menu = new JMenu("Convert");

		JMenu rdfView = new JMenu("RDF");
		menu.add(rdfView);
		JMenuItem mi = new JMenuItem("RDF/XML");
		mi.addActionListener(new JGraphToRDFAction());
		rdfView.add(mi);
		mi = new JMenuItem("RDF/N-Triple");
		mi.addActionListener(new JGraphToNTripleAction());
		rdfView.add(mi);

		JMenu rdfsView = new JMenu("RDFS");
		menu.add(rdfsView);
		mi = new JMenuItem("RDFS(Class/Property)/XML");
		mi.addActionListener(new JGraphToRDFSAction());
		rdfsView.add(mi);
		mi = new JMenuItem("RDFS(Class)/XML");
		mi.addActionListener(new JGraphToClassAction());
		rdfsView.add(mi);
		mi = new JMenuItem("RDFS(Property)/XML");
		mi.addActionListener(new JGraphToPropertyAction());
		rdfsView.add(mi);

		JMenu selectedRDFView = new JMenu("Selected RDF");
		menu.add(selectedRDFView);
		mi = new JMenuItem("Selected RDF/XML");
		mi.addActionListener(new JGraphToSelectedRDFAction());
		selectedRDFView.add(mi);
		mi = new JMenuItem("Selected RDF/N-Triple");
		mi.addActionListener(new JGraphToSelectedNTripleAction());
		selectedRDFView.add(mi);

		JMenu selectedRDFSView = new JMenu("Selected RDFS");
		menu.add(selectedRDFSView);
		mi = new JMenuItem("Selected RDFS(Class/Property)/XML");
		mi.addActionListener(new JGraphToRDFSAction());
		selectedRDFSView.add(mi);
		mi = new JMenuItem("Selected RDFS(Class)/XML");
		mi.addActionListener(new JGraphToSelectedClassAction());
		selectedRDFSView.add(mi);
		mi = new JMenuItem("Selected RDFS(Property)/XML");
		mi.addActionListener(new JGraphToSelectedPropertyAction());
		selectedRDFSView.add(mi);

		return menu;
	}

	private JMenu getHelpMenu() {
		JMenu menu = new JMenu("Help");
		JMenuItem mi = new JMenuItem("About MR^3");
		mi.addActionListener(new HelpAboutAction());
		menu.add(mi);
		return menu;
	}

	class HelpAboutAction extends AbstractAction {
		public void actionPerformed(ActionEvent e) {
			URL logoUrl = this.getClass().getClassLoader().getResource("mr3/resources/mr3_logo.png");
			new HelpDialog(new ImageIcon(logoUrl));
		}
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

	class FindAction implements ActionListener {
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
			RDFGraph rdfGraph = gmanager.getRDFGraph();
			GraphLayoutCache graphLayoutCache = rdfGraph.getGraphLayoutCache();
			Object[] rdfCells = rdfGraph.getAllCells();
			List typeList = new ArrayList();
			for (int i = 0; i < rdfCells.length; i++) {
				GraphCell cell = (GraphCell) rdfCells[i];
				if (rdfGraph.isTypeCell(cell)) {
					typeList.add(cell);
					//					rdfGraph.getGraphLayoutCache().setVisible(cell, showTypeCell.getState());
				}
			}
			graphLayoutCache.setVisible(typeList.toArray(), showTypeCell.getState());
		}
	}

	private void showSrcView() {
		try {
			srcFrame.toFront();
			srcFrame.setVisible(true);
			srcFrame.setIcon(false);
			showSrcView.setState(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	class JGraphToRDFAction extends AbstractAction {
		public void actionPerformed(ActionEvent e) {
			rdfEditor.convertRDFSRC(srcArea, false);
			showSrcView();
		}
	}

	class JGraphToSelectedRDFAction extends AbstractAction {
		public void actionPerformed(ActionEvent e) {
			rdfEditor.convertRDFSRC(srcArea, true);
			showSrcView();
		}
	}

	class JGraphToNTripleAction extends AbstractAction {
		public void actionPerformed(ActionEvent e) {
			rdfEditor.convertNTripleSRC(srcArea, false);
			showSrcView();
		}
	}

	class JGraphToSelectedNTripleAction extends AbstractAction {
		public void actionPerformed(ActionEvent e) {
			rdfEditor.convertNTripleSRC(srcArea, true);
			showSrcView();
		}
	}

	class JGraphToClassAction extends AbstractAction {
		public void actionPerformed(ActionEvent e) {
			classEditor.convertSRC(srcArea, false);
			showSrcView();
		}
	}

	class JGraphToSelectedClassAction extends AbstractAction {
		public void actionPerformed(ActionEvent e) {
			classEditor.convertSRC(srcArea, true);
			showSrcView();
		}
	}

	class JGraphToPropertyAction extends AbstractAction {
		public void actionPerformed(ActionEvent e) {
			propertyEditor.convertSRC(srcArea, false);
			showSrcView();
		}
	}

	class JGraphToSelectedPropertyAction extends AbstractAction {
		public void actionPerformed(ActionEvent e) {
			propertyEditor.convertSRC(srcArea, true);
			showSrcView();
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

	class JGraphToRDFSAction extends AbstractAction {
		public void actionPerformed(ActionEvent e) {
			String command = e.getActionCommand();
			try {
				Model model = null;
				if (command.equals("RDFS(Class/Property)/XML")) {
					model = getRDFSModel();
				} else {
					model = getSelectedRDFSModel();
				}

				Writer output = new StringWriter();
				RDFWriter writer = new RDFWriterFImpl().getWriter("RDF/XML-ABBREV");
				rdfEditor.writeModel(model, output, writer);
				srcArea.setText(output.toString());
				showSrcView();
			} catch (RDFException rex) {
				rex.printStackTrace();
			}
		}
	}

	public static void main(String[] arg) {
		new MR3("MR^3 - New Project");
	}
}
