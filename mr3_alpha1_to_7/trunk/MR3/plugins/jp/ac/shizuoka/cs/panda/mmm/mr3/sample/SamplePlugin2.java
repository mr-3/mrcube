package jp.ac.shizuoka.cs.panda.mmm.mr3.sample;
import java.awt.*;
import java.io.*;

import javax.swing.*;
import javax.swing.event.*;

import jp.ac.shizuoka.cs.panda.mmm.mr3.plugin.*;

import com.hp.hpl.mesa.rdf.jena.model.*;
/**
 *
 * @author takeshi morita
 *  print RDF Model
 */
public class SamplePlugin2 extends MR3Plugin {

	private JTextArea textArea;
	private JInternalFrame srcFrame;

	public SamplePlugin2() {
		textArea = new JTextArea();
		initSRCFrame();
		srcFrame.getContentPane().add(textArea);				
	}

	private void initSRCFrame() {
		srcFrame = new JInternalFrame("Sample Plugin 2", true, true);
		srcFrame.addInternalFrameListener(new InternalFrameAdapter() {
			public void internalFrameClosing(InternalFrameEvent e) {
				srcFrame.setVisible(false);
			}
		});
		srcFrame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		srcFrame.setBounds(new Rectangle(100, 100, 450, 300));
	}

	public void exec() {
		getDesktopPane().add(srcFrame);
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
