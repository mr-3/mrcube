/*
 * Project Name: MR^3 (Meta-Model Management based on RDFs Revision Reflection)
 * Project Website: http://mrcube.org/
 * 
 * Copyright (C) 2003-2019 Yamaguchi Laboratory, Keio University. All rights reserved.
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

import org.jgraph.graph.GraphCell;
import org.jgraph.graph.GraphConstants;
import org.mrcube.models.NamespaceModel;
import org.mrcube.models.RDFSModel;

import javax.swing.*;
import java.util.*;

/**
 * @author Takeshi Morita
 */
public class PrefixNSUtil {

    private static Set<NamespaceModel> namespaceModelSet;

    public static void setNamespaceModelSet(Set<NamespaceModel> set) {
        namespaceModelSet = set;
    }

    public static String getBaseURIPrefix(String baseURI) {
        for (NamespaceModel info : namespaceModelSet) {
            if (info.getNameSpace().equals(baseURI)) {
                return info.getPrefix();
            }
        }
        return null;
    }

    private static Set<String> getPropNSSet(List propList) {
        Set<String> propNSSet = new HashSet<>();
        for (Object o : propList) {
            GraphCell cell = (GraphCell) o;
            RDFSModel info = (RDFSModel) GraphConstants.getValue(cell.getAttributes());
            propNSSet.add(info.getNameSpace());
        }
        return propNSSet;
    }

    public static Set getPropPrefixes(List propList) {
        Set<String> prefixSet = new TreeSet<>();
        Set<String> propNSSet = getPropNSSet(propList);
        for (NamespaceModel info : namespaceModelSet) {
            if (propNSSet.contains(info.getNameSpace())) {
                prefixSet.add(info.getPrefix());
            }
        }
        return prefixSet;
    }

    public static Set getPrefixes() {
        Set<String> prefixes = new TreeSet<>();
        for (NamespaceModel info : namespaceModelSet) {
            prefixes.add(info.getPrefix());
        }
        return prefixes;
    }

    public static String getNameSpace(String prefix) {
        for (NamespaceModel info : namespaceModelSet) {
            if (info.getPrefix().equals(prefix)) {
                return info.getNameSpace();
            }
        }
        return "#";
    }

    public static void setNSLabel(JLabel nsLabel, String str) {
        nsLabel.setText(str);
        nsLabel.setToolTipText(str);
    }

    /* nsLabelをprefixに対応する名前空間に置き換える */
    public static void replacePrefix(String prefix, JLabel nsLabel) {
        setNSLabel(nsLabel, getNameSpace(prefix));
    }

}
