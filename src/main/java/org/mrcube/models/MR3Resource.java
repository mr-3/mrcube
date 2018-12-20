/*
 * Project Name: MR^3 (Meta-Model Management based on RDFs Revision Reflection)
 * Project Website: http://mrcube.org/
 * 
 * Copyright (C) 2003-2018 Yamaguchi Laboratory, Keio University. All rights reserved.
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

package org.mrcube.models;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;

/**
 * @author Takeshi Morita
 * 
 *         MR3で用いるPropertyとResource
 */
public class MR3Resource {

	private static final String DEFAULT_URI = "http://mr3.sourceforge.net#";
	private static final String DEFAULT_LANG = "DefaultLang";
	private static final String PROPERTY = "Property";
	private static final String NIL = "nil";
	private static final String EMPTY = "Empty";
	private static final String LITERAL = "Literal";
	private static final String HAS_LITERAL_RESOURCE = "hasLiteralResource";
	private static final String LITERAL_PROPERTY = "literalProperty";
	private static final String LITERAL_LANG = "literalLang";
	private static final String LITERAL_STRING = "literalValue";
	private static final String LITERAL_DATATYPE = "literalDatatype";
	private static final String POINT_X = "pointX";
	private static final String POINT_Y = "pointY";
	private static final String NODE_WIDTH = "nodeWidth";
	private static final String NODE_HEIGHT = "nodeHeight";
	private static final String PREFIX = "prefix";
	private static final String IS_PREFIX_AVAILABLE = "isPrefixAvailable";
	private static final String CONCEPT_LABEL = "conceptLabel";

	public static Resource DefaultURI;
	public static org.apache.jena.rdf.model.Property DefaultLang;
	public static Property Property;
	public static Property Nil;
	public static Resource Empty;
	public static Resource Literal;
	public static Property LiteralProperty;
	public static Property HasLiteralResource;
	public static Property LiteralLang;
	public static Property LiteralDatatype;
	public static Property LiteralString;
	public static Property PointX;
	public static Property PointY;
	public static Property NodeWidth;
	public static Property NodeHeight;
	public static Property Prefix;
	public static Property IsPrefixAvailable;
	public static Property conceptLabel;

	static {
		DefaultURI = ResourceFactory.createResource(DEFAULT_URI);
		DefaultLang = ResourceFactory.createProperty(DEFAULT_URI + DEFAULT_LANG);
		Property = ResourceFactory.createProperty(DEFAULT_URI + PROPERTY);
		Nil = ResourceFactory.createProperty(DEFAULT_URI + NIL);
		Empty = ResourceFactory.createResource(DEFAULT_URI + EMPTY);
		Literal = ResourceFactory.createResource(DEFAULT_URI + LITERAL);
		LiteralProperty = ResourceFactory.createProperty(DEFAULT_URI + LITERAL_PROPERTY);
		HasLiteralResource = ResourceFactory.createProperty(DEFAULT_URI + HAS_LITERAL_RESOURCE);
		LiteralLang = ResourceFactory.createProperty(DEFAULT_URI + LITERAL_LANG);
		LiteralDatatype = ResourceFactory.createProperty(DEFAULT_URI + LITERAL_DATATYPE);
		LiteralString = ResourceFactory.createProperty(DEFAULT_URI + LITERAL_STRING);
		PointX = ResourceFactory.createProperty(DEFAULT_URI + POINT_X);
		PointY = ResourceFactory.createProperty(DEFAULT_URI + POINT_Y);
		NodeWidth = ResourceFactory.createProperty(DEFAULT_URI + NODE_WIDTH);
		NodeHeight = ResourceFactory.createProperty(DEFAULT_URI + NODE_HEIGHT);
		Prefix = ResourceFactory.createProperty(DEFAULT_URI + PREFIX);
		IsPrefixAvailable = ResourceFactory.createProperty(DEFAULT_URI + IS_PREFIX_AVAILABLE);
		conceptLabel = ResourceFactory.createProperty(DEFAULT_URI + CONCEPT_LABEL);
	}

	public static String getURI() {
		return DEFAULT_URI;
	}
}
