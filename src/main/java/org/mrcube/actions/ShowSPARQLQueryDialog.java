package org.mrcube.actions;

import org.mrcube.MR3;
import org.mrcube.utils.Translator;
import org.mrcube.utils.Utilities;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

public class ShowSPARQLQueryDialog extends MR3AbstractAction {
    private static final ImageIcon ICON = Utilities.getImageIcon(Translator.getString("FindResourceDialog.Icon"));

    public ShowSPARQLQueryDialog(MR3 mr3, String title) {
        super(mr3, title, ICON);
        setValues();
    }

    private void setValues() {
        putValue(SHORT_DESCRIPTION, getName());
        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_R,
                Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
    }

    public void actionPerformed(ActionEvent e) {
        var sparqlQueryDialog = mr3.getSparqlQueryDialog();
        if (sparqlQueryDialog != null) {
            sparqlQueryDialog.setVisible(true);
        }
    }
}
