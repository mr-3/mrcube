package jp.ac.shizuoka.cs.panda.mmm.mr3.util;

import java.io.*;
import java.util.*;

public class Translator {

	protected static ResourceBundle resourceBundle;
	private static final String RESOURCE_DIR = "jp/ac/shizuoka/cs/panda/mmm/mr3/resources/";

	public static String getString(String sKey) {
		return resourceBundle.getString(sKey);
	}

	public static void loadResourceBundle() {
		try {
			InputStream is = Utilities.class.getClassLoader().getResourceAsStream(RESOURCE_DIR + "MR3_ja.properties");
			resourceBundle = new PropertyResourceBundle(is);
		} catch (FileNotFoundException fe) {
			fe.printStackTrace();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		} 
	}

	public static void loadResourceBundle(String filename) {
		resourceBundle = ResourceBundle.getBundle(filename);
	}
}