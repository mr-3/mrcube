package mr3;
import java.awt.*;
import java.awt.Container;
import java.awt.event.*;
import java.io.*;
import java.lang.reflect.*;
import java.net.*;
import java.util.*;
import java.util.List;
import java.util.prefs.*;

import javax.swing.*;
import javax.swing.event.*;

import mr3.data.*;
import mr3.editor.*;
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
  *   Meta-  Model Management founded on RDF-based Revision Reflection  
  *   {MR} ^3
  */
public class MR3 extends JFrame {

	private JDesktopPane desktop;
	private static final int MAIN_FRAME_HEIGHT = 600;
	private static final int MAIN_FRAME_WIDTH = 800;
	private static final int FRAME_HEIGHT = 400;
	private static final int FRAME_WIDTH = 600;
	private static final Integer DEMO_FRAME_LAYER = new Integer(0);

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
	private SearchResourceDialog searchResDialog;
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
	private JRadioButton labelView;

	private File lastSelectedFile;

	private static final Color DESKTOP_BACK_COLOR = new Color(225, 225, 225);

	private Preferences userPrefs; // ユーザの設定を保存(Windowサイズなど）
	private static ResourceBundle resources;

	private MR3 obj_for_plugin;

	MR3(String title) {
		super(title);

		userPrefs = Preferences.userNodeForPackage(this.getClass());

		setSize(userPrefs.getInt(PrefConstants.WindowWidth, MAIN_FRAME_WIDTH), userPrefs.getInt(PrefConstants.WindowHeight, MAIN_FRAME_HEIGHT));
		setLocation(userPrefs.getInt(PrefConstants.WindowPositionX, 50), userPrefs.getInt(PrefConstants.WindowPositionY, 50));
		//setLookAndFeel();

		attrDialog = new AttributeDialog();
		gmanager = new GraphManager(attrDialog, userPrefs);

		rdfEditor = new RDFEditor(attrDialog, gmanager);
		//		realRDFEditor = new RealRDFEditor(propDialog, gmanager);
		classEditor = new ClassEditor(attrDialog, gmanager);
		propertyEditor = new PropertyEditor(attrDialog, gmanager);
		srcArea = new JTextArea();
		srcArea.setEditable(false);

		mr3Reader = new MR3Reader(gmanager);
		mr3Writer = new MR3Writer(gmanager);

		desktop = new JDesktopPane();
		desktop.add(attrDialog, JLayeredPane.MODAL_LAYER);
		searchResDialog = new SearchResourceDialog("Search Resource", gmanager);
		desktop.add(searchResDialog, JLayeredPane.MODAL_LAYER);
		nsTableDialog = new NameSpaceTableDialog(gmanager);
		desktop.add(nsTableDialog, JLayeredPane.MODAL_LAYER);
		desktop.add(gmanager.getRefDialog(), JLayeredPane.MODAL_LAYER);

		prefDialog = new PrefDialog(gmanager, userPrefs);
		prefDialog.setVisible(false);
		desktop.add(prefDialog, JLayeredPane.MODAL_LAYER);

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

		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				saveWindows();
				System.exit(0);
			}
		});
		setVisible(true);
		loadWindows();
	}

	private void createInternalFrames() {
		internalFrames[0] = createInternalFrame(rdfEditor, "RDF Editor", DEMO_FRAME_LAYER);
		internalFrames[0].setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		internalFrames[0].addInternalFrameListener(new CloseInternalFrameAction());

		//		createInternalFrame(realRDFEditor, "Real RDF Editor", DEMO_FRAME_LAYER);
		internalFrames[1] = createInternalFrame(classEditor, "Class Editor", DEMO_FRAME_LAYER);
		internalFrames[1].setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		internalFrames[1].addInternalFrameListener(new CloseInternalFrameAction());

		internalFrames[2] = createInternalFrame(propertyEditor, "Property Editor", DEMO_FRAME_LAYER);
		internalFrames[2].setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		internalFrames[2].addInternalFrameListener(new CloseInternalFrameAction());

		srcFrame = createInternalFrame(new JScrollPane(srcArea), "Src View", DEMO_FRAME_LAYER);
		srcFrame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		srcFrame.addInternalFrameListener(new CloseInternalFrameAction());
		srcFrame.setBounds(new Rectangle(0, 0, FRAME_WIDTH, FRAME_HEIGHT));
		srcFrame.setVisible(false);
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
			System.out.println("Error L&F Setting");
		}
	}

	public JInternalFrame createInternalFrame(Container container, String title, Integer layer) {
		JInternalFrame jif = new JInternalFrame(title);
		jif.setClosable(true);
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

	protected void setIcon() {
		URL jgraphUrl = this.getClass().getClassLoader().getResource("mr3/resources/mr3_icon.gif");
		if (jgraphUrl != null) { // If Valid URL
			ImageIcon jgraphIcon = new ImageIcon(jgraphUrl); // Load Icon
			setIconImage(jgraphIcon.getImage()); // Use in Window
		}
	}

	protected JMenuBar createMenuBar() {
		JMenuBar mb = new JMenuBar();
		mb.add(getFileMenu());
		mb.add(getEditMenu());
		mb.add(getViewMenu());
		mb.add(getConvertMenu());
		mb.add(getToolMenu());
		return mb;
	}

	protected JMenu getEditMenu() {
		JMenu menu = new JMenu("Edit");
		JMenu selectMenu = new JMenu("Select");
		JMenuItem mi = new JMenuItem("Select all nodes");
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

	protected JMenu getFileMenu() {
		JMenu menu = new JMenu("File");
		JMenuItem mi = new JMenuItem("New Project");
		mi.addActionListener(new NewAction());
		menu.add(mi);
		mi = new JMenuItem("Open Project");
		mi.addActionListener(new OpenProjectAction());
		mi.setEnabled(false);
		menu.add(mi);
		mi = new JMenuItem("Save Project");
		mi.addActionListener(new SaveProjectAction());
		mi.setEnabled(false);
		menu.add(mi);
		menu.addSeparator();
		JMenu importRDF = new JMenu("Import");
		JMenu replace = new JMenu("Replace");
		// RDFSだけReplaceはとりあえず，禁止．
		//mi = new JMenuItem("RDFS(File)");
		//mi.addActionListener(new ReplaceRDFSFileAction());
		//replace.add(mi);
		mi = new JMenuItem("RDF/XML(File)");
		mi.addActionListener(new ReplaceRDFFileAction());
		replace.add(mi);
		mi = new JMenuItem("RDF/XML(URI)");
		mi.addActionListener(new ReplaceRDFURIAction());
		replace.add(mi);
		importRDF.add(replace);

		JMenu mergeMenu = new JMenu("Merge");
		mi = new JMenuItem("RDFS(File)");
		mi.addActionListener(new MergeRDFSFileAction());
		mergeMenu.add(mi);
		mi = new JMenuItem("RDFS(URI)");
		mi.addActionListener(new MergeRDFSURIAction());
		mergeMenu.add(mi);
		mi = new JMenuItem("RDF/XML(File)");
		mi.addActionListener(new MergeRDFFileAction());
		mergeMenu.add(mi);
		mi = new JMenuItem("RDF/XML(URI)");
		mi.addActionListener(new MergeRDFURIAction());
		mergeMenu.add(mi);
		importRDF.add(mergeMenu);

		//		mi = new JMenuItem("Real RDF");
		//		mi.addActionListener(new ImportRealRDFAction());
		//		importRDF.add(mi);
		menu.add(importRDF);

		JMenu exportMenu = new JMenu("Export");
		mi = new JMenuItem("RDFS");
		mi.addActionListener(new ExportAction());
		exportMenu.add(mi);
		mi = new JMenuItem("RDF");
		mi.addActionListener(new ExportAction());
		exportMenu.add(mi);
		mi = new JMenuItem("N-Triple");
		mi.addActionListener(new ExportAction());
		exportMenu.add(mi);
		menu.add(exportMenu);

		menu.addSeparator();
		//		menu.add(getPluginMenus());  // JavaWebStartでは，pluginは使用できないと思われる．
		//		menu.addSeparator();

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

	class ExitAction extends AbstractAction {
		public void actionPerformed(ActionEvent e) {
			saveWindows();
			System.exit(0);
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
			File file = getImportFile();
			if (file == null) {
				return;
			}
			realRDFEditor.importFile(file);
		}
	}

	class NewAction extends AbstractAction {
		public void actionPerformed(ActionEvent e) {
			int messageType =
				JOptionPane.showInternalConfirmDialog(desktop, "Are you sure you want to continue?", "Warning", JOptionPane.YES_NO_OPTION);
			if (messageType == JOptionPane.YES_OPTION) {
				attrDialog.setNullPanel();
				resInfoMap.clear();
				litInfoMap.clear();
				rdfsInfoMap.clear();
				gmanager.removeAllCells();
			}
		}
	}

	class OpenProjectAction extends AbstractAction {
		public void loadProject(File file) {
			try {
				if (file != null) {
					FileInputStream fi = new FileInputStream(file);
					//GZIPInputStream gzin = new GZIPInputStream(fi);
					ObjectInputStream oi = new ObjectInputStream(fi);
					gmanager.loadState(oi.readObject());
					fi.close();
				}
			} catch (IOException ex) {
				ex.printStackTrace();
			} catch (ClassNotFoundException cnfe) {
				cnfe.printStackTrace();
			}
		}

		public void actionPerformed(ActionEvent e) {
			File file = getImportFile();
			loadProject(file);
		}
	}

	class SaveProjectAction extends AbstractAction {
		protected void saveProject(File file) {
			try {
				FileOutputStream fo = new FileOutputStream(file);
				//GZIPOutputStream gzout = new GZIPOutputStream(fo);
				ObjectOutputStream oo = new ObjectOutputStream(fo);
				oo.writeObject(gmanager.storeState());
				oo.flush();
				fo.close();
			} catch (FileNotFoundException fne) {
				fne.printStackTrace();
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}
		}

		public void actionPerformed(ActionEvent e) {
			File file = getImportFile();
			saveProject(file);
		}
	}

	private File getImportFile() {
		File file = null;
		JFileChooser jfc = new JFileChooser(lastSelectedFile);
		int fd = jfc.showOpenDialog(desktop);

		if (fd == JFileChooser.APPROVE_OPTION) {
			file = jfc.getSelectedFile();
			lastSelectedFile = file;
		} else {
			System.out.println("Can not open File");
		}
		return file;
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

	private Reader getReader(String uri) {
		if (uri == null) {
			return null;
		}
		URL rdfURI = null;
		try {
			rdfURI = getURI(uri);
			Reader reader = new InputStreamReader(rdfURI.openStream());
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

	private Reader getReader() {
		File file = getImportFile();
		if (file == null) {
			return null;
		}
		try {
			Reader reader = new FileReader(file);
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
			Model model = readModel(getReader(), gmanager.getBaseURI());
			mr3Reader.replaceRDF(model);
			rdfEditor.fitWindow();
		}
	}

	class ReplaceRDFURIAction extends AbstractAction {
		public void actionPerformed(ActionEvent e) {
			String uri = JOptionPane.showInternalInputDialog(desktop, "Open URI");
			Model model = readModel(getReader(uri), gmanager.getBaseURI());
			mr3Reader.replaceRDF(model);
			rdfEditor.fitWindow();
		}
	}

	public void mergeRDFModel(Model model) {
		mr3Reader.mergeRDF(model);
	}

	class MergeRDFFileAction extends AbstractAction {
		public void actionPerformed(ActionEvent e) {
			Model model = readModel(getReader(), gmanager.getBaseURI());
			mr3Reader.mergeRDF(model);
		}
	}

	class MergeRDFURIAction extends AbstractAction {
		public void actionPerformed(ActionEvent e) {
			String uri = JOptionPane.showInternalInputDialog(desktop, "Open URI");
			Model model = readModel(getReader(uri), gmanager.getBaseURI());
			mr3Reader.mergeRDF(model);
		}
	}

	public void mergeRDFSModel(Model model) {
		mr3Reader.mergeRDFS(model);
	}

	class MergeRDFSFileAction extends AbstractAction {
		public void actionPerformed(ActionEvent e) {
			Model model = readModel(getReader(), gmanager.getBaseURI());
			mr3Reader.mergeRDFS(model);
		}
	}

	class MergeRDFSURIAction extends AbstractAction {
		public void actionPerformed(ActionEvent e) {
			String uri = JOptionPane.showInternalInputDialog(desktop, "Open URI");
			Model model = readModel(getReader(uri), gmanager.getBaseURI());
			mr3Reader.mergeRDFS(model);
		}
	}

	class ExportAction implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			if (e.getActionCommand().equals("RDFS")) {
				rdfEditor.exportRDFSFile(getRDFSModel());
			} else {
				rdfEditor.exportRDFFile(e.getActionCommand());
			}
		}
	}

	protected JMenu getViewMenu() {
		JMenu menu = new JMenu("View");
		ChangeCellViewAction changeCellViewAction = new ChangeCellViewAction();
		uriView = new JRadioButton("URI View");
		uriView.addItemListener(changeCellViewAction);
		uriView.setSelected(true);
		labelView = new JRadioButton("Label View");
		labelView.addItemListener(changeCellViewAction);
		ButtonGroup group = new ButtonGroup();
		group.add(uriView);
		group.add(labelView);
		menu.add(uriView);
		menu.add(labelView);
		menu.addSeparator();
		JMenuItem item = new JMenuItem("Deploy windows");
		item.addActionListener(new DeployWindows());
		menu.add(item);
		menu.addSeparator();
		menu.add(getEditorViewMenu());
		showSrcView = new JCheckBoxMenuItem("Show SRC", false);
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

	//	class LightViewAction extends AbstractAction {
	//	public void actionPerformed(ActionEvent e) {	
	//		
	//		}
	//	}

	protected JMenu getConvertMenu() {
		JMenu menu = new JMenu("Convert");
		JMenu rdfView = new JMenu("RDF");
		menu.add(rdfView);
		JMenuItem mi = new JMenuItem("RDF");
		mi.addActionListener(new JGraphToRDFAction());
		rdfView.add(mi);
		mi = new JMenuItem("N-Triple");
		mi.addActionListener(new JGraphToNTripleAction());
		rdfView.add(mi);
		JMenu rdfsView = new JMenu("RDFS");
		mi = new JMenuItem("RDFS(Class/Property)");
		mi.addActionListener(new JGraphToRDFSAction());
		rdfsView.add(mi);
		mi = new JMenuItem("RDFS(Class)");
		mi.addActionListener(new JGraphToClassAction());
		rdfsView.add(mi);
		mi = new JMenuItem("RDFS(Property)");
		mi.addActionListener(new JGraphToPropertyAction());
		rdfsView.add(mi);
		menu.add(rdfsView);

		return menu;
	}

	class ShowToolTipsAction extends AbstractAction {
		public void actionPerformed(ActionEvent e) {
			ToolTipManager.sharedInstance().setEnabled(showToolTips.getState());
		}
	}

	protected JMenu getToolMenu() {
		JMenu menu = new JMenu("Tool");
		JMenuItem mi = new JMenuItem("search resource");
		mi.addActionListener(new SearchAction());
		menu.add(mi);
		//		selectAbstractLevelMode = new JCheckBoxMenuItem("Change Abstract Level", false);
		//		selectAbstractLevelMode.addActionListener(new SelectAbstractLevelAction());
		//		menu.add(selectAbstractLevelMode);
		//JMenu layout = new JMenu("Layout");
		//mi = new JMenuItem("TreeAlgorithm");
		//mi.addActionListener(new GraphLayoutAction());
		//		layout.add(mi);
		//		menu.add(layout);

		return menu;
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
			} else if (e.getItemSelectable() == labelView) {
				gmanager.setCellViewType(CellViewType.LABEL);
			}
			gmanager.changeCellView();
		}
	}

	class SearchAction implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			searchResDialog.setVisible(true);
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
			srcFrame.setVisible(true);
			srcFrame.setIcon(false);
			showSrcView.setState(true);
		} catch (Exception e) {
		}
	}

	class JGraphToRDFAction extends AbstractAction {
		public void actionPerformed(ActionEvent e) {
			rdfEditor.convertRDFSRC(srcArea);
			showSrcView();
		}
	}

	class JGraphToNTripleAction extends AbstractAction {
		public void actionPerformed(ActionEvent e) {
			rdfEditor.convertNTripleSRC(srcArea);
			showSrcView();
		}
	}

	class JGraphToClassAction extends AbstractAction {
		public void actionPerformed(ActionEvent e) {
			classEditor.convertSRC(srcArea);
			showSrcView();
		}
	}

	class JGraphToPropertyAction extends AbstractAction {
		public void actionPerformed(ActionEvent e) {
			propertyEditor.convertSRC(srcArea);
			showSrcView();
		}
	}

	public Model getRDFModel() {
		return mr3Writer.getRDFModel();
	}

	public Model getClassModel() {
		return mr3Writer.getClassModel();
	}

	public Model getPropertyModel() {
		return mr3Writer.getPropertyModel();
	}

	public Model getRDFSModel() {
		return mr3Writer.getRDFSModel();
	}

	class JGraphToRDFSAction extends AbstractAction {
		public void actionPerformed(ActionEvent e) {
			try {
				Model model = getRDFSModel();
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
		new MR3("MR^3");
	}
}
