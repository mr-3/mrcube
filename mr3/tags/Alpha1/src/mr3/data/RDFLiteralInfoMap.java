package mr3.data;
import java.io.*;
import java.util.*;

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

	public void putCellInfo(Object cell, Literal info) {
		cellInfoMap.put(cell, info);
	}

	public Literal getCellInfo(Object cell) {
		return (Literal) cellInfoMap.get(cell);
	}

	public Serializable getState() {
		ArrayList list = new ArrayList();
		list.add(cellInfoMap);
		return list;
	}
	
	public void setState(Map newMap) {
		cellInfoMap.putAll(newMap);	
	}
	
	public void clear() {
		cellInfoMap.clear();
	}

	public Collection values() {
		return cellInfoMap.values();
	}
}
