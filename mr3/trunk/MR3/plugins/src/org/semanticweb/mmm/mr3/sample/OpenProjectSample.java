/*
 * @(#) SamplePlugin3.java
 * 
 * Copyright (C) 2003 The MMM Project
 * 
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation; either version 2.1 of the License, or (at your
 * option) any later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *  
 */

package org.semanticweb.mmm.mr3.sample;

import java.io.*;

import javax.jnlp.*;
import javax.swing.*;

import org.semanticweb.mmm.mr3.plugin.*;

import com.hp.hpl.jena.rdf.model.*;

/**
 * @author takeshi morita
 * 
 * ProjectFileÇì«Ç›çûÇﬁÉvÉâÉOÉCÉì
 */
public class OpenProjectSample extends MR3Plugin {

    public void exec() {
        try {
            Model model = ModelFactory.createDefaultModel();
            InputStream is = getInputStream();
            if (is != null) {
                model.read(is, getBaseURI(), "RDF/XML");
                replaceProjectModel(model);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private InputStream getInputStream() throws IOException {
        FileOpenService fos = null;
        FileContents fileContents = null;
        try {
            fos = (FileOpenService) ServiceManager.lookup("javax.jnlp.FileOpenService");
        } catch (UnavailableServiceException exc) {
            JFileChooser jfc = new JFileChooser();
            if (jfc.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) { return new BufferedInputStream(
                    new FileInputStream(jfc.getSelectedFile())); }
            return null;
        }

        if (fos != null) {
            try {
                fileContents = fos.openFileDialog(null, null);
            } catch (Exception exc) {
                exc.printStackTrace();
            }
            if (fileContents != null) { return new BufferedInputStream(fileContents.getInputStream()); }
            return null;

        }
        return null;
    }
}
