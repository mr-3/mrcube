package mr3.data;
import java.io.*;
import java.util.*;

import com.hp.hpl.mesa.rdf.jena.common.*;
import com.hp.hpl.mesa.rdf.jena.mem.*;
import com.hp.hpl.mesa.rdf.jena.model.*;
import com.jgraph.graph.*;

public class RDFResourceInfo implements Serializable {

	private Object typeCell; // RDFS Classに対応するCellを保持する
	private GraphCell typeViewCell; // RDF Resourceにつく矩形のCellを保持する

	transient private Resource uri;
	private URIType uriType;
	transient private RDFSInfoMap rdfsInfoMap = RDFSInfoMap.getInstance();

	public RDFResourceInfo(URIType ut, String uri, GraphCell typeCell) {
		uriType = ut;
		if (uriType == URIType.ANONYMOUS) {
			this.uri = getAnonResource();
		} else {
			this.uri = new ResourceImpl(uri);
		}
		this.typeViewCell = typeCell;
	}

	private static Model anonModel = new ModelMem();
	private Resource getAnonResource() {
		try {
			return anonModel.createResource();
		} catch (RDFException e) {
			e.printStackTrace();
		}
		return null;
	}

	/** RDFのTypeの見せ方を変える */
	public void setTypeCellValue(GraphCell cell) {
		Object value = null;
		if (cell != null) {
			Map map = cell.getAttributes();
			value = GraphConstants.getValue(map);
		} else {
			value = "";
		}

		Map typeMap = typeViewCell.getAttributes();
		GraphConstants.setValue(typeMap, value);
		typeViewCell.changeAttributes(typeMap);
	}

	public boolean equals(Object o) {
		String uriStr = (String) o;
		return uriStr.equals(uri.getURI());
	}

	public void setTypeCellValue() {
		if (typeViewCell != null) { // 仮ルートを作る時，typeViewCellを作らないため
			setTypeCellValue((GraphCell) typeCell);
		}
	}

	public void setTypeCell(Object type) {
		this.typeCell = type;
		setTypeCellValue();
	}

	public Resource getType() {
		RDFSInfo info = rdfsInfoMap.getCellInfo(typeCell);
		if (info == null || info.getURI() == null) {
			return new ResourceImpl("");
		} else {
			return info.getURI();
		}
	}

	public Object getTypeCell() {
		return typeCell;
	}

	public Object getTypeViewCell() {
		return typeViewCell;
	}

	public void setURIType(URIType type) {
		uriType = type;
	}

	public URIType getURIType() {
		return uriType;
	}

	public void setURI(String str) {
		if (uriType == URIType.ANONYMOUS) {
			uri = getAnonResource();
		} else {
			uri = new ResourceImpl(str);
		}
	}

	public Resource getURI() {
		return uri;
	}

	public String getURIStr() {
		return uri.getURI();
	}

	public String toString() {
		String msg = "URIType: " + uriType + "\n";
		msg += "URI: " + uri + "\n";
		msg += "Type: " + typeCell + "\n";
		return msg;
	}
}
