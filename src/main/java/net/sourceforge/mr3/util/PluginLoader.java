/*
 * Project Name: MR^3 (Meta-Model Management based on RDFs Revision Reflection)
 * Project Website: http://mr3.sourceforge.net/
 * 
 * Copyright (C) 2003-2015 Yamaguchi Laboratory, Keio University. All rights reserved. 
 * 
 * This file is part of MR^3.
 * 
 * MR^3 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * MR^3 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with MR^3.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */

package net.sourceforge.mr3.util;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.prefs.Preferences;

import net.sourceforge.mr3.MR3;
import net.sourceforge.mr3.data.PrefConstants;
import net.sourceforge.mr3.plugin.MR3Plugin;

import org.apache.oro.text.perl.Perl5Util;

/**
 * @author Takeshi Morita
 */
public class PluginLoader {

	private static String pluginPath;
	private static ClassLoader classLoader;
	private static Collection<Manifest> manifests;
	private static SortedMap<String, List> pluginMenuMap;

	private static FilenameFilter jarFilter = new FilenameFilter() {
		public boolean accept(File dir, String name) {
			return name.endsWith(".jar");
		}
	};

	public static Map getPluginMenuMap() {
		Collection<File> files = null;
		manifests = new ArrayList<Manifest>();
		pluginMenuMap = new TreeMap<String, List>();
		try {
			files = getClassPathFiles();
			classLoader = createClassLoader(files);
			Thread.currentThread().setContextClassLoader(classLoader);
		} catch (SecurityException e) {
			System.err.println(e);
		}
		loadManifests();
		processManifests();
		return Collections.unmodifiableMap(pluginMenuMap);
	}

	private static Collection<File> getClassPathFiles() {
		Collection<File> files = new ArrayList<File>();
		Preferences userPrefs = Preferences.userNodeForPackage(MR3.class);
		pluginPath = userPrefs.get(PrefConstants.PluginsDirectory, System.getProperty("user.dir"));
		if (pluginPath.equals(System.getProperty("user.dir"))) {
			pluginPath += "/plugins";
		}
		if (pluginPath != null) {
			File directory = new File(pluginPath);
			if (directory.exists()) {
				files.add(directory);
				File[] fileArray = directory.listFiles(jarFilter);
				Arrays.sort(fileArray);
				files.addAll(Arrays.asList(fileArray));
			}
		}
		return files;
	}

	private static ClassLoader createClassLoader(Collection<File> files) {
		Collection<URL> urls = new ArrayList<URL>();
		for (File file : files) {
			try {
				urls.add(file.toURL());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		URL[] urlArray = urls.toArray(new URL[urls.size()]);
		return new URLClassLoader(urlArray, PluginLoader.class.getClassLoader());
	}

	private static void loadManifests() {
		Perl5Util util = new Perl5Util();
		try {
			for (Enumeration e = classLoader.getResources("META-INF/MANIFEST.MF"); e
					.hasMoreElements();) {
				URL url = (URL) e.nextElement();
				if (util.match("/" + pluginPath.replace('\\', '/') + "/", url.getFile())) {
					InputStream inputStream = url.openStream();
					manifests.add(new Manifest(inputStream));
					inputStream.close();
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void processManifests() {
		for (Manifest m : manifests) {
			processManifest(m);
		}
	}

	private static final String PLUGIN_MENU_KEY = "menu-name";
	private static final String PLUGIN_NAME_KEY = "plugin-name";
	private static final String PLUGIN_CREATOR_KEY = "creator";
	private static final String PLUGIN_DATE_KEY = "date";
	private static final String PLUGIN_DESCRIPTION_KEY = "description";

	private static void processManifest(Manifest manifest) {
		Iterator i = manifest.getEntries().keySet().iterator();
		while (i.hasNext()) {
			String attributeName = (String) i.next();
			Attributes attributes = manifest.getAttributes(attributeName);
			// MR3プラグインに関係のある属性を含まないものはパス
			if (attributes.getValue(PLUGIN_NAME_KEY) == null
					&& attributes.getValue(PLUGIN_MENU_KEY) == null) {
				continue;
			}
			String className = attributeNameToClassName(attributeName);
			Class classObj = forName(className);
			if (classObj == null || classObj.getSuperclass() == null) {
				continue;
			}
			if (classObj.getSuperclass().equals(MR3Plugin.class)) {
				String pluginName = attributes.getValue(PLUGIN_NAME_KEY);
				if (pluginName == null) {
					pluginName = attributes.getValue(PLUGIN_MENU_KEY);
				}

				if (pluginName == null) {
					continue;
				}
				List<Object> pluginInfo = new ArrayList<Object>();
				pluginInfo.add(classObj);
				pluginInfo.add(attributes.getValue(PLUGIN_CREATOR_KEY));
				pluginInfo.add(attributes.getValue(PLUGIN_DATE_KEY));
				pluginInfo.add(attributes.getValue(PLUGIN_DESCRIPTION_KEY));
				pluginMenuMap.put(pluginName, pluginInfo);
			}
		}
	}

	private static Class forName(String className) {
		Class clas = null;
		try {
			// clas = classLoader.loadClass(className);
			clas = Class.forName(className, true, classLoader);
		} catch (ClassNotFoundException cnfe) {
			// 無視
		} catch (Error error) {
			error.printStackTrace();
		} catch (Exception exp) {
			exp.printStackTrace();
		}
		return clas;
	}

	private static String attributeNameToClassName(String attributeName) {
		String className;
		if (attributeName.endsWith(".class")) {
			className = attributeName.substring(0, attributeName.length() - 6);
		} else {
			className = attributeName;
		}
		className = className.replace('/', '.');
		return className;
	}
}
