/*
 * Created on 2003/02/15
 *
 */
package org.semanticweb.mmm.mr3.data;

/**
 * @author takeshi morita
 */
public class CreateRDFSType {

	private String type;
    
	private CreateRDFSType(String t) {
		type = t;
	}

	public static final CreateRDFSType RENAME = new CreateRDFSType("Rename");
	public static final CreateRDFSType CREATE = new CreateRDFSType("Create");

	public String toString() {
		return type;
	}
}
