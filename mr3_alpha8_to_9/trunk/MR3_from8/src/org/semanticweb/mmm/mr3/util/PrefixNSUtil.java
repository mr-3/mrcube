/*
 * @(#) PrefixNSUtil.java
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

import java.util.*;

import javax.swing.*;

import org.semanticweb.mmm.mr3.data.*;

/**
 * @author takeshi morita
 */
public class PrefixNSUtil {

	private static Set prefixNSInfoSet;
	private static RDFSInfoMap rdfsInfoMap = RDFSInfoMap.getInstance();

	public static void setPrefixNSInfoSet(Set set) {
		prefixNSInfoSet = set;
	}

	public static String getBaseURIPrefix(String baseURI) {
		for (Iterator i = prefixNSInfoSet.iterator(); i.hasNext();) {
			PrefixNSInfo info = (PrefixNSInfo) i.next();
			if (info.getNameSpace().equals(baseURI)) {
				return info.getPrefix();
			}
		}
		return null;
	}

	private static Set getPropNSSet(List propList) {
		Set propNSSet = new HashSet();
		for (Iterator i = propList.iterator(); i.hasNext();) {
			Object cell = i.next();
			RDFSInfo info = rdfsInfoMap.getCellInfo(cell);
			propNSSet.add(info.getNameSpace());
		}
		return propNSSet;
	}

	public static Set getPropPrefixes(List propList) {
		Set prefixSet = new TreeSet();
		Set propNSSet = getPropNSSet(propList);
		for (Iterator i = prefixNSInfoSet.iterator(); i.hasNext();) {
			PrefixNSInfo info = (PrefixNSInfo) i.next();
			if (propNSSet.contains(info.getNameSpace())) {
				prefixSet.add(info.getPrefix());
			}
		}
		return prefixSet;
	}

	public static Set getPrefixes() {
		Set prefixes = new TreeSet();
		for (Iterator i = prefixNSInfoSet.iterator(); i.hasNext();) {
			PrefixNSInfo info = (PrefixNSInfo) i.next();
			prefixes.add(info.getPrefix());
		}
		return prefixes;
	}

	private static String getNameSpace(String prefix) {
		for (Iterator i = prefixNSInfoSet.iterator(); i.hasNext();) {
			PrefixNSInfo info = (PrefixNSInfo) i.next();
			if (info.getPrefix().equals(prefix)) {
				return info.getNameSpace();
			}
		}
		return "#";
	}

	public static void setNSLabel(JLabel nsLabel, String str) {
		nsLabel.setText(str);
		nsLabel.setToolTipText(str);
	}

	/* nsLabel‚ðprefix‚É‘Î‰ž‚·‚é–¼‘O‹óŠÔ‚É’u‚«Š·‚¦‚é */
	public static void replacePrefix(String prefix, JLabel nsLabel) {
		setNSLabel(nsLabel, getNameSpace(prefix));
	}

}
