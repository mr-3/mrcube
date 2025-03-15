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

package jp.ac.aoyama.it.ke.mrcube.models;

import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import jp.ac.aoyama.it.ke.mrcube.utils.Utilities;

import java.awt.*;
import java.io.Serializable;

/**
 * @author Takeshi Morita
 */
public class MR3Literal implements Serializable {

    private String str;
    private String lang;
    private RDFDatatype dataType;
    private Rectangle litRect;
    private Resource resource;
    private Property property;
    private static final long serialVersionUID = 75073546338792276L;

    public MR3Literal() {
        str = "";
        lang = "";
        dataType = null;
        litRect = new Rectangle();
        resource = null;
        property = null;
    }

    public MR3Literal(String s, String l, RDFDatatype dt) {
        str = s;
        lang = l;
        dataType = dt;
    }

    public MR3Literal(MR3Literal lit) {
        str = lit.getString();
        lang = lit.getLanguage();
        dataType = lit.getDatatype();
    }

    public MR3Literal(Literal lit) {
        str = lit.getString();
        lang = lit.getLanguage();
        if (!lang.isEmpty()) {
            dataType = null;
        } else {
            dataType = lit.getDatatype();
        }
    }

    public boolean equals(MR3Literal lit) {
        if (dataType == null && lit.getDatatype() != null) {
            return false;
        }
        if (dataType != null && lit.getDatatype() == null) {
            return false;
        }
        if (dataType == null && lit.getDatatype() == null) {
            return lit.getLanguage().equals(lang) && lit.getString().equals(str);
        }
        return lit.getLanguage().equals(lang) && lit.getDatatype().equals(dataType)
                && lit.getString().equals(str);
    }

    public Literal getLiteral() {
        return Utilities.createLiteral(str, lang, dataType);
    }

    public void setString(String s) {
        str = s;
    }

    public String getString() {
        return str;
    }

    public void setLanguage(String s) {
        lang = s;
    }

    public String getLanguage() {
        return lang;
    }

    public void setDatatype(RDFDatatype dt) {
        dataType = dt;
    }

    public RDFDatatype getDatatype() {
        return dataType;
    }

    public void setRectangle(Rectangle rect) {
        litRect = rect;
    }

    public Rectangle getRectangle() {
        return litRect;
    }

    public Point getLocation() {
        return litRect.getLocation();
    }

    public void setX(int x) {
        litRect.x = x;
    }

    public void setY(int y) {
        litRect.y = y;
    }

    public void setWidth(int width) {
        litRect.width = width;
    }

    public void setHeight(int height) {
        litRect.height = height;
    }

    public void setResource(String res) {
        resource = ResourceFactory.createResource(res);
    }

    public Resource getResource() {
        return resource;
    }

    public void setProperty(String prop) {
        property = ResourceFactory.createProperty(prop);
    }

    public Property getProperty() {
        return property;
    }

    public String toString() {
        return str;
    }
}
