package jp.ac.shizuoka.cs.panda.mmm.mr3.rssPlugin;

import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.*;

import javax.swing.*;
import javax.swing.table.*;

import mr3.plugin.*;

import org.cyberneko.html.parsers.*;
import org.w3c.dom.*;
import org.xml.sax.*;

import com.hp.hpl.mesa.rdf.jena.common.*;
import com.hp.hpl.mesa.rdf.jena.mem.*;
import com.hp.hpl.mesa.rdf.jena.model.*;
import com.hp.hpl.mesa.rdf.jena.vocabulary.*;

public class RSSPlugin extends MR3Plugin {

	private Map itemInfoMap;
	private ItemInfo channelInfo;
	private JInternalFrame frame;

	private JButton importButton;

	private RSSPluginUI pluginUI;
	private DefaultTableModel tblModel;

	private void parse(String uri) throws URISyntaxException, SAXException, IOException {
		itemInfoMap = new HashMap();
		DOMParser parser = new DOMParser();
		parser.parse(uri);
		channelInfo = new ItemInfo(new URI(uri));
		//		System.out.println(parser.getDocument().getDocumentElement());
		Element rootElement = parser.getDocument().getDocumentElement();
		storeChannelInfo(rootElement);
	}

	private void showRSSPluginUI() {
		frame = new JInternalFrame("RSS Plugin", true, true);
		getDesktopPane().add(frame, JLayeredPane.MODAL_LAYER);
		pluginUI = new RSSPluginUI(channelInfo, itemInfoMap);
		tblModel = pluginUI.getTableModel();
		importButton = new JButton("Import RSS RDF Model");
		importButton.addActionListener(new RSSPlugin.ImportAction());
		pluginUI.add(importButton);
		frame.getContentPane().add(pluginUI);
		frame.setBounds(200, 100, 400, 400);
		frame.setVisible(true);
	}

	public class ImportAction extends AbstractAction {
		public void actionPerformed(ActionEvent e) {
			try {
				Model rssModel = new ModelMem();
				Resource channel = new ResourceImpl(pluginUI.getChannelURI());
				rssModel.add(new StatementImpl(channel, RDF.type, RSS.channel));
				rssModel.add(new StatementImpl(channel, RSS.link, new LiteralImpl(pluginUI.getChannelLink())));
				rssModel.add(new StatementImpl(channel, RSS.description, new LiteralImpl(pluginUI.getChannelDescription())));
				rssModel.add(new StatementImpl(channel, RSS.title, new LiteralImpl(pluginUI.getChannelTitle())));
				rssModel.add(new StatementImpl(channel, DC.language, new LiteralImpl(pluginUI.getChannelLanguage())));
				Resource anon = new ResourceImpl(new AnonId());
				rssModel.add(new StatementImpl(channel, RSS.items, anon));
				rssModel.add(new StatementImpl(anon, RDF.type, RDF.Seq));

				int itemCnt = 0;
				DOMParser parser = new DOMParser();
				for (int i = 0; i < tblModel.getRowCount(); i++) {
					Boolean bool = (Boolean) tblModel.getValueAt(i, 0);
					if (bool.booleanValue()) {
						URI uri = (URI) tblModel.getValueAt(i, 1);
						ItemInfo info = (ItemInfo) itemInfoMap.get(uri);						
						parser.parse(uri.toString());
						storeItemInfo(parser.getDocument().getDocumentElement(), info);
						
						Resource item = new ResourceImpl(info.getURI().toString());
						rssModel.add(new StatementImpl(anon, RDF.li(itemCnt++), item));
						rssModel.add(new StatementImpl(item, RDF.type, RSS.item));
						rssModel.add(new StatementImpl(item, RSS.link, new LiteralImpl(info.getLink())));
						rssModel.add(new StatementImpl(item, RSS.description, new LiteralImpl(info.getDescription())));
						rssModel.add(new StatementImpl(item, RSS.title, new LiteralImpl(info.getTitle())));

						//						System.out.println("uri: " + info.getURI());
						//						System.out.println("title: " + info.getTitle());
						//						System.out.println("link: " + info.getLink());
						//						System.out.println("description: " + info.getDescription() + "\n");
					}
				}
				replaceRDFModel(rssModel);
			} catch (RDFException rdfex) {
				rdfex.printStackTrace();
			} catch (SAXException rdfex) {
			} catch (IOException rdfex) {
			}
			frame.setVisible(false);
		}
	}

