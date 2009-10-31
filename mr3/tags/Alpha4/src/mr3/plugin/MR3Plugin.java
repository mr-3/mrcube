/*
 * Created on 2003/03/24
 *
 */
package mr3.plugin;

import mr3.*;

import com.hp.hpl.mesa.rdf.jena.model.*;

/**
 * @author cs9088
 */
public abstract class MR3Plugin {

	private String menuName;
	private MR3 mrcube;

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
		mrcube = mr3;
	}

	public abstract void exec();

	public String toString() {
		return menuName;
	}

	protected void replaceRDFModel(Model model) {
		mrcube.replaceRDFModel(model);
	}

	protected void mergeRDFModel(Model model) {
		mrcube.mergeRDFModel(model);
	}

	protected void mergeRDFSModel(Model model) {
		mrcube.mergeRDFSModel(model);
	}

	protected Model getRDFModel() {
		return mrcube.getRDFModel();
	}

	protected Model getRDFSModel() {
		return mrcube.getRDFSModel();
	}

	protected Model getClassModel() {
		return mrcube.getClassModel();
	}

	protected Model getPropertyModel() {
		return mrcube.getPropertyModel();
	}
}
