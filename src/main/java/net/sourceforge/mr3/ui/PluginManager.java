/*
 * Project Name: MR^3 (Meta-Model Management based on RDFs Revision Reflection)
 * Project Website: http://mr3.sourceforge.net/
 * 
 * Copyright (C) 2003-2008 Yamaguchi Laboratory, Keio University. All rights reserved. 
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

package net.sourceforge.mr3.ui;

import java.awt.*;
import java.awt.event.*;
import java.lang.reflect.*;
import java.util.*;
import java.util.List;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;

import net.sourceforge.mr3.*;
import net.sourceforge.mr3.data.*;
import net.sourceforge.mr3.util.*;

/**
 * @author takeshi morita
 */
public class PluginManager extends JDialog implements ActionListener {

    private MR3 mr3;
    private static Map pluginMenuMap;
    private static JTable pluginTable = new JTable();
    private static JTextArea pluginDescriptionArea = new JTextArea(6, 3);
    private JButton reloadButton;
    private JButton execButton;
    private JButton cancelButton;

    private static final int FRAME_WIDTH = 500;
    private static final int FRAME_HEIGHT = 300;

    public PluginManager(MR3 mr3) {
        super(mr3.getGraphManager().getRootFrame(), Translator.getString("Component.Tools.Plugins.PluginManager.Text"),
                false);
        setIconImage(Utilities.getImageIcon(Translator.getString("Component.Tools.Plugins.Icon")).getImage());
        this.mr3 = mr3;
        pluginDescriptionArea.setEditable(false);
        pluginDescriptionArea.setLineWrap(true);
        JScrollPane pluginDescriptionAreaScroll = new JScrollPane(pluginDescriptionArea,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        pluginDescriptionAreaScroll.setBorder(BorderFactory.createTitledBorder(Translator
                .getString("Component.Tools.Plugins.Description")));
        pluginTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        pluginTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                int selectedRow = pluginTable.getSelectedRow();
                if (selectedRow == -1) { return; }
                String pluginName = (String) pluginTable.getValueAt(selectedRow, 0);
                List pluginInfo = (List) pluginMenuMap.get(pluginName);
                String description = (String) pluginInfo.get(3);
                if (description != null) {
                    pluginDescriptionArea.setText(description);
                }
            }
        });

        execButton = new JButton(MR3Constants.EXEC + "(E)");
        execButton.setMnemonic('e');
        execButton.addActionListener(this);
        reloadButton = new JButton(MR3Constants.RELOAD + "(L)");
        reloadButton.setMnemonic('l');
        reloadButton.addActionListener(this);
        cancelButton = new JButton(MR3Constants.CANCEL);
        cancelButton.setMnemonic('c');
        cancelButton.addActionListener(this);
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(1, 3, 5, 5));
        buttonPanel.add(execButton);
        buttonPanel.add(reloadButton);
        buttonPanel.add(cancelButton);

        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.add(new JScrollPane(pluginTable));
        centerPanel.add(pluginDescriptionAreaScroll);
        getContentPane().add(centerPanel, BorderLayout.CENTER);
        getContentPane().add(Utilities.createEastPanel(buttonPanel), BorderLayout.SOUTH);

        Dimension size = new Dimension(FRAME_WIDTH, FRAME_HEIGHT);
        setSize(size);
        setLocationRelativeTo(mr3.getGraphManager().getRootFrame());
        setVisible(false);
    }

    private static final String PLUGIN_METHOD_NAME = "exec";

    public static void reloadPlugins() {
        pluginMenuMap = PluginLoader.getPluginMenuMap();
        Object[] columnNames = new Object[] { Translator.getString("Component.Tools.Plugins.PluginName"),
                Translator.getString("Component.Tools.Plugins.PluginCreatorName"),
                Translator.getString("Component.Tools.Plugins.Date")};
        DefaultTableModel pluginTableModel = new DefaultTableModel(columnNames, 0);
        // System.out.println(pluginMenuMap);
        for (Iterator i = pluginMenuMap.keySet().iterator(); i.hasNext();) {
            String pluginName = (String) i.next();
            List pluginInfo = (List) pluginMenuMap.get(pluginName);
            String creatorName = (String) pluginInfo.get(1);
            String date = (String) pluginInfo.get(2);
            pluginTableModel.insertRow(pluginTableModel.getRowCount(), new Object[] { pluginName, creatorName, date});
        }
        pluginTable.setModel(pluginTableModel);
        pluginDescriptionArea.setText("");
    }

    public void setVisible(boolean arg0) {
        if (arg0 && (pluginMenuMap == null || pluginMenuMap.keySet().isEmpty())) {
            reloadPlugins();
        }
        super.setVisible(arg0);
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == reloadButton) {
            reloadPlugins();
        } else if (e.getSource() == execButton) {
            if (pluginTable.getSelectedRow() == -1) { return; }
            String pluginName = (String) pluginTable.getValueAt(pluginTable.getSelectedRow(), 0);
            try {
                List pluginInfo = (List) pluginMenuMap.get(pluginName);
                Class classObj = (Class) pluginInfo.get(0);
                // System.out.println("class: "+classObj.hashCode());
                Object instance = classObj.newInstance();
                Method initMethod = classObj.getMethod("setMR3", new Class[] { MR3.class});
                initMethod.invoke(instance, new Object[] { mr3});
                Method m = classObj.getMethod(PLUGIN_METHOD_NAME);
                m.invoke(instance);
            } catch (NoClassDefFoundError ncdfe) {
                ncdfe.printStackTrace();
            } catch (NoSuchMethodException nsme) {
                nsme.printStackTrace();
            } catch (InstantiationException ine) {
                ine.printStackTrace();
            } catch (IllegalAccessException ille) {
                ille.printStackTrace();
            } catch (InvocationTargetException inve) {
                inve.printStackTrace();
            }
        } else if (e.getSource() == cancelButton) {
            setVisible(false);
        }
    }
}
