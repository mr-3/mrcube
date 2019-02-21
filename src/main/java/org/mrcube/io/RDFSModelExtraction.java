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

import org.apache.jena.rdf.model.*;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.jgraph.graph.GraphCell;
import org.mrcube.jgraph.GraphManager;
import org.mrcube.models.*;

import java.util.HashSet;
import java.util.Set;

/*
 *
 * @author Takeshi Morita
 *
 */
public class RDFSModelExtraction {

    private final GraphManager gmanager;

    public RDFSModelExtraction(GraphManager gm) {
        gmanager = gm;
    }

    public Model extractClassModel(Model orgModel) {
        Model classModel = ModelFactory.createDefaultModel();
        addInnerClassModel(orgModel);
        Set<Resource> classClassList = gmanager.getClassClassList();
        Set<Resource> findClassClassList = new HashSet<>();
        for (Resource clsClass : classClassList) {
            gmanager.findMetaClass(orgModel, clsClass, findClassClassList);
        }
        classClassList.addAll(findClassClassList);
        // System.out.println(classClassList);
        gmanager.setClassClassList(classClassList);
        Model removeModel = ModelFactory.createDefaultModel();
        for (Resource clsClass : classClassList) {
            for (Resource classResource : orgModel.listSubjectsWithProperty(RDF.type, clsClass).toList()) {
                ClassModel info = getClassResInfo(classResource);
                setDefaultSubClassOf(classResource, info);
                removeModel.add(extractRDFSModel(classModel, classResource, info));
            }
        }
        orgModel.remove(removeModel);
        return classModel;
    }

    // subPropertyOfが省略されたプロパティは，Propertyクラスのサブクラスとみなす、
    private void setDefaultSubPropertyOf(Resource property) {
        if (!property.hasProperty(RDFS.subPropertyOf)) {
            RDFSModelMap rdfsModelMap = gmanager.getCurrentRDFSInfoMap();
            rdfsModelMap.addRootProperties(property);
        }
    }

    // subClassOfで指定されているクラスをオリジナルに追加．（定義されていないかもしれないので）
    private void addInnerClassModel(Model orgModel) {
        Model tmpModel = ModelFactory.createDefaultModel();
        for (Resource classRes : orgModel.listSubjectsWithProperty(RDF.type, RDFS.Class).toList()) {
            for (Statement stmt : classRes.listProperties().toList()) {
                if (stmt.getPredicate().equals(RDFS.subClassOf)) {
                    tmpModel.add(ResourceFactory.createStatement((Resource) stmt.getObject(), RDF.type, RDFS.Class));
                }
            }
        }
        orgModel.add(tmpModel);
    }

    // subPropertyOfで指定されているプロパティとdomain, rangeで指定されているクラスをオリジナルに追加
    // 先にプロパティを読み込むことで，domain, rangeに指定されたクラスを処理できる
    private void addInnerPropertyModel(Model orgModel) {
        Model tmpModel = ModelFactory.createDefaultModel();
        for (Resource propRes : orgModel.listSubjectsWithProperty(RDF.type, RDF.Property).toList()) {
            for (Statement stmt : propRes.listProperties().toList()) {
                if (stmt.getPredicate().equals(RDFS.domain) || stmt.getPredicate().equals(RDFS.range)) {
                    tmpModel.add(ResourceFactory.createStatement((Resource) stmt.getObject(), RDF.type, RDFS.Class));
                } else if (stmt.getPredicate().equals(RDFS.subPropertyOf)) {
                    tmpModel.add(ResourceFactory.createStatement((Resource) stmt.getObject(), RDF.type, RDF.Property));
                }
            }
        }
        orgModel.add(tmpModel);
    }

    public Model extractPropertyModel(Model orgModel) {
        Model propertyModel = ModelFactory.createDefaultModel();
        addInnerPropertyModel(orgModel);
        Set<Resource> propClassList = gmanager.getPropertyClassList();
        Set<Resource> findPropClassList = new HashSet<>();
        for (Resource propClass : propClassList) {
            gmanager.findMetaClass(orgModel, propClass, findPropClassList);
        }
        propClassList.addAll(findPropClassList);
        // System.out.println(propClassList);
        gmanager.setPropertyClassList(propClassList);
        Model removeModel = ModelFactory.createDefaultModel();
        for (Resource propClass : propClassList) {
            for (Resource propClassRes : orgModel.listSubjectsWithProperty(RDF.type, propClass).toList()) {
                PropertyModel info = getPropertyResInfo(propClassRes);
                setDefaultSubPropertyOf(propClassRes);
                removeModel.add(extractRDFSModel(propertyModel, propClassRes, info));
                info.setMetaClass(propClass.toString());
            }
        }
        orgModel.remove(removeModel);

        return propertyModel;
    }

    private Model extractRDFSModel(Model rdfsModel, Resource metaResource, RDFSModel info) {
        Model removeModel = ModelFactory.createDefaultModel();
        for (Statement stmt : metaResource.listProperties().toList()) {
            if (setRDFSInfo(stmt, info)) {
                rdfsModel.add(stmt);
            } else {
                info.addStatement(stmt); // RDFS以外のプロパティを持つ文を保存
            }
            removeModel.add(stmt);
        }

        info.setURI(metaResource.toString());
        RDFSModelMap rdfsModelMap = gmanager.getCurrentRDFSInfoMap();
        rdfsModelMap.putResourceInfo(metaResource, info);

        return removeModel;
    }

