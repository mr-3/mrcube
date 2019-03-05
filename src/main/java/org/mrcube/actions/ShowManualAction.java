package org.mrcube.actions;

import org.mrcube.MR3;
import org.mrcube.models.PrefConstants;
import org.mrcube.utils.Translator;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class ShowManualAction extends MR3AbstractAction {
    private static final String TITLE = Translator.getString("Menu.Help.Manual.Text");

    public ShowManualAction(MR3 mr3) {
        super(mr3, TITLE);
        setValues();
    }

    private void setValues() {
        putValue(SHORT_DESCRIPTION, TITLE);
    }

    public void actionPerformed(ActionEvent e) {
        if (Desktop.isDesktopSupported()) {
            try {
                String uiLang = getUserPrefs().get(PrefConstants.UILang, "en");
                switch (uiLang) {
                    case "ja":
                        Desktop.getDesktop().browse(new URI("http://docs.mrcube.org/ja/latest/"));
                        break;
                    default:
                        Desktop.getDesktop().browse(new URI("http://docs.mrcube.org/en/latest/"));

                }
            } catch (IOException e1) {
                e1.printStackTrace();
            } catch (URISyntaxException e1) {
                e1.printStackTrace();
            }
        }
    }
}
