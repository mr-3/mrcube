package jp.ac.shizuoka.cs.panda.mmm.mr3.util;

import java.io.*;
import java.util.*;

public class Translator {

	protected static ResourceBundle resourceBundle;
	private static final String RESOURCE_DIR = "jp/ac/shizuoka/cs/panda/mmm/mr3/resources/";

	public static String getString(String sKey) {
		try {
			return resourceBundle.getString(sKey);
		} catch(Exception e) {
			e.printStackTrace();
			return "Failed";
		}
	}

	public static void loadResourceBundle(String lang) {
		try {			
			InputStream ins = Utilities.class.getClassLoader().getResourceAsStream(RESOURCE_DIR + "MR3_"+lang+".properties");
			resourceBundle = new PropertyResourceBundle(ins);
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}
}