/*
 * Created on 2003/07/12
 *
 */
package mr3.util;

import java.awt.*;
import java.util.*;

import mr3.data.*;
import mr3.jgraph.*;
import mr3.ui.*;
import mr3.ui.NameSpaceTableDialog.*;

import com.hp.hpl.mesa.rdf.jena.common.*;
import com.hp.hpl.mesa.rdf.jena.mem.*;
import com.hp.hpl.mesa.rdf.jena.model.*;
import com.hp.hpl.mesa.rdf.jena.vocabulary.*;
import com.jgraph.graph.*;

/**
 * @author takeshi morita
 */
public class ProjectManager {

	private GraphManager gmanager;
	private NSTableModel nsTableModel;
	private NameSpaceTableDialog nsTableDialog;
	private RDFResourceInfoMap resInfoMap = RDFResourceInfoMap.getInstance();
	private RDFSInfoMap rdfsInfoMap = RDFSInfoMap.getInstance();
	private RDFLiteralInfoMap litInfoMap = RDFLiteralInfoMap.getInstance();

	public ProjectManager(GraphManager gm, NameSpaceTableDialog nsTableD) {
		gmanager = gm;
		nsTableDialog = nsTableD;
		nsTableModel = nsTableD.getNSTableModel();
	}

	private void addRDFProjectModel(Model projectModel) throws RDFException {
		int literal_cnt = 0;
		RDFGraph graph = gmanager.getRDFGraph();
		Object[] cells = graph.getAllCells();
		for (int i = 0; i < cells.length; i++) {
			if (graph.isRDFResourceCell(cells[i])) {
				GraphCell cell = (GraphCell) cells[i];
				Rectangle rec = GraphConstants.getBounds(cell.getAttributes());
				RDFResourceInfo info = resInfoMap.getCellInfo(cell);
				projectModel.add(info.getURI(), MR3Resource.PointX, rec.getX());
				projectModel.add(info.getURI(), MR3Resource.PointY, rec.getY());
				projectModel.add(info.getURI(), MR3Resource.NodeWidth, rec.getWidth());
				projectModel.add(info.getURI(), MR3Resource.NodeHeight, rec.getHeight());
			} else if (graph.isRDFPropertyCell(cells[i])) {
				Edge edge = (Edge) cells[i];
				GraphCell sourceCell = (GraphCell) graph.getSourceVertex(edge);
				GraphCell targetCell = (GraphCell) graph.getTargetVertex(edge);
				if (graph.isRDFLiteralCell(targetCell)) {
					Object propCell = rdfsInfoMap.getEdgeInfo(edge);
					RDFSInfo propInfo = rdfsInfoMap.getCellInfo(propCell);
					RDFResourceInfo info = resInfoMap.getCellInfo(sourceCell);
					Property property = null;
					if (propInfo == null) {
						property = MR3Resource.Nil;
					} else {
						property = new PropertyImpl(propInfo.getURI().getURI());
					}
					Rectangle rec = GraphConstants.getBounds(targetCell.getAttributes());
					Literal litInfo = litInfoMap.getCellInfo(targetCell);
					Resource litRes = new ResourceImpl();
					projectModel.add(litRes, RDF.type, new ResourceImpl(MR3Resource.Literal + Integer.toString(literal_cnt++)));
					projectModel.add(info.getURI(), property, litRes);
					projectModel.add(litRes, MR3Resource.LiteralLang, litInfo.getLanguage());
					projectModel.add(litRes, MR3Resource.LiteralString, litInfo.getString());
					projectModel.add(litRes, MR3Resource.PointX, rec.getX());
					projectModel.add(litRes, MR3Resource.PointY, rec.getY());
					projectModel.add(litRes, MR3Resource.NodeWidth, rec.getWidth());
					projectModel.add(litRes, MR3Resource.NodeHeight, rec.getHeight());
				}
			}
		}
	}

	private void addRDFSProjectModel(Model projectModel, RDFGraph graph) throws RDFException {
		Object[] cells = graph.getAllCells();
		for (int i = 0; i < cells.length; i++) {
			if (graph.isRDFSCell(cells[i])) {
				GraphCell cell = (GraphCell) cells[i];
				Rectangle rec = GraphConstants.getBounds(cell.getAttributes());
				RDFSInfo info = rdfsInfoMap.getCellInfo(cell);
				projectModel.add(info.getURI(), MR3Resource.PointX, rec.getX());
				projectModel.add(info.getURI(), MR3Resource.PointY, rec.getY());
				projectModel.add(info.getURI(), MR3Resource.NodeWidth, rec.getWidth());
				projectModel.add(info.getURI(), MR3Resource.NodeHeight, rec.getHeight());
			}
		}
	}

	private static final int IS_AVAILABLE_COLUMN = 0;
	private static final int PREFIX_COLUMN = 1;
	private static final int NS_COLUMN = 2;

	private void addPrefixNSProjectModel(Model projectModel) throws RDFException {
		for (int i = 0; i < nsTableModel.getRowCount(); i++) {
			Boolean isAvailable = (Boolean) nsTableModel.getValueAt(i, IS_AVAILABLE_COLUMN);
			String prefix = (String) nsTableModel.getValueAt(i, PREFIX_COLUMN);
			String nameSpace = (String) nsTableModel.getValueAt(i, NS_COLUMN);
			projectModel.add(new ResourceImpl(nameSpace), MR3Resource.Is_prefix_available, isAvailable.toString());
			projectModel.add(new ResourceImpl(nameSpace), MR3Resource.Prefix, prefix);
		}
	}

