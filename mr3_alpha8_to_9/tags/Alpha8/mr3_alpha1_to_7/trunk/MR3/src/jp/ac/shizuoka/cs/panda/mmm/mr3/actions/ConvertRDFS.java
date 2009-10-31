/*
 * Created on 2003/07/19
 *
 */
package jp.ac.shizuoka.cs.panda.mmm.mr3.actions;

import java.awt.event.*;
import java.io.*;

import jp.ac.shizuoka.cs.panda.mmm.mr3.*;

import com.hp.hpl.mesa.rdf.jena.common.*;
import com.hp.hpl.mesa.rdf.jena.model.*;

/**
 * @author takeshi morita
 *
 */
public class ConvertRDFS extends AbstractActionFile {
	
	public ConvertRDFS(MR3 mr3, String title) {
		super(mr3, title);
	}

	public void actionPerformed(ActionEvent e) {
		String command = e.getActionCommand();
		try {
			Model model = null;
			if (command.equals("RDFS(Class/Property)/XML")) {
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
