/*
 * @(#)  2008/03/12
 */

package net.sourceforge.mr3.ui;

import java.awt.*;

import javax.swing.*;

import net.sourceforge.mr3.*;
import net.sourceforge.mr3.actions.*;
import net.sourceforge.mr3.util.*;

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
        tabButton.setOpaque(true);
        tabButton.setContentAreaFilled(false);
        int width = icon.getIconWidth();
        int height = icon.getIconHeight();
        tabButton.setPreferredSize(new Dimension(width, height));
        tabNameLabel = new JLabel("　" + tabName + "　");
        add(tabNameLabel, BorderLayout.CENTER);
        add(tabButton, BorderLayout.EAST);
    }

    public void setTabName(String name) {
        tabNameLabel.setText(name);
    }
    
}
