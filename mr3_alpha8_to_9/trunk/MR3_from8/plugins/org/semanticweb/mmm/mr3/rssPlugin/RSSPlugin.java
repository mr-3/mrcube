/*
 * @(#) RSSPlugin.java
 * 
 * Copyright (C) 2003 The MMM Project
 * 
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation; either version 2.1 of the License, or (at your
 * option) any later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *  
 */

package org.semanticweb.mmm.mr3.rssPlugin;

import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.*;

import javax.swing.*;
import javax.swing.table.*;

import org.cyberneko.html.parsers.*;
import org.semanticweb.mmm.mr3.plugin.*;
import org.w3c.dom.*;
import org.xml.sax.*;

import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.vocabulary.*;

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
		//		System.out.println("root:
		// "+parser.getDocument().getDocumentElement());
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
				Model rssModel = ModelFactory.createDefaultModel();
				Resource channel = ResourceFactory.createResource(pluginUI.getChannelURI());
				rssModel.add(ResourceFactory.createStatement(channel, RDF.type, RSS.channel));
				rssModel.add(ResourceFactory.createStatement(channel, RSS.link, rssModel.createLiteral(pluginUI.getChannelLink())));
				rssModel.add(ResourceFactory.createStatement(channel, RSS.description, rssModel.createLiteral(pluginUI.getChannelDescription())));
				rssModel.add(ResourceFactory.createStatement(channel, RSS.title, rssModel.createLiteral(pluginUI.getChannelTitle())));
				rssModel.add(ResourceFactory.createStatement(channel, DC.language, rssModel.createLiteral(pluginUI.getChannelLanguage())));
//				Resource anon = new ResourceImpl(new AnonId());
				Resource anon = ResourceFactory.createResource();
				rssModel.add(ResourceFactory.createStatement(channel, RSS.items, anon));
				rssModel.add(ResourceFactory.createStatement(anon, RDF.type, RDF.Seq));

				int itemCnt = 0;
				DOMParser parser = new DOMParser();
				for (int i = 0; i < tblModel.getRowCount(); i++) {
					Boolean bool = (Boolean) tblModel.getValueAt(i, 0);
					if (bool.booleanValue()) {
						URI uri = (URI) tblModel.getValueAt(i, 1);
						try {
							parser.parse(uri.toString());
						} catch (SAXException saxEx) {
							saxEx.printStackTrace();
						} catch (IOException ioEx) {
							ioEx.printStackTrace();
						}
						ItemInfo info = (ItemInfo) itemInfoMap.get(uri);
						storeItemInfo(parser.getDocument().getDocumentElement(), info);

						Resource item = ResourceFactory.createResource(info.getURI().toString());
						rssModel.add(ResourceFactory.createStatement(anon, RDF.li(itemCnt++), item));
						rssModel.add(ResourceFactory.createStatement(item, RDF.type, RSS.item));
						rssModel.add(ResourceFactory.createStatement(item, RSS.link, rssModel.createLiteral(info.getLink())));
						rssModel.add(ResourceFactory.createStatement(item, RSS.description, rssModel.createLiteral(info.getDescription())));
						rssModel.add(ResourceFactory.createStatement(item, RSS.title, rssModel.createLiteral(info.getTitle())));
					}
				}
				replaceRDFModel(rssModel);
			} catch (RDFException rdfex) {
				rdfex.printStackTrace();
			}
			frame.setVisible(false);
		}
	}

	public void exec() {
		try {
			String uri = JOptionPane.showInternalInputDialog(getDesktopPane(), "Input URI (exp: http://mmm.semanticweb.org/mr3/");
			if (uri == null || uri.length() == 0) {
				return;
			}
			parse(uri);
			showRSSPluginUI();
		} catch (IOException ex1) {
		} catch (SAXException ex2) {
		} catch (URISyntaxException ex3) {
		}
	}

	/** デバッグ用メソッド */
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
		// uri.getFragment() == null;
		return (uri.getPath().matches(".*/") || uri.getPath().matches(".*.html") || uri.getPath().matches(".*.htm"));
	}

	private boolean isValidURI(URI uri) {
		return (uri != null && isValidScheme(uri.getScheme()) && isValidHost(uri.getHost()) && isFile(uri));
	}

	public void storeChannelInfo(Element rootElement) throws URISyntaxException {
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
			String uri = JOptionPane.showInputDialog("Input URI (exp: http://mmm.semanticweb.org/mr3/");
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
