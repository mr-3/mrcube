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
