package org.semanticweb.mmm.mr3.util;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.jar.*;

import org.semanticweb.mmm.mr3.plugin.*;

public class PluginLoader {

	private static ClassLoader _classLoader;
	private static Collection _manifests = new ArrayList();
	private static SortedMap _pluginMenuMap = new TreeMap();

	private static FilenameFilter _jarFilter = new FilenameFilter() {
		public boolean accept(File dir, String name) {
			return name.endsWith(".jar");
		}
	};

	static  {
		Collection files = null;
		try {
			files = getClassPathFiles();
			_classLoader = createClassLoader(files);
			Thread.currentThread().setContextClassLoader(_classLoader);
		} catch (SecurityException e) {
			// expect this for applets
			//            _classLoader = SystemUtilities.class.getClassLoader();
		}
		loadManifests();
		processManifests();
		//        printSystemInfo(System.err);
		//System.out.println(_pluginMenuMap);
	}

	public static Map getPluginMenuMap() {
		return Collections.unmodifiableMap(_pluginMenuMap);
	}

	private static Collection getClassPathFiles() {
		Collection files = new ArrayList();
		//        String dirPath = ApplicationProperties.getApplicationDirectory();
		String dirPath = System.getProperty("user.dir");
		if (dirPath == null) {
			//  Log.warning("Unable to find plugins directory", SystemUtilities.class, "getClassPathFiles");
		} else {
			File directory = new File(dirPath, "plugins");
			if (directory.exists()) {
				files.add(directory);
				File[] fileArray = directory.listFiles(_jarFilter);
				Arrays.sort(fileArray);
				files.addAll(Arrays.asList(fileArray));
			}
		}
		return files;
	}

	private static ClassLoader createClassLoader(Collection files) {
		Collection urls = new ArrayList();
		Iterator i = files.iterator();
		while (i.hasNext()) {
			File file = (File) i.next();
			try {
				urls.add(file.toURL());
			} catch (Exception e) {
				//  Log.exception(e, SystemUtilities.class, "createClassLoader");
			}
		}
		URL[] urlArray = (URL[]) urls.toArray(new URL[urls.size()]);
		return new URLClassLoader(urlArray, PluginLoader.class.getClassLoader());
	}

	private static void loadManifests() {
		//        loadExtraManifest();
		try {
			Enumeration e = _classLoader.getResources("META-INF/MANIFEST.MF");
			while (e.hasMoreElements()) {
				URL url = (URL) e.nextElement();
				_manifests.add(new Manifest(url.openStream()));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void processManifests() {
		Iterator i = _manifests.iterator();
		while (i.hasNext()) {
			Manifest m = (Manifest) i.next();
			processManifest(m);
		}
	}

	private static final String PLUGIN_MENU_KEY = "menu-name";

	private static void processManifest(Manifest manifest) {
		Iterator i = manifest.getEntries().keySet().iterator();
		while (i.hasNext()) {
			String attributeName = (String) i.next();
			Attributes attributes = manifest.getAttributes(attributeName);
			String className = attributeNameToClassName(attributeName);
			Class classObj = forName(className);
			if (classObj == null || classObj.getSuperclass() == null) {
				continue;
			}
			if (classObj.getSuperclass().equals(MR3Plugin.class)) {
				String menuName = attributes.getValue(PLUGIN_MENU_KEY);
				if (menuName == null) {
					continue;
				}
				_pluginMenuMap.put(menuName, classObj);
			}
		}
	}

	public static Class forName(String className) {
		Class clas = null;
		try {
			clas = Class.forName(className, true, _classLoader);
		} catch (ClassNotFoundException e) {
			// do nothing
		} catch (Exception e) {
			//Log.exception(e, SystemUtilities.class, "forName", className);
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
