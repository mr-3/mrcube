package org.semanticweb.mmm.mr3.data;

import com.hp.hpl.jena.rdf.model.*;

/**
 * @author takeshi morita
 * 
 * MR^3‚Å—p‚¢‚éProperty‚ÆResource
 */
public class MR3Resource {

	private static final String DEFAULT_URI = "http://panda.cs.inf.shizuoka.ac.jp/mmm/mr3#";
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

	public static Resource DefaultURI;
	public static Property DefaultLang;
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

	static {
		try {
			DefaultURI = ResourceFactory.createResource(DEFAULT_URI); 
			DefaultLang = ResourceFactory.createProperty(DEFAULT_URI+DEFAULT_LANG); 
			Property = ResourceFactory.createProperty(DEFAULT_URI + PROPERTY);
			Nil = ResourceFactory.createProperty(DEFAULT_URI + NIL);
			Empty = ResourceFactory.createResource(DEFAULT_URI + EMPTY);
			Literal = ResourceFactory.createResource(DEFAULT_URI + LITERAL);
			LiteralProperty = ResourceFactory.createProperty(DEFAULT_URI + LITERAL_PROPERTY);
			HasLiteralResource = ResourceFactory.createProperty(DEFAULT_URI + HAS_LITERAL_RESOURCE);
			LiteralLang = ResourceFactory.createProperty(DEFAULT_URI + LITERAL_LANG);
			LiteralDatatype = ResourceFactory.createProperty(DEFAULT_URI+LITERAL_DATATYPE);
			LiteralString = ResourceFactory.createProperty(DEFAULT_URI + LITERAL_STRING);
			PointX = ResourceFactory.createProperty(DEFAULT_URI + POINT_X);
			PointY = ResourceFactory.createProperty(DEFAULT_URI + POINT_Y);
			NodeWidth = ResourceFactory.createProperty(DEFAULT_URI + NODE_WIDTH);
			NodeHeight = ResourceFactory.createProperty(DEFAULT_URI + NODE_HEIGHT);
			Prefix = ResourceFactory.createProperty(DEFAULT_URI + PREFIX);
			IsPrefixAvailable = ResourceFactory.createProperty(DEFAULT_URI + IS_PREFIX_AVAILABLE);
		} catch (RDFException e) {
			e.printStackTrace();
		}
	}

	public static String getURI() {
		return DEFAULT_URI;
	}
}
