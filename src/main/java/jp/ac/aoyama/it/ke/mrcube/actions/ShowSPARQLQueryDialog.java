package jp.ac.aoyama.it.ke.mrcube.actions;

import jp.ac.aoyama.it.ke.mrcube.MR3;
import jp.ac.aoyama.it.ke.mrcube.utils.Translator;
import jp.ac.aoyama.it.ke.mrcube.utils.Utilities;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

public class ShowSPARQLQueryDialog extends MR3AbstractAction {
    private static final ImageIcon ICON = Utilities.getSVGIcon(Translator.getString("FindResourceDialog.Icon"));

    public ShowSPARQLQueryDialog(MR3 mr3, String title) {
        super(mr3, title, ICON);
        setValues();
    }

    private void setValues() {
        putValue(SHORT_DESCRIPTION, getName());
        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_F,
                Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx() | InputEvent.SHIFT_DOWN_MASK));
    }

    public void actionPerformed(ActionEvent e) {
        var sparqlQueryDialog = mr3.getSparqlQueryDialog();
        if (sparqlQueryDialog != null) {
            sparqlQueryDialog.setVisible(true);
        }
    }
}
