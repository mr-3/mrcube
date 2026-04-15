/*
 * Project Name: MR^3 (Meta-Model Management based on RDFs Revision Reflection)
 * Project Website: https://mr-3.github.io/
 * 
 * Copyright (C) 2003-2025 Takeshi Morita. All rights reserved.
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

package jp.ac.aoyama.it.ke.mrcube.io;

import org.apache.jena.ontapi.model.OntClass;
import org.apache.jena.ontapi.model.OntModel;
import org.apache.jena.ontapi.model.OntProperty;
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

    private static void addType(RDFNode ontRes, Resource type, Model rdfsModel) {
        if (ontRes.isResource()) {
            Statement stmt = rdfsModel.createStatement(ontRes.asResource(), RDF.type, type);
            rdfsModel.add(stmt);
        }
    }

    private static void addComments(RDFNode ontRes, Model rdfsModel) {
        if (!ontRes.isResource()) return;
        Resource res = ontRes.asResource();
        res.listProperties(RDFS.comment).forEachRemaining(stmt -> {
            rdfsModel.add(res, RDFS.comment, stmt.getObject());
        });
    }

    private static void addLabels(RDFNode ontRes, Model rdfsModel) {
        if (!ontRes.isResource()) return;
        Resource res = ontRes.asResource();
        res.listProperties(RDFS.label).forEachRemaining(stmt -> {
            rdfsModel.add(res, RDFS.label, stmt.getObject());
        });
    }

    private static void addDomains(OntProperty ontProp, Model rdfsModel) {
        ontProp.domains().forEach(domainRes -> {
            rdfsModel.add(ontProp, RDFS.domain, domainRes);
        });
    }

    private static void addRanges(OntProperty ontProp, Model rdfsModel) {
        ontProp.ranges().forEach(rangeRes -> {
            rdfsModel.add(ontProp, RDFS.range, rangeRes);
        });
    }

    private static void addSubClassOf(OntClass ontClass, OntClass.Named ontSupClass, Model rdfsModel) {
        Statement stmt = rdfsModel.createStatement(ontClass, RDFS.subClassOf, ontSupClass);
        rdfsModel.add(stmt);
        addType(ontClass, OWL.Class, rdfsModel);
    }

    private static void addSubPropertyOf(OntProperty ontProp, OntProperty ontSupProp, Model rdfsModel) {
        Statement stmt = rdfsModel.createStatement(ontProp, RDFS.subPropertyOf, ontSupProp);
        rdfsModel.add(stmt);
        addType(ontProp, OWL.ObjectProperty, rdfsModel);
    }

    private static void addClassModel(OntModel ontModel, Model rdfsModel) {
        ontModel.ontObjects(OntClass.Named.class).forEach(ontClass -> {
            addType(ontClass, OWL.Class, rdfsModel);
            addLabels(ontClass, rdfsModel);
            addComments(ontClass, rdfsModel);

            ontClass.subClasses(true).forEach(subOntClass -> {
                addSubClassOf(subOntClass, ontClass, rdfsModel);
            });
        });
    }

    private static void addPropertyModel(OntProperty ontProp, Model rdfsModel) {
        addLabels(ontProp, rdfsModel);
        addComments(ontProp, rdfsModel);
        addDomains(ontProp, rdfsModel);
        addRanges(ontProp, rdfsModel);

        ontProp.subProperties(true)
                .filter(sub -> !sub.equals(ontProp)) // 自分自身を除外
                .forEach(subOntProp -> {
                    addSubPropertyOf(subOntProp, ontProp, rdfsModel);
                });

//        for (Iterator i = ontProp.listSubProperties(true); i.hasNext();) {
//            OntProperty subOntProp = (OntProperty) i.next();
//            if (!subOntProp.equals(ontProp)) {
//                addSubPropertyOf(subOntProp, ontProp, rdfsModel);
//            } else {
//                // System.out.println("Property: " + ontProp);
//                // System.out.println("SubProperty: " + subOntProp);
//            }
//        }
    }

    private static void addObjectPropertyModel(OntModel ontModel, Model rdfsModel) {
        ontModel.objectProperties().forEach(ontProp -> {
            addType(ontProp, OWL.ObjectProperty, rdfsModel);
            addPropertyModel(ontProp, rdfsModel);
        });
    }

    private static void addDatatypePropertyModel(OntModel ontModel, Model rdfsModel) {
        ontModel.dataProperties().forEach(ontProp -> {
            addType(ontProp, OWL.DatatypeProperty, rdfsModel);
            addPropertyModel(ontProp, rdfsModel);
        });
    }

}
