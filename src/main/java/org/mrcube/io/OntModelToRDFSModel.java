/*
 * Project Name: MR^3 (Meta-Model Management based on RDFs Revision Reflection)
 * Project Website: http://mrcube.org/
 * 
 * Copyright (C) 2003-2019 Yamaguchi Laboratory, Keio University. All rights reserved.
 * 
 * This file is part of MR^3.
 * 
 * MR^3 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * MR^3 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with MR^3.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */

package org.mrcube.io;

import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntProperty;
import org.apache.jena.ontology.OntResource;
import org.apache.jena.rdf.model.*;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;

import java.util.Iterator;

/**
 * @author Takeshi Morita
 */
class OntModelToRDFSModel {

    public static Model convertOntModelToRDFSModel(OntModel ontModel) {
        Model rdfsModel = ModelFactory.createDefaultModel();
        rdfsModel.setNsPrefixes(ontModel.getNsPrefixMap());
        addClassModel(ontModel, rdfsModel);
        addObjectPropertyModel(ontModel, rdfsModel);
        addDatatypePropertyModel(ontModel, rdfsModel);

        // StringWriter out = new StringWriter();
        // rdfsModel.write(new PrintWriter(out), "RDF/XML-ABBREV");
        // System.out.println(out.toString());

        return rdfsModel;
    }

    private static void addType(OntResource ontRes, Resource type, Model rdfsModel) {
        Statement stmt = rdfsModel.createStatement(ontRes, RDF.type, type);
        rdfsModel.add(stmt);
    }

    private static void addComments(OntResource ontRes, Model rdfsModel) {
        for (Iterator i = ontRes.listComments(null); i.hasNext();) {
            Literal literal = (Literal) i.next();
            Statement stmt = rdfsModel.createStatement(ontRes, RDFS.comment, literal);
            rdfsModel.add(stmt);
        }
    }

    private static void addLabels(OntResource ontRes, Model rdfsModel) {
        for (Iterator i = ontRes.listLabels(null); i.hasNext();) {
            Literal literal = (Literal) i.next();
            Statement stmt = rdfsModel.createStatement(ontRes, RDFS.label, literal);
            rdfsModel.add(stmt);
        }
    }

    private static void addDomains(OntProperty ontProp, Model rdfsModel) {
        for (Iterator i = ontProp.listDomain(); i.hasNext();) {
            OntResource res = (OntResource) i.next();
            Statement stmt = rdfsModel.createStatement(ontProp, RDFS.domain, res);
            rdfsModel.add(stmt);
        }
    }

    private static void addRanges(OntProperty ontProp, Model rdfsModel) {
        for (Iterator i = ontProp.listRange(); i.hasNext();) {
            OntResource res = (OntResource) i.next();
            Statement stmt = rdfsModel.createStatement(ontProp, RDFS.range, res);
            rdfsModel.add(stmt);
        }
    }

    private static void addSubClassOf(OntResource ontRes, OntResource ontSupRes, Model rdfsModel) {
        Statement stmt = rdfsModel.createStatement(ontRes, RDFS.subClassOf, ontSupRes);
        rdfsModel.add(stmt);
        addType(ontRes, OWL.Class, rdfsModel);
    }

    private static void addSubPropertyOf(OntResource ontRes, OntResource ontSupRes, Model rdfsModel) {
        Statement stmt = rdfsModel.createStatement(ontRes, RDFS.subPropertyOf, ontSupRes);
        rdfsModel.add(stmt);
        addType(ontRes, OWL.ObjectProperty, rdfsModel);
    }

    private static void addClassModel(OntModel ontModel, Model rdfsModel) {
        for (Iterator i = ontModel.listNamedClasses(); i.hasNext();) {
            OntClass ontClass = (OntClass) i.next();
            addType(ontClass, OWL.Class, rdfsModel);
            addLabels(ontClass, rdfsModel);
            addComments(ontClass, rdfsModel);

            for (Iterator j = ontClass.listSubClasses(true); j.hasNext();) {
                OntClass subOntClass = (OntClass) j.next();
                addSubClassOf(subOntClass, ontClass, rdfsModel);
            }
        }
    }

    private static void addPropertyModel(OntProperty ontProp, Model rdfsModel) {
        addLabels(ontProp, rdfsModel);
        addComments(ontProp, rdfsModel);
        addDomains(ontProp, rdfsModel);
        addRanges(ontProp, rdfsModel);
        for (Iterator i = ontProp.listSubProperties(true); i.hasNext();) {
            OntProperty subOntProp = (OntProperty) i.next();
            if (!subOntProp.equals(ontProp)) {
                addSubPropertyOf(subOntProp, ontProp, rdfsModel);
            } else {
                // System.out.println("Property: " + ontProp);
                // System.out.println("SubProperty: " + subOntProp);
            }
        }
    }

    private static void addObjectPropertyModel(OntModel ontModel, Model rdfsModel) {
        for (Iterator i = ontModel.listObjectProperties(); i.hasNext();) {
            OntProperty ontProp = (OntProperty) i.next();
            addType(ontProp, OWL.ObjectProperty, rdfsModel);
            addPropertyModel(ontProp, rdfsModel);
        }
    }

    private static void addDatatypePropertyModel(OntModel ontModel, Model rdfsModel) {
        for (Iterator i = ontModel.listDatatypeProperties(); i.hasNext();) {
            OntProperty ontProp = (OntProperty) i.next();
            addType(ontProp, OWL.DatatypeProperty, rdfsModel);
            addPropertyModel(ontProp, rdfsModel);
        }
    }

}
