/*
 * Created on 2003/07/06
 *
 */
package mr3.util;

import java.util.*;

import javax.swing.*;

import mr3.data.*;

/**
 * @author takeshi morita
 */
public class PrefixNSUtil {

	private static Set prefixNSInfoSet;

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

	public static Set getPrefixes() {
		Set prefixes = new HashSet();
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
//		Resource resource = new ResourceImpl(field.getText());
//		if (!resource.getNameSpace().equals("http://")) {
//			String localName = resource.getLocalName();
//			setNSLabel(field, getNameSpace(prefix) + localName);
			setNSLabel(nsLabel, getNameSpace(prefix));
//		}
	}

}
