/*
 * @(#) Translator.java
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

package org.semanticweb.mmm.mr3.util;

import java.io.*;
import java.util.*;
import java.util.prefs.*;

import org.semanticweb.mmm.mr3.data.*;

/*
 * 
 * @author takeshi morita
 *
 */
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