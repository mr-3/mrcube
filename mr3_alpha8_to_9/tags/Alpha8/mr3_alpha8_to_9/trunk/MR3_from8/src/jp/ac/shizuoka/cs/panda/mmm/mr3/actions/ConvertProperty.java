/*
 * Created on 2003/07/19
 *
 */
package jp.ac.shizuoka.cs.panda.mmm.mr3.actions;

import java.awt.event.*;
import java.io.*;

import jp.ac.shizuoka.cs.panda.mmm.mr3.*;

import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.rdf.model.impl.*;

/**
 * @author takeshi morita
 *
 */
public class ConvertProperty extends AbstractActionFile {

	public ConvertProperty(MR3 mr3, String name) {
		super(mr3, name);
	}

	public void convertPropertySRC(boolean isSelected) {
		Writer output = new StringWriter();
		try {
			Model model = null;
			if (isSelected) {
				model = mr3.getSelectedPropertyModel();
			} else {
				model = mr3.getPropertyModel();
			}
			RDFWriter writer = new RDFWriterFImpl().getWriter("RDF/XML-ABBREV");
			writeModel(model, output, writer);
		} catch (RDFException e) {
			e.printStackTrace();
		}

		mr3.getSourceArea().setText(output.toString());
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals("RDFS(Property)/XML")) {
			convertPropertySRC(false);
		} else {
			convertPropertySRC(true);
		}
		showSrcView();
	}

}