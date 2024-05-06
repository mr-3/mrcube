/*
 * Project Name: MR^3 (Meta-Model Management based on RDFs Revision Reflection)
 * Project Website: https://mr-3.github.io/
 *
 * Copyright (C) 2003-2024 Takeshi Morita. All rights reserved.
 *
 * This file is part of MR^3.
 *
 * MR^3 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * MR^3 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with MR^3.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package jp.ac.aoyama.it.ke.mrcube.actions;

import jp.ac.aoyama.it.ke.mrcube.MR3;
import jp.ac.aoyama.it.ke.mrcube.views.HistoryManager;
import jp.ac.aoyama.it.ke.mrcube.models.PrefConstants;
import jp.ac.aoyama.it.ke.mrcube.utils.Translator;
import jp.ac.aoyama.it.ke.mrcube.utils.Utilities;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.prefs.Preferences;

/**
 * @author Takeshi Morita
 */
public class QuitAction extends AbstractActionFile {

    private static final String TITLE = Translator.getString("Menu.File.Quit.Text");
    private static final ImageIcon ICON = Utilities.getImageIcon(Translator.getString("Menu.File.Quit.Icon"));

    public QuitAction(MR3 mr3) {
        super(mr3, TITLE, ICON);
        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_Q, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
    }

    public void quitMR3() {
        mr3.getGraphManager().closeAllDialogs();
        int messageType = JOptionPane.showConfirmDialog(mr3, Translator.getString("SaveChanges"), "MR^3 - "
                        + Translator.getString("Menu.File.Quit.Text"), JOptionPane.YES_NO_CANCEL_OPTION,
                JOptionPane.INFORMATION_MESSAGE);
        if (messageType == JOptionPane.YES_OPTION) {
            confirmExitProject();
            saveWindows();
            HistoryManager.closeLogFile();
            System.exit(0);
        } else if (messageType == JOptionPane.CANCEL_OPTION) {
        } else if (messageType == JOptionPane.NO_OPTION) {
            saveWindows();
            HistoryManager.closeLogFile();
            System.exit(0);
        }
    }

    private void saveWindowBounds(Preferences userPrefs) {
        Rectangle windowRect = mr3.getBounds();
        userPrefs.putInt(PrefConstants.WindowHeight, (int) windowRect.getHeight());
        userPrefs.putInt(PrefConstants.WindowWidth, (int) windowRect.getWidth());
        userPrefs.putInt(PrefConstants.WindowPositionX, (int) windowRect.getX());
        userPrefs.putInt(PrefConstants.WindowPositionY, (int) windowRect.getY());
    }

    private void saveWindows() {
        Preferences userPrefs = mr3.getUserPrefs();
        saveWindowBounds(userPrefs);
    }

    public void actionPerformed(ActionEvent e) {
        quitMR3();
    }
}
