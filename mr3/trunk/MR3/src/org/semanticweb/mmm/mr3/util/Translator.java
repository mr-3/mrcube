/*
 * @(#) Translator.java
 * 
 * Copyright (C) 2003-2005 The MMM Project
 * 
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation; either version 2.1 of the License, or (at your
 * option) any later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *  
 */

package org.semanticweb.mmm.mr3.util;

import java.io.*;
import java.util.*;
import java.util.prefs.*;

import javax.swing.*;

import org.semanticweb.mmm.mr3.*;
import org.semanticweb.mmm.mr3.actions.*;
import org.semanticweb.mmm.mr3.data.*;
import org.semanticweb.mmm.mr3.editor.*;
import org.semanticweb.mmm.mr3.jgraph.*;
import org.semanticweb.mmm.mr3.ui.*;

import com.hp.hpl.jena.shared.*;

/*
 * 
 * @author takeshi morita
 * 
 */
public class Translator {

    protected static ResourceBundle resourceBundle;
    private static final String RESOURCE_DIR = "org/semanticweb/mmm/mr3/resources/";

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
    private static Set<Locale> systemLocaleSet;
    static {
        systemLocaleSet = new HashSet<Locale>();
        systemLocaleSet.add(Locale.JAPAN);
        systemLocaleSet.add(Locale.ENGLISH);
        systemLocaleSet.add(Locale.CHINA);
    }

    /*
     * デフォルトのロカールの言語ファイルがシステムに内蔵されている場合は， その言語を返し，内蔵されていない場合には，英語の言語を返す.
     */
    public static String getSystemLanguage() {
        if (systemLocaleSet.contains(Locale.getDefault())) { return Locale.getDefault().getLanguage(); }
        return Locale.ENGLISH.getLanguage();
    }

    private static boolean isSystemLanguage(String lang) {
        for (Locale locale : systemLocaleSet) {
            if (locale.getLanguage().equals(lang)) { return true; }
        }
        return false;
    }

    public static void loadResourceBundle(Preferences userPrefs) {
        String lang = userPrefs.get(PrefConstants.UILang, Locale.getDefault().getLanguage());
        userPrefs.put(PrefConstants.UILang, lang);
        try {
            String resDirStr = userPrefs.get(PrefConstants.ResourceDirectory, System.getProperty("user.dir")
                    + "\\resources");
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
                    ins = Utilities.class.getClassLoader().getResourceAsStream(
                            RESOURCE_DIR + "MR3_" + lang + ".properties");
                } else {
                    ins = Utilities.class.getClassLoader().getResourceAsStream(
                            RESOURCE_DIR + "MR3_" + getSystemLanguage() + ".properties");
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
        // MR3Constants.loadResourceBundle();
        EditorSelect.loadResourceBundle();

        RDFEditor.updateComponents();
        if (OptionDialog.topLevelComponent != null) {
            SwingUtilities.updateComponentTreeUI(OptionDialog.topLevelComponent);
        }
        if (GraphManager.getDesktop() != null) {
            SwingUtilities.updateComponentTreeUI(GraphManager.getDesktop());
        }
    }

    public static void main(String[] args) {
        InputStream ins = Utilities.class.getClassLoader().getResourceAsStream(
                RESOURCE_DIR + "MR3_" + "ja" + ".properties");
        Properties property = new Properties();
        try {
            property.load(ins);
            OutputStream os = new FileOutputStream("C:/usr/eclipse_workspace/MR3/src/" + RESOURCE_DIR
                    + "MR3Test_ja.properties");
            property.storeToXML(os, "Japanese Properties", "SJIS");
            os.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }
}