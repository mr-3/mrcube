/*
 * @(#) URIType.java
 *
 *
 * Copyright (C) 2003 The MMM Project
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 */

package org.semanticweb.mmm.mr3.data;

import java.io.*;

/*
 * 
 * @author takeshi morita
 *
 */
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

	private Object readResolve()  {
		return PRIVATE_VALUES[ordinal];
	}

	public String toString() {
		return type;
	}

}
