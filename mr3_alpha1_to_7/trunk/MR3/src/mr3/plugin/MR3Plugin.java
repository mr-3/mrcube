/*
 * Created on 2003/06/17
 *
 * getSelectedXXXメソッドを追加した．
 */
package mr3.plugin;

import mr3.*;

import com.hp.hpl.mesa.rdf.jena.model.*;

/**
 * @author takeshi morita
 */
public abstract class MR3Plugin {

	private String menuName;
	private MR3 mr3;

	protected MR3Plugin(String mn) {
		menuName = mn;
	}

	protected MR3Plugin() {
		menuName = "none";
	}

	public String getMenuName() {
		return menuName;
	}

	public void setMRCube(MR3 mr3) {
		this.mr3 = mr3;
	}

	public abstract void exec();

	public String toString() {
		return menuName;
	}

	protected void replaceRDFModel(Model model) {
		mr3.replaceRDFModel(model);
	}

	protected void mergeRDFModel(Model model) {
		mr3.mergeRDFModel(model);
	}

	protected void mergeRDFSModel(Model model) {
		mr3.mergeRDFSModel(model);
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
}
