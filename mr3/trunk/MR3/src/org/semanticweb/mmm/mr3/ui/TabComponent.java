/*
 * @(#)  2008/03/12
 */

package org.semanticweb.mmm.mr3.ui;

import java.awt.*;

import javax.swing.*;

import org.semanticweb.mmm.mr3.*;
import org.semanticweb.mmm.mr3.actions.*;
import org.semanticweb.mmm.mr3.util.*;

/**
 * @author takeshi morita
 */
public class TabComponent extends JPanel {

    private ImageIcon icon;
    private JLabel tabNameLabel;
    private JButton tabButton;

    public TabComponent(MR3 mr3, String tabName) {
        setLayout(new BorderLayout());
        icon = Utilities.getImageIcon(Translator.getString("CloseTab.Icon"));
        ExitProjectAction exitProjectAction = new ExitProjectAction(mr3, "", icon);
        tabButton = new JButton(exitProjectAction);
        tabButton.setAlignmentX(0);
        tabButton.setAlignmentY(0);
        int width = icon.getIconWidth();
        int height = icon.getIconHeight();
        tabButton.setPreferredSize(new Dimension(width, height));
        tabNameLabel = new JLabel("Å@" + tabName + "Å@");
        tabNameLabel.setBackground(Color.gray);
        add(tabNameLabel, BorderLayout.CENTER);
        add(tabButton, BorderLayout.EAST);
    }

    public void setTabName(String name) {
        tabNameLabel.setText(name);
    }
    
}
