package jp.ac.shizuoka.cs.panda.mmm.mr3.data;

import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.rdf.model.impl.*;

/**
 * @author takeshi morita
 * 
 * MRCUBE‚ÅŽg‚¤Property‚ÆResource
 */
public class MR3Resource {

	private static final String DEFAULT_URI = "http://panda.cs.inf.shizuoka.ac.jp/mmm/mr3#";
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

	public static Resource Default_URI;
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
	public static Property Is_prefix_available;

	static {
		try {
			Default_URI = new ResourceImpl(DEFAULT_URI);
			Property = new PropertyImpl(DEFAULT_URI + PROPERTY);
			Nil = new PropertyImpl(DEFAULT_URI + NIL);
			Empty = new ResourceImpl(DEFAULT_URI + EMPTY);
			Literal = new ResourceImpl(DEFAULT_URI + LITERAL);
			LiteralProperty = new PropertyImpl(DEFAULT_URI + LITERAL_PROPERTY);
			HasLiteralResource = new PropertyImpl(DEFAULT_URI + HAS_LITERAL_RESOURCE);
			LiteralLang = new PropertyImpl(DEFAULT_URI + LITERAL_LANG);
			LiteralDatatype = new PropertyImpl(DEFAULT_URI+LITERAL_DATATYPE);
			LiteralString = new PropertyImpl(DEFAULT_URI + LITERAL_STRING);
			PointX = new PropertyImpl(DEFAULT_URI + POINT_X);
			PointY = new PropertyImpl(DEFAULT_URI + POINT_Y);
			NodeWidth = new PropertyImpl(DEFAULT_URI + NODE_WIDTH);
			NodeHeight = new PropertyImpl(DEFAULT_URI + NODE_HEIGHT);
			Prefix = new PropertyImpl(DEFAULT_URI + PREFIX);
			Is_prefix_available = new PropertyImpl(DEFAULT_URI + IS_PREFIX_AVAILABLE);
		} catch (RDFException e) {
			e.printStackTrace();
		}
	}

	public static String getURI() {
		return DEFAULT_URI;
	}
}
