/*
 * Created on 2003/11/19
 *  
 */
package org.semanticweb.mmm.mr3.owlPlugin;

import java.io.*;
import java.util.*;

import javax.swing.*;

import org.semanticweb.mmm.mr3.plugin.*;

import com.hp.hpl.jena.ontology.*;
import com.hp.hpl.jena.rdf.arp.*;
import com.hp.hpl.jena.rdf.model.*;

/**
 * @author takeshi morita
 */
public class OWLImportPlugin extends MR3Plugin {

	public void exec() {
		JFileChooser jfc = new JFileChooser();
		if (jfc.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
			File file = jfc.getSelectedFile();
			try {
				OntModel model = ModelFactory.createOntologyModel();
				RDFReader jenaReader = new JenaReader();
				Reader r = new InputStreamReader(new FileInputStream(file), "UTF8");
				jenaReader.read(model, r, getBaseURI());

				for (Iterator i = model.listNamedClasses(); i.hasNext();) {
					OntClass ontClass = (OntClass) i.next();
					System.out.println("Named Classes: " + ontClass);
					for (Iterator j = ontClass.listSubClasses(); j.hasNext();) {
						OntClass subOntClass = (OntClass)j.next();
						System.out.println("Named Class: "+ontClass+" subClass: "+subOntClass);
					}
				}

				for (Iterator i = model.listObjectProperties(); i.hasNext();) {
					OntProperty ontProp = (OntProperty) i.next();
					System.out.println("Object Properties: " + ontProp);
				}

				for (Iterator i = model.listDatatypeProperties(); i.hasNext();) {
					OntProperty ontProp = (OntProperty) i.next();
					System.out.println("Datatype Properties: " + ontProp);
				}

				//replaceProjectModel(model);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
