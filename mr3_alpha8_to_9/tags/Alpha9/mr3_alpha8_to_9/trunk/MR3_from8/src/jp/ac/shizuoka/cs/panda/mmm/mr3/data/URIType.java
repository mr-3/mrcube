package jp.ac.shizuoka.cs.panda.mmm.mr3.data;

import java.io.*;

public class URIType implements Serializable {

	private String type;
	private static int nextOrdinal = 0;
	private final int ordinal = nextOrdinal++;
	
	private static final long serialVersionUID = -5726179777288542207L;

	private URIType(String t) {
		type = t;
	}

	public static final URIType URI = new URIType("URI");
	public static final URIType ANONYMOUS = new URIType("ANONYMOUS");

	private static final URIType[] PRIVATE_VALUES = { URI, ANONYMOUS };

	public static URIType getURIType(String t) {
		if (t.equals("URI")) {
			return URI;
		} else if (t.equals("ANONYMOUS")) {
			return ANONYMOUS;
		}
		return null;
	}

	private Object readResolve() throws ObjectStreamException {
		return PRIVATE_VALUES[ordinal];
	}

	public String toString() {
		return type;
	}

}
