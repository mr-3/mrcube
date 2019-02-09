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

package org.mrcube.views.class_editor;

import org.jgraph.graph.GraphCell;
import org.mrcube.MR3;
import org.mrcube.jgraph.GraphManager;
import org.mrcube.models.MR3Constants;
import org.mrcube.models.MR3Constants.GraphType;
import org.mrcube.utils.Translator;
import org.mrcube.utils.Utilities;
import org.mrcube.views.OntologyPanel;

import javax.swing.*;
import java.awt.*;
import java.util.Set;

/*
 *
 * @author Takeshi Morita
 *
 */
public class ClassPanel extends OntologyPanel {

    private final JList supClasses;

    public ClassPanel(GraphManager gm) {
        super(gm.getCurrentClassGraph(), gm);
        labelPanel.setGraphType(GraphType.CLASS);
        commentPanel.setGraphType(GraphType.CLASS);

        // setBorder(BorderFactory.createTitledBorder(Translator.getString("AttributeDialog.OntClassAttribute.Text")));
        supClasses = new JList();
        menuList = new JList(new Object[]{basePanel.toString(), labelPanel.toString(), commentPanel.toString(),
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
        titlePanel.setBackground(MR3Constants.TITLE_BACKGROUND_COLOR);
        ImageIcon icon = Utilities.getImageIcon(Translator.getString("ClassEditor.Icon"));
        JLabel titleLabel = new JLabel(Translator.getString("AttributeDialog.OntClassAttribute.Text"),
                icon, SwingConstants.LEFT);
        titleLabel.setForeground(Color.white);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, MR3Constants.TITLE_FONT_SIZE));
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
