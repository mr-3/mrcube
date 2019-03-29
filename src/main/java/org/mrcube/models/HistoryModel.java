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

package org.mrcube.models;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.mrcube.models.MR3Constants.HistoryType;

import java.util.Calendar;
import java.util.Date;

/**
 * @author Takeshi Morita
 */
public class HistoryModel {

    private final Date savedTime;
    private final Model projectModel;
    private final HistoryType historyType;

    public HistoryModel(HistoryType type, Model model) {
        savedTime = Calendar.getInstance().getTime();
        // TODO: プロジェクトを開くと過去のHistoryModelのprojectModelが空になる不具合がある
        projectModel = ModelFactory.createModelForGraph(model.getGraph());
        historyType = type;
    }

    public Model getProjectModel() {
        return projectModel;
    }

    public Date getDate() {
        return savedTime;
    }

    public HistoryType getHistoryType() {
        return historyType;
    }
}
