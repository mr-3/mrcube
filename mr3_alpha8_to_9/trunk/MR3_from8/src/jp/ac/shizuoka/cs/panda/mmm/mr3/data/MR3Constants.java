/*
 * Created on 2003/10/01
 *
 */
package jp.ac.shizuoka.cs.panda.mmm.mr3.data;

import javax.swing.*;

import jp.ac.shizuoka.cs.panda.mmm.mr3.util.*;

/**
 * @author takeshi morita
 */
public class MR3Constants {

	public static String APPLY;
	public static String CLOSE;
	public static String OK;
	public static String CANCEL;
	public static String PREFIX;
	public static String NAME_SPACE;
	public static ImageIcon LOGO = Utilities.getImageIcon(Translator.getString("Logo"));

	public static void initConstants() {
		APPLY = Translator.getString("Apply");
		CLOSE = Translator.getString("Close");
		OK = Translator.getString("OK");
		CANCEL = Translator.getString("Cancel");
		PREFIX = Translator.getString("Prefix");
		NAME_SPACE = Translator.getString("NameSpace");
	}

}
