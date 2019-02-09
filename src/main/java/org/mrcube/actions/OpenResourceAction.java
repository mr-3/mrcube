package org.mrcube.actions;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.mrcube.MR3;
import org.mrcube.io.MR3Reader;
import org.mrcube.utils.Translator;
import org.mrcube.utils.Utilities;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class OpenResourceAction extends AbstractActionFile {

    private MR3Reader mr3Reader;
    public static final String TITLE = Translator.getString("Menu.File.Open.Text");
    public static final ImageIcon ICON = Utilities.getImageIcon("baseline_open_in_browser_black_18dp.png");

    public OpenResourceAction(MR3 mr3) {
        super(mr3, TITLE, ICON);
        setValues();
        mr3Reader = new MR3Reader(mr3.getGraphManager());
        initializeJFileChooser();
    }

    protected void setValues() {
        putValue(SHORT_DESCRIPTION, TITLE);
    }

    public void openResource(String resourcePath) {
        try {
            if (resourcePath.endsWith("mr3")) {
                Model model = RDFDataMgr.loadModel(resourcePath, Lang.TURTLE);
                mr3.getMR3Reader().replaceProjectModel(model);
                mr3.setTitle("MR^3: " + MR3.getCurrentProject().getTitle());
            } else {
                Model model = RDFDataMgr.loadModel(resourcePath);
                mr3Reader.mergeRDFandRDFSModel(model);
            }
        } catch (Exception e) {
            Utilities.showErrorMessageDialog(Translator.getString("Warning.Message11"));
        }
    }

    public void actionPerformed(ActionEvent e) {
        var message = confirmExitProject();
        if (message != JOptionPane.CANCEL_OPTION) {
            mr3.newProject();
            openResource(MR3.ResourcePathTextField.getText());
        }
    }
}
