/*
 * Created on 2003/07/19
 *
 */
package jp.ac.shizuoka.cs.panda.mmm.mr3.actions;

import java.awt.event.*;
import java.io.*;

import jp.ac.shizuoka.cs.panda.mmm.mr3.*;
import jp.ac.shizuoka.cs.panda.mmm.mr3.util.*;

import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.rdf.model.impl.*;

/**
 * @author takeshi morita
 *
 */
public class ConvertRDFSDoc extends AbstractActionFile {
	
	public ConvertRDFSDoc(MR3 mr3, String title) {
		super(mr3, title);
	}

	public void actionPerformed(ActionEvent e) {
		String command = e.getActionCommand();
		try {
			Model model = null;
			if (command.equals(Translator.getString("Component.Convert.RDFS/XML.RDFS(Class/Property).Text"))) {
				model = mr3.getRDFSModel();
			} else {
				model = mr3.getSelectedRDFSModel();
			}

			Writer output = new StringWriter();
			RDFWriter writer = new RDFWriterFImpl().getWriter("RDF/XML-ABBREV");
			writeModel(model, output, writer);
			mr3.getSourceArea().setText(output.toString());
			showSrcView();
		} catch (RDFException rex) {
			rex.printStackTrace();
		}
	}
}