	public void exec() {
		System.out.println(getDesktopPane());
		try {
			String uri = JOptionPane.showInternalInputDialog(getDesktopPane(), "Input URI (exp: http://panda.cs.inf.shizuoka.ac.jp/mmm/mr3/");
			parse(uri);
			showRSSPluginUI();
		} catch (IOException ex1) {
		} catch (SAXException ex2) {
		} catch (URISyntaxException ex3) {
		}
	}

	/** 　デバッグ用メソッド */
	public void printModel(Model model) {
		try {
			model.write(new PrintWriter(System.out));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void storeItemInfo(Element rootElement, ItemInfo info) {
		if (rootElement == null) {
			return;
		}

		NodeList titleList = rootElement.getElementsByTagName("TITLE");
		for (int i = 0; i < titleList.getLength(); i++) {
			Node node = titleList.item(i);
			info.setTitle(node.getFirstChild().getNodeValue());
		}

		NodeList metaList = rootElement.getElementsByTagName("META");
		for (int i = 0; i < metaList.getLength(); i++) {
			Element metaElement = (Element) metaList.item(i);
			if (metaElement.getAttribute("name").equals("description")) {
				info.setDescription(metaElement.getAttribute("content"));
			}
		}
	}

	private boolean isValidScheme(String scheme) {
		return (scheme != null && (scheme.equals("http") || scheme.equals("https")));
	}

	private boolean isValidHost(String host) {
		return host != null && host.equals(channelInfo.getURI().getHost());
	}

	private boolean isFile(URI uri) {
		System.out.println(uri.getPath());
		//uri.getFragment() == null;
		return (uri.getPath().matches(".*/") || uri.getPath().matches(".*.html") || uri.getPath().matches(".*.htm"));
	}

	private boolean isValidURI(URI uri) {
		return true;
		//		return (uri != null && isValidScheme(uri.getScheme()) && isValidHost(uri.getHost()) && isFile(uri));
	}

	public void storeChannelInfo(Element rootElement) throws MalformedURLException, IOException, SAXException, URISyntaxException {
		if (rootElement == null) {
			return;
		}

		NodeList titleList = rootElement.getElementsByTagName("TITLE");
		Node node = titleList.item(0);
		if (node != null) {
			channelInfo.setTitle(node.getFirstChild().getNodeValue());
		}

		NodeList metaList = rootElement.getElementsByTagName("META");
		for (int i = 0; i < metaList.getLength(); i++) {
			Element metaElement = (Element) metaList.item(i);
			if (metaElement.getAttribute("name").equals("description")) {
				channelInfo.setDescription(metaElement.getAttribute("content"));
			}
		}

		NodeList aList = rootElement.getElementsByTagName("A");
		for (int i = 0; i < aList.getLength(); i++) {
			System.out.println(i);
			Element aElement = (Element) aList.item(i);
			URI uri = new URI(aElement.getAttribute("href"));
			if (!uri.isAbsolute()) {
				uri = channelInfo.getURI().resolve(uri.toString());
			}
			if (isValidURI(uri)) {
				ItemInfo info = new ItemInfo(uri);
				itemInfoMap.put(uri, info);
			}
		}
	}

	public static void main(String[] argv) {
		try {
			RSSPlugin plugin = new RSSPlugin();
			String uri = JOptionPane.showInputDialog("Input URI (exp: http://panda.cs.inf.shizuoka.ac.jp/mmm/mr3/");
			plugin.parse(uri);
			plugin.showRSSPluginUI();
		} catch (IOException ex1) {
			ex1.printStackTrace();
		} catch (SAXException ex2) {
			ex2.printStackTrace();
		} catch (URISyntaxException ex3) {
			ex3.printStackTrace();
		}
	}

}
