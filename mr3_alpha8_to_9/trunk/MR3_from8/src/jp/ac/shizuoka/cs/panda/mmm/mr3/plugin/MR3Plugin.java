/*
 * Created on 2003/06/17
 *
 * getSelectedXXXメソッドを追加した．
 */
package jp.ac.shizuoka.cs.panda.mmm.mr3.plugin;

import javax.swing.*;

import jp.ac.shizuoka.cs.panda.mmm.mr3.*;

import org.jgraph.*;

import com.hp.hpl.jena.rdf.model.*;

/**
 * @author takeshi morita
 */
public abstract class MR3Plugin {

	private MR3 mr3;
	private String menuName;

	protected MR3Plugin(String mn) {
		menuName = mn;
	}

	protected MR3Plugin() {
		menuName = "none";
	}

	public String getMenuName() {
		return menuName;
	}

	public void setMR3(MR3 mr3) {
		this.mr3 = mr3;
	}

	public abstract void exec();

	public String toString() {
		return menuName;
	}

	protected void replaceRDFModel(Model model) {		
		mr3.replaceRDFModel(model);
		mr3.getGraphManager().applyTreeLayout();
	}

	protected void mergeRDFModel(Model model) {
		mr3.mergeRDFModel(model);
		mr3.getGraphManager().applyTreeLayout();
	}

	protected void mergeRDFSModel(Model model) {
		mr3.mergeRDFSModel(model);
		mr3.getGraphManager().applyTreeLayout();
	}

	protected void replaceProjectModel(Model model) {
		mr3.replaceProjectModel(model);
	}
	
	protected Model getRDFModel() {
		return mr3.getRDFModel();
	}

	protected Model getSelectedRDFModel() {
		return mr3.getSelectedRDFModel();
	}

	protected Model getRDFSModel() {
		return mr3.getRDFSModel();
	}

	protected Model getSelectedRDFSModel() {
		return mr3.getSelectedRDFSModel();
	}

	protected Model getClassModel() {
		return mr3.getClassModel();
	}

	protected Model getSelectedClassModel() {
		return mr3.getSelectedClassModel();
	}

	protected Model getPropertyModel() {
		return mr3.getPropertyModel();
	}

	protected Model getSelectedPropertyModel() {
		return mr3.getSelectedPropertyModel();
	}

	protected Model getProjectModel() {
		return mr3.getProjectModel();
	}

	protected JGraph getRDFGraph() {
		return mr3.getRDFGraph();
	}

	protected JGraph getClassGraph() {
		return mr3.getClassGraph();
	}

	protected JGraph getPropertyGraph(){
		return mr3.getPropertyGraph();
	}

	protected String getBaseURI() {
		return mr3.getBaseURI();
	}
	
	protected JDesktopPane getDesktopPane() {
		return mr3.getDesktopPane();
	}
}
