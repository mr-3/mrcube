/*
 * Created on 2003/07/19
 *
 */
package mr3.actions;

import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.util.zip.*;

import mr3.*;

/**
 * @author takeshi morita
 *
 */
public class ExportJavaObject extends AbstractActionFile {
	
	private static final String PROJECT = "Project (Java Object)";

	public ExportJavaObject(MR3 mr3) {
		super(mr3, PROJECT);
	}

	private ObjectOutputStream createOutputStream(File file) throws FileNotFoundException, IOException {
		OutputStream fo = new FileOutputStream(file);
		fo = new GZIPOutputStream(fo);
		return new ObjectOutputStream(fo);
	}

	public void actionPerformed(ActionEvent e) {
		File file = getFile(false, "mr3");
		if (file == null) {
			return;
		}
		try {
			ObjectOutputStream oo = createOutputStream(file);
			ArrayList list = mr3.getGraphManager().storeState();
			list.add(mr3.getNSTableDialog().getState());
			oo.writeObject(list);
			oo.flush();
			oo.close();
			mr3.setTitle("MR^3 - " + file.getAbsolutePath());
			mr3.setCurrentProject(file);
		} catch (FileNotFoundException fne) {
			fne.printStackTrace();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}
}
