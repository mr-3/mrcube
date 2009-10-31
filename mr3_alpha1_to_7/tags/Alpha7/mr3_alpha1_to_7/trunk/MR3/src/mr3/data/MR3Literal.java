/*
 * Created on 2003/06/08
 *
 */
package mr3.data;

import java.awt.*;
import java.io.*;

import com.hp.hpl.mesa.rdf.jena.common.*;
import com.hp.hpl.mesa.rdf.jena.model.*;

/**
 * @author takeshi morita
 * 
 */
public class MR3Literal implements Serializable {

	private String str;
	private String lang;
	private Rectangle litRect;
	private Resource resource;
	private Property property;
	private static final long serialVersionUID = 75073546338792276L;

	public MR3Literal() {
		str = "";
		lang = "";
		litRect = new Rectangle();
		resource = null;
		property = null;
	}

	public MR3Literal(String s, String l) {
		str = s;
		lang = l;
	}

	public MR3Literal(Literal lit) {
		try {
			str = lit.getString();
			lang = lit.getLanguage();
		} catch (RDFException e) {
			e.printStackTrace();
		}
	}

	public Literal getLiteral() {
		return new LiteralImpl(str, lang);
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
		resource = new ResourceImpl(res);
	}

	public Resource getResource() {
		return resource;
	}

	public void setProperty(String prop) {
		try {
			property = new PropertyImpl(prop);
		} catch (RDFException e) {
			e.printStackTrace();
		}
	}

	public Property getProperty() {
		return property;
	}
}
