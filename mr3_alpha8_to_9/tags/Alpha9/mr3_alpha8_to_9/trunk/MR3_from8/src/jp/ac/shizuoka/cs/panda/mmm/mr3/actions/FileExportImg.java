package jp.ac.shizuoka.cs.panda.mmm.mr3.actions;

import java.awt.event.*;
import java.awt.image.*;
import java.io.*;

import javax.imageio.*;

import jp.ac.shizuoka.cs.panda.mmm.mr3.*;
import jp.ac.shizuoka.cs.panda.mmm.mr3.data.*;
import jp.ac.shizuoka.cs.panda.mmm.mr3.jgraph.*;
import jp.ac.shizuoka.cs.panda.mmm.mr3.util.*;

/**
 *
 * @author takeshi morita
 *
 */
public class FileExportImg extends AbstractActionFile {

	private String fileType;
	private GraphType graphType;

	public FileExportImg(MR3 mr3, GraphType gt, String ft, String title) {
		super(mr3, title);
		graphType = gt;
		fileType = ft;
	}

	public void actionPerformed(ActionEvent e) {
		GraphManager gmanager =mr3.getGraphManager(); 
		File file = getFile(false, "png");
		if (file == null) {
			return;
		}
		try {
			BufferedImage img = null;
			if (graphType == GraphType.RDF && gmanager.getRDFGraph().getModel().getRootCount() > 0) {
				img = GPConverter.toImage(gmanager.getRDFGraph());
			} else if (graphType == GraphType.CLASS && gmanager.getClassGraph().getModel().getRootCount() > 0) {
				img = GPConverter.toImage(gmanager.getClassGraph());
			} else if (graphType == GraphType.PROPERTY && gmanager.getPropertyGraph().getModel().getRootCount() > 0) {
				img = GPConverter.toImage(gmanager.getPropertyGraph());
			}
			if (img != null) {
				ImageIO.write(img, fileType.toLowerCase(), file);
			}
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}
}
