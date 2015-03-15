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

package net.sourceforge.mr3.data;

import java.util.*;

import net.sourceforge.mr3.data.MR3Constants.*;

import com.hp.hpl.jena.rdf.model.*;

/**
 * @author Takeshi Morita
 */
public class HistoryData {

    private Date savedTime;
    private Model projectModel;
    private HistoryType historyType;

    /*
     * 上記の他に，どのリソースを追加したとか，何を削除したなどの情報も 保存できるようにすべきだが，とりあえずは，履歴のタイプのみで
     * あと，メタモデル管理に関わる変更かどうかも区別したい．
     */

    public HistoryData(HistoryType type, Model model) {
        savedTime = Calendar.getInstance().getTime();
        projectModel = model;
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
