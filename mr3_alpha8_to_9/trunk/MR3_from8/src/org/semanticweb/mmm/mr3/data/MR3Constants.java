/*
 * @(#) MR3Constants.java
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

package org.semanticweb.mmm.mr3.data;

import javax.swing.*;

import org.semanticweb.mmm.mr3.util.*;

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
