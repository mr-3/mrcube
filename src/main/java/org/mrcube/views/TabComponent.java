/*
 * @(#)  2008/03/12
 */

package org.mrcube.views;

import org.mrcube.MR3;
import org.mrcube.actions.ExitProjectAction;
import org.mrcube.utils.Translator;
import org.mrcube.utils.Utilities;

import javax.swing.*;
import java.awt.*;

/**
 * @author Takeshi Morita
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
