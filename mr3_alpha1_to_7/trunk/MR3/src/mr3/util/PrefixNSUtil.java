/*
 * Created on 2003/07/06
 *
 */
package mr3.util;

import java.util.*;

import javax.swing.*;

import mr3.data.*;

import com.hp.hpl.mesa.rdf.jena.common.*;
import com.hp.hpl.mesa.rdf.jena.model.*;

/**
 * @author takeshi morita
 */
public class PrefixNSUtil {

	private static Set prefixNSInfoSet;

	public static void setPrefixNSInfoSet(Set set) {
		prefixNSInfoSet = set;
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

	public static void replacePrefix(String prefix, JTextField field) {
		Resource resource = new ResourceImpl(field.getText());
		if (!resource.getNameSpace().equals("http://")) {
			String localName = resource.getLocalName();
			field.setText(getNameSpace(prefix) + localName);
		}
	}

}
