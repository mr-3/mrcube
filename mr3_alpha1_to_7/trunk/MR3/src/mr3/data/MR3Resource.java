package mr3.data;

import java.net.*;

import com.hp.hpl.mesa.rdf.jena.common.*;
import com.hp.hpl.mesa.rdf.jena.model.*;

/**
 * @author takeshi morita
 * 
 * MRCUBEで使うPropertyとResource
 */
public class MR3Resource {

	private static final String DEFAULT_URI = "http://panda.cs.inf.shizuoka.ac.jp/mmm/mr3#";
	private static final String PROPERTY = "Property";
	private static final String NIL = "nil";
	private static final String LITERAL = "literal";
	private static final String HAS_LITERAL_RESOURCE = "hasLiteralResource";
	private static final String LITERAL_VALUE = "literal_value";
	private static final String POINT_X = "point_x";
	private static final String POINT_Y = "point_y";
	private static final String NODE_WIDTH = "node_width";
	private static final String NODE_HEIGHT = "node_height";
	private static final String PREFIX = "prefix";
	private static final String IS_PREFIX_AVAILABLE = "is_prefix_available";

	public static Resource Default_URI;
	public static Property Property;
	public static Property Nil;
	public static Resource Literal;
	public static Property HasLiteralResource;
	public static Property LiteralValue;
	public static Property PointX;
	public static Property PointY;
	public static Property NodeWidth;
	public static Property NodeHeight;
	public static Property Prefix;
	public static Property Is_prefix_available;
	public static final String IMAGE_PATH = "mr3/resources/";

	// JavaWebStartでは，使えないので，
	// this.getClass().getClassLoader().getResource()を使うことにしている．
	public static URL getImageIcon(String img) {
		return ClassLoader.getSystemClassLoader().getResource(MR3Resource.IMAGE_PATH + img);
	}

	static {
		try {
			Default_URI = new ResourceImpl(DEFAULT_URI);
			Property = new PropertyImpl(DEFAULT_URI + PROPERTY);
			Nil = new PropertyImpl(DEFAULT_URI + NIL);
			Literal = new ResourceImpl(DEFAULT_URI + LITERAL);
			HasLiteralResource = new PropertyImpl(DEFAULT_URI + HAS_LITERAL_RESOURCE);
			LiteralValue = new PropertyImpl(DEFAULT_URI + LITERAL_VALUE);
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
