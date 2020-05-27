/*
 * Project Name: MR^3 (Meta-Model Management based on RDFs Revision Reflection)
 * Project Website: http://mrcube.org/
 *
 * Copyright (C) 2003-2020 Takeshi Morita. All rights reserved.
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

import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.rdf.model.*;
import org.mrcube.MR3;

import javax.swing.*;
import java.awt.Container;
import java.awt.*;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author Takeshi Morita
 */
public class Utilities {

    private static final String WARNING = Translator.getString("Warning");

    public static void showErrorMessageDialog(String message) {
        JOptionPane.showMessageDialog(MR3.getProjectPanel(),
                message, WARNING, JOptionPane.ERROR_MESSAGE);
    }

    /*
     * ResourceクラスのgetNameSpaceメソッドは，ローカル名が数字からはじまる場合に名前空間の分割がうまくできないため，
     * 独自に実装している．（不完全）
     */
    public static String getNameSpace(Resource res) {
        String ns = res.getNameSpace();
        if (ns == null) {
            return "";
        }
        if (ns.matches(".*#$") || ns.matches(".*/$")) {
            return ns;
        }
        String ns2 = ns.split("#\\d*[^#/]*$")[0];
        if (ns2 != null && !ns2.equals(ns)) {
            return ns2 + "#";
        }
        ns2 = ns.split("/\\d*[^#/]*$")[0];
        if (ns2 != null && !ns2.equals(ns)) {
            return ns2 + "/";
        }
        return "";
    }

    /*
     * getNameSpaceと同様の理由でgetLocalNameメソッドを独自に実装（不完全）
     */
    public static String getLocalName(Resource res) {
        String ns = res.getNameSpace();
        String localName = res.getLocalName();
        if (localName == null) {
            return "";
        }
        if (localName.length() == 0 || ns.matches(".*[^#/]$")) {
            String uri = res.getURI();
            if (uri.indexOf('#') != -1 && !uri.matches(".*#$")) {
                String[] names = uri.split("#");
                return names[names.length - 1];
            } else if (uri.indexOf('#') == -1 && !uri.matches(".*/$")) {
                String[] names = uri.split("/");
                return names[names.length - 1];
            }
        } else {
            return localName;
        }
        return "";
    }

    public static void center(Window frame) {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension frameSize = frame.getSize();
        frame.setLocation(screenSize.width / 2 - (frameSize.width / 2),
                screenSize.height / 2 - (frameSize.height / 2));
    }

    public static ImageIcon getImageIcon(String image) {
        return new ImageIcon(Utilities.class.getClassLoader().getResource("images/" + image));
    }

    public static JComponent createTitledPanel(JComponent component, String title) {
        JPanel p = new JPanel();
        p.setLayout(new BorderLayout());
        p.setBorder(BorderFactory.createTitledBorder(title));
        p.add(component, BorderLayout.CENTER);
        return p;
    }

    public static JComponent createTitledPanel(JComponent component, String title, int width, int height) {
        component.setPreferredSize(new Dimension(width, height));
        component.setMinimumSize(new Dimension(width, height));
        return createTitledPanel(component, title);
    }

    public static void initComponent(JComponent component, String title, int width, int height) {
        component.setPreferredSize(new Dimension(width, height));
        component.setMinimumSize(new Dimension(width, height));
        component.setBorder(BorderFactory.createTitledBorder(title));
    }

    public static JComponent createWestPanel(JComponent p) {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.add(p, BorderLayout.WEST);
        return panel;
    }

    public static JComponent createEastPanel(JComponent p) {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.add(p, BorderLayout.EAST);
        return panel;
    }

    public static JComponent createNorthPanel(JComponent p) {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.add(p, BorderLayout.NORTH);
        return panel;
    }

    public static JComponent createSouthPanel(JComponent p) {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.add(p, BorderLayout.SOUTH);
        return panel;
    }

    private static int focusNum;
    private static Component[] order;

    public static FocusTraversalPolicy getMyFocusTraversalPolicy(Component[] c, int num) {
        order = c;
        focusNum = num;
        return new FocusTraversalPolicy() {
            private final List list = Arrays.asList(order);

            public Component getDefaultComponent(Container focusCycleRoot) {
                return order[focusNum];
            }

            public Component getFirstComponent(Container arg0) {
                return order[focusNum];
            }

            public Component getInitialComponent(Window window) {
                return order[focusNum];
            }

            public Component getLastComponent(Container arg0) {
                return order[order.length - 1];
            }

            public Component getComponentAfter(Container arg0, Component arg1) {
                int index = list.indexOf(arg1);
                return order[(index + 1) % order.length];
            }

            public Component getComponentBefore(Container arg0, Component arg1) {
                int index = list.indexOf(arg1);
                return order[(index - 1 + order.length) % order.length];
            }
        };
    }

    public static Object[] getSortedCellSet(Object[] cells) {
        Map<String, Object> map = new TreeMap<>();
        for (Object cell : cells) {
            if (cell == null) {
                map.put("", cell);
            } else {
                map.put(cell.toString(), cell);
            }
        }
        return map.values().toArray();
    }

    /**
     * dataTypeがある場合は，ラベルは指定できない．
     *
     * @param value
     * @param lang
     * @param dataType
     * @return Literal
     */
    public static Literal createLiteral(String value, String lang, RDFDatatype dataType) {
        if (value == null) {
            value = "";
        }
        if (dataType == null) {
            return ResourceFactory.createLangLiteral(value, lang);
        }
        return ResourceFactory.createTypedLiteral(value, dataType);
    }

}
