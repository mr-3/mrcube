import java.io.*;

import javax.swing.*;
import javax.swing.event.*;

import mr3.plugin.*;

import com.hp.hpl.mesa.rdf.jena.model.*;
/**
 *
 * @auther takeshi morita
 *  print RDF Model
 */
public class SamplePlugin2 extends MR3Plugin {

	private JTextArea textArea;
	private JInternalFrame srcFrame;

	public SamplePlugin2() {
		super("Sample Plugin 2");
		textArea = new JTextArea();
		initSRCFrame();
		srcFrame.getContentPane().add(textArea);				
		getDesktopPane().add(srcFrame);
	}

	private void initSRCFrame() {
		srcFrame = new JInternalFrame("Sample Plugin 2");
		srcFrame.addInternalFrameListener(new InternalFrameAdapter() {
			public void internalFrameClosing(InternalFrameEvent e) {
				srcFrame.setVisible(false);
			}
		});
		srcFrame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
	}

	public void exec() {
		srcFrame.setVisible(true);
		try {
			Model rdfModel = getRDFModel();
			Writer out = new StringWriter();
			rdfModel.write(new PrintWriter(out));
			textArea.setText(out.toString());
		} catch (RDFException e) {
			e.printStackTrace();
		}
	}
}
