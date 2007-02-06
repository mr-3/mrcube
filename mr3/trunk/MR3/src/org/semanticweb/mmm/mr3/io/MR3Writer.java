/*
 * @(#) MR3Writer.java
 *
 *
 * Copyright (C) 2003 The MMM Project
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 */

package org.semanticweb.mmm.mr3.io;

import org.semanticweb.mmm.mr3.jgraph.*;
import org.semanticweb.mmm.mr3.util.*;

import com.hp.hpl.jena.rdf.model.*;

/**
 * @author takeshi morita
 */
public class MR3Writer {

    private GraphManager gmanager;
    private MR3Generator mr3Generator;

    public MR3Writer(GraphManager gm) {
        gmanager = gm;
        mr3Generator = new MR3Generator(gmanager);
    }

    public Model getRDFModel() {
        return mr3Generator.getRDFModel(false);
    }

    public Model getSelectedRDFModel() {
        return mr3Generator.getRDFModel(true);
    }

    public Model getRDFSModel() {
        Model model = ModelFactory.createDefaultModel();
        try {
            model.add(getClassModel());
            model.add(getPropertyModel());
        } catch (RDFException e) {
            e.printStackTrace();
        }

        return model;
    }

    public Model getSelectedRDFSModel() {
        Model model = ModelFactory.createDefaultModel();
        try {
            model.add(getSelectedClassModel());
            model.add(getSelectedPropertyModel());
        } catch (RDFException e) {
            e.printStackTrace();
        }

        return model;
    }

    public Model getClassModel() {
        return mr3Generator.getClassModel(false);
    }

    public Model getSelectedClassModel() {
        return mr3Generator.getClassModel(true);
    }

    public Model getPropertyModel() {
        return mr3Generator.getPropertyModel(false);
    }

    public Model getSelectedPropertyModel() {
        return mr3Generator.getPropertyModel(true);
    }

    /**
     * 1. RDFモデルからリテラルを含むStatementの集合を得る -> literalModel 
     * 2. RDFSモデルを得る 
     * 3.プロジェクトモデルを得る． 
     * 4. リテラルモデルを削除する．
     */
    public Model getProjectModel() {
        Model exportModel = getRDFModel();
        try {
            ProjectManager projectManager = new ProjectManager(gmanager);
            Model literalModel = projectManager.getLiteralModel(exportModel);
            exportModel.add(getRDFSModel());
            exportModel.add(projectManager.getProjectModel());
            exportModel.remove(literalModel);
        } catch (RDFException e) {
            e.printStackTrace();
        }
        return exportModel;
    }
}
