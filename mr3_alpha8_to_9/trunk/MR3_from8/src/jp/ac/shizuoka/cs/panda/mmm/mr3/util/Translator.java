package jp.ac.shizuoka.cs.panda.mmm.mr3.util;

import java.io.*;
import java.net.*;
import java.util.*;

public class Translator {

	protected static ResourceBundle resourceBundle;
	private static final String RESOURCE_DIR = "jp/ac/shizuoka/cs/panda/mmm/mr3/resources/";

	public static String getString(String sKey) {
		return resourceBundle.getString(sKey);
	}

	public static void loadResourceBundle() {
		try {
			URL url = Utilities.class.getClassLoader().getResource(RESOURCE_DIR + "MR3_en.properties");
			InputStream is = new FileInputStream(new File(url.getPath()));
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