/*
 * Created on 2003/07/12
 *
 */
package mr3.util;

import java.awt.*;

import mr3.data.*;
import mr3.jgraph.*;
import mr3.ui.*;
import mr3.ui.NameSpaceTableDialog.*;

import com.hp.hpl.mesa.rdf.jena.common.*;
import com.hp.hpl.mesa.rdf.jena.mem.*;
import com.hp.hpl.mesa.rdf.jena.model.*;
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

	public ProjectManager(GraphManager gm, NameSpaceTableDialog nsTableD) {
		gmanager = gm;
		nsTableDialog = nsTableD;
		nsTableModel = nsTableD.getNSTableModel();
	}

	private void addRDFProjectModel(Model projectModel) throws RDFException {
		RDFGraph graph = gmanager.getRDFGraph();
		Object[] cells = graph.getAllCells();
		for (int i = 0; i < cells.length; i++) {
			if (graph.isRDFResourceCell(cells[i])) {
				GraphCell cell = (GraphCell) cells[i];
				Rectangle rec = GraphConstants.getBounds(cell.getAttributes());
				RDFResourceInfo info = resInfoMap.getCellInfo(cell);
				projectModel.add(info.getURI(), MR3Resource.Point_x, rec.getX());
				projectModel.add(info.getURI(), MR3Resource.Point_y, rec.getY());
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
				projectModel.add(info.getURI(), MR3Resource.Point_x, rec.getX());
				projectModel.add(info.getURI(), MR3Resource.Point_y, rec.getY());
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
			stmt.getPredicate().equals(MR3Resource.Point_x)
				|| stmt.getPredicate().equals(MR3Resource.Point_y)
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

	public void changeNSModel(Resource res, RDFNode object, int column) {
		for (int i = 0; i < nsTableModel.getRowCount(); i++) {
			String nameSpace = (String) nsTableModel.getValueAt(i, NS_COLUMN);
			if (column == PREFIX_COLUMN && nameSpace.equals(res.getNameSpace())) {
				nsTableModel.setValueAt(object.toString(), i, column);
				break;
			} else if (column == IS_AVAILABLE_COLUMN && nameSpace.equals(res.getNameSpace())) {
				if (object.toString().equals("true")){
					nsTableModel.setValueAt(new Boolean(true), i, column);					
				} else {
					nsTableModel.setValueAt(new Boolean(false), i, column);
				}	
				break;			
			}
		}
	}

	public void loadProject(Model model, RDFGraph graph) throws RDFException {
		for (StmtIterator i = model.listStatements(); i.hasNext();) {
			Statement stmt = i.next();
			if (stmt.getPredicate().equals(MR3Resource.Point_x)) {
				gmanager.setPositionX(stmt.getSubject(), stmt.getObject(), graph);
			} else if (stmt.getPredicate().equals(MR3Resource.Point_y)) {
				gmanager.setPositionY(stmt.getSubject(), stmt.getObject(), graph);
			} else if (stmt.getPredicate().equals(MR3Resource.Prefix)) {
//				changeNSModel(stmt.getSubject(), stmt.getObject(), PREFIX_COLUMN);
			} else if (stmt.getPredicate().equals(MR3Resource.Is_prefix_available)) {
//				changeNSModel(stmt.getSubject(), stmt.getObject(), IS_AVAILABLE_COLUMN);
			}
		}
	}
}
