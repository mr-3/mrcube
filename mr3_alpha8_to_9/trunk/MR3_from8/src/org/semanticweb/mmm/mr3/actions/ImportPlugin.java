/*
 * @(#) ImportPlugin.java
 *
 *
 * Copyright (C) 2003 The MMM Project
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 */

package org.semanticweb.mmm.mr3.actions;

import java.awt.event.*;
import java.lang.reflect.*;
import java.util.*;

import org.semanticweb.mmm.mr3.*;
import org.semanticweb.mmm.mr3.util.*;

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
