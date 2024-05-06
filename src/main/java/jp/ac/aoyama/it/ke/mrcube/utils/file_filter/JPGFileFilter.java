package jp.ac.aoyama.it.ke.mrcube.utils.file_filter;

import java.io.File;

public class JPGFileFilter extends MR3FileFilter {
    public String getExtension() {
        return "jpg";
    }

    public boolean accept(File f) {
        if (f.isDirectory()) {
            return true;
        }
        String extension = getExtension(f);
        return extension != null && extension.equals("jpg");
    }

    public String getDescription() {
        return "JPEG (*.jpg)";
    }
}
