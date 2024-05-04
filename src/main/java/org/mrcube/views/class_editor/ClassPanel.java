/*
 * Project Name: MR^3 (Meta-Model Management based on RDFs Revision Reflection)
 * Project Website: http://mrcube.org/
 *
 * Copyright (C) 2003-2022 Takeshi Morita. All rights reserved.
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
import org.mrcube.jgraph.GraphManager;
import org.mrcube.models.MR3Constants;
import org.mrcube.models.MR3Constants.GraphType;
import org.mrcube.utils.Translator;
import org.mrcube.utils.Utilities;
import org.mrcube.views.OntologyPanel;
import org.mrcube.views.common.ResourceListCellRenderer;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.util.Set;

/*
 *
 * @author Takeshi Morita
 *
 */
public class ClassPanel extends OntologyPanel {

    private final JList superClassJList;

    public ClassPanel(GraphManager gm) {
        super(gm.getClassGraph(), gm);
        labelPanel.setGraphType(GraphType.Class);
        commentPanel.setGraphType(GraphType.Class);

        // setBorder(BorderFactory.createTitledBorder(Translator.getString("AttributeDialog.OntClassAttribute.Text")));
        superClassJList = new JList();
        superClassJList.setCellRenderer(new ResourceListCellRenderer());
        superClassJList.addListSelectionListener(new EditRDFSClassAction());

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
        menuPanel.add(Translator.getString("SuperClasses"), new JScrollPane(superClassJList));

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
        instanceJList.setListData(gmanager.getClassInstanceSet(cell).toArray());
    }

    class EditRDFSClassAction implements ListSelectionListener {
        public void valueChanged(ListSelectionEvent e) {
            Object cell = superClassJList.getSelectedValue();
            gmanager.selectClassCell(cell);
        }
    }

    public void setValue(Set<GraphCell> supCellSet) {
        super.setValue();
        basePanel.setMetaClassList(gmanager.getClassClassList());
        superClassJList.setListData(getTargetInfo(supCellSet));
    }
}
