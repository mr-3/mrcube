package jp.ac.shizuoka.cs.panda.mmm.mr3.data;
import java.io.*;
import java.util.*;

import com.hp.hpl.mesa.rdf.jena.model.*;
import com.jgraph.graph.*;

/**
 * @author takeshi morita
 *
 */
public class RDFResourceInfoMap {

	private Map cellInfoMap;
	private static RDFResourceInfoMap resInfoMap = new RDFResourceInfoMap();

	private RDFResourceInfoMap() {
		cellInfoMap = new HashMap();
	}

	public static RDFResourceInfoMap getInstance() {
		return resInfoMap;
	}

	public RDFResourceInfo cloneRDFResourceInfo(RDFResourceInfo orgInfo, GraphCell typeCell) {
		RDFResourceInfo newInfo = null;
		if (orgInfo.getURIType() == URIType.ANONYMOUS) {
			newInfo = new RDFResourceInfo(orgInfo.getURIType(), new AnonId().toString(), typeCell); 
		} else {
			newInfo = new RDFResourceInfo(orgInfo.getURIType(), orgInfo.getURIStr(), typeCell);
		}
		newInfo.setTypeCell(orgInfo.getTypeCell());
		return newInfo;
	}

	public void putCellInfo(Object cell, RDFResourceInfo info) {
		cellInfoMap.put(cell, info);
	}

	public RDFResourceInfo getCellInfo(Object cell) {
		return (RDFResourceInfo) cellInfoMap.get(cell);
	}

	public void removeCellInfo(Object cell) {
		cellInfoMap.remove(cell);
	}

	public Serializable getState() {
		return (Serializable) cellInfoMap;
	}

	public void setState(Map newMap) {
		for (Iterator i = newMap.keySet().iterator(); i.hasNext();) {
			Object cell = i.next();
			RDFResourceInfo info = (RDFResourceInfo) newMap.get(cell);
			putCellInfo(cell, new RDFResourceInfo(info));
		}
	}

	public void clear() {
		cellInfoMap.clear();
	}

	public Collection entrySet() {
		return cellInfoMap.entrySet();
	}
}
