package org.semanticweb.mmm.mr3.data;
import java.io.*;
import java.util.*;

import org.semanticweb.mmm.mr3.util.*;

import com.hp.hpl.jena.rdf.model.*;

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
		return RDFLiteralUtil.createLiteral(orgInfo.getString(), orgInfo.getLanguage(), orgInfo.getDatatype());
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
				map.put(cell, new MR3Literal(lit.getString(), lit.getLanguage(), lit.getDatatypeURI()));
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
			putCellInfo(cell, RDFLiteralUtil.createLiteral(lit.getString(), lit.getLanguage(), lit.getDatatype()));
		}
	}

	public void clear() {
		cellInfoMap.clear();
	}

	public Collection values() {
		return cellInfoMap.values();
	}
}
