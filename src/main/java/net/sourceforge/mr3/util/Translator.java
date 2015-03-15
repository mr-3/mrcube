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

import java.io.*;
import java.util.*;
import java.util.prefs.*;

import javax.swing.*;

import net.sourceforge.mr3.actions.*;
import net.sourceforge.mr3.data.*;
import net.sourceforge.mr3.editor.*;
import net.sourceforge.mr3.ui.*;

/*
 * 
 * @author Takeshi Morita
 * 
 */
public class Translator {

	protected static ResourceBundle resourceBundle;
	public static final String RESOURCE_DIR = "net/sourceforge/mr3/resources/";

	public static String getString(String sKey) {
		try {
			return resourceBundle.getString(sKey);
		} catch (Exception e) {
			e.printStackTrace();
			return "FAILED";
		}
	}

	/*
	 * システムに内蔵しておく言語ファイルのセット(Locale型)
	 */
	private static Set<Locale> systemLocaleSet;
	static {
		systemLocaleSet = new HashSet<Locale>();
		systemLocaleSet.add(Locale.JAPAN);
		systemLocaleSet.add(Locale.ENGLISH);
		systemLocaleSet.add(Locale.CHINA);
	}

	/*
	 * デフォルトのロカールの言語ファイルがシステムに内蔵されている場合は， その言語を返し，内蔵されていない場合には，英語の言語を返す.
	 */
	public static String getSystemLanguage() {
		if (systemLocaleSet.contains(Locale.getDefault())) {
			return Locale.getDefault().getLanguage();
		}
		return Locale.ENGLISH.getLanguage();
	}

	private static boolean isSystemLanguage(String lang) {
		for (Locale locale : systemLocaleSet) {
			if (locale.getLanguage().equals(lang)) {
				return true;
			}
		}
		return false;
	}

	public static void loadResourceBundle(Preferences userPrefs) {
		String lang = userPrefs.get(PrefConstants.UILang, Locale.getDefault().getLanguage());
		userPrefs.put(PrefConstants.UILang, lang);
		try {
			String resDirStr = userPrefs.get(PrefConstants.ResourceDirectory,
					System.getProperty("user.dir") + "\\resources");
			File resDir = new File(resDirStr);
			InputStream ins = null;
			if (resDir != null) {
				File resFile = new File(resDir.getAbsolutePath() + "/MR3_" + lang + ".properties");
				if (resFile.exists()) {
					ins = new FileInputStream(resFile);
				}
			}
			if (ins == null) {
				if (isSystemLanguage(lang)) {
					ins = Utilities.class.getClassLoader().getResourceAsStream(
							RESOURCE_DIR + "MR3_" + lang + ".properties");
				} else {
					ins = Utilities.class.getClassLoader().getResourceAsStream(
							RESOURCE_DIR + "MR3_" + getSystemLanguage() + ".properties");
				}
			}

			resourceBundle = new PropertyResourceBundle(ins);
			loadResourceBundle();
			ins.close();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}

	private static void loadResourceBundle() {
		OptionDialog.loadResourceBundle();
		// MR3Constants.loadResourceBundle();
		EditorSelect.loadResourceBundle();

		RDFEditor.updateComponents();
		if (OptionDialog.topLevelComponent != null) {
			SwingUtilities.updateComponentTreeUI(OptionDialog.topLevelComponent);
		}
		// if (GraphManager.getDesktop() != null) {
		// SwingUtilities.updateComponentTreeUI(GraphManager.getDesktop());
		// }
	}

	public static void main(String[] args) {
		InputStream ins = Utilities.class.getClassLoader().getResourceAsStream(
				RESOURCE_DIR + "MR3_" + "ja" + ".properties");
		Properties property = new Properties();
		try {
			property.load(ins);
			OutputStream os = new FileOutputStream("C:/usr/eclipse_workspace/MR3/src/"
					+ RESOURCE_DIR + "MR3Test_ja.properties");
			property.storeToXML(os, "Japanese Properties", "SJIS");
			os.close();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}
}