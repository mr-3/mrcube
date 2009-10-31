package jp.ac.shizuoka.cs.panda.mmm.mr3.util;
import java.text.*;

/**
 * @author takeshi morita
 *
 */
public class RDFLiteralUtil {

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
