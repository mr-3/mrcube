/*
 * Created on 2003/07/20
 *
 */
package jp.ac.shizuoka.cs.panda.mmm.mr3.actions;

import java.awt.event.*;
import java.lang.reflect.*;
import java.util.*;

import jp.ac.shizuoka.cs.panda.mmm.mr3.*;
import jp.ac.shizuoka.cs.panda.mmm.mr3.util.*;

/**
 * @author takeshi morita
 *
 */
public class ImportPlugin extends MR3AbstractAction {

	public ImportPlugin(MR3 mr3, String name) {
		super(mr3, name);
	}
	
	private static final String PLUGIN_METHOD_NAME = "exec";

	public void actionPerformed(ActionEvent e) {
		String menuName = e.getActionCommand();
		Map pluginMenuMap = PluginLoader.getPluginMenuMap();
		try {
			Class classObj = (Class) pluginMenuMap.get(menuName);
			Object instance = classObj.newInstance();
			Method initMethod = classObj.getMethod("setMR3", new Class[] { MR3.class });
			initMethod.invoke(instance, new Object[] { mr3 });
			Method m = classObj.getMethod(PLUGIN_METHOD_NAME, null);
			m.invoke(instance, null);
		} catch (NoClassDefFoundError ncdfe) {
			ncdfe.printStackTrace();
		} catch (NoSuchMethodException nsme) {
			nsme.printStackTrace();
		} catch (InstantiationException ine) {
			ine.printStackTrace();
		} catch (IllegalAccessException ille) {
			ille.printStackTrace();
		} catch (InvocationTargetException inve) {
			inve.printStackTrace();
		}
	}

}
