package mr3.data;
import java.io.*;
import java.util.*;

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
