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

	private GraphManager gmanager;
	private RDFResourceInfoMap resInfoMap = RDFResourceInfoMap.getInstance();
	private RDFLiteralInfoMap litInfoMap = RDFLiteralInfoMap.getInstance();
	private RDFSInfoMap rdfsInfoMap = RDFSInfoMap.getInstance();

	private JCheckBoxMenuItem showTypeCell;
	private JCheckBoxMenuItem selectAbstractLevelMode;
	private JCheckBoxMenuItem showToolTips;
	private JInternalFrame[] internalFrames = new JInternalFrame[3];
	private JInternalFrame srcFrame;
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

		setSize(userPrefs.getInt(WindowWidth, MAIN_FRAME_WIDTH), userPrefs.getInt(WindowHeight, MAIN_FRAME_HEIGHT));
		setLocation(userPrefs.getInt(WindowPositionX, 50), userPrefs.getInt(WindowPositionY, 50));
		//		setLookAndFeel();

		attrDialog = new AttributeDialog();
		gmanager = new GraphManager(attrDialog);

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

		internalFrames[0] = createInternalFrame(rdfEditor, "RDF Editor", DEMO_FRAME_LAYER);
		//		createInternalFrame(realRDFEditor, "Real RDF Editor", DEMO_FRAME_LAYER);
		internalFrames[1] = createInternalFrame(classEditor, "Class Editor", DEMO_FRAME_LAYER);
		internalFrames[2] = createInternalFrame(propertyEditor, "Property Editor", DEMO_FRAME_LAYER);
		srcFrame = createInternalFrame(new JScrollPane(srcArea), "Src View", DEMO_FRAME_LAYER);
		srcFrame.setBounds(new Rectangle(0, 0, FRAME_WIDTH, FRAME_HEIGHT));
		srcFrame.setVisible(false);

		desktop.setBackground(DESKTOP_BACK_COLOR);

		classTreePanel = new RDFSTreePanel(gmanager, rdfsInfoMap.getClassTreeModel(), new ClassTreeCellRenderer());
		propTreePanel = new RDFSTreePanel(gmanager, rdfsInfoMap.getPropTreeModel(), new PropertyTreeCellRenderer());
		JTabbedPane treeTab = new JTabbedPane();
		treeTab.add("Class", classTreePanel);
		treeTab.add("Property", propTreePanel);

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
				new PrefDialog(gmanager);
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

	private static final String WindowHeight = "Window Height";
	private static final String WindowWidth = "Window Width";
	private static final String WindowPositionX = "Window Position X";
	private static final String WindowPositionY = "Window Position Y";

	private static final String RDFEditorHeight = "RDF Editor Height";
	private static final String RDFEditorWidth = "RDF Editor Width";
	private static final String RDFEditorPositionX = "RDF Editor Position X";
	private static final String RDFEditorPositionY = "RDF Editor Position Y";

	private static final String ClassEditorHeight = "Class Editor Height";
	private static final String ClassEditorWidth = "Class Editor Width";
	private static final String ClassEditorPositionX = "Class Editor Position X";
	private static final String ClassEditorPositionY = "Class Editor Position Y";

	private static final String PropertyEditorHeight = "Property Editor Height";
	private static final String PropertyEditorWidth = "Property Editor Width";
	private static final String PropertyEditorPositionX = "Property Editor Position X";
	private static final String PropertyEditorPositionY = "Property Editor Position Y";

	private void saveWindowBounds() {
		Rectangle windowRect = getBounds();
		userPrefs.putInt(WindowHeight, (int) windowRect.getHeight());
		userPrefs.putInt(WindowWidth, (int) windowRect.getWidth());
		userPrefs.putInt(WindowPositionX, (int) windowRect.getX());
		userPrefs.putInt(WindowPositionY, (int) windowRect.getY());
	}

	private void saveRDFEditorBounds() {
		Rectangle rdfEditorRect = internalFrames[0].getBounds();
		userPrefs.putInt(RDFEditorHeight, (int) rdfEditorRect.getHeight());
		userPrefs.putInt(RDFEditorWidth, (int) rdfEditorRect.getWidth());
		userPrefs.putInt(RDFEditorPositionX, (int) rdfEditorRect.getX());
		userPrefs.putInt(RDFEditorPositionY, (int) rdfEditorRect.getY());
	}

	private void saveClassEditorBounds() {
		Rectangle classEditorRect = internalFrames[1].getBounds();
		userPrefs.putInt(ClassEditorHeight, (int) classEditorRect.getHeight());
		userPrefs.putInt(ClassEditorWidth, (int) classEditorRect.getWidth());
		userPrefs.putInt(ClassEditorPositionX, (int) classEditorRect.getX());
		userPrefs.putInt(ClassEditorPositionY, (int) classEditorRect.getY());
	}

	private void savePropertyEditorBounds() {
		Rectangle propertyEditorRect = internalFrames[2].getBounds();
		userPrefs.putInt(PropertyEditorHeight, (int) propertyEditorRect.getHeight());
		userPrefs.putInt(PropertyEditorWidth, (int) propertyEditorRect.getWidth());
		userPrefs.putInt(PropertyEditorPositionX, (int) propertyEditorRect.getX());
		userPrefs.putInt(PropertyEditorPositionY, (int) propertyEditorRect.getY());
	}

	private void loadWindows() {
		int width = desktop.getWidth();
		int height = desktop.getHeight();

		int editorPositionX = userPrefs.getInt(RDFEditorPositionX, 0);
		int editorPositionY = userPrefs.getInt(RDFEditorPositionY, height / 2);
		int editorWidth = userPrefs.getInt(RDFEditorWidth, width);
		int editorHeight = userPrefs.getInt(RDFEditorHeight, height / 2);
		internalFrames[0].setBounds(new Rectangle(editorPositionX, editorPositionY, editorWidth, editorHeight)); // RDF

		editorPositionX = userPrefs.getInt(ClassEditorPositionX, 0);
		editorPositionY = userPrefs.getInt(ClassEditorPositionY, 0);
		editorWidth = userPrefs.getInt(ClassEditorWidth, width / 2);
		editorHeight = userPrefs.getInt(ClassEditorHeight, height / 2);
		internalFrames[1].setBounds(new Rectangle(editorPositionX, editorPositionY, editorWidth, editorHeight)); // RDF

		editorPositionX = userPrefs.getInt(PropertyEditorPositionX, width / 2);
		editorPositionY = userPrefs.getInt(PropertyEditorPositionY, 0);
		editorWidth = userPrefs.getInt(PropertyEditorWidth, width / 2);
		editorHeight = userPrefs.getInt(PropertyEditorHeight, height / 2);
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
			int messageType = JOptionPane.showConfirmDialog(null, "Are you sure you want to continue?", "Warning", JOptionPane.YES_NO_OPTION);
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

	private Reader getReader(String uri) {
		if (uri == null) {
			return null;
		}
		URL rdfURI = null;
		try {
			rdfURI = new URL(uri);
			Reader reader = new InputStreamReader(rdfURI.openStream());
			return reader;
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
			String uri = JOptionPane.showInputDialog(null, "Open URI");
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
			String uri = JOptionPane.showInputDialog(null, "Open URI");
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
		menu.add(attrDialog.getShowPropWindow());
		menu.add(nsTableDialog.getShowNSTable());
		showTypeCell = new JCheckBoxMenuItem("Show Type", true);
		showTypeCell.addActionListener(new ShowTypeCellAction());
		menu.add(showTypeCell);
		showToolTips = new JCheckBoxMenuItem("Show ToolTips", false);
		showToolTips.addActionListener(new ShowToolTipsAction());
		ToolTipManager.sharedInstance().setEnabled(false);
		menu.add(showToolTips);
		showSrcView = new JCheckBoxMenuItem("Show SRC View", false);
		showSrcView.addActionListener(new ShowSrcViewAction());
		menu.add(showSrcView);
		//		lightView = new JCheckBoxMenuItem("Color Mode", false);
		//		lightView.addActionListener(new LightViewAction());
		//		menu.add(lightView);

		return menu;
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

	class ShowSrcViewAction extends AbstractAction {
		public void actionPerformed(ActionEvent e) {
			srcFrame.setVisible(showSrcView.getState());
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
			//			System.out.println(showTypeCell.getState());
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