	public Model getProjectModel() {
		Model projectModel = new ModelMem();
		try {
			addRDFProjectModel(projectModel);
			addRDFSProjectModel(projectModel, gmanager.getClassGraph());
			addRDFSProjectModel(projectModel, gmanager.getPropertyGraph());
			addPrefixNSProjectModel(projectModel);
		} catch (RDFException e) {
			e.printStackTrace();
		}
		return projectModel;
	}

	private boolean hasProjectPredicate(Statement stmt) {
		return (
			stmt.getPredicate().equals(MR3Resource.PointX)
				|| stmt.getPredicate().equals(MR3Resource.PointY)
				|| stmt.getPredicate().equals(MR3Resource.NodeWidth)
				|| stmt.getPredicate().equals(MR3Resource.NodeHeight)
				|| stmt.getPredicate().equals(MR3Resource.LiteralLang)
				|| stmt.getPredicate().equals(MR3Resource.LiteralString)
				|| stmt.getPredicate().equals(MR3Resource.Prefix)
				|| stmt.getPredicate().equals(MR3Resource.Is_prefix_available));
	}

	public Model extractProjectModel(Model model) throws RDFException {
		Model extractModel = new ModelMem();
		for (StmtIterator i = model.listStatements(); i.hasNext();) {
			Statement stmt = i.next();
			if (hasProjectPredicate(stmt)) {
				extractModel.add(stmt);
			}
		}
		model.remove(extractModel);
		return extractModel;
	}

	public void changeNSModel(Map uriPrefixMap, Map uriIsAvailableMap) {
		Set existNSSet = new HashSet();
		for (int i = 0; i < nsTableModel.getRowCount(); i++) {
			String nameSpace = (String) nsTableModel.getValueAt(i, NS_COLUMN);
			String prefix = (String) uriPrefixMap.get(nameSpace);
			Boolean isAvailable = (Boolean) uriIsAvailableMap.get(nameSpace);
			if (!nsTableModel.getValueAt(i, PREFIX_COLUMN).equals(prefix)) {
				nsTableModel.setValueAt(prefix, i, PREFIX_COLUMN);
			}
			nsTableModel.setValueAt(isAvailable, i, IS_AVAILABLE_COLUMN);
			existNSSet.add(nameSpace);
		}
		Collection notExistNSSet = uriPrefixMap.keySet();
		notExistNSSet.removeAll(existNSSet);
		for (Iterator i = notExistNSSet.iterator(); i.hasNext();) {
			String nameSpace = (String) i.next();
			String prefix = (String) uriPrefixMap.get(nameSpace);
			Boolean isAvailable = (Boolean) uriIsAvailableMap.get(nameSpace);
			nsTableDialog.addNameSpaceTable(isAvailable, prefix, nameSpace);
		}
	}

	public void loadProject(Model model) throws RDFException {
		Map uriNodeInfoMap = new HashMap();
		Map uriPrefixMap = new HashMap();
		Map uriIsAvailableMap = new HashMap();
		for (StmtIterator i = model.listStatements(); i.hasNext();) {
			Statement stmt = i.next();
			Rectangle rec = (Rectangle) uriNodeInfoMap.get(stmt.getSubject());
			if (rec == null) {
				rec = new Rectangle();
				uriNodeInfoMap.put(stmt.getSubject(), rec);
			}
			if (stmt.getPredicate().equals(MR3Resource.PointX)) {
				setPositionX(uriNodeInfoMap, stmt, rec);
			} else if (stmt.getPredicate().equals(MR3Resource.PointY)) {
				setPositionY(uriNodeInfoMap, stmt, rec);
			} else if (stmt.getPredicate().equals(MR3Resource.NodeWidth)) {
				setNodeWidth(uriNodeInfoMap, stmt, rec);
			} else if (stmt.getPredicate().equals(MR3Resource.NodeHeight)) {
				setNodeHeight(uriNodeInfoMap, stmt, rec);
			} else if (stmt.getPredicate().equals(MR3Resource.Prefix)) {
				uriPrefixMap.put(stmt.getSubject().getURI(), stmt.getObject().toString());
			} else if (stmt.getPredicate().equals(MR3Resource.Is_prefix_available)) {
				if (stmt.getObject().toString().equals("true")) {
					uriIsAvailableMap.put(stmt.getSubject().getURI(), new Boolean(true));
				} else {
					uriIsAvailableMap.put(stmt.getSubject().getURI(), new Boolean(false));
				}
			}
		}
		gmanager.setNodeBounds(uriNodeInfoMap);
		changeNSModel(uriPrefixMap, uriIsAvailableMap);
	}

	private void setNodeWidth(Map uriNodeInfoMap, Statement stmt, Rectangle rec) {
		int width = (int) Float.parseFloat(stmt.getObject().toString());
		rec.width = width;
		uriNodeInfoMap.put(stmt.getSubject(), rec);
	}

	private void setNodeHeight(Map uriNodeInfoMap, Statement stmt, Rectangle rec) {
		int height = (int) Float.parseFloat(stmt.getObject().toString());
		rec.height = height;
		uriNodeInfoMap.put(stmt.getSubject(), rec);
	}

	private void setPositionY(Map uriNodeInfoMap, Statement stmt, Rectangle rec) {
		int y = (int) Float.parseFloat(stmt.getObject().toString());
		rec.y = y;
		uriNodeInfoMap.put(stmt.getSubject(), rec);
	}

	private void setPositionX(Map uriNodeInfoMap, Statement stmt, Rectangle rec) {
		int x = (int) Float.parseFloat(stmt.getObject().toString());
		rec.x = x;
		uriNodeInfoMap.put(stmt.getSubject(), rec);
	}

}
