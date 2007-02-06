/*
 * @(#)  2004/11/11
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

package org.semanticweb.mmm.mr3.data;

import java.util.*;

import org.semanticweb.mmm.mr3.data.MR3Constants.*;

import com.hp.hpl.jena.rdf.model.*;

/**
 * @author takeshi morita
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
