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
import com.hp.hpl.jena.vocabulary.*;

/**
 * @author takeshi morita
 */
public class OWLImportPlugin extends MR3Plugin {

	private Model convertOntModelToRDFSModel(OntModel ontModel) {
		Model rdfsModel = ModelFactory.createDefaultModel();
		addClassModel(ontModel, rdfsModel);
		addObjectPropertyModel(ontModel, rdfsModel);
		addDatatypePropertyModel(ontModel, rdfsModel);

		StringWriter out = new StringWriter();
		rdfsModel.write(new PrintWriter(out), "RDF/XML-ABBREV");
		//		System.out.println(out.toString());

		return rdfsModel;
	}

	private void addType(OntResource ontRes, Resource type, Model rdfsModel) {
		Statement stmt = rdfsModel.createStatement(ontRes, RDF.type, type);
		rdfsModel.add(stmt);
	}

	private void addComments(OntResource ontRes, Model rdfsModel) {
		for (Iterator i = ontRes.listComments(null); i.hasNext();) {
			Literal literal = (Literal) i.next();
			Statement stmt = rdfsModel.createStatement(ontRes, RDFS.comment, literal);
			rdfsModel.add(stmt);
		}
	}

	private void addLabels(OntResource ontRes, Model rdfsModel) {
		for (Iterator i = ontRes.listLabels(null); i.hasNext();) {
			Literal literal = (Literal) i.next();
			Statement stmt = rdfsModel.createStatement(ontRes, RDFS.comment, literal);
			rdfsModel.add(stmt);
		}
	}

	private void addDomains(OntProperty ontProp, Model rdfsModel) {
		for (Iterator i = ontProp.listDomain(); i.hasNext();) {
			OntResource res = (OntResource) i.next();
			Statement stmt = rdfsModel.createStatement(ontProp, RDFS.domain, res);
			rdfsModel.add(stmt);
		}
	}

	private void addRanges(OntProperty ontProp, Model rdfsModel) {
		for (Iterator i = ontProp.listRange(); i.hasNext();) {
			OntResource res = (OntResource) i.next();
			Statement stmt = rdfsModel.createStatement(ontProp, RDFS.range, res);
			rdfsModel.add(stmt);
		}
	}

	private void addSubClassOf(OntResource ontRes, OntResource ontSupRes, Model rdfsModel) {
		Statement stmt = rdfsModel.createStatement(ontRes, RDFS.subClassOf, ontSupRes);
		rdfsModel.add(stmt);
		addType(ontRes, OWL.Class, rdfsModel);
	}

	private void addSubPropertyOf(OntResource ontRes, OntResource ontSupRes, Model rdfsModel) {
		Statement stmt = rdfsModel.createStatement(ontRes, RDFS.subPropertyOf, ontSupRes);
		rdfsModel.add(stmt);
		addType(ontRes, OWL.ObjectProperty, rdfsModel);
	}

	private void addClassModel(OntModel ontModel, Model rdfsModel) {
		for (Iterator i = ontModel.listNamedClasses(); i.hasNext();) {
			OntClass ontClass = (OntClass) i.next();
			addType(ontClass, OWL.Class, rdfsModel);
			addLabels(ontClass, rdfsModel);
			addComments(ontClass, rdfsModel);

			for (Iterator j = ontClass.listSubClasses(); j.hasNext();) {
				OntClass subOntClass = (OntClass) j.next();
				addSubClassOf(subOntClass, ontClass, rdfsModel);
			}
		}
	}

	private void addPropertyModel(OntProperty ontProp, Model rdfsModel) {
		addLabels(ontProp, rdfsModel);
		addComments(ontProp, rdfsModel);
		addDomains(ontProp, rdfsModel);
		addRanges(ontProp, rdfsModel);
		for (Iterator i = ontProp.listSubProperties(); i.hasNext();) {
			OntProperty subOntProp = (OntProperty) i.next();
			if (!subOntProp.equals(ontProp)) {
				addSubPropertyOf(subOntProp, ontProp, rdfsModel);
			} else {
//				System.out.println("Property: " + ontProp);
//				System.out.println("SubProperty: " + subOntProp);
			}
		}
	}

	private void addObjectPropertyModel(OntModel ontModel, Model rdfsModel) {
		for (Iterator i = ontModel.listObjectProperties(); i.hasNext();) {
			OntProperty ontProp = (OntProperty) i.next();
			addType(ontProp, OWL.ObjectProperty, rdfsModel);
			addPropertyModel(ontProp, rdfsModel);
		}
	}

	private void addDatatypePropertyModel(OntModel ontModel, Model rdfsModel) {
		for (Iterator i = ontModel.listDatatypeProperties(); i.hasNext();) {
			OntProperty ontProp = (OntProperty) i.next();
			addType(ontProp, OWL.DatatypeProperty, rdfsModel);
			addPropertyModel(ontProp, rdfsModel);
		}
	}

	public void exec() {
		JFileChooser jfc = new JFileChooser();
		if (jfc.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
			File file = jfc.getSelectedFile();
			try {
				OntModel ontModel = ModelFactory.createOntologyModel();
				RDFReader jenaReader = new JenaReader();
				Reader r = new InputStreamReader(new FileInputStream(file), "UTF8");
				jenaReader.read(ontModel, r, getBaseURI());
				mergeRDFSModel(convertOntModelToRDFSModel(ontModel));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
