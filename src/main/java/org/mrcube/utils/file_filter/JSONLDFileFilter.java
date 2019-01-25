package org.mrcube.utils.file_filter;

import java.io.File;

public class JSONLDFileFilter extends MR3FileFilter implements java.io.FileFilter {

    private final boolean isShowDirectories;

    public JSONLDFileFilter(boolean isShowDirectories) {
        this.isShowDirectories = isShowDirectories;
    }

    public String getExtension() {
        return "jsonld";
    }

    public boolean accept(File f) {
        if (f.isDirectory()) {
            return isShowDirectories;
        }
        String extension = getExtension(f);
        if (extension != null) {
            if (extension.equals("jsonld")) {
                return true;
            }
        }
        return false;
    }

    public String getDescription() {
        return "JSONLD (*.jsonld)";
    }
}
