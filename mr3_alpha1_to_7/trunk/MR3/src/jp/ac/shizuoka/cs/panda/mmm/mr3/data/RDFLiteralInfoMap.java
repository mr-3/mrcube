package jp.ac.shizuoka.cs.panda.mmm.mr3.data;
import java.io.*;
import java.util.*;

import com.hp.hpl.mesa.rdf.jena.common.*;
import com.hp.hpl.mesa.rdf.jena.model.*;

/**
 * @author takeshi morita
 *
 */
public class RDFLiteralInfoMap {

	private Map cellInfoMap;
	private static RDFLiteralInfoMap litInfoMap = new RDFLiteralInfoMap();
	
	private RDFLiteralInfoMap() {
		cellInfoMap = new HashMap();
	}

	public static RDFLiteralInfoMap getInstance() {
		return litInfoMap;
	}

	public Literal cloneRDFLiteralInfo(Literal orgInfo) {
		Literal newInfo = null;
		try {
			newInfo = new LiteralImpl(orgInfo.getString(), orgInfo.getLanguage());
		} catch (RDFException e) {
			e.printStackTrace();
		}
		return newInfo;
	}

	public void putCellInfo(Object cell, Literal info) {
		cellInfoMap.put(cell, info);
	}

	public Literal getCellInfo(Object cell) {
		return (Literal) cellInfoMap.get(cell);
	}

	public Serializable getState() {
		HashMap map = new HashMap();
		try {
			for (Iterator i = cellInfoMap.keySet().iterator(); i.hasNext();) {
				Object cell = i.next();
				Literal lit = getCellInfo(cell);
				map.put(cell, new MR3Literal(lit.getString(), lit.getLanguage()));
			}
		} catch (RDFException e) {
			e.printStackTrace();
		}
		return map;
	}

	public void setState(Map newMap) {
		for (Iterator i = newMap.keySet().iterator(); i.hasNext();) {
			Object cell = i.next();
			MR3Literal lit = (MR3Literal) newMap.get(cell);
			putCellInfo(cell, new LiteralImpl(lit.getString(), lit.getLanguage()));
		}
	}

	public void clear() {
		cellInfoMap.clear();
	}

	public Collection values() {
		return cellInfoMap.values();
	}
}
