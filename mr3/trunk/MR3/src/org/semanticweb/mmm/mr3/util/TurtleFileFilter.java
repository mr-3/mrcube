/*
 * @(#)  2005/12/03
 */

package org.semanticweb.mmm.mr3.util;

import java.io.*;

/**
 * @author takeshi morita
 */
public class TurtleFileFilter extends MR3FileFilter implements java.io.FileFilter {

    public String getExtension() {
        return "ttl";
    }

    public boolean accept(File f) {
        if (f.isDirectory()) { return true; }
        String extension = getExtension(f);
        if (extension != null && extension.equals("ttl")) { return true; }
        return false;
    }

    public String getDescription() {
        return "Turtle (*.ttl)";
    }
}
