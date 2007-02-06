package org.semanticweb.mmm.mr3.util;

import java.io.*;

import javax.swing.filechooser.FileFilter;

/**
 * @author takeshi morita
 */
public abstract class MR3FileFilter extends FileFilter {

    public abstract String getExtension();

    public String getExtension(File f) {
        String ext = null;
        String s = f.getName();
        int i = s.lastIndexOf('.');

        if (i > 0 && i < s.length() - 1) {
            ext = s.substring(i + 1).toLowerCase();
        }
        return ext;
    }

    public String toString() {
        return getDescription();
    }
}
