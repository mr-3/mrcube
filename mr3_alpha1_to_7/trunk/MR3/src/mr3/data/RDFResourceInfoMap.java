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
		list.add(getCellURIState());
		list.add((Serializable) cellInfoMap);
		return list;
	}

	public Serializable getCellURIState() {
		HashMap map = new HashMap();
		for (Iterator i = cellInfoMap.keySet().iterator(); i.hasNext();) {
			Object cell = i.next();
			RDFResourceInfo info = (RDFResourceInfo)cellInfoMap.get(cell);
			map.put(cell, info.getURIStr());
		}
		return map;
	}

	public void setState(List list) {
		Map cellURIMap = (Map)list.get(0);
		Map newMap = (Map)list.get(1);
		for (Iterator i = newMap.keySet().iterator(); i.hasNext();) {
			Object cell = i.next();
			RDFResourceInfo info = (RDFResourceInfo)newMap.get(cell);
			String uri = (String)cellURIMap.get(cell);
			RDFResourceInfo newInfo =new RDFResourceInfo(info.getURIType(), uri, (GraphCell)info.getTypeViewCell());
			newInfo.setTypeCell(info.getTypeCell()); 
			putCellInfo(cell, newInfo);
		}
	}

	public void clear() {
		cellInfoMap.clear();
	}

	public Collection entrySet() {
		return cellInfoMap.entrySet();
	}
}
