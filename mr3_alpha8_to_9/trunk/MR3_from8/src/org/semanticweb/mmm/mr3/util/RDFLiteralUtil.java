/*
 * @(#) RDFLiteralUtil.java
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

import java.text.*;

import com.hp.hpl.jena.datatypes.*;
import com.hp.hpl.jena.rdf.model.*;

/**
 * @author takeshi morita
 *
 */
public class RDFLiteralUtil {

	private static Model model = ModelFactory.createDefaultModel();
	
	public static Literal createLiteral(String value, String lang, RDFDatatype dataType) {
		return model.createTypedLiteral(value, dataType);
	}
	
	public static String insertLineFeed(String str, int lineLen) {
		str = str.replaceAll("(\n|\r)+", "");
		BreakIterator i = BreakIterator.getLineInstance();
		i.setText(str);

		StringBuffer buf = new StringBuffer();
		for (int cnt = 0, start = i.first(), end = i.next(); end != BreakIterator.DONE; start = end, end = i.next()) {
			if (lineLen < (cnt + (end - start))) {
				buf.append("\n");
				cnt = 0;
			}
			buf.append(str.substring(start, end));
			cnt += str.substring(start, end).length();
		}
		
		return buf.toString();
	}

	public static String fixString(String str) {
		str = str.replaceAll("<html>", "");
		str = str.replaceAll("</html>", "");
		str = str.replaceAll("<br>", "\n");
		return str;
	}
}
