package mr3.data;
import java.io.*;
import java.util.*;

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
		RDFResourceInfo newInfo = new RDFResourceInfo(orgInfo.getURIType(), orgInfo.getURI().getURI(), typeCell);
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

	public Collection entrySet() {
		return cellInfoMap.entrySet();
	}
}