    // Resourceクラス以外のsubClassOfが省略されたクラスは，
    // Resourceクラスのサブクラスとみなす
    private void setDefaultSubClassOf(Resource rdfsResource, ClassModel info) {
        if (!rdfsResource.equals(RDFS.Resource) && !rdfsResource.hasProperty(RDFS.subClassOf)) {
            ClassModel supResInfo = getClassResInfo(RDFS.Resource);
            supResInfo.addSubClass(rdfsResource);
            info.addSupClass(RDFS.Resource);
            RDFSModelMap rdfsModelMap = gmanager.getCurrentRDFSInfoMap();
            rdfsModelMap.putResourceInfo(RDFS.Resource, supResInfo);
        }
    }

    private boolean setClassInfo(Statement stmt, ClassModel info) {
        Resource subject = stmt.getSubject();
        Property predicate = stmt.getPredicate();
        RDFNode object = stmt.getObject();

        if (setBaseInfo(stmt, info)) {
            return true;
        }

        if (predicate.equals(RDFS.subClassOf)) { // rdfs:subClassOf
            // subject < object
            // info -> subject info supInfo -> object info
            ClassModel supResInfo = getClassResInfo((Resource) object);
            supResInfo.addSubClass(subject);
            info.addSupClass((Resource) object);
            RDFSModelMap rdfsModelMap = gmanager.getCurrentRDFSInfoMap();
            rdfsModelMap.putResourceInfo((Resource) object, supResInfo);
        } else {
            return false;
        }

        return true;
    }

    private boolean setPropertyInfo(Statement stmt, PropertyModel info) {
        Resource subject = stmt.getSubject();
        Property predicate = stmt.getPredicate();
        RDFNode object = stmt.getObject();

        if (setBaseInfo(stmt, info)) {
            return true;
        }

        Resource objectResource = null;
        if (object instanceof Resource) {
            objectResource = (Resource) object;
        }

        if (predicate.equals(RDFS.domain)) { // rdfs:domain
            GraphCell cell = gmanager.getClassCell(objectResource, false);
            info.addDomain(cell);
        } else if (predicate.equals(RDFS.range)) { // rdfs:range
            GraphCell cell = gmanager.getClassCell(objectResource, false);
            info.addRange(cell);
        } else if (predicate.equals(RDFS.subPropertyOf)) { // rdfs:subPropertyOf
            // subject < object
            // info -> subject info supInfo -> object info
            PropertyModel supResInfo = getPropertyResInfo(objectResource);
            // rdfsModelMap.addRootProperties((Resource)object); //一時しのぎ
            supResInfo.addSubProperty(subject);
            RDFSModelMap rdfsModelMap = gmanager.getCurrentRDFSInfoMap();
            rdfsModelMap.putResourceInfo(objectResource, supResInfo);
            info.addSupProperty(object);
        } else {
            return false;
        }
        return true;
    }

    private boolean setBaseInfo(Statement stmt, RDFSModel info) {
        Property predicate = stmt.getPredicate();
        RDFNode object = stmt.getObject();

        if (predicate.equals(RDFS.label)) { // rdfs:label
            MR3Literal literal = new MR3Literal((Literal) object);
            info.addLabel(literal);
        } else if (predicate.equals(RDFS.comment)) { // rdfs:comment
            MR3Literal literal = new MR3Literal((Literal) object);
            info.addComment(literal);
        } else if (predicate.equals(RDF.type)) {
            info.setMetaClass(object.toString());
        } else if (predicate.getURI().matches(MR3Resource.conceptLabel.getURI() + ".*")) {
            // subClassOfやsubPropertyOfと同時に使った場合にのみ有効
            RDFSModelMap rdfsModelMap = gmanager.getCurrentRDFSInfoMap();
            rdfsModelMap.addPropertyLabelModel(stmt);
        } else {
            return false;
        }
        return true;
    }

    private boolean setRDFSInfo(Statement stmt, RDFSModel info) {
        if (info instanceof ClassModel) {
            return setClassInfo(stmt, (ClassModel) info);
        } else if (info instanceof PropertyModel) {
            return setPropertyInfo(stmt, (PropertyModel) info);
        } else {
            return false;
        }
    }

    private ClassModel getClassResInfo(Resource resource) {
        RDFSModelMap rdfsModelMap = gmanager.getCurrentRDFSInfoMap();
        ClassModel info = (ClassModel) rdfsModelMap.getResourceInfo(resource);
        if (info == null)
            info = new ClassModel("");
        return info;
    }

    private PropertyModel getPropertyResInfo(Resource resource) {
        RDFSModelMap rdfsModelMap = gmanager.getCurrentRDFSInfoMap();
        PropertyModel info = (PropertyModel) rdfsModelMap.getResourceInfo(resource);
        if (info == null)
            info = new PropertyModel("");
        return info;
    }
}
