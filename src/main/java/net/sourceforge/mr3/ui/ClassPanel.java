/*
 * Project Name: MR^3 (Meta-Model Management based on RDFs Revision Reflection)
 * Project Website: http://mr3.sourceforge.net/
 * 
 * Copyright (C) 2003-2015 Yamaguchi Laboratory, Keio University. All rights reserved. 
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
import java.util.*;

import javax.swing.*;

import net.sourceforge.mr3.data.MR3Constants.*;
import net.sourceforge.mr3.jgraph.*;
import net.sourceforge.mr3.util.*;

import org.jgraph.graph.*;

/*
 * 
 * @author Takeshi Morita
 * 
 */
public class ClassPanel extends OntologyPanel {

    private JList supClasses;

    public ClassPanel(GraphManager gm) {
        super(gm.getCurrentClassGraph(), gm);
        labelPanel.setGraphType(GraphType.CLASS);
        commentPanel.setGraphType(GraphType.CLASS);

        // setBorder(BorderFactory.createTitledBorder(Translator.getString("AttributeDialog.OntClassAttribute.Text")));
        supClasses = new JList();
        menuList = new JList(new Object[] { basePanel.toString(), labelPanel.toString(), commentPanel.toString(),
                Translator.getString("Instances"), Translator.getString("SuperClasses")});
        menuList.addListSelectionListener(this);
        cardLayout = new CardLayout();
        menuPanel = new JPanel();
        menuPanel.setLayout(cardLayout);
        menuPanel.add(basePanel.toString(), basePanel);
        menuPanel.add(labelPanel.toString(), labelPanel);
        menuPanel.add(commentPanel.toString(), commentPanel);
        menuPanel.add(Translator.getString("Instances"), instanceListScroll);
        menuPanel.add(Translator.getString("SuperClasses"), new JScrollPane(supClasses));

        menuList.setSelectedIndex(0);

        setLayout(new BorderLayout());
        JPanel titlePanel = new JPanel();
        titlePanel.setBackground(new Color(49, 105, 198));
        ImageIcon icon = Utilities.getImageIcon(Translator.getString("ClassEditor.Icon"));
        JLabel titleLabel = new JLabel(Translator.getString("AttributeDialog.OntClassAttribute.Text"), icon,
                SwingConstants.LEFT);
        titleLabel.setForeground(Color.white);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 13));
        titlePanel.setLayout(new BorderLayout());
        titlePanel.add(titleLabel, BorderLayout.WEST);

        add(titlePanel, BorderLayout.NORTH);
        add(menuPanel, BorderLayout.CENTER);
        add(Utilities.createTitledPanel(menuList, "", MENU_WIDTH, MENU_WIDTH), BorderLayout.WEST);
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(1, 3, 5, 5));
        buttonPanel.add(applyButton);
        buttonPanel.add(resetButton);
        buttonPanel.add(cancelButton);
        add(Utilities.createEastPanel(buttonPanel), BorderLayout.SOUTH);
    }

    public void setInstanceList() {
        instanceList.setListData(gmanager.getClassInstanceSet(cell).toArray());
    }

    public void setValue(Set<GraphCell> supCellSet) {
        super.setValue();
        basePanel.setMetaClassList(gmanager.getClassClassList());
        supClasses.setListData(getTargetInfo(supCellSet));
    }
}
