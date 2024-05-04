package org.mrcube.actions;

import org.mrcube.MR3;
import org.mrcube.models.PrefConstants;
import org.mrcube.utils.Translator;
import org.mrcube.utils.Utilities;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class ShowManualAction extends MR3AbstractAction {
    private static final String TITLE = Translator.getString("Menu.Help.Manual.Text");
    private static final ImageIcon ICON = Utilities.getImageIcon(Translator.getString("Menu.Help.Manual.Icon"));

    public ShowManualAction(MR3 mr3) {
        super(mr3, TITLE, ICON);
        setValues();
    }

    private void setValues() {
        putValue(SHORT_DESCRIPTION, TITLE);
    }

    public void actionPerformed(ActionEvent e) {
        if (Desktop.isDesktopSupported()) {
            try {
                String uiLang = getUserPrefs().get(PrefConstants.UILang, "en");
                if ("ja".equals(uiLang)) {
                    Desktop.getDesktop().browse(new URI("https://mrcube.readthedocs.io/ja/latest/index.html"));
                } else {
                    Desktop.getDesktop().browse(new URI("https://mrcube.readthedocs.io/en/latest/index.html"));
                }
            } catch (IOException | URISyntaxException e1) {
                e1.printStackTrace();
            }
        }
    }
}
