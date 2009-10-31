package mr3.data;

import java.net.*;

import com.hp.hpl.mesa.rdf.jena.common.*;
import com.hp.hpl.mesa.rdf.jena.model.*;

/**
 * @author takeshi morita
 * 
 * MRCUBE�Ŏg��Property��Resource
 */
public class MR3Resource {

	private static final String uri = "http://mr3#";
	private static final String nProperty = "Property";
	public static Property Property;
	private static final String nNil = "nil";
	public static Property Nil;
	public static final String IMAGE_PATH="mr3/resources/";
	
	// JavaWebStart�ł́C�g���Ȃ��̂ŁC
	// this.getClass().getClassLoader().getResource()���g�����Ƃɂ��Ă���D
	public static URL getImageIcon(String img) {
		return ClassLoader.getSystemClassLoader().getResource(MR3Resource.IMAGE_PATH+img);
	}

	static {
		try {
			Property = new PropertyImpl(uri + nProperty);
			Nil = new PropertyImpl(uri + nNil);
		} catch (RDFException e) {
			e.printStackTrace();
		}
	}

	public static String getURI() {
		return uri;
	}
}