/*
 * Project Name: MR^3 (Meta-Model Management based on RDFs Revision Reflection)
 * Project Website: http://mrcube.org/
 *
 * Copyright (C) 2003-2022 Takeshi Morita. All rights reserved.
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

package org.mrcube.utils;

import org.mrcube.actions.SelectEditorAction;
import org.mrcube.editors.RDFEditor;
import org.mrcube.models.PrefConstants;
import org.mrcube.views.OptionDialog;

import javax.swing.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.prefs.Preferences;

/*
 *
 * @author Takeshi Morita
 *
 */
public class Translator {

    private static ResourceBundle resourceBundle;

    public static String getString(String sKey) {
        try {
            return resourceBundle.getString(sKey);
        } catch (Exception e) {
            e.printStackTrace();
            return "FAILED";
        }
    }

    /*
     * システムに内蔵しておく言語ファイルのセット(Locale型)
     */
    private static final Set<Locale> systemLocaleSet;

    static {
        systemLocaleSet = new HashSet<>();
        systemLocaleSet.add(Locale.JAPAN);
        systemLocaleSet.add(Locale.ENGLISH);
        systemLocaleSet.add(Locale.CHINA);
    }

    /*
     * デフォルトのロカールの言語ファイルがシステムに内蔵されている場合は， その言語を返し，内蔵されていない場合には，英語の言語を返す.
     */
    private static String getSystemLanguage() {
        if (systemLocaleSet.contains(Locale.getDefault())) {
            return Locale.getDefault().getLanguage();
        }
        return Locale.ENGLISH.getLanguage();
    }

    private static boolean isSystemLanguage(String lang) {
        for (Locale locale : systemLocaleSet) {
            if (locale.getLanguage().equals(lang)) {
                return true;
            }
        }
        return false;
    }

    public static void loadResourceBundle(Preferences userPrefs) {
        String lang = userPrefs.get(PrefConstants.UILang, Locale.getDefault().getLanguage());
        userPrefs.put(PrefConstants.UILang, lang);
        try {
            String resDirStr = userPrefs.get(PrefConstants.ResourceDirectory,
                    System.getProperty("user.dir") + "\\resources");
            File resDir = new File(resDirStr);
            InputStream ins = null;
            if (resDir != null) {
                File resFile = new File(resDir.getAbsolutePath() + "/MR3_" + lang + ".properties");
                if (resFile.exists()) {
                    ins = new FileInputStream(resFile);
                }
            }
            if (ins == null) {
                if (isSystemLanguage(lang)) {
                    ins = Utilities.class.getClassLoader().getResourceAsStream("MR3_" + lang + ".properties");
                } else {
                    ins = Utilities.class.getClassLoader().getResourceAsStream("MR3_" + getSystemLanguage() + ".properties");
                }
            }
            resourceBundle = new PropertyResourceBundle(ins);
            loadResourceBundle();
            ins.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    private static void loadResourceBundle() {
        OptionDialog.loadResourceBundle();
        SelectEditorAction.loadResourceBundle();

        RDFEditor.updateComponents();
        if (OptionDialog.topLevelComponent != null) {
            SwingUtilities.updateComponentTreeUI(OptionDialog.topLevelComponent);
        }
        // if (GraphManager.getDesktop() != null) {
        // SwingUtilities.updateComponentTreeUI(GraphManager.getDesktop());
        // }
    }

    public static void main(String[] args) {
        InputStream ins = Utilities.class.getClassLoader().getResourceAsStream("MR3_ja.properties");
        try {
            ResourceBundle resourceBundle = new PropertyResourceBundle(ins);
            System.out.println(resourceBundle.getString("Title"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}