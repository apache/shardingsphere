/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.infra.binder.doris.bind.type;

import com.cedarsoftware.util.CaseInsensitiveMap;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import org.apache.shardingsphere.infra.binder.engine.segment.dml.from.context.TableSegmentBinderContext;
import org.apache.shardingsphere.infra.binder.engine.segment.dml.from.type.SimpleTableSegmentBinder;
import org.apache.shardingsphere.infra.binder.engine.statement.SQLStatementBinder;
import org.apache.shardingsphere.infra.binder.engine.statement.SQLStatementBinderContext;
import org.apache.shardingsphere.infra.binder.engine.statement.SQLStatementCopyUtils;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.doris.dal.DorisAnalyzeTableStatement;

/**
 * Doris analyze table statement binder for Doris.
 */
public final class DorisAnalyzeTableStatementBinder implements SQLStatementBinder<DorisAnalyzeTableStatement> {
    
    @Override
    public DorisAnalyzeTableStatement bind(final DorisAnalyzeTableStatement sqlStatement, final SQLStatementBinderContext binderContext) {
        if (null == sqlStatement.getTable()) {
            return sqlStatement;
        }
        Multimap<CaseInsensitiveMap.CaseInsensitiveString, TableSegmentBinderContext> tableBinderContexts = LinkedHashMultimap.create();
        SimpleTableSegment boundTable = SimpleTableSegmentBinder.bind(sqlStatement.getTable(), binderContext, tableBinderContexts);
        return copy(sqlStatement, boundTable);
    }
    
    private DorisAnalyzeTableStatement copy(final DorisAnalyzeTableStatement sqlStatement, final SimpleTableSegment boundTable) {
        DorisAnalyzeTableStatement result = new DorisAnalyzeTableStatement(sqlStatement.getDatabaseType(), boundTable, sqlStatement.getDatabase(), sqlStatement.getColumns(), sqlStatement.isSync(),
                sqlStatement.getSampleType().orElse(null), sqlStatement.getSampleValue().orElse(null));
        SQLStatementCopyUtils.copyAttributes(sqlStatement, result);
        return result;
    }
}
