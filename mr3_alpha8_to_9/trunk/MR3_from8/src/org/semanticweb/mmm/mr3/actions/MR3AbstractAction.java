/*
 * Created on 2003/07/19
 *
 */
package org.semanticweb.mmm.mr3.actions;

import javax.swing.*;

import org.semanticweb.mmm.mr3.*;

/**
 * @author takeshi morita
 *
 */
public abstract class MR3AbstractAction extends AbstractAction {

	protected MR3 mr3;

	public MR3AbstractAction() {

	}

	public MR3AbstractAction(MR3 mr3) {
		this.mr3 = mr3;
	}

	public MR3AbstractAction(String name) {
		super(name);
	}

	public MR3AbstractAction(MR3 mr3, String name) {
		super(name);
		this.mr3 = mr3;
	}

	public MR3AbstractAction(MR3 mr3, String name, ImageIcon icon) {
		super(name, icon);
		this.mr3 = mr3;
	}

	public String getName() {
		return (String) getValue(NAME);
	}

}
