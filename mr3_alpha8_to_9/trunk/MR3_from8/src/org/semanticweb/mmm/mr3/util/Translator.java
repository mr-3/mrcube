package org.semanticweb.mmm.mr3.util;

import java.io.*;
import java.util.*;
import java.util.prefs.*;

import org.semanticweb.mmm.mr3.data.*;

public class Translator {

	protected static ResourceBundle resourceBundle;
	private static final String RESOURCE_DIR = "org/semanticweb/mmm/mr3/resources/";

	public static String getString(String sKey) {
		try {
			return resourceBundle.getString(sKey);
		} catch (Exception e) {
			e.printStackTrace();
			return "Failed";
		}
	}

	private static String getDefaultLocaleStr() {
		if (Locale.getDefault().equals(Locale.JAPAN)) {
			return "ja";
		} else {
			return "en";
		}
	}

	public static void loadResourceBundle(Preferences userPrefs) {
		String lang = userPrefs.get(PrefConstants.UILang, getDefaultLocaleStr());
		userPrefs.put(PrefConstants.UILang, lang);
		loadResourceBundle(lang);
	}

	public static void loadResourceBundle(String lang) {
		try {
			InputStream ins = Utilities.class.getClassLoader().getResourceAsStream(RESOURCE_DIR + "MR3_" + lang + ".properties");
			resourceBundle = new PropertyResourceBundle(ins);
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}
}