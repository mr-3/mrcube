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
	private static final String POINT_X = "point_x";
	private static final String POINT_Y = "point_y";
	private static final String PREFIX = "prefix";
	private static final String IS_PREFIX_AVAILABLE = "is_prefix_available";
	
	public static Resource Default_URI;
	public static Property Property;
	public static Property Nil;
	public static Property Point_x;
	public static Property Point_y;
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
			Point_x = new PropertyImpl(DEFAULT_URI+POINT_X);
			Point_y = new PropertyImpl(DEFAULT_URI+POINT_Y);
			Prefix = new PropertyImpl(DEFAULT_URI+PREFIX);
			Is_prefix_available = new PropertyImpl(DEFAULT_URI+IS_PREFIX_AVAILABLE);
		} catch (RDFException e) {
			e.printStackTrace();
		}
	}

	public static String getURI() {
		return DEFAULT_URI;
	}
}
