import java.io.*;

import javax.swing.*;

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
		srcFrame = new JInternalFrame("Sample Plugin 2");
		textArea = new JTextArea();
		srcFrame.getContentPane().add(textArea);
		addInternalFrameListener(new InternalFrameAdapter() {
			public void internalFrameClosing(InternalFrameEvent e) {
				setVisible(false);
			}
		});
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		getDesktopPane().add(srcFrame);
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
