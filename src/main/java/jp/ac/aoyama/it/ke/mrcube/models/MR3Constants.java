/*
 * Project Name: MR^3 (Meta-Model Management based on RDFs Revision Reflection)
 * Project Website: https://mr-3.github.io/
 * 
 * Copyright (C) 2003-2025 Takeshi Morita. All rights reserved.
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

package jp.ac.aoyama.it.ke.mrcube.models;

import jp.ac.aoyama.it.ke.mrcube.utils.Translator;
import jp.ac.aoyama.it.ke.mrcube.utils.Utilities;

import javax.swing.*;
import java.awt.*;

/**
 * @author Takeshi Morita
 */
public class MR3Constants {

    public static String OK;
    public static String APPLY;
    public static String CANCEL;
    public static String PREFIX;
    public static String NAMESPACE;
    public static String RELOAD;
    public static String RESET;
    public static String EXEC;
    public static String EDIT;
    public static String ADD;
    public static String REMOVE;
    public static String CLEAR;
    public static String LANGUAGE_TAG;
    public static String LABEL;
    public static String COMMENT;
    public static Color TITLE_BACKGROUND_COLOR = new Color(43, 43, 43);
    public static final int TITLE_FONT_SIZE = 14;
    public static final ImageIcon SPLASH_LOGO = Utilities.getSVGIcon(Translator.getString("SplashLogo"));

    public static void loadResourceBundle() {
        APPLY = Translator.getString("Apply") + "(A)";
        OK = Translator.getString("OK") + "(O)";
        CANCEL = Translator.getString("Cancel") + "(C)";
        PREFIX = Translator.getString("Prefix");
        NAMESPACE = Translator.getString("Namespace");
        RELOAD = Translator.getString("Reload");
        RESET = Translator.getString("Reset") + "(S)";
        EXEC = Translator.getString("Exec");
        LANGUAGE_TAG = Translator.getString("LanguageTag");
        LABEL = Translator.getString("Label");
        COMMENT = Translator.getString("Comment");
        EDIT = Translator.getString("Edit");
        ADD = Translator.getString("Add");
        CLEAR = Translator.getString("Clear");
        REMOVE = Translator.getString("Remove");
    }

    public static boolean IS_SHOW_PROPERTY_LABEL = true;

    public enum GraphType {
        Instance, Class, Property
    }

    public enum ArrangeWindowsType {
        CPI, CI, PI
    }

    public enum CellViewType {
        URI, ID, LABEL
    }

    public enum CreateRDFSType {
        RENAME, CREATE
    }

    public enum URIType {
        URI, ANONYMOUS
    }

    public enum HistoryType {
        INSERT_RESOURCE,
        INSERT_CONNECTED_RESOURCE,
        INSERT_PROPERTY,
        INSERT_LITERAL,
        INSERT_CONNECTED_LITERAL,
        INSERT_CLASS,
        INSERT_ONT_PROPERTY,
        INSERT_CONNECTED_ONT_PROPERTY,
        CONNECT_SUP_SUB_CLASS,
        CONNECT_SUP_SUB_PROPERTY,
        ADD_RESOURCE_LABEL,
        ADD_RESOURCE_COMMENT,
        ADD_CLASS_LABEL,
        ADD_CLASS_COMMENT,
        ADD_ONT_PROPERTY_LABEL,
        ADD_ONT_PROPERTY_COMMENT,
        ADD_ONT_PROPERTY_DOMAIN,
        ADD_ONT_PROPERTY_RANGE,
        EDIT_RESOURCE_WITH_DIALOG,
        EDIT_RESOURCE_WITH_GRAPH,
        EDIT_RESOURCE_LABEL,
        EDIT_RESOURCE_LABEL_WITH_GRAPH,
        EDIT_RESOURCE_COMMENT,
        EDIT_CLASS_WITH_DIAGLOG,
        EDIT_CLASS_WITH_GRAPH,
        EDIT_CLASS_LABEL,
        EDIT_CLASS_LABEL_WITH_GRAPH,
        EDIT_CLASS_COMMENT,
        EDIT_PROPERTY_WITH_DIAGLOG,
        EDIT_PROPERTY_WITH_GRAPH,
        EDIT_ONT_PROPERTY_WITH_DIAGLOG,
        EDIT_ONT_PROPERTY_WITH_GRAPH,
        EDIT_ONT_PROPERTY_LABEL,
        EDIT_ONT_PROPERTY_LABEL_WITH_GRAPH,
        EDIT_ONT_PROPERTY_COMMENT,
        EDIT_LITERAL_WITH_DIAGLOG,
        EDIT_LITERAL_WITH_GRAPH,
        DELETE_RESOURCE_LABEL,
        DELETE_RESOURCE_COMMENT,
        DELETE_CLASS_LABEL,
        DELETE_CLASS_COMMENT,
        DELETE_ONT_PROPERTY_LABEL,
        DELETE_ONT_PROPERTY_COMMENT,
        DELETE_ONT_PROPERTY_DOMAIN,
        DELETE_ONT_PROPERTY_RANGE,
        DELETE_RDF,
        DELETE_CLASS,
        DELETE_ONT_PROPERTY,
        NEW_PROJECT,
        SAVE_PROJECT,
        SAVE_PROJECT_AS,
        OPEN_PROJECT,
        LOAD_HISTORY,
        SAVE_HISTORY,
        META_MODEL_MANAGEMNET_REPLACE_CLASS,
        META_MODEL_MANAGEMNET_REPLACE_ONT_PROPERTY,
        META_MODEL_MANAGEMNET_REPLACE_RESOURCE_TYPE_WITH_CREATE_CLASS,
        META_MODEL_MANAGEMNET_REPLACE_RESOURCE_TYPE_WITH_REPLACE_CLASS,
        META_MODEL_MANAGEMNET_REPLACE_PROPERTY_WITH_CREATE_ONT_PROPERTY,
        META_MODEL_MANAGEMNET_REPLACE_PROPERTY_WITH_REPLACE_ONT_PROPERTY,
        COPY_RDF_GRAPH,
        COPY_CLASS_GRAPH,
        COPY_PROPERTY_GRAPH,
        CUT_RDF_GRAPH,
        CUT_CLASS_GRAPH,
        CUT_PROPERTY_GRAPH,
        PASTE_RDF_GRAPH,
        PASTE_CLASS_GRAPH,
        PASTE_PROPERTY_GRAPH,
    }

}
