/*
 * Created on 2003/06/08
 *
 */
package mr3.data;

import java.io.*;

import com.hp.hpl.mesa.rdf.jena.common.*;
import com.hp.hpl.mesa.rdf.jena.model.*;

/**
 * @author takeshi morita
 * 
 * LiteralImplに相当するシリアライズ可能なクラスが見つかるまでは，
 * 一時的にこのクラスを使って，シリアライズすることにする．
 */
public class MR3Literal implements Serializable {
	
	private String str;
	private String lang;
	private static final long serialVersionUID = 75073546338792276L; 

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

	public String getString() {
		return str;
	}

	public String getLanguage() {
		return lang;
	}
}
