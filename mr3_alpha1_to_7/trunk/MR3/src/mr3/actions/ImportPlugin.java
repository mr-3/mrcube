/*
 * Created on 2003/07/20
 *
 */
package mr3.actions;

import java.awt.event.*;
import java.lang.reflect.*;
import java.util.*;

import mr3.*;
import mr3.util.*;

/**
 * @author takeshi morita
 *
 */
public class ImportPlugin extends MR3AbstractAction {

	public ImportPlugin(MR3 mr3, String title) {
		super(mr3, title);
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
