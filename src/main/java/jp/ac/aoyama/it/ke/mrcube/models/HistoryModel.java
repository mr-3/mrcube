/*
 * Project Name: MR^3 (Meta-Model Management based on RDFs Revision Reflection)
 * Project Website: https://mr-3.github.io/
 *
 * Copyright (C) 2003-2025 Takeshi Morita. All rights reserved.
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

package jp.ac.aoyama.it.ke.mrcube.models;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import jp.ac.aoyama.it.ke.mrcube.models.MR3Constants.HistoryType;

import java.util.Calendar;
import java.util.Date;

/**
 * @author Takeshi Morita
 */
public class HistoryModel {

    private final Date savedTime;
    private final Model projectModel;
    private final HistoryType historyType;
    private final String message;

    public HistoryModel(HistoryType type, Model model, String message) {
        savedTime = Calendar.getInstance().getTime();
        projectModel = ModelFactory.createDefaultModel().add(model);
        historyType = type;
        this.message = message;
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

    public String getMessage() {
        return message;
    }
}
